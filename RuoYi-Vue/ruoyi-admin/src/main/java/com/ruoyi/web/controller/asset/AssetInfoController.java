package com.ruoyi.web.controller.asset;

import java.util.List;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.asset.AssetInfo;
import com.ruoyi.system.service.asset.IAssetInfoService;

/**
 * 资产台账管理
 */
@RestController
@RequestMapping("/asset/info")
public class AssetInfoController extends BaseController
{
    @Autowired
    private IAssetInfoService assetInfoService;

    /**
     * 查询资产台账列表
     */
    @PreAuthorize("@ss.hasPermi('asset:info:list')")
    @GetMapping("/list")
    public TableDataInfo list(AssetInfo assetInfo)
    {
        startPage();
        List<AssetInfo> list = assetInfoService.selectAssetInfoList(assetInfo);
        return getDataTable(list);
    }

    /**
     * 导出资产台账
     */
    @PreAuthorize("@ss.hasPermi('asset:info:export')")
    @Log(title = "资产台账", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AssetInfo assetInfo)
    {
        List<AssetInfo> list = assetInfoService.selectAssetInfoList(assetInfo);
        ExcelUtil<AssetInfo> util = new ExcelUtil<>(AssetInfo.class);
        util.exportExcel(response, list, "资产台账数据");
    }

    /**
     * 导入资产台账
     */
    @PreAuthorize("@ss.hasPermi('asset:info:import')")
    @Log(title = "资产台账", businessType = BusinessType.IMPORT)
    @PostMapping("/importData")
    public AjaxResult importData(@RequestParam("file") MultipartFile file,
        @RequestParam(value = "updateSupport", defaultValue = "false") boolean updateSupport) throws Exception
    {
        ExcelUtil<AssetInfo> util = new ExcelUtil<>(AssetInfo.class);
        List<AssetInfo> assetList = util.importExcel(file.getInputStream());
        String message = assetInfoService.importAssetInfo(assetList, updateSupport, getUsername());
        return success(message);
    }

    /**
     * 下载资产台账导入模板
     */
    @PreAuthorize("@ss.hasPermi('asset:info:import')")
    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response)
    {
        ExcelUtil<AssetInfo> util = new ExcelUtil<>(AssetInfo.class);
        util.importTemplateExcel(response, "资产台账数据");
    }

    /**
     * 查询资产台账详情
     */
    @PreAuthorize("@ss.hasPermi('asset:info:query')")
    @GetMapping("/{assetId}")
    public AjaxResult getInfo(@PathVariable Long assetId)
    {
        return success(assetInfoService.selectAssetInfoById(assetId));
    }

    /**
     * 新增资产台账
     */
    @PreAuthorize("@ss.hasPermi('asset:info:add')")
    @Log(title = "资产台账", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody AssetInfo assetInfo)
    {
        if (!assetInfoService.checkAssetCodeUnique(assetInfo))
        {
            return error("新增资产失败，资产编码已存在");
        }
        assetInfo.setCreateBy(getUsername());
        return toAjax(assetInfoService.insertAssetInfo(assetInfo));
    }

    /**
     * 修改资产台账
     */
    @PreAuthorize("@ss.hasPermi('asset:info:edit')")
    @Log(title = "资产台账", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody AssetInfo assetInfo)
    {
        if (!assetInfoService.checkAssetCodeUnique(assetInfo))
        {
            return error("修改资产失败，资产编码已存在");
        }
        assetInfo.setUpdateBy(getUsername());
        return toAjax(assetInfoService.updateAssetInfo(assetInfo));
    }

    /**
     * 删除资产台账
     */
    @PreAuthorize("@ss.hasPermi('asset:info:remove')")
    @Log(title = "资产台账", businessType = BusinessType.DELETE)
    @DeleteMapping("/{assetIds}")
    public AjaxResult remove(@PathVariable Long[] assetIds)
    {
        return toAjax(assetInfoService.deleteAssetInfoByIds(assetIds));
    }
}
