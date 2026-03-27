package com.ruoyi.system.mapper.asset;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.asset.AssetRepairOrder;
import com.ruoyi.system.domain.asset.AssetRepairOrderItem;

/**
 * 资产维修单 Mapper 接口
 */
public interface AssetRepairOrderMapper
{
    List<AssetRepairOrder> selectAssetRepairOrderList(AssetRepairOrder assetRepairOrder);

    AssetRepairOrder selectAssetRepairOrderById(Long repairId);

    AssetRepairOrder selectActiveAssetRepairOrder(@Param("assetId") Long assetId, @Param("excludeRepairId") Long excludeRepairId);

    List<AssetRepairOrderItem> selectAssetRepairOrderItemsByRepairId(Long repairId);

    int insertAssetRepairOrder(AssetRepairOrder assetRepairOrder);

    int insertAssetRepairOrderItem(AssetRepairOrderItem item);

    int updateAssetRepairOrder(AssetRepairOrder assetRepairOrder);

    int updateAssetRepairOrderItem(AssetRepairOrderItem item);

    int updateAssetRepairOrderStatus(AssetRepairOrder assetRepairOrder);

    int updateAttachmentCount(@Param("repairId") Long repairId, @Param("attachmentCount") Integer attachmentCount);

    int deleteAssetRepairOrderByIds(Long[] repairIds);

    int deleteAssetRepairOrderItemsByRepairIds(Long[] repairIds);

    int deleteAssetRepairOrderItemsByRepairId(@Param("repairId") Long repairId);

    int batchInsertAssetRepairOrderItems(@Param("list") List<AssetRepairOrderItem> itemList);
}
