package com.ruoyi.system.service.asset.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.annotation.DataScope;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.asset.AssetInfo;
import com.ruoyi.system.domain.asset.AssetInventoryTask;
import com.ruoyi.system.domain.asset.AssetInventoryTaskItem;
import com.ruoyi.system.mapper.asset.AssetInfoMapper;
import com.ruoyi.system.mapper.asset.AssetInventoryTaskMapper;
import com.ruoyi.system.service.asset.IAssetEventLogService;
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
    private static final String PROCESS_STATUS_PROCESSED = "PROCESSED";
    private static final String EVENT_TYPE_INVENTORY = "INVENTORY";
    private static final String SOURCE_ORDER_TYPE_INVENTORY_TASK = "INVENTORY_TASK";

    @Autowired
    private AssetInventoryTaskMapper inventoryTaskMapper;

    @Autowired
    private AssetInfoMapper assetInfoMapper;

    @Autowired
    private IAssetEventLogService assetEventLogService;

    @Autowired
    @Lazy
    private IAssetInventoryTaskService assetInventoryTaskServiceProxy;

    @Override
    @DataScope(deptAlias = "scope_dept", userAlias = "scope_user")
    public List<AssetInventoryTask> selectAssetInventoryTaskList(AssetInventoryTask task)
    {
        return inventoryTaskMapper.selectAssetInventoryTaskList(task);
    }

    @Override
    public AssetInventoryTask selectAssetInventoryTaskById(Long taskId)
    {
        AssetInventoryTask query = new AssetInventoryTask();
        query.setTaskId(taskId);
        List<AssetInventoryTask> scopedTasks = assetInventoryTaskServiceProxy.selectAssetInventoryTaskList(query);
        AssetInventoryTask task = scopedTasks.isEmpty() ? null : scopedTasks.get(0);
        if (StringUtils.isNotNull(task))
        {
            task.setItemList(selectAssetInventoryTaskItemsByTaskId(taskId));
        }
        return task;
    }

    @Override
    public List<AssetInventoryTaskItem> selectAssetInventoryTaskItemsByTaskId(Long taskId)
    {
        AssetInventoryTask query = new AssetInventoryTask();
        query.setTaskId(taskId);
        if (assetInventoryTaskServiceProxy.selectAssetInventoryTaskList(query).isEmpty())
        {
            return new ArrayList<>();
        }
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
        validateCategoryScopePermission(task);
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
        if (!TASK_STATUS_DRAFT.equals(dbTask.getTaskStatus()))
        {
            throw new ServiceException("只有草稿状态的盘点任务才能编辑");
        }
        normalizeTask(task);
        validateCategoryScopePermission(task);
        task.setTaskStatus(StringUtils.isEmpty(dbTask.getTaskStatus()) ? TASK_STATUS_DRAFT : dbTask.getTaskStatus());
        int rows = inventoryTaskMapper.updateAssetInventoryTask(task);
        refreshTaskItems(requireTask(task.getTaskId()));
        return rows;
    }

    /**
     * 按分类盘点一期只开放给资产管理员和审计角色，避免部门主管绕过前端直接发请求。
     */
    private void validateCategoryScopePermission(AssetInventoryTask task)
    {
        if (!StringUtils.equals(task.getTaskScopeType(), "CATEGORY"))
        {
            return;
        }
        if (SecurityUtils.isAdmin() || SecurityUtils.hasRole("asset_admin") || SecurityUtils.hasRole("asset_auditor"))
        {
            return;
        }
        throw new ServiceException("按分类盘点仅资产管理员或审计角色可创建");
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
        if (!TASK_STATUS_DRAFT.equals(task.getTaskStatus()))
        {
            throw new ServiceException("只有草稿状态的盘点任务才能开始");
        }
        // 开始盘点时只切换任务状态，不再重刷明细，避免把创建时锁定的账面快照洗掉。
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

        writeFinishEventLogs(taskId, items, operateUserId);
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
        AssetInventoryTask task = requireTask(taskId);
        if (!TASK_STATUS_FINISHED.equals(task.getTaskStatus()))
        {
            throw new ServiceException("只有已完成的盘点任务才能处理差异");
        }
        if (itemIds == null || itemIds.isEmpty())
        {
            throw new ServiceException("请选择需要处理的差异明细");
        }

        List<AssetInventoryTaskItem> processableItems = selectProcessableDiffItems(taskId, itemIds);
        if (processableItems.isEmpty())
        {
            throw new ServiceException("所选差异明细已处理或当前不可处理，请刷新后重试");
        }

        List<Long> processableItemIds = processableItems.stream()
            .map(AssetInventoryTaskItem::getItemId)
            .filter(Objects::nonNull)
            .toList();
        String targetProcessStatus = StringUtils.isEmpty(processStatus) ? PROCESS_STATUS_PENDING : processStatus;
        int rows = inventoryTaskMapper.batchUpdateInventoryProcess(taskId, processableItemIds, targetProcessStatus, processDesc);
        if (rows <= 0)
        {
            throw new ServiceException("所选差异明细未命中可处理数据，请刷新后重试");
        }
        applyProcessedInventoryDiff(taskId, processableItemIds, targetProcessStatus, processDesc, operateBy);
        return rows;
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
            // 明细初始态只锁账面快照，现场值和盘点结果要等真实扫码或结束盘点时再落。
            item.setInventoryResult(null);
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

    /**
     * 盘点结束只记“发现了什么差异”，不在这里直接改主档，避免盘点执行人与资产管理员职责混在一起。
     */
    private void writeFinishEventLogs(Long taskId, List<AssetInventoryTaskItem> items, Long operatorUserId)
    {
        List<AssetInventoryTaskItem> diffItems = items.stream()
            .filter(this::shouldRecordFinishEvent)
            .toList();
        if (diffItems.isEmpty())
        {
            return;
        }

        Map<Long, AssetInfo> assetSnapshotMap = loadAssetSnapshotMap(extractAssetIds(diffItems));
        for (AssetInventoryTaskItem item : diffItems)
        {
            AssetInfo currentAsset = requireAsset(assetSnapshotMap, item.getAssetId());
            assetEventLogService.recordAssetEvent(
                item.getAssetId(),
                EVENT_TYPE_INVENTORY,
                taskId,
                SOURCE_ORDER_TYPE_INVENTORY_TASK,
                currentAsset,
                currentAsset,
                buildFinishEventDesc(item),
                operatorUserId);
        }
    }

    /**
     * 差异处理才是真正落账点：已处理的差异回写主档，仍待处理或无法自动修正的差异只留痕不盲改。
     */
    private void applyProcessedInventoryDiff(Long taskId, List<Long> itemIds, String processStatus, String processDesc, String operateBy)
    {
        List<AssetInventoryTaskItem> items = selectAssetInventoryTaskItemsByTaskId(taskId).stream()
            .filter(item -> item.getItemId() != null && itemIds.contains(item.getItemId()))
            .toList();
        if (items.isEmpty())
        {
            throw new ServiceException("未找到需要处理的盘点明细");
        }

        Map<Long, AssetInfo> assetSnapshotMap = loadAssetSnapshotMap(extractAssetIds(items));
        boolean shouldPostAssetSnapshot = StringUtils.equals(processStatus, PROCESS_STATUS_PROCESSED);
        for (AssetInventoryTaskItem item : items)
        {
            AssetInfo beforeAsset = requireAsset(assetSnapshotMap, item.getAssetId());
            AssetInfo afterAsset = buildProcessedAfterAsset(beforeAsset, item, shouldPostAssetSnapshot);
            boolean snapshotChanged = hasSnapshotChanged(beforeAsset, afterAsset);

            if (shouldPostAssetSnapshot && snapshotChanged)
            {
                afterAsset.setUpdateBy(operateBy);
                assetInfoMapper.updateAssetSnapshot(afterAsset);
                assetSnapshotMap.put(afterAsset.getAssetId(), copyAssetSnapshot(afterAsset));
            }

            assetEventLogService.recordAssetEvent(
                item.getAssetId(),
                EVENT_TYPE_INVENTORY,
                taskId,
                SOURCE_ORDER_TYPE_INVENTORY_TASK,
                beforeAsset,
                afterAsset,
                buildProcessEventDesc(item, processStatus, processDesc, snapshotChanged),
                item.getInventoryUserId());
        }
    }

    /**
     * 差异处理只命中“非正常且尚未处理”的明细，避免运行中提前处理或重复落账。
     */
    private List<AssetInventoryTaskItem> selectProcessableDiffItems(Long taskId, List<Long> itemIds)
    {
        return selectAssetInventoryTaskItemsByTaskId(taskId).stream()
            .filter(item -> item.getItemId() != null && itemIds.contains(item.getItemId()))
            .filter(item -> StringUtils.isNotEmpty(item.getInventoryResult()))
            .filter(item -> !StringUtils.equals(item.getInventoryResult(), INVENTORY_RESULT_NORMAL))
            .filter(item -> StringUtils.isEmpty(item.getProcessStatus())
                || StringUtils.equals(item.getProcessStatus(), PROCESS_STATUS_PENDING))
            .toList();
    }

    private boolean shouldRecordFinishEvent(AssetInventoryTaskItem item)
    {
        return item.getInventoryTime() != null && !StringUtils.equals(item.getInventoryResult(), INVENTORY_RESULT_NORMAL);
    }

    private String buildFinishEventDesc(AssetInventoryTaskItem item)
    {
        if (StringUtils.equals(item.getInventoryResult(), INVENTORY_RESULT_LOSS))
        {
            return "盘点结束，未扫描到该资产，主档暂未自动修正";
        }
        return "盘点结束，发现差异：" + StringUtils.defaultString(item.getInventoryDesc(), "待确认的盘点差异")
            + "，主档暂未自动修正";
    }

    private String buildProcessEventDesc(AssetInventoryTaskItem item, String processStatus, String processDesc,
        boolean snapshotChanged)
    {
        String desc = StringUtils.defaultIfBlank(processDesc, item.getInventoryDesc());
        if (!StringUtils.equals(processStatus, PROCESS_STATUS_PROCESSED))
        {
            return "盘点差异已标记为待处理：" + StringUtils.defaultString(desc, "待后续跟进");
        }
        if (StringUtils.equals(item.getInventoryResult(), INVENTORY_RESULT_LOSS))
        {
            return "盘点差异已确认，资产未找回，主档未自动修正"
                + (StringUtils.isBlank(desc) ? "" : "；" + desc);
        }
        if (snapshotChanged)
        {
            return "盘点差异已处理，已按现场结果回写主档"
                + (StringUtils.isBlank(desc) ? "" : "；" + desc);
        }
        return "盘点差异已确认，但主档字段无需自动修正"
            + (StringUtils.isBlank(desc) ? "" : "；" + desc);
    }

    /**
     * 处理差异时只改被证实的当前快照字段，其余字段沿用主档现状，避免把账面之外的信息误清空。
     */
    private AssetInfo buildProcessedAfterAsset(AssetInfo beforeAsset, AssetInventoryTaskItem item, boolean shouldPostAssetSnapshot)
    {
        AssetInfo afterAsset = copyAssetSnapshot(beforeAsset);
        if (!shouldPostAssetSnapshot)
        {
            return afterAsset;
        }
        if (StringUtils.equals(item.getInventoryResult(), INVENTORY_RESULT_LOSS))
        {
            return afterAsset;
        }

        if (!Objects.equals(item.getExpectedLocationId(), item.getActualLocationId()))
        {
            afterAsset.setCurrentLocationId(item.getActualLocationId());
        }
        if (!Objects.equals(item.getExpectedUserId(), item.getActualUserId()))
        {
            afterAsset.setCurrentUserId(item.getActualUserId());
        }
        if (!StringUtils.equals(item.getExpectedStatus(), item.getActualStatus()) && StringUtils.isNotBlank(item.getActualStatus()))
        {
            afterAsset.setAssetStatus(item.getActualStatus());
        }

        Integer beforeVersion = beforeAsset.getVersionNo() == null ? 0 : beforeAsset.getVersionNo();
        afterAsset.setVersionNo(beforeVersion + 1);
        return afterAsset;
    }

    private Map<Long, AssetInfo> loadAssetSnapshotMap(Collection<Long> assetIds)
    {
        if (assetIds == null || assetIds.isEmpty())
        {
            return Map.of();
        }
        return assetInfoMapper.selectAssetInfoByIds(assetIds).stream()
            .collect(Collectors.toMap(
                AssetInfo::getAssetId,
                this::copyAssetSnapshot,
                (left, right) -> left,
                LinkedHashMap::new));
    }

    private List<Long> extractAssetIds(List<AssetInventoryTaskItem> items)
    {
        return items.stream()
            .map(AssetInventoryTaskItem::getAssetId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
    }

    private AssetInfo requireAsset(Map<Long, AssetInfo> assetSnapshotMap, Long assetId)
    {
        AssetInfo assetInfo = assetSnapshotMap.get(assetId);
        if (assetInfo == null)
        {
            throw new ServiceException("资产主档不存在，无法完成盘点落账");
        }
        return copyAssetSnapshot(assetInfo);
    }

    private AssetInfo copyAssetSnapshot(AssetInfo source)
    {
        AssetInfo target = new AssetInfo();
        target.setAssetId(source.getAssetId());
        target.setAssetCode(source.getAssetCode());
        target.setAssetName(source.getAssetName());
        target.setCategoryId(source.getCategoryId());
        target.setAssetStatus(source.getAssetStatus());
        target.setAssetSource(source.getAssetSource());
        target.setUseOrgDeptId(source.getUseOrgDeptId());
        target.setManageDeptId(source.getManageDeptId());
        target.setCurrentUserId(source.getCurrentUserId());
        target.setCurrentLocationId(source.getCurrentLocationId());
        target.setPurchaseDate(source.getPurchaseDate());
        target.setInboundDate(source.getInboundDate());
        target.setStartUseDate(source.getStartUseDate());
        target.setVersionNo(source.getVersionNo());
        target.setStatus(source.getStatus());
        target.setDelFlag(source.getDelFlag());
        target.setRemark(source.getRemark());
        return target;
    }

    private boolean hasSnapshotChanged(AssetInfo beforeAsset, AssetInfo afterAsset)
    {
        return !Objects.equals(beforeAsset.getAssetStatus(), afterAsset.getAssetStatus())
            || !Objects.equals(beforeAsset.getUseOrgDeptId(), afterAsset.getUseOrgDeptId())
            || !Objects.equals(beforeAsset.getManageDeptId(), afterAsset.getManageDeptId())
            || !Objects.equals(beforeAsset.getCurrentUserId(), afterAsset.getCurrentUserId())
            || !Objects.equals(beforeAsset.getCurrentLocationId(), afterAsset.getCurrentLocationId());
    }

    private AssetInventoryTask requireTask(Long taskId)
    {
        AssetInventoryTask task = selectAssetInventoryTaskById(taskId);
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
