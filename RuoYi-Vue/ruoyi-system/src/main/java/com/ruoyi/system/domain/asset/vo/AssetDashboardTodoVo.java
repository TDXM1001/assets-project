package com.ruoyi.system.domain.asset.vo;

import java.util.ArrayList;
import java.util.List;

/**
 * 资产看板待办数据
 */
public class AssetDashboardTodoVo
{
    /**
     * 待办项列表
     */
    private List<AssetDashboardTodoItemVo> itemList = new ArrayList<>();

    public List<AssetDashboardTodoItemVo> getItemList()
    {
        return itemList;
    }

    public void setItemList(List<AssetDashboardTodoItemVo> itemList)
    {
        this.itemList = itemList;
    }
}
