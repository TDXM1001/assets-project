package com.ruoyi.system.service.asset.impl;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.ruoyi.common.annotation.DataScope;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.bean.BeanValidators;
import com.ruoyi.system.domain.asset.AssetInfo;
import com.ruoyi.system.domain.asset.vo.AssetCategoryFieldTemplateFieldVo;
import com.ruoyi.system.domain.asset.vo.AssetCategoryFieldTemplateVo;
import com.ruoyi.system.mapper.asset.AssetInfoMapper;
import com.ruoyi.system.service.asset.IAssetCategoryService;
import com.ruoyi.system.service.asset.IAssetInfoService;

/**
 * 资产台账服务实现
 */
@Service
public class AssetInfoServiceImpl implements IAssetInfoService
{
    @Autowired
    private AssetInfoMapper assetInfoMapper;

    @Autowired
    private IAssetCategoryService assetCategoryService;

    @Autowired
    protected Validator validator;

    @Autowired
    @Lazy
    private IAssetInfoService assetInfoServiceProxy;

    @Override
    @DataScope(deptAlias = "scope_dept", userAlias = "scope_user")
    public List<AssetInfo> selectAssetInfoList(AssetInfo assetInfo)
    {
        return assetInfoMapper.selectAssetInfoList(assetInfo);
    }

    @Override
    public AssetInfo selectAssetInfoById(Long assetId)
    {
        AssetInfo query = new AssetInfo();
        query.setAssetId(assetId);
        List<AssetInfo> list = assetInfoServiceProxy.selectAssetInfoList(query);
        if (list.isEmpty())
        {
            return null;
        }
        AssetInfo assetInfo = list.get(0);
        hydrateExtraFieldValues(assetInfo);
        return assetInfo;
    }

    @Override
    public boolean checkAssetCodeUnique(AssetInfo assetInfo)
    {
        Long assetId = StringUtils.isNull(assetInfo.getAssetId()) ? -1L : assetInfo.getAssetId();
        AssetInfo info = assetInfoMapper.checkAssetCodeUnique(assetInfo.getAssetCode());
        return StringUtils.isNull(info) || info.getAssetId().longValue() == assetId.longValue();
    }

    @Override
    public String importAssetInfo(List<AssetInfo> assetList, Boolean updateSupport, String operName)
    {
        if (StringUtils.isNull(assetList) || assetList.isEmpty())
        {
            throw new ServiceException("导入资产数据不能为空");
        }

        int successNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();

        for (AssetInfo asset : assetList)
        {
            String assetCode = StringUtils.defaultIfBlank(asset.getAssetCode(), "未填写编码");
            try
            {
                BeanValidators.validateWithException(validator, asset);
                normalizeImportAsset(asset);

                AssetInfo dbAsset = assetInfoMapper.checkAssetCodeUnique(asset.getAssetCode());
                if (StringUtils.isNull(dbAsset))
                {
                    // 导入入口也走动态字段规则，避免和页面保存出现双轨口径。
                    normalizeDynamicFields(asset);
                    asset.setCreateBy(operName);
                    asset.setUpdateBy(operName);
                    if (assetInfoMapper.insertAssetInfo(asset) <= 0)
                    {
                        throw new ServiceException("插入资产主档失败");
                    }
                    successNum++;
                    successMsg.append("\n").append(successNum).append("、资产编码 ").append(assetCode).append(" 导入成功");
                }
                else if (Boolean.TRUE.equals(updateSupport))
                {
                    asset.setAssetId(dbAsset.getAssetId());
                    // 更新导入同样受模板必填、停用和只读规则约束。
                    normalizeDynamicFields(asset);
                    asset.setUpdateBy(operName);
                    if (assetInfoMapper.updateAssetInfo(asset) <= 0)
                    {
                        throw new ServiceException("更新资产主档失败");
                    }
                    successNum++;
                    successMsg.append("\n").append(successNum).append("、资产编码 ").append(assetCode).append(" 更新成功");
                }
                else
                {
                    failureNum++;
                    failureMsg.append("\n").append(failureNum).append("、资产编码 ").append(assetCode).append(" 已存在");
                }
            }
            catch (Exception e)
            {
                failureNum++;
                String msg = "\n" + failureNum + "、资产编码 " + assetCode + " 导入失败：";
                failureMsg.append(msg).append(e.getMessage());
            }
        }

        if (failureNum > 0)
        {
            failureMsg.insert(0, "很抱歉，导入失败！共 " + failureNum + " 条数据不符合要求，错误如下：");
            if (successNum > 0)
            {
                successMsg.insert(0, "恭喜您，部分数据已导入成功！共 " + successNum + " 条，成功如下：");
                return successMsg.append("\n").append(failureMsg).toString();
            }
            throw new ServiceException(failureMsg.toString());
        }

        successMsg.insert(0, "恭喜您，数据已全部导入成功！共 " + successNum + " 条，数据如下：");
        return successMsg.toString();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertAssetInfo(AssetInfo assetInfo)
    {
        normalizeDynamicFields(assetInfo);
        return assetInfoMapper.insertAssetInfo(assetInfo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateAssetInfo(AssetInfo assetInfo)
    {
        normalizeDynamicFields(assetInfo);
        return assetInfoMapper.updateAssetInfo(assetInfo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteAssetInfoByIds(Long[] assetIds)
    {
        return assetInfoMapper.deleteAssetInfoByIds(assetIds);
    }

    /**
     * 导入先补齐主档默认值，扩展字段规则统一交给 normalizeDynamicFields 处理。
     */
    private void normalizeImportAsset(AssetInfo asset)
    {
        if (StringUtils.isBlank(asset.getAssetSource()))
        {
            asset.setAssetSource("PURCHASE");
        }
        if (StringUtils.isBlank(asset.getStatus()))
        {
            asset.setStatus("0");
        }
        if (asset.getVersionNo() == null)
        {
            asset.setVersionNo(1);
        }
    }

    /**
     * 统一整理扩展字段，保证页面新增、页面编辑和导入入口共用同一套模板规则。
     */
    private void normalizeDynamicFields(AssetInfo assetInfo)
    {
        AssetInfo dbAsset = null;
        Map<String, Object> existingValues = new LinkedHashMap<>();
        if (assetInfo.getAssetId() != null)
        {
            dbAsset = assetInfoMapper.selectAssetInfoById(assetInfo.getAssetId());
            if (dbAsset != null)
            {
                existingValues.putAll(parseExtraFieldValues(dbAsset.getExtraFieldsJson()));
            }
        }

        Map<String, Object> incomingValues = collectIncomingDynamicValues(assetInfo);
        Map<String, Object> normalizedValues = applyTemplateRules(assetInfo, existingValues, incomingValues, dbAsset != null);
        assetInfo.setExtraFieldValues(normalizedValues);
        assetInfo.setExtraFieldsJson(normalizedValues.isEmpty() ? "{}" : JSON.toJSONString(normalizedValues));
    }

    /**
     * 先整理前端传入的动态字段快照，后续再由模板规则决定哪些值可以落库。
     */
    private Map<String, Object> collectIncomingDynamicValues(AssetInfo assetInfo)
    {
        Map<String, Object> incomingValues = new LinkedHashMap<>();
        incomingValues.putAll(parseExtraFieldValues(assetInfo.getExtraFieldsJson()));
        if (assetInfo.getExtraFieldValues() != null)
        {
            incomingValues.putAll(assetInfo.getExtraFieldValues());
        }
        return incomingValues;
    }

    /**
     * 在服务层统一收口模板启用、字段停用、只读和默认值规则，避免不同入口出现口径漂移。
     */
    private Map<String, Object> applyTemplateRules(AssetInfo assetInfo, Map<String, Object> existingValues,
        Map<String, Object> incomingValues, boolean preserveHistory)
    {
        Map<String, Object> normalizedValues = preserveHistory
            ? new LinkedHashMap<>(existingValues)
            : new LinkedHashMap<>();
        if (assetInfo.getCategoryId() == null)
        {
            return normalizedValues;
        }

        AssetCategoryFieldTemplateVo fieldTemplate = assetCategoryService
            .selectCategoryFieldTemplate(assetInfo.getCategoryId(), assetInfo.getTemplateVersion());
        if (fieldTemplate == null)
        {
            return normalizedValues;
        }

        assetInfo.setTemplateVersion(fieldTemplate.getTemplateVersion());
        if (!UserConstants.NORMAL.equals(fieldTemplate.getStatus()) || fieldTemplate.getFields() == null)
        {
            return normalizedValues;
        }

        for (AssetCategoryFieldTemplateFieldVo field : fieldTemplate.getFields())
        {
            if (field == null || !UserConstants.NORMAL.equals(field.getStatus()) || StringUtils.isBlank(field.getFieldCode()))
            {
                // 停用字段不再接收新值，但保留历史值，避免模板调整直接破坏旧数据。
                continue;
            }

            String fieldCode = field.getFieldCode();
            boolean hasExistingValue = existingValues.containsKey(fieldCode);
            boolean hasIncomingValue = incomingValues.containsKey(fieldCode);
            Object rawValue = resolveFieldValue(field, existingValues.get(fieldCode), incomingValues.get(fieldCode),
                hasExistingValue, hasIncomingValue);

            if (rawValue == null && !hasExistingValue && StringUtils.isNotBlank(field.getDefaultValue()))
            {
                rawValue = normalizeTemplateValue(field, field.getDefaultValue());
            }

            if (UserConstants.EXCEPTION.equals(field.getRequiredFlag()) && isBlankDynamicValue(rawValue))
            {
                throw new ServiceException("扩展字段[" + field.getFieldName() + "]不能为空");
            }

            if (isBlankDynamicValue(rawValue))
            {
                if (!preserveHistory || !hasExistingValue)
                {
                    normalizedValues.remove(fieldCode);
                }
                continue;
            }

            normalizedValues.put(fieldCode, normalizeTemplateValue(field, rawValue));
        }
        return normalizedValues;
    }

    /**
     * 只读字段在更新时始终保留历史值，不能被新的提交结果覆盖。
     */
    private Object resolveFieldValue(AssetCategoryFieldTemplateFieldVo field, Object existingValue, Object incomingValue,
        boolean hasExistingValue, boolean hasIncomingValue)
    {
        if (UserConstants.EXCEPTION.equals(field.getReadonlyFlag()) && hasExistingValue)
        {
            return existingValue;
        }
        if (hasIncomingValue)
        {
            return incomingValue;
        }
        if (hasExistingValue)
        {
            return existingValue;
        }
        return null;
    }

    /**
     * 按模板声明做最小类型收口，避免数值字段被错误保存成字符串。
     */
    private Object normalizeTemplateValue(AssetCategoryFieldTemplateFieldVo field, Object rawValue)
    {
        if (rawValue == null)
        {
            return null;
        }
        if ("number".equals(field.getDataType()) || "number".equals(field.getComponentType()))
        {
            String stringValue = String.valueOf(rawValue).trim();
            if (StringUtils.isBlank(stringValue))
            {
                return null;
            }
            try
            {
                return new BigDecimal(stringValue);
            }
            catch (NumberFormatException ex)
            {
                throw new ServiceException("扩展字段[" + field.getFieldName() + "]必须是数字格式");
            }
        }
        return rawValue;
    }

    private void hydrateExtraFieldValues(AssetInfo assetInfo)
    {
        if (assetInfo == null)
        {
            return;
        }
        assetInfo.setExtraFieldValues(parseExtraFieldValues(assetInfo.getExtraFieldsJson()));
    }

    private Map<String, Object> parseExtraFieldValues(String extraFieldsJson)
    {
        if (StringUtils.isBlank(extraFieldsJson))
        {
            return new LinkedHashMap<>();
        }
        try
        {
            Map<String, Object> fieldValues = JSON.parseObject(extraFieldsJson,
                new TypeReference<LinkedHashMap<String, Object>>() { });
            return fieldValues == null ? new LinkedHashMap<>() : fieldValues;
        }
        catch (Exception ex)
        {
            throw new ServiceException("扩展字段数据格式不正确");
        }
    }

    private boolean isBlankDynamicValue(Object rawValue)
    {
        if (rawValue == null)
        {
            return true;
        }
        if (rawValue instanceof String)
        {
            return StringUtils.isBlank((String) rawValue);
        }
        return false;
    }
}
