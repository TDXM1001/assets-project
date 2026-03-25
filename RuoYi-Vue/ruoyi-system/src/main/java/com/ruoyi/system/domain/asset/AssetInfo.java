package com.ruoyi.system.domain.asset;

import java.math.BigDecimal;
import java.util.Date;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 资产主档对象 asset_info
 */
public class AssetInfo extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long assetId;

    @Excel(name = "资产编码")
    private String assetCode;

    @Excel(name = "资产名称")
    private String assetName;

    @NotNull(message = "资产分类不能为空")
    @Excel(name = "分类ID")
    private Long categoryId;

    private String brand;

    private String model;

    private String specification;

    @Excel(name = "序列号")
    private String serialNo;

    @Excel(name = "资产状态", dictType = "asset_status", comboReadDict = true)
    private String assetStatus;

    @Excel(name = "资产来源", dictType = "asset_source", comboReadDict = true)
    private String assetSource;

    @Excel(name = "所属部门ID")
    private Long useOrgDeptId;

    @Excel(name = "管理部门ID")
    private Long manageDeptId;

    @Excel(name = "责任人ID")
    private Long currentUserId;

    @Excel(name = "当前位置ID")
    private Long currentLocationId;

    @Excel(name = "采购日期", width = 20, dateFormat = "yyyy-MM-dd")
    private Date purchaseDate;

    @Excel(name = "入库日期", width = 20, dateFormat = "yyyy-MM-dd")
    private Date inboundDate;

    @Excel(name = "启用日期", width = 20, dateFormat = "yyyy-MM-dd")
    private Date startUseDate;

    @Excel(name = "原值")
    private BigDecimal originalValue;

    @Excel(name = "残值")
    private BigDecimal residualValue;

    @Excel(name = "保修截止日期", width = 20, dateFormat = "yyyy-MM-dd")
    private Date warrantyExpireDate;

    @Excel(name = "供应商")
    private String supplierName;

    @Excel(name = "二维码")
    private String qrCode;

    @Excel(name = "版本号")
    private Integer versionNo;

    @Excel(name = "台账状态", dictType = "sys_normal_disable", comboReadDict = true)
    private String status;

    private String delFlag;

    public Long getAssetId()
    {
        return assetId;
    }

    public void setAssetId(Long assetId)
    {
        this.assetId = assetId;
    }

    @NotBlank(message = "资产编码不能为空")
    @Size(max = 64, message = "资产编码长度不能超过64个字符")
    public String getAssetCode()
    {
        return assetCode;
    }

    public void setAssetCode(String assetCode)
    {
        this.assetCode = assetCode;
    }

    @NotBlank(message = "资产名称不能为空")
    @Size(max = 200, message = "资产名称长度不能超过200个字符")
    public String getAssetName()
    {
        return assetName;
    }

    public void setAssetName(String assetName)
    {
        this.assetName = assetName;
    }

    public Long getCategoryId()
    {
        return categoryId;
    }

    public void setCategoryId(Long categoryId)
    {
        this.categoryId = categoryId;
    }

    public String getBrand()
    {
        return brand;
    }

    public void setBrand(String brand)
    {
        this.brand = brand;
    }

    public String getModel()
    {
        return model;
    }

    public void setModel(String model)
    {
        this.model = model;
    }

    public String getSpecification()
    {
        return specification;
    }

    public void setSpecification(String specification)
    {
        this.specification = specification;
    }

    public String getSerialNo()
    {
        return serialNo;
    }

    public void setSerialNo(String serialNo)
    {
        this.serialNo = serialNo;
    }

    @NotBlank(message = "资产状态不能为空")
    public String getAssetStatus()
    {
        return assetStatus;
    }

    public void setAssetStatus(String assetStatus)
    {
        this.assetStatus = assetStatus;
    }

    public String getAssetSource()
    {
        return assetSource;
    }

    public void setAssetSource(String assetSource)
    {
        this.assetSource = assetSource;
    }

    public Long getUseOrgDeptId()
    {
        return useOrgDeptId;
    }

    public void setUseOrgDeptId(Long useOrgDeptId)
    {
        this.useOrgDeptId = useOrgDeptId;
    }

    public Long getManageDeptId()
    {
        return manageDeptId;
    }

    public void setManageDeptId(Long manageDeptId)
    {
        this.manageDeptId = manageDeptId;
    }

    public Long getCurrentUserId()
    {
        return currentUserId;
    }

    public void setCurrentUserId(Long currentUserId)
    {
        this.currentUserId = currentUserId;
    }

    public Long getCurrentLocationId()
    {
        return currentLocationId;
    }

    public void setCurrentLocationId(Long currentLocationId)
    {
        this.currentLocationId = currentLocationId;
    }

    public Date getPurchaseDate()
    {
        return purchaseDate;
    }

    public void setPurchaseDate(Date purchaseDate)
    {
        this.purchaseDate = purchaseDate;
    }

    public Date getInboundDate()
    {
        return inboundDate;
    }

    public void setInboundDate(Date inboundDate)
    {
        this.inboundDate = inboundDate;
    }

    public Date getStartUseDate()
    {
        return startUseDate;
    }

    public void setStartUseDate(Date startUseDate)
    {
        this.startUseDate = startUseDate;
    }

    public BigDecimal getOriginalValue()
    {
        return originalValue;
    }

    public void setOriginalValue(BigDecimal originalValue)
    {
        this.originalValue = originalValue;
    }

    public BigDecimal getResidualValue()
    {
        return residualValue;
    }

    public void setResidualValue(BigDecimal residualValue)
    {
        this.residualValue = residualValue;
    }

    public Date getWarrantyExpireDate()
    {
        return warrantyExpireDate;
    }

    public void setWarrantyExpireDate(Date warrantyExpireDate)
    {
        this.warrantyExpireDate = warrantyExpireDate;
    }

    public String getSupplierName()
    {
        return supplierName;
    }

    public void setSupplierName(String supplierName)
    {
        this.supplierName = supplierName;
    }

    public String getQrCode()
    {
        return qrCode;
    }

    public void setQrCode(String qrCode)
    {
        this.qrCode = qrCode;
    }

    public Integer getVersionNo()
    {
        return versionNo;
    }

    public void setVersionNo(Integer versionNo)
    {
        this.versionNo = versionNo;
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
            .append("assetId", getAssetId())
            .append("assetCode", getAssetCode())
            .append("assetName", getAssetName())
            .append("categoryId", getCategoryId())
            .append("assetStatus", getAssetStatus())
            .append("useOrgDeptId", getUseOrgDeptId())
            .append("manageDeptId", getManageDeptId())
            .append("currentUserId", getCurrentUserId())
            .append("currentLocationId", getCurrentLocationId())
            .append("originalValue", getOriginalValue())
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
