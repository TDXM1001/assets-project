package com.ruoyi.system.service.asset;

import java.util.List;
import com.ruoyi.system.domain.asset.AssetCategory;
import com.ruoyi.system.domain.asset.vo.AssetCategoryFieldTemplateVo;
import com.ruoyi.system.domain.asset.vo.AssetTreeSelect;

/**
 * 资产分类服务接口
 */
public interface IAssetCategoryService
{
    List<AssetCategory> selectCategoryList(AssetCategory category);

    List<AssetTreeSelect> selectCategoryTreeList(AssetCategory category);

    AssetCategory selectCategoryById(Long categoryId);

    AssetCategoryFieldTemplateVo selectCategoryFieldTemplate(Long categoryId);

    boolean checkCategoryCodeUnique(AssetCategory category);

    boolean checkCategoryNameUnique(AssetCategory category);

    int insertCategory(AssetCategory category);

    int updateCategory(AssetCategory category);

    int updateCategoryFieldTemplate(Long categoryId, AssetCategoryFieldTemplateVo fieldTemplate, String operator);

    int deleteCategoryByIds(Long[] categoryIds);
}
