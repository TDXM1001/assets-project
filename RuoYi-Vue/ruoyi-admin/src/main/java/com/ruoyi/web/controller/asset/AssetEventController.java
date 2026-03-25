package com.ruoyi.web.controller.asset;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.asset.AssetEventLog;
import com.ruoyi.system.service.asset.IAssetEventLogService;

/**
 * 资产事件流水查询
 */
@RestController
@RequestMapping("/asset/event")
public class AssetEventController extends BaseController
{
    @Autowired
    private IAssetEventLogService assetEventLogService;

    /**
     * 查询资产事件流水列表
     */
    @PreAuthorize("@ss.hasPermi('asset:event:list')")
    @GetMapping("/list")
    public TableDataInfo list(AssetEventLog assetEventLog)
    {
        startPage();
        List<AssetEventLog> list = assetEventLogService.selectAssetEventLogList(assetEventLog);
        return getDataTable(list);
    }

    /**
     * 导出资产事件流水
     */
    @PreAuthorize("@ss.hasPermi('asset:event:export')")
    @Log(title = "资产事件流水", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AssetEventLog assetEventLog)
    {
        List<AssetEventLog> list = assetEventLogService.selectAssetEventLogList(assetEventLog);
        ExcelUtil<AssetEventLog> util = new ExcelUtil<>(AssetEventLog.class);
        util.exportExcel(response, list, "资产事件流水数据");
    }

    /**
     * 查询资产事件流水详情
     */
    @PreAuthorize("@ss.hasPermi('asset:event:query')")
    @GetMapping("/{eventId}")
    public AjaxResult getInfo(@PathVariable Long eventId)
    {
        return success(assetEventLogService.selectAssetEventLogById(eventId));
    }

    /**
     * 查询某项资产最近的流水记录
     */
    @PreAuthorize("@ss.hasPermi('asset:event:query')")
    @GetMapping("/asset/{assetId}")
    public AjaxResult getAssetEvents(@PathVariable Long assetId,
        @RequestParam(value = "limit", required = false) Integer limit)
    {
        return success(assetEventLogService.selectRecentAssetEventLogListByAssetId(assetId, limit));
    }
}
