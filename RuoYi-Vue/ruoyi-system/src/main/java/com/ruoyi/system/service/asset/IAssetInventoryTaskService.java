package com.ruoyi.system.service.asset;

import java.util.List;
import com.ruoyi.system.domain.asset.AssetInventoryTask;
import com.ruoyi.system.domain.asset.AssetInventoryTaskItem;

/**
 * 资产盘点任务服务接口
 */
public interface IAssetInventoryTaskService
{
    List<AssetInventoryTask> selectAssetInventoryTaskList(AssetInventoryTask task);

    AssetInventoryTask selectAssetInventoryTaskById(Long taskId);

    List<AssetInventoryTaskItem> selectAssetInventoryTaskItemsByTaskId(Long taskId);

    boolean checkTaskNoUnique(AssetInventoryTask task);

    int insertAssetInventoryTask(AssetInventoryTask task);

    int updateAssetInventoryTask(AssetInventoryTask task);

    int deleteAssetInventoryTaskByIds(Long[] taskIds);

    int startAssetInventoryTask(Long taskId, Long operateUserId, String operateBy);

    int scanAssetInventoryTask(Long taskId, String scanCode, Long operateUserId, String operateBy);

    int finishAssetInventoryTask(Long taskId, Long operateUserId, String operateBy);

    int processAssetInventoryDiff(Long taskId, List<Long> itemIds, String processStatus, String processDesc, String operateBy);
}
