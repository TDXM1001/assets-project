package com.ruoyi.system.service.asset.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.utils.StringUtils;
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

    @Override
    public List<AssetInfo> selectAssetInfoList(AssetInfo assetInfo)
    {
        return assetInfoMapper.selectAssetInfoList(assetInfo);
    }

    @Override
    public AssetInfo selectAssetInfoById(Long assetId)
    {
        return assetInfoMapper.selectAssetInfoById(assetId);
    }

    @Override
    public boolean checkAssetCodeUnique(AssetInfo assetInfo)
    {
        Long assetId = StringUtils.isNull(assetInfo.getAssetId()) ? -1L : assetInfo.getAssetId();
        AssetInfo info = assetInfoMapper.checkAssetCodeUnique(assetInfo.getAssetCode());
        return StringUtils.isNull(info) || info.getAssetId().longValue() == assetId.longValue();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
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
}
