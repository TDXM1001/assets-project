package com.ruoyi.system.service.asset;

import java.util.List;
import com.ruoyi.system.domain.asset.AssetInfo;

/**
 * 资产台账服务接口
 */
public interface IAssetInfoService
{
    List<AssetInfo> selectAssetInfoList(AssetInfo assetInfo);

    AssetInfo selectAssetInfoById(Long assetId);

    boolean checkAssetCodeUnique(AssetInfo assetInfo);

    int insertAssetInfo(AssetInfo assetInfo);

    int updateAssetInfo(AssetInfo assetInfo);

    int deleteAssetInfoByIds(Long[] assetIds);
}
