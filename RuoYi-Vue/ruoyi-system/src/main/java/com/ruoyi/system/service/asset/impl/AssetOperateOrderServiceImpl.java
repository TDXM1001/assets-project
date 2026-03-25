package com.ruoyi.system.service.asset.impl;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.asset.AssetOperateOrder;
import com.ruoyi.system.domain.asset.AssetOperateOrderItem;
import com.ruoyi.system.mapper.asset.AssetOperateOrderMapper;
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

    @Override
    public List<AssetOperateOrder> selectAssetOperateOrderList(AssetOperateOrder assetOperateOrder)
    {
        return assetOperateOrderMapper.selectAssetOperateOrderList(assetOperateOrder);
    }

    @Override
    public AssetOperateOrder selectAssetOperateOrderById(Long orderId)
    {
        AssetOperateOrder order = assetOperateOrderMapper.selectAssetOperateOrderById(orderId);
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
     * 新增时把后端托底字段补齐，避免前端只传流程核心字段时落库失败。
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
        assetOperateOrderMapper.deleteAssetOperateOrderItemsByOrderId(assetOperateOrder.getOrderId());
        if (assetOperateOrder.getItemList() == null || assetOperateOrder.getItemList().isEmpty())
        {
            return;
        }
        for (AssetOperateOrderItem item : assetOperateOrder.getItemList())
        {
            item.setOrderId(assetOperateOrder.getOrderId());
            if (StringUtils.isBlank(item.getItemStatus()))
            {
                item.setItemStatus(assetOperateOrder.getOrderStatus());
            }
        }
        assetOperateOrderMapper.batchInsertAssetOperateOrderItems(assetOperateOrder.getItemList());
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
        AssetOperateOrder order = assetOperateOrderMapper.selectAssetOperateOrderById(orderId);
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
