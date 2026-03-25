package com.ruoyi.system.domain.asset.vo;

import java.util.ArrayList;
import java.util.List;

/**
 * 资产看板汇总数据
 */
public class AssetDashboardSummaryVo
{
    /**
     * 可见资产总数
     */
    private Long assetTotal = 0L;

    /**
     * 可见单据总数
     */
    private Long orderTotal = 0L;

    /**
     * 可见盘点任务总数
     */
    private Long inventoryTaskTotal = 0L;

    /**
     * 最近事件数
     */
    private Long recentEventTotal = 0L;

    /**
     * 最近事件统计窗口天数
     */
    private Integer recentEventDays;

    /**
     * 资产状态分布
     */
    private List<AssetDashboardStatusCountVo> statusList = new ArrayList<>();

    public Long getAssetTotal()
    {
        return assetTotal;
    }

    public void setAssetTotal(Long assetTotal)
    {
        this.assetTotal = assetTotal;
    }

    public Long getOrderTotal()
    {
        return orderTotal;
    }

    public void setOrderTotal(Long orderTotal)
    {
        this.orderTotal = orderTotal;
    }

    public Long getInventoryTaskTotal()
    {
        return inventoryTaskTotal;
    }

    public void setInventoryTaskTotal(Long inventoryTaskTotal)
    {
        this.inventoryTaskTotal = inventoryTaskTotal;
    }

    public Long getRecentEventTotal()
    {
        return recentEventTotal;
    }

    public void setRecentEventTotal(Long recentEventTotal)
    {
        this.recentEventTotal = recentEventTotal;
    }

    public Integer getRecentEventDays()
    {
        return recentEventDays;
    }

    public void setRecentEventDays(Integer recentEventDays)
    {
        this.recentEventDays = recentEventDays;
    }

    public List<AssetDashboardStatusCountVo> getStatusList()
    {
        return statusList;
    }

    public void setStatusList(List<AssetDashboardStatusCountVo> statusList)
    {
        this.statusList = statusList;
    }
}
