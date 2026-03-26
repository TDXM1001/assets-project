package com.ruoyi.system.service.asset;

import java.util.List;
import com.ruoyi.system.domain.asset.AssetRepairOrder;

/**
 * 资产维修单服务接口
 */
public interface IAssetRepairOrderService
{
    List<AssetRepairOrder> selectAssetRepairOrderList(AssetRepairOrder assetRepairOrder);

    AssetRepairOrder selectAssetRepairOrderById(Long repairId);

    int insertAssetRepairOrder(AssetRepairOrder assetRepairOrder);

    int updateAssetRepairOrder(AssetRepairOrder assetRepairOrder);

    int deleteAssetRepairOrderByIds(Long[] repairIds);

    int submitAssetRepairOrder(Long repairId, String updateBy);

    int approveAssetRepairOrder(Long repairId, String remark, Long approveUserId, String updateBy);

    int rejectAssetRepairOrder(Long repairId, String remark, Long approveUserId, String updateBy);

    int finishAssetRepairOrder(Long repairId, String resultType, String remark, String updateBy);

    int cancelAssetRepairOrder(Long repairId, String updateBy);
}
