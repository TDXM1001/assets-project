package com.ruoyi.system.service.asset.impl;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson2.JSON;
import com.ruoyi.system.domain.asset.AssetEventLog;
import com.ruoyi.system.domain.asset.AssetInfo;
import com.ruoyi.system.mapper.asset.AssetEventLogMapper;
import com.ruoyi.system.service.asset.IAssetEventLogService;

/**
 * 资产事件流水服务实现
 */
@Service
public class AssetEventLogServiceImpl implements IAssetEventLogService
{
    @Autowired
    private AssetEventLogMapper assetEventLogMapper;

    @Override
    public void recordAssetEvent(Long assetId, String eventType, Long sourceOrderId, String sourceOrderType,
        AssetInfo beforeAsset, AssetInfo afterAsset, String eventDesc, Long operatorUserId)
    {
        AssetEventLog assetEventLog = new AssetEventLog();
        assetEventLog.setAssetId(assetId);
        assetEventLog.setEventType(eventType);
        assetEventLog.setSourceOrderId(sourceOrderId);
        assetEventLog.setSourceOrderType(sourceOrderType);
        assetEventLog.setBeforeSnapshot(buildSnapshotJson(beforeAsset));
        assetEventLog.setAfterSnapshot(buildSnapshotJson(afterAsset));
        assetEventLog.setEventDesc(eventDesc);
        assetEventLog.setOperatorUserId(operatorUserId);
        assetEventLog.setOperateTime(new Date());
        assetEventLogMapper.insertAssetEventLog(assetEventLog);
    }

    /**
     * 事件流水保留的是可追溯快照，而不是整行对象，避免后续字段扩充时日志体积失控。
     */
    private String buildSnapshotJson(AssetInfo assetInfo)
    {
        if (assetInfo == null)
        {
            return null;
        }

        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("assetId", assetInfo.getAssetId());
        snapshot.put("assetCode", assetInfo.getAssetCode());
        snapshot.put("assetName", assetInfo.getAssetName());
        snapshot.put("assetStatus", assetInfo.getAssetStatus());
        snapshot.put("useOrgDeptId", assetInfo.getUseOrgDeptId());
        snapshot.put("manageDeptId", assetInfo.getManageDeptId());
        snapshot.put("currentUserId", assetInfo.getCurrentUserId());
        snapshot.put("currentLocationId", assetInfo.getCurrentLocationId());
        snapshot.put("inboundDate", assetInfo.getInboundDate());
        snapshot.put("startUseDate", assetInfo.getStartUseDate());
        snapshot.put("versionNo", assetInfo.getVersionNo());
        return JSON.toJSONString(snapshot);
    }
}
