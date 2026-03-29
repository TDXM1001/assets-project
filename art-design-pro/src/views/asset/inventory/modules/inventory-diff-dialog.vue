<template>
  <ElDialog
    :title="dialogTitle"
    v-model="visible"
    width="1100px"
    destroy-on-close
    append-to-body
    @closed="handleClosed"
  >
    <ElAlert
      class="mb-4"
      type="info"
      :closable="false"
      show-icon
      title="这里只处理已结束任务中的待处理差异。处理完成后，后端会根据结果决定是否回写台账并记录流水。"
    />

    <template v-if="taskData">
      <ElDescriptions :column="2" border class="mb-4">
        <ElDescriptionsItem label="任务编号">{{ taskData.taskNo || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="任务名称">{{ taskData.taskName || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="任务状态">
          <DictTag :options="asset_inventory_task_status" :value="taskData.taskStatus" />
        </ElDescriptionsItem>
        <ElDescriptionsItem label="差异数量">{{ taskData.summaryDiff ?? 0 }}</ElDescriptionsItem>
      </ElDescriptions>
    </template>

    <div class="inventory-diff-metrics">
      <ElCard shadow="never">
        <ElStatistic title="差异总数" :value="diffItems.length" />
      </ElCard>
      <ElCard shadow="never">
        <ElStatistic title="待处理" :value="pendingDiffItems.length" />
      </ElCard>
      <ElCard shadow="never">
        <ElStatistic title="已处理" :value="processedDiffItems.length" />
      </ElCard>
      <ElCard shadow="never">
        <ElStatistic title="盘亏" :value="lossCount" />
      </ElCard>
    </div>

    <ElForm :model="formData" label-width="110px" class="mb-4">
      <ElRow :gutter="16">
        <ElCol :span="12">
          <ElFormItem label="处理状态">
            <ElSelect v-model="formData.processStatus" class="w-full" placeholder="请选择处理状态">
              <ElOption
                v-for="dict in asset_inventory_process_status"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value"
              />
            </ElSelect>
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="筛选视图">
            <ElRadioGroup v-model="activeFilter">
              <ElRadioButton label="待处理" value="PENDING" />
              <ElRadioButton label="已处理" value="PROCESSED" />
              <ElRadioButton label="盘亏" value="LOSS" />
              <ElRadioButton label="全部" value="ALL" />
            </ElRadioGroup>
          </ElFormItem>
        </ElCol>
        <ElCol :span="24">
          <ElFormItem label="处理说明">
            <ElInput
              v-model="formData.processDesc"
              type="textarea"
              :rows="3"
              maxlength="500"
              show-word-limit
              placeholder="请输入差异处理说明，例如：已核实现场位置、责任人已调整、确认盘亏待追责等。"
            />
          </ElFormItem>
        </ElCol>
      </ElRow>
    </ElForm>

    <div class="inventory-diff-toolbar">
      <ElText type="info"> 默认仅勾选待处理差异，已处理项保留为只读核对，避免重复落账。 </ElText>
      <ElTag type="warning" effect="light">已选 {{ selectedRows.length }} 项</ElTag>
    </div>

    <ElTable
      ref="tableRef"
      :data="filteredItems"
      row-key="itemId"
      border
      stripe
      height="440px"
      @selection-change="handleSelectionChange"
    >
      <ElTableColumn type="selection" width="55" :selectable="isSelectableDiffRow" />
      <ElTableColumn prop="assetCode" label="资产编码" min-width="130" fixed="left" />
      <ElTableColumn prop="assetName" label="资产名称" min-width="160" />
      <ElTableColumn label="账面责任人" min-width="140">
        <template #default="{ row }">
          {{ resolveUserName(row, 'expected') }}
        </template>
      </ElTableColumn>
      <ElTableColumn label="现场责任人" min-width="140">
        <template #default="{ row }">
          {{ resolveUserName(row, 'actual') }}
        </template>
      </ElTableColumn>
      <ElTableColumn label="账面位置" min-width="150">
        <template #default="{ row }">
          {{ resolveLocationName(row, 'expected') }}
        </template>
      </ElTableColumn>
      <ElTableColumn label="现场位置" min-width="150">
        <template #default="{ row }">
          {{ resolveLocationName(row, 'actual') }}
        </template>
      </ElTableColumn>
      <ElTableColumn label="账面状态" width="120" align="center">
        <template #default="{ row }">
          <DictTag :options="asset_status" :value="row.expectedStatus" />
        </template>
      </ElTableColumn>
      <ElTableColumn label="现场状态" width="120" align="center">
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
      <ElTableColumn prop="inventoryDesc" label="差异说明" min-width="220" show-overflow-tooltip />
      <ElTableColumn prop="processDesc" label="处理说明" min-width="220" show-overflow-tooltip />
    </ElTable>

    <template #footer>
      <div class="dialog-footer">
        <ElButton @click="visible = false">取消</ElButton>
        <ElButton
          type="primary"
          :loading="submitLoading"
          :disabled="!hasPendingSelection"
          @click="handleSubmit"
        >
          确认处理
        </ElButton>
      </div>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import { computed, nextTick, reactive, ref, watch } from 'vue'
  import { ElMessage, type ElTable } from 'element-plus'
  import DictTag from '@/components/DictTag/index.vue'
  import { useDict } from '@/utils/dict'
  import { processAssetInventoryDiff } from '@/api/asset/inventory'

  interface InventoryTaskRow {
    taskId?: number
    taskNo?: string
    taskName?: string
    taskStatus?: string
    summaryDiff?: number
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
    processStatus?: string
    processDesc?: string
  }

  interface LabelMap {
    [key: string]: string
  }


  type DiffFilter = 'PENDING' | 'PROCESSED' | 'LOSS' | 'ALL'

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
    items: InventoryItemRow[]
    categoryLabelMap: LabelMap
    locationLabelMap: LabelMap
    deptLabelMap: LabelMap
    userLabelMap: LabelMap
  }>()

  const emit = defineEmits<{
    (e: 'update:modelValue', value: boolean): void
    (e: 'success'): void
  }>()

  const visible = computed({
    get: () => props.modelValue,
    set: (value: boolean) => emit('update:modelValue', value)
  })

  const tableRef = ref<InstanceType<typeof ElTable>>()
  const submitLoading = ref(false)
  const selectedRows = ref<InventoryItemRow[]>([])
  const activeFilter = ref<DiffFilter>('PENDING')

  const formData = reactive({
    processStatus: 'PROCESSED',
    processDesc: ''
  })


  const dialogTitle = computed(() => {
    if (!props.taskData?.taskName) {
      return '处理盘点差异'
    }
    return `处理盘点差异 - ${props.taskData.taskName}`
  })

  const isPendingDiffItem = (item?: InventoryItemRow) =>
    Boolean(
      item?.inventoryResult &&
        item.inventoryResult !== 'NORMAL' &&
        item.processStatus !== 'PROCESSED'
    )

  const isProcessedDiffItem = (item?: InventoryItemRow) =>
    Boolean(
      item?.inventoryResult &&
        item.inventoryResult !== 'NORMAL' &&
        item.processStatus === 'PROCESSED'
    )

  const diffItems = computed(() =>
    props.items.filter((item) => item.inventoryResult && item.inventoryResult !== 'NORMAL')
  )
  const pendingDiffItems = computed(() => diffItems.value.filter((item) => isPendingDiffItem(item)))
  const processedDiffItems = computed(() =>
    diffItems.value.filter((item) => isProcessedDiffItem(item))
  )
  const lossCount = computed(
    () => diffItems.value.filter((item) => item.inventoryResult === 'LOSS').length
  )

  const filteredItems = computed(() => {
    if (activeFilter.value === 'ALL') {
      return diffItems.value
    }
    if (activeFilter.value === 'LOSS') {
      return diffItems.value.filter((item) => item.inventoryResult === 'LOSS')
    }
    if (activeFilter.value === 'PROCESSED') {
      return processedDiffItems.value
    }
    return pendingDiffItems.value
  })

  const hasPendingSelection = computed(() =>
    selectedRows.value.some((item) => isPendingDiffItem(item))
  )

  const resolveFromMap = (map: LabelMap, value?: number | string) => {
    if (value === null || value === undefined || value === '') {
      return '-'
    }
    return map[String(value)] || String(value)
  }

  const resolveLocationLabel = (value?: number | string) =>
    resolveFromMap(props.locationLabelMap, value)
  const resolveUserLabel = (value?: number | string) => resolveFromMap(props.userLabelMap, value)

  const resolveLocationName = (row: InventoryItemRow, scene: 'expected' | 'actual') => {
    if (scene === 'expected') {
      return row.expectedLocationName || resolveLocationLabel(row.expectedLocationId)
    }
    return row.actualLocationName || resolveLocationLabel(row.actualLocationId)
  }

  const resolveUserName = (row: InventoryItemRow, scene: 'expected' | 'actual') => {
    if (scene === 'expected') {
      return row.expectedUserName || resolveUserLabel(row.expectedUserId)
    }
    return row.actualUserName || resolveUserLabel(row.actualUserId)
  }

  // 只默认勾选待处理差异，避免把已处理项再次带入处理流程。
  const syncSelection = async () => {
    const pendingIds = new Set(
      filteredItems.value.filter((item) => isPendingDiffItem(item)).map((item) => item.itemId)
    )
    selectedRows.value = filteredItems.value.filter((item) => pendingIds.has(item.itemId))
    await nextTick()
    tableRef.value?.clearSelection()
    filteredItems.value.forEach((item) => {
      if (pendingIds.has(item.itemId)) {
        tableRef.value?.toggleRowSelection(item, true)
      }
    })
  }

  watch(
    () => [props.modelValue, props.items.length, activeFilter.value],
    async ([isOpen]) => {
      if (isOpen) {
        await syncSelection()
      }
    },
    { immediate: true }
  )

  const handleSelectionChange = (rows: InventoryItemRow[]) => {
    selectedRows.value = rows.filter((item) => isPendingDiffItem(item))
  }

  const isSelectableDiffRow = (row: InventoryItemRow) => isPendingDiffItem(row)

  const handleSubmit = async () => {
    if (!props.taskData?.taskId) {
      return
    }
    if (props.taskData.taskStatus !== 'FINISHED') {
      ElMessage.warning('请先结束盘点任务，再处理差异')
      return
    }

    const pendingItemIds = selectedRows.value
      .filter((item) => isPendingDiffItem(item) && item.itemId)
      .map((item) => item.itemId)

    if (pendingItemIds.length === 0) {
      ElMessage.warning('请选择待处理的差异项')
      return
    }

    submitLoading.value = true
    try {
      await processAssetInventoryDiff(props.taskData.taskId, {
        itemIds: pendingItemIds,
        processStatus: formData.processStatus,
        processDesc: formData.processDesc
      })
      ElMessage.success('差异处理成功')
      emit('success')
      visible.value = false
    } catch (error) {
      console.error('处理盘点差异失败:', error)
    } finally {
      submitLoading.value = false
    }
  }

  const handleClosed = () => {
    activeFilter.value = 'PENDING'
    formData.processStatus = 'PROCESSED'
    formData.processDesc = ''
    selectedRows.value = []
  }
</script>

<style scoped>
  .inventory-diff-metrics {
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: 16px;
    margin-bottom: 16px;
  }

  .inventory-diff-toolbar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    margin-bottom: 16px;
  }
</style>
