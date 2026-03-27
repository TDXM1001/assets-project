package com.ruoyi.system.service.asset.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.annotation.DataScope;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.asset.AssetInfo;
import com.ruoyi.system.domain.asset.AssetRepairOrder;
import com.ruoyi.system.domain.asset.AssetRepairOrderItem;
import com.ruoyi.system.mapper.asset.AssetInfoMapper;
import com.ruoyi.system.mapper.asset.AssetRepairOrderMapper;
import com.ruoyi.system.service.asset.IAssetEventLogService;
import com.ruoyi.system.service.asset.IAssetInfoService;
import com.ruoyi.system.service.asset.IAssetRepairOrderService;

/**
 * 资产维修单服务实现
 *
 * 兼容说明：
 * 1. 单头上的 assetId / assetCode / faultDesc 仍然保留，旧页面和历史报表不会被打断。
 * 2. 真正的流程处理统一以 itemList 为准，多资产维修会逐条更新资产状态和事件流水。
 */
@Service
public class AssetRepairOrderServiceImpl implements IAssetRepairOrderService
{
    private static final String STATUS_DRAFT = "DRAFT";
    private static final String STATUS_SUBMITTED = "SUBMITTED";
    private static final String STATUS_APPROVED = "APPROVED";
    private static final String STATUS_REJECTED = "REJECTED";
    private static final String STATUS_FINISHED = "FINISHED";
    private static final String STATUS_CANCELLED = "CANCELED";

    private static final String RESULT_RESUME_USE = "RESUME_USE";
    private static final String RESULT_TO_IDLE = "TO_IDLE";
    private static final String RESULT_SUGGEST_DISPOSAL = "SUGGEST_DISPOSAL";

    private static final String ASSET_STATUS_IN_USE = "IN_USE";
    private static final String ASSET_STATUS_IDLE = "IDLE";
    private static final String ASSET_STATUS_REPAIRING = "REPAIRING";
    private static final String ASSET_STATUS_PENDING_DISPOSAL = "PENDING_DISPOSAL";

    private static final String EVENT_TYPE_REPAIR = "REPAIR";
    private static final String BIZ_TYPE_REPAIR = "ASSET_REPAIR";

    @Autowired
    private AssetRepairOrderMapper assetRepairOrderMapper;

    @Autowired
    private AssetInfoMapper assetInfoMapper;

    @Autowired
    private IAssetInfoService assetInfoService;

    @Autowired
    private IAssetEventLogService assetEventLogService;

    @Autowired
    @Lazy
    private IAssetRepairOrderService assetRepairOrderServiceProxy;

    @Override
    @DataScope(deptAlias = "scope_dept", userAlias = "scope_user")
    public List<AssetRepairOrder> selectAssetRepairOrderList(AssetRepairOrder assetRepairOrder)
    {
        return assetRepairOrderMapper.selectAssetRepairOrderList(assetRepairOrder);
    }

    @Override
    public AssetRepairOrder selectAssetRepairOrderById(Long repairId)
    {
        AssetRepairOrder query = new AssetRepairOrder();
        query.setRepairId(repairId);
        List<AssetRepairOrder> scopedOrders = assetRepairOrderServiceProxy.selectAssetRepairOrderList(query);
        AssetRepairOrder order = scopedOrders.isEmpty() ? null : scopedOrders.get(0);
        if (order == null)
        {
            return null;
        }

        List<AssetRepairOrderItem> itemList = assetRepairOrderMapper.selectAssetRepairOrderItemsByRepairId(repairId);
        if (itemList == null || itemList.isEmpty())
        {
            itemList = buildCompatItems(order);
        }
        else
        {
            itemList = cloneItems(itemList);
        }
        order.setItemList(itemList);
        order.setItemCount(itemList.size());
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertAssetRepairOrder(AssetRepairOrder assetRepairOrder)
    {
        List<AssetRepairOrderItem> draftItems = prepareDraftItems(assetRepairOrder, null);
        buildInsertDefaults(assetRepairOrder, draftItems);
        int rows = assetRepairOrderMapper.insertAssetRepairOrder(assetRepairOrder);
        saveRepairItems(assetRepairOrder.getRepairId(), draftItems, assetRepairOrder.getUpdateBy());
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateAssetRepairOrder(AssetRepairOrder assetRepairOrder)
    {
        AssetRepairOrder dbOrder = requireRepair(assetRepairOrder.getRepairId());
        ensureStatus(dbOrder.getRepairStatus(), STATUS_DRAFT, STATUS_REJECTED);

        List<AssetRepairOrderItem> draftItems = prepareDraftItems(assetRepairOrder, dbOrder);
        fillUpdateDefaults(assetRepairOrder, dbOrder, draftItems);
        int rows = assetRepairOrderMapper.updateAssetRepairOrder(assetRepairOrder);
        saveRepairItems(dbOrder.getRepairId(), draftItems, assetRepairOrder.getUpdateBy());
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteAssetRepairOrderByIds(Long[] repairIds)
    {
        for (Long repairId : repairIds)
        {
            AssetRepairOrder dbOrder = requireRepair(repairId);
            ensureStatus(dbOrder.getRepairStatus(), STATUS_DRAFT, STATUS_REJECTED, STATUS_CANCELLED);
        }
        assetRepairOrderMapper.deleteAssetRepairOrderItemsByRepairIds(repairIds);
        return assetRepairOrderMapper.deleteAssetRepairOrderByIds(repairIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int submitAssetRepairOrder(Long repairId, String updateBy)
    {
        AssetRepairOrder dbOrder = requireRepair(repairId);
        ensureStatus(dbOrder.getRepairStatus(), STATUS_DRAFT, STATUS_REJECTED);
        for (AssetRepairOrderItem item : getCompatibleItems(dbOrder))
        {
            ensureNoActiveRepair(item.getAssetId(), dbOrder.getRepairId());
        }
        return assetRepairOrderMapper.updateAssetRepairOrderStatus(buildStatusUpdate(repairId, STATUS_SUBMITTED, updateBy));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int approveAssetRepairOrder(Long repairId, String remark, Long approveUserId, String updateBy)
    {
        AssetRepairOrder dbOrder = requireRepair(repairId);
        ensureStatus(dbOrder.getRepairStatus(), STATUS_SUBMITTED);

        List<AssetRepairOrderItem> dbItems = getCompatibleItems(dbOrder);
        for (AssetRepairOrderItem item : dbItems)
        {
            AssetInfo beforeAsset = requireVisibleAsset(item.getAssetId());
            if (StringUtils.equals(beforeAsset.getAssetStatus(), ASSET_STATUS_REPAIRING))
            {
                throw new ServiceException("资产[" + beforeAsset.getAssetCode() + "]已经处于维修中，请勿重复审批");
            }

            AssetInfo afterAsset = copyAssetSnapshot(beforeAsset);
            afterAsset.setAssetStatus(ASSET_STATUS_REPAIRING);
            afterAsset.setUpdateBy(updateBy);
            assetInfoMapper.updateAssetSnapshot(afterAsset);

            item.setBeforeStatus(beforeAsset.getAssetStatus());
            item.setAfterStatus(ASSET_STATUS_REPAIRING);
            item.setUpdateBy(updateBy);
            assetEventLogService.recordAssetEvent(
                beforeAsset.getAssetId(),
                EVENT_TYPE_REPAIR,
                repairId,
                BIZ_TYPE_REPAIR,
                beforeAsset,
                afterAsset,
                buildStartRepairEventDesc(dbOrder, item, beforeAsset),
                SecurityUtils.getUserId());
        }

        saveRepairItems(repairId, dbItems, updateBy);
        syncOrderHeaderFromItems(dbOrder, dbItems);
        AssetRepairOrder update = buildStatusUpdate(repairId, STATUS_APPROVED, updateBy);
        update.setApproveUserId(approveUserId);
        update.setApproveTime(new Date());
        update.setApproveResult(STATUS_APPROVED);
        update.setSendRepairTime(new Date());
        update.setBeforeStatus(dbOrder.getBeforeStatus());
        update.setAfterStatus(ASSET_STATUS_REPAIRING);
        if (StringUtils.isNotBlank(remark))
        {
            update.setRemark(remark);
        }
        return assetRepairOrderMapper.updateAssetRepairOrderStatus(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int rejectAssetRepairOrder(Long repairId, String remark, Long approveUserId, String updateBy)
    {
        AssetRepairOrder dbOrder = requireRepair(repairId);
        ensureStatus(dbOrder.getRepairStatus(), STATUS_SUBMITTED);
        AssetRepairOrder update = buildStatusUpdate(repairId, STATUS_REJECTED, updateBy);
        update.setApproveUserId(approveUserId);
        update.setApproveTime(new Date());
        update.setApproveResult(STATUS_REJECTED);
        update.setRemark(remark);
        return assetRepairOrderMapper.updateAssetRepairOrderStatus(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int finishAssetRepairOrder(Long repairId, AssetRepairOrder assetRepairOrder, String updateBy)
    {
        AssetRepairOrder dbOrder = requireRepair(repairId);
        ensureStatus(dbOrder.getRepairStatus(), STATUS_APPROVED);

        AssetRepairOrder finishPayload = assetRepairOrder == null ? new AssetRepairOrder() : assetRepairOrder;
        List<AssetRepairOrderItem> finishItems = mergeFinishItems(dbOrder, finishPayload);
        if (finishItems.isEmpty())
        {
            throw new ServiceException("当前维修单没有资产明细，无法完成维修");
        }

        for (AssetRepairOrderItem item : finishItems)
        {
            AssetInfo beforeAsset = requireVisibleAsset(item.getAssetId());
            if (!StringUtils.equals(beforeAsset.getAssetStatus(), ASSET_STATUS_REPAIRING))
            {
                throw new ServiceException("资产[" + beforeAsset.getAssetCode() + "]未处于维修中，无法完成维修");
            }

            String normalizedResultType = normalizeResultType(item.getResultType());
            String resolvedAfterStatus = resolveAfterStatus(normalizedResultType);
            AssetInfo afterAsset = copyAssetSnapshot(beforeAsset);
            afterAsset.setAssetStatus(resolvedAfterStatus);
            afterAsset.setUpdateBy(updateBy);
            assetInfoMapper.updateAssetSnapshot(afterAsset);

            item.setBeforeStatus(beforeAsset.getAssetStatus());
            item.setResultType(normalizedResultType);
            item.setAfterStatus(resolvedAfterStatus);
            item.setReworkFlag(normalizeReworkFlag(item.getReworkFlag(), dbOrder.getReworkFlag()));
            item.setUpdateBy(updateBy);
            assetEventLogService.recordAssetEvent(
                beforeAsset.getAssetId(),
                EVENT_TYPE_REPAIR,
                repairId,
                BIZ_TYPE_REPAIR,
                beforeAsset,
                afterAsset,
                buildFinishRepairEventDesc(dbOrder, item, afterAsset),
                SecurityUtils.getUserId());
        }

        saveRepairItems(repairId, finishItems, updateBy);
        syncOrderHeaderFromItems(dbOrder, finishItems);
        AssetRepairOrder update = buildStatusUpdate(repairId, STATUS_FINISHED, updateBy);
        update.setFinishTime(finishPayload.getFinishTime() == null ? new Date() : finishPayload.getFinishTime());
        update.setResultType(dbOrder.getResultType());
        update.setVendorName(StringUtils.defaultIfBlank(finishPayload.getVendorName(), dbOrder.getVendorName()));
        update.setRepairCost(resolveSummaryRepairCost(finishItems));
        update.setDowntimeHours(resolveSummaryDowntimeHours(finishItems));
        update.setReworkFlag(resolveSummaryReworkFlag(finishItems, dbOrder.getReworkFlag()));
        update.setBeforeStatus(dbOrder.getBeforeStatus());
        update.setAfterStatus(dbOrder.getAfterStatus());
        update.setRemark(StringUtils.defaultIfBlank(finishPayload.getRemark(), dbOrder.getRemark()));
        return assetRepairOrderMapper.updateAssetRepairOrderStatus(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cancelAssetRepairOrder(Long repairId, String updateBy)
    {
        AssetRepairOrder dbOrder = requireRepair(repairId);
        ensureStatus(dbOrder.getRepairStatus(), STATUS_DRAFT, STATUS_SUBMITTED, STATUS_REJECTED, STATUS_APPROVED);

        if (StringUtils.equals(dbOrder.getRepairStatus(), STATUS_APPROVED))
        {
            for (AssetRepairOrderItem item : getCompatibleItems(dbOrder))
            {
                AssetInfo beforeAsset = requireVisibleAsset(item.getAssetId());
                if (StringUtils.equals(beforeAsset.getAssetStatus(), ASSET_STATUS_REPAIRING))
                {
                    AssetInfo afterAsset = copyAssetSnapshot(beforeAsset);
                    afterAsset.setAssetStatus(StringUtils.defaultIfBlank(item.getBeforeStatus(), ASSET_STATUS_IN_USE));
                    afterAsset.setUpdateBy(updateBy);
                    assetInfoMapper.updateAssetSnapshot(afterAsset);

                    assetEventLogService.recordAssetEvent(
                        beforeAsset.getAssetId(),
                        EVENT_TYPE_REPAIR,
                        repairId,
                        BIZ_TYPE_REPAIR,
                        beforeAsset,
                        afterAsset,
                        "维修单已取消，资产[" + StringUtils.defaultString(item.getAssetCode(), String.valueOf(item.getAssetId()))
                            + "]状态恢复为" + afterAsset.getAssetStatus(),
                        SecurityUtils.getUserId());
                }
            }
        }

        return assetRepairOrderMapper.updateAssetRepairOrderStatus(buildStatusUpdate(repairId, STATUS_CANCELLED, updateBy));
    }

    /**
     * 草稿阶段允许前端继续传单资产字段，但服务层统一整理成明细，避免后续流程分叉。
     */
    private List<AssetRepairOrderItem> prepareDraftItems(AssetRepairOrder targetOrder, AssetRepairOrder sourceOrder)
    {
        List<AssetRepairOrderItem> draftItems = normalizeDraftItems(targetOrder, sourceOrder);
        if (draftItems.isEmpty())
        {
            throw new ServiceException("维修单至少需要选择一条资产明细");
        }

        Set<Long> assetIds = new LinkedHashSet<>();
        List<AssetRepairOrderItem> preparedItems = new ArrayList<>();
        int sort = 1;
        for (AssetRepairOrderItem item : draftItems)
        {
            if (item.getAssetId() == null)
            {
                throw new ServiceException("维修明细缺少资产ID");
            }
            if (!assetIds.add(item.getAssetId()))
            {
                throw new ServiceException("同一维修单内不允许重复选择相同资产");
            }

            AssetInfo assetInfo = requireVisibleAsset(item.getAssetId());
            ensureNoActiveRepair(assetInfo.getAssetId(), sourceOrder == null ? null : sourceOrder.getRepairId());

            AssetRepairOrderItem preparedItem = cloneItem(item);
            preparedItem.setAssetId(assetInfo.getAssetId());
            preparedItem.setAssetCode(assetInfo.getAssetCode());
            preparedItem.setAssetName(assetInfo.getAssetName());
            preparedItem.setBeforeStatus(StringUtils.defaultIfBlank(preparedItem.getBeforeStatus(), assetInfo.getAssetStatus()));
            preparedItem.setAfterStatus(StringUtils.defaultIfBlank(preparedItem.getAfterStatus(), preparedItem.getBeforeStatus()));
            preparedItem.setFaultDesc(resolveFaultDesc(preparedItem.getFaultDesc(), targetOrder, sourceOrder));
            preparedItem.setSortOrder(sort++);
            preparedItem.setStatus(StringUtils.defaultIfBlank(preparedItem.getStatus(), "0"));
            preparedItem.setDelFlag("0");
            preparedItems.add(preparedItem);
        }
        return preparedItems;
    }

    private List<AssetRepairOrderItem> normalizeDraftItems(AssetRepairOrder targetOrder, AssetRepairOrder sourceOrder)
    {
        if (targetOrder != null && targetOrder.getItemList() != null && !targetOrder.getItemList().isEmpty())
        {
            return cloneItems(targetOrder.getItemList());
        }
        if (targetOrder != null && targetOrder.getAssetId() != null)
        {
            return buildDraftSourceItems(targetOrder);
        }
        if (sourceOrder != null && sourceOrder.getItemList() != null && !sourceOrder.getItemList().isEmpty())
        {
            return cloneItems(sourceOrder.getItemList());
        }
        if (sourceOrder != null && sourceOrder.getAssetId() != null)
        {
            return buildDraftSourceItems(sourceOrder);
        }
        return Collections.emptyList();
    }

    private List<AssetRepairOrderItem> buildDraftSourceItems(AssetRepairOrder order)
    {
        AssetRepairOrderItem item = new AssetRepairOrderItem();
        item.setRepairItemId(null);
        item.setAssetId(order.getAssetId());
        item.setAssetCode(order.getAssetCode());
        item.setAssetName(order.getAssetName());
        item.setBeforeStatus(order.getBeforeStatus());
        item.setAfterStatus(order.getAfterStatus());
        item.setFaultDesc(order.getFaultDesc());
        item.setResultType(order.getResultType());
        item.setRepairCost(order.getRepairCost());
        item.setDowntimeHours(order.getDowntimeHours());
        item.setReworkFlag(order.getReworkFlag());
        item.setRemark(order.getRemark());
        return Collections.singletonList(item);
    }

    /**
     * 新增时统一回写单头快照，继续兼容旧接口和导出模板。
     */
    private void buildInsertDefaults(AssetRepairOrder order, List<AssetRepairOrderItem> draftItems)
    {
        if (StringUtils.isBlank(order.getRepairNo()))
        {
            order.setRepairNo(generateRepairNo());
        }
        if (order.getReportTime() == null)
        {
            order.setReportTime(DateUtils.getNowDate());
        }
        if (StringUtils.isBlank(order.getRepairStatus()))
        {
            order.setRepairStatus(STATUS_DRAFT);
        }
        if (StringUtils.isBlank(order.getStatus()))
        {
            order.setStatus("0");
        }
        if (StringUtils.isBlank(order.getReworkFlag()))
        {
            order.setReworkFlag("0");
        }
        if (order.getAttachmentCount() == null)
        {
            order.setAttachmentCount(0);
        }
        order.setDelFlag("0");
        syncOrderHeaderFromItems(order, draftItems);
    }

    private void fillUpdateDefaults(AssetRepairOrder target, AssetRepairOrder source, List<AssetRepairOrderItem> draftItems)
    {
        if (StringUtils.isBlank(target.getRepairNo()))
        {
            target.setRepairNo(source.getRepairNo());
        }
        if (target.getReportTime() == null)
        {
            target.setReportTime(source.getReportTime());
        }
        if (StringUtils.isBlank(target.getRepairStatus()))
        {
            target.setRepairStatus(source.getRepairStatus());
        }
        if (StringUtils.isBlank(target.getStatus()))
        {
            target.setStatus(source.getStatus());
        }
        if (StringUtils.isBlank(target.getReworkFlag()))
        {
            target.setReworkFlag(source.getReworkFlag());
        }
        if (target.getAttachmentCount() == null)
        {
            target.setAttachmentCount(source.getAttachmentCount());
        }
        syncOrderHeaderFromItems(target, draftItems);
    }

    private void saveRepairItems(Long repairId, List<AssetRepairOrderItem> itemList, String updateBy)
    {
        assetRepairOrderMapper.deleteAssetRepairOrderItemsByRepairId(repairId);
        if (itemList == null || itemList.isEmpty())
        {
            return;
        }

        for (AssetRepairOrderItem item : itemList)
        {
            item.setRepairId(repairId);
            item.setUpdateBy(updateBy);
            if (StringUtils.isBlank(item.getCreateBy()))
            {
                item.setCreateBy(updateBy);
            }
            if (StringUtils.isBlank(item.getStatus()))
            {
                item.setStatus("0");
            }
            if (StringUtils.isBlank(item.getDelFlag()))
            {
                item.setDelFlag("0");
            }
        }
        assetRepairOrderMapper.batchInsertAssetRepairOrderItems(itemList);
    }

    private List<AssetRepairOrderItem> getCompatibleItems(AssetRepairOrder order)
    {
        if (order.getItemList() != null && !order.getItemList().isEmpty())
        {
            return cloneItems(order.getItemList());
        }
        return buildCompatItems(order);
    }

    private List<AssetRepairOrderItem> buildCompatItems(AssetRepairOrder order)
    {
        if (order == null || order.getAssetId() == null)
        {
            return new ArrayList<>();
        }

        AssetRepairOrderItem item = new AssetRepairOrderItem();
        item.setRepairId(order.getRepairId());
        item.setAssetId(order.getAssetId());
        item.setAssetCode(order.getAssetCode());
        item.setAssetName(order.getAssetName());
        item.setFaultDesc(order.getFaultDesc());
        item.setBeforeStatus(order.getBeforeStatus());
        item.setAfterStatus(order.getAfterStatus());
        item.setResultType(order.getResultType());
        item.setRepairCost(order.getRepairCost());
        item.setDowntimeHours(order.getDowntimeHours());
        item.setReworkFlag(order.getReworkFlag());
        item.setSortOrder(1);
        item.setStatus(order.getStatus());
        item.setDelFlag(order.getDelFlag());
        item.setCreateBy(order.getCreateBy());
        item.setCreateTime(order.getCreateTime());
        item.setUpdateBy(order.getUpdateBy());
        item.setUpdateTime(order.getUpdateTime());
        item.setRemark(order.getRemark());
        List<AssetRepairOrderItem> items = new ArrayList<>();
        items.add(item);
        return items;
    }

    private void syncOrderHeaderFromItems(AssetRepairOrder order, List<AssetRepairOrderItem> itemList)
    {
        order.setItemList(cloneItems(itemList));
        order.setItemCount(itemList == null ? 0 : itemList.size());
        if (itemList == null || itemList.isEmpty())
        {
            order.setAssetId(null);
            order.setAssetCode(null);
            order.setAssetName(null);
            order.setBeforeStatus(null);
            order.setAfterStatus(null);
            order.setFaultDesc(null);
            return;
        }

        AssetRepairOrderItem firstItem = itemList.get(0);
        order.setAssetId(firstItem.getAssetId());
        order.setAssetCode(firstItem.getAssetCode());
        order.setAssetName(firstItem.getAssetName());
        order.setBeforeStatus(firstItem.getBeforeStatus());
        order.setAfterStatus(firstItem.getAfterStatus());
        order.setFaultDesc(firstItem.getFaultDesc());
        if (StringUtils.isNotBlank(firstItem.getResultType()))
        {
            order.setResultType(firstItem.getResultType());
        }
    }

    private AssetRepairOrder buildStatusUpdate(Long repairId, String repairStatus, String updateBy)
    {
        AssetRepairOrder update = new AssetRepairOrder();
        update.setRepairId(repairId);
        update.setRepairStatus(repairStatus);
        update.setUpdateBy(updateBy);
        return update;
    }

    private AssetRepairOrder requireRepair(Long repairId)
    {
        AssetRepairOrder order = selectAssetRepairOrderById(repairId);
        if (order == null)
        {
            throw new ServiceException("维修单不存在或无权限访问");
        }
        return order;
    }

    private AssetInfo requireVisibleAsset(Long assetId)
    {
        AssetInfo assetInfo = assetInfoService.selectAssetInfoById(assetId);
        if (assetInfo == null)
        {
            throw new ServiceException("资产不存在或无权限访问");
        }
        if (StringUtils.equals(assetInfo.getAssetStatus(), "DISPOSED"))
        {
            throw new ServiceException("已报废资产不允许发起维修");
        }
        return assetInfo;
    }

    /**
     * 同一资产同一时刻只允许存在一条未结束维修主线，避免状态联动被并发覆盖。
     */
    private void ensureNoActiveRepair(Long assetId, Long excludeRepairId)
    {
        AssetRepairOrder activeOrder = assetRepairOrderMapper.selectActiveAssetRepairOrder(assetId, excludeRepairId);
        if (activeOrder != null)
        {
            throw new ServiceException("资产[" + StringUtils.defaultString(activeOrder.getAssetCode(), String.valueOf(assetId))
                + "]已有未完成维修单，请先处理后再发起新的维修");
        }
    }

    private void ensureStatus(String currentStatus, String... allowStatuses)
    {
        for (String allowStatus : allowStatuses)
        {
            if (StringUtils.equals(currentStatus, allowStatus))
            {
                return;
            }
        }
        throw new ServiceException("当前维修单状态不允许执行该操作");
    }

    private String normalizeResultType(String resultType)
    {
        String normalized = StringUtils.trim(resultType);
        if (StringUtils.equalsAny(normalized, RESULT_RESUME_USE, RESULT_TO_IDLE, RESULT_SUGGEST_DISPOSAL))
        {
            return normalized;
        }
        throw new ServiceException("维修完成结果仅支持 RESUME_USE、TO_IDLE、SUGGEST_DISPOSAL");
    }

    private String normalizeReworkFlag(String reworkFlag, String fallback)
    {
        String normalized = StringUtils.defaultIfBlank(StringUtils.trim(reworkFlag), StringUtils.defaultIfBlank(fallback, "0"));
        if (StringUtils.equalsAny(normalized, "0", "1"))
        {
            return normalized;
        }
        throw new ServiceException("返修标记仅支持 0 或 1");
    }

    private String resolveAfterStatus(String resultType)
    {
        return switch (resultType)
        {
            case RESULT_RESUME_USE -> ASSET_STATUS_IN_USE;
            case RESULT_TO_IDLE -> ASSET_STATUS_IDLE;
            case RESULT_SUGGEST_DISPOSAL -> ASSET_STATUS_PENDING_DISPOSAL;
            default -> ASSET_STATUS_IN_USE;
        };
    }

    private String resolveFaultDesc(String itemFaultDesc, AssetRepairOrder targetOrder, AssetRepairOrder sourceOrder)
    {
        String resolved = StringUtils.defaultIfBlank(itemFaultDesc,
            targetOrder == null ? null : targetOrder.getFaultDesc());
        resolved = StringUtils.defaultIfBlank(resolved, sourceOrder == null ? null : sourceOrder.getFaultDesc());
        if (StringUtils.isBlank(resolved))
        {
            throw new ServiceException("维修明细缺少故障描述");
        }
        return resolved;
    }

    private List<AssetRepairOrderItem> mergeFinishItems(AssetRepairOrder dbOrder, AssetRepairOrder finishPayload)
    {
        List<AssetRepairOrderItem> dbItems = getCompatibleItems(dbOrder);
        Map<String, AssetRepairOrderItem> payloadMap = new LinkedHashMap<>();
        if (finishPayload != null && finishPayload.getItemList() != null)
        {
            for (AssetRepairOrderItem item : finishPayload.getItemList())
            {
                payloadMap.put(buildFinishItemKey(item), item);
            }
        }

        List<AssetRepairOrderItem> mergedItems = new ArrayList<>();
        for (AssetRepairOrderItem dbItem : dbItems)
        {
            AssetRepairOrderItem mergedItem = cloneItem(dbItem);
            AssetRepairOrderItem payloadItem = payloadMap.get(buildFinishItemKey(dbItem));
            if (payloadItem != null)
            {
                if (StringUtils.isNotBlank(payloadItem.getFaultDesc()))
                {
                    mergedItem.setFaultDesc(payloadItem.getFaultDesc());
                }
                if (StringUtils.isNotBlank(payloadItem.getResultType()))
                {
                    mergedItem.setResultType(payloadItem.getResultType());
                }
                if (payloadItem.getRepairCost() != null)
                {
                    mergedItem.setRepairCost(payloadItem.getRepairCost());
                }
                if (payloadItem.getDowntimeHours() != null)
                {
                    mergedItem.setDowntimeHours(payloadItem.getDowntimeHours());
                }
                if (StringUtils.isNotBlank(payloadItem.getReworkFlag()))
                {
                    mergedItem.setReworkFlag(payloadItem.getReworkFlag());
                }
                if (StringUtils.isNotBlank(payloadItem.getRemark()))
                {
                    mergedItem.setRemark(payloadItem.getRemark());
                }
            }

            mergedItem.setResultType(StringUtils.defaultIfBlank(mergedItem.getResultType(), finishPayload.getResultType()));
            mergedItem.setRepairCost(mergedItem.getRepairCost() == null ? finishPayload.getRepairCost() : mergedItem.getRepairCost());
            mergedItem.setDowntimeHours(mergedItem.getDowntimeHours() == null ? finishPayload.getDowntimeHours() : mergedItem.getDowntimeHours());
            mergedItem.setReworkFlag(StringUtils.defaultIfBlank(mergedItem.getReworkFlag(), finishPayload.getReworkFlag()));
            mergedItem.setRemark(StringUtils.defaultIfBlank(mergedItem.getRemark(), finishPayload.getRemark()));
            mergedItems.add(mergedItem);
        }
        return mergedItems;
    }

    private String buildFinishItemKey(AssetRepairOrderItem item)
    {
        if (item == null)
        {
            return "";
        }
        if (item.getRepairItemId() != null)
        {
            return "RID:" + item.getRepairItemId();
        }
        return "AID:" + item.getAssetId();
    }

    private List<AssetRepairOrderItem> cloneItems(List<AssetRepairOrderItem> sourceItems)
    {
        List<AssetRepairOrderItem> clonedItems = new ArrayList<>();
        if (sourceItems == null)
        {
            return clonedItems;
        }
        for (AssetRepairOrderItem sourceItem : sourceItems)
        {
            clonedItems.add(cloneItem(sourceItem));
        }
        return clonedItems;
    }

    private AssetRepairOrderItem cloneItem(AssetRepairOrderItem source)
    {
        AssetRepairOrderItem target = new AssetRepairOrderItem();
        if (source == null)
        {
            return target;
        }
        target.setRepairItemId(source.getRepairItemId());
        target.setRepairId(source.getRepairId());
        target.setAssetId(source.getAssetId());
        target.setAssetCode(source.getAssetCode());
        target.setAssetName(source.getAssetName());
        target.setFaultDesc(source.getFaultDesc());
        target.setBeforeStatus(source.getBeforeStatus());
        target.setAfterStatus(source.getAfterStatus());
        target.setResultType(source.getResultType());
        target.setRepairCost(source.getRepairCost());
        target.setDowntimeHours(source.getDowntimeHours());
        target.setReworkFlag(source.getReworkFlag());
        target.setSortOrder(source.getSortOrder());
        target.setStatus(source.getStatus());
        target.setDelFlag(source.getDelFlag());
        target.setCreateBy(source.getCreateBy());
        target.setCreateTime(source.getCreateTime());
        target.setUpdateBy(source.getUpdateBy());
        target.setUpdateTime(source.getUpdateTime());
        target.setRemark(source.getRemark());
        return target;
    }

    private BigDecimal resolveSummaryRepairCost(List<AssetRepairOrderItem> itemList)
    {
        BigDecimal total = BigDecimal.ZERO;
        boolean hasValue = false;
        for (AssetRepairOrderItem item : itemList)
        {
            if (item.getRepairCost() != null)
            {
                total = total.add(item.getRepairCost());
                hasValue = true;
            }
        }
        return hasValue ? total : null;
    }

    private BigDecimal resolveSummaryDowntimeHours(List<AssetRepairOrderItem> itemList)
    {
        BigDecimal total = BigDecimal.ZERO;
        boolean hasValue = false;
        for (AssetRepairOrderItem item : itemList)
        {
            if (item.getDowntimeHours() != null)
            {
                total = total.add(item.getDowntimeHours());
                hasValue = true;
            }
        }
        return hasValue ? total : null;
    }

    private String resolveSummaryReworkFlag(List<AssetRepairOrderItem> itemList, String fallback)
    {
        for (AssetRepairOrderItem item : itemList)
        {
            if (StringUtils.equals(item.getReworkFlag(), "1"))
            {
                return "1";
            }
        }
        return normalizeReworkFlag(fallback, "0");
    }

    private String buildStartRepairEventDesc(AssetRepairOrder order, AssetRepairOrderItem item, AssetInfo beforeAsset)
    {
        return "维修单[" + order.getRepairNo() + "]审批通过，资产[" + StringUtils.defaultString(item.getAssetCode(),
            String.valueOf(item.getAssetId())) + "]状态由" + beforeAsset.getAssetStatus() + "变更为维修中";
    }

    private String buildFinishRepairEventDesc(AssetRepairOrder order, AssetRepairOrderItem item, AssetInfo afterAsset)
    {
        String resultLabel = switch (item.getResultType())
        {
            case RESULT_RESUME_USE -> "恢复在用";
            case RESULT_TO_IDLE -> "转闲置";
            case RESULT_SUGGEST_DISPOSAL -> "建议报废";
            default -> item.getResultType();
        };
        return "维修单[" + order.getRepairNo() + "]完成，资产[" + StringUtils.defaultString(item.getAssetCode(),
            String.valueOf(item.getAssetId())) + "]处理结果为" + resultLabel + "，资产状态更新为" + afterAsset.getAssetStatus();
    }

    private String generateRepairNo()
    {
        return "RP" + DateUtils.dateTimeNow("yyyyMMddHHmmss") + ThreadLocalRandom.current().nextInt(100, 1000);
    }

    private AssetInfo copyAssetSnapshot(AssetInfo source)
    {
        AssetInfo target = new AssetInfo();
        target.setAssetId(source.getAssetId());
        target.setAssetCode(source.getAssetCode());
        target.setAssetName(source.getAssetName());
        target.setCategoryId(source.getCategoryId());
        target.setBrand(source.getBrand());
        target.setModel(source.getModel());
        target.setSpecification(source.getSpecification());
        target.setSerialNo(source.getSerialNo());
        target.setAssetStatus(source.getAssetStatus());
        target.setAssetSource(source.getAssetSource());
        target.setUseOrgDeptId(source.getUseOrgDeptId());
        target.setManageDeptId(source.getManageDeptId());
        target.setCurrentUserId(source.getCurrentUserId());
        target.setCurrentLocationId(source.getCurrentLocationId());
        target.setPurchaseDate(source.getPurchaseDate());
        target.setInboundDate(source.getInboundDate());
        target.setStartUseDate(source.getStartUseDate());
        target.setOriginalValue(source.getOriginalValue());
        target.setResidualValue(source.getResidualValue());
        target.setWarrantyExpireDate(source.getWarrantyExpireDate());
        target.setSupplierName(source.getSupplierName());
        target.setQrCode(source.getQrCode());
        target.setVersionNo(source.getVersionNo());
        target.setTemplateVersion(source.getTemplateVersion());
        target.setExtraFieldsJson(source.getExtraFieldsJson());
        target.setStatus(source.getStatus());
        target.setDelFlag(source.getDelFlag());
        target.setRemark(source.getRemark());
        return target;
    }
}
