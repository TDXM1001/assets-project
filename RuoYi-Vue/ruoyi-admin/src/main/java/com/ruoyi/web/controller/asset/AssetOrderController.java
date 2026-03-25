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
import com.ruoyi.system.domain.asset.AssetOperateOrder;
import com.ruoyi.system.service.asset.IAssetOperateOrderService;

/**
 * 资产业务单据管理
 */
@RestController
@RequestMapping("/asset/order")
public class AssetOrderController extends BaseController
{
    @Autowired
    private IAssetOperateOrderService assetOperateOrderService;

    /**
     * 查询资产业务单据列表
     */
    @PreAuthorize("@ss.hasPermi('asset:order:list')")
    @GetMapping("/list")
    public TableDataInfo list(AssetOperateOrder assetOperateOrder)
    {
        startPage();
        List<AssetOperateOrder> list = assetOperateOrderService.selectAssetOperateOrderList(assetOperateOrder);
        return getDataTable(list);
    }

    /**
     * 导出资产业务单据
     */
    @PreAuthorize("@ss.hasPermi('asset:order:export')")
    @Log(title = "资产业务单据", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AssetOperateOrder assetOperateOrder)
    {
        List<AssetOperateOrder> list = assetOperateOrderService.selectAssetOperateOrderList(assetOperateOrder);
        ExcelUtil<AssetOperateOrder> util = new ExcelUtil<>(AssetOperateOrder.class);
        util.exportExcel(response, list, "资产业务单据数据");
    }

    /**
     * 查询资产业务单据详情
     */
    @PreAuthorize("@ss.hasPermi('asset:order:query')")
    @GetMapping("/{orderId}")
    public AjaxResult getInfo(@PathVariable Long orderId)
    {
        return success(assetOperateOrderService.selectAssetOperateOrderById(orderId));
    }

    /**
     * 新增资产业务单据
     */
    @PreAuthorize("@ss.hasPermi('asset:order:add')")
    @Log(title = "资产业务单据", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody AssetOperateOrder assetOperateOrder)
    {
        assetOperateOrder.setCreateBy(getUsername());
        return toAjax(assetOperateOrderService.insertAssetOperateOrder(assetOperateOrder));
    }

    /**
     * 修改资产业务单据
     */
    @PreAuthorize("@ss.hasPermi('asset:order:edit')")
    @Log(title = "资产业务单据", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody AssetOperateOrder assetOperateOrder)
    {
        assetOperateOrder.setUpdateBy(getUsername());
        return toAjax(assetOperateOrderService.updateAssetOperateOrder(assetOperateOrder));
    }

    /**
     * 删除资产业务单据
     */
    @PreAuthorize("@ss.hasPermi('asset:order:remove')")
    @Log(title = "资产业务单据", businessType = BusinessType.DELETE)
    @DeleteMapping("/{orderIds}")
    public AjaxResult remove(@PathVariable Long[] orderIds)
    {
        return toAjax(assetOperateOrderService.deleteAssetOperateOrderByIds(orderIds));
    }

    /**
     * 提交资产业务单据
     */
    @PreAuthorize("@ss.hasPermi('asset:order:submit')")
    @Log(title = "资产业务单据", businessType = BusinessType.UPDATE)
    @PostMapping("/submit/{orderId}")
    public AjaxResult submit(@PathVariable Long orderId)
    {
        return toAjax(assetOperateOrderService.submitAssetOperateOrder(orderId, getUsername()));
    }

    /**
     * 审批通过资产业务单据
     */
    @PreAuthorize("@ss.hasPermi('asset:order:approve')")
    @Log(title = "资产业务单据", businessType = BusinessType.UPDATE)
    @PostMapping("/approve/{orderId}")
    public AjaxResult approve(@PathVariable Long orderId, @RequestBody(required = false) Map<String, Object> body)
    {
        return toAjax(assetOperateOrderService.approveAssetOperateOrder(
            orderId,
            Convert.toStr(body == null ? null : body.get("remark")),
            getUserId(),
            getUsername()));
    }

    /**
     * 驳回资产业务单据
     */
    @PreAuthorize("@ss.hasPermi('asset:order:reject')")
    @Log(title = "资产业务单据", businessType = BusinessType.UPDATE)
    @PostMapping("/reject/{orderId}")
    public AjaxResult reject(@PathVariable Long orderId, @RequestBody(required = false) Map<String, Object> body)
    {
        return toAjax(assetOperateOrderService.rejectAssetOperateOrder(
            orderId,
            Convert.toStr(body == null ? null : body.get("remark")),
            getUserId(),
            getUsername()));
    }

    /**
     * 完成资产业务单据
     */
    @PreAuthorize("@ss.hasPermi('asset:order:finish')")
    @Log(title = "资产业务单据", businessType = BusinessType.UPDATE)
    @PostMapping("/finish/{orderId}")
    public AjaxResult finish(@PathVariable Long orderId)
    {
        return toAjax(assetOperateOrderService.finishAssetOperateOrder(orderId, getUsername()));
    }

    /**
     * 取消资产业务单据
     */
    @PreAuthorize("@ss.hasPermi('asset:order:cancel')")
    @Log(title = "资产业务单据", businessType = BusinessType.UPDATE)
    @PostMapping("/cancel/{orderId}")
    public AjaxResult cancel(@PathVariable Long orderId)
    {
        return toAjax(assetOperateOrderService.cancelAssetOperateOrder(orderId, getUsername()));
    }
}
