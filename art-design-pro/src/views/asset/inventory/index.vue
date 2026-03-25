<template>
  <div class="asset-inventory-page art-full-height">
    <ArtSearchBar
      :key="searchBarKey"
      v-model="formFilters"
      :items="formItems"
      :showExpand="false"
      @search="handleSearch"
      @reset="handleReset"
    />

    <ElCard class="art-table-card" shadow="never">
      <ArtTableHeader
        :showZebra="false"
        :loading="loading"
        v-model:columns="columnChecks"
        @refresh="handleRefresh"
      >
        <template #left>
          <ElSpace wrap>
            <ElButton v-auth="'asset:inventory:add'" type="primary" @click="handleAdd" v-ripple>
              新建任务
            </ElButton>
            <ElButton
              v-auth="'asset:inventory:export'"
              type="warning"
              plain
              :loading="exportLoading"
              @click="handleExport"
              v-ripple
            >
              导出
            </ElButton>
            <ElButton
              v-auth="'asset:inventory:remove'"
              type="danger"
              plain
              :disabled="multiple"
              @click="handleDelete()"
              v-ripple
            >
              删除
            </ElButton>
          </ElSpace>
        </template>
      </ArtTableHeader>

      <ElAlert
        v-if="tableError"
        class="mb-3"
        type="error"
        :closable="false"
        show-icon
        :title="tableError"
      >
        <template #default>
          <ElButton link type="primary" @click="handleRefresh">重新加载</ElButton>
        </template>
      </ElAlert>

      <div v-if="showEmptyState" class="asset-inventory-empty">
        <ElEmpty :description="emptyDescription">
          <ElButton v-if="!hasAnyFilter" type="primary" @click="handleAdd">新建任务</ElButton>
          <ElButton v-else @click="handleReset">重置筛选</ElButton>
        </ElEmpty>
      </div>

      <ArtTable
        v-else
        v-model:selection="selection"
        :loading="loading"
        :data="data"
        :columns="columns"
        :pagination="pagination"
        rowKey="taskId"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      />
    </ElCard>

    <InventoryDialog
      v-model="dialogVisible"
      :dialog-type="dialogType"
      :task-data="currentTask"
      :category-options="categoryTreeOptions"
      :location-options="locationTreeOptions"
      :dept-options="deptTreeOptions"
      :user-options="userOptions"
      :scope-type-options="scopeTypeOptions"
      @success="handleDialogSuccess"
    />

    <InventoryDetailDrawer
      :key="drawerKey"
      v-model="drawerVisible"
      :task-data="currentTask"
      :scope-type-options="scopeTypeOptions"
      :category-label-map="categoryLabelMap"
      :location-label-map="locationLabelMap"
      :dept-label-map="deptLabelMap"
      :user-label-map="userLabelMap"
      @edit="handleEditFromDrawer"
      @start="handleStartFromDrawer"
      @finish="handleFinishFromDrawer"
      @process-diff="handleProcessDiffFromDrawer"
      @refresh="handleRefreshCurrentTask"
    />

    <InventoryDiffDialog
      :key="diffDialogKey"
      v-model="diffDialogVisible"
      :task-data="currentTask"
      :items="diffItems"
      :category-label-map="categoryLabelMap"
      :location-label-map="locationLabelMap"
      :dept-label-map="deptLabelMap"
      :user-label-map="userLabelMap"
      @success="handleDiffDialogSuccess"
    />
  </div>
</template>

<script setup lang="ts">
  import { computed, h, onMounted, reactive, ref, watch } from 'vue'
  import FileSaver from 'file-saver'
  import { ElButton, ElMessage, ElMessageBox, ElSpace } from 'element-plus'
  import { useTable } from '@/hooks/core/useTable'
  import { useUserStore } from '@/store/modules/user'
  import { useDict } from '@/utils/dict'
  import DictTag from '@/components/DictTag/index.vue'
  import { deptTreeSelect, listUser } from '@/api/system/user'
  import { treeCategorySelect } from '@/api/asset/category'
  import { treeLocationSelect } from '@/api/asset/location'
  import {
    delAssetInventory,
    exportAssetInventory,
    finishAssetInventory,
    getAssetInventory,
    getAssetInventoryItems,
    listAssetInventory,
    startAssetInventory
  } from '@/api/asset/inventory'
  import InventoryDialog from './modules/inventory-dialog.vue'
  import InventoryDetailDrawer from './modules/inventory-detail-drawer.vue'
  import InventoryDiffDialog from './modules/inventory-diff-dialog.vue'

  defineOptions({ name: 'AssetInventory' })

  interface TreeOption {
    id: number
    label: string
    children?: TreeOption[]
  }

  interface UserOption {
    userId: number
    userName: string
  }

  interface ScopeOption {
    label: string
    value: string
  }

  interface LabelMap {
    [key: string]: string
  }

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
    status?: string
    remark?: string
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

  const userStore = useUserStore()
  const { asset_inventory_task_status, asset_inventory_result } = useDict(
    'asset_inventory_task_status',
    'asset_inventory_result'
  )

  const scopeTypeOptions: ScopeOption[] = [
    { label: '全量盘点', value: 'ALL' },
    { label: '按部门盘点', value: 'DEPT' },
    { label: '按位置盘点', value: 'LOCATION' },
    { label: '按分类盘点', value: 'CATEGORY' }
  ]

  const exportLoading = ref(false)
  const tableError = ref('')
  const dialogVisible = ref(false)
  const drawerVisible = ref(false)
  const diffDialogVisible = ref(false)
  const dialogType = ref<'add' | 'edit'>('add')
  const currentTask = ref<InventoryTaskRow>()
  const selection = ref<InventoryTaskRow[]>([])
  const diffItems = ref<InventoryItemRow[]>([])
  const drawerKey = ref(0)
  const diffDialogKey = ref(0)
  const pendingDiffTaskMap = ref<Record<string, boolean>>({})

  const categoryTreeOptions = ref<TreeOption[]>([])
  const locationTreeOptions = ref<TreeOption[]>([])
  const deptTreeOptions = ref<TreeOption[]>([])
  const userOptions = ref<UserOption[]>([])

  const categoryLabelMap = ref<LabelMap>({})
  const locationLabelMap = ref<LabelMap>({})
  const deptLabelMap = ref<LabelMap>({})
  const userLabelMap = ref<LabelMap>({})

  const initialSearchState = {
    taskNo: '',
    taskName: '',
    taskStatus: '',
    taskScopeType: '',
    ownerUserId: undefined as number | undefined,
    executeUserId: undefined as number | undefined
  }

  const formFilters = reactive({ ...initialSearchState })

  const searchBarKey = computed(
    () =>
      `${asset_inventory_task_status.value.length}-${asset_inventory_result.value.length}-${userOptions.value.length}`
  )

  const multiple = computed(() => selection.value.length === 0)

  const hasAnyFilter = computed(
    () =>
      Boolean(formFilters.taskNo?.trim()) ||
      Boolean(formFilters.taskName?.trim()) ||
      Boolean(formFilters.taskStatus) ||
      Boolean(formFilters.taskScopeType) ||
      Boolean(formFilters.ownerUserId) ||
      Boolean(formFilters.executeUserId)
  )

  const hasPermission = (permission: string) => {
    const permissions = userStore.permissions || []
    return permissions.includes('*:*:*') || permissions.includes(permission)
  }

  const flattenTreeLabels = (nodes: TreeOption[], map: LabelMap) => {
    nodes.forEach((node) => {
      map[String(node.id)] = node.label
      if (node.children?.length) {
        flattenTreeLabels(node.children, map)
      }
    })
  }

  const formatScopeType = (scopeType?: string) => {
    if (!scopeType) return '-'
    return scopeTypeOptions.find((item) => item.value === scopeType)?.label || scopeType
  }

  const resolveLabel = (map: LabelMap, value?: number | string) => {
    if (value === null || value === undefined || value === '') {
      return '-'
    }
    return map[String(value)] || String(value)
  }

  const formatTargetLabel = (row: InventoryTaskRow) => {
    if (row.taskScopeType === 'ALL') {
      return '全量盘点'
    }

    if (row.taskScopeType === 'DEPT') {
      return resolveLabel(deptLabelMap.value, row.targetDeptId)
    }

    if (row.taskScopeType === 'LOCATION') {
      return resolveLabel(locationLabelMap.value, row.targetLocationId)
    }

    if (row.taskScopeType === 'CATEGORY') {
      return resolveLabel(categoryLabelMap.value, row.targetCategoryId)
    }

    return '-'
  }

  const formatTimeRange = (startTime?: string, endTime?: string) => {
    if (!startTime && !endTime) {
      return '-'
    }
    return `${startTime || '-'} ~ ${endTime || '-'}`
  }

  const normalizeListResponse = (response: any) =>
    Array.isArray(response) ? response : response?.rows || response?.data || []

  /**
   * 前端统一识别“待处理差异”，保证列表、抽屉、弹窗的入口条件完全一致。
   */
  const isPendingDiffItem = (item?: InventoryItemRow) =>
    Boolean(
      item?.inventoryResult &&
        item.inventoryResult !== 'NORMAL' &&
        item.processStatus !== 'PROCESSED'
    )

  const setPendingDiffState = (taskId: number, items: InventoryItemRow[]) => {
    pendingDiffTaskMap.value[String(taskId)] = items.some((item) => isPendingDiffItem(item))
  }

  const hasPendingDiffTask = (row?: InventoryTaskRow) => {
    if (!row?.taskId || row.taskStatus !== 'FINISHED' || !row.summaryDiff) {
      return false
    }
    return Boolean(pendingDiffTaskMap.value[String(row.taskId)])
  }

  /**
   * 列表层补一层待处理差异缓存，避免已处理任务继续显示“处理差异”入口。
   */
  const syncPendingDiffTaskMap = async (rows: InventoryTaskRow[]) => {
    const nextMap: Record<string, boolean> = {}
    const candidateRows = rows.filter(
      (row) => row.taskId && row.taskStatus === 'FINISHED' && (row.summaryDiff ?? 0) > 0
    )

    await Promise.all(
      candidateRows.map(async (row) => {
        try {
          const response: any = await getAssetInventoryItems(row.taskId!)
          const items = normalizeListResponse(response)
          nextMap[String(row.taskId)] = items.some((item: InventoryItemRow) =>
            isPendingDiffItem(item)
          )
        } catch (error) {
          console.error('加载待处理差异状态失败:', error)
          nextMap[String(row.taskId)] = false
        }
      })
    )

    pendingDiffTaskMap.value = nextMap
  }

  /**
   * 统一拉取任务详情，保证流程动作后的抽屉和差异弹窗看到的是最新状态。
   */
  const loadTaskDetail = async (taskId: number) => {
    const response: any = await getAssetInventory(taskId)
    const detail = response?.data || response || {}
    currentTask.value = { ...detail }
    return currentTask.value
  }

  const loadBaseOptions = async () => {
    try {
      const [categoryRes, locationRes, deptRes, userRes] = await Promise.all([
        treeCategorySelect(),
        treeLocationSelect(),
        deptTreeSelect(),
        listUser({ pageNum: 1, pageSize: 200 })
      ])

      const categoryResponse = normalizeListResponse(categoryRes as any)
      const locationResponse = normalizeListResponse(locationRes as any)
      const deptResponse = normalizeListResponse(deptRes as any)
      const userResponse = normalizeListResponse(userRes as any)

      categoryTreeOptions.value = categoryResponse
      locationTreeOptions.value = locationResponse
      deptTreeOptions.value = deptResponse
      userOptions.value = userResponse.map((item: any) => ({
        userId: item.userId,
        userName: item.nickName ? `${item.nickName} (${item.userName})` : item.userName
      }))

      const categoryMap: LabelMap = {}
      const locationMap: LabelMap = {}
      const deptMap: LabelMap = {}
      flattenTreeLabels(categoryResponse, categoryMap)
      flattenTreeLabels(locationResponse, locationMap)
      flattenTreeLabels(deptResponse, deptMap)
      categoryLabelMap.value = categoryMap
      locationLabelMap.value = locationMap
      deptLabelMap.value = deptMap

      userLabelMap.value = userResponse.reduce((result: LabelMap, item: any) => {
        result[String(item.userId)] = item.nickName
          ? `${item.nickName} (${item.userName})`
          : item.userName
        return result
      }, {})
    } catch (error) {
      console.error('加载盘点页基础数据失败:', error)
      categoryTreeOptions.value = []
      locationTreeOptions.value = []
      deptTreeOptions.value = []
      userOptions.value = []
      categoryLabelMap.value = {}
      locationLabelMap.value = {}
      deptLabelMap.value = {}
      userLabelMap.value = {}
    }
  }

  const {
    columns,
    columnChecks,
    data,
    loading,
    pagination,
    searchParams,
    handleSizeChange,
    handleCurrentChange,
    refreshData,
    getData
  } = useTable({
    core: {
      apiFn: listAssetInventory,
      apiParams: {
        pageNum: 1,
        pageSize: 10
      },
      columnsFactory: () => [
        { type: 'selection', width: 55, align: 'center' },
        { prop: 'taskNo', label: '任务编号', minWidth: 140 },
        { prop: 'taskName', label: '任务名称', minWidth: 180 },
        {
          prop: 'taskScopeType',
          label: '盘点范围',
          width: 120,
          align: 'center',
          formatter: (row: InventoryTaskRow) => formatScopeType(row.taskScopeType)
        },
        {
          prop: 'taskStatus',
          label: '任务状态',
          width: 120,
          align: 'center',
          formatter: (row: InventoryTaskRow) =>
            h(DictTag, { options: asset_inventory_task_status.value, value: row.taskStatus })
        },
        {
          prop: 'targetScope',
          label: '盘点目标',
          minWidth: 160,
          formatter: (row: InventoryTaskRow) => formatTargetLabel(row)
        },
        {
          prop: 'planStartTime',
          label: '计划时间',
          minWidth: 180,
          formatter: (row: InventoryTaskRow) => formatTimeRange(row.planStartTime, row.planEndTime)
        },
        {
          prop: 'summaryTotal',
          label: '应盘',
          width: 90,
          align: 'right',
          formatter: (row: InventoryTaskRow) => row.summaryTotal ?? 0
        },
        {
          prop: 'summaryOk',
          label: '正常',
          width: 90,
          align: 'right',
          formatter: (row: InventoryTaskRow) => row.summaryOk ?? 0
        },
        {
          prop: 'summaryDiff',
          label: '差异',
          width: 90,
          align: 'right',
          formatter: (row: InventoryTaskRow) => row.summaryDiff ?? 0
        },
        {
          prop: 'ownerUserId',
          label: '负责人',
          minWidth: 130,
          formatter: (row: InventoryTaskRow) => resolveLabel(userLabelMap.value, row.ownerUserId)
        },
        {
          prop: 'executeUserId',
          label: '执行人',
          minWidth: 130,
          formatter: (row: InventoryTaskRow) => resolveLabel(userLabelMap.value, row.executeUserId)
        },
        {
          prop: 'operation',
          label: '操作',
          width: 320,
          align: 'right',
          formatter: (row: InventoryTaskRow) => renderActions(row)
        }
      ]
    },
    hooks: {
      onError: (error) => {
        tableError.value = error.message
      },
      onSuccess: () => {
        tableError.value = ''
      }
    }
  })

  const showEmptyState = computed(
    () => !loading.value && !tableError.value && data.value.length === 0
  )

  const emptyDescription = computed(() => {
    if (hasAnyFilter.value) {
      return '没有匹配的盘点任务，请调整筛选条件后重试。'
    }
    return '当前还没有盘点任务，先新建任务再开始盘点流程。'
  })

  const formItems = computed(() => [
    {
      label: '任务编号',
      key: 'taskNo',
      type: 'input',
      props: {
        placeholder: '请输入任务编号',
        clearable: true
      }
    },
    {
      label: '任务名称',
      key: 'taskName',
      type: 'input',
      props: {
        placeholder: '请输入任务名称',
        clearable: true
      }
    },
    {
      label: '任务状态',
      key: 'taskStatus',
      type: 'select',
      props: {
        placeholder: '请选择任务状态',
        clearable: true,
        options: asset_inventory_task_status.value
      }
    },
    {
      label: '盘点范围',
      key: 'taskScopeType',
      type: 'select',
      props: {
        placeholder: '请选择盘点范围',
        clearable: true,
        options: scopeTypeOptions
      }
    },
    {
      label: '负责人',
      key: 'ownerUserId',
      type: 'select',
      props: {
        placeholder: '请选择负责人',
        clearable: true,
        options: userOptions.value.map((item) => ({
          label: item.userName,
          value: item.userId
        }))
      }
    },
    {
      label: '执行人',
      key: 'executeUserId',
      type: 'select',
      props: {
        placeholder: '请选择执行人',
        clearable: true,
        options: userOptions.value.map((item) => ({
          label: item.userName,
          value: item.userId
        }))
      }
    }
  ])

  /**
   * 列表渲染时统一处理流程动作，避免状态判断散落到模板里。
   */
  const renderActions = (row: InventoryTaskRow) => {
    const actionNodes: any[] = []

    if (hasPermission('asset:inventory:query')) {
      actionNodes.push(
        h(
          ElButton,
          { link: true, type: 'primary', onClick: () => handleOpenDetail(row) },
          () => '详情'
        )
      )
    }

    if (hasPermission('asset:inventory:edit') && row.taskStatus === 'DRAFT') {
      actionNodes.push(
        h(ElButton, { link: true, type: 'primary', onClick: () => handleEdit(row) }, () => '编辑')
      )
    }

    if (hasPermission('asset:inventory:start') && row.taskStatus === 'DRAFT') {
      actionNodes.push(
        h(ElButton, { link: true, type: 'success', onClick: () => handleStart(row) }, () => '开始')
      )
    }

    if (hasPermission('asset:inventory:finish') && row.taskStatus === 'RUNNING') {
      actionNodes.push(
        h(ElButton, { link: true, type: 'warning', onClick: () => handleFinish(row) }, () => '结束')
      )
    }

    if (hasPermission('asset:inventory:processDiff') && hasPendingDiffTask(row)) {
      actionNodes.push(
        h(
          ElButton,
          { link: true, type: 'danger', onClick: () => handleProcessDiff(row) },
          () => '处理差异'
        )
      )
    }

    if (hasPermission('asset:inventory:remove')) {
      actionNodes.push(
        h(ElButton, { link: true, type: 'danger', onClick: () => handleDelete(row) }, () => '删除')
      )
    }

    return h(ElSpace, { wrap: true, size: 8 }, () => actionNodes)
  }

  const syncSearchParams = () => {
    Object.assign(searchParams, {
      taskNo: formFilters.taskNo || undefined,
      taskName: formFilters.taskName || undefined,
      taskStatus: formFilters.taskStatus || undefined,
      taskScopeType: formFilters.taskScopeType || undefined,
      ownerUserId: formFilters.ownerUserId || undefined,
      executeUserId: formFilters.executeUserId || undefined,
      pageNum: 1
    })
  }

  const handleSearch = () => {
    syncSearchParams()
    getData()
  }

  const handleReset = () => {
    Object.assign(formFilters, initialSearchState)
    syncSearchParams()
    getData()
  }

  const handleRefresh = () => {
    refreshData()
  }

  const handleAdd = () => {
    dialogType.value = 'add'
    currentTask.value = undefined
    dialogVisible.value = true
  }

  const handleEdit = (row: InventoryTaskRow) => {
    dialogType.value = 'edit'
    currentTask.value = { ...row }
    dialogVisible.value = true
  }

  const handleDialogSuccess = () => {
    refreshData()
    if (drawerVisible.value && currentTask.value?.taskId) {
      drawerKey.value += 1
    }
  }

  const handleOpenDetail = async (row: InventoryTaskRow) => {
    currentTask.value = { ...row }
    drawerVisible.value = true
    drawerKey.value += 1
    if (row.taskId) {
      try {
        await loadTaskDetail(row.taskId)
        drawerKey.value += 1
      } catch (error) {
        console.error('加载盘点任务详情失败:', error)
      }
    }
  }

  const handleEditFromDrawer = () => {
    if (!currentTask.value) return
    handleEdit(currentTask.value)
  }

  const handleStartFromDrawer = () => {
    if (!currentTask.value) return
    handleStart(currentTask.value)
  }

  const handleFinishFromDrawer = () => {
    if (!currentTask.value) return
    handleFinish(currentTask.value)
  }

  const handleProcessDiffFromDrawer = () => {
    if (!currentTask.value) return
    handleProcessDiff(currentTask.value)
  }

  const handleRefreshCurrentTask = async () => {
    if (!currentTask.value?.taskId) return

    const taskId = currentTask.value.taskId
    await loadTaskDetail(taskId)
    const response: any = await getAssetInventoryItems(taskId)
    const detailItems = normalizeListResponse(response)
    setPendingDiffState(taskId, detailItems)
    if (drawerVisible.value && currentTask.value.taskId === taskId) {
      drawerKey.value += 1
    }
    diffItems.value = detailItems.filter((item: InventoryItemRow) => isPendingDiffItem(item))
  }

  const handleStart = async (row: InventoryTaskRow) => {
    if (!row.taskId) return

    try {
      await ElMessageBox.confirm(
        `是否确认开始盘点任务“${row.taskName || row.taskNo || row.taskId}”？`,
        '提示',
        {
          type: 'warning'
        }
      )
      await startAssetInventory(row.taskId)
      ElMessage.success('开始盘点成功')
      await refreshAfterWorkflow(row.taskId)
    } catch (error) {
      if (error !== 'cancel') {
        console.error('开始盘点失败:', error)
      }
    }
  }

  const handleFinish = async (row: InventoryTaskRow) => {
    if (!row.taskId) return

    try {
      await ElMessageBox.confirm(
        `是否确认结束盘点任务“${row.taskName || row.taskNo || row.taskId}”？`,
        '提示',
        {
          type: 'warning'
        }
      )
      await finishAssetInventory(row.taskId)
      ElMessage.success('结束盘点成功')
      await refreshAfterWorkflow(row.taskId)
    } catch (error) {
      if (error !== 'cancel') {
        console.error('结束盘点失败:', error)
      }
    }
  }

  const loadDiffItems = async (taskId: number) => {
    const response: any = await getAssetInventoryItems(taskId)
    const rows = normalizeListResponse(response)
    setPendingDiffState(taskId, rows)
    return rows.filter((item: InventoryItemRow) => isPendingDiffItem(item))
  }

  const handleProcessDiff = async (row: InventoryTaskRow) => {
    if (!row.taskId) return

    currentTask.value = { ...row }
    try {
      await loadTaskDetail(row.taskId)
      diffItems.value = await loadDiffItems(row.taskId)
    } catch (error) {
      console.error('加载差异项失败:', error)
      diffItems.value = []
    }
    if (currentTask.value?.taskStatus !== 'FINISHED') {
      ElMessage.warning('请先结束盘点任务，再处理差异')
      return
    }
    if (diffItems.value.length === 0) {
      ElMessage.warning('当前没有待处理的差异项')
      return
    }
    diffDialogVisible.value = true
    diffDialogKey.value += 1
  }

  const handleDiffDialogSuccess = async () => {
    await refreshData()
    if (drawerVisible.value && currentTask.value?.taskId) {
      await loadTaskDetail(currentTask.value.taskId)
      drawerKey.value += 1
    }
  }

  const refreshAfterWorkflow = async (taskId: number) => {
    await refreshData()
    if (currentTask.value?.taskId === taskId) {
      await loadTaskDetail(taskId)
      drawerKey.value += 1
    }
  }

  const handleDelete = async (row?: InventoryTaskRow) => {
    const taskIds =
      row?.taskId ||
      selection.value
        .map((item) => item.taskId)
        .filter(Boolean)
        .join(',')
    if (!taskIds) return

    try {
      await ElMessageBox.confirm(`是否确认删除盘点任务“${row?.taskName || taskIds}”？`, '提示', {
        type: 'warning'
      })
      await delAssetInventory(taskIds)
      ElMessage.success('删除成功')
      selection.value = []
      refreshData()
      if (drawerVisible.value && currentTask.value?.taskId === row?.taskId) {
        drawerVisible.value = false
      }
    } catch (error) {
      if (error !== 'cancel') {
        console.error('删除盘点任务失败:', error)
      }
    }
  }

  const handleExport = async () => {
    exportLoading.value = true
    try {
      const blob = await exportAssetInventory({
        taskNo: formFilters.taskNo || undefined,
        taskName: formFilters.taskName || undefined,
        taskStatus: formFilters.taskStatus || undefined,
        taskScopeType: formFilters.taskScopeType || undefined,
        ownerUserId: formFilters.ownerUserId || undefined,
        executeUserId: formFilters.executeUserId || undefined
      })
      FileSaver.saveAs(blob as Blob, '资产盘点任务.xlsx')
      ElMessage.success('导出成功')
    } catch (error) {
      console.error('导出盘点任务失败:', error)
    } finally {
      exportLoading.value = false
    }
  }

  onMounted(async () => {
    await loadBaseOptions()
    syncSearchParams()
    await getData()
  })

  watch(
    data,
    (rows) => {
      void syncPendingDiffTaskMap(rows as InventoryTaskRow[])
    },
    { immediate: true }
  )
</script>
