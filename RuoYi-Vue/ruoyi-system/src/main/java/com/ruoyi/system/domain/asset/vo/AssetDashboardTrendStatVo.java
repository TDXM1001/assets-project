package com.ruoyi.system.domain.asset.vo;

/**
 * 资产看板趋势原始统计行
 */
public class AssetDashboardTrendStatVo
{
    /**
     * 统计日期
     */
    private String statDate;

    /**
     * 统计数量
     */
    private Long totalCount;

    public String getStatDate()
    {
        return statDate;
    }

    public void setStatDate(String statDate)
    {
        this.statDate = statDate;
    }

    public Long getTotalCount()
    {
        return totalCount;
    }

    public void setTotalCount(Long totalCount)
    {
        this.totalCount = totalCount;
    }
}
