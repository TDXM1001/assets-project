package com.ruoyi.system.domain.asset;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 维修单明细对象 asset_repair_order_item
 *
 * 设计说明：
 * 1. 单头继续承载审批、完成等流程状态，保证旧接口和旧菜单权限不需要大改。
 * 2. 明细逐条记录资产、故障、前后状态和处理结果，用来承接一单多资产维修。
 */
public class AssetRepairOrderItem extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 明细ID */
    private Long repairItemId;

    /** 维修单ID */
    private Long repairId;

    /** 资产ID */
    private Long assetId;

    /** 资产编码 */
    private String assetCode;

    /** 资产名称 */
    private String assetName;

    /** 故障描述 */
    private String faultDesc;

    /** 维修前状态 */
    private String beforeStatus;

    /** 维修后状态 */
    private String afterStatus;

    /** 完成结果 */
    private String resultType;

    /** 维修费用 */
    private BigDecimal repairCost;

    /** 停用时长 */
    private BigDecimal downtimeHours;

    /** 是否返修 */
    private String reworkFlag;

    /** 排序号 */
    private Integer sortOrder;

    /** 状态 */
    private String status;

    /** 删除标记 */
    private String delFlag;

    public Long getRepairItemId()
    {
        return repairItemId;
    }

    public void setRepairItemId(Long repairItemId)
    {
        this.repairItemId = repairItemId;
    }

    public Long getRepairId()
    {
        return repairId;
    }

    public void setRepairId(Long repairId)
    {
        this.repairId = repairId;
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

    public String getFaultDesc()
    {
        return faultDesc;
    }

    public void setFaultDesc(String faultDesc)
    {
        this.faultDesc = faultDesc;
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

    public String getResultType()
    {
        return resultType;
    }

    public void setResultType(String resultType)
    {
        this.resultType = resultType;
    }

    public BigDecimal getRepairCost()
    {
        return repairCost;
    }

    public void setRepairCost(BigDecimal repairCost)
    {
        this.repairCost = repairCost;
    }

    public BigDecimal getDowntimeHours()
    {
        return downtimeHours;
    }

    public void setDowntimeHours(BigDecimal downtimeHours)
    {
        this.downtimeHours = downtimeHours;
    }

    public String getReworkFlag()
    {
        return reworkFlag;
    }

    public void setReworkFlag(String reworkFlag)
    {
        this.reworkFlag = reworkFlag;
    }

    public Integer getSortOrder()
    {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder)
    {
        this.sortOrder = sortOrder;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("repairItemId", getRepairItemId())
            .append("repairId", getRepairId())
            .append("assetId", getAssetId())
            .append("assetCode", getAssetCode())
            .append("assetName", getAssetName())
            .append("faultDesc", getFaultDesc())
            .append("beforeStatus", getBeforeStatus())
            .append("afterStatus", getAfterStatus())
            .append("resultType", getResultType())
            .append("repairCost", getRepairCost())
            .append("downtimeHours", getDowntimeHours())
            .append("reworkFlag", getReworkFlag())
            .append("sortOrder", getSortOrder())
            .append("status", getStatus())
            .append("delFlag", getDelFlag())
            .append("remark", getRemark())
            .toString();
    }
}
