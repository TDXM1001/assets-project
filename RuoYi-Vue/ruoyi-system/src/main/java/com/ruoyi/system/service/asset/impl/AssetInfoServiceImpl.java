package com.ruoyi.system.service.asset.impl;

import java.util.List;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.annotation.DataScope;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.bean.BeanValidators;
import com.ruoyi.system.domain.asset.AssetInfo;
import com.ruoyi.system.mapper.asset.AssetInfoMapper;
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
        return list.isEmpty() ? null : list.get(0);
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
    public int insertAssetInfo(AssetInfo assetInfo)
    {
        return assetInfoMapper.insertAssetInfo(assetInfo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateAssetInfo(AssetInfo assetInfo)
    {
        return assetInfoMapper.updateAssetInfo(assetInfo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteAssetInfoByIds(Long[] assetIds)
    {
        return assetInfoMapper.deleteAssetInfoByIds(assetIds);
    }

    /**
     * 导入时补齐默认字段，避免模板中未填写时落库出错。
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
}
