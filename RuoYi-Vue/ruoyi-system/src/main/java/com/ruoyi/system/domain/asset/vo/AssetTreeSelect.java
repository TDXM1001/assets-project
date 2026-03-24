package com.ruoyi.system.domain.asset.vo;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.asset.AssetCategory;
import com.ruoyi.system.domain.asset.AssetLocation;

/**
 * 资产模块树下拉结构
 */
public class AssetTreeSelect implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long id;

    private String label;

    private boolean disabled;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<AssetTreeSelect> children;

    public AssetTreeSelect()
    {
    }

    public AssetTreeSelect(AssetCategory category)
    {
        this.id = category.getCategoryId();
        this.label = category.getCategoryName();
        this.disabled = StringUtils.equals(UserConstants.DEPT_DISABLE, category.getStatus());
        this.children = category.getChildren().stream().map(AssetTreeSelect::new).collect(Collectors.toList());
    }

    public AssetTreeSelect(AssetLocation location)
    {
        this.id = location.getLocationId();
        this.label = location.getLocationName();
        this.disabled = StringUtils.equals(UserConstants.DEPT_DISABLE, location.getStatus());
        this.children = location.getChildren().stream().map(AssetTreeSelect::new).collect(Collectors.toList());
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    public boolean isDisabled()
    {
        return disabled;
    }

    public void setDisabled(boolean disabled)
    {
        this.disabled = disabled;
    }

    public List<AssetTreeSelect> getChildren()
    {
        return children;
    }

    public void setChildren(List<AssetTreeSelect> children)
    {
        this.children = children;
    }
}
