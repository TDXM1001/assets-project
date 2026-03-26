package com.ruoyi.system.service.asset.impl;

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
     * 导入只走核心字段，模板化字段后续由页面化新增入口承接。
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
     * 统一整理扩展字段，避免模板调整后直接把历史 JSON 覆盖掉。
     */
    private void normalizeDynamicFields(AssetInfo assetInfo)
    {
        Map<String, Object> mergedValues = new LinkedHashMap<>();
        if (assetInfo.getAssetId() != null)
        {
            AssetInfo dbAsset = assetInfoMapper.selectAssetInfoById(assetInfo.getAssetId());
            if (dbAsset != null)
            {
                mergedValues.putAll(parseExtraFieldValues(dbAsset.getExtraFieldsJson()));
            }
        }
        mergedValues.putAll(parseExtraFieldValues(assetInfo.getExtraFieldsJson()));
        if (assetInfo.getExtraFieldValues() != null)
        {
            mergedValues.putAll(assetInfo.getExtraFieldValues());
        }

        applyTemplateRules(assetInfo, mergedValues);
        assetInfo.setExtraFieldValues(mergedValues);
        assetInfo.setExtraFieldsJson(mergedValues.isEmpty() ? "{}" : JSON.toJSONString(mergedValues));
    }

    /**
     * 把模板的默认值和必填约束前置到服务层，避免前后端口径漂移。
     */
    private void applyTemplateRules(AssetInfo assetInfo, Map<String, Object> mergedValues)
    {
        if (assetInfo.getCategoryId() == null)
        {
            return;
        }

        AssetCategoryFieldTemplateVo fieldTemplate = assetCategoryService.selectCategoryFieldTemplate(assetInfo.getCategoryId());
        if (fieldTemplate == null)
        {
            return;
        }

        assetInfo.setTemplateVersion(fieldTemplate.getTemplateVersion());
        if (!UserConstants.NORMAL.equals(fieldTemplate.getStatus()) || fieldTemplate.getFields() == null)
        {
            return;
        }

        for (AssetCategoryFieldTemplateFieldVo field : fieldTemplate.getFields())
        {
            if (field == null)
            {
                continue;
            }

            Object rawValue = mergedValues.get(field.getFieldCode());
            if (rawValue == null && StringUtils.isNotBlank(field.getDefaultValue()))
            {
                mergedValues.put(field.getFieldCode(), field.getDefaultValue());
                rawValue = field.getDefaultValue();
            }

            if (UserConstants.EXCEPTION.equals(field.getRequiredFlag()) && isBlankDynamicValue(rawValue))
            {
                throw new ServiceException("扩展字段[" + field.getFieldName() + "]不能为空");
            }
        }
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
