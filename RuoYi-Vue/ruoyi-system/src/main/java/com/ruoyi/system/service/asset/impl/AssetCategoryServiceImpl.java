package com.ruoyi.system.service.asset.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.asset.AssetCategory;
import com.ruoyi.system.domain.asset.vo.AssetTreeSelect;
import com.ruoyi.system.mapper.asset.AssetCategoryMapper;
import com.ruoyi.system.service.asset.IAssetCategoryService;

/**
 * 资产分类服务实现
 */
@Service
public class AssetCategoryServiceImpl implements IAssetCategoryService
{
    @Autowired
    private AssetCategoryMapper categoryMapper;

    @Override
    public List<AssetCategory> selectCategoryList(AssetCategory category)
    {
        return categoryMapper.selectCategoryList(category);
    }

    @Override
    public List<AssetTreeSelect> selectCategoryTreeList(AssetCategory category)
    {
        List<AssetCategory> categories = selectCategoryList(category);
        return buildCategoryTree(categories).stream().map(AssetTreeSelect::new).collect(Collectors.toList());
    }

    @Override
    public AssetCategory selectCategoryById(Long categoryId)
    {
        return categoryMapper.selectCategoryById(categoryId);
    }

    @Override
    public boolean checkCategoryCodeUnique(AssetCategory category)
    {
        Long categoryId = StringUtils.isNull(category.getCategoryId()) ? -1L : category.getCategoryId();
        AssetCategory info = categoryMapper.checkCategoryCodeUnique(category.getCategoryCode());
        return StringUtils.isNull(info) || info.getCategoryId().longValue() == categoryId.longValue();
    }

    @Override
    public boolean checkCategoryNameUnique(AssetCategory category)
    {
        Long categoryId = StringUtils.isNull(category.getCategoryId()) ? -1L : category.getCategoryId();
        AssetCategory info = categoryMapper.checkCategoryNameUnique(category.getCategoryName(), category.getParentId());
        return StringUtils.isNull(info) || info.getCategoryId().longValue() == categoryId.longValue();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertCategory(AssetCategory category)
    {
        if (category.getParentId() == null || category.getParentId().longValue() == 0L)
        {
            category.setParentId(0L);
            category.setAncestors("0");
        }
        else
        {
            AssetCategory parent = categoryMapper.selectCategoryById(category.getParentId());
            if (StringUtils.isNull(parent))
            {
                throw new ServiceException("上级分类不存在");
            }
            if (!UserConstants.NORMAL.equals(parent.getStatus()))
            {
                throw new ServiceException("上级分类已停用，不允许新增子分类");
            }
            category.setAncestors(parent.getAncestors() + "," + parent.getCategoryId());
        }
        return categoryMapper.insertCategory(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateCategory(AssetCategory category)
    {
        AssetCategory oldCategory = categoryMapper.selectCategoryById(category.getCategoryId());
        if (StringUtils.isNull(oldCategory))
        {
            throw new ServiceException("分类不存在");
        }
        if (category.getCategoryId().equals(category.getParentId()))
        {
            throw new ServiceException("上级分类不能是自己");
        }

        String newAncestors = "0";
        if (category.getParentId() != null && category.getParentId().longValue() != 0L)
        {
            AssetCategory newParent = categoryMapper.selectCategoryById(category.getParentId());
            if (StringUtils.isNull(newParent))
            {
                throw new ServiceException("上级分类不存在");
            }
            newAncestors = newParent.getAncestors() + "," + newParent.getCategoryId();
        }
        category.setAncestors(newAncestors);
        updateCategoryChildren(category.getCategoryId(), newAncestors, oldCategory.getAncestors());
        return categoryMapper.updateCategory(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteCategoryByIds(Long[] categoryIds)
    {
        for (Long categoryId : categoryIds)
        {
            if (categoryMapper.hasChildByCategoryId(categoryId) > 0)
            {
                throw new ServiceException("存在下级分类，不允许删除");
            }
            if (categoryMapper.checkCategoryExistAsset(categoryId) > 0)
            {
                throw new ServiceException("分类下存在资产，不允许删除");
            }
        }
        return categoryMapper.deleteCategoryByIds(categoryIds);
    }

    private List<AssetCategory> buildCategoryTree(List<AssetCategory> categories)
    {
        List<AssetCategory> returnList = new ArrayList<>();
        List<Long> ids = categories.stream().map(AssetCategory::getCategoryId).collect(Collectors.toList());
        for (AssetCategory category : categories)
        {
            if (!ids.contains(category.getParentId()))
            {
                recursionFn(categories, category);
                returnList.add(category);
            }
        }
        return returnList.isEmpty() ? categories : returnList;
    }

    private void recursionFn(List<AssetCategory> list, AssetCategory parent)
    {
        List<AssetCategory> childList = getChildList(list, parent);
        parent.setChildren(childList);
        for (AssetCategory child : childList)
        {
            if (hasChild(list, child))
            {
                recursionFn(list, child);
            }
        }
    }

    private List<AssetCategory> getChildList(List<AssetCategory> list, AssetCategory parent)
    {
        List<AssetCategory> childList = new ArrayList<>();
        Iterator<AssetCategory> it = list.iterator();
        while (it.hasNext())
        {
            AssetCategory category = it.next();
            if (StringUtils.isNotNull(category.getParentId()) && category.getParentId().longValue() == parent.getCategoryId().longValue())
            {
                childList.add(category);
            }
        }
        return childList;
    }

    private boolean hasChild(List<AssetCategory> list, AssetCategory parent)
    {
        return !getChildList(list, parent).isEmpty();
    }

    private void updateCategoryChildren(Long categoryId, String newAncestors, String oldAncestors)
    {
        List<AssetCategory> children = categoryMapper.selectChildrenCategoryById(categoryId);
        for (AssetCategory child : children)
        {
            child.setAncestors(child.getAncestors().replaceFirst(oldAncestors, newAncestors));
        }
        if (!children.isEmpty())
        {
            categoryMapper.updateCategoryChildren(children);
        }
    }
}
