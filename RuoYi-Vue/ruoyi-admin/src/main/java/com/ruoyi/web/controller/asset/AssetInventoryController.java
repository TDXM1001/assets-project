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
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.asset.AssetInventoryTask;
import com.ruoyi.system.domain.asset.AssetInventoryTaskItem;
import com.ruoyi.system.service.asset.IAssetInventoryTaskService;

/**
 * 资产盘点任务管理
 */
@RestController
@RequestMapping("/asset/inventory")
public class AssetInventoryController extends BaseController
{
    @Autowired
    private IAssetInventoryTaskService inventoryTaskService;

    /**
     * 查询盘点任务列表
     */
    @PreAuthorize("@ss.hasPermi('asset:inventory:list')")
    @GetMapping("/list")
    public TableDataInfo list(AssetInventoryTask task)
    {
        startPage();
        List<AssetInventoryTask> list = inventoryTaskService.selectAssetInventoryTaskList(task);
        return getDataTable(list);
    }

    /**
     * 导出盘点任务
     */
    @PreAuthorize("@ss.hasPermi('asset:inventory:export')")
    @Log(title = "资产盘点任务", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AssetInventoryTask task)
    {
        List<AssetInventoryTask> list = inventoryTaskService.selectAssetInventoryTaskList(task);
        ExcelUtil<AssetInventoryTask> util = new ExcelUtil<>(AssetInventoryTask.class);
        util.exportExcel(response, list, "资产盘点任务数据");
    }

    /**
     * 查询盘点任务详情
     */
    @PreAuthorize("@ss.hasPermi('asset:inventory:query')")
    @GetMapping("/{taskId}")
    public AjaxResult getInfo(@PathVariable Long taskId)
    {
        return success(inventoryTaskService.selectAssetInventoryTaskById(taskId));
    }

    /**
     * 查询盘点任务明细
     */
    @PreAuthorize("@ss.hasPermi('asset:inventory:query')")
    @GetMapping("/{taskId}/items")
    public AjaxResult getItems(@PathVariable Long taskId)
    {
        List<AssetInventoryTaskItem> list = inventoryTaskService.selectAssetInventoryTaskItemsByTaskId(taskId);
        return success(list);
    }

    /**
     * 新增盘点任务
     */
    @PreAuthorize("@ss.hasPermi('asset:inventory:add')")
    @Log(title = "资产盘点任务", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody AssetInventoryTask task)
    {
        if (!inventoryTaskService.checkTaskNoUnique(task))
        {
            return error("新增盘点任务失败，任务编号已存在");
        }
        task.setCreateBy(getUsername());
        task.setUpdateBy(getUsername());
        return toAjax(inventoryTaskService.insertAssetInventoryTask(task));
    }

    /**
     * 修改盘点任务
     */
    @PreAuthorize("@ss.hasPermi('asset:inventory:edit')")
    @Log(title = "资产盘点任务", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody AssetInventoryTask task)
    {
        if (!inventoryTaskService.checkTaskNoUnique(task))
        {
            return error("修改盘点任务失败，任务编号已存在");
        }
        task.setUpdateBy(getUsername());
        return toAjax(inventoryTaskService.updateAssetInventoryTask(task));
    }

    /**
     * 删除盘点任务
     */
    @PreAuthorize("@ss.hasPermi('asset:inventory:remove')")
    @Log(title = "资产盘点任务", businessType = BusinessType.DELETE)
    @DeleteMapping("/{taskIds}")
    public AjaxResult remove(@PathVariable Long[] taskIds)
    {
        return toAjax(inventoryTaskService.deleteAssetInventoryTaskByIds(taskIds));
    }

    /**
     * 开始盘点
     */
    @PreAuthorize("@ss.hasPermi('asset:inventory:start')")
    @Log(title = "资产盘点任务", businessType = BusinessType.UPDATE)
    @PostMapping("/start/{taskId}")
    public AjaxResult start(@PathVariable Long taskId)
    {
        return toAjax(inventoryTaskService.startAssetInventoryTask(taskId, getUserId(), getUsername()));
    }

    /**
     * 盘点录入
     */
    @PreAuthorize("@ss.hasPermi('asset:inventory:start')")
    @Log(title = "资产盘点任务", businessType = BusinessType.UPDATE)
    @PostMapping("/{taskId}/scan")
    public AjaxResult scan(@PathVariable Long taskId, @RequestBody Map<String, Object> body)
    {
        String scanCode = body == null ? null : StringUtils.trim((String) body.get("scanCode"));
        return toAjax(inventoryTaskService.scanAssetInventoryTask(taskId, scanCode, getUserId(), getUsername()));
    }

    /**
     * 结束盘点
     */
    @PreAuthorize("@ss.hasPermi('asset:inventory:finish')")
    @Log(title = "资产盘点任务", businessType = BusinessType.UPDATE)
    @PostMapping("/finish/{taskId}")
    public AjaxResult finish(@PathVariable Long taskId)
    {
        return toAjax(inventoryTaskService.finishAssetInventoryTask(taskId, getUserId(), getUsername()));
    }

    /**
     * 处理盘点差异
     */
    @PreAuthorize("@ss.hasPermi('asset:inventory:processDiff')")
    @Log(title = "资产盘点任务", businessType = BusinessType.UPDATE)
    @PostMapping("/processDiff/{taskId}")
    @SuppressWarnings("unchecked")
    public AjaxResult processDiff(@PathVariable Long taskId, @RequestBody Map<String, Object> body)
    {
        List<Long> itemIds = null;
        if (body != null && body.get("itemIds") instanceof List<?> rawList)
        {
            itemIds = rawList.stream()
                .filter(item -> item != null)
                .map(item -> Long.valueOf(String.valueOf(item)))
                .toList();
        }
        String processStatus = body == null ? null : (String) body.get("processStatus");
        String processDesc = body == null ? null : (String) body.get("processDesc");
        return toAjax(inventoryTaskService.processAssetInventoryDiff(taskId, itemIds, processStatus, processDesc, getUsername()));
    }
}
