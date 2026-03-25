<template>
  <div class="asset-info-page art-full-height">
    <div class="asset-info-layout">
      <aside v-if="!isMobile" class="asset-info-aside">
        <InfoFilterTree
          :mode="filterMode"
          :data="currentFilterTree"
          :current-node-key="currentFilterNodeKey"
          @switch-mode="handleSwitchMode"
          @node-click="handleFilterNodeClick"
          @update:current-node-key="handleFilterNodeKeyChange"
        />
      </aside>

      <div class="asset-info-main">
        <div v-if="isMobile" class="asset-info-mobile-filter">
          <ElButton @click="filterDrawerVisible = true">
            {{ filterMode === 'category' ? '按分类筛选' : '按位置筛选' }}
          </ElButton>
          <ElTag effect="light" type="info">
            {{ currentFilterLabel || '未选择节点' }}
          </ElTag>
        </div>

        <ElAlert
          v-if="infoScopeNotice"
          class="asset-info-scope-alert"
          type="info"
          :closable="false"
          show-icon
          :title="infoScopeNotice"
        />

        <ArtSearchBar
          :key="searchBarKey"
          v-model="formFilters"
          :items="formItems"
          :showExpand="false"
          @search="handleSearch"
          @reset="handleReset"
        />

        <ElCard class="art-table-card flex-1 overflow-hidden" shadow="never">
          <ArtTableHeader
            :showZebra="false"
            :loading="loading"
            v-model:columns="columnChecks"
            @refresh="refreshData"
          >
            <template #left>
              <ElSpace wrap>
                <ElButton v-auth="'asset:info:add'" type="primary" @click="handleAdd" v-ripple>
                  新增资产
                </ElButton>
                <ElButton type="info" plain @click="importDialogVisible = true" v-ripple>
                  导入
                </ElButton>
                <ElButton
                  v-auth="'asset:info:export'"
                  type="warning"
                  plain
                  :loading="exportLoading"
                  @click="handleExport"
                  v-ripple
                >
                  导出
                </ElButton>
                <ElButton
                  v-auth="'asset:info:remove'"
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
              <ElButton link type="primary" @click="refreshData">重新加载</ElButton>
            </template>
          </ElAlert>

          <div v-if="showEmptyState" class="asset-info-empty">
            <ElEmpty :description="emptyDescription">
              <ElButton v-if="!hasAnyFilter" type="primary" @click="handleAdd">新增资产</ElButton>
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
            rowKey="assetId"
            @pagination:size-change="handleSizeChange"
            @pagination:current-change="handleCurrentChange"
          />
        </ElCard>
      </div>
    </div>

    <InfoDialog
      v-model="dialogVisible"
      :dialog-type="dialogType"
      :asset-data="currentAsset"
      :category-options="categoryTreeOptions"
      :location-options="locationTreeOptions"
      :dept-options="deptTreeOptions"
      :user-options="userOptions"
      @success="refreshData"
    />

    <InfoImportDialog v-model="importDialogVisible" @success="refreshData" />
    <InfoEventDrawer v-model="eventDrawerVisible" :asset-data="currentAsset" />
    <AssetAttachmentDrawer
      v-model="attachmentDrawerVisible"
      biz-type="ASSET_INFO"
      :biz-id="currentAsset?.assetId"
      :biz-title="currentAsset?.assetName || currentAsset?.assetCode || '资产台账'"
      permission-prefix="asset:info"
    />

    <ElDrawer
      v-model="filterDrawerVisible"
      title="资产筛选"
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
  import { computed, h, onMounted, reactive, ref } from 'vue'
  import { useWindowSize } from '@vueuse/core'
  import FileSaver from 'file-saver'
  import { ElButton, ElMessage, ElMessageBox, ElSpace } from 'element-plus'
  import { listUser, deptTreeSelect } from '@/api/system/user'
  import { treeCategorySelect } from '@/api/asset/category'
  import { treeLocationSelect } from '@/api/asset/location'
  import { delAssetInfo, exportAssetInfo, listAssetInfo } from '@/api/asset/info'
  import { useDict } from '@/utils/dict'
  import { useTable } from '@/hooks/core/useTable'
  import { useUserStore } from '@/store/modules/user'
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import DictTag from '@/components/DictTag/index.vue'
  import { useAssetRoleScope } from '../shared/use-asset-role-scope'
  import AssetAttachmentDrawer from '../shared/asset-attachment-drawer.vue'
  import InfoDialog from './modules/info-dialog.vue'
  import InfoEventDrawer from './modules/info-event-drawer.vue'
  import InfoFilterTree from './modules/info-filter-tree.vue'
  import InfoImportDialog from './modules/info-import-dialog.vue'

  defineOptions({ name: 'AssetInfo' })

  interface TreeOption {
    id: number
    label: string
    children?: TreeOption[]
  }

  interface UserOption {
    userId: number
    userName: string
  }

  const { width } = useWindowSize()
  const isMobile = computed(() => width.value < 768)
  const userStore = useUserStore()
  const { isSelfScopedAssetUser } = useAssetRoleScope()
  const { asset_status, asset_source } = useDict('asset_status', 'asset_source')

  const filterMode = ref<'category' | 'location'>('category')
  const selectedCategoryId = ref<number | string | null>(null)
  const selectedLocationId = ref<number | string | null>(null)
  const filterDrawerVisible = ref(false)
  const dialogVisible = ref(false)
  const importDialogVisible = ref(false)
  const eventDrawerVisible = ref(false)
  const attachmentDrawerVisible = ref(false)
  const dialogType = ref<'add' | 'edit'>('add')
  const currentAsset = ref<any>()
  const selection = ref<any[]>([])
  const exportLoading = ref(false)
  const tableError = ref('')

  const categoryTreeOptions = ref<TreeOption[]>([])
  const locationTreeOptions = ref<TreeOption[]>([])
  const deptTreeOptions = ref<TreeOption[]>([])
  const userOptions = ref<UserOption[]>([])

  const categoryLabelMap = ref<Record<string, string>>({})
  const locationLabelMap = ref<Record<string, string>>({})
  const deptLabelMap = ref<Record<string, string>>({})
  const userLabelMap = ref<Record<string, string>>({})

  const initialSearchState = {
    assetCode: '',
    assetName: '',
    assetStatus: '',
    currentUserId: undefined as number | undefined
  }

  const formFilters = reactive({ ...initialSearchState })

  const currentFilterNodeKey = computed(() =>
    filterMode.value === 'category' ? selectedCategoryId.value : selectedLocationId.value
  )

  const currentFilterTree = computed(() =>
    filterMode.value === 'category' ? categoryTreeOptions.value : locationTreeOptions.value
  )

  const currentFilterLabel = computed(() => {
    const currentId = currentFilterNodeKey.value
    if (currentId === null || currentId === undefined) {
      return ''
    }
    return filterMode.value === 'category'
      ? categoryLabelMap.value[String(currentId)] || ''
      : locationLabelMap.value[String(currentId)] || ''
  })

  const searchBarKey = computed(
    () =>
      `${asset_status.value.length}-${asset_source.value.length}-${userOptions.value.length}-${filterMode.value}-${isSelfScopedAssetUser.value}`
  )

  const multiple = computed(() => selection.value.length === 0)

  const hasAnyFilter = computed(
    () =>
      Boolean(formFilters.assetCode?.trim()) ||
      Boolean(formFilters.assetName?.trim()) ||
      Boolean(formFilters.assetStatus) ||
      (!isSelfScopedAssetUser.value && Boolean(formFilters.currentUserId)) ||
      Boolean(selectedCategoryId.value) ||
      Boolean(selectedLocationId.value)
  )

  /**
   * 统一判断页面内按钮权限，保证主按钮和行内按钮口径一致。
   */
  const hasPermission = (permission: string) => {
    const permissions = userStore.permissions || []
    return permissions.includes('*:*:*') || permissions.includes(permission)
  }

  const infoScopeNotice = computed(() =>
    isSelfScopedAssetUser.value ? '当前为“我的资产”视角，系统仅展示你本人可见的资产数据。' : ''
  )

  const formItems = computed(() => {
    const items = [
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
        label: '资产状态',
        key: 'assetStatus',
        type: 'select',
        props: {
          placeholder: '请选择资产状态',
          clearable: true,
          options: asset_status.value
        }
      }
    ]

    if (!isSelfScopedAssetUser.value) {
      items.push({
        label: '责任人',
        key: 'currentUserId',
        type: 'select',
        props: {
          placeholder: '请选择责任人',
          clearable: true,
          options: userOptions.value.map((item) => ({
            label: item.userName,
            value: item.userId
          }))
        }
      })
    }

    return items
  })

  /**
   * 扁平化树节点，既用于列表映射，也用于筛选节点回显。
   */
  const flattenTreeLabels = (nodes: TreeOption[], map: Record<string, string>) => {
    nodes.forEach((node) => {
      map[String(node.id)] = node.label
      if (node.children?.length) {
        flattenTreeLabels(node.children, map)
      }
    })
  }

  /**
   * 统一应用左侧树筛选条件，确保分类和位置不会同时生效。
   */
  const applyTreeFilterToSearch = () => {
    if (filterMode.value === 'category') {
      searchParams.categoryId = selectedCategoryId.value || undefined
      searchParams.currentLocationId = undefined
      return
    }
    searchParams.categoryId = undefined
    searchParams.currentLocationId = selectedLocationId.value || undefined
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
      apiFn: listAssetInfo,
      apiParams: {
        pageNum: 1,
        pageSize: 10
      },
      columnsFactory: () => [
        { type: 'selection', width: 55, align: 'center' },
        { prop: 'assetCode', label: '资产编码', minWidth: 130 },
        { prop: 'assetName', label: '资产名称', minWidth: 180 },
        {
          prop: 'assetStatus',
          label: '资产状态',
          width: 110,
          align: 'center',
          formatter: (row: any) =>
            h(DictTag, { options: asset_status.value, value: row.assetStatus })
        },
        {
          prop: 'assetSource',
          label: '来源',
          width: 100,
          align: 'center',
          formatter: (row: any) =>
            h(DictTag, { options: asset_source.value, value: row.assetSource })
        },
        {
          prop: 'categoryId',
          label: '分类',
          minWidth: 120,
          formatter: (row: any) =>
            categoryLabelMap.value[String(row.categoryId)] || row.categoryId || '-'
        },
        {
          prop: 'manageDeptId',
          label: '管理部门',
          minWidth: 120,
          formatter: (row: any) =>
            deptLabelMap.value[String(row.manageDeptId)] || row.manageDeptId || '-'
        },
        {
          prop: 'currentUserId',
          label: '责任人',
          minWidth: 120,
          formatter: (row: any) =>
            userLabelMap.value[String(row.currentUserId)] || row.currentUserId || '-'
        },
        {
          prop: 'currentLocationId',
          label: '当前位置',
          minWidth: 140,
          formatter: (row: any) =>
            locationLabelMap.value[String(row.currentLocationId)] || row.currentLocationId || '-'
        },
        {
          prop: 'originalValue',
          label: '原值',
          width: 110,
          align: 'right',
          formatter: (row: any) =>
            row.originalValue !== null && row.originalValue !== undefined ? row.originalValue : '-'
        },
        {
          prop: 'operation',
          label: '操作',
          width: 170,
          align: 'right',
          formatter: (row: any) => {
            const actionNodes = []

            if (hasPermission('asset:info:edit')) {
              actionNodes.push(
                h(ArtButtonTable, {
                  type: 'edit',
                  onClick: () => handleEdit(row)
                })
              )
            }

            actionNodes.push(
              h(ArtButtonTable, {
                type: 'view',
                onClick: () => handleOpenEventDrawer(row)
              })
            )

            actionNodes.push(
              h(
                ElButton,
                {
                  link: true,
                  type: 'primary',
                  size: 'small',
                  onClick: () => handleOpenAttachments(row)
                },
                () => '附件'
              )
            )

            if (hasPermission('asset:info:remove')) {
              actionNodes.push(
                h(ArtButtonTable, {
                  type: 'delete',
                  onClick: () => handleDelete(row)
                })
              )
            }

            return h('div', { class: 'flex justify-end' }, actionNodes)
          }
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
      return '当前筛选条件下暂无资产，请调整左侧节点或搜索条件。'
    }
    return '还没有资产台账。你可以先新增一条资产，或者后续接通导入接口后批量导入。'
  })

  /**
   * 预加载资产页所需的分类、位置、组织与人员映射。
   */
  const loadPageOptions = async () => {
    try {
      const [categoryRes, locationRes, deptRes, userRes] = await Promise.all([
        treeCategorySelect(),
        treeLocationSelect(),
        deptTreeSelect(),
        listUser({ pageNum: 1, pageSize: 200 })
      ])

      const categoryResponse = categoryRes as any
      const locationResponse = locationRes as any
      const deptResponse = deptRes as any
      const userResponse = userRes as any

      categoryTreeOptions.value = Array.isArray(categoryResponse)
        ? categoryResponse
        : categoryResponse?.data || []
      locationTreeOptions.value = Array.isArray(locationResponse)
        ? locationResponse
        : locationResponse?.data || []
      deptTreeOptions.value = Array.isArray(deptResponse) ? deptResponse : deptResponse?.data || []

      const userData = Array.isArray(userResponse)
        ? userResponse
        : userResponse?.rows || userResponse?.data || []
      userOptions.value = userData.map((item: any) => ({
        userId: item.userId,
        userName: item.nickName ? `${item.nickName} (${item.userName})` : item.userName
      }))

      const categoryMap: Record<string, string> = {}
      const locationMap: Record<string, string> = {}
      const deptMap: Record<string, string> = {}

      flattenTreeLabels(categoryTreeOptions.value, categoryMap)
      flattenTreeLabels(locationTreeOptions.value, locationMap)
      flattenTreeLabels(deptTreeOptions.value, deptMap)

      categoryLabelMap.value = categoryMap
      locationLabelMap.value = locationMap
      deptLabelMap.value = deptMap
      userLabelMap.value = userOptions.value.reduce((result: Record<string, string>, item) => {
        result[String(item.userId)] = item.userName
        return result
      }, {})
    } catch (error) {
      console.error('加载资产台账关联数据失败:', error)
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

  const handleFilterNodeKeyChange = (value: number | string | null) => {
    if (filterMode.value === 'category') {
      selectedCategoryId.value = value
      return
    }
    selectedLocationId.value = value
  }

  /**
   * 左侧节点点击后立即刷新工作区，保持“树筛选 + 表格”联动。
   */
  const handleFilterNodeClick = () => {
    applyTreeFilterToSearch()
    getData()
  }

  const handleMobileFilterNodeClick = () => {
    handleFilterNodeClick()
    filterDrawerVisible.value = false
  }

  /**
   * 切换左侧维度时保留各自已选节点，但只让当前模式生效。
   */
  const handleSwitchMode = (mode: 'category' | 'location') => {
    filterMode.value = mode
    applyTreeFilterToSearch()
    getData()
  }

  const handleSearch = () => {
    Object.assign(searchParams, {
      assetCode: formFilters.assetCode || undefined,
      assetName: formFilters.assetName || undefined,
      assetStatus: formFilters.assetStatus || undefined,
      currentUserId: isSelfScopedAssetUser.value
        ? undefined
        : formFilters.currentUserId || undefined,
      pageNum: 1
    })
    applyTreeFilterToSearch()
    getData()
  }

  /**
   * 重置顶部搜索栏，但保留左侧当前维度和节点选择。
   */
  const handleReset = () => {
    Object.assign(formFilters, initialSearchState)
    Object.assign(searchParams, {
      assetCode: undefined,
      assetName: undefined,
      assetStatus: undefined,
      currentUserId: undefined,
      pageNum: 1
    })
    applyTreeFilterToSearch()
    getData()
  }

  const handleAdd = () => {
    dialogType.value = 'add'
    currentAsset.value = undefined
    dialogVisible.value = true
  }

  const handleEdit = (row: any) => {
    dialogType.value = 'edit'
    currentAsset.value = { ...row }
    dialogVisible.value = true
  }

  const handleOpenEventDrawer = (row: any) => {
    currentAsset.value = { ...row }
    eventDrawerVisible.value = true
  }

  const handleOpenAttachments = (row: any) => {
    currentAsset.value = { ...row }
    attachmentDrawerVisible.value = true
  }

  /**
   * 支持删除单条或多条资产记录。
   */
  const handleDelete = async (row?: any) => {
    const assetIds = row?.assetId || selection.value.map((item) => item.assetId).join(',')
    if (!assetIds) return

    try {
      await ElMessageBox.confirm(`是否确认删除资产“${row?.assetName || assetIds}”？`, '提示', {
        type: 'warning'
      })
      await delAssetInfo(assetIds)
      ElMessage.success('删除成功')
      refreshData()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('删除资产失败:', error)
      }
    }
  }

  /**
   * 当前仅接通导出接口，文件名先按一期固定名称输出。
   */
  const handleExport = async () => {
    exportLoading.value = true
    try {
      const blob = await exportAssetInfo({
        assetCode: formFilters.assetCode || undefined,
        assetName: formFilters.assetName || undefined,
        assetStatus: formFilters.assetStatus || undefined,
        currentUserId: isSelfScopedAssetUser.value
          ? undefined
          : formFilters.currentUserId || undefined,
        categoryId:
          filterMode.value === 'category' ? selectedCategoryId.value || undefined : undefined,
        currentLocationId:
          filterMode.value === 'location' ? selectedLocationId.value || undefined : undefined
      })
      FileSaver.saveAs(blob as Blob, '资产台账.xlsx')
      ElMessage.success('导出成功')
    } catch (error) {
      console.error('导出资产台账失败:', error)
    } finally {
      exportLoading.value = false
    }
  }

  onMounted(async () => {
    await loadPageOptions()
    applyTreeFilterToSearch()
    await getData()
    void asset_status.value
    void asset_source.value
  })
</script>

<style lang="scss" scoped>
  .asset-info-page {
    padding: 12px;
  }

  .asset-info-layout {
    height: 100%;
    display: flex;
    gap: 12px;
    overflow: hidden;
  }

  .asset-info-aside {
    width: 280px;
    flex-shrink: 0;
    padding: 16px;
    overflow: hidden;
    border: 1px solid var(--art-gray-200);
    border-radius: 12px;
    background: var(--default-box-color);
  }

  .asset-info-main {
    min-width: 0;
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 12px;
    overflow: hidden;
  }

  .asset-info-empty {
    min-height: 420px;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .asset-info-mobile-filter {
    display: none;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
  }

  .asset-info-scope-alert {
    margin-bottom: 12px;
  }

  @media (max-width: 767px) {
    .asset-info-page {
      padding: 8px;
    }

    .asset-info-layout {
      display: block;
    }

    .asset-info-mobile-filter {
      display: flex;
      margin-bottom: 12px;
    }
  }
</style>
