package com.ruoyi.system.domain.asset;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 资产维修单对象 asset_repair_order
 *
 * 兼容说明：
 * 1. 单头字段继续保留，旧前端和历史报表仍然可以读取 assetId、assetCode、faultDesc 等字段。
 * 2. 多资产维修通过 itemList 承载明细，服务层会将首条明细回写到单头快照字段。
 */
public class AssetRepairOrder extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long repairId;

    @Excel(name = "维修单号")
    private String repairNo;

    private Long assetId;

    @Excel(name = "资产编码")
    private String assetCode;

    @Excel(name = "资产名称")
    private String assetName;

    @Excel(name = "维修状态")
    private String repairStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "报修时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date reportTime;

    private Long applyUserId;
    private String applyUserName;
    private Long applyDeptId;
    private String applyDeptName;
    private Long approveUserId;
    private String approveUserName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "审批时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date approveTime;

    private String approveResult;
    private String faultDesc;

    @Excel(name = "送修方式")
    private String repairMode;

    @Excel(name = "维修供应商")
    private String vendorName;

    @Excel(name = "维修费用")
    private BigDecimal repairCost;

    @Excel(name = "停用时长(小时)")
    private BigDecimal downtimeHours;

    @Excel(name = "是否返修")
    private String reworkFlag;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "送修时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date sendRepairTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "完成时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date finishTime;

    @Excel(name = "完成结果")
    private String resultType;

    @Excel(name = "维修前状态")
    private String beforeStatus;

    @Excel(name = "维修后状态")
    private String afterStatus;

    @Excel(name = "附件数量")
    private Integer attachmentCount;

    @Excel(name = "缁翠慨璧勪骇鏁伴噺")
    private Integer itemCount;

    private String status;
    private String delFlag;
    private String reportTimeStart;
    private String reportTimeEnd;

    /** 维修明细列表 */
    private List<AssetRepairOrderItem> itemList;

    public Long getRepairId()
    {
        return repairId;
    }

    public void setRepairId(Long repairId)
    {
        this.repairId = repairId;
    }

    @Size(max = 64, message = "维修单号长度不能超过64个字符")
    public String getRepairNo()
    {
        return repairNo;
    }

    public void setRepairNo(String repairNo)
    {
        this.repairNo = repairNo;
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

    public String getRepairStatus()
    {
        return repairStatus;
    }

    public void setRepairStatus(String repairStatus)
    {
        this.repairStatus = repairStatus;
    }

    public Date getReportTime()
    {
        return reportTime;
    }

    public void setReportTime(Date reportTime)
    {
        this.reportTime = reportTime;
    }

    public Long getApplyUserId()
    {
        return applyUserId;
    }

    public void setApplyUserId(Long applyUserId)
    {
        this.applyUserId = applyUserId;
    }

    public String getApplyUserName()
    {
        return applyUserName;
    }

    public void setApplyUserName(String applyUserName)
    {
        this.applyUserName = applyUserName;
    }

    public Long getApplyDeptId()
    {
        return applyDeptId;
    }

    public void setApplyDeptId(Long applyDeptId)
    {
        this.applyDeptId = applyDeptId;
    }

    public String getApplyDeptName()
    {
        return applyDeptName;
    }

    public void setApplyDeptName(String applyDeptName)
    {
        this.applyDeptName = applyDeptName;
    }

    public Long getApproveUserId()
    {
        return approveUserId;
    }

    public void setApproveUserId(Long approveUserId)
    {
        this.approveUserId = approveUserId;
    }

    public String getApproveUserName()
    {
        return approveUserName;
    }

    public void setApproveUserName(String approveUserName)
    {
        this.approveUserName = approveUserName;
    }

    public Date getApproveTime()
    {
        return approveTime;
    }

    public void setApproveTime(Date approveTime)
    {
        this.approveTime = approveTime;
    }

    public String getApproveResult()
    {
        return approveResult;
    }

    public void setApproveResult(String approveResult)
    {
        this.approveResult = approveResult;
    }

    @Size(max = 500, message = "故障描述长度不能超过500个字符")
    public String getFaultDesc()
    {
        return faultDesc;
    }

    public void setFaultDesc(String faultDesc)
    {
        this.faultDesc = faultDesc;
    }

    public String getRepairMode()
    {
        return repairMode;
    }

    public void setRepairMode(String repairMode)
    {
        this.repairMode = repairMode;
    }

    public String getVendorName()
    {
        return vendorName;
    }

    public void setVendorName(String vendorName)
    {
        this.vendorName = vendorName;
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

    public Date getSendRepairTime()
    {
        return sendRepairTime;
    }

    public void setSendRepairTime(Date sendRepairTime)
    {
        this.sendRepairTime = sendRepairTime;
    }

    public Date getFinishTime()
    {
        return finishTime;
    }

    public void setFinishTime(Date finishTime)
    {
        this.finishTime = finishTime;
    }

    public String getResultType()
    {
        return resultType;
    }

    public void setResultType(String resultType)
    {
        this.resultType = resultType;
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

    public Integer getAttachmentCount()
    {
        return attachmentCount;
    }

    public void setAttachmentCount(Integer attachmentCount)
    {
        this.attachmentCount = attachmentCount;
    }

    public Integer getItemCount()
    {
        return itemCount;
    }

    public void setItemCount(Integer itemCount)
    {
        this.itemCount = itemCount;
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

    public String getReportTimeStart()
    {
        return reportTimeStart;
    }

    public void setReportTimeStart(String reportTimeStart)
    {
        this.reportTimeStart = reportTimeStart;
    }

    public String getReportTimeEnd()
    {
        return reportTimeEnd;
    }

    public void setReportTimeEnd(String reportTimeEnd)
    {
        this.reportTimeEnd = reportTimeEnd;
    }

    public List<AssetRepairOrderItem> getItemList()
    {
        return itemList;
    }

    public void setItemList(List<AssetRepairOrderItem> itemList)
    {
        this.itemList = itemList;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("repairId", getRepairId())
            .append("repairNo", getRepairNo())
            .append("assetId", getAssetId())
            .append("assetCode", getAssetCode())
            .append("assetName", getAssetName())
            .append("repairStatus", getRepairStatus())
            .append("reportTime", getReportTime())
            .append("applyUserId", getApplyUserId())
            .append("applyDeptId", getApplyDeptId())
            .append("approveUserId", getApproveUserId())
            .append("approveTime", getApproveTime())
            .append("approveResult", getApproveResult())
            .append("faultDesc", getFaultDesc())
            .append("repairMode", getRepairMode())
            .append("vendorName", getVendorName())
            .append("repairCost", getRepairCost())
            .append("downtimeHours", getDowntimeHours())
            .append("reworkFlag", getReworkFlag())
            .append("sendRepairTime", getSendRepairTime())
            .append("finishTime", getFinishTime())
            .append("resultType", getResultType())
            .append("beforeStatus", getBeforeStatus())
            .append("afterStatus", getAfterStatus())
            .append("attachmentCount", getAttachmentCount())
            .append("itemCount", getItemCount())
            .append("status", getStatus())
            .append("delFlag", getDelFlag())
            .append("itemList", getItemList())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
