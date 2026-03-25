<template>
  <ElDrawer v-model="visible" :title="drawerTitle" size="760px" append-to-body destroy-on-close>
    <template v-if="taskData">
      <ElSpace wrap class="mb-4">
        <ElButton
          v-auth="'asset:inventory:edit'"
          type="primary"
          plain
          :disabled="taskData.taskStatus !== 'DRAFT'"
          @click="emitEdit"
        >
          编辑任务
        </ElButton>
        <ElButton
          v-auth="'asset:inventory:start'"
          type="success"
          plain
          :disabled="taskData.taskStatus !== 'DRAFT'"
          @click="emitStart"
        >
          开始盘点
        </ElButton>
        <ElButton
          v-auth="'asset:inventory:finish'"
          type="warning"
          plain
          :disabled="taskData.taskStatus !== 'RUNNING'"
          @click="emitFinish"
        >
          结束盘点
        </ElButton>
        <ElButton
          v-auth="'asset:inventory:processDiff'"
          type="danger"
          plain
          :disabled="!hasDiffItems"
          @click="emitProcessDiff"
        >
          处理差异
        </ElButton>
        <ElButton plain @click="emitRefresh">刷新明细</ElButton>
      </ElSpace>

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

      <ElCard class="mb-4" shadow="never">
        <template #header>
          <div class="flex items-center justify-between">
            <span>快速盘点录入</span>
            <ElTag effect="light" type="info">扫描后会通过后端 `scan` 接口记录盘点结果</ElTag>
          </div>
        </template>

        <ElAlert
          class="mb-3"
          type="warning"
          :closable="false"
          show-icon
          title="这里先保留一个轻量录入入口，便于后续直接接扫码枪或手工录入。"
        />

        <ElForm inline :model="scanForm">
          <ElFormItem label="扫描内容">
            <ElInput
              v-model="scanForm.scanCode"
              class="w-[260px]"
              placeholder="请输入资产编码、二维码或盘点标识"
              clearable
            />
          </ElFormItem>
          <ElFormItem>
            <ElButton
              v-auth="'asset:inventory:start'"
              type="primary"
              :loading="scanLoading"
              @click="handleScan"
            >
              提交盘点
            </ElButton>
          </ElFormItem>
        </ElForm>
      </ElCard>

      <ElCard shadow="never">
        <template #header>
          <div class="flex items-center justify-between">
            <span>盘点明细</span>
            <ElText type="info">差异项后续会在“处理差异”弹窗里统一处理</ElText>
          </div>
        </template>

        <div v-loading="itemLoading">
          <ElEmpty
            v-if="itemRows.length === 0"
            description="暂无盘点明细，等待后端接入或先进行盘点录入。"
          >
            <template #image>
              <ElIcon size="48">
                <Document />
              </ElIcon>
            </template>
          </ElEmpty>

          <ElTable v-else :data="itemRows" row-key="itemId" border stripe>
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
            <ElTableColumn
              prop="inventoryDesc"
              label="差异说明"
              min-width="180"
              show-overflow-tooltip
            />
            <ElTableColumn
              prop="processDesc"
              label="处理说明"
              min-width="180"
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
  import { computed, reactive, ref, watch } from 'vue'
  import { Document } from '@element-plus/icons-vue'
  import { ElMessage } from 'element-plus'
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
    expectedLocationId?: number
    actualLocationId?: number
    inventoryResult?: string
    inventoryDesc?: string
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

  const { asset_inventory_task_status, asset_inventory_result, asset_inventory_process_status } =
    useDict(
      'asset_inventory_task_status',
      'asset_inventory_result',
      'asset_inventory_process_status'
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

  const scanForm = reactive({
    scanCode: ''
  })

  const hasDiffItems = computed(() =>
    itemRows.value.some((item) => item.inventoryResult && item.inventoryResult !== 'NORMAL')
  )

  const drawerTitle = computed(() => {
    if (!props.taskData?.taskName) {
      return '盘点任务详情'
    }
    return `盘点任务详情 - ${props.taskData.taskName}`
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

  /**
   * 只要抽屉打开，就重新拉一次明细，避免列表状态变更后数据陈旧。
   */
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
      } else {
        itemRows.value = []
        scanForm.scanCode = ''
      }
    },
    { immediate: true }
  )

  const emitEdit = () => emit('edit')
  const emitStart = () => emit('start')
  const emitFinish = () => emit('finish')
  const emitProcessDiff = () => emit('process-diff')
  const emitRefresh = async () => {
    await loadTaskItems()
    emit('refresh')
  }

  /**
   * 盘点扫码的轻量入口，后续可以直接替换成扫码枪回车录入。
   */
  const handleScan = async () => {
    if (!props.taskData?.taskId || !scanForm.scanCode.trim()) {
      return
    }

    scanLoading.value = true
    try {
      await scanAssetInventory(props.taskData.taskId, {
        scanCode: scanForm.scanCode.trim()
      })
      ElMessage.success('盘点录入成功')
      scanForm.scanCode = ''
      await emitRefresh()
    } catch (error) {
      console.error('盘点录入失败:', error)
    } finally {
      scanLoading.value = false
    }
  }
</script>
