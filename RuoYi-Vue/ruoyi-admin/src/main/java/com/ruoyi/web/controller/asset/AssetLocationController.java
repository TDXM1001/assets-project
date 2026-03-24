package com.ruoyi.web.controller.asset;

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
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.domain.asset.AssetLocation;
import com.ruoyi.system.service.asset.IAssetLocationService;

/**
 * 资产位置管理
 */
@RestController
@RequestMapping("/asset/location")
public class AssetLocationController extends BaseController
{
    @Autowired
    private IAssetLocationService locationService;

    /**
     * 查询资产位置列表
     */
    @PreAuthorize("@ss.hasPermi('asset:location:list')")
    @GetMapping("/list")
    public AjaxResult list(AssetLocation location)
    {
        return success(locationService.selectLocationList(location));
    }

    /**
     * 查询资产位置树
     */
    @PreAuthorize("@ss.hasPermi('asset:location:list')")
    @GetMapping("/treeSelect")
    public AjaxResult treeSelect(AssetLocation location)
    {
        return success(locationService.selectLocationTreeList(location));
    }

    /**
     * 查询资产位置详情
     */
    @PreAuthorize("@ss.hasPermi('asset:location:query')")
    @GetMapping("/{locationId}")
    public AjaxResult getInfo(@PathVariable Long locationId)
    {
        return success(locationService.selectLocationById(locationId));
    }

    /**
     * 新增资产位置
     */
    @PreAuthorize("@ss.hasPermi('asset:location:add')")
    @Log(title = "资产位置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody AssetLocation location)
    {
        if (!locationService.checkLocationCodeUnique(location))
        {
            return error("新增位置失败，位置编码已存在");
        }
        if (!locationService.checkLocationNameUnique(location))
        {
            return error("新增位置失败，同级位置名称已存在");
        }
        location.setCreateBy(getUsername());
        return toAjax(locationService.insertLocation(location));
    }

    /**
     * 修改资产位置
     */
    @PreAuthorize("@ss.hasPermi('asset:location:edit')")
    @Log(title = "资产位置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody AssetLocation location)
    {
        if (!locationService.checkLocationCodeUnique(location))
        {
            return error("修改位置失败，位置编码已存在");
        }
        if (!locationService.checkLocationNameUnique(location))
        {
            return error("修改位置失败，同级位置名称已存在");
        }
        location.setUpdateBy(getUsername());
        return toAjax(locationService.updateLocation(location));
    }

    /**
     * 删除资产位置
     */
    @PreAuthorize("@ss.hasPermi('asset:location:remove')")
    @Log(title = "资产位置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{locationIds}")
    public AjaxResult remove(@PathVariable Long[] locationIds)
    {
        return toAjax(locationService.deleteLocationByIds(locationIds));
    }
}
