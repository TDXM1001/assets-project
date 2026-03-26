package com.ruoyi.system.mapper.asset;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.asset.AssetCategory;

/**
 * 资产分类 Mapper 接口
 */
public interface AssetCategoryMapper
{
    List<AssetCategory> selectCategoryList(AssetCategory category);

    AssetCategory selectCategoryById(Long categoryId);

    List<AssetCategory> selectChildrenCategoryById(Long categoryId);

    int hasChildByCategoryId(Long categoryId);

    int checkCategoryExistAsset(Long categoryId);

    AssetCategory checkCategoryCodeUnique(String categoryCode);

    AssetCategory checkCategoryNameUnique(@Param("categoryName") String categoryName, @Param("parentId") Long parentId);

    int insertCategory(AssetCategory category);

    int updateCategory(AssetCategory category);

    int updateCategoryFieldTemplate(AssetCategory category);

    int updateCategoryChildren(@Param("categories") List<AssetCategory> categories);

    int deleteCategoryByIds(Long[] categoryIds);
}
