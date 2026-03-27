package com.ruoyi.system.service.asset.impl;

import java.util.Comparator;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.asset.AssetCategory;
import com.ruoyi.system.domain.asset.vo.AssetCategoryFieldTemplateFieldVo;
import com.ruoyi.system.domain.asset.vo.AssetCategoryFieldTemplateVo;
import com.ruoyi.system.domain.asset.vo.AssetTreeSelect;
import com.ruoyi.system.mapper.asset.AssetCategoryMapper;
import com.ruoyi.system.service.asset.IAssetCategoryService;

/**
 * 资产分类服务实现
 */
@Service
public class AssetCategoryServiceImpl implements IAssetCategoryService
{
    private static final String FLAG_YES = "1";

    private static final String FLAG_NO = "0";

    private static final Set<String> SUPPORTED_DATA_TYPES = Set.of("string", "number", "date", "boolean");

    private static final Set<String> SUPPORTED_COMPONENT_TYPES = Set.of(
        "input", "textarea", "number", "select", "radio", "date");

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
    public AssetCategoryFieldTemplateVo selectCategoryFieldTemplate(Long categoryId, Integer templateVersion)
    {
        return selectCategoryFieldTemplateByVersion(categoryId, templateVersion);
    }

    @Override
    public AssetCategoryFieldTemplateVo selectCategoryFieldTemplate(Long categoryId)
    {
        AssetCategory category = categoryMapper.selectCategoryById(categoryId);
        if (StringUtils.isNull(category))
        {
            throw new ServiceException("分类不存在");
        }
        return buildFieldTemplate(category);
    }

    @Override
    public AssetCategoryFieldTemplateVo selectCategoryFieldTemplateByVersion(Long categoryId, Integer templateVersion)
    {
        if (templateVersion == null || templateVersion.intValue() <= 0)
        {
            return selectCategoryFieldTemplate(categoryId);
        }

        AssetCategory category = categoryMapper.selectCategoryById(categoryId);
        if (StringUtils.isNull(category))
        {
            throw new ServiceException("分类不存在");
        }

        // 当前分类上只保留最新模板，历史版本统一从快照表读取。
        if (templateVersion.equals(category.getFieldTemplateVersion()))
        {
            return buildFieldTemplate(category);
        }

        AssetCategory snapshot = categoryMapper.selectCategoryFieldTemplateSnapshot(categoryId, templateVersion);
        if (StringUtils.isNull(snapshot))
        {
            throw new ServiceException("未找到对应的分类模板版本");
        }
        return buildFieldTemplate(snapshot);
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
        if (isParentInCategorySubtree(category.getCategoryId(), category.getParentId()))
        {
            throw new ServiceException("上级分类不能选择当前分类的下级节点");
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
    public int updateCategoryFieldTemplate(Long categoryId, AssetCategoryFieldTemplateVo fieldTemplate, String operator)
    {
        AssetCategory category = categoryMapper.selectCategoryById(categoryId);
        if (StringUtils.isNull(category))
        {
            throw new ServiceException("分类不存在");
        }

        AssetCategoryFieldTemplateVo currentSnapshot = snapshotCurrentFieldTemplate(category);
        AssetCategoryFieldTemplateVo normalizedTemplate = normalizeFieldTemplate(categoryId, fieldTemplate);
        normalizedTemplate.setTemplateVersion(resolveNextTemplateVersion(category, normalizedTemplate));

        AssetCategory updateTarget = new AssetCategory();
        updateTarget.setCategoryId(categoryId);
        updateTarget.setFieldTemplateStatus(normalizedTemplate.getStatus());
        updateTarget.setFieldTemplateVersion(normalizedTemplate.getTemplateVersion());
        updateTarget.setFieldTemplateJson(JSON.toJSONString(normalizedTemplate));
        updateTarget.setUpdateBy(operator);
        updateTarget.setCreateBy(StringUtils.defaultIfBlank(operator, category.getUpdateBy()));

        int rows = categoryMapper.updateCategoryFieldTemplate(updateTarget);
        if (rows > 0 && currentSnapshot != null)
        {
            // 保存最新模板的同时固化一份快照，保证后续能按分类+版本反查。
            AssetCategory snapshotTarget = new AssetCategory();
            snapshotTarget.setCategoryId(categoryId);
            snapshotTarget.setFieldTemplateVersion(currentSnapshot == null ? null : currentSnapshot.getTemplateVersion());
            snapshotTarget.setFieldTemplateStatus(currentSnapshot == null ? null : currentSnapshot.getStatus());
            snapshotTarget.setFieldTemplateJson(currentSnapshot == null ? null : JSON.toJSONString(currentSnapshot));
            snapshotTarget.setCreateBy(StringUtils.defaultIfBlank(operator, category.getUpdateBy()));
            snapshotTarget.setUpdateBy(operator);
            categoryMapper.insertCategoryFieldTemplateSnapshot(snapshotTarget);
        }
        return rows;
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

    private AssetCategoryFieldTemplateVo snapshotCurrentFieldTemplate(AssetCategory category)
    {
        if (StringUtils.isBlank(category.getFieldTemplateJson()) || category.getFieldTemplateVersion() == null)
        {
            return null;
        }

        AssetCategory snapshot = new AssetCategory();
        snapshot.setCategoryId(category.getCategoryId());
        snapshot.setFieldTemplateVersion(category.getFieldTemplateVersion());
        snapshot.setFieldTemplateStatus(category.getFieldTemplateStatus());
        snapshot.setFieldTemplateJson(category.getFieldTemplateJson());
        return buildFieldTemplate(snapshot);
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

    private boolean isParentInCategorySubtree(Long categoryId, Long parentId)
    {
        if (parentId == null || parentId.longValue() == 0L)
        {
            return false;
        }
        List<AssetCategory> descendants = categoryMapper.selectChildrenCategoryById(categoryId);
        for (AssetCategory descendant : descendants)
        {
            if (descendant.getCategoryId() != null && descendant.getCategoryId().longValue() == parentId.longValue())
            {
                return true;
            }
        }
        return false;
    }

    /**
     * 统一把模板整理成稳定结构，避免同一模板在前后端来回传递时字段顺序漂移。
     */
    private AssetCategoryFieldTemplateVo normalizeFieldTemplate(Long categoryId, AssetCategoryFieldTemplateVo fieldTemplate)
    {
        AssetCategoryFieldTemplateVo target = fieldTemplate == null ? new AssetCategoryFieldTemplateVo() : fieldTemplate;
        target.setCategoryId(categoryId);
        target.setStatus(StringUtils.defaultIfBlank(target.getStatus(), UserConstants.NORMAL));

        List<AssetCategoryFieldTemplateFieldVo> sourceFields = target.getFields() == null
            ? new ArrayList<>()
            : target.getFields();
        List<AssetCategoryFieldTemplateFieldVo> normalizedFields = new ArrayList<>();
        Set<String> fieldCodes = new HashSet<>();

        for (int index = 0; index < sourceFields.size(); index++)
        {
            AssetCategoryFieldTemplateFieldVo item = sourceFields.get(index);
            if (item == null)
            {
                continue;
            }

            String fieldCode = StringUtils.trim(item.getFieldCode());
            String fieldName = StringUtils.trim(item.getFieldName());
            if (StringUtils.isBlank(fieldCode))
            {
                throw new ServiceException("字段编码不能为空");
            }
            if (StringUtils.isBlank(fieldName))
            {
                throw new ServiceException("字段名称不能为空");
            }
            if (!fieldCodes.add(fieldCode))
            {
                throw new ServiceException("字段编码[" + fieldCode + "]重复");
            }

            String dataType = StringUtils.defaultIfBlank(item.getDataType(), "string");
            if (!SUPPORTED_DATA_TYPES.contains(dataType))
            {
                throw new ServiceException("字段[" + fieldName + "]的数据类型不支持");
            }

            String componentType = StringUtils.defaultIfBlank(item.getComponentType(), "input");
            if (!SUPPORTED_COMPONENT_TYPES.contains(componentType))
            {
                throw new ServiceException("字段[" + fieldName + "]的组件类型不支持");
            }

            item.setFieldCode(fieldCode);
            item.setFieldName(fieldName);
            item.setDataType(dataType);
            item.setComponentType(componentType);
            item.setRequiredFlag(StringUtils.defaultIfBlank(item.getRequiredFlag(), FLAG_NO));
            item.setReadonlyFlag(StringUtils.defaultIfBlank(item.getReadonlyFlag(), FLAG_NO));
            item.setDictType(StringUtils.trimToNull(item.getDictType()));
            item.setDefaultValue(StringUtils.trimToEmpty(item.getDefaultValue()));
            item.setGroupName(StringUtils.defaultIfBlank(StringUtils.trimToNull(item.getGroupName()), "默认分组"));
            item.setOrderNum(item.getOrderNum() == null ? index + 1 : item.getOrderNum());
            item.setStatus(StringUtils.defaultIfBlank(item.getStatus(), UserConstants.NORMAL));
            normalizedFields.add(item);
        }

        normalizedFields.sort(Comparator.comparing(AssetCategoryFieldTemplateFieldVo::getOrderNum));
        target.setFields(normalizedFields);
        return target;
    }

    private AssetCategoryFieldTemplateVo buildFieldTemplate(AssetCategory category)
    {
        AssetCategoryFieldTemplateVo template = new AssetCategoryFieldTemplateVo();
        if (StringUtils.isNotBlank(category.getFieldTemplateJson()))
        {
            template = JSON.parseObject(category.getFieldTemplateJson(), AssetCategoryFieldTemplateVo.class);
        }
        if (template == null)
        {
            template = new AssetCategoryFieldTemplateVo();
        }
        template.setCategoryId(category.getCategoryId());
        template.setTemplateVersion(category.getFieldTemplateVersion() == null ? 1 : category.getFieldTemplateVersion());
        template.setStatus(StringUtils.defaultIfBlank(category.getFieldTemplateStatus(), UserConstants.EXCEPTION));
        template.setFields(template.getFields() == null ? new ArrayList<>() : template.getFields());
        template.getFields().sort(Comparator.comparing(AssetCategoryFieldTemplateFieldVo::getOrderNum,
            Comparator.nullsLast(Integer::compareTo)));
        return template;
    }

    private int resolveNextTemplateVersion(AssetCategory category, AssetCategoryFieldTemplateVo fieldTemplate)
    {
        int currentVersion = category.getFieldTemplateVersion() == null ? 0 : category.getFieldTemplateVersion();
        if (currentVersion <= 0)
        {
            return 1;
        }
        return currentVersion + 1;
    }
}
