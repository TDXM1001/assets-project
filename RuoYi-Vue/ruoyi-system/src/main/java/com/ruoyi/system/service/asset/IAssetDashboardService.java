package com.ruoyi.system.service.asset;

import java.util.List;
import com.ruoyi.system.domain.asset.vo.AssetDashboardQueryVo;
import com.ruoyi.system.domain.asset.vo.AssetDashboardStatusCountVo;
import com.ruoyi.system.domain.asset.vo.AssetDashboardSummaryVo;
import com.ruoyi.system.domain.asset.vo.AssetDashboardTodoVo;
import com.ruoyi.system.domain.asset.vo.AssetDashboardTrendStatVo;
import com.ruoyi.system.domain.asset.vo.AssetDashboardTrendVo;

/**
 * 资产看板服务接口
 */
public interface IAssetDashboardService
{
    /**
     * 查询看板汇总数据
     */
    AssetDashboardSummaryVo selectDashboardSummary();

    /**
     * 查询看板待办数据
     */
    AssetDashboardTodoVo selectDashboardTodo();

    /**
     * 查询看板趋势数据
     */
    AssetDashboardTrendVo selectDashboardTrend();

    /**
     * 查询可见资产总数
     */
    Long selectVisibleAssetTotal(AssetDashboardQueryVo query);

    /**
     * 查询可见资产状态分布
     */
    List<AssetDashboardStatusCountVo> selectVisibleAssetStatusCountList(AssetDashboardQueryVo query);

    /**
     * 查询可见单据总数
     */
    Long selectVisibleOrderTotal(AssetDashboardQueryVo query);

    /**
     * 查询可见盘点任务总数
     */
    Long selectVisibleInventoryTaskTotal(AssetDashboardQueryVo query);

    /**
     * 查询最近事件数
     */
    Long selectVisibleRecentEventTotal(AssetDashboardQueryVo query);

    /**
     * 查询待处理单据数
     */
    Long selectTodoUnfinishedOrderCount(AssetDashboardQueryVo query);

    /**
     * 查询待完成盘点数
     */
    Long selectTodoUnfinishedInventoryTaskCount(AssetDashboardQueryVo query);

    /**
     * 查询待处理差异数
     */
    Long selectTodoPendingDiffCount(AssetDashboardQueryVo query);

    /**
     * 查询事件趋势统计
     */
    List<AssetDashboardTrendStatVo> selectEventTrendStatList(AssetDashboardQueryVo query);

    /**
     * 查询单据趋势统计
     */
    List<AssetDashboardTrendStatVo> selectOrderTrendStatList(AssetDashboardQueryVo query);

    /**
     * 查询盘点任务趋势统计
     */
    List<AssetDashboardTrendStatVo> selectInventoryTrendStatList(AssetDashboardQueryVo query);

    /**
     * 查询异常差异趋势统计
     */
    List<AssetDashboardTrendStatVo> selectDiffTrendStatList(AssetDashboardQueryVo query);
}
