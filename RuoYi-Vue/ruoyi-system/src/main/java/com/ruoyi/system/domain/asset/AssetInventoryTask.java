package com.ruoyi.system.domain.asset;

import java.util.Date;
import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 资产盘点任务对象 asset_inventory_task
 */
public class AssetInventoryTask extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long taskId;

    @Excel(name = "任务编号")
    private String taskNo;

    @Excel(name = "任务名称")
    private String taskName;

    @NotBlank(message = "盘点范围不能为空")
    @Excel(name = "盘点范围")
    private String taskScopeType;

    @Excel(name = "任务状态")
    private String taskStatus;

    private Long targetDeptId;
    private Long targetLocationId;
    private Long targetCategoryId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date planStartTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date planEndTime;

    private Long ownerUserId;
    private Long executeUserId;

    @Excel(name = "应盘数量")
    private Integer summaryTotal;

    @Excel(name = "正常数量")
    private Integer summaryOk;

    @Excel(name = "差异数量")
    private Integer summaryDiff;

    private String status;
    private String delFlag;

    /**
     * 详情接口直接带出盘点明细，方便前端抽屉复用。
     */
    private List<AssetInventoryTaskItem> itemList;

    public Long getTaskId()
    {
        return taskId;
    }

    public void setTaskId(Long taskId)
    {
        this.taskId = taskId;
    }

    @NotBlank(message = "任务编号不能为空")
    @Size(max = 64, message = "任务编号长度不能超过64个字符")
    public String getTaskNo()
    {
        return taskNo;
    }

    public void setTaskNo(String taskNo)
    {
        this.taskNo = taskNo;
    }

    @NotBlank(message = "任务名称不能为空")
    @Size(max = 200, message = "任务名称长度不能超过200个字符")
    public String getTaskName()
    {
        return taskName;
    }

    public void setTaskName(String taskName)
    {
        this.taskName = taskName;
    }

    public String getTaskScopeType()
    {
        return taskScopeType;
    }

    public void setTaskScopeType(String taskScopeType)
    {
        this.taskScopeType = taskScopeType;
    }

    public String getTaskStatus()
    {
        return taskStatus;
    }

    public void setTaskStatus(String taskStatus)
    {
        this.taskStatus = taskStatus;
    }

    public Long getTargetDeptId()
    {
        return targetDeptId;
    }

    public void setTargetDeptId(Long targetDeptId)
    {
        this.targetDeptId = targetDeptId;
    }

    public Long getTargetLocationId()
    {
        return targetLocationId;
    }

    public void setTargetLocationId(Long targetLocationId)
    {
        this.targetLocationId = targetLocationId;
    }

    public Long getTargetCategoryId()
    {
        return targetCategoryId;
    }

    public void setTargetCategoryId(Long targetCategoryId)
    {
        this.targetCategoryId = targetCategoryId;
    }

    @NotNull(message = "计划开始时间不能为空")
    public Date getPlanStartTime()
    {
        return planStartTime;
    }

    public void setPlanStartTime(Date planStartTime)
    {
        this.planStartTime = planStartTime;
    }

    @NotNull(message = "计划结束时间不能为空")
    public Date getPlanEndTime()
    {
        return planEndTime;
    }

    public void setPlanEndTime(Date planEndTime)
    {
        this.planEndTime = planEndTime;
    }

    @NotNull(message = "负责人不能为空")
    public Long getOwnerUserId()
    {
        return ownerUserId;
    }

    public void setOwnerUserId(Long ownerUserId)
    {
        this.ownerUserId = ownerUserId;
    }

    @NotNull(message = "执行人不能为空")
    public Long getExecuteUserId()
    {
        return executeUserId;
    }

    public void setExecuteUserId(Long executeUserId)
    {
        this.executeUserId = executeUserId;
    }

    public Integer getSummaryTotal()
    {
        return summaryTotal;
    }

    public void setSummaryTotal(Integer summaryTotal)
    {
        this.summaryTotal = summaryTotal;
    }

    public Integer getSummaryOk()
    {
        return summaryOk;
    }

    public void setSummaryOk(Integer summaryOk)
    {
        this.summaryOk = summaryOk;
    }

    public Integer getSummaryDiff()
    {
        return summaryDiff;
    }

    public void setSummaryDiff(Integer summaryDiff)
    {
        this.summaryDiff = summaryDiff;
    }

    @NotBlank(message = "状态不能为空")
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

    public List<AssetInventoryTaskItem> getItemList()
    {
        return itemList;
    }

    public void setItemList(List<AssetInventoryTaskItem> itemList)
    {
        this.itemList = itemList;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("taskId", getTaskId())
            .append("taskNo", getTaskNo())
            .append("taskName", getTaskName())
            .append("taskScopeType", getTaskScopeType())
            .append("taskStatus", getTaskStatus())
            .append("targetDeptId", getTargetDeptId())
            .append("targetLocationId", getTargetLocationId())
            .append("targetCategoryId", getTargetCategoryId())
            .append("planStartTime", getPlanStartTime())
            .append("planEndTime", getPlanEndTime())
            .append("ownerUserId", getOwnerUserId())
            .append("executeUserId", getExecuteUserId())
            .append("summaryTotal", getSummaryTotal())
            .append("summaryOk", getSummaryOk())
            .append("summaryDiff", getSummaryDiff())
            .append("status", getStatus())
            .append("delFlag", getDelFlag())
            .append("remark", getRemark())
            .build();
    }
}
