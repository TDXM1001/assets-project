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
import com.ruoyi.system.domain.asset.AssetCategory;
import com.ruoyi.system.service.asset.IAssetCategoryService;

/**
 * 资产分类管理
 */
@RestController
@RequestMapping("/asset/category")
public class AssetCategoryController extends BaseController
{
    @Autowired
    private IAssetCategoryService categoryService;

    /**
     * 查询资产分类列表
     */
    @PreAuthorize("@ss.hasPermi('asset:category:list')")
    @GetMapping("/list")
    public AjaxResult list(AssetCategory category)
    {
        return success(categoryService.selectCategoryList(category));
    }

    /**
     * 查询资产分类树
     */
    @PreAuthorize("@ss.hasPermi('asset:category:list')")
    @GetMapping("/treeSelect")
    public AjaxResult treeSelect(AssetCategory category)
    {
        return success(categoryService.selectCategoryTreeList(category));
    }

    /**
     * 查询资产分类详情
     */
    @PreAuthorize("@ss.hasPermi('asset:category:query')")
    @GetMapping("/{categoryId}")
    public AjaxResult getInfo(@PathVariable Long categoryId)
    {
        return success(categoryService.selectCategoryById(categoryId));
    }

    /**
     * 新增资产分类
     */
    @PreAuthorize("@ss.hasPermi('asset:category:add')")
    @Log(title = "资产分类", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody AssetCategory category)
    {
        if (!categoryService.checkCategoryCodeUnique(category))
        {
            return error("新增分类失败，分类编码已存在");
        }
        if (!categoryService.checkCategoryNameUnique(category))
        {
            return error("新增分类失败，同级分类名称已存在");
        }
        category.setCreateBy(getUsername());
        return toAjax(categoryService.insertCategory(category));
    }

    /**
     * 修改资产分类
     */
    @PreAuthorize("@ss.hasPermi('asset:category:edit')")
    @Log(title = "资产分类", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody AssetCategory category)
    {
        if (!categoryService.checkCategoryCodeUnique(category))
        {
            return error("修改分类失败，分类编码已存在");
        }
        if (!categoryService.checkCategoryNameUnique(category))
        {
            return error("修改分类失败，同级分类名称已存在");
        }
        category.setUpdateBy(getUsername());
        return toAjax(categoryService.updateCategory(category));
    }

    /**
     * 删除资产分类
     */
    @PreAuthorize("@ss.hasPermi('asset:category:remove')")
    @Log(title = "资产分类", businessType = BusinessType.DELETE)
    @DeleteMapping("/{categoryIds}")
    public AjaxResult remove(@PathVariable Long[] categoryIds)
    {
        return toAjax(categoryService.deleteCategoryByIds(categoryIds));
    }
}
