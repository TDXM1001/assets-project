<template>
  <div class="asset-repair-page art-full-height">
    <ArtSearchBar
      :key="searchBarKey"
      v-model="formFilters"
      :items="searchItems"
      :showExpand="false"
      @search="handleSearch"
      @reset="handleReset"
    />

    <ElCard class="art-table-card" shadow="never">
      <div class="asset-repair-toolbar">
        <div class="asset-repair-toolbar__left">
          <div class="asset-repair-toolbar__label">维修状态</div>
          <ElRadioGroup v-model="activeStatus" size="small" @change="handleStatusChange">
            <ElRadioButton label="ALL">全部</ElRadioButton>
            <ElRadioButton v-for="item in statusOptions" :key="item.value" :label="item.value">
              {{ item.label }}
            </ElRadioButton>
          </ElRadioGroup>
        </div>
        <div class="asset-repair-toolbar__tip">{{ scopeTip }}</div>
      </div>

      <ArtTableHeader
        :showZebra="false"
        :loading="loading"
        v-model:columns="columnChecks"
        @refresh="refreshData"
      >
        <template #left>
          <ElSpace wrap>
            <ElButton v-auth="'asset:repair:add'" type="primary" @click="handleAdd" v-ripple>
              新增维修单
            </ElButton>
            <ElButton
              v-auth="'asset:repair:export'"
              type="warning"
              plain
              :loading="exportLoading"
              @click="handleExport"
              v-ripple
            >
              导出
            </ElButton>
          </ElSpace>
        </template>
      </ArtTableHeader>

      <div v-if="showEmptyState" class="asset-repair-empty">
        <ElEmpty :description="emptyDescription">
          <ElButton v-if="!hasAnyFilter" type="primary" @click="handleAdd">新增维修单</ElButton>
          <ElButton v-else @click="handleReset">重置筛选</ElButton>
        </ElEmpty>
      </div>

      <ArtTable
        v-else
        rowKey="repairId"
        :loading="loading"
        :columns="columns"
        :data="data"
        :pagination="pagination"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      />
    </ElCard>

    <RepairDialog
      v-model="dialogVisible"
      :dialog-type="dialogType"
      :repair-data="currentRepair"
      @success="refreshData"
    />

    <RepairDetailDrawer
      v-model="detailDrawerVisible"
      :repair-data="currentRepair"
      @edit="handleEdit(currentRepair)"
      @submit="handleSubmitRepair(currentRepair)"
      @approve="openApproveDialog(currentRepair, 'approve')"
      @reject="openApproveDialog(currentRepair, 'reject')"
      @finish="openFinishDialog(currentRepair)"
      @cancel="handleCancelRepairAndRefresh(currentRepair)"
      @attachments="handleOpenAttachments(currentRepair)"
      @create-disposal="handleCreateDisposalOrder(currentRepair)"
      @view-disposal="handleViewRelatedDisposalOrder"
    />

    <RepairApproveDialog
      v-model="approveDialogVisible"
      :action-type="approveActionType"
      :repair-data="currentRepair"
      @confirm="handleApproveConfirm"
    />

    <RepairFinishDialog
      v-model="finishDialogVisible"
      :repair-data="currentRepair"
      @confirm="handleFinishConfirm"
    />

    <AssetAttachmentDrawer
      v-model="attachmentDrawerVisible"
      biz-type="ASSET_REPAIR"
      :biz-id="currentRepair?.repairId"
      :biz-title="currentRepair?.repairNo || '维修单'"
      permission-prefix="asset:repair"
    />
  </div>
</template>

<script setup lang="ts">
  import { computed, h, onMounted, reactive, ref } from 'vue'
  import { useRouter } from 'vue-router'
  import FileSaver from 'file-saver'
  import { ElButton, ElMessage, ElMessageBox, ElSpace, ElTag } from 'element-plus'
  import { useTable } from '@/hooks/core/useTable'
  import { useUserStore } from '@/store/modules/user'
  import { getAssetInfo } from '@/api/asset/info'
  import { getLinkedAssetOrder } from '@/api/asset/order'
  import { useAssetRoleScope } from '../shared/use-asset-role-scope'
  import AssetAttachmentDrawer from '../shared/asset-attachment-drawer.vue'
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import {
    approveAssetRepair,
    cancelAssetRepair,
    delAssetRepair,
    exportAssetRepair,
    finishAssetRepair,
    getAssetRepair,
    listAssetRepair,
    rejectAssetRepair,
    submitAssetRepair
  } from '@/api/asset/repair'
  import RepairApproveDialog from './modules/repair-approve-dialog.vue'
  import RepairDetailDrawer from './modules/repair-detail-drawer.vue'
  import RepairDialog from './modules/repair-dialog.vue'
  import RepairFinishDialog from './modules/repair-finish-dialog.vue'

  defineOptions({ name: 'AssetRepair' })

  const DISPOSAL_BRIDGE_STORAGE_KEY = 'asset-order-disposal-bridge'

  const statusOptions = [
    { label: '草稿', value: 'DRAFT' },
    { label: '待审批', value: 'SUBMITTED' },
    { label: '维修中', value: 'APPROVED' },
    { label: '已驳回', value: 'REJECTED' },
    { label: '已完成', value: 'FINISHED' },
    { label: '已取消', value: 'CANCELED' }
  ]

  const statusMap = statusOptions.reduce<
    Record<string, { label: string; type: 'success' | 'warning' | 'info' | 'danger' }>
  >((acc, item) => {
    const typeMap: Record<string, 'success' | 'warning' | 'info' | 'danger'> = {
      DRAFT: 'info',
      SUBMITTED: 'warning',
      APPROVED: 'warning',
      REJECTED: 'danger',
      FINISHED: 'success',
      CANCELED: 'info'
    }
    acc[item.value] = { label: item.label, type: typeMap[item.value] || 'info' }
    return acc
  }, {})

  const resultTypeMap: Record<string, string> = {
    RESUME_USE: '恢复在用',
    TO_IDLE: '转闲置',
    SUGGEST_DISPOSAL: '建议报废'
  }

  const resultTypeOptions = Object.entries(resultTypeMap).map(([value, label]) => ({
    value,
    label
  }))

  const getRepairItems = (repair?: any) => {
    if (Array.isArray(repair?.itemList) && repair.itemList.length > 0) {
      return repair.itemList.filter(Boolean)
    }
    if (Array.isArray(repair?.repairItems) && repair.repairItems.length > 0) {
      return repair.repairItems.filter(Boolean)
    }
    if (Number(repair?.itemCount || 0) > 1) {
      // 列表页只拿到头表时，如果明确是多资产维修，就不要伪装成单资产。
      return []
    }
    if (repair?.assetId) {
      return [
        {
          assetId: repair.assetId,
          assetCode: repair.assetCode || '',
          assetName: repair.assetName || '',
          beforeStatus: repair.beforeStatus || '',
          afterStatus: repair.afterStatus || repair.beforeStatus || '',
          resultType: repair.resultType || '',
          faultDesc: repair.faultDesc || '',
          remark: repair.remark || '',
          currentUserId: repair.currentUserId,
          currentDeptId: repair.currentDeptId || repair.useOrgDeptId,
          currentLocationId: repair.currentLocationId
        }
      ]
    }
    return []
  }

  const getPrimaryRepairItem = (repair?: any) => getRepairItems(repair)[0]

  const normalizeRepairDetail = (repair?: any) => {
    // 列表、详情和历史数据可能处在不同阶段，这里统一归一成 itemList 结构给前端消费。
    const itemList = getRepairItems(repair)
    const primaryItem = itemList[0]
    return {
      ...(repair || {}),
      itemList,
      itemCount: itemList.length,
      assetId: repair?.assetId ?? primaryItem?.assetId,
      assetCode: repair?.assetCode || primaryItem?.assetCode || '',
      assetName: repair?.assetName || primaryItem?.assetName || '',
      beforeStatus: repair?.beforeStatus || primaryItem?.beforeStatus || '',
      afterStatus:
        repair?.afterStatus || primaryItem?.afterStatus || primaryItem?.beforeStatus || '',
      faultDesc: repair?.faultDesc || primaryItem?.faultDesc || ''
    }
  }

  const formatAssetCode = (repair?: any) => {
    const items = getRepairItems(repair)
    if (!items.length) {
      if (repair?.assetCode) {
        return Number(repair?.itemCount || 0) > 1
          ? `${repair.assetCode} 等 ${repair.itemCount} 项`
          : repair.assetCode
      }
      return '-'
    }
    if (items.length === 1) return items[0].assetCode || '-'
    return `${items[0].assetCode || '-'} 等 ${items.length} 项`
  }

  const formatAssetName = (repair?: any) => {
    const items = getRepairItems(repair)
    if (!items.length) {
      if (repair?.assetName) {
        return Number(repair?.itemCount || 0) > 1
          ? `${repair.assetName} 等 ${repair.itemCount} 项`
          : repair.assetName
      }
      return '-'
    }
    if (items.length === 1) return items[0].assetName || '-'
    return `${items[0].assetName || '-'} 等 ${items.length} 项`
  }

  const router = useRouter()
  const userStore = useUserStore()
  const { isSelfScopedAssetUser } = useAssetRoleScope()

  const activeStatus = ref<'ALL' | string>('ALL')
  const dialogVisible = ref(false)
  const detailDrawerVisible = ref(false)
  const approveDialogVisible = ref(false)
  const finishDialogVisible = ref(false)
  const attachmentDrawerVisible = ref(false)
  const approveActionType = ref<'approve' | 'reject'>('approve')
  const dialogType = ref<'add' | 'edit'>('add')
  const currentRepair = ref<any>()
  const exportLoading = ref(false)

  const initialSearchState = {
    repairNo: '',
    assetCode: '',
    vendorName: '',
    resultType: ''
  }

  const formFilters = reactive({ ...initialSearchState })

  const searchBarKey = computed(() => `${activeStatus.value}-${isSelfScopedAssetUser.value}`)
  const scopeTip = computed(() =>
    isSelfScopedAssetUser.value
      ? '当前是“我的维修单”视角，只展示由你本人发起的维修申请。'
      : '维修页聚焦故障报修、审批、维修完成和资产状态联动。'
  )

  const hasPermission = (permission: string) => {
    const permissions = userStore.permissions || []
    return permissions.includes('*:*:*') || permissions.includes(permission)
  }

  const hasAnyFilter = computed(
    () =>
      Boolean(formFilters.repairNo?.trim()) ||
      Boolean(formFilters.assetCode?.trim()) ||
      Boolean(formFilters.vendorName?.trim()) ||
      Boolean(formFilters.resultType) ||
      activeStatus.value !== 'ALL'
  )

  const showEmptyState = computed(() => !loading.value && data.value.length === 0)
  const emptyDescription = computed(() => {
    if (formFilters.resultType === 'SUGGEST_DISPOSAL') {
      return '当前没有待转报废的维修单，后续可从“建议报废”的维修结果继续进入报废单。'
    }
    return hasAnyFilter.value
      ? '没有匹配的维修单，请调整筛选条件后再看看。'
      : '还没有维修单，先创建一张维修单把流程跑起来。'
  })

  const buildQuery = () => ({
    repairNo: formFilters.repairNo || undefined,
    assetCode: formFilters.assetCode || undefined,
    vendorName: formFilters.vendorName || undefined,
    resultType: formFilters.resultType || undefined,
    repairStatus: activeStatus.value === 'ALL' ? undefined : activeStatus.value
  })

  const syncSearchParams = () => {
    Object.assign(searchParams, buildQuery())
  }

  const canEditRepair = (row: any) => ['DRAFT', 'REJECTED'].includes(row?.repairStatus)
  const canSubmitRepair = (row: any) => ['DRAFT', 'REJECTED'].includes(row?.repairStatus)
  const canApproveRepair = (row: any) => ['SUBMITTED'].includes(row?.repairStatus)
  const canRejectRepair = (row: any) => ['SUBMITTED'].includes(row?.repairStatus)
  const canFinishRepair = (row: any) => ['APPROVED'].includes(row?.repairStatus)
  const canCancelRepair = (row: any) =>
    ['DRAFT', 'SUBMITTED', 'REJECTED', 'APPROVED'].includes(row?.repairStatus)
  const canDeleteRepair = (row: any) =>
    ['DRAFT', 'REJECTED', 'CANCELED'].includes(row?.repairStatus)
  const canCreateDisposalOrder = (row: any) =>
    row?.repairStatus === 'FINISHED' &&
    row?.resultType === 'SUGGEST_DISPOSAL' &&
    !row?.relatedDisposalOrder?.orderId &&
    hasPermission('asset:order:query') &&
    hasPermission('asset:order:add')

  const displayText = (value: unknown) => {
    if (value === null || value === undefined || value === '') return '-'
    return String(value)
  }

  const renderStatus = (status?: string) =>
    h(
      ElTag,
      { type: statusMap[status || '']?.type || 'info', effect: 'light' },
      () => statusMap[status || '']?.label || status || '-'
    )

  const formatResultType = (value?: string) => resultTypeMap[value || ''] || value || '-'

  const {
    columns,
    columnChecks,
    data,
    loading,
    pagination,
    searchParams,
    resetSearchParams,
    handleSizeChange,
    handleCurrentChange,
    refreshData,
    getData
  } = useTable({
    core: {
      apiFn: listAssetRepair,
      apiParams: { pageNum: 1, pageSize: 10 },
      columnsFactory: () => [
        { prop: 'repairNo', label: '维修单号', minWidth: 150 },
        {
          prop: 'assetCode',
          label: '资产编码',
          minWidth: 160,
          formatter: (row: any) => formatAssetCode(row)
        },
        {
          prop: 'assetName',
          label: '资产名称',
          minWidth: 220,
          formatter: (row: any) => formatAssetName(row)
        },
        {
          prop: 'repairStatus',
          label: '维修状态',
          width: 110,
          align: 'center',
          formatter: (row: any) => renderStatus(row.repairStatus)
        },
        { prop: 'applyDeptName', label: '发起部门', minWidth: 140 },
        { prop: 'applyUserName', label: '发起人', minWidth: 120 },
        { prop: 'reportTime', label: '报修时间', minWidth: 170, align: 'center' },
        { prop: 'vendorName', label: '供应商', minWidth: 140 },
        {
          prop: 'repairCost',
          label: '维修费用',
          width: 110,
          align: 'right',
          formatter: (row: any) => displayText(row.repairCost ?? '-')
        },
        {
          prop: 'resultType',
          label: '完成结果',
          minWidth: 120,
          formatter: (row: any) => formatResultType(row.resultType)
        },
        {
          prop: 'operation',
          label: '操作',
          width: 360,
          align: 'right',
          formatter: (row: any) => renderOperation(row)
        }
      ]
    }
  })

  const searchItems = computed(() => [
    {
      label: '维修单号',
      key: 'repairNo',
      type: 'input',
      props: { placeholder: '请输入维修单号', clearable: true }
    },
    {
      label: '资产编码',
      key: 'assetCode',
      type: 'input',
      props: { placeholder: '请输入资产编码', clearable: true }
    },
    {
      label: '供应商',
      key: 'vendorName',
      type: 'input',
      props: { placeholder: '请输入供应商', clearable: true }
    },
    {
      label: '完成结果',
      key: 'resultType',
      type: 'select',
      props: {
        placeholder: '请选择完成结果',
        clearable: true,
        options: resultTypeOptions
      }
    }
  ])

  const createDisposalBridgePayload = async (repair: any) => {
    const primaryItem = getPrimaryRepairItem(repair)
    let assetSnapshot: Record<string, any> | undefined
    if (primaryItem?.assetId) {
      try {
        const response: any = await getAssetInfo(primaryItem.assetId)
        assetSnapshot = response?.data || response || undefined
      } catch (error) {
        console.error('加载报废桥接资产快照失败，回退到维修单快照:', error)
      }
    }

    return {
      source: 'repair',
      orderType: 'DISPOSAL',
      sourceBizType: 'ASSET_REPAIR',
      sourceBizId: repair?.repairId,
      sourceBizNo: repair?.repairNo || '',
      repairId: repair?.repairId,
      repairNo: repair?.repairNo || '',
      assetId: primaryItem?.assetId || repair?.assetId,
      assetCode: primaryItem?.assetCode || repair?.assetCode || '',
      assetName: primaryItem?.assetName || repair?.assetName || '',
      assetStatus: assetSnapshot?.assetStatus || repair?.afterStatus || repair?.beforeStatus || '',
      beforeStatus: repair?.beforeStatus || assetSnapshot?.assetStatus || '',
      afterStatus: repair?.afterStatus || assetSnapshot?.assetStatus || repair?.beforeStatus || '',
      currentUserId: assetSnapshot?.currentUserId,
      useOrgDeptId: assetSnapshot?.useOrgDeptId,
      currentLocationId: assetSnapshot?.currentLocationId,
      resultType: repair?.resultType || '',
      faultDesc: repair?.faultDesc || '',
      repairRemark: repair?.remark || '',
      repairCost: repair?.repairCost,
      downtimeHours: repair?.downtimeHours,
      vendorName: repair?.vendorName || '',
      finishTime: repair?.finishTime || '',
      disposalReason:
        repair?.disposeSuggestion ||
        repair?.repairConclusion ||
        repair?.remark ||
        repair?.faultDesc ||
        ''
    }
  }

  const navigateToDisposalDraft = async (repair?: any) => {
    if (!repair?.repairId) return
    if (repair?.relatedDisposalOrder?.orderId) {
      if (hasPermission('asset:order:query')) {
        ElMessage.warning('该维修单已有关联报废单，正在打开单据详情')
        await handleViewRelatedDisposalOrder(repair.relatedDisposalOrder)
      } else {
        ElMessage.warning('该维修单已有关联报废单，请联系有单据查询权限的处理人继续查看')
      }
      return
    }

    const bridgePayload = await createDisposalBridgePayload(repair)
    sessionStorage.setItem(DISPOSAL_BRIDGE_STORAGE_KEY, JSON.stringify(bridgePayload))
    await router.push({
      path: '/asset/order',
      query: {
        bridgeSource: 'repair',
        orderType: 'DISPOSAL',
        repairId: String(repair.repairId)
      }
    })
  }

  const renderOperation = (row: any) => {
    const actionNodes = [
      canCreateDisposalOrder(row) &&
        h(
          ElButton,
          {
            link: true,
            type: 'warning',
            size: 'small',
            onClick: () => handleCreateDisposalOrder(row)
          },
          () => '建报废单'
        ),
      hasPermission('asset:repair:query') &&
        h(ArtButtonTable, { type: 'view', onClick: () => handleView(row) }),
      hasPermission('asset:repair:query') &&
        h(
          ElButton,
          { link: true, type: 'primary', size: 'small', onClick: () => handleOpenAttachments(row) },
          () => '附件'
        ),
      hasPermission('asset:repair:edit') &&
        canEditRepair(row) &&
        h(ArtButtonTable, { type: 'edit', onClick: () => handleEdit(row) }),
      hasPermission('asset:repair:remove') &&
        canDeleteRepair(row) &&
        h(ArtButtonTable, { type: 'delete', onClick: () => handleDelete(row) }),
      hasPermission('asset:repair:submit') &&
        canSubmitRepair(row) &&
        h(
          ElButton,
          { link: true, type: 'primary', size: 'small', onClick: () => handleSubmitRepair(row) },
          () => '提交'
        ),
      hasPermission('asset:repair:approve') &&
        canApproveRepair(row) &&
        h(
          ElButton,
          {
            link: true,
            type: 'success',
            size: 'small',
            onClick: () => openApproveDialog(row, 'approve')
          },
          () => '通过'
        ),
      hasPermission('asset:repair:reject') &&
        canRejectRepair(row) &&
        h(
          ElButton,
          {
            link: true,
            type: 'danger',
            size: 'small',
            onClick: () => openApproveDialog(row, 'reject')
          },
          () => '驳回'
        ),
      hasPermission('asset:repair:finish') &&
        canFinishRepair(row) &&
        h(
          ElButton,
          { link: true, type: 'warning', size: 'small', onClick: () => openFinishDialog(row) },
          () => '完成'
        ),
      hasPermission('asset:repair:cancel') &&
        canCancelRepair(row) &&
        h(
          ElButton,
          {
            link: true,
            type: 'info',
            size: 'small',
            onClick: () => handleCancelRepairAndRefresh(row)
          },
          () => '取消'
        )
    ].filter(Boolean)

    return h('div', { class: 'asset-repair-operation' }, [
      h(ElSpace, { wrap: true, size: 8 }, () => actionNodes as any)
    ])
  }

  const handleAdd = () => {
    dialogType.value = 'add'
    currentRepair.value = undefined
    dialogVisible.value = true
  }

  const handleEdit = (row?: any) => {
    if (!row?.repairId) return
    dialogType.value = 'edit'
    currentRepair.value = normalizeRepairDetail(row)
    dialogVisible.value = true
  }

  const loadRepairDetail = async (row?: any) => {
    if (!row?.repairId) {
      currentRepair.value = row ? normalizeRepairDetail(row) : undefined
      return
    }
    currentRepair.value = normalizeRepairDetail(row)
    try {
      const response: any = await getAssetRepair(row.repairId)
      const repairDetail = normalizeRepairDetail({ ...row, ...(response?.data || response || {}) })
      const relatedDisposalOrder = await findRelatedDisposalOrder(repairDetail)
      currentRepair.value = { ...repairDetail, relatedDisposalOrder }
    } catch (error) {
      console.error('加载维修单详情失败，继续使用列表行数据:', error)
    }
  }

  const findRelatedDisposalOrder = async (repair?: any) => {
    const repairId = Number(repair?.repairId)
    if (!repairId) return undefined

    try {
      const response: any = await getLinkedAssetOrder({
        orderType: 'DISPOSAL',
        sourceBizType: 'ASSET_REPAIR',
        sourceBizId: repairId
      })
      const matchedOrder = response?.data || undefined
      if (!matchedOrder) return undefined

      return {
        orderId: matchedOrder.orderId,
        orderNo: matchedOrder.orderNo,
        orderStatus: matchedOrder.orderStatus,
        sourceBizNo: matchedOrder.sourceBizNo,
        disposalAmount: matchedOrder.disposalAmount,
        disposalReason: matchedOrder.disposalReason,
        approveUserName: matchedOrder.approveUserName,
        approveTime: matchedOrder.approveTime
      }
    } catch (error) {
      console.error('查询关联报废单失败，继续展示维修详情主体:', error)
      return undefined
    }
  }

  const handleView = async (row?: any) => {
    if (!row?.repairId) return
    await loadRepairDetail(row)
    detailDrawerVisible.value = true
  }

  const handleCreateDisposalOrder = async (row?: any) => {
    if (!row?.repairId) return
    await loadRepairDetail(row)
    await navigateToDisposalDraft(currentRepair.value || row)
  }

  const handleViewRelatedDisposalOrder = async (order?: any) => {
    const orderId = Number(order?.orderId)
    if (!orderId) return

    await router.push({
      path: '/asset/order',
      query: {
        orderType: 'DISPOSAL',
        orderId: String(orderId),
        openDetail: '1'
      }
    })
  }

  const handleOpenAttachments = async (row?: any) => {
    if (!row?.repairId) return
    await loadRepairDetail(row)
    attachmentDrawerVisible.value = true
  }

  const handleDelete = async (row?: any) => {
    if (!row?.repairId) return
    try {
      await ElMessageBox.confirm(`确认删除维修单“${row.repairNo || row.repairId}”吗？`, '提示', {
        type: 'warning'
      })
      await delAssetRepair(row.repairId)
      ElMessage.success('删除成功')
      refreshData()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('删除维修单失败:', error)
      }
    }
  }

  const handleSubmitRepair = async (row?: any) => {
    if (!row?.repairId) return
    try {
      await ElMessageBox.confirm(`确认提交维修单“${row.repairNo || row.repairId}”吗？`, '提示', {
        type: 'warning'
      })
      await submitAssetRepair(row.repairId)
      ElMessage.success('提交成功')
      refreshData()
      if (detailDrawerVisible.value) {
        await loadRepairDetail(row)
      }
    } catch (error) {
      if (error !== 'cancel') {
        console.error('提交维修单失败:', error)
      }
    }
  }

  const openApproveDialog = (row?: any, actionType: 'approve' | 'reject' = 'approve') => {
    if (!row?.repairId) return
    currentRepair.value = { ...row }
    approveActionType.value = actionType
    approveDialogVisible.value = true
  }

  const handleApproveConfirm = async (payload: {
    remark: string
    actionType: 'approve' | 'reject'
  }) => {
    if (!currentRepair.value?.repairId) return
    try {
      if (payload.actionType === 'approve') {
        await approveAssetRepair(currentRepair.value.repairId, { remark: payload.remark })
        ElMessage.success('审批通过')
      } else {
        await rejectAssetRepair(currentRepair.value.repairId, { remark: payload.remark })
        ElMessage.success('审批驳回')
      }
      approveDialogVisible.value = false
      refreshData()
      if (detailDrawerVisible.value) {
        await loadRepairDetail(currentRepair.value)
      }
    } catch (error) {
      console.error('审批维修单失败:', error)
    }
  }

  const openFinishDialog = async (row?: any) => {
    if (!row?.repairId) return
    await loadRepairDetail(row)
    finishDialogVisible.value = true
  }

  const handleFinishConfirm = async (payload: any) => {
    if (!currentRepair.value?.repairId) return
    try {
      const finishPayload = {
        ...payload,
        // 兼容多资产维修完成场景，优先使用弹窗传回的 itemList。
        itemList: Array.isArray(payload?.itemList)
          ? payload.itemList
          : currentRepair.value?.itemList
      }
      await finishAssetRepair(currentRepair.value.repairId, finishPayload)
      currentRepair.value = normalizeRepairDetail({
        ...currentRepair.value,
        ...finishPayload,
        repairStatus: 'FINISHED'
      })
      finishDialogVisible.value = false
      refreshData()
      if (detailDrawerVisible.value) {
        await loadRepairDetail(currentRepair.value)
      }

      if (payload.resultType === 'SUGGEST_DISPOSAL') {
        try {
          await ElMessageBox.confirm(
            '维修结果已标记为“建议报废”。是否立即去创建报废单草稿？',
            '维修完成',
            {
              type: 'warning',
              confirmButtonText: '去创建报废单',
              cancelButtonText: '稍后处理',
              distinguishCancelAndClose: true
            }
          )
          await navigateToDisposalDraft(currentRepair.value)
        } catch (error) {
          if (error === 'cancel') {
            ElMessage.warning('已标记为建议报废，可稍后从维修详情或列表继续创建报废单')
          }
        }
        return
      }

      ElMessage.success('维修完成成功')
    } catch (error) {
      console.error('完成维修单失败:', error)
    }
  }

  const handleCancelRepair = async (row?: any) => {
    if (!row?.repairId) return
    try {
      await ElMessageBox.confirm(`确认取消维修单“${row.repairNo || row.repairId}”吗？`, '提示', {
        type: 'warning'
      })
      await cancelAssetRepair(row.repairId)
      ElMessage.success('取消成功')
      refreshData()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('取消维修单失败:', error)
      }
    }
  }

  const handleCancelRepairAndRefresh = async (row?: any) => {
    await handleCancelRepair(row)
    if (detailDrawerVisible.value && row?.repairId) {
      await loadRepairDetail(row)
    }
  }

  const handleStatusChange = () => {
    syncSearchParams()
    searchParams.pageNum = 1
    getData()
  }

  const handleSearch = () => {
    syncSearchParams()
    searchParams.pageNum = 1
    getData()
  }

  const handleReset = () => {
    Object.assign(formFilters, initialSearchState)
    activeStatus.value = 'ALL'
    resetSearchParams()
    syncSearchParams()
    searchParams.pageNum = 1
    getData()
  }

  const handleExport = async () => {
    exportLoading.value = true
    try {
      const blob = await exportAssetRepair(buildQuery())
      FileSaver.saveAs(blob as Blob, '资产维修单.xlsx')
      ElMessage.success('导出成功')
    } catch (error) {
      console.error('导出维修单失败:', error)
    } finally {
      exportLoading.value = false
    }
  }

  onMounted(() => {
    syncSearchParams()
  })
</script>

<style scoped lang="scss">
  .asset-repair-page {
    padding: 12px;
  }

  .asset-repair-toolbar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    margin-bottom: 12px;
    padding: 12px 16px;
    border-radius: 12px;
    background:
      linear-gradient(135deg, rgba(217, 119, 6, 0.08), rgba(249, 115, 22, 0.04)),
      var(--el-fill-color-lighter);
  }

  .asset-repair-toolbar__left {
    display: flex;
    align-items: center;
    gap: 12px;
    flex-wrap: wrap;
  }

  .asset-repair-toolbar__label {
    font-weight: 600;
    color: var(--el-text-color-primary);
  }

  .asset-repair-toolbar__tip {
    color: var(--el-text-color-secondary);
    font-size: 12px;
    white-space: nowrap;
  }

  .asset-repair-empty {
    padding: 32px 12px 12px;
  }

  .asset-repair-operation {
    display: flex;
    justify-content: flex-end;
  }
</style>
