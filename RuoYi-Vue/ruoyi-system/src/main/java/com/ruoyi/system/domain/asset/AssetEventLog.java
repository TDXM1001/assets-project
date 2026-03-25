package com.ruoyi.system.domain.asset;

import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 资产事件流水对象 asset_event_log
 */
public class AssetEventLog
{
    private Long eventId;

    private Long assetId;

    private String eventType;

    private Long sourceOrderId;

    private String sourceOrderType;

    private String beforeSnapshot;

    private String afterSnapshot;

    private String eventDesc;

    private Long operatorUserId;

    private Date operateTime;

    public Long getEventId()
    {
        return eventId;
    }

    public void setEventId(Long eventId)
    {
        this.eventId = eventId;
    }

    public Long getAssetId()
    {
        return assetId;
    }

    public void setAssetId(Long assetId)
    {
        this.assetId = assetId;
    }

    public String getEventType()
    {
        return eventType;
    }

    public void setEventType(String eventType)
    {
        this.eventType = eventType;
    }

    public Long getSourceOrderId()
    {
        return sourceOrderId;
    }

    public void setSourceOrderId(Long sourceOrderId)
    {
        this.sourceOrderId = sourceOrderId;
    }

    public String getSourceOrderType()
    {
        return sourceOrderType;
    }

    public void setSourceOrderType(String sourceOrderType)
    {
        this.sourceOrderType = sourceOrderType;
    }

    public String getBeforeSnapshot()
    {
        return beforeSnapshot;
    }

    public void setBeforeSnapshot(String beforeSnapshot)
    {
        this.beforeSnapshot = beforeSnapshot;
    }

    public String getAfterSnapshot()
    {
        return afterSnapshot;
    }

    public void setAfterSnapshot(String afterSnapshot)
    {
        this.afterSnapshot = afterSnapshot;
    }

    public String getEventDesc()
    {
        return eventDesc;
    }

    public void setEventDesc(String eventDesc)
    {
        this.eventDesc = eventDesc;
    }

    public Long getOperatorUserId()
    {
        return operatorUserId;
    }

    public void setOperatorUserId(Long operatorUserId)
    {
        this.operatorUserId = operatorUserId;
    }

    public Date getOperateTime()
    {
        return operateTime;
    }

    public void setOperateTime(Date operateTime)
    {
        this.operateTime = operateTime;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("eventId", getEventId())
            .append("assetId", getAssetId())
            .append("eventType", getEventType())
            .append("sourceOrderId", getSourceOrderId())
            .append("sourceOrderType", getSourceOrderType())
            .append("eventDesc", getEventDesc())
            .append("operatorUserId", getOperatorUserId())
            .append("operateTime", getOperateTime())
            .toString();
    }
}
