package com.ruoyi.system.mapper.asset;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.asset.AssetInventoryTask;
import com.ruoyi.system.domain.asset.AssetInventoryTaskItem;

/**
 * 资产盘点任务 Mapper 接口
 */
public interface AssetInventoryTaskMapper
{
    List<AssetInventoryTask> selectAssetInventoryTaskList(AssetInventoryTask task);

    AssetInventoryTask selectAssetInventoryTaskById(Long taskId);

    AssetInventoryTask checkTaskNoUnique(String taskNo);

    int insertAssetInventoryTask(AssetInventoryTask task);

    int updateAssetInventoryTask(AssetInventoryTask task);

    int updateAssetInventoryTaskStatus(AssetInventoryTask task);

    int deleteAssetInventoryTaskByIds(Long[] taskIds);

    int deleteAssetInventoryTaskItemByTaskId(Long taskId);

    int deleteAssetInventoryTaskItemByTaskIds(Long[] taskIds);

    int batchInsertAssetInventoryTaskItem(@Param("list") List<AssetInventoryTaskItem> list);

    List<AssetInventoryTaskItem> selectAssetInventoryTaskItemListByTaskId(Long taskId);

    List<AssetInventoryTaskItem> selectSeedInventoryTaskItems(AssetInventoryTask task);

    AssetInventoryTaskItem selectInventoryTaskItemByScanCode(@Param("taskId") Long taskId, @Param("scanCode") String scanCode);

    int updateAssetInventoryTaskItem(AssetInventoryTaskItem item);

    int batchUpdateInventoryProcess(@Param("taskId") Long taskId,
        @Param("itemIds") List<Long> itemIds,
        @Param("processStatus") String processStatus,
        @Param("processDesc") String processDesc);
}
