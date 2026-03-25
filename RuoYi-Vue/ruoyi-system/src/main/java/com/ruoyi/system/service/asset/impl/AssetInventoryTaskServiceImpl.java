package com.ruoyi.system.service.asset.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.asset.AssetInventoryTask;
import com.ruoyi.system.domain.asset.AssetInventoryTaskItem;
import com.ruoyi.system.mapper.asset.AssetInventoryTaskMapper;
import com.ruoyi.system.service.asset.IAssetInventoryTaskService;

/**
 * 资产盘点任务服务实现
 */
@Service
public class AssetInventoryTaskServiceImpl implements IAssetInventoryTaskService
{
    private static final String TASK_STATUS_DRAFT = "DRAFT";
    private static final String TASK_STATUS_RUNNING = "RUNNING";
    private static final String TASK_STATUS_FINISHED = "FINISHED";

    private static final String INVENTORY_RESULT_NORMAL = "NORMAL";
    private static final String INVENTORY_RESULT_LOSS = "LOSS";
    private static final String INVENTORY_RESULT_LOCATION_DIFF = "LOCATION_DIFF";
    private static final String INVENTORY_RESULT_USER_DIFF = "USER_DIFF";
    private static final String INVENTORY_RESULT_STATUS_DIFF = "STATUS_DIFF";

    private static final String PROCESS_STATUS_PENDING = "PENDING";

    @Autowired
    private AssetInventoryTaskMapper inventoryTaskMapper;

    @Override
    public List<AssetInventoryTask> selectAssetInventoryTaskList(AssetInventoryTask task)
    {
        return inventoryTaskMapper.selectAssetInventoryTaskList(task);
    }

    @Override
    public AssetInventoryTask selectAssetInventoryTaskById(Long taskId)
    {
        AssetInventoryTask task = inventoryTaskMapper.selectAssetInventoryTaskById(taskId);
        if (StringUtils.isNotNull(task))
        {
            task.setItemList(selectAssetInventoryTaskItemsByTaskId(taskId));
        }
        return task;
    }

    @Override
    public List<AssetInventoryTaskItem> selectAssetInventoryTaskItemsByTaskId(Long taskId)
    {
        return inventoryTaskMapper.selectAssetInventoryTaskItemListByTaskId(taskId);
    }

    @Override
    public boolean checkTaskNoUnique(AssetInventoryTask task)
    {
        Long taskId = StringUtils.isNull(task.getTaskId()) ? -1L : task.getTaskId();
        AssetInventoryTask info = inventoryTaskMapper.checkTaskNoUnique(task.getTaskNo());
        return StringUtils.isNull(info) || info.getTaskId().longValue() == taskId.longValue();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertAssetInventoryTask(AssetInventoryTask task)
    {
        normalizeTask(task);
        task.setTaskStatus(TASK_STATUS_DRAFT);
        task.setSummaryTotal(0);
        task.setSummaryOk(0);
        task.setSummaryDiff(0);
        int rows = inventoryTaskMapper.insertAssetInventoryTask(task);
        refreshTaskItems(task);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateAssetInventoryTask(AssetInventoryTask task)
    {
        AssetInventoryTask dbTask = requireTask(task.getTaskId());
        if (TASK_STATUS_FINISHED.equals(dbTask.getTaskStatus()))
        {
            throw new ServiceException("已完成的盘点任务不允许修改");
        }

        normalizeTask(task);
        task.setTaskStatus(StringUtils.isEmpty(dbTask.getTaskStatus()) ? TASK_STATUS_DRAFT : dbTask.getTaskStatus());
        int rows = inventoryTaskMapper.updateAssetInventoryTask(task);
        refreshTaskItems(requireTask(task.getTaskId()));
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteAssetInventoryTaskByIds(Long[] taskIds)
    {
        inventoryTaskMapper.deleteAssetInventoryTaskItemByTaskIds(taskIds);
        return inventoryTaskMapper.deleteAssetInventoryTaskByIds(taskIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int startAssetInventoryTask(Long taskId, Long operateUserId, String operateBy)
    {
        AssetInventoryTask task = requireTask(taskId);
        if (TASK_STATUS_FINISHED.equals(task.getTaskStatus()))
        {
            throw new ServiceException("已完成的盘点任务不能重新开始");
        }

        refreshTaskItems(task);
        task.setTaskStatus(TASK_STATUS_RUNNING);
        task.setUpdateBy(operateBy);
        inventoryTaskMapper.updateAssetInventoryTaskStatus(task);
        return 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int scanAssetInventoryTask(Long taskId, String scanCode, Long operateUserId, String operateBy)
    {
        AssetInventoryTask task = requireTask(taskId);
        if (!TASK_STATUS_RUNNING.equals(task.getTaskStatus()))
        {
            throw new ServiceException("请先开始盘点任务，再录入盘点结果");
        }
        if (StringUtils.isEmpty(scanCode))
        {
            throw new ServiceException("盘点扫描内容不能为空");
        }

        AssetInventoryTaskItem item = inventoryTaskMapper.selectInventoryTaskItemByScanCode(taskId, scanCode);
        if (StringUtils.isNull(item))
        {
            throw new ServiceException("未找到匹配的盘点明细，请确认资产编码或二维码是否正确");
        }

        item.setInventoryUserId(operateUserId);
        item.setInventoryTime(new Date());
        item.setInventoryResult(resolveInventoryResult(item));
        item.setInventoryDesc(resolveInventoryDesc(item));
        item.setProcessStatus(INVENTORY_RESULT_NORMAL.equals(item.getInventoryResult()) ? null : PROCESS_STATUS_PENDING);
        item.setProcessDesc(null);
        item.setUpdateBy(operateBy);
        inventoryTaskMapper.updateAssetInventoryTaskItem(item);
        recalculateTaskSummary(taskId, operateBy);
        return 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int finishAssetInventoryTask(Long taskId, Long operateUserId, String operateBy)
    {
        AssetInventoryTask task = requireTask(taskId);
        if (!TASK_STATUS_RUNNING.equals(task.getTaskStatus()))
        {
            throw new ServiceException("只有进行中的盘点任务才能结束");
        }

        List<AssetInventoryTaskItem> items = selectAssetInventoryTaskItemsByTaskId(taskId);
        Date now = new Date();
        for (AssetInventoryTaskItem item : items)
        {
            if (item.getInventoryTime() == null)
            {
                item.setInventoryTime(now);
                item.setInventoryUserId(operateUserId);
                item.setActualLocationId(null);
                item.setActualUserId(null);
                item.setActualStatus(null);
                item.setInventoryResult(INVENTORY_RESULT_LOSS);
                item.setInventoryDesc("盘点结束时未扫描到该资产");
                item.setProcessStatus(PROCESS_STATUS_PENDING);
                item.setProcessDesc(null);
                item.setUpdateBy(operateBy);
                inventoryTaskMapper.updateAssetInventoryTaskItem(item);
            }
        }

        recalculateTaskSummary(taskId, operateBy);
        task.setTaskStatus(TASK_STATUS_FINISHED);
        task.setUpdateBy(operateBy);
        inventoryTaskMapper.updateAssetInventoryTaskStatus(task);
        return 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int processAssetInventoryDiff(Long taskId, List<Long> itemIds, String processStatus, String processDesc, String operateBy)
    {
        requireTask(taskId);
        if (itemIds == null || itemIds.isEmpty())
        {
            throw new ServiceException("请选择需要处理的差异明细");
        }
        return inventoryTaskMapper.batchUpdateInventoryProcess(taskId, itemIds,
            StringUtils.isEmpty(processStatus) ? PROCESS_STATUS_PENDING : processStatus,
            processDesc);
    }

    /**
     * 新建和修改任务时都重建一次盘点明细，确保盘点范围和任务内容保持一致。
     */
    private void refreshTaskItems(AssetInventoryTask task)
    {
        List<AssetInventoryTaskItem> seedItems = inventoryTaskMapper.selectSeedInventoryTaskItems(task);
        for (AssetInventoryTaskItem item : seedItems)
        {
            item.setTaskId(task.getTaskId());
            item.setInventoryResult(INVENTORY_RESULT_NORMAL);
            item.setInventoryDesc(null);
            item.setInventoryTime(null);
            item.setInventoryUserId(null);
            item.setProcessStatus(null);
            item.setProcessDesc(null);
        }

        inventoryTaskMapper.deleteAssetInventoryTaskItemByTaskId(task.getTaskId());
        if (!seedItems.isEmpty())
        {
            inventoryTaskMapper.batchInsertAssetInventoryTaskItem(seedItems);
        }

        AssetInventoryTask summaryTask = new AssetInventoryTask();
        summaryTask.setTaskId(task.getTaskId());
        summaryTask.setTaskScopeType(task.getTaskScopeType());
        summaryTask.setTargetDeptId(task.getTargetDeptId());
        summaryTask.setTargetLocationId(task.getTargetLocationId());
        summaryTask.setTargetCategoryId(task.getTargetCategoryId());
        summaryTask.setSummaryTotal(seedItems.size());
        summaryTask.setSummaryOk(0);
        summaryTask.setSummaryDiff(0);
        summaryTask.setUpdateBy(task.getUpdateBy());
        inventoryTaskMapper.updateAssetInventoryTask(summaryTask);
    }

    /**
     * 盘点结果以账面值和现场值逐项对比，先保证状态与汇总数字可用，后续再细化差异原因。
     */
    private String resolveInventoryResult(AssetInventoryTaskItem item)
    {
        if (!Objects.equals(item.getExpectedLocationId(), item.getActualLocationId()))
        {
            return INVENTORY_RESULT_LOCATION_DIFF;
        }
        if (!Objects.equals(item.getExpectedUserId(), item.getActualUserId()))
        {
            return INVENTORY_RESULT_USER_DIFF;
        }
        if (!StringUtils.equals(item.getExpectedStatus(), item.getActualStatus()))
        {
            return INVENTORY_RESULT_STATUS_DIFF;
        }
        return INVENTORY_RESULT_NORMAL;
    }

    private String resolveInventoryDesc(AssetInventoryTaskItem item)
    {
        List<String> messages = new ArrayList<>();
        if (!Objects.equals(item.getExpectedLocationId(), item.getActualLocationId()))
        {
            messages.add("现场位置与账面位置不一致");
        }
        if (!Objects.equals(item.getExpectedUserId(), item.getActualUserId()))
        {
            messages.add("现场责任人与账面责任人不一致");
        }
        if (!StringUtils.equals(item.getExpectedStatus(), item.getActualStatus()))
        {
            messages.add("现场状态与账面状态不一致");
        }
        return messages.isEmpty() ? "盘点正常" : String.join("；", messages);
    }

    private void recalculateTaskSummary(Long taskId, String operateBy)
    {
        AssetInventoryTask currentTask = requireTask(taskId);
        List<AssetInventoryTaskItem> items = selectAssetInventoryTaskItemsByTaskId(taskId);
        int normalCount = 0;
        int diffCount = 0;
        for (AssetInventoryTaskItem item : items)
        {
            if (item.getInventoryTime() == null)
            {
                continue;
            }
            if (INVENTORY_RESULT_NORMAL.equals(item.getInventoryResult()))
            {
                normalCount++;
            }
            else
            {
                diffCount++;
            }
        }

        currentTask.setSummaryTotal(items.size());
        currentTask.setSummaryOk(normalCount);
        currentTask.setSummaryDiff(diffCount);
        currentTask.setUpdateBy(operateBy);
        inventoryTaskMapper.updateAssetInventoryTask(currentTask);
    }

    private AssetInventoryTask requireTask(Long taskId)
    {
        AssetInventoryTask task = inventoryTaskMapper.selectAssetInventoryTaskById(taskId);
        if (StringUtils.isNull(task))
        {
            throw new ServiceException("盘点任务不存在");
        }
        return task;
    }

    private void normalizeTask(AssetInventoryTask task)
    {
        if (StringUtils.isEmpty(task.getStatus()))
        {
            task.setStatus("0");
        }
        if (StringUtils.isEmpty(task.getTaskStatus()))
        {
            task.setTaskStatus(TASK_STATUS_DRAFT);
        }
        normalizeScopeTargets(task);
    }

    /**
     * 盘点范围只能命中一个维度，保存前统一清理其余目标字段，避免任务边界混乱。
     */
    private void normalizeScopeTargets(AssetInventoryTask task)
    {
        if ("ALL".equals(task.getTaskScopeType()))
        {
            task.setTargetDeptId(null);
            task.setTargetLocationId(null);
            task.setTargetCategoryId(null);
            return;
        }
        if (!"DEPT".equals(task.getTaskScopeType()))
        {
            task.setTargetDeptId(null);
        }
        if (!"LOCATION".equals(task.getTaskScopeType()))
        {
            task.setTargetLocationId(null);
        }
        if (!"CATEGORY".equals(task.getTaskScopeType()))
        {
            task.setTargetCategoryId(null);
        }
    }
}
