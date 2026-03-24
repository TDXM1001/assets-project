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
 * 资产位置对象 asset_location
 */
public class AssetLocation extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long locationId;

    private Long parentId;

    private String ancestors;

    @Excel(name = "位置编码")
    private String locationCode;

    @Excel(name = "位置名称")
    private String locationName;

    private String locationType;

    private Long deptId;

    private Long managerUserId;

    @Excel(name = "显示顺序")
    private Integer orderNum;

    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    private String delFlag;

    private String parentName;

    private List<AssetLocation> children = new ArrayList<>();

    public Long getLocationId()
    {
        return locationId;
    }

    public void setLocationId(Long locationId)
    {
        this.locationId = locationId;
    }

    @NotNull(message = "父位置不能为空")
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

    @NotBlank(message = "位置编码不能为空")
    @Size(max = 64, message = "位置编码长度不能超过64个字符")
    public String getLocationCode()
    {
        return locationCode;
    }

    public void setLocationCode(String locationCode)
    {
        this.locationCode = locationCode;
    }

    @NotBlank(message = "位置名称不能为空")
    @Size(max = 100, message = "位置名称长度不能超过100个字符")
    public String getLocationName()
    {
        return locationName;
    }

    public void setLocationName(String locationName)
    {
        this.locationName = locationName;
    }

    public String getLocationType()
    {
        return locationType;
    }

    public void setLocationType(String locationType)
    {
        this.locationType = locationType;
    }

    public Long getDeptId()
    {
        return deptId;
    }

    public void setDeptId(Long deptId)
    {
        this.deptId = deptId;
    }

    public Long getManagerUserId()
    {
        return managerUserId;
    }

    public void setManagerUserId(Long managerUserId)
    {
        this.managerUserId = managerUserId;
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

    public List<AssetLocation> getChildren()
    {
        return children;
    }

    public void setChildren(List<AssetLocation> children)
    {
        this.children = children;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("locationId", getLocationId())
            .append("parentId", getParentId())
            .append("ancestors", getAncestors())
            .append("locationCode", getLocationCode())
            .append("locationName", getLocationName())
            .append("deptId", getDeptId())
            .append("managerUserId", getManagerUserId())
            .append("orderNum", getOrderNum())
            .append("status", getStatus())
            .append("delFlag", getDelFlag())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
