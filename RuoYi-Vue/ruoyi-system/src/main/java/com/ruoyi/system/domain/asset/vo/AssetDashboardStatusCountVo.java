package com.ruoyi.system.domain.asset.vo;

/**
 * 资产状态分布统计
 */
public class AssetDashboardStatusCountVo
{
    /**
     * 状态值
     */
    private String status;

    /**
     * 状态名称
     */
    private String label;

    /**
     * 数量
     */
    private Long totalCount;

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
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
