package com.ruoyi.system.service.asset;

import java.util.List;
import com.ruoyi.system.domain.asset.AssetLocation;
import com.ruoyi.system.domain.asset.vo.AssetTreeSelect;

/**
 * 资产位置服务接口
 */
public interface IAssetLocationService
{
    List<AssetLocation> selectLocationList(AssetLocation location);

    List<AssetTreeSelect> selectLocationTreeList(AssetLocation location);

    AssetLocation selectLocationById(Long locationId);

    boolean checkLocationCodeUnique(AssetLocation location);

    boolean checkLocationNameUnique(AssetLocation location);

    int insertLocation(AssetLocation location);

    int updateLocation(AssetLocation location);

    int deleteLocationByIds(Long[] locationIds);
}
