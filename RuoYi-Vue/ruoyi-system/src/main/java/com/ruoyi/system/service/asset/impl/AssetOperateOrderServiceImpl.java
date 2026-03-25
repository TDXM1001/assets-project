package com.ruoyi.system.service.asset.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;
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
import com.ruoyi.system.domain.asset.AssetOperateOrder;
import com.ruoyi.system.domain.asset.AssetOperateOrderItem;
import com.ruoyi.system.mapper.asset.AssetInfoMapper;
import com.ruoyi.system.mapper.asset.AssetOperateOrderMapper;
import com.ruoyi.system.service.asset.IAssetEventLogService;
import com.ruoyi.system.service.asset.IAssetOperateOrderService;

/**
 * 资产业务单据服务实现
 */
@Service
public class AssetOperateOrderServiceImpl implements IAssetOperateOrderService
{
    private static final String STATUS_DRAFT = "DRAFT";
    private static final String STATUS_SUBMITTED = "SUBMITTED";
    private static final String STATUS_APPROVING = "APPROVING";
    private static final String STATUS_APPROVED = "APPROVED";
    private static final String STATUS_REJECTED = "REJECTED";
    private static final String STATUS_FINISHED = "DONE";
    private static final String STATUS_CANCELLED = "CANCELED";

    @Autowired
    private AssetOperateOrderMapper assetOperateOrderMapper;

    @Autowired
    private AssetInfoMapper assetInfoMapper;

    @Autowired
    private IAssetEventLogService assetEventLogService;

    @Autowired
    @Lazy
    private IAssetOperateOrderService assetOperateOrderServiceProxy;

    @Override
    @DataScope(deptAlias = "scope_dept", userAlias = "apply_user")
    public List<AssetOperateOrder> selectAssetOperateOrderList(AssetOperateOrder assetOperateOrder)
    {
        return assetOperateOrderMapper.selectAssetOperateOrderList(assetOperateOrder);
    }

    @Override
    public AssetOperateOrder selectAssetOperateOrderById(Long orderId)
    {
        AssetOperateOrder query = new AssetOperateOrder();
        query.setOrderId(orderId);
        List<AssetOperateOrder> scopedOrders = assetOperateOrderServiceProxy.selectAssetOperateOrderList(query);
        AssetOperateOrder order = scopedOrders.isEmpty() ? null : scopedOrders.get(0);
        if (StringUtils.isNotNull(order))
        {
            order.setItemList(assetOperateOrderMapper.selectAssetOperateOrderItemsByOrderId(orderId));
        }
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertAssetOperateOrder(AssetOperateOrder assetOperateOrder)
    {
        buildInsertDefaults(assetOperateOrder);
        int rows = assetOperateOrderMapper.insertAssetOperateOrder(assetOperateOrder);
        syncOrderItems(assetOperateOrder);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateAssetOperateOrder(AssetOperateOrder assetOperateOrder)
    {
        AssetOperateOrder dbOrder = requireOrder(assetOperateOrder.getOrderId());
        ensureStatus(dbOrder.getOrderStatus(), STATUS_DRAFT, STATUS_REJECTED);
        fillUpdateDefaults(assetOperateOrder, dbOrder);
        int rows = assetOperateOrderMapper.updateAssetOperateOrder(assetOperateOrder);
        syncOrderItems(assetOperateOrder);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteAssetOperateOrderByIds(Long[] orderIds)
    {
        for (Long orderId : orderIds)
        {
            AssetOperateOrder dbOrder = requireOrder(orderId);
            ensureStatus(dbOrder.getOrderStatus(), STATUS_DRAFT, STATUS_REJECTED);
        }
        assetOperateOrderMapper.deleteAssetOperateOrderItemsByOrderIds(orderIds);
        return assetOperateOrderMapper.deleteAssetOperateOrderByIds(orderIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int submitAssetOperateOrder(Long orderId, String updateBy)
    {
        AssetOperateOrder dbOrder = requireOrder(orderId);
        ensureStatus(dbOrder.getOrderStatus(), STATUS_DRAFT, STATUS_REJECTED);
        return assetOperateOrderMapper.updateAssetOperateOrderStatus(buildStatusUpdate(orderId, STATUS_SUBMITTED, updateBy));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int approveAssetOperateOrder(Long orderId, String remark, Long approveUserId, String updateBy)
    {
        AssetOperateOrder dbOrder = requireOrder(orderId);
        ensureStatus(dbOrder.getOrderStatus(), STATUS_SUBMITTED, STATUS_APPROVING);
        AssetOperateOrder update = buildStatusUpdate(orderId, STATUS_APPROVED, updateBy);
        update.setApproveUserId(approveUserId);
        update.setApproveTime(new Date());
        update.setApproveResult(STATUS_APPROVED);
        if (StringUtils.isNotBlank(remark))
        {
            update.setRemark(remark);
        }
        return assetOperateOrderMapper.updateAssetOperateOrderStatus(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int rejectAssetOperateOrder(Long orderId, String remark, Long approveUserId, String updateBy)
    {
        AssetOperateOrder dbOrder = requireOrder(orderId);
        ensureStatus(dbOrder.getOrderStatus(), STATUS_SUBMITTED, STATUS_APPROVING);
        AssetOperateOrder update = buildStatusUpdate(orderId, STATUS_REJECTED, updateBy);
        update.setApproveUserId(approveUserId);
        update.setApproveTime(new Date());
        update.setApproveResult(STATUS_REJECTED);
        update.setRemark(remark);
        return assetOperateOrderMapper.updateAssetOperateOrderStatus(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int finishAssetOperateOrder(Long orderId, String updateBy)
    {
        AssetOperateOrder dbOrder = requireOrder(orderId);
        ensureStatus(dbOrder.getOrderStatus(), STATUS_APPROVED);

        List<AssetOperateOrderItem> itemList = assetOperateOrderMapper.selectAssetOperateOrderItemsByOrderId(orderId);
        if (itemList == null || itemList.isEmpty())
        {
            throw new ServiceException("当前单据没有资产明细，不能执行完成落账");
        }

        Set<Long> assetIds = itemList.stream()
            .map(AssetOperateOrderItem::getAssetId)
            .filter(assetId -> assetId != null)
            .collect(Collectors.toCollection(LinkedHashSet::new));
        if (assetIds.isEmpty())
        {
            throw new ServiceException("当前单据明细缺少资产ID，不能执行完成落账");
        }

        List<AssetInfo> assetList = assetInfoMapper.selectAssetInfoByIds(assetIds);
        Map<Long, AssetInfo> assetMap = assetList.stream()
            .collect(Collectors.toMap(AssetInfo::getAssetId, Function.identity()));
        Long operatorUserId = SecurityUtils.getUserId();
        List<AssetOperateOrderItem> finishedItems = new ArrayList<>();

        for (AssetOperateOrderItem item : itemList)
        {
            AssetInfo beforeAsset = assetMap.get(item.getAssetId());
            if (beforeAsset == null)
            {
                throw new ServiceException("资产明细中的资产不存在或已删除，资产ID：" + item.getAssetId());
            }

            AssetInfo afterAsset = buildAfterSnapshot(dbOrder, item, beforeAsset, updateBy);
            assetInfoMapper.updateAssetSnapshot(afterAsset);
            assetEventLogService.recordAssetEvent(
                beforeAsset.getAssetId(),
                dbOrder.getOrderType(),
                dbOrder.getOrderId(),
                dbOrder.getOrderType(),
                beforeAsset,
                afterAsset,
                buildEventDesc(dbOrder, beforeAsset, afterAsset),
                operatorUserId);

            item.setItemStatus(STATUS_FINISHED);
            item.setItemResult(buildItemResult(dbOrder, afterAsset));
            finishedItems.add(item);
        }

        dbOrder.setItemList(finishedItems);
        dbOrder.setOrderStatus(STATUS_FINISHED);
        syncOrderItems(dbOrder);
        return assetOperateOrderMapper.updateAssetOperateOrderStatus(buildStatusUpdate(orderId, STATUS_FINISHED, updateBy));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cancelAssetOperateOrder(Long orderId, String updateBy)
    {
        AssetOperateOrder dbOrder = requireOrder(orderId);
        ensureStatus(dbOrder.getOrderStatus(), STATUS_DRAFT, STATUS_SUBMITTED, STATUS_APPROVING, STATUS_APPROVED);
        return assetOperateOrderMapper.updateAssetOperateOrderStatus(buildStatusUpdate(orderId, STATUS_CANCELLED, updateBy));
    }

    /**
     * 新增时把后端兜底字段补齐，避免前端只传流程核心字段时落库失败。
     */
    private void buildInsertDefaults(AssetOperateOrder assetOperateOrder)
    {
        if (StringUtils.isBlank(assetOperateOrder.getOrderNo()))
        {
            assetOperateOrder.setOrderNo(generateOrderNo(assetOperateOrder.getOrderType()));
        }
        if (assetOperateOrder.getBizDate() == null)
        {
            assetOperateOrder.setBizDate(DateUtils.getNowDate());
        }
        if (StringUtils.isBlank(assetOperateOrder.getOrderStatus()))
        {
            assetOperateOrder.setOrderStatus(STATUS_DRAFT);
        }
        if (StringUtils.isBlank(assetOperateOrder.getStatus()))
        {
            assetOperateOrder.setStatus("0");
        }
        assetOperateOrder.setDelFlag("0");
        if (assetOperateOrder.getAttachmentCount() == null)
        {
            assetOperateOrder.setAttachmentCount(0);
        }
    }

    private void fillUpdateDefaults(AssetOperateOrder assetOperateOrder, AssetOperateOrder dbOrder)
    {
        if (StringUtils.isBlank(assetOperateOrder.getOrderNo()))
        {
            assetOperateOrder.setOrderNo(dbOrder.getOrderNo());
        }
        if (assetOperateOrder.getBizDate() == null)
        {
            assetOperateOrder.setBizDate(dbOrder.getBizDate());
        }
        if (StringUtils.isBlank(assetOperateOrder.getOrderStatus()))
        {
            assetOperateOrder.setOrderStatus(dbOrder.getOrderStatus());
        }
        if (StringUtils.isBlank(assetOperateOrder.getStatus()))
        {
            assetOperateOrder.setStatus(dbOrder.getStatus());
        }
        if (assetOperateOrder.getAttachmentCount() == null)
        {
            assetOperateOrder.setAttachmentCount(dbOrder.getAttachmentCount());
        }
    }

    /**
     * 当前阶段前端还没提供完整的明细编辑器，这里先把明细同步能力封装好。
     */
    private void syncOrderItems(AssetOperateOrder assetOperateOrder)
    {
        if (assetOperateOrder.getOrderId() == null)
        {
            return;
        }
        if (assetOperateOrder.getItemList() == null)
        {
            return;
        }

        List<AssetOperateOrderItem> normalizedItems = normalizeOrderItems(assetOperateOrder);
        assetOperateOrderMapper.deleteAssetOperateOrderItemsByOrderId(assetOperateOrder.getOrderId());
        if (normalizedItems.isEmpty())
        {
            return;
        }
        for (AssetOperateOrderItem item : normalizedItems)
        {
            item.setOrderId(assetOperateOrder.getOrderId());
            if (StringUtils.isBlank(item.getItemStatus()))
            {
                item.setItemStatus(assetOperateOrder.getOrderStatus());
            }
        }
        assetOperateOrderMapper.batchInsertAssetOperateOrderItems(normalizedItems);
    }

    /**
     * 单据明细在新增、编辑、详情三个场景共用一套快照口径：
     * 1. 拦住缺少资产ID、重复资产ID 这类明显脏数据；
     * 2. 用当前台账补齐变更前快照，避免前端自己拼 before_*；
     * 3. 若前端未显式传 after_*，就基于单据头推导一个预期变更后快照，方便详情回显。
     */
    private List<AssetOperateOrderItem> normalizeOrderItems(AssetOperateOrder assetOperateOrder)
    {
        if (assetOperateOrder.getItemList() == null || assetOperateOrder.getItemList().isEmpty())
        {
            return new ArrayList<>();
        }

        Set<Long> duplicateCheck = new LinkedHashSet<>();
        Set<Long> assetIds = new LinkedHashSet<>();
        for (AssetOperateOrderItem item : assetOperateOrder.getItemList())
        {
            if (item.getAssetId() == null)
            {
                throw new ServiceException("单据明细缺少资产ID，请先重新选择资产");
            }
            if (!duplicateCheck.add(item.getAssetId()))
            {
                throw new ServiceException("单据明细中存在重复资产，资产ID：" + item.getAssetId());
            }
            assetIds.add(item.getAssetId());
        }

        List<AssetInfo> assetList = assetInfoMapper.selectAssetInfoByIds(assetIds);
        Map<Long, AssetInfo> assetMap = assetList.stream()
            .collect(Collectors.toMap(AssetInfo::getAssetId, Function.identity()));

        List<AssetOperateOrderItem> normalizedItems = new ArrayList<>();
        for (AssetOperateOrderItem item : assetOperateOrder.getItemList())
        {
            AssetInfo beforeAsset = assetMap.get(item.getAssetId());
            if (beforeAsset == null)
            {
                throw new ServiceException("单据明细中的资产不存在或无权限访问，资产ID：" + item.getAssetId());
            }
            normalizedItems.add(buildNormalizedItem(assetOperateOrder, item, beforeAsset));
        }
        return normalizedItems;
    }

    private AssetOperateOrderItem buildNormalizedItem(AssetOperateOrder order, AssetOperateOrderItem source, AssetInfo beforeAsset)
    {
        AssetOperateOrderItem target = new AssetOperateOrderItem();
        target.setItemId(source.getItemId());
        target.setOrderId(source.getOrderId());
        target.setAssetId(beforeAsset.getAssetId());
        target.setAssetCode(beforeAsset.getAssetCode());
        target.setAssetName(beforeAsset.getAssetName());
        target.setBeforeStatus(StringUtils.isNotBlank(source.getBeforeStatus()) ? source.getBeforeStatus() : beforeAsset.getAssetStatus());
        target.setBeforeUserId(source.getBeforeUserId() != null ? source.getBeforeUserId() : beforeAsset.getCurrentUserId());
        target.setBeforeDeptId(source.getBeforeDeptId() != null ? source.getBeforeDeptId() : beforeAsset.getUseOrgDeptId());
        target.setBeforeLocationId(source.getBeforeLocationId() != null ? source.getBeforeLocationId() : beforeAsset.getCurrentLocationId());
        target.setAfterStatus(StringUtils.isNotBlank(source.getAfterStatus()) ? source.getAfterStatus() : resolveAfterStatus(order, source, beforeAsset));
        target.setAfterUserId(source.getAfterUserId() != null ? source.getAfterUserId() : resolveAfterUser(order, source, beforeAsset));
        target.setAfterDeptId(source.getAfterDeptId() != null ? source.getAfterDeptId() : resolveAfterDept(order, source, beforeAsset));
        target.setAfterLocationId(source.getAfterLocationId() != null ? source.getAfterLocationId() : resolveAfterLocation(order, source, beforeAsset));
        target.setItemStatus(StringUtils.isNotBlank(source.getItemStatus()) ? source.getItemStatus() : order.getOrderStatus());
        target.setItemResult(source.getItemResult());
        return target;
    }

    /**
     * 单据真正完成时，要基于当前主档快照和明细目标值构造一份完整的“变更后”快照，避免误把可空字段清掉。
     */
    private AssetInfo buildAfterSnapshot(AssetOperateOrder order, AssetOperateOrderItem item, AssetInfo beforeAsset, String updateBy)
    {
        AssetInfo afterAsset = copyAssetSnapshot(beforeAsset);
        afterAsset.setAssetStatus(resolveAfterStatus(order, item, beforeAsset));
        afterAsset.setUseOrgDeptId(resolveAfterDept(order, item, beforeAsset));
        afterAsset.setCurrentUserId(resolveAfterUser(order, item, beforeAsset));
        afterAsset.setCurrentLocationId(resolveAfterLocation(order, item, beforeAsset));
        afterAsset.setManageDeptId(resolveAfterManageDept(beforeAsset, afterAsset));
        Integer beforeVersion = beforeAsset.getVersionNo() == null ? 0 : beforeAsset.getVersionNo();
        afterAsset.setVersionNo(beforeVersion + 1);
        afterAsset.setUpdateBy(updateBy);
        if (isInboundOrder(order) && afterAsset.getInboundDate() == null)
        {
            afterAsset.setInboundDate(order.getBizDate());
        }
        if (needsStartUseDate(order, afterAsset) && afterAsset.getStartUseDate() == null)
        {
            afterAsset.setStartUseDate(order.getBizDate());
        }
        return afterAsset;
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

    private String resolveAfterStatus(AssetOperateOrder order, AssetOperateOrderItem item, AssetInfo beforeAsset)
    {
        if (StringUtils.isNotBlank(item.getAfterStatus()))
        {
            return item.getAfterStatus();
        }
        return switch (order.getOrderType())
        {
            case "INBOUND" -> "IDLE";
            case "ASSIGN", "TRANSFER" -> "IN_USE";
            case "BORROW" -> "BORROWED";
            case "RETURN" -> "IDLE";
            case "DISPOSAL" -> "DISPOSED";
            default -> beforeAsset.getAssetStatus();
        };
    }

    private Long resolveAfterDept(AssetOperateOrder order, AssetOperateOrderItem item, AssetInfo beforeAsset)
    {
        if (item.getAfterDeptId() != null)
        {
            return item.getAfterDeptId();
        }
        return switch (order.getOrderType())
        {
            case "INBOUND", "ASSIGN", "BORROW", "RETURN", "TRANSFER" ->
                order.getToDeptId() != null ? order.getToDeptId() : beforeAsset.getUseOrgDeptId();
            case "DISPOSAL" -> beforeAsset.getUseOrgDeptId();
            default -> beforeAsset.getUseOrgDeptId();
        };
    }

    private Long resolveAfterUser(AssetOperateOrder order, AssetOperateOrderItem item, AssetInfo beforeAsset)
    {
        if (item.getAfterUserId() != null)
        {
            return item.getAfterUserId();
        }
        return switch (order.getOrderType())
        {
            case "ASSIGN", "BORROW", "TRANSFER" -> order.getToUserId();
            case "INBOUND", "RETURN", "DISPOSAL" -> null;
            default -> beforeAsset.getCurrentUserId();
        };
    }

    private Long resolveAfterLocation(AssetOperateOrder order, AssetOperateOrderItem item, AssetInfo beforeAsset)
    {
        if (item.getAfterLocationId() != null)
        {
            return item.getAfterLocationId();
        }
        return switch (order.getOrderType())
        {
            case "INBOUND", "ASSIGN", "BORROW", "RETURN", "TRANSFER" ->
                order.getToLocationId() != null ? order.getToLocationId() : beforeAsset.getCurrentLocationId();
            case "DISPOSAL" -> order.getToLocationId() != null ? order.getToLocationId() : beforeAsset.getCurrentLocationId();
            default -> beforeAsset.getCurrentLocationId();
        };
    }

    private Long resolveAfterManageDept(AssetInfo beforeAsset, AssetInfo afterAsset)
    {
        if (beforeAsset.getManageDeptId() != null)
        {
            return beforeAsset.getManageDeptId();
        }
        return afterAsset.getUseOrgDeptId();
    }

    private boolean isInboundOrder(AssetOperateOrder order)
    {
        return StringUtils.equals(order.getOrderType(), "INBOUND");
    }

    private boolean needsStartUseDate(AssetOperateOrder order, AssetInfo afterAsset)
    {
        if (StringUtils.equals(order.getOrderType(), "RETURN") || StringUtils.equals(order.getOrderType(), "DISPOSAL"))
        {
            return false;
        }
        return StringUtils.equalsAny(afterAsset.getAssetStatus(), "IN_USE", "BORROWED");
    }

    private String buildEventDesc(AssetOperateOrder order, AssetInfo beforeAsset, AssetInfo afterAsset)
    {
        StringBuilder builder = new StringBuilder("单据完成落账：");
        builder.append(order.getOrderNo());
        builder.append("，资产编码=").append(beforeAsset.getAssetCode());
        builder.append("，状态 ").append(defaultText(beforeAsset.getAssetStatus())).append(" -> ").append(defaultText(afterAsset.getAssetStatus()));
        builder.append("，责任人 ").append(defaultText(beforeAsset.getCurrentUserId())).append(" -> ").append(defaultText(afterAsset.getCurrentUserId()));
        builder.append("，位置 ").append(defaultText(beforeAsset.getCurrentLocationId())).append(" -> ").append(defaultText(afterAsset.getCurrentLocationId()));
        builder.append("，部门 ").append(defaultText(beforeAsset.getUseOrgDeptId())).append(" -> ").append(defaultText(afterAsset.getUseOrgDeptId()));
        return builder.toString();
    }

    private String buildItemResult(AssetOperateOrder order, AssetInfo afterAsset)
    {
        return "已完成" + order.getOrderType() + "落账，当前状态：" + defaultText(afterAsset.getAssetStatus());
    }

    private String defaultText(Object value)
    {
        return value == null ? "-" : String.valueOf(value);
    }

    private AssetOperateOrder buildStatusUpdate(Long orderId, String orderStatus, String updateBy)
    {
        AssetOperateOrder update = new AssetOperateOrder();
        update.setOrderId(orderId);
        update.setOrderStatus(orderStatus);
        update.setUpdateBy(updateBy);
        return update;
    }

    private AssetOperateOrder requireOrder(Long orderId)
    {
        AssetOperateOrder order = selectAssetOperateOrderById(orderId);
        if (StringUtils.isNull(order))
        {
            throw new ServiceException("资产业务单据不存在");
        }
        return order;
    }

    private void ensureStatus(String currentStatus, String... expectedStatuses)
    {
        for (String expectedStatus : expectedStatuses)
        {
            if (StringUtils.equals(currentStatus, expectedStatus))
            {
                return;
            }
        }
        throw new ServiceException("当前单据状态不允许执行该操作");
    }

    /**
     * 单据编号先用时间戳加随机尾号兜底，后续统一编号规则时只需要替换这里。
     */
    private String generateOrderNo(String orderType)
    {
        String prefix = StringUtils.isBlank(orderType) ? "ORDER" : orderType;
        return prefix + DateUtils.dateTimeNow() + ThreadLocalRandom.current().nextInt(1000, 9999);
    }
}
