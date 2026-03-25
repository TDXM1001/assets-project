package com.ruoyi.system.domain.asset;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 资产事件流水对象 asset_event_log
 */
public class AssetEventLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long eventId;

    private Long assetId;

    @Excel(name = "资产编码")
    private String assetCode;

    @Excel(name = "资产名称")
    private String assetName;

    private String assetStatus;

    @Excel(name = "事件类型")
    private String eventType;

    private Long sourceOrderId;

    @Excel(name = "来源业务类型")
    private String sourceOrderType;

    @Excel(name = "来源单据编号")
    private String sourceOrderNo;

    private String beforeSnapshot;

    private String afterSnapshot;

    @Excel(name = "事件说明")
    private String eventDesc;

    private Long operatorUserId;

    @Excel(name = "操作人")
    private String operatorUserName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "操作时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date operateTime;

    private String beginOperateTime;

    private String endOperateTime;

    /**
     * 抽屉接口会按资产读取最近几条流水，这里复用同一套查询模型承接 limit。
     */
    private Integer limitSize;

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

    public String getAssetCode()
    {
        return assetCode;
    }

    public void setAssetCode(String assetCode)
    {
        this.assetCode = assetCode;
    }

    public String getAssetName()
    {
        return assetName;
    }

    public void setAssetName(String assetName)
    {
        this.assetName = assetName;
    }

    public String getAssetStatus()
    {
        return assetStatus;
    }

    public void setAssetStatus(String assetStatus)
    {
        this.assetStatus = assetStatus;
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

    public String getSourceOrderNo()
    {
        return sourceOrderNo;
    }

    public void setSourceOrderNo(String sourceOrderNo)
    {
        this.sourceOrderNo = sourceOrderNo;
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

    public String getOperatorUserName()
    {
        return operatorUserName;
    }

    public void setOperatorUserName(String operatorUserName)
    {
        this.operatorUserName = operatorUserName;
    }

    public Date getOperateTime()
    {
        return operateTime;
    }

    public void setOperateTime(Date operateTime)
    {
        this.operateTime = operateTime;
    }

    public String getBeginOperateTime()
    {
        return beginOperateTime;
    }

    public void setBeginOperateTime(String beginOperateTime)
    {
        this.beginOperateTime = beginOperateTime;
    }

    public String getEndOperateTime()
    {
        return endOperateTime;
    }

    public void setEndOperateTime(String endOperateTime)
    {
        this.endOperateTime = endOperateTime;
    }

    public Integer getLimitSize()
    {
        return limitSize;
    }

    public void setLimitSize(Integer limitSize)
    {
        this.limitSize = limitSize;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("eventId", getEventId())
            .append("assetId", getAssetId())
            .append("assetCode", getAssetCode())
            .append("assetName", getAssetName())
            .append("eventType", getEventType())
            .append("sourceOrderId", getSourceOrderId())
            .append("sourceOrderType", getSourceOrderType())
            .append("sourceOrderNo", getSourceOrderNo())
            .append("eventDesc", getEventDesc())
            .append("operatorUserId", getOperatorUserId())
            .append("operatorUserName", getOperatorUserName())
            .append("operateTime", getOperateTime())
            .toString();
    }
}
