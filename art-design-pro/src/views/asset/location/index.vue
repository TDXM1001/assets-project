<template>
  <div class="asset-location-page art-full-height">
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
          <ElButton v-auth="'asset:location:add'" type="primary" @click="handleAddRoot" v-ripple>
            新增顶级位置
          </ElButton>
          <ElButton @click="toggleExpand" v-ripple>
            {{ isExpanded ? '收起全部' : '展开全部' }}
          </ElButton>
        </template>
      </ArtTableHeader>

      <ElAlert
        v-if="errorMessage"
        class="mb-3"
        type="error"
        :closable="false"
        show-icon
        :title="errorMessage"
      >
        <template #default>
          <ElButton link type="primary" @click="handleRefresh">重新加载</ElButton>
        </template>
      </ElAlert>

      <div v-if="showEmptyState" class="asset-location-empty">
        <ElEmpty :description="emptyDescription">
          <ElButton
            v-if="!hasActiveFilter"
            v-auth="'asset:location:add'"
            type="primary"
            @click="handleAddRoot"
          >
            新增顶级位置
          </ElButton>
          <ElButton v-else @click="handleReset">重置筛选</ElButton>
        </ElEmpty>
      </div>

      <ArtTable
        v-else
        ref="tableRef"
        rowKey="locationId"
        :loading="loading"
        :columns="columns"
        :data="locationList"
        :stripe="false"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        :default-expand-all="isExpanded"
      />
    </ElCard>

    <LocationDialog
      v-model="dialogVisible"
      :dialog-type="dialogType"
      :location-data="currentData"
      @success="getList"
    />
  </div>
</template>

<script setup lang="ts">
  import { computed, h, nextTick, onMounted, reactive, ref } from 'vue'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import { listUser, deptTreeSelect } from '@/api/system/user'
  import { listLocation, delLocation } from '@/api/asset/location'
  import { handleTree } from '@/utils/ruoyi'
  import { useDict } from '@/utils/dict'
  import { useTableColumns } from '@/hooks/core/useTableColumns'
  import { useUserStore } from '@/store/modules/user'
  import DictTag from '@/components/DictTag/index.vue'
  import AssetRowActionBar, { type AssetRowActionItem } from '../shared/asset-row-action-bar.vue'
  import LocationDialog from './modules/location-dialog.vue'

  defineOptions({ name: 'AssetLocation' })

  const { sys_normal_disable, asset_location_type } = useDict(
    'sys_normal_disable',
    'asset_location_type'
  )
  const userStore = useUserStore()

  const loading = ref(false)
  const isExpanded = ref(true)
  const tableRef = ref()
  const locationList = ref<any[]>([])
  const errorMessage = ref('')
  const dialogVisible = ref(false)
  const dialogType = ref<'add' | 'edit'>('add')
  const currentData = ref<any>()
  const deptLabelMap = ref<Record<string, string>>({})
  const userLabelMap = ref<Record<string, string>>({})

  const initialSearchState = {
    locationName: '',
    locationType: '',
    status: ''
  }

  const formFilters = reactive({ ...initialSearchState })

  const searchBarKey = computed(
    () => `${sys_normal_disable.value.length}-${asset_location_type.value.length}`
  )

  const hasActiveFilter = computed(
    () =>
      Boolean(formFilters.locationName?.trim()) ||
      Boolean(formFilters.locationType) ||
      Boolean(formFilters.status)
  )

  const showEmptyState = computed(
    () => !loading.value && !errorMessage.value && locationList.value.length === 0
  )

  const emptyDescription = computed(() => {
    if (hasActiveFilter.value) {
      return '没有匹配的位置，请调整筛选条件后重试。'
    }
    return '还没有存放位置，先补齐楼栋、楼层、房间或仓库结构。'
  })

  const formItems = computed(() => [
    {
      label: '位置名称',
      key: 'locationName',
      type: 'input',
      props: {
        placeholder: '请输入位置名称',
        clearable: true
      }
    },
    {
      label: '位置类型',
      key: 'locationType',
      type: 'select',
      props: {
        placeholder: '请选择位置类型',
        clearable: true,
        options: asset_location_type.value
      }
    },
    {
      label: '位置状态',
      key: 'status',
      type: 'select',
      props: {
        placeholder: '请选择位置状态',
        clearable: true,
        options: sys_normal_disable.value
      }
    }
  ])

  /**
   * 统一判断页面内按钮权限，保证主按钮和行内按钮口径一致。
   */
  const hasPermission = (permission: string) => {
    const permissions = userStore.permissions || []
    return permissions.includes('*:*:*') || permissions.includes(permission)
  }

  /**
   * 位置页的行内动作只保留新增、编辑、删除三项，减少树表操作区噪音。
   */
  const renderOperation = (row: any) => {
    const actions: AssetRowActionItem[] = []

    if (hasPermission('asset:location:add')) {
      actions.push({
        key: 'add-child',
        label: '新增子级',
        type: 'primary',
        icon: 'ri:add-line',
        onClick: () => handleAddChild(row)
      })
    }

    if (hasPermission('asset:location:edit')) {
      actions.push({
        key: 'edit',
        label: '编辑',
        type: 'primary',
        icon: 'ri:edit-line',
        onClick: () => handleEdit(row)
      })
    }

    if (hasPermission('asset:location:remove')) {
      actions.push({
        key: 'delete',
        label: '删除',
        type: 'danger',
        icon: 'ri:delete-bin-line',
        color: '#f56c6c',
        onClick: () => handleDelete(row)
      })
    }

    return h(AssetRowActionBar, { actions })
  }

  /**
   * 把部门树拍平成映射表，便于列表渲染人能看懂的名称。
   */
  const flattenTreeLabels = (nodes: any[], map: Record<string, string>) => {
    nodes.forEach((node) => {
      map[String(node.id)] = node.label
      if (node.children?.length) {
        flattenTreeLabels(node.children, map)
      }
    })
  }

  /**
   * 预加载位置页需要的部门和管理员映射，减少表格里出现纯 ID。
   */
  const loadRelationOptions = async () => {
    try {
      const [deptRes, userRes] = await Promise.all([
        deptTreeSelect(),
        listUser({ pageNum: 1, pageSize: 200 })
      ])

      const deptResponse = deptRes as any
      const userResponse = userRes as any
      const deptData = Array.isArray(deptResponse) ? deptResponse : deptResponse?.data || []
      const userData = Array.isArray(userResponse)
        ? userResponse
        : userResponse?.rows || userResponse?.data || []

      const deptMap: Record<string, string> = {}
      flattenTreeLabels(deptData, deptMap)
      deptLabelMap.value = deptMap

      userLabelMap.value = userData.reduce((result: Record<string, string>, item: any) => {
        result[String(item.userId)] = item.nickName
          ? `${item.nickName} (${item.userName})`
          : item.userName
        return result
      }, {})
    } catch (error) {
      console.error('加载位置关联选项失败:', error)
      deptLabelMap.value = {}
      userLabelMap.value = {}
    }
  }

  const { columnChecks, columns } = useTableColumns(() => [
    {
      prop: 'locationCode',
      label: '位置编码',
      minWidth: 140
    },
    {
      prop: 'locationName',
      label: '位置名称',
      minWidth: 180
    },
    {
      prop: 'locationType',
      label: '位置类型',
      width: 120,
      align: 'center',
      formatter: (row: any) => {
        return h(DictTag, { options: asset_location_type.value, value: row.locationType })
      }
    },
    {
      prop: 'deptId',
      label: '所属部门',
      minWidth: 140,
      formatter: (row: any) => deptLabelMap.value[String(row.deptId)] || row.deptId || '-'
    },
    {
      prop: 'managerUserId',
      label: '管理员',
      minWidth: 140,
      formatter: (row: any) =>
        userLabelMap.value[String(row.managerUserId)] || row.managerUserId || '-'
    },
    {
      prop: 'status',
      label: '状态',
      width: 100,
      align: 'center',
      formatter: (row: any) => {
        return h(DictTag, { options: sys_normal_disable.value, value: row.status })
      }
    },
    {
      prop: 'createTime',
      label: '创建时间',
      width: 180,
      align: 'center',
      formatter: (row: any) => row.createTime || '-'
    },
    {
      prop: 'operation',
      label: '操作',
      width: 240,
      align: 'right',
      formatter: (row: any) => renderOperation(row)
    }
  ])

  /**
   * 查询位置列表，并组装成树表需要的层级结构。
   */
  const getList = async () => {
    loading.value = true
    errorMessage.value = ''
    try {
      const response: any = await listLocation(formFilters)
      const data = Array.isArray(response) ? response : response?.data || response?.rows || []
      locationList.value = handleTree(data, 'locationId')
    } catch (error: any) {
      locationList.value = []
      errorMessage.value = error?.message || '获取存放位置失败，请稍后重试。'
      console.error('获取存放位置失败:', error)
    } finally {
      loading.value = false
    }
  }

  const handleReset = () => {
    Object.assign(formFilters, initialSearchState)
    getList()
  }

  const handleSearch = () => {
    getList()
  }

  const handleRefresh = () => {
    getList()
  }

  /**
   * 控制整棵树的展开与收起，便于快速检查位置层级。
   */
  const toggleExpand = () => {
    isExpanded.value = !isExpanded.value
    nextTick(() => {
      if (!tableRef.value?.elTableRef || !locationList.value.length) return

      const processRows = (rows: any[]) => {
        rows.forEach((row) => {
          if (row.children?.length) {
            tableRef.value.elTableRef.toggleRowExpansion(row, isExpanded.value)
            processRows(row.children)
          }
        })
      }

      processRows(locationList.value)
    })
  }

  const handleAddRoot = () => {
    dialogType.value = 'add'
    currentData.value = { parentId: 0 }
    dialogVisible.value = true
  }

  const handleAddChild = (row: any) => {
    dialogType.value = 'add'
    currentData.value = row
    dialogVisible.value = true
  }

  const handleEdit = (row: any) => {
    dialogType.value = 'edit'
    currentData.value = { ...row }
    dialogVisible.value = true
  }

  /**
   * 删除前给出明确确认，避免误删仍被资产引用的位置节点。
   */
  const handleDelete = async (row: any) => {
    try {
      await ElMessageBox.confirm(
        `是否确认删除位置“${row.locationName}”？删除后不可恢复。`,
        '提示',
        {
          type: 'warning'
        }
      )
      await delLocation(row.locationId)
      ElMessage.success('删除成功')
      await getList()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('删除存放位置失败:', error)
      }
    }
  }

  onMounted(async () => {
    await loadRelationOptions()
    await getList()
    void sys_normal_disable.value
    void asset_location_type.value
  })
</script>

<style lang="scss" scoped>
  .asset-location-page {
    padding: 12px;
  }

  .asset-location-empty {
    min-height: 360px;
    display: flex;
    align-items: center;
    justify-content: center;
  }
</style>
