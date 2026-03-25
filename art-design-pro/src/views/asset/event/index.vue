<template>
  <div class="asset-event-page art-full-height">
    <ArtSearchBar
      :key="searchBarKey"
      v-model="formFilters"
      :items="searchItems"
      :showExpand="false"
      @search="handleSearch"
      @reset="handleReset"
    />

    <ElAlert
      v-if="lockedAssetId"
      class="mb-3"
      type="info"
      :closable="false"
      show-icon
      :title="`当前已锁定资产范围：${lockedAssetLabel}`"
    >
      <template #default>
        <ElButton link type="primary" @click="handleClearLockedAsset">清除锁定</ElButton>
      </template>
    </ElAlert>

    <ElCard class="art-table-card" shadow="never">
      <div class="asset-event-toolbar">
        <div class="asset-event-toolbar__title"
          >资产流水是只读审计页，重点查看“什么时候、谁、因为什么动作改变了资产”。</div
        >
        <ElButton
          v-auth="'asset:event:export'"
          type="warning"
          plain
          :loading="exportLoading"
          @click="handleExport"
          v-ripple
        >
          导出
        </ElButton>
      </div>

      <ArtTableHeader
        :showZebra="false"
        :loading="loading"
        v-model:columns="columnChecks"
        @refresh="handleRefresh"
      />

      <ElAlert
        v-if="tableError"
        class="mb-3"
        type="error"
        :closable="false"
        show-icon
        :title="tableError"
      >
        <template #default>
          <ElButton link type="primary" @click="refreshData">重新加载</ElButton>
        </template>
      </ElAlert>

      <div v-if="showEmptyState" class="asset-event-empty">
        <ElEmpty :description="emptyDescription">
          <ElButton v-if="hasAnyFilter" @click="handleReset">重置筛选</ElButton>
        </ElEmpty>
      </div>

      <ArtTable
        v-else
        rowKey="eventId"
        :loading="loading"
        :columns="columns"
        :data="data"
        :pagination="pagination"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      />
    </ElCard>

    <AssetEventDrawer v-model="drawerVisible" :asset-data="currentAsset" />
  </div>
</template>

<script setup lang="ts">
  import { computed, h, onMounted, reactive, ref } from 'vue'
  import { useRoute, useRouter } from 'vue-router'
  import FileSaver from 'file-saver'
  import { ElButton, ElMessage } from 'element-plus'
  import { exportAssetEvent, listAssetEvent } from '@/api/asset/event'
  import DictTag from '@/components/DictTag/index.vue'
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import { useTable } from '@/hooks/core/useTable'
  import { useDict } from '@/utils/dict'
  import AssetEventDrawer from '../shared/asset-event-drawer.vue'

  defineOptions({ name: 'AssetEvent' })

  const route = useRoute()
  const router = useRouter()
  const { asset_event_type, asset_order_type } = useDict('asset_event_type', 'asset_order_type')

  const initialSearchState = {
    assetCode: '',
    assetName: '',
    eventType: '',
    sourceOrderNo: '',
    sourceOrderType: '',
    operatorUserName: '',
    operateTimeRange: [] as string[]
  }

  const formFilters = reactive({ ...initialSearchState })
  const drawerVisible = ref(false)
  const currentAsset = ref<any>()
  const exportLoading = ref(false)
  const tableError = ref('')
  const lockedAssetId = ref<number>()
  const lockedAssetCode = ref('')
  const lockedAssetName = ref('')

  const sourceTypeOptions = computed(() => [
    ...(asset_order_type.value || []),
    { label: '盘点任务', value: 'INVENTORY_TASK' }
  ])

  const searchBarKey = computed(
    () =>
      `${asset_event_type.value.length}-${sourceTypeOptions.value.length}-${lockedAssetId.value || ''}`
  )

  const lockedAssetLabel = computed(() => {
    return (
      [lockedAssetCode.value, lockedAssetName.value].filter(Boolean).join(' / ') ||
      `资产ID ${lockedAssetId.value}`
    )
  })

  const hasAnyFilter = computed(
    () =>
      Boolean(formFilters.assetCode?.trim()) ||
      Boolean(formFilters.assetName?.trim()) ||
      Boolean(formFilters.eventType) ||
      Boolean(formFilters.sourceOrderNo?.trim()) ||
      Boolean(formFilters.sourceOrderType) ||
      Boolean(formFilters.operatorUserName?.trim()) ||
      Boolean(formFilters.operateTimeRange?.length) ||
      Boolean(lockedAssetId.value)
  )

  const showEmptyState = computed(
    () => !loading.value && !tableError.value && data.value.length === 0
  )

  const emptyDescription = computed(() => {
    if (hasAnyFilter.value) {
      return '当前筛选条件下没有匹配的资产流水，请调整时间范围、资产信息或事件类型。'
    }
    return '还没有资产流水记录。先完成单据落账或盘点差异处理后，这里会自动出现审计链路。'
  })

  const formatSourceType = (sourceOrderType?: string) => {
    if (!sourceOrderType) {
      return '-'
    }
    if (sourceOrderType === 'INVENTORY_TASK') {
      return '盘点任务'
    }
    return (
      sourceTypeOptions.value.find((item) => String(item.value) === String(sourceOrderType))
        ?.label || sourceOrderType
    )
  }

  const buildQuery = () => {
    const [beginOperateTime, endOperateTime] = formFilters.operateTimeRange || []
    return {
      assetId: lockedAssetId.value || undefined,
      assetCode: formFilters.assetCode || undefined,
      assetName: formFilters.assetName || undefined,
      eventType: formFilters.eventType || undefined,
      sourceOrderNo: formFilters.sourceOrderNo || undefined,
      sourceOrderType: formFilters.sourceOrderType || undefined,
      operatorUserName: formFilters.operatorUserName || undefined,
      beginOperateTime: beginOperateTime || undefined,
      endOperateTime: endOperateTime || undefined
    }
  }

  const applyLockedAssetFromRoute = () => {
    const assetId = Number(route.query.assetId)
    lockedAssetId.value = Number.isFinite(assetId) && assetId > 0 ? assetId : undefined
    lockedAssetCode.value = typeof route.query.assetCode === 'string' ? route.query.assetCode : ''
    lockedAssetName.value = typeof route.query.assetName === 'string' ? route.query.assetName : ''

    if (lockedAssetCode.value) {
      formFilters.assetCode = lockedAssetCode.value
    }
    if (lockedAssetName.value) {
      formFilters.assetName = lockedAssetName.value
    }
  }

  const syncSearchParams = () => {
    Object.assign(searchParams, buildQuery())
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
      apiFn: listAssetEvent,
      apiParams: {
        pageNum: 1,
        pageSize: 10
      },
      columnsFactory: () => [
        { prop: 'assetCode', label: '资产编码', minWidth: 140 },
        { prop: 'assetName', label: '资产名称', minWidth: 180 },
        {
          prop: 'eventType',
          label: '事件类型',
          width: 110,
          align: 'center',
          formatter: (row: any) =>
            h(DictTag, { options: asset_event_type.value, value: row.eventType })
        },
        {
          prop: 'sourceSummary',
          label: '来源单据',
          minWidth: 180,
          formatter: (row: any) => {
            const text = [formatSourceType(row.sourceOrderType), row.sourceOrderNo]
              .filter(Boolean)
              .join(' / ')
            return text || '-'
          }
        },
        { prop: 'eventDesc', label: '事件说明', minWidth: 280 },
        {
          prop: 'operatorUserName',
          label: '操作人',
          minWidth: 120,
          formatter: (row: any) => row.operatorUserName || '系统'
        },
        { prop: 'operateTime', label: '操作时间', width: 170, align: 'center' },
        {
          prop: 'operation',
          label: '操作',
          width: 110,
          align: 'right',
          formatter: (row: any) =>
            h(ArtButtonTable, {
              type: 'view',
              onClick: () => handleOpenAssetTimeline(row)
            })
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

  const searchItems = computed(() => [
    {
      label: '资产编码',
      key: 'assetCode',
      type: 'input',
      props: {
        placeholder: '请输入资产编码',
        clearable: true
      }
    },
    {
      label: '资产名称',
      key: 'assetName',
      type: 'input',
      props: {
        placeholder: '请输入资产名称',
        clearable: true
      }
    },
    {
      label: '事件类型',
      key: 'eventType',
      type: 'select',
      props: {
        placeholder: '请选择事件类型',
        clearable: true,
        options: asset_event_type.value
      }
    },
    {
      label: '来源单据',
      key: 'sourceOrderNo',
      type: 'input',
      props: {
        placeholder: '请输入来源单据编号',
        clearable: true
      }
    },
    {
      label: '来源业务',
      key: 'sourceOrderType',
      type: 'select',
      props: {
        placeholder: '请选择来源业务',
        clearable: true,
        options: sourceTypeOptions.value
      }
    },
    {
      label: '操作人',
      key: 'operatorUserName',
      type: 'input',
      props: {
        placeholder: '请输入操作人',
        clearable: true
      }
    },
    {
      label: '操作时间',
      key: 'operateTimeRange',
      type: 'daterange',
      props: {
        startPlaceholder: '开始日期',
        endPlaceholder: '结束日期',
        rangeSeparator: '至',
        valueFormat: 'YYYY-MM-DD'
      }
    }
  ])

  /**
   * 独立流水页按资产维度继续下钻时，统一复用同一套只读抽屉。
   */
  const handleOpenAssetTimeline = (row: any) => {
    currentAsset.value = {
      assetId: row.assetId,
      assetCode: row.assetCode,
      assetName: row.assetName,
      assetStatus: row.assetStatus
    }
    drawerVisible.value = true
  }

  const handleSearch = () => {
    syncSearchParams()
    searchParams.pageNum = 1
    getData()
  }

  const handleReset = async () => {
    Object.assign(formFilters, initialSearchState)
    lockedAssetId.value = undefined
    lockedAssetCode.value = ''
    lockedAssetName.value = ''
    resetSearchParams()
    syncSearchParams()
    searchParams.pageNum = 1
    if (route.query.assetId) {
      await router.replace({ path: '/asset/event' })
    }
    getData()
  }

  const handleClearLockedAsset = async () => {
    await handleReset()
  }

  const handleRefresh = () => {
    refreshData()
  }

  const handleExport = async () => {
    exportLoading.value = true
    try {
      const blob = await exportAssetEvent(buildQuery())
      FileSaver.saveAs(blob as Blob, '资产流水.xlsx')
      ElMessage.success('导出成功')
    } catch (error) {
      console.error('导出资产流水失败:', error)
    } finally {
      exportLoading.value = false
    }
  }

  onMounted(() => {
    applyLockedAssetFromRoute()
    syncSearchParams()
    void asset_event_type.value
    void asset_order_type.value
  })
</script>

<style lang="scss" scoped>
  .asset-event-page {
    padding: 12px;
  }

  .asset-event-toolbar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    margin-bottom: 12px;
    padding: 12px 16px;
    border-radius: 12px;
    background:
      linear-gradient(135deg, rgba(14, 116, 144, 0.08), rgba(6, 182, 212, 0.04)),
      var(--el-fill-color-lighter);
  }

  .asset-event-toolbar__title {
    color: var(--el-text-color-primary);
    line-height: 1.7;
  }

  .asset-event-empty {
    padding: 40px 12px 12px;
  }
</style>
