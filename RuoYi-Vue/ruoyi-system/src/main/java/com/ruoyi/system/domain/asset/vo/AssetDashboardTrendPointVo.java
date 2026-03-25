package com.ruoyi.system.domain.asset.vo;

/**
 * 资产看板趋势点
 */
public class AssetDashboardTrendPointVo
{
    /**
     * 统计日期
     */
    private String statDate;

    /**
     * 事件趋势值
     */
    private Long eventCount = 0L;

    /**
     * 单据趋势值
     */
    private Long orderCount = 0L;

    /**
     * 盘点任务趋势值
     */
    private Long inventoryCount = 0L;

    /**
     * 异常差异趋势值
     */
    private Long diffCount = 0L;

    public String getStatDate()
    {
        return statDate;
    }

    public void setStatDate(String statDate)
    {
        this.statDate = statDate;
    }

    public Long getEventCount()
    {
        return eventCount;
    }

    public void setEventCount(Long eventCount)
    {
        this.eventCount = eventCount;
    }

    public Long getOrderCount()
    {
        return orderCount;
    }

    public void setOrderCount(Long orderCount)
    {
        this.orderCount = orderCount;
    }

    public Long getInventoryCount()
    {
        return inventoryCount;
    }

    public void setInventoryCount(Long inventoryCount)
    {
        this.inventoryCount = inventoryCount;
    }

    public Long getDiffCount()
    {
        return diffCount;
    }

    public void setDiffCount(Long diffCount)
    {
        this.diffCount = diffCount;
    }
}
