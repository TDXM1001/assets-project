package com.ruoyi.system.service.asset;

import java.util.List;
import com.ruoyi.system.domain.asset.AssetEventLog;
import com.ruoyi.system.domain.asset.AssetInfo;

/**
 * 资产事件流水服务接口
 */
public interface IAssetEventLogService
{
    /**
     * 查询资产事件流水列表
     */
    List<AssetEventLog> selectAssetEventLogList(AssetEventLog assetEventLog);

    /**
     * 查询资产事件流水详情
     */
    AssetEventLog selectAssetEventLogById(Long eventId);

    /**
     * 查询某项资产最近的流水记录
     */
    List<AssetEventLog> selectRecentAssetEventLogListByAssetId(Long assetId, Integer limit);

    /**
     * 记录资产事件流水
     *
     * @param assetId 资产ID
     * @param eventType 事件类型
     * @param sourceOrderId 来源业务ID
     * @param sourceOrderType 来源业务类型
     * @param beforeAsset 变更前快照
     * @param afterAsset 变更后快照
     * @param eventDesc 事件说明
     * @param operatorUserId 操作人ID
     */
    void recordAssetEvent(Long assetId, String eventType, Long sourceOrderId, String sourceOrderType,
        AssetInfo beforeAsset, AssetInfo afterAsset, String eventDesc, Long operatorUserId);
}
