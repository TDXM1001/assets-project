package com.ruoyi.system.domain.asset;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 资产盘点任务明细对象 asset_inventory_task_item
 */
public class AssetInventoryTaskItem extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long itemId;
    private Long taskId;
    private Long assetId;

    @Excel(name = "资产编码")
    private String assetCode;

    @Excel(name = "资产名称")
    private String assetName;

    private Long expectedUserId;
    private Long actualUserId;
    private String expectedUserName;
    private String actualUserName;
    private Long expectedLocationId;
    private Long actualLocationId;
    private String expectedLocationName;
    private String actualLocationName;
    private String expectedStatus;
    private String actualStatus;

    @Excel(name = "盘点结果")
    private String inventoryResult;

    @Excel(name = "差异说明")
    private String inventoryDesc;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date inventoryTime;

    private Long inventoryUserId;
    private String inventoryUserName;

    @Excel(name = "处理状态")
    private String processStatus;

    @Excel(name = "处理说明")
    private String processDesc;

    public Long getItemId()
    {
        return itemId;
    }

    public void setItemId(Long itemId)
    {
        this.itemId = itemId;
    }

    public Long getTaskId()
    {
        return taskId;
    }

    public void setTaskId(Long taskId)
    {
        this.taskId = taskId;
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

    public Long getExpectedUserId()
    {
        return expectedUserId;
    }

    public void setExpectedUserId(Long expectedUserId)
    {
        this.expectedUserId = expectedUserId;
    }

    public Long getActualUserId()
    {
        return actualUserId;
    }

    public void setActualUserId(Long actualUserId)
    {
        this.actualUserId = actualUserId;
    }

    public String getExpectedUserName()
    {
        return expectedUserName;
    }

    public void setExpectedUserName(String expectedUserName)
    {
        this.expectedUserName = expectedUserName;
    }

    public String getActualUserName()
    {
        return actualUserName;
    }

    public void setActualUserName(String actualUserName)
    {
        this.actualUserName = actualUserName;
    }

    public Long getExpectedLocationId()
    {
        return expectedLocationId;
    }

    public void setExpectedLocationId(Long expectedLocationId)
    {
        this.expectedLocationId = expectedLocationId;
    }

    public Long getActualLocationId()
    {
        return actualLocationId;
    }

    public void setActualLocationId(Long actualLocationId)
    {
        this.actualLocationId = actualLocationId;
    }

    public String getExpectedLocationName()
    {
        return expectedLocationName;
    }

    public void setExpectedLocationName(String expectedLocationName)
    {
        this.expectedLocationName = expectedLocationName;
    }

    public String getActualLocationName()
    {
        return actualLocationName;
    }

    public void setActualLocationName(String actualLocationName)
    {
        this.actualLocationName = actualLocationName;
    }

    public String getExpectedStatus()
    {
        return expectedStatus;
    }

    public void setExpectedStatus(String expectedStatus)
    {
        this.expectedStatus = expectedStatus;
    }

    public String getActualStatus()
    {
        return actualStatus;
    }

    public void setActualStatus(String actualStatus)
    {
        this.actualStatus = actualStatus;
    }

    public String getInventoryResult()
    {
        return inventoryResult;
    }

    public void setInventoryResult(String inventoryResult)
    {
        this.inventoryResult = inventoryResult;
    }

    public String getInventoryDesc()
    {
        return inventoryDesc;
    }

    public void setInventoryDesc(String inventoryDesc)
    {
        this.inventoryDesc = inventoryDesc;
    }

    public Date getInventoryTime()
    {
        return inventoryTime;
    }

    public void setInventoryTime(Date inventoryTime)
    {
        this.inventoryTime = inventoryTime;
    }

    public Long getInventoryUserId()
    {
        return inventoryUserId;
    }

    public void setInventoryUserId(Long inventoryUserId)
    {
        this.inventoryUserId = inventoryUserId;
    }

    public String getInventoryUserName()
    {
        return inventoryUserName;
    }

    public void setInventoryUserName(String inventoryUserName)
    {
        this.inventoryUserName = inventoryUserName;
    }

    public String getProcessStatus()
    {
        return processStatus;
    }

    public void setProcessStatus(String processStatus)
    {
        this.processStatus = processStatus;
    }

    public String getProcessDesc()
    {
        return processDesc;
    }

    public void setProcessDesc(String processDesc)
    {
        this.processDesc = processDesc;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("itemId", getItemId())
            .append("taskId", getTaskId())
            .append("assetId", getAssetId())
            .append("assetCode", getAssetCode())
            .append("assetName", getAssetName())
            .append("expectedUserId", getExpectedUserId())
            .append("actualUserId", getActualUserId())
            .append("expectedUserName", getExpectedUserName())
            .append("actualUserName", getActualUserName())
            .append("expectedLocationId", getExpectedLocationId())
            .append("actualLocationId", getActualLocationId())
            .append("expectedLocationName", getExpectedLocationName())
            .append("actualLocationName", getActualLocationName())
            .append("expectedStatus", getExpectedStatus())
            .append("actualStatus", getActualStatus())
            .append("inventoryResult", getInventoryResult())
            .append("inventoryDesc", getInventoryDesc())
            .append("inventoryTime", getInventoryTime())
            .append("inventoryUserId", getInventoryUserId())
            .append("inventoryUserName", getInventoryUserName())
            .append("processStatus", getProcessStatus())
            .append("processDesc", getProcessDesc())
            .build();
    }
}
