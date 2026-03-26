package com.ruoyi.system.service.asset;

import java.util.List;
import com.ruoyi.system.domain.asset.AssetOperateOrder;

/**
 * 资产业务单据服务接口
 */
public interface IAssetOperateOrderService
{
    List<AssetOperateOrder> selectAssetOperateOrderList(AssetOperateOrder assetOperateOrder);

    AssetOperateOrder selectAssetOperateOrderById(Long orderId);

    AssetOperateOrder selectLinkedAssetOperateOrder(String orderType, String sourceBizType, Long sourceBizId);

    int insertAssetOperateOrder(AssetOperateOrder assetOperateOrder);

    int updateAssetOperateOrder(AssetOperateOrder assetOperateOrder);

    int deleteAssetOperateOrderByIds(Long[] orderIds);

    int submitAssetOperateOrder(Long orderId, String updateBy);

    int approveAssetOperateOrder(Long orderId, String remark, Long approveUserId, String updateBy);

    int rejectAssetOperateOrder(Long orderId, String remark, Long approveUserId, String updateBy);

    int finishAssetOperateOrder(Long orderId, String updateBy);

    int cancelAssetOperateOrder(Long orderId, String updateBy);
}
