package com.ruoyi.system.mapper.asset;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.asset.AssetLocation;

/**
 * 资产位置 Mapper 接口
 */
public interface AssetLocationMapper
{
    List<AssetLocation> selectLocationList(AssetLocation location);

    AssetLocation selectLocationById(Long locationId);

    List<AssetLocation> selectChildrenLocationById(Long locationId);

    int hasChildByLocationId(Long locationId);

    int checkLocationExistAsset(Long locationId);

    AssetLocation checkLocationCodeUnique(String locationCode);

    AssetLocation checkLocationNameUnique(@Param("locationName") String locationName, @Param("parentId") Long parentId);

    int insertLocation(AssetLocation location);

    int updateLocation(AssetLocation location);

    int updateLocationChildren(@Param("locations") List<AssetLocation> locations);

    int deleteLocationByIds(Long[] locationIds);
}
