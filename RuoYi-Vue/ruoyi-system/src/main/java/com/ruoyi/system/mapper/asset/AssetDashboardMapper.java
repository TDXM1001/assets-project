package com.ruoyi.system.mapper.asset;

import java.util.List;
import com.ruoyi.system.domain.asset.vo.AssetDashboardQueryVo;
import com.ruoyi.system.domain.asset.vo.AssetDashboardStatusCountVo;
import com.ruoyi.system.domain.asset.vo.AssetDashboardTrendStatVo;

/**
 * 资产看板聚合 Mapper 接口
 */
public interface AssetDashboardMapper
{
    Long selectVisibleAssetTotal(AssetDashboardQueryVo query);

    List<AssetDashboardStatusCountVo> selectVisibleAssetStatusCountList(AssetDashboardQueryVo query);

    Long selectVisibleOrderTotal(AssetDashboardQueryVo query);

    Long selectVisibleInventoryTaskTotal(AssetDashboardQueryVo query);

    Long selectVisibleRecentEventTotal(AssetDashboardQueryVo query);

    Long selectTodoUnfinishedOrderCount(AssetDashboardQueryVo query);

    Long selectTodoUnfinishedInventoryTaskCount(AssetDashboardQueryVo query);

    Long selectTodoPendingDiffCount(AssetDashboardQueryVo query);

    List<AssetDashboardTrendStatVo> selectEventTrendStatList(AssetDashboardQueryVo query);

    List<AssetDashboardTrendStatVo> selectOrderTrendStatList(AssetDashboardQueryVo query);

    List<AssetDashboardTrendStatVo> selectInventoryTrendStatList(AssetDashboardQueryVo query);

    List<AssetDashboardTrendStatVo> selectDiffTrendStatList(AssetDashboardQueryVo query);
}
