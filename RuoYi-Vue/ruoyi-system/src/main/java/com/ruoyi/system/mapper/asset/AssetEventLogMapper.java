package com.ruoyi.system.mapper.asset;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.asset.AssetEventLog;

/**
 * 资产事件流水 Mapper 接口
 */
public interface AssetEventLogMapper
{
    List<AssetEventLog> selectAssetEventLogList(AssetEventLog assetEventLog);

    AssetEventLog selectAssetEventLogById(Long eventId);

    List<AssetEventLog> selectRecentAssetEventLogListByAssetId(@Param("assetId") Long assetId,
        @Param("limit") Integer limit);

    int insertAssetEventLog(AssetEventLog assetEventLog);
}
