package com.ruoyi.system.domain.asset.vo;

import java.util.ArrayList;
import java.util.List;

/**
 * 资产看板趋势数据
 */
public class AssetDashboardTrendVo
{
    /**
     * 趋势天数
     */
    private Integer days;

    /**
     * 趋势点列表
     */
    private List<AssetDashboardTrendPointVo> pointList = new ArrayList<>();

    public Integer getDays()
    {
        return days;
    }

    public void setDays(Integer days)
    {
        this.days = days;
    }

    public List<AssetDashboardTrendPointVo> getPointList()
    {
        return pointList;
    }

    public void setPointList(List<AssetDashboardTrendPointVo> pointList)
    {
        this.pointList = pointList;
    }
}
