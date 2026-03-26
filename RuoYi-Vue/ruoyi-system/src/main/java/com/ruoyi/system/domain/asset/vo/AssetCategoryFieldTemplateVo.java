package com.ruoyi.system.domain.asset.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 分类字段模板
 */
public class AssetCategoryFieldTemplateVo implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long categoryId;

    private Integer templateVersion;

    private String status;

    private List<AssetCategoryFieldTemplateFieldVo> fields = new ArrayList<>();

    public Long getCategoryId()
    {
        return categoryId;
    }

    public void setCategoryId(Long categoryId)
    {
        this.categoryId = categoryId;
    }

    public Integer getTemplateVersion()
    {
        return templateVersion;
    }

    public void setTemplateVersion(Integer templateVersion)
    {
        this.templateVersion = templateVersion;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public List<AssetCategoryFieldTemplateFieldVo> getFields()
    {
        return fields;
    }

    public void setFields(List<AssetCategoryFieldTemplateFieldVo> fields)
    {
        this.fields = fields;
    }
}
