package com.ruoyi.system.domain.asset;

import java.io.Serializable;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 资产业务单据明细对象 asset_operate_order_item
 */
public class AssetOperateOrderItem implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long itemId;
    private Long orderId;
    private Long assetId;
    private String assetCode;
    private String assetName;
    private String beforeStatus;
    private String afterStatus;
    private Long beforeUserId;
    private Long afterUserId;
    private String beforeUserName;
    private String afterUserName;
    private Long beforeDeptId;
    private Long afterDeptId;
    private String beforeDeptName;
    private String afterDeptName;
    private Long beforeLocationId;
    private Long afterLocationId;
    private String beforeLocationName;
    private String afterLocationName;
    private String itemStatus;
    private String itemResult;

    public Long getItemId()
    {
        return itemId;
    }

    public void setItemId(Long itemId)
    {
        this.itemId = itemId;
    }

    public Long getOrderId()
    {
        return orderId;
    }

    public void setOrderId(Long orderId)
    {
        this.orderId = orderId;
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

    public String getBeforeStatus()
    {
        return beforeStatus;
    }

    public void setBeforeStatus(String beforeStatus)
    {
        this.beforeStatus = beforeStatus;
    }

    public String getAfterStatus()
    {
        return afterStatus;
    }

    public void setAfterStatus(String afterStatus)
    {
        this.afterStatus = afterStatus;
    }

    public Long getBeforeUserId()
    {
        return beforeUserId;
    }

    public void setBeforeUserId(Long beforeUserId)
    {
        this.beforeUserId = beforeUserId;
    }

    public Long getAfterUserId()
    {
        return afterUserId;
    }

    public void setAfterUserId(Long afterUserId)
    {
        this.afterUserId = afterUserId;
    }

    public String getBeforeUserName()
    {
        return beforeUserName;
    }

    public void setBeforeUserName(String beforeUserName)
    {
        this.beforeUserName = beforeUserName;
    }

    public String getAfterUserName()
    {
        return afterUserName;
    }

    public void setAfterUserName(String afterUserName)
    {
        this.afterUserName = afterUserName;
    }

    public Long getBeforeDeptId()
    {
        return beforeDeptId;
    }

    public void setBeforeDeptId(Long beforeDeptId)
    {
        this.beforeDeptId = beforeDeptId;
    }

    public Long getAfterDeptId()
    {
        return afterDeptId;
    }

    public void setAfterDeptId(Long afterDeptId)
    {
        this.afterDeptId = afterDeptId;
    }

    public String getBeforeDeptName()
    {
        return beforeDeptName;
    }

    public void setBeforeDeptName(String beforeDeptName)
    {
        this.beforeDeptName = beforeDeptName;
    }

    public String getAfterDeptName()
    {
        return afterDeptName;
    }

    public void setAfterDeptName(String afterDeptName)
    {
        this.afterDeptName = afterDeptName;
    }

    public Long getBeforeLocationId()
    {
        return beforeLocationId;
    }

    public void setBeforeLocationId(Long beforeLocationId)
    {
        this.beforeLocationId = beforeLocationId;
    }

    public Long getAfterLocationId()
    {
        return afterLocationId;
    }

    public void setAfterLocationId(Long afterLocationId)
    {
        this.afterLocationId = afterLocationId;
    }

    public String getBeforeLocationName()
    {
        return beforeLocationName;
    }

    public void setBeforeLocationName(String beforeLocationName)
    {
        this.beforeLocationName = beforeLocationName;
    }

    public String getAfterLocationName()
    {
        return afterLocationName;
    }

    public void setAfterLocationName(String afterLocationName)
    {
        this.afterLocationName = afterLocationName;
    }

    public String getItemStatus()
    {
        return itemStatus;
    }

    public void setItemStatus(String itemStatus)
    {
        this.itemStatus = itemStatus;
    }

    public String getItemResult()
    {
        return itemResult;
    }

    public void setItemResult(String itemResult)
    {
        this.itemResult = itemResult;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("itemId", getItemId())
            .append("orderId", getOrderId())
            .append("assetId", getAssetId())
            .append("assetCode", getAssetCode())
            .append("assetName", getAssetName())
            .append("beforeStatus", getBeforeStatus())
            .append("afterStatus", getAfterStatus())
            .append("beforeUserId", getBeforeUserId())
            .append("afterUserId", getAfterUserId())
            .append("beforeUserName", getBeforeUserName())
            .append("afterUserName", getAfterUserName())
            .append("beforeDeptId", getBeforeDeptId())
            .append("afterDeptId", getAfterDeptId())
            .append("beforeDeptName", getBeforeDeptName())
            .append("afterDeptName", getAfterDeptName())
            .append("beforeLocationId", getBeforeLocationId())
            .append("afterLocationId", getAfterLocationId())
            .append("beforeLocationName", getBeforeLocationName())
            .append("afterLocationName", getAfterLocationName())
            .append("itemStatus", getItemStatus())
            .append("itemResult", getItemResult())
            .toString();
    }
}
