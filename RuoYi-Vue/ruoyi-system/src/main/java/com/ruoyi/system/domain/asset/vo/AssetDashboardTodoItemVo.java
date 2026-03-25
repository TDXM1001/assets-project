package com.ruoyi.system.domain.asset.vo;

/**
 * 资产看板待办项
 */
public class AssetDashboardTodoItemVo
{
    /**
     * 待办标识
     */
    private String key;

    /**
     * 待办名称
     */
    private String label;

    /**
     * 待办数量
     */
    private Long count = 0L;

    /**
     * 当前角色是否可见该模块
     */
    private Boolean permitted = Boolean.FALSE;

    /**
     * 前端跳转路径
     */
    private String routePath;

    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    public Long getCount()
    {
        return count;
    }

    public void setCount(Long count)
    {
        this.count = count;
    }

    public Boolean getPermitted()
    {
        return permitted;
    }

    public void setPermitted(Boolean permitted)
    {
        this.permitted = permitted;
    }

    public String getRoutePath()
    {
        return routePath;
    }

    public void setRoutePath(String routePath)
    {
        this.routePath = routePath;
    }
}
