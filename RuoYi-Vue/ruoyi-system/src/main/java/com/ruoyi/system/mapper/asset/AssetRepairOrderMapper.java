package com.ruoyi.system.mapper.asset;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.asset.AssetRepairOrder;

/**
 * 资产维修单 Mapper 接口
 */
public interface AssetRepairOrderMapper
{
    List<AssetRepairOrder> selectAssetRepairOrderList(AssetRepairOrder assetRepairOrder);

    AssetRepairOrder selectAssetRepairOrderById(Long repairId);

    AssetRepairOrder selectActiveAssetRepairOrder(@Param("assetId") Long assetId, @Param("excludeRepairId") Long excludeRepairId);

    int insertAssetRepairOrder(AssetRepairOrder assetRepairOrder);

    int updateAssetRepairOrder(AssetRepairOrder assetRepairOrder);

    int updateAssetRepairOrderStatus(AssetRepairOrder assetRepairOrder);

    int updateAttachmentCount(@Param("repairId") Long repairId, @Param("attachmentCount") Integer attachmentCount);

    int deleteAssetRepairOrderByIds(Long[] repairIds);
}
