package com.ruoyi.web.controller.asset;

import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.asset.AssetRepairOrder;
import com.ruoyi.system.service.asset.IAssetRepairOrderService;

/**
 * 资产维修单管理
 */
@RestController
@RequestMapping("/asset/repair")
public class AssetRepairController extends BaseController
{
    @Autowired
    private IAssetRepairOrderService assetRepairOrderService;

    /**
     * 查询资产维修单列表
     */
    @PreAuthorize("@ss.hasPermi('asset:repair:list')")
    @GetMapping("/list")
    public TableDataInfo list(AssetRepairOrder assetRepairOrder)
    {
        startPage();
        List<AssetRepairOrder> list = assetRepairOrderService.selectAssetRepairOrderList(assetRepairOrder);
        return getDataTable(list);
    }

    /**
     * 导出资产维修单
     */
    @PreAuthorize("@ss.hasPermi('asset:repair:export')")
    @Log(title = "资产维修单", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AssetRepairOrder assetRepairOrder)
    {
        List<AssetRepairOrder> list = assetRepairOrderService.selectAssetRepairOrderList(assetRepairOrder);
        ExcelUtil<AssetRepairOrder> util = new ExcelUtil<>(AssetRepairOrder.class);
        util.exportExcel(response, list, "资产维修单数据");
    }

    /**
     * 查询资产维修单详情
     */
    @PreAuthorize("@ss.hasPermi('asset:repair:query')")
    @GetMapping("/{repairId}")
    public AjaxResult getInfo(@PathVariable Long repairId)
    {
        return success(assetRepairOrderService.selectAssetRepairOrderById(repairId));
    }

    /**
     * 新增资产维修单
     */
    @PreAuthorize("@ss.hasPermi('asset:repair:add')")
    @Log(title = "资产维修单", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody AssetRepairOrder assetRepairOrder)
    {
        assetRepairOrder.setCreateBy(getUsername());
        assetRepairOrder.setUpdateBy(getUsername());
        return toAjax(assetRepairOrderService.insertAssetRepairOrder(assetRepairOrder));
    }

    /**
     * 修改资产维修单
     */
    @PreAuthorize("@ss.hasPermi('asset:repair:edit')")
    @Log(title = "资产维修单", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody AssetRepairOrder assetRepairOrder)
    {
        assetRepairOrder.setUpdateBy(getUsername());
        return toAjax(assetRepairOrderService.updateAssetRepairOrder(assetRepairOrder));
    }

    /**
     * 删除资产维修单
     */
    @PreAuthorize("@ss.hasPermi('asset:repair:remove')")
    @Log(title = "资产维修单", businessType = BusinessType.DELETE)
    @DeleteMapping("/{repairIds}")
    public AjaxResult remove(@PathVariable Long[] repairIds)
    {
        return toAjax(assetRepairOrderService.deleteAssetRepairOrderByIds(repairIds));
    }

    /**
     * 提交资产维修单
     */
    @PreAuthorize("@ss.hasPermi('asset:repair:submit')")
    @Log(title = "资产维修单", businessType = BusinessType.UPDATE)
    @PostMapping("/submit/{repairId}")
    public AjaxResult submit(@PathVariable Long repairId)
    {
        return toAjax(assetRepairOrderService.submitAssetRepairOrder(repairId, getUsername()));
    }

    /**
     * 审批通过资产维修单
     */
    @PreAuthorize("@ss.hasPermi('asset:repair:approve')")
    @Log(title = "资产维修单", businessType = BusinessType.UPDATE)
    @PostMapping("/approve/{repairId}")
    public AjaxResult approve(@PathVariable Long repairId, @RequestBody(required = false) Map<String, Object> body)
    {
        return toAjax(assetRepairOrderService.approveAssetRepairOrder(
            repairId,
            Convert.toStr(body == null ? null : body.get("remark")),
            getUserId(),
            getUsername()));
    }

    /**
     * 驳回资产维修单
     */
    @PreAuthorize("@ss.hasPermi('asset:repair:reject')")
    @Log(title = "资产维修单", businessType = BusinessType.UPDATE)
    @PostMapping("/reject/{repairId}")
    public AjaxResult reject(@PathVariable Long repairId, @RequestBody(required = false) Map<String, Object> body)
    {
        return toAjax(assetRepairOrderService.rejectAssetRepairOrder(
            repairId,
            Convert.toStr(body == null ? null : body.get("remark")),
            getUserId(),
            getUsername()));
    }

    /**
     * 完成资产维修单
     */
    @PreAuthorize("@ss.hasPermi('asset:repair:finish')")
    @Log(title = "资产维修单", businessType = BusinessType.UPDATE)
    @PostMapping("/finish/{repairId}")
    public AjaxResult finish(@PathVariable Long repairId, @RequestBody(required = false) AssetRepairOrder assetRepairOrder)
    {
        AssetRepairOrder finishPayload = assetRepairOrder == null ? new AssetRepairOrder() : assetRepairOrder;
        finishPayload.setRepairId(repairId);
        finishPayload.setUpdateBy(getUsername());
        return toAjax(assetRepairOrderService.finishAssetRepairOrder(repairId, finishPayload, getUsername()));
    }

    /**
     * 取消资产维修单
     */
    @PreAuthorize("@ss.hasPermi('asset:repair:cancel')")
    @Log(title = "资产维修单", businessType = BusinessType.UPDATE)
    @PostMapping("/cancel/{repairId}")
    public AjaxResult cancel(@PathVariable Long repairId)
    {
        return toAjax(assetRepairOrderService.cancelAssetRepairOrder(repairId, getUsername()));
    }
}
