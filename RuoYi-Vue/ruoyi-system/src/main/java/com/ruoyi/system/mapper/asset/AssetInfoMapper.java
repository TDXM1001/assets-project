package com.ruoyi.system.mapper.asset;

import java.util.Collection;
import java.util.List;
import com.ruoyi.system.domain.asset.AssetInfo;

/**
 * 资产台账 Mapper 接口
 */
public interface AssetInfoMapper
{
    List<AssetInfo> selectAssetInfoList(AssetInfo assetInfo);

    AssetInfo selectAssetInfoById(Long assetId);

    AssetInfo checkAssetCodeUnique(String assetCode);

    List<AssetInfo> selectAssetInfoByIds(Collection<Long> assetIds);

    int updateAssetSnapshot(AssetInfo assetInfo);

    int insertAssetInfo(AssetInfo assetInfo);

    int updateAssetInfo(AssetInfo assetInfo);

    int deleteAssetInfoByIds(Long[] assetIds);
}
