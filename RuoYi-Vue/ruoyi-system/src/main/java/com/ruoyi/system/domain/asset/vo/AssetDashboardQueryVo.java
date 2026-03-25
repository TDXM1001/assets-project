package com.ruoyi.system.domain.asset.vo;

import java.util.Date;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 资产看板聚合查询参数
 */
public class AssetDashboardQueryVo extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /**
     * 统计最近天数，默认由服务层兜底
     */
    private Integer days;

    /**
     * 聚合统计开始时间
     */
    private Date beginTime;

    /**
     * 聚合统计结束时间，按左闭右开处理
     */
    private Date endTime;

    public Integer getDays()
    {
        return days;
    }

    public void setDays(Integer days)
    {
        this.days = days;
    }

    public Date getBeginTime()
    {
        return beginTime;
    }

    public void setBeginTime(Date beginTime)
    {
        this.beginTime = beginTime;
    }

    public Date getEndTime()
    {
        return endTime;
    }

    public void setEndTime(Date endTime)
    {
        this.endTime = endTime;
    }
}
