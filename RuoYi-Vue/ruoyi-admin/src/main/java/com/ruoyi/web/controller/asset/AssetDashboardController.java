package com.ruoyi.web.controller.asset;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.system.service.asset.IAssetDashboardService;

/**
 * 资产看板聚合接口
 */
@RestController
@RequestMapping("/asset/dashboard")
public class AssetDashboardController extends BaseController
{
    @Autowired
    private IAssetDashboardService assetDashboardService;

    /**
     * 查询看板汇总卡片
     */
    @PreAuthorize("@ss.hasPermi('asset:dashboard:view')")
    @GetMapping("/summary")
    public AjaxResult summary()
    {
        return success(assetDashboardService.selectDashboardSummary());
    }

    /**
     * 查询看板待办计数
     */
    @PreAuthorize("@ss.hasPermi('asset:dashboard:view')")
    @GetMapping("/todo")
    public AjaxResult todo()
    {
        return success(assetDashboardService.selectDashboardTodo());
    }

    /**
     * 查询看板近 7 天趋势
     */
    @PreAuthorize("@ss.hasPermi('asset:dashboard:view')")
    @GetMapping("/trend")
    public AjaxResult trend()
    {
        return success(assetDashboardService.selectDashboardTrend());
    }
}
