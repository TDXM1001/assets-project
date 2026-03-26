package com.ruoyi.system.service.asset.impl;

import java.util.Date;
import java.util.List;
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
import com.ruoyi.system.mapper.asset.AssetInfoMapper;
import com.ruoyi.system.mapper.asset.AssetRepairOrderMapper;
import com.ruoyi.system.service.asset.IAssetEventLogService;
import com.ruoyi.system.service.asset.IAssetInfoService;
import com.ruoyi.system.service.asset.IAssetRepairOrderService;

/**
 * 资产维修单服务实现
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
        return scopedOrders.isEmpty() ? null : scopedOrders.get(0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertAssetRepairOrder(AssetRepairOrder assetRepairOrder)
    {
        AssetInfo assetInfo = requireVisibleAsset(assetRepairOrder.getAssetId());
        ensureNoActiveRepair(assetRepairOrder.getAssetId(), null);
        buildInsertDefaults(assetRepairOrder, assetInfo);
        return assetRepairOrderMapper.insertAssetRepairOrder(assetRepairOrder);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateAssetRepairOrder(AssetRepairOrder assetRepairOrder)
    {
        AssetRepairOrder dbOrder = requireRepair(assetRepairOrder.getRepairId());
        ensureStatus(dbOrder.getRepairStatus(), STATUS_DRAFT, STATUS_REJECTED);
        AssetInfo assetInfo = requireVisibleAsset(assetRepairOrder.getAssetId() == null ? dbOrder.getAssetId() : assetRepairOrder.getAssetId());
        ensureNoActiveRepair(assetInfo.getAssetId(), dbOrder.getRepairId());
        fillUpdateDefaults(assetRepairOrder, dbOrder, assetInfo);
        return assetRepairOrderMapper.updateAssetRepairOrder(assetRepairOrder);
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
        return assetRepairOrderMapper.deleteAssetRepairOrderByIds(repairIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int submitAssetRepairOrder(Long repairId, String updateBy)
    {
        AssetRepairOrder dbOrder = requireRepair(repairId);
        ensureStatus(dbOrder.getRepairStatus(), STATUS_DRAFT, STATUS_REJECTED);
        ensureNoActiveRepair(dbOrder.getAssetId(), dbOrder.getRepairId());
        return assetRepairOrderMapper.updateAssetRepairOrderStatus(buildStatusUpdate(repairId, STATUS_SUBMITTED, updateBy));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int approveAssetRepairOrder(Long repairId, String remark, Long approveUserId, String updateBy)
    {
        AssetRepairOrder dbOrder = requireRepair(repairId);
        ensureStatus(dbOrder.getRepairStatus(), STATUS_SUBMITTED);

        AssetInfo beforeAsset = requireVisibleAsset(dbOrder.getAssetId());
        if (StringUtils.equals(beforeAsset.getAssetStatus(), ASSET_STATUS_REPAIRING))
        {
            throw new ServiceException("当前资产已经处于维修中，请勿重复审批维修单");
        }

        AssetInfo afterAsset = copyAssetSnapshot(beforeAsset);
        afterAsset.setAssetStatus(ASSET_STATUS_REPAIRING);
        afterAsset.setUpdateBy(updateBy);
        assetInfoMapper.updateAssetSnapshot(afterAsset);

        AssetRepairOrder update = buildStatusUpdate(repairId, STATUS_APPROVED, updateBy);
        update.setApproveUserId(approveUserId);
        update.setApproveTime(new Date());
        update.setApproveResult(STATUS_APPROVED);
        update.setSendRepairTime(new Date());
        update.setBeforeStatus(beforeAsset.getAssetStatus());
        update.setAfterStatus(ASSET_STATUS_REPAIRING);
        if (StringUtils.isNotBlank(remark))
        {
            update.setRemark(remark);
        }
        int rows = assetRepairOrderMapper.updateAssetRepairOrderStatus(update);

        assetEventLogService.recordAssetEvent(
            beforeAsset.getAssetId(),
            EVENT_TYPE_REPAIR,
            repairId,
            BIZ_TYPE_REPAIR,
            beforeAsset,
            afterAsset,
            buildStartRepairEventDesc(dbOrder, beforeAsset),
            SecurityUtils.getUserId());
        return rows;
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
    public int finishAssetRepairOrder(Long repairId, String resultType, String remark, String updateBy)
    {
        AssetRepairOrder dbOrder = requireRepair(repairId);
        ensureStatus(dbOrder.getRepairStatus(), STATUS_APPROVED);
        String normalizedResultType = normalizeResultType(resultType);

        AssetInfo beforeAsset = requireVisibleAsset(dbOrder.getAssetId());
        if (!StringUtils.equals(beforeAsset.getAssetStatus(), ASSET_STATUS_REPAIRING))
        {
            throw new ServiceException("当前资产未处于维修中，无法完成维修单");
        }

        AssetInfo afterAsset = copyAssetSnapshot(beforeAsset);
        afterAsset.setAssetStatus(resolveAfterStatus(normalizedResultType));
        afterAsset.setUpdateBy(updateBy);
        assetInfoMapper.updateAssetSnapshot(afterAsset);

        AssetRepairOrder update = buildStatusUpdate(repairId, STATUS_FINISHED, updateBy);
        update.setFinishTime(new Date());
        update.setResultType(normalizedResultType);
        update.setAfterStatus(afterAsset.getAssetStatus());
        if (StringUtils.isNotBlank(remark))
        {
            update.setRemark(remark);
        }
        int rows = assetRepairOrderMapper.updateAssetRepairOrderStatus(update);

        assetEventLogService.recordAssetEvent(
            beforeAsset.getAssetId(),
            EVENT_TYPE_REPAIR,
            repairId,
            BIZ_TYPE_REPAIR,
            beforeAsset,
            afterAsset,
            buildFinishRepairEventDesc(dbOrder, afterAsset, normalizedResultType),
            SecurityUtils.getUserId());
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cancelAssetRepairOrder(Long repairId, String updateBy)
    {
        AssetRepairOrder dbOrder = requireRepair(repairId);
        ensureStatus(dbOrder.getRepairStatus(), STATUS_DRAFT, STATUS_SUBMITTED, STATUS_REJECTED, STATUS_APPROVED);

        if (StringUtils.equals(dbOrder.getRepairStatus(), STATUS_APPROVED))
        {
            AssetInfo beforeAsset = requireVisibleAsset(dbOrder.getAssetId());
            if (StringUtils.equals(beforeAsset.getAssetStatus(), ASSET_STATUS_REPAIRING))
            {
                AssetInfo afterAsset = copyAssetSnapshot(beforeAsset);
                afterAsset.setAssetStatus(StringUtils.defaultIfBlank(dbOrder.getBeforeStatus(), ASSET_STATUS_IN_USE));
                afterAsset.setUpdateBy(updateBy);
                assetInfoMapper.updateAssetSnapshot(afterAsset);

                assetEventLogService.recordAssetEvent(
                    beforeAsset.getAssetId(),
                    EVENT_TYPE_REPAIR,
                    repairId,
                    BIZ_TYPE_REPAIR,
                    beforeAsset,
                    afterAsset,
                    "维修单已取消，资产状态恢复为" + afterAsset.getAssetStatus(),
                    SecurityUtils.getUserId());
            }
        }

        return assetRepairOrderMapper.updateAssetRepairOrderStatus(buildStatusUpdate(repairId, STATUS_CANCELLED, updateBy));
    }

    /**
     * 新增时统一补齐快照和默认字段，避免前端只传核心输入导致落库失败。
     */
    private void buildInsertDefaults(AssetRepairOrder order, AssetInfo assetInfo)
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
        order.setAssetId(assetInfo.getAssetId());
        order.setAssetCode(assetInfo.getAssetCode());
        order.setAssetName(assetInfo.getAssetName());
        if (StringUtils.isBlank(order.getBeforeStatus()))
        {
            order.setBeforeStatus(assetInfo.getAssetStatus());
        }
        order.setAfterStatus(StringUtils.defaultIfBlank(order.getAfterStatus(), order.getBeforeStatus()));
    }

    private void fillUpdateDefaults(AssetRepairOrder target, AssetRepairOrder source, AssetInfo assetInfo)
    {
        target.setAssetId(assetInfo.getAssetId());
        target.setAssetCode(assetInfo.getAssetCode());
        target.setAssetName(assetInfo.getAssetName());
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
        if (StringUtils.isBlank(target.getBeforeStatus()))
        {
            target.setBeforeStatus(source.getBeforeStatus());
        }
        if (StringUtils.isBlank(target.getAfterStatus()))
        {
            target.setAfterStatus(source.getAfterStatus());
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
     * 同一资产同一时刻只保留一条未结束维修主线，避免状态联动相互覆盖。
     */
    private void ensureNoActiveRepair(Long assetId, Long excludeRepairId)
    {
        AssetRepairOrder activeOrder = assetRepairOrderMapper.selectActiveAssetRepairOrder(assetId, excludeRepairId);
        if (activeOrder != null)
        {
            throw new ServiceException("当前资产已有未完成维修单，请先处理完成后再发起新的维修");
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

    private String buildStartRepairEventDesc(AssetRepairOrder order, AssetInfo beforeAsset)
    {
        return "维修单[" + order.getRepairNo() + "]审批通过，资产状态由" + beforeAsset.getAssetStatus() + "变更为维修中";
    }

    private String buildFinishRepairEventDesc(AssetRepairOrder order, AssetInfo afterAsset, String resultType)
    {
        String resultLabel = switch (resultType)
        {
            case RESULT_RESUME_USE -> "恢复在用";
            case RESULT_TO_IDLE -> "转闲置";
            case RESULT_SUGGEST_DISPOSAL -> "建议报废";
            default -> resultType;
        };
        return "维修单[" + order.getRepairNo() + "]完成，处理结果为" + resultLabel + "，资产状态更新为" + afterAsset.getAssetStatus();
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
        target.setStatus(source.getStatus());
        target.setDelFlag(source.getDelFlag());
        target.setRemark(source.getRemark());
        return target;
    }
}
