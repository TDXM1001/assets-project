<template>
  <ElDialog
    :title="dialogTitle"
    v-model="visible"
    width="980px"
    destroy-on-close
    append-to-body
    @closed="handleClosed"
  >
    <ElAlert
      class="mb-4"
      type="warning"
      :closable="false"
      show-icon
      title="这里只做差异处理的工作台骨架，后续后端返回更细的处理方式后，可以继续扩展成批量修正。"
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

    <ElForm :model="formData" label-width="110px" class="mb-4">
      <ElRow :gutter="16">
        <ElCol :span="12">
          <ElFormItem label="处理状态">
            <ElSelect v-model="formData.processStatus" class="w-full" placeholder="请选择处理状态">
              <ElOption
                v-for="item in processStatusOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </ElSelect>
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="已选差异">
            <ElTag type="warning" effect="light">{{ selectedRows.length }} 条</ElTag>
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
              placeholder="请输入差异处理说明，例如：已核实、已修正、待补充资料等"
            />
          </ElFormItem>
        </ElCol>
      </ElRow>
    </ElForm>

    <ElTable
      :data="items"
      row-key="itemId"
      border
      stripe
      height="380px"
      @selection-change="handleSelectionChange"
    >
      <ElTableColumn type="selection" width="55" />
      <ElTableColumn prop="assetCode" label="资产编码" min-width="130" />
      <ElTableColumn prop="assetName" label="资产名称" min-width="160" />
      <ElTableColumn label="账面位置" min-width="140">
        <template #default="{ row }">
          {{ resolveLocationLabel(row.expectedLocationId) }}
        </template>
      </ElTableColumn>
      <ElTableColumn label="实际位置" min-width="140">
        <template #default="{ row }">
          {{ resolveLocationLabel(row.actualLocationId) }}
        </template>
      </ElTableColumn>
      <ElTableColumn label="账面责任人" min-width="140">
        <template #default="{ row }">
          {{ resolveUserLabel(row.expectedUserId) }}
        </template>
      </ElTableColumn>
      <ElTableColumn label="实际责任人" min-width="140">
        <template #default="{ row }">
          {{ resolveUserLabel(row.actualUserId) }}
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
      <ElTableColumn prop="inventoryDesc" label="差异说明" min-width="180" show-overflow-tooltip />
      <ElTableColumn prop="processDesc" label="处理说明" min-width="180" show-overflow-tooltip />
    </ElTable>

    <template #footer>
      <div class="dialog-footer">
        <ElButton @click="visible = false">取消</ElButton>
        <ElButton
          type="primary"
          :loading="submitLoading"
          :disabled="selectedRows.length === 0"
          @click="handleSubmit"
        >
          确认处理
        </ElButton>
      </div>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import { computed, reactive, ref, watch } from 'vue'
  import { ElMessage } from 'element-plus'
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
    expectedLocationId?: number
    actualLocationId?: number
    expectedUserId?: number
    actualUserId?: number
    inventoryResult?: string
    inventoryDesc?: string
    processStatus?: string
    processDesc?: string
  }

  interface LabelMap {
    [key: string]: string
  }

  interface ProcessOption {
    label: string
    value: string
  }

  const { asset_inventory_task_status, asset_inventory_result, asset_inventory_process_status } =
    useDict(
      'asset_inventory_task_status',
      'asset_inventory_result',
      'asset_inventory_process_status'
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

  const submitLoading = ref(false)
  const selectedRows = ref<InventoryItemRow[]>([])

  const formData = reactive({
    processStatus: 'PROCESSED',
    processDesc: ''
  })

  const processStatusOptions: ProcessOption[] = [
    { label: '已处理', value: 'PROCESSED' },
    { label: '待处理', value: 'PENDING' }
  ]

  const dialogTitle = computed(() => {
    if (!props.taskData?.taskName) {
      return '处理盘点差异'
    }
    return `处理盘点差异 - ${props.taskData.taskName}`
  })

  const resolveFromMap = (map: LabelMap, value?: number | string) => {
    if (value === null || value === undefined || value === '') {
      return '-'
    }
    return map[String(value)] || String(value)
  }

  const resolveLocationLabel = (value?: number | string) =>
    resolveFromMap(props.locationLabelMap, value)

  const resolveUserLabel = (value?: number | string) => resolveFromMap(props.userLabelMap, value)

  /**
   * 默认勾选所有差异项，方便后续一键处理；没有差异时则保持空选择。
   */
  const syncSelection = () => {
    selectedRows.value = props.items.filter(
      (item) => item.inventoryResult && item.inventoryResult !== 'NORMAL'
    )
  }

  watch(
    () => [props.modelValue, props.items.length],
    ([isOpen]) => {
      if (isOpen) {
        syncSelection()
      }
    },
    { immediate: true }
  )

  const handleSelectionChange = (rows: InventoryItemRow[]) => {
    selectedRows.value = rows
  }

  const handleSubmit = async () => {
    if (!props.taskData?.taskId || selectedRows.value.length === 0) {
      return
    }

    submitLoading.value = true
    try {
      await processAssetInventoryDiff(props.taskData.taskId, {
        itemIds: selectedRows.value.map((item) => item.itemId).filter(Boolean),
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
    formData.processStatus = 'PROCESSED'
    formData.processDesc = ''
    selectedRows.value = []
  }
</script>
