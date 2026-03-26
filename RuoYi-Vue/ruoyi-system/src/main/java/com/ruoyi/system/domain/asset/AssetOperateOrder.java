package com.ruoyi.system.domain.asset;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 资产业务单据对象 asset_operate_order
 */
public class AssetOperateOrder extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long orderId;

    @Excel(name = "单据编号")
    private String orderNo;

    @NotBlank(message = "单据类型不能为空")
    @Excel(name = "单据类型")
    private String orderType;
    private String sourceBizType;
    private Long sourceBizId;
    private String sourceBizNo;

    @Excel(name = "单据状态")
    private String orderStatus;

    @NotNull(message = "业务时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "业务时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date bizDate;

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
    private Long fromDeptId;
    private String fromDeptName;
    private Long toDeptId;
    private String toDeptName;
    private Long fromUserId;
    private String fromUserName;
    private Long toUserId;
    private String toUserName;
    private Long fromLocationId;
    private String fromLocationName;
    private Long toLocationId;
    private String toLocationName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "预计归还日期", width = 20, dateFormat = "yyyy-MM-dd")
    private Date expectedReturnDate;

    @Size(max = 500, message = "报废原因长度不能超过500个字符")
    private String disposalReason;

    @Excel(name = "处置金额")
    private BigDecimal disposalAmount;

    @Excel(name = "附件数量")
    private Integer attachmentCount;

    private String status;
    private String delFlag;
    private String bizDateStart;
    private String bizDateEnd;
    private Long excludeOrderId;
    private List<AssetOperateOrderItem> itemList;

    public Long getOrderId()
    {
        return orderId;
    }

    public void setOrderId(Long orderId)
    {
        this.orderId = orderId;
    }

    @Size(max = 64, message = "单据编号长度不能超过64个字符")
    public String getOrderNo()
    {
        return orderNo;
    }

    public void setOrderNo(String orderNo)
    {
        this.orderNo = orderNo;
    }

    public String getOrderType()
    {
        return orderType;
    }

    public void setOrderType(String orderType)
    {
        this.orderType = orderType;
    }

    public String getSourceBizType()
    {
        return sourceBizType;
    }

    public void setSourceBizType(String sourceBizType)
    {
        this.sourceBizType = sourceBizType;
    }

    public Long getSourceBizId()
    {
        return sourceBizId;
    }

    public void setSourceBizId(Long sourceBizId)
    {
        this.sourceBizId = sourceBizId;
    }

    @Size(max = 64, message = "来源业务单号长度不能超过64个字符")
    public String getSourceBizNo()
    {
        return sourceBizNo;
    }

    public void setSourceBizNo(String sourceBizNo)
    {
        this.sourceBizNo = sourceBizNo;
    }

    public String getOrderStatus()
    {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus)
    {
        this.orderStatus = orderStatus;
    }

    public Date getBizDate()
    {
        return bizDate;
    }

    public void setBizDate(Date bizDate)
    {
        this.bizDate = bizDate;
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

    public Long getFromDeptId()
    {
        return fromDeptId;
    }

    public void setFromDeptId(Long fromDeptId)
    {
        this.fromDeptId = fromDeptId;
    }

    public String getFromDeptName()
    {
        return fromDeptName;
    }

    public void setFromDeptName(String fromDeptName)
    {
        this.fromDeptName = fromDeptName;
    }

    public Long getToDeptId()
    {
        return toDeptId;
    }

    public void setToDeptId(Long toDeptId)
    {
        this.toDeptId = toDeptId;
    }

    public String getToDeptName()
    {
        return toDeptName;
    }

    public void setToDeptName(String toDeptName)
    {
        this.toDeptName = toDeptName;
    }

    public Long getFromUserId()
    {
        return fromUserId;
    }

    public void setFromUserId(Long fromUserId)
    {
        this.fromUserId = fromUserId;
    }

    public String getFromUserName()
    {
        return fromUserName;
    }

    public void setFromUserName(String fromUserName)
    {
        this.fromUserName = fromUserName;
    }

    public Long getToUserId()
    {
        return toUserId;
    }

    public void setToUserId(Long toUserId)
    {
        this.toUserId = toUserId;
    }

    public String getToUserName()
    {
        return toUserName;
    }

    public void setToUserName(String toUserName)
    {
        this.toUserName = toUserName;
    }

    public Long getFromLocationId()
    {
        return fromLocationId;
    }

    public void setFromLocationId(Long fromLocationId)
    {
        this.fromLocationId = fromLocationId;
    }

    public String getFromLocationName()
    {
        return fromLocationName;
    }

    public void setFromLocationName(String fromLocationName)
    {
        this.fromLocationName = fromLocationName;
    }

    public Long getToLocationId()
    {
        return toLocationId;
    }

    public void setToLocationId(Long toLocationId)
    {
        this.toLocationId = toLocationId;
    }

    public String getToLocationName()
    {
        return toLocationName;
    }

    public void setToLocationName(String toLocationName)
    {
        this.toLocationName = toLocationName;
    }

    public Date getExpectedReturnDate()
    {
        return expectedReturnDate;
    }

    public void setExpectedReturnDate(Date expectedReturnDate)
    {
        this.expectedReturnDate = expectedReturnDate;
    }

    public String getDisposalReason()
    {
        return disposalReason;
    }

    public void setDisposalReason(String disposalReason)
    {
        this.disposalReason = disposalReason;
    }

    public BigDecimal getDisposalAmount()
    {
        return disposalAmount;
    }

    public void setDisposalAmount(BigDecimal disposalAmount)
    {
        this.disposalAmount = disposalAmount;
    }

    public Integer getAttachmentCount()
    {
        return attachmentCount;
    }

    public void setAttachmentCount(Integer attachmentCount)
    {
        this.attachmentCount = attachmentCount;
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

    public String getBizDateStart()
    {
        return bizDateStart;
    }

    public void setBizDateStart(String bizDateStart)
    {
        this.bizDateStart = bizDateStart;
    }

    public String getBizDateEnd()
    {
        return bizDateEnd;
    }

    public void setBizDateEnd(String bizDateEnd)
    {
        this.bizDateEnd = bizDateEnd;
    }

    public Long getExcludeOrderId()
    {
        return excludeOrderId;
    }

    public void setExcludeOrderId(Long excludeOrderId)
    {
        this.excludeOrderId = excludeOrderId;
    }

    public List<AssetOperateOrderItem> getItemList()
    {
        return itemList;
    }

    public void setItemList(List<AssetOperateOrderItem> itemList)
    {
        this.itemList = itemList;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("orderId", getOrderId())
            .append("orderNo", getOrderNo())
            .append("orderType", getOrderType())
            .append("sourceBizType", getSourceBizType())
            .append("sourceBizId", getSourceBizId())
            .append("sourceBizNo", getSourceBizNo())
            .append("orderStatus", getOrderStatus())
            .append("bizDate", getBizDate())
            .append("applyUserId", getApplyUserId())
            .append("applyDeptId", getApplyDeptId())
            .append("approveUserId", getApproveUserId())
            .append("approveTime", getApproveTime())
            .append("approveResult", getApproveResult())
            .append("fromDeptId", getFromDeptId())
            .append("toDeptId", getToDeptId())
            .append("fromUserId", getFromUserId())
            .append("toUserId", getToUserId())
            .append("fromLocationId", getFromLocationId())
            .append("toLocationId", getToLocationId())
            .append("expectedReturnDate", getExpectedReturnDate())
            .append("disposalReason", getDisposalReason())
            .append("disposalAmount", getDisposalAmount())
            .append("attachmentCount", getAttachmentCount())
            .append("status", getStatus())
            .append("delFlag", getDelFlag())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .append("excludeOrderId", getExcludeOrderId())
            .append("itemList", getItemList())
            .toString();
    }
}
