<template>
  <div class="asset-order-page art-full-height">
    <ArtSearchBar
      :key="searchBarKey"
      v-model="formFilters"
      :items="searchItems"
      :showExpand="false"
      @search="handleSearch"
      @reset="handleReset"
    />

    <ElCard class="art-table-card" shadow="never">
      <div class="asset-order-toolbar">
        <div class="asset-order-toolbar__left">
          <div class="asset-order-toolbar__label">单据类型</div>
          <ElRadioGroup v-model="activeOrderType" size="small" @change="handleOrderTypeChange">
            <ElRadioButton label="ALL">全部</ElRadioButton>
            <ElRadioButton v-for="item in orderTypeOptions" :key="item.value" :label="item.value">
              {{ item.label }}
            </ElRadioButton>
          </ElRadioGroup>
        </div>
        <div class="asset-order-toolbar__tip">
          {{ orderScopeTip }}
        </div>
      </div>

      <ArtTableHeader
        :showZebra="false"
        :loading="loading"
        v-model:columns="columnChecks"
        @refresh="handleRefresh"
      >
        <template #left>
          <ElSpace wrap>
            <ElButton v-auth="'asset:order:add'" type="primary" @click="handleAdd" v-ripple>
              新增单据
            </ElButton>
            <ElButton
              v-auth="'asset:order:export'"
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

      <div v-if="showEmptyState" class="asset-order-empty">
        <ElEmpty :description="emptyDescription">
          <ElButton v-if="!hasAnyFilter" type="primary" @click="handleAdd">新增单据</ElButton>
          <ElButton v-else @click="handleReset">重置筛选</ElButton>
        </ElEmpty>
      </div>

      <ArtTable
        v-else
        rowKey="orderId"
        :loading="loading"
        :columns="columns"
        :data="data"
        :pagination="pagination"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      />
    </ElCard>

    <OrderDialog
      v-model="orderDialogVisible"
      :dialog-type="dialogType"
      :order-data="currentOrder"
      :dialog-context="dialogContext"
      @success="refreshData"
    />

    <OrderDetailDrawer
      v-model="detailDrawerVisible"
      :order-data="currentOrder"
      @edit="handleEdit(currentOrder)"
      @submit="handleSubmitOrder(currentOrder)"
      @approve="openApprovalDialog(currentOrder, 'approve')"
      @reject="openApprovalDialog(currentOrder, 'reject')"
      @finish="handleFinishOrderAndRefresh(currentOrder)"
      @cancel="handleCancelOrderAndRefresh(currentOrder)"
      @attachments="handleOpenAttachments(currentOrder)"
    />

    <OrderApproveDialog
      v-model="approveDialogVisible"
      :action-type="approveActionType"
      :order-data="currentOrder"
      @confirm="handleApproveConfirm"
    />

    <AssetAttachmentDrawer
      v-model="attachmentDrawerVisible"
      biz-type="ASSET_ORDER"
      :biz-id="currentOrder?.orderId"
      :biz-title="currentOrder?.orderNo || '业务单据'"
      permission-prefix="asset:order"
    />
  </div>
</template>

<script setup lang="ts">
  import { computed, h, onMounted, reactive, ref } from 'vue'
  import FileSaver from 'file-saver'
  import { ElButton, ElMessage, ElMessageBox, ElSpace } from 'element-plus'
  import { useRoute, useRouter } from 'vue-router'
  import { useTable } from '@/hooks/core/useTable'
  import { useDict } from '@/utils/dict'
  import { useUserStore } from '@/store/modules/user'
  import {
    approveAssetOrder,
    cancelAssetOrder,
    delAssetOrder,
    exportAssetOrder,
    finishAssetOrder,
    getAssetOrder,
    listAssetOrder,
    rejectAssetOrder,
    submitAssetOrder
  } from '@/api/asset/order'
  import DictTag from '@/components/DictTag/index.vue'
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import { useAssetRoleScope } from '../shared/use-asset-role-scope'
  import AssetAttachmentDrawer from '../shared/asset-attachment-drawer.vue'
  import OrderApproveDialog from './modules/order-approve-dialog.vue'
  import OrderDetailDrawer from './modules/order-detail-drawer.vue'
  import OrderDialog from './modules/order-dialog.vue'

  defineOptions({ name: 'AssetOrder' })

  const { asset_order_type, asset_order_status } = useDict('asset_order_type', 'asset_order_status')
  const route = useRoute()
  const router = useRouter()
  const userStore = useUserStore()
  const { isSelfScopedAssetUser } = useAssetRoleScope()

  const activeOrderType = ref<'ALL' | string>('ALL')
  const orderDialogVisible = ref(false)
  const detailDrawerVisible = ref(false)
  const approveDialogVisible = ref(false)
  const approveActionType = ref<'approve' | 'reject'>('approve')
  const dialogType = ref<'add' | 'edit'>('add')
  const currentOrder = ref<any>()
  const dialogContext = ref<Record<string, any>>({})
  const attachmentDrawerVisible = ref(false)
  const exportLoading = ref(false)

  const initialSearchState = {
    orderNo: '',
    orderStatus: '',
    applyUserName: '',
    applyDeptName: '',
    bizDateRange: [] as string[]
  }

  const formFilters = reactive({ ...initialSearchState })

  const orderTypeOptions = computed(() => asset_order_type.value || [])
  const searchBarKey = computed(
    () =>
      `${asset_order_type.value.length}-${asset_order_status.value.length}-${activeOrderType.value}-${isSelfScopedAssetUser.value}`
  )

  const orderScopeTip = computed(() =>
    isSelfScopedAssetUser.value
      ? '当前为“我的单据”视角，只展示由你本人发起的资产单据。'
      : activeOrderType.value === 'DISPOSAL'
        ? '当前聚焦报废单，完成后会把资产落到已报废。'
        : '这里不是普通表格页，而是单据流转工作台，先用类型切换把流程边界收住。'
  )

  const hasPermission = (permission: string) => {
    const permissions = userStore.permissions || []
    return permissions.includes('*:*:*') || permissions.includes(permission)
  }

  const hasAnyFilter = computed(
    () =>
      Boolean(formFilters.orderNo?.trim()) ||
      Boolean(formFilters.orderStatus) ||
      (!isSelfScopedAssetUser.value && Boolean(formFilters.applyUserName?.trim())) ||
      (!isSelfScopedAssetUser.value && Boolean(formFilters.applyDeptName?.trim())) ||
      Boolean(formFilters.bizDateRange?.length) ||
      activeOrderType.value !== 'ALL'
  )

  const showEmptyState = computed(() => !loading.value && data.value.length === 0)

  const emptyDescription = computed(() => {
    if (hasAnyFilter.value) {
      return '没有匹配的业务单据，先调整筛选条件再看看。'
    }
    if (activeOrderType.value === 'DISPOSAL') {
      return '还没有报废单，先创建一张报废单，把处置流程跑起来。'
    }
    return '还没有业务单据，先新增一张单据，把流程跑起来。'
  })

  const buildQuery = () => {
    const [bizDateStart, bizDateEnd] = formFilters.bizDateRange || []

    return {
      orderNo: formFilters.orderNo || undefined,
      orderStatus: formFilters.orderStatus || undefined,
      applyUserName: isSelfScopedAssetUser.value
        ? undefined
        : formFilters.applyUserName || undefined,
      applyDeptName: isSelfScopedAssetUser.value
        ? undefined
        : formFilters.applyDeptName || undefined,
      bizDateStart: bizDateStart || undefined,
      bizDateEnd: bizDateEnd || undefined,
      orderType: activeOrderType.value === 'ALL' ? undefined : activeOrderType.value
    }
  }

  const syncSearchParams = () => {
    Object.assign(searchParams, buildQuery())
  }

  /**
   * 单据状态决定能不能改、能不能提交流程，这里统一收口，避免按钮规则分散在模板里。
   */
  const canEditOrder = (row: any) => ['DRAFT', 'REJECTED'].includes(row?.orderStatus)
  const canSubmitOrder = (row: any) => ['DRAFT', 'REJECTED'].includes(row?.orderStatus)
  const canApproveOrder = (row: any) => ['SUBMITTED', 'APPROVING'].includes(row?.orderStatus)
  const canRejectOrder = (row: any) => ['SUBMITTED', 'APPROVING'].includes(row?.orderStatus)
  const canFinishOrder = (row: any) => ['APPROVED'].includes(row?.orderStatus)
  const canCancelOrder = (row: any) =>
    ['DRAFT', 'SUBMITTED', 'APPROVING', 'APPROVED'].includes(row?.orderStatus)
  const canDeleteOrder = (row: any) => ['DRAFT', 'REJECTED'].includes(row?.orderStatus)

  /**
   * 把单据类型翻译成业务摘要，列表页就能一眼看出单据大概在做什么。
   */
  const formatScopeSummary = (row: any) => {
    const fromDept = row?.fromDeptName || row?.fromDeptId || '来源部门待补充'
    const toDept = row?.toDeptName || row?.toDeptId || '目标部门待补充'
    const fromUser = row?.fromUserName || row?.fromUserId || '来源责任人待补充'
    const toUser = row?.toUserName || row?.toUserId || '目标责任人待补充'
    const fromLocation = row?.fromLocationName || row?.fromLocationId || '来源位置待补充'
    const toLocation = row?.toLocationName || row?.toLocationId || '目标位置待补充'

    switch (row?.orderType) {
      case 'INBOUND':
        return `入库至 ${toDept} / ${toLocation}`
      case 'ASSIGN':
        return `${fromDept} / ${fromUser} 领用到 ${toDept} / ${toUser}`
      case 'BORROW':
        return `借用给 ${toUser}，预计归还 ${row?.expectedReturnDate || '待补充'}`
      case 'RETURN':
        return `归还到 ${toDept} / ${toLocation}`
      case 'TRANSFER':
        return `${fromDept} / ${fromLocation} 调拨到 ${toDept} / ${toLocation}`
      case 'DISPOSAL':
        return `报废原因：${row?.disposalReason || '待补充'} / 处置金额：${displayText(
          row?.disposalAmount ?? '待补充'
        )}`
      default:
        return `${fromDept} -> ${toDept}`
    }
  }

  const formatApproveSummary = (row: any) => {
    const approveUser = row?.approveUserName || row?.approveUserId
    const approveTime = row?.approveTime
    if (!approveUser && !approveTime) return '-'
    return [approveUser, approveTime].filter(Boolean).join(' · ')
  }

  const displayText = (value: unknown) => {
    if (value === null || value === undefined || value === '') return '-'
    return String(value)
  }

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
      apiFn: listAssetOrder,
      apiParams: {
        pageNum: 1,
        pageSize: 10
      },
      columnsFactory: () => [
        { prop: 'orderNo', label: '单据编号', minWidth: 160 },
        {
          prop: 'orderType',
          label: '单据类型',
          width: 110,
          align: 'center',
          formatter: (row: any) =>
            h(DictTag, { options: asset_order_type.value, value: row.orderType })
        },
        {
          prop: 'orderStatus',
          label: '单据状态',
          width: 110,
          align: 'center',
          formatter: (row: any) =>
            h(DictTag, { options: asset_order_status.value, value: row.orderStatus })
        },
        { prop: 'applyDeptName', label: '发起部门', minWidth: 140 },
        { prop: 'applyUserName', label: '发起人', minWidth: 120 },
        { prop: 'bizDate', label: '业务时间', width: 170, align: 'center' },
        {
          prop: 'scopeSummary',
          label: '流程摘要',
          minWidth: 260,
          formatter: (row: any) => displayText(formatScopeSummary(row))
        },
        {
          prop: 'approveSummary',
          label: '审批信息',
          minWidth: 220,
          formatter: (row: any) => displayText(formatApproveSummary(row))
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

  const searchItems = computed(() => {
    const items = [
      {
        label: '单据编号',
        key: 'orderNo',
        type: 'input',
        props: {
          placeholder: '请输入单据编号',
          clearable: true
        }
      },
      {
        label: '单据状态',
        key: 'orderStatus',
        type: 'select',
        props: {
          placeholder: '请选择单据状态',
          clearable: true,
          options: asset_order_status.value
        }
      },
      {
        label: '业务时间',
        key: 'bizDateRange',
        type: 'daterange',
        props: {
          startPlaceholder: '开始日期',
          endPlaceholder: '结束日期',
          rangeSeparator: '至',
          valueFormat: 'YYYY-MM-DD'
        }
      }
    ]

    if (!isSelfScopedAssetUser.value) {
      items.splice(1, 0, {
        label: '发起部门',
        key: 'applyDeptName',
        type: 'input',
        props: {
          placeholder: '请输入发起部门',
          clearable: true
        }
      })
      items.splice(2, 0, {
        label: '发起人',
        key: 'applyUserName',
        type: 'input',
        props: {
          placeholder: '请输入发起人',
          clearable: true
        }
      })
    }

    return items
  })

  /**
   * 这里把单据行内动作拆出来，避免模板里塞满 if/else。
   */
  const renderOperation = (row: any) => {
    const actionNodes = [
      hasPermission('asset:order:query') &&
        h(ArtButtonTable, {
          type: 'view',
          onClick: () => handleView(row)
        }),
      hasPermission('asset:order:query') &&
        h(
          ElButton,
          {
            link: true,
            type: 'primary',
            size: 'small',
            onClick: () => handleOpenAttachments(row)
          },
          () => '附件'
        ),
      hasPermission('asset:order:edit') &&
        canEditOrder(row) &&
        h(ArtButtonTable, {
          type: 'edit',
          onClick: () => handleEdit(row)
        }),
      hasPermission('asset:order:remove') &&
        canDeleteOrder(row) &&
        h(ArtButtonTable, {
          type: 'delete',
          onClick: () => handleDelete(row)
        }),
      hasPermission('asset:order:submit') &&
        canSubmitOrder(row) &&
        h(
          ElButton,
          {
            link: true,
            type: 'primary',
            size: 'small',
            onClick: () => handleSubmitOrder(row)
          },
          () => '提交'
        ),
      hasPermission('asset:order:approve') &&
        canApproveOrder(row) &&
        h(
          ElButton,
          {
            link: true,
            type: 'success',
            size: 'small',
            onClick: () => openApprovalDialog(row, 'approve')
          },
          () => '通过'
        ),
      hasPermission('asset:order:reject') &&
        canRejectOrder(row) &&
        h(
          ElButton,
          {
            link: true,
            type: 'danger',
            size: 'small',
            onClick: () => openApprovalDialog(row, 'reject')
          },
          () => '驳回'
        ),
      hasPermission('asset:order:finish') &&
        canFinishOrder(row) &&
        h(
          ElButton,
          {
            link: true,
            type: 'warning',
            size: 'small',
            onClick: () => handleFinishOrderAndRefresh(row)
          },
          () => '完成'
        ),
      hasPermission('asset:order:cancel') &&
        canCancelOrder(row) &&
        h(
          ElButton,
          {
            link: true,
            type: 'info',
            size: 'small',
            onClick: () => handleCancelOrderAndRefresh(row)
          },
          () => '取消'
        )
    ].filter(Boolean)

    return h('div', { class: 'asset-order-operation' }, [
      h(ElSpace, { wrap: true, size: 8 }, () => actionNodes as any)
    ])
  }

  const handleAdd = () => {
    const query: Record<string, string> = {}
    if (activeOrderType.value !== 'ALL') {
      query.orderType = activeOrderType.value
    }
    // 新增单据已经升级为独立页面，这里直接路由跳转，避免再把复杂表单塞回弹窗。
    router.push({ path: '/asset/order/create', query }).catch(() => undefined)
  }

  const safeParseBridgeContext = (value: string | null) => {
    if (!value) return undefined
    try {
      const parsed = JSON.parse(value)
      return parsed && typeof parsed === 'object' ? parsed : undefined
    } catch (error) {
      console.error('解析报废桥接上下文失败:', error)
      return undefined
    }
  }

  const readBridgeContextFromStorage = (bridgeKey?: string) => {
    const storageKeys = bridgeKey
      ? [
          `asset-order-disposal-bridge:${bridgeKey}`,
          `asset.order.disposal.bridge:${bridgeKey}`,
          'asset-order-disposal-bridge',
          'asset.order.disposal.bridge',
          'asset.order.bridge.payload'
        ]
      : ['asset-order-disposal-bridge', 'asset.order.disposal.bridge', 'asset.order.bridge.payload']

    for (const storageKey of storageKeys) {
      const storedValue = sessionStorage.getItem(storageKey)
      if (!storedValue) continue
      sessionStorage.removeItem(storageKey)
      const parsed = safeParseBridgeContext(storedValue)
      if (parsed) return parsed
    }

    return undefined
  }

  const normalizeBridgeContext = (context: Record<string, any> = {}) => {
    const orderType = String(context.orderType || '').toUpperCase()
    return {
      ...context,
      orderType: orderType === 'DISPOSAL' ? 'DISPOSAL' : context.orderType || 'DISPOSAL'
    }
  }

  const stripBridgeQuery = () => {
    const nextQuery = { ...route.query }
    delete nextQuery.bridgeSource
    delete nextQuery.bridgeKey
    delete nextQuery.bridgeData
    delete nextQuery.repairId
    delete nextQuery.autoOpen
    router.replace({ query: nextQuery }).catch(() => undefined)
  }

  const stripDetailQuery = () => {
    const nextQuery = { ...route.query }
    delete nextQuery.orderId
    delete nextQuery.openDetail
    router.replace({ query: nextQuery }).catch(() => undefined)
  }

  const hydrateBridgeContext = () => {
    const queryBridgeSource =
      typeof route.query.bridgeSource === 'string' ? route.query.bridgeSource : ''
    const queryBridgeKey = typeof route.query.bridgeKey === 'string' ? route.query.bridgeKey : ''
    const queryBridgeData = typeof route.query.bridgeData === 'string' ? route.query.bridgeData : ''
    const queryOrderType = typeof route.query.orderType === 'string' ? route.query.orderType : ''
    const queryAutoOpen = typeof route.query.autoOpen === 'string' ? route.query.autoOpen : ''
    const normalizedQueryOrderType = queryOrderType.toUpperCase()
    const isRepairBridgeEntry =
      queryBridgeSource === 'repair' && normalizedQueryOrderType === 'DISPOSAL'

    const parsedBridgeContext =
      safeParseBridgeContext(queryBridgeData) || readBridgeContextFromStorage(queryBridgeKey)

    if (normalizedQueryOrderType && normalizedQueryOrderType !== 'ALL') {
      activeOrderType.value = normalizedQueryOrderType
      syncSearchParams()
      getData()
    }

    if (!parsedBridgeContext) {
      if (isRepairBridgeEntry) {
        ElMessage.warning('未找到维修桥接上下文，请从维修单重新发起报废单')
        stripBridgeQuery()
        return
      }
      if (normalizedQueryOrderType === 'DISPOSAL' && queryAutoOpen === '1') {
        router
          .replace({
            path: '/asset/order/create',
            query: { orderType: 'DISPOSAL', autoOpen: '1' }
          })
          .catch(() => undefined)
      }
      return
    }

    const nextDialogContext = normalizeBridgeContext(parsedBridgeContext)
    if (nextDialogContext.orderType && nextDialogContext.orderType !== 'INBOUND') {
      activeOrderType.value = String(nextDialogContext.orderType).toUpperCase()
      syncSearchParams()
      getData()
    }

    router
      .replace({
        path: '/asset/order/create',
        query: {
          orderType: String(nextDialogContext.orderType || 'INBOUND'),
          bridgeSource: queryBridgeSource || '',
          bridgeKey: queryBridgeKey || '',
          bridgeData: JSON.stringify(nextDialogContext)
        }
      })
      .catch(() => undefined)
  }

  const hydrateRouteOrderDetail = async () => {
    const queryOrderId = Number(route.query.orderId)
    const shouldOpenDetail =
      typeof route.query.openDetail === 'string' && route.query.openDetail === '1'
    if (!queryOrderId || !shouldOpenDetail) return

    try {
      const response: any = await getAssetOrder(queryOrderId)
      const orderDetail = response?.data || response || undefined
      if (!orderDetail?.orderId) return

      const queryOrderType = typeof route.query.orderType === 'string' ? route.query.orderType : ''
      const normalizedQueryOrderType = queryOrderType.toUpperCase()
      const normalizedDetailOrderType = String(orderDetail.orderType || '').toUpperCase()
      const nextActiveOrderType = normalizedQueryOrderType || normalizedDetailOrderType
      if (nextActiveOrderType && nextActiveOrderType !== 'ALL') {
        activeOrderType.value = nextActiveOrderType
        syncSearchParams()
        getData()
      }

      currentOrder.value = orderDetail
      detailDrawerVisible.value = true
      stripDetailQuery()
    } catch (error) {
      console.error('根据路由打开业务单据详情失败:', error)
    }
  }

  const handleEdit = (row?: any) => {
    if (!row?.orderId) return
    dialogType.value = 'edit'
    currentOrder.value = { ...row }
    dialogContext.value = {}
    orderDialogVisible.value = true
  }

  const loadOrderDetail = async (row?: any) => {
    if (!row?.orderId) {
      currentOrder.value = row ? { ...row } : undefined
      return
    }

    currentOrder.value = { ...row }
    try {
      const response: any = await getAssetOrder(row.orderId)
      currentOrder.value = {
        ...row,
        ...(response?.data || response || {})
      }
    } catch (error) {
      console.error('加载单据详情失败，继续使用列表行数据:', error)
    }
  }

  const handleView = async (row?: any) => {
    if (!row?.orderId) return
    await loadOrderDetail(row)
    detailDrawerVisible.value = true
  }

  const handleOpenAttachments = async (row?: any) => {
    if (!row?.orderId) return
    await loadOrderDetail(row)
    attachmentDrawerVisible.value = true
  }

  const handleDelete = async (row?: any) => {
    if (!row?.orderId) return

    try {
      await ElMessageBox.confirm(`确认删除单据【${row.orderNo || row.orderId}】吗？`, '提示', {
        type: 'warning'
      })
      await delAssetOrder(row.orderId)
      ElMessage.success('删除成功')
      refreshData()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('删除业务单据失败:', error)
      }
    }
  }

  const handleSubmitOrder = async (row?: any) => {
    if (!row?.orderId) return

    try {
      await ElMessageBox.confirm(`确认提交单据【${row.orderNo || row.orderId}】吗？`, '提示', {
        type: 'warning'
      })
      await submitAssetOrder(row.orderId)
      ElMessage.success('提交成功')
      refreshData()
      if (detailDrawerVisible.value) {
        await loadOrderDetail(row)
      }
    } catch (error) {
      if (error !== 'cancel') {
        console.error('提交业务单据失败:', error)
      }
    }
  }

  const openApprovalDialog = async (row?: any, actionType: 'approve' | 'reject' = 'approve') => {
    if (!row?.orderId) return
    currentOrder.value = { ...row }
    approveActionType.value = actionType
    approveDialogVisible.value = true
  }

  const handleApproveConfirm = async (payload: {
    remark: string
    actionType: 'approve' | 'reject'
  }) => {
    if (!currentOrder.value?.orderId) return

    try {
      if (payload.actionType === 'approve') {
        await approveAssetOrder(currentOrder.value.orderId, { remark: payload.remark })
        ElMessage.success('审批通过')
      } else {
        await rejectAssetOrder(currentOrder.value.orderId, { remark: payload.remark })
        ElMessage.success('审批驳回')
      }
      approveDialogVisible.value = false
      refreshData()
      if (detailDrawerVisible.value) {
        await loadOrderDetail(currentOrder.value)
      }
    } catch (error) {
      console.error('审批单据失败:', error)
    }
  }

  const handleFinishOrder = async (row?: any) => {
    if (!row?.orderId) return

    try {
      const confirmMessage =
        row?.orderType === 'DISPOSAL'
          ? `确认执行报废单【${row.orderNo || row.orderId}】吗？执行后资产状态将落到已报废，后续不再允许继续流转。`
          : `确认完成单据【${row.orderNo || row.orderId}】吗？`
      await ElMessageBox.confirm(confirmMessage, '提示', {
        type: 'warning'
      })
      await finishAssetOrder(row.orderId)
      ElMessage.success(row?.orderType === 'DISPOSAL' ? '报废执行成功' : '完成成功')
      refreshData()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('完成业务单据失败:', error)
      }
    }
  }

  const handleCancelOrder = async (row?: any) => {
    if (!row?.orderId) return

    try {
      await ElMessageBox.confirm(`确认取消单据【${row.orderNo || row.orderId}】吗？`, '提示', {
        type: 'warning'
      })
      await cancelAssetOrder(row.orderId)
      ElMessage.success('取消成功')
      refreshData()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('取消业务单据失败:', error)
      }
    }
  }

  const handleFinishOrderAndRefresh = async (row?: any) => {
    await handleFinishOrder(row)
    if (detailDrawerVisible.value && row?.orderId) {
      await loadOrderDetail(row)
    }
  }

  const handleCancelOrderAndRefresh = async (row?: any) => {
    await handleCancelOrder(row)
    if (detailDrawerVisible.value && row?.orderId) {
      await loadOrderDetail(row)
    }
  }

  const handleOrderTypeChange = () => {
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
    activeOrderType.value = 'ALL'
    resetSearchParams()
    syncSearchParams()
    searchParams.pageNum = 1
    getData()
  }

  const handleRefresh = () => {
    refreshData()
  }

  const handleExport = async () => {
    exportLoading.value = true
    try {
      const blob = await exportAssetOrder(buildQuery())
      FileSaver.saveAs(blob as Blob, '业务单据.xlsx')
      ElMessage.success('导出成功')
    } catch (error) {
      console.error('导出业务单据失败:', error)
    } finally {
      exportLoading.value = false
    }
  }

  onMounted(() => {
    syncSearchParams()
    void asset_order_type.value
    void asset_order_status.value
    hydrateBridgeContext()
    void hydrateRouteOrderDetail()
  })
</script>

<style lang="scss" scoped>
  .asset-order-page {
    padding: 12px;
  }

  .asset-order-toolbar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    margin-bottom: 12px;
    padding: 12px 16px;
    border-radius: 12px;
    background:
      linear-gradient(135deg, rgba(29, 78, 216, 0.08), rgba(59, 130, 246, 0.04)),
      var(--el-fill-color-lighter);
  }

  .asset-order-toolbar__left {
    display: flex;
    align-items: center;
    gap: 12px;
    flex-wrap: wrap;
  }

  .asset-order-toolbar__label {
    font-weight: 600;
    color: var(--el-text-color-primary);
  }

  .asset-order-toolbar__tip {
    color: var(--el-text-color-secondary);
    font-size: 12px;
    white-space: nowrap;
  }

  .asset-order-empty {
    padding: 32px 12px 12px;
  }

  .asset-order-operation {
    display: flex;
    justify-content: flex-end;
  }
</style>
