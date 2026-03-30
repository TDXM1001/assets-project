package com.ruoyi.system.domain.asset;

import java.util.ArrayList;
import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 资产分类对象 asset_category
 */
public class AssetCategory extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long categoryId;

    private Long parentId;

    private String ancestors;

    @Excel(name = "分类编码")
    private String categoryCode;

    @Excel(name = "分类名称")
    private String categoryName;

    @Excel(name = "显示顺序")
    private Integer orderNum;

    private String depreciableFlag;

    private String serialRequiredFlag;

    private String borrowableFlag;

    private String inventoryRequiredFlag;

    private Integer usefulLifeMonths;

    private Integer fieldTemplateVersion;

    private String fieldTemplateStatus;

    private String fieldTemplateJson;
    
    private String assetType;


    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    private String delFlag;

    private String parentName;

    private List<AssetCategory> children = new ArrayList<>();

    public Long getCategoryId()
    {
        return categoryId;
    }

    public void setCategoryId(Long categoryId)
    {
        this.categoryId = categoryId;
    }

    @NotNull(message = "父分类不能为空")
    public Long getParentId()
    {
        return parentId;
    }

    public void setParentId(Long parentId)
    {
        this.parentId = parentId;
    }

    public String getAncestors()
    {
        return ancestors;
    }

    public void setAncestors(String ancestors)
    {
        this.ancestors = ancestors;
    }

    @NotBlank(message = "分类编码不能为空")
    @Size(max = 64, message = "分类编码长度不能超过64个字符")
    public String getCategoryCode()
    {
        return categoryCode;
    }

    public void setCategoryCode(String categoryCode)
    {
        this.categoryCode = categoryCode;
    }

    @NotBlank(message = "分类名称不能为空")
    @Size(max = 100, message = "分类名称长度不能超过100个字符")
    public String getCategoryName()
    {
        return categoryName;
    }

    public void setCategoryName(String categoryName)
    {
        this.categoryName = categoryName;
    }

    @NotNull(message = "显示顺序不能为空")
    public Integer getOrderNum()
    {
        return orderNum;
    }

    public void setOrderNum(Integer orderNum)
    {
        this.orderNum = orderNum;
    }

    public String getDepreciableFlag()
    {
        return depreciableFlag;
    }

    public void setDepreciableFlag(String depreciableFlag)
    {
        this.depreciableFlag = depreciableFlag;
    }

    public String getSerialRequiredFlag()
    {
        return serialRequiredFlag;
    }

    public void setSerialRequiredFlag(String serialRequiredFlag)
    {
        this.serialRequiredFlag = serialRequiredFlag;
    }

    public String getBorrowableFlag()
    {
        return borrowableFlag;
    }

    public void setBorrowableFlag(String borrowableFlag)
    {
        this.borrowableFlag = borrowableFlag;
    }

    public String getInventoryRequiredFlag()
    {
        return inventoryRequiredFlag;
    }

    public void setInventoryRequiredFlag(String inventoryRequiredFlag)
    {
        this.inventoryRequiredFlag = inventoryRequiredFlag;
    }

    public Integer getUsefulLifeMonths()
    {
        return usefulLifeMonths;
    }

    public void setUsefulLifeMonths(Integer usefulLifeMonths)
    {
        this.usefulLifeMonths = usefulLifeMonths;
    }

    public Integer getFieldTemplateVersion()
    {
        return fieldTemplateVersion;
    }

    public void setFieldTemplateVersion(Integer fieldTemplateVersion)
    {
        this.fieldTemplateVersion = fieldTemplateVersion;
    }

    public String getFieldTemplateStatus()
    {
        return fieldTemplateStatus;
    }

    public void setFieldTemplateStatus(String fieldTemplateStatus)
    {
        this.fieldTemplateStatus = fieldTemplateStatus;
    }

    public String getFieldTemplateJson()
    {
        return fieldTemplateJson;
    }

    public void setFieldTemplateJson(String fieldTemplateJson)
    {
        this.fieldTemplateJson = fieldTemplateJson;
    }

    public String getAssetType()
    {
        return assetType;
    }

    public void setAssetType(String assetType)
    {
        this.assetType = assetType;
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

    public String getParentName()
    {
        return parentName;
    }

    public void setParentName(String parentName)
    {
        this.parentName = parentName;
    }

    public List<AssetCategory> getChildren()
    {
        return children;
    }

    public void setChildren(List<AssetCategory> children)
    {
        this.children = children;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("categoryId", getCategoryId())
            .append("parentId", getParentId())
            .append("ancestors", getAncestors())
            .append("categoryCode", getCategoryCode())
            .append("categoryName", getCategoryName())
            .append("orderNum", getOrderNum())
            .append("fieldTemplateVersion", getFieldTemplateVersion())
            .append("fieldTemplateStatus", getFieldTemplateStatus())
            .append("status", getStatus())
            .append("assetType", getAssetType())
            .append("delFlag", getDelFlag())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
