package com.ruoyi.system.mapper.asset;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.asset.AssetOperateOrder;
import com.ruoyi.system.domain.asset.AssetOperateOrderItem;

/**
 * 资产业务单据 Mapper 接口
 */
public interface AssetOperateOrderMapper
{
    List<AssetOperateOrder> selectAssetOperateOrderList(AssetOperateOrder assetOperateOrder);

    AssetOperateOrder selectAssetOperateOrderById(Long orderId);

    List<AssetOperateOrderItem> selectAssetOperateOrderItemsByOrderId(Long orderId);

    int insertAssetOperateOrder(AssetOperateOrder assetOperateOrder);

    int updateAssetOperateOrder(AssetOperateOrder assetOperateOrder);

    int updateAssetOperateOrderStatus(AssetOperateOrder assetOperateOrder);

    int deleteAssetOperateOrderByIds(Long[] orderIds);

    int deleteAssetOperateOrderItemsByOrderIds(Long[] orderIds);

    int deleteAssetOperateOrderItemsByOrderId(Long orderId);

    int batchInsertAssetOperateOrderItems(@Param("list") List<AssetOperateOrderItem> itemList);
}
