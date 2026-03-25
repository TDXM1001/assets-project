<template>
  <ElDrawer v-model="visible" :title="drawerTitle" size="920px" append-to-body destroy-on-close>
    <template v-if="taskData">
      <div class="inventory-detail-toolbar">
        <ElSpace wrap>
          <ElButton
            v-auth="'asset:inventory:edit'"
            type="primary"
            plain
            :disabled="taskData.taskStatus !== 'DRAFT'"
            @click="emit('edit')"
          >
            编辑任务
          </ElButton>
          <ElButton
            v-auth="'asset:inventory:start'"
            type="success"
            plain
            :disabled="taskData.taskStatus !== 'DRAFT'"
            @click="emit('start')"
          >
            开始盘点
          </ElButton>
          <ElButton
            v-auth="'asset:inventory:finish'"
            type="warning"
            plain
            :disabled="taskData.taskStatus !== 'RUNNING'"
            @click="emit('finish')"
          >
            结束盘点
          </ElButton>
          <ElButton
            v-auth="'asset:inventory:processDiff'"
            type="danger"
            plain
            :disabled="!hasPendingDiffItems"
            @click="emit('process-diff')"
          >
            处理差异
          </ElButton>
          <ElButton plain @click="emitRefresh">刷新明细</ElButton>
        </ElSpace>
      </div>

      <ElDescriptions :column="2" border class="mb-4">
        <ElDescriptionsItem label="任务编号">{{ taskData.taskNo || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="任务名称">{{ taskData.taskName || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="盘点范围">{{
          formatScopeType(taskData.taskScopeType)
        }}</ElDescriptionsItem>
        <ElDescriptionsItem label="任务状态">
          <DictTag :options="asset_inventory_task_status" :value="taskData.taskStatus" />
        </ElDescriptionsItem>
        <ElDescriptionsItem label="负责人">{{
          resolveUserLabel(taskData.ownerUserId)
        }}</ElDescriptionsItem>
        <ElDescriptionsItem label="执行人">{{
          resolveUserLabel(taskData.executeUserId)
        }}</ElDescriptionsItem>
        <ElDescriptionsItem label="目标部门">{{
          resolveDeptLabel(taskData.targetDeptId)
        }}</ElDescriptionsItem>
        <ElDescriptionsItem label="目标位置">{{
          resolveLocationLabel(taskData.targetLocationId)
        }}</ElDescriptionsItem>
        <ElDescriptionsItem label="目标分类">{{
          resolveCategoryLabel(taskData.targetCategoryId)
        }}</ElDescriptionsItem>
        <ElDescriptionsItem label="计划时间">
          {{ formatTimeRange(taskData.planStartTime, taskData.planEndTime) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="应盘数量">{{ taskData.summaryTotal ?? 0 }}</ElDescriptionsItem>
        <ElDescriptionsItem label="正常数量">{{ taskData.summaryOk ?? 0 }}</ElDescriptionsItem>
        <ElDescriptionsItem label="差异数量">{{ taskData.summaryDiff ?? 0 }}</ElDescriptionsItem>
        <ElDescriptionsItem label="备注">{{ taskData.remark || '-' }}</ElDescriptionsItem>
      </ElDescriptions>

      <div class="inventory-metrics">
        <ElCard shadow="never">
          <ElStatistic title="已录入" :value="scannedCount" />
        </ElCard>
        <ElCard shadow="never">
          <ElStatistic title="待处理差异" :value="pendingDiffCount" />
        </ElCard>
        <ElCard shadow="never">
          <ElStatistic title="盘亏" :value="lossCount" />
        </ElCard>
      </div>

      <ElCard class="mb-4" shadow="never">
        <template #header>
          <div class="inventory-card-header">
            <span>盘点执行台</span>
            <ElTag effect="light" type="info">支持扫码枪回车或手动录入编码</ElTag>
          </div>
        </template>

        <ElAlert
          class="mb-4"
          :type="scanAlertType"
          :closable="false"
          show-icon
          :title="scanAlertTitle"
        />

        <ElForm inline :model="scanForm">
          <ElFormItem label="扫描内容">
            <ElInput
              ref="scanInputRef"
              v-model="scanForm.scanCode"
              class="inventory-scan-input"
              placeholder="请输入资产编码、二维码或盘点标识"
              clearable
              @keyup.enter="handleScan"
            />
          </ElFormItem>
          <ElFormItem>
            <ElButton
              v-auth="'asset:inventory:start'"
              type="primary"
              :loading="scanLoading"
              :disabled="taskData.taskStatus !== 'RUNNING'"
              @click="handleScan"
            >
              提交盘点
            </ElButton>
          </ElFormItem>
        </ElForm>

        <div v-if="lastScanFeedback" class="inventory-last-feedback">
          <ElTag :type="lastScanFeedback.type" effect="light">{{ lastScanFeedback.title }}</ElTag>
          <span>{{ lastScanFeedback.description }}</span>
        </div>

        <div class="inventory-recent-block">
          <div class="inventory-recent-block__title">最近扫码</div>
          <ElEmpty v-if="recentScannedItems.length === 0" description="当前还没有盘点录入记录" />
          <ElTimeline v-else>
            <ElTimelineItem
              v-for="item in recentScannedItems"
              :key="item.itemId"
              :timestamp="item.inventoryTime || '未记录时间'"
              placement="top"
            >
              <div class="inventory-recent-item">
                <div class="inventory-recent-item__main">
                  <strong>{{ item.assetCode || '-' }}</strong>
                  <span>{{ item.assetName || '-' }}</span>
                </div>
                <div class="inventory-recent-item__meta">
                  <DictTag :options="asset_inventory_result" :value="item.inventoryResult" />
                  <span>{{
                    item.inventoryUserName || resolveUserLabel(item.inventoryUserId)
                  }}</span>
                </div>
              </div>
            </ElTimelineItem>
          </ElTimeline>
        </div>
      </ElCard>

      <ElCard shadow="never">
        <template #header>
          <div class="inventory-card-header">
            <span>盘点明细</span>
            <ElText type="info"
              >差异项会在“处理差异”里统一落账，明细页负责快速核对现场结果。</ElText
            >
          </div>
        </template>

        <div v-loading="itemLoading">
          <ElEmpty
            v-if="itemRows.length === 0"
            description="暂无盘点明细，等待任务生成资产清单或先开始盘点。"
          >
            <template #image>
              <ElIcon size="48">
                <Document />
              </ElIcon>
            </template>
          </ElEmpty>

          <ElTable v-else :data="itemRows" row-key="itemId" border stripe>
            <ElTableColumn prop="assetCode" label="资产编码" min-width="130" fixed="left" />
            <ElTableColumn prop="assetName" label="资产名称" min-width="160" />
            <ElTableColumn label="账面责任人" min-width="150">
              <template #default="{ row }">
                {{ row.expectedUserName || resolveUserLabel(row.expectedUserId) }}
              </template>
            </ElTableColumn>
            <ElTableColumn label="现场责任人" min-width="150">
              <template #default="{ row }">
                {{ row.actualUserName || resolveUserLabel(row.actualUserId) }}
              </template>
            </ElTableColumn>
            <ElTableColumn label="账面位置" min-width="150">
              <template #default="{ row }">
                {{ row.expectedLocationName || resolveLocationLabel(row.expectedLocationId) }}
              </template>
            </ElTableColumn>
            <ElTableColumn label="现场位置" min-width="150">
              <template #default="{ row }">
                {{ row.actualLocationName || resolveLocationLabel(row.actualLocationId) }}
              </template>
            </ElTableColumn>
            <ElTableColumn label="账面状态" width="130" align="center">
              <template #default="{ row }">
                <DictTag :options="asset_status" :value="row.expectedStatus" />
              </template>
            </ElTableColumn>
            <ElTableColumn label="现场状态" width="130" align="center">
              <template #default="{ row }">
                <DictTag :options="asset_status" :value="row.actualStatus" />
              </template>
            </ElTableColumn>
            <ElTableColumn label="盘点结果" width="120" align="center">
              <template #default="{ row }">
                <DictTag :options="asset_inventory_result" :value="row.inventoryResult" />
              </template>
            </ElTableColumn>
            <ElTableColumn label="处理状态" width="120" align="center">
              <template #default="{ row }">
                <DictTag :options="asset_inventory_process_status" :value="row.processStatus" />
              </template>
            </ElTableColumn>
            <ElTableColumn label="盘点人" min-width="140">
              <template #default="{ row }">
                {{ row.inventoryUserName || resolveUserLabel(row.inventoryUserId) }}
              </template>
            </ElTableColumn>
            <ElTableColumn prop="inventoryTime" label="盘点时间" min-width="170" />
            <ElTableColumn
              prop="inventoryDesc"
              label="差异说明"
              min-width="220"
              show-overflow-tooltip
            />
            <ElTableColumn
              prop="processDesc"
              label="处理说明"
              min-width="220"
              show-overflow-tooltip
            />
          </ElTable>
        </div>
      </ElCard>
    </template>

    <ElEmpty v-else description="请先选择一个盘点任务查看详情。">
      <template #image>
        <ElIcon size="48">
          <Document />
        </ElIcon>
      </template>
    </ElEmpty>
  </ElDrawer>
</template>

<script setup lang="ts">
  import { computed, nextTick, reactive, ref, watch } from 'vue'
  import { Document } from '@element-plus/icons-vue'
  import { ElMessage, type ElInput } from 'element-plus'
  import DictTag from '@/components/DictTag/index.vue'
  import { useDict } from '@/utils/dict'
  import { getAssetInventoryItems, scanAssetInventory } from '@/api/asset/inventory'

  interface InventoryTaskRow {
    taskId?: number
    taskNo?: string
    taskName?: string
    taskScopeType?: string
    taskStatus?: string
    targetDeptId?: number
    targetLocationId?: number
    targetCategoryId?: number
    planStartTime?: string
    planEndTime?: string
    ownerUserId?: number
    executeUserId?: number
    summaryTotal?: number
    summaryOk?: number
    summaryDiff?: number
    remark?: string
  }

  interface InventoryItemRow {
    itemId?: number
    assetCode?: string
    assetName?: string
    expectedUserId?: number
    actualUserId?: number
    expectedUserName?: string
    actualUserName?: string
    expectedLocationId?: number
    actualLocationId?: number
    expectedLocationName?: string
    actualLocationName?: string
    expectedStatus?: string
    actualStatus?: string
    inventoryResult?: string
    inventoryDesc?: string
    inventoryTime?: string
    inventoryUserId?: number
    inventoryUserName?: string
    processStatus?: string
    processDesc?: string
  }

  interface LabelMap {
    [key: string]: string
  }

  interface ScopeOption {
    label: string
    value: string
  }

  interface ScanFeedback {
    type: 'success' | 'warning' | 'info'
    title: string
    description: string
  }

  const {
    asset_inventory_task_status,
    asset_inventory_result,
    asset_inventory_process_status,
    asset_status
  } = useDict(
    'asset_inventory_task_status',
    'asset_inventory_result',
    'asset_inventory_process_status',
    'asset_status'
  )

  const props = defineProps<{
    modelValue: boolean
    taskData?: InventoryTaskRow
    scopeTypeOptions: ScopeOption[]
    categoryLabelMap: LabelMap
    locationLabelMap: LabelMap
    deptLabelMap: LabelMap
    userLabelMap: LabelMap
  }>()

  const emit = defineEmits<{
    (e: 'update:modelValue', value: boolean): void
    (e: 'refresh'): void
    (e: 'edit'): void
    (e: 'start'): void
    (e: 'finish'): void
    (e: 'process-diff'): void
  }>()

  const visible = computed({
    get: () => props.modelValue,
    set: (value: boolean) => emit('update:modelValue', value)
  })

  const itemLoading = ref(false)
  const scanLoading = ref(false)
  const itemRows = ref<InventoryItemRow[]>([])
  const scanInputRef = ref<InstanceType<typeof ElInput>>()
  const lastScanFeedback = ref<ScanFeedback>()

  const scanForm = reactive({
    scanCode: ''
  })

  const hasPendingDiffItems = computed(
    () =>
      props.taskData?.taskStatus === 'FINISHED' &&
      itemRows.value.some(
        (item) =>
          item.inventoryResult &&
          item.inventoryResult !== 'NORMAL' &&
          item.processStatus !== 'PROCESSED'
      )
  )

  const drawerTitle = computed(() => {
    if (!props.taskData?.taskName) {
      return '盘点任务详情'
    }
    return `盘点任务详情 - ${props.taskData.taskName}`
  })

  const scannedItems = computed(() =>
    itemRows.value
      .filter((item) => Boolean(item.inventoryTime))
      .sort(
        (a, b) =>
          new Date(b.inventoryTime || 0).getTime() - new Date(a.inventoryTime || 0).getTime()
      )
  )

  const recentScannedItems = computed(() => scannedItems.value.slice(0, 6))
  const scannedCount = computed(() => scannedItems.value.length)
  const pendingDiffCount = computed(
    () =>
      itemRows.value.filter(
        (item) =>
          item.inventoryResult &&
          item.inventoryResult !== 'NORMAL' &&
          item.processStatus !== 'PROCESSED'
      ).length
  )
  const lossCount = computed(
    () => itemRows.value.filter((item) => item.inventoryResult === 'LOSS').length
  )

  const scanAlertType = computed(() => {
    if (props.taskData?.taskStatus === 'RUNNING') return 'success'
    if (props.taskData?.taskStatus === 'FINISHED') return 'info'
    return 'warning'
  })

  const scanAlertTitle = computed(() => {
    if (props.taskData?.taskStatus === 'RUNNING') {
      return '任务已开始，可以连续扫码盘点；每次录入后都会刷新明细和统计。'
    }
    if (props.taskData?.taskStatus === 'FINISHED') {
      return '任务已结束，当前只读查看盘点结果；如仍有待处理差异，请直接进入差异处理。'
    }
    return '任务还未开始，先确认范围后启动任务，再进入现场扫码。'
  })

  const resolveFromMap = (map: LabelMap, value?: number | string) => {
    if (value === null || value === undefined || value === '') {
      return '-'
    }
    return map[String(value)] || String(value)
  }

  const formatScopeType = (scopeType?: string) => {
    if (!scopeType) return '-'
    return props.scopeTypeOptions.find((item) => item.value === scopeType)?.label || scopeType
  }

  const resolveCategoryLabel = (value?: number | string) =>
    resolveFromMap(props.categoryLabelMap, value)
  const resolveLocationLabel = (value?: number | string) =>
    resolveFromMap(props.locationLabelMap, value)
  const resolveDeptLabel = (value?: number | string) => resolveFromMap(props.deptLabelMap, value)
  const resolveUserLabel = (value?: number | string) => resolveFromMap(props.userLabelMap, value)

  const formatTimeRange = (startTime?: string, endTime?: string) => {
    if (!startTime && !endTime) {
      return '-'
    }
    return `${startTime || '-'} ~ ${endTime || '-'}`
  }

  const loadTaskItems = async () => {
    if (!props.taskData?.taskId) {
      itemRows.value = []
      return
    }

    itemLoading.value = true
    try {
      const response: any = await getAssetInventoryItems(props.taskData.taskId)
      const data = Array.isArray(response) ? response : response?.rows || response?.data || []
      itemRows.value = data
    } catch (error) {
      console.error('加载盘点明细失败:', error)
      itemRows.value = []
    } finally {
      itemLoading.value = false
    }
  }

  watch(
    () => [props.modelValue, props.taskData?.taskId],
    async ([isOpen]) => {
      if (isOpen) {
        await loadTaskItems()
        await nextTick()
        if (props.taskData?.taskStatus === 'RUNNING') {
          scanInputRef.value?.focus?.()
        }
      } else {
        itemRows.value = []
        scanForm.scanCode = ''
        lastScanFeedback.value = undefined
      }
    },
    { immediate: true }
  )

  const emitRefresh = async () => {
    await loadTaskItems()
    emit('refresh')
  }

  const buildScanFeedback = (scanCode: string): ScanFeedback => {
    const latestMatchedItem = itemRows.value.find((item) => item.assetCode === scanCode)
    if (!latestMatchedItem) {
      return {
        type: 'success',
        title: '扫码成功',
        description: `已记录扫描内容 ${scanCode}，明细已刷新。`
      }
    }

    const resultText =
      asset_inventory_result.value.find(
        (item: { value?: string; label?: string }) =>
          item.value === latestMatchedItem.inventoryResult
      )?.label ||
      latestMatchedItem.inventoryResult ||
      '待确认'

    return {
      type:
        latestMatchedItem.inventoryResult && latestMatchedItem.inventoryResult !== 'NORMAL'
          ? 'warning'
          : 'success',
      title: `${latestMatchedItem.assetCode || scanCode} 已录入`,
      description: `盘点结果：${resultText}，盘点人：${
        latestMatchedItem.inventoryUserName || resolveUserLabel(latestMatchedItem.inventoryUserId)
      }。`
    }
  }

  /**
   * 详情抽屉直接承担一线执行工作台，扫码完成后立刻刷新明细并给出最近一次录入反馈。
   */
  const handleScan = async () => {
    if (!props.taskData?.taskId || !scanForm.scanCode.trim()) {
      ElMessage.warning('请先输入待盘点的资产编码或二维码')
      return
    }
    if (props.taskData.taskStatus !== 'RUNNING') {
      ElMessage.warning('只有进行中的盘点任务才能录入盘点结果')
      return
    }

    const scanCode = scanForm.scanCode.trim()
    scanLoading.value = true
    try {
      await scanAssetInventory(props.taskData.taskId, { scanCode })
      ElMessage.success('盘点录入成功')
      scanForm.scanCode = ''
      await emitRefresh()
      lastScanFeedback.value = buildScanFeedback(scanCode)
      await nextTick()
      scanInputRef.value?.focus?.()
    } catch (error) {
      console.error('盘点录入失败:', error)
    } finally {
      scanLoading.value = false
    }
  }
</script>

<style scoped>
  .inventory-detail-toolbar {
    margin-bottom: 16px;
  }

  .inventory-metrics {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 16px;
    margin-bottom: 16px;
  }

  .inventory-card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
  }

  .inventory-scan-input {
    width: 320px;
  }

  .inventory-last-feedback {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 16px;
  }

  .inventory-recent-block__title {
    margin-bottom: 12px;
    font-weight: 600;
  }

  .inventory-recent-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
  }

  .inventory-recent-item__main,
  .inventory-recent-item__meta {
    display: flex;
    align-items: center;
    gap: 12px;
  }
</style>
