package com.ruoyi.system.service.asset.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.annotation.DataScope;
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
