<template>
  <div class="real-estate-page art-full-height">
    <div class="real-estate-layout">
      <!-- 1. 侧边栏：过滤树 -->
      <aside v-if="!isMobile" class="real-estate-aside">
        <InfoFilterTree
          :mode="filterMode"
          :data="currentFilterTree"
          :current-node-key="currentFilterNodeKey"
          @switch-mode="handleSwitchMode"
          @node-click="handleFilterNodeClick"
          @update:current-node-key="handleFilterNodeKeyChange"
        />
      </aside>

      <!-- 2. 列表主区域 -->
      <div class="real-estate-main">
        <div v-if="isMobile" class="real-estate-mobile-filter">
          <ElButton @click="filterDrawerVisible = true">
            {{ filterMode === 'category' ? '按分类筛选' : '按位置筛选' }}
          </ElButton>
          <ElTag effect="light" type="info">
            {{ currentFilterLabel || '未选择节点' }}
          </ElTag>
        </div>

        <ArtSearchBar
          v-model="searchParams"
          :items="formItems"
          :showExpand="false"
          @search="handleSearch"
          @reset="handleReset"
        />

        <ElCard class="art-table-card flex-1 overflow-hidden" shadow="never">
          <ArtTableHeader :loading="loading" v-model:columns="columnChecks" @refresh="refreshData">
            <template #left>
              <ElSpace wrap>
                <ElButton
                  v-auth="'asset:real-estate:add'"
                  type="primary"
                  @click="handleAdd"
                  v-ripple
                >
                  新增不动产
                </ElButton>
                <ElButton
                  v-auth="'asset:real-estate:import'"
                  type="info"
                  plain
                  @click="handleImport"
                  v-ripple
                >
                  导入
                </ElButton>
                <ElButton
                  v-auth="'asset:real-estate:export'"
                  type="warning"
                  plain
                  :loading="exportLoading"
                  @click="handleExport"
                  v-ripple
                >
                  导出
                </ElButton>
                <ElButton
                  v-auth="'asset:real-estate:remove'"
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

          <ArtTable
            v-model:selection="selection"
            :loading="loading"
            :data="data"
            :columns="columns"
            :pagination="pagination"
            rowKey="assetId"
            @pagination:size-change="handleSizeChange"
            @pagination:current-change="handleCurrentChange"
          />
        </ElCard>
      </div>
    </div>

    <InfoImportDialog
      v-model="importDialogVisible"
      assetType="REAL_ESTATE"
      @success="refreshData"
    />
    <!-- 附件抽屉 -->
    <AssetAttachmentDrawer
      v-model="attachmentDrawerVisible"
      biz-type="ASSET_INFO"
      :biz-id="currentAsset?.assetId"
      :biz-title="currentAsset?.assetName || '不动产详情'"
      permission-prefix="asset:real-estate"
    />

    <!-- 移动端筛选抽屉 -->
    <ElDrawer
      v-model="filterDrawerVisible"
      title="不动产筛选"
      size="85%"
      append-to-body
      destroy-on-close
    >
      <InfoFilterTree
        :mode="filterMode"
        :data="currentFilterTree"
        :current-node-key="currentFilterNodeKey"
        @switch-mode="handleSwitchMode"
        @node-click="handleMobileFilterNodeClick"
        @update:current-node-key="handleFilterNodeKeyChange"
      />
    </ElDrawer>
  </div>
</template>

<script setup lang="ts">
  import { computed, h, onMounted, ref } from 'vue'
  import { useWindowSize } from '@vueuse/core'
  import { ElButton, ElDrawer, ElMessage, ElMessageBox, ElSpace, ElTag } from 'element-plus'
  import { useRouter } from 'vue-router'
  import { treeCategorySelect } from '@/api/asset/category'
  import { treeLocationSelect } from '@/api/asset/location'
  import { listAssetInfo, delAssetInfo, exportAssetInfo } from '@/api/asset/info'
  import { useDict } from '@/utils/dict'
  import { useTable } from '@/hooks/core/useTable'
  import { useUserStore } from '@/store/modules/user'
  import DictTag from '@/components/DictTag/index.vue'
  import AssetAttachmentDrawer from '../shared/asset-attachment-drawer.vue'
  import AssetRowActionBar from '../shared/asset-row-action-bar.vue'
  import type { AssetRowActionItem } from '../shared/asset-row-action-bar.vue'
  import InfoFilterTree from '../info/modules/info-filter-tree.vue'
  import InfoImportDialog from '../info/modules/info-import-dialog.vue'

  defineOptions({ name: 'RealEstateIndex' })

  const router = useRouter()
  const userStore = useUserStore()
  const { real_estate_status } = useDict('real_estate_status')

  const hasPermission = (permission: string) => {
    const permissions = userStore.permissions || []
    return permissions.includes('*:*:*') || permissions.includes(permission)
  }

  // 1. 列定义
  const rawColumns = computed(() => [
    { type: 'selection' as const, width: 50 },
    { label: '不动产编码', prop: 'assetCode', width: 140 },
    { label: '名称', prop: 'assetName', minWidth: 150, showOverflowTooltip: true },
    {
      label: '状态',
      prop: 'assetStatus',
      width: 100,
      render: (row: any) => {
        return h(DictTag, {
          options: real_estate_status.value || [],
          value: row.assetStatus
        })
      }
    },
    { label: '详细地址', prop: 'addrDetail', minWidth: 200, showOverflowTooltip: true },
    {
      label: '地理坐标',
      prop: 'coordinate',
      width: 150,
      render: (row: any) => {
        const { longitude, latitude } = row
        return longitude != null && latitude != null ? `${longitude}, ${latitude}` : '-'
      }
    },
    {
      label: '操作',
      prop: 'actions',
      width: 180,
      fixed: 'right' as const,
      render: (row: any) => {
        const actions: AssetRowActionItem[] = []
        if (hasPermission('asset:real-estate:edit')) {
          actions.push({
            key: 'edit',
            label: '编辑',
            onClick: () => {
              void handleEdit(row)
            }
          })
        }
        actions.push({
          key: 'attach',
          label: '附件',
          onClick: () => {
            handleAttachment(row)
          }
        })
        if (hasPermission('asset:real-estate:remove')) {
          actions.push({
            key: 'del',
            label: '删除',
            type: 'danger',
            onClick: () => {
              handleDelete(row)
            }
          })
        }
        return h(AssetRowActionBar, { actions })
      }
    }
  ])

  // 2. 搜索项
  const formItems = computed(() => [
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
      label: '状态',
      key: 'assetStatus',
      type: 'select',
      props: {
        placeholder: '请选择状态',
        clearable: true,
        options: real_estate_status.value || []
      }
    }
  ])

  // 3. useTable
  const {
    loading,
    data,
    pagination,
    columns,
    handleSizeChange,
    handleCurrentChange,
    getData: handleSearch,
    resetSearchParams: handleReset,
    refreshData,
    searchParams,
    columnChecks
  } = useTable({
    core: {
      apiFn: listAssetInfo,
      apiParams: { assetType: 'REAL_ESTATE' },
      columnsFactory: () => rawColumns.value
    }
  })

  const selection = ref<any[]>([])
  const multiple = ref(true)
  const exportLoading = ref(false)
  const importDialogVisible = ref(false)
  const attachmentDrawerVisible = ref(false)
  const currentAsset = ref<any>(null)

  /** 导入按钮操作 */
  function handleImport() {
    importDialogVisible.value = true
  }

  // 4. 树形过滤逻辑
  const { width } = useWindowSize()
  const isMobile = computed(() => width.value < 768)
  const filterMode = ref<'category' | 'location'>('location')
  const filterDrawerVisible = ref(false)
  const currentFilterNodeKey = ref<number | string | null>(null)
  const currentFilterLabel = ref('')
  const categoryTreeOptions = ref<any[]>([])
  const locationTreeOptions = ref<any[]>([])

  const currentFilterTree = computed(() => {
    return filterMode.value === 'category' ? categoryTreeOptions.value : locationTreeOptions.value
  })

  const getTreeData = async () => {
    const [catRes, locRes]: any = await Promise.all([treeCategorySelect(), treeLocationSelect()])
    categoryTreeOptions.value = catRes.data || []
    locationTreeOptions.value = locRes.data || []
  }

  const handleSwitchMode = (mode: 'category' | 'location') => {
    filterMode.value = mode
    currentFilterNodeKey.value = null
    currentFilterLabel.value = ''
    handleReset()
  }

  const handleFilterNodeClick = (node: any) => {
    currentFilterLabel.value = node.label
    if (filterMode.value === 'category') {
      searchParams.categoryId = node.id
      searchParams.currentLocationId = undefined
    } else {
      searchParams.currentLocationId = node.id
      searchParams.categoryId = undefined
    }
    handleSearch()
  }

  const handleMobileFilterNodeClick = (node: any) => {
    handleFilterNodeClick(node)
    filterDrawerVisible.value = false
  }

  const handleFilterNodeKeyChange = (key: number | string | null) => {
    currentFilterNodeKey.value = key
  }

  onMounted(() => {
    getTreeData()
  })

  const handleAdd = () => router.push('/asset/real-estate/create')
  const handleEdit = (row: any) => router.push(`/asset/real-estate/create?assetId=${row.assetId}`)
  const handleAttachment = (row: any) => {
    currentAsset.value = row
    attachmentDrawerVisible.value = true
  }

  const handleDelete = (row?: any) => {
    const ids = row ? [row.assetId] : selection.value.map((i: any) => i.assetId)
    ElMessageBox.confirm('是否确认删除选中的不动产台账？', '警告', { type: 'warning' }).then(async () => {
      await delAssetInfo(ids.join(','))
      ElMessage.success('删除成功')
      refreshData()
    })
  }

  const handleExport = async () => {
    exportLoading.value = true
    try {
      await exportAssetInfo(searchParams)
    } finally {
      exportLoading.value = false
    }
  }
</script>

<style scoped>
  .real-estate-page {
    display: flex;
    flex-direction: column;
    background: var(--art-bg-color);
  }
  .real-estate-layout {
    display: flex;
    flex: 1;
    overflow: hidden;
  }
  .real-estate-aside {
    width: 240px;
    height: 100%;
    padding: 16px;
    background: var(--default-box-color);
    border-right: 1px solid var(--art-border-color);
    flex-shrink: 0;
  }
  .real-estate-main {
    flex: 1;
    display: flex;
    flex-direction: column;
    padding: 16px;
    gap: 16px;
    overflow: hidden;
  }
  .real-estate-mobile-filter {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 2px 0;
  }
</style>
