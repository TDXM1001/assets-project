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
  </div>
</template>

<script setup lang="ts">
  import { computed, h, onMounted, reactive, ref } from 'vue'
  import { useRoute, useRouter } from 'vue-router'
  import FileSaver from 'file-saver'
  import { ElButton, ElMessage, ElSpace, ElTag } from 'element-plus'
  import { useTable } from '@/hooks/core/useTable'
  import { useUserStore } from '@/store/modules/user'
  import { useAssetRoleScope } from '../shared/use-asset-role-scope'
  import { exportAssetRepair, listAssetRepair } from '@/api/asset/repair'
  import AssetRowActionBar, { type AssetRowActionItem } from '../shared/asset-row-action-bar.vue'
  import {
    buildRepairListRestoreQuery,
    resolveRepairListRestoreState
  } from './modules/repair-list-query'
  import { formatRepairAssetCode, formatRepairAssetName } from './modules/repair-item-normalize'

  defineOptions({ name: 'AssetRepair' })

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

  const route = useRoute()
  const router = useRouter()
  const userStore = useUserStore()
  const { isSelfScopedAssetUser } = useAssetRoleScope()

  const activeStatus = ref<'ALL' | string>('ALL')
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
      : '维修列表页只负责检索与入口跳转，审批与完工在独立页面处理。'
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
  const emptyDescription = computed(() =>
    hasAnyFilter.value
      ? '没有匹配的维修单，请调整筛选条件后再查看。'
      : '还没有维修单，先创建一张维修单把流程跑起来。'
  )

  const buildQuery = () => ({
    repairNo: formFilters.repairNo || undefined,
    assetCode: formFilters.assetCode || undefined,
    vendorName: formFilters.vendorName || undefined,
    resultType: formFilters.resultType || undefined,
    repairStatus: activeStatus.value === 'ALL' ? undefined : activeStatus.value
  })

  const buildListRestoreQuery = () => buildRepairListRestoreQuery(buildQuery())

  const hydrateListStateFromRoute = () => {
    const restoredState = resolveRepairListRestoreState(route.query)
    activeStatus.value = restoredState.repairStatus
    formFilters.repairNo = restoredState.repairNo
    formFilters.assetCode = restoredState.assetCode
    formFilters.vendorName = restoredState.vendorName
    formFilters.resultType = restoredState.resultType

    return Boolean(
      restoredState.repairStatus !== 'ALL' ||
        restoredState.repairNo ||
        restoredState.assetCode ||
        restoredState.vendorName ||
        restoredState.resultType
    )
  }

  const syncSearchParams = () => {
    Object.assign(searchParams, buildQuery())
  }

  const canEditRepair = (row: any) => ['DRAFT', 'REJECTED'].includes(row?.repairStatus)

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
          formatter: (row: any) => formatRepairAssetCode(row, { strictListMode: true })
        },
        {
          prop: 'assetName',
          label: '资产名称',
          minWidth: 220,
          formatter: (row: any) => formatRepairAssetName(row, { strictListMode: true })
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
        { prop: 'vendorName', label: '维修厂商', minWidth: 140 },
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
          width: 220,
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
        options: Object.entries(resultTypeMap).map(([value, label]) => ({ value, label }))
      }
    }
  ])

  const handleAdd = () => {
    router.push({
      path: '/asset/repair/create',
      query: {
        ...buildListRestoreQuery(),
        sourcePage: 'repair-list',
        bridgeSource: 'repair-list'
      }
    })
  }

  const handleEdit = (row?: any) => {
    if (!row?.repairId) return
    router.push({
      path: `/asset/repair/edit/${row.repairId}`,
      query: {
        ...buildListRestoreQuery(),
        sourcePage: 'repair-list',
        bridgeSource: 'repair-list'
      }
    })
  }

  const handleView = (row?: any) => {
    if (!row?.repairId) return
    router.push({
      path: `/asset/repair/detail/${row.repairId}`,
      query: {
        ...buildListRestoreQuery(),
        sourcePage: 'repair-list',
        bridgeSource: 'repair-list'
      }
    })
  }

  const renderOperation = (row: any) => {
    const actions: AssetRowActionItem[] = []

    // 列表页只保留路由型入口动作，主流程操作统一在独立页面完成。
    if (hasPermission('asset:repair:query')) {
      actions.push({
        key: 'view',
        label: '详情',
        type: 'primary',
        icon: 'ri:eye-line',
        onClick: () => handleView(row)
      })
    }

    if (hasPermission('asset:repair:edit') && canEditRepair(row)) {
      actions.push({
        key: 'edit',
        label: '编辑',
        type: 'primary',
        icon: 'ri:edit-line',
        onClick: () => handleEdit(row)
      })
    }

    return h(AssetRowActionBar, { actions })
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
    const hasRestoredListState = hydrateListStateFromRoute()
    syncSearchParams()
    if (hasRestoredListState) {
      searchParams.pageNum = 1
      getData()
    }
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
</style>
