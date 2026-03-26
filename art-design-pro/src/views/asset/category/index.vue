<template>
  <div class="asset-category-page art-full-height">
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
          <ElButton v-auth="'asset:category:add'" type="primary" @click="handleAddRoot" v-ripple>
            新增一级分类
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

      <div v-if="showEmptyState" class="asset-category-empty">
        <ElEmpty :description="emptyDescription">
          <ElButton
            v-if="!hasActiveFilter"
            v-auth="'asset:category:add'"
            type="primary"
            @click="handleAddRoot"
          >
            新增一级分类
          </ElButton>
          <ElButton v-else @click="handleReset">重置筛选</ElButton>
        </ElEmpty>
      </div>

      <ArtTable
        v-else
        ref="tableRef"
        rowKey="categoryId"
        :loading="loading"
        :columns="columns"
        :data="categoryList"
        :stripe="false"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        :default-expand-all="isExpanded"
      />
    </ElCard>

    <CategoryDialog
      v-model="dialogVisible"
      :dialog-type="dialogType"
      :category-data="currentData"
      @success="getList"
    />
    <CategoryTemplateDialog
      v-model="templateDialogVisible"
      :category-id="templateTargetCategory?.categoryId"
      :category-name="templateTargetCategory?.categoryName"
      @success="getList"
    />
  </div>
</template>

<script setup lang="ts">
  import { computed, h, nextTick, onMounted, reactive, ref } from 'vue'
  import { ElButton, ElMessage, ElMessageBox, ElTag } from 'element-plus'
  import { listCategory, delCategory } from '@/api/asset/category'
  import { handleTree } from '@/utils/ruoyi'
  import { useDict } from '@/utils/dict'
  import { useTableColumns } from '@/hooks/core/useTableColumns'
  import { useUserStore } from '@/store/modules/user'
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import DictTag from '@/components/DictTag/index.vue'
  import CategoryDialog from './modules/category-dialog.vue'
  import CategoryTemplateDialog from './modules/category-template-dialog.vue'

  defineOptions({ name: 'AssetCategory' })

  const { sys_normal_disable } = useDict('sys_normal_disable')
  const userStore = useUserStore()

  const loading = ref(false)
  const isExpanded = ref(true)
  const tableRef = ref()
  const categoryList = ref<any[]>([])
  const errorMessage = ref('')
  const dialogVisible = ref(false)
  const templateDialogVisible = ref(false)
  const dialogType = ref<'add' | 'edit'>('add')
  const currentData = ref<any>()
  const templateTargetCategory = ref<any>()

  const initialSearchState = {
    categoryName: '',
    status: ''
  }

  const formFilters = reactive({ ...initialSearchState })

  /**
   * 给字典值统一做空数组兜底，避免首屏渲染时字典尚未回填导致页面报错。
   */
  const statusOptions = computed(() => {
    return Array.isArray(sys_normal_disable.value) ? sys_normal_disable.value : []
  })

  const searchBarKey = computed(() => `${statusOptions.value.length}`)

  const flagTagTypeMap: Record<string, '' | 'success' | 'info' | 'warning' | 'danger'> = {
    '1': 'success',
    '0': 'info'
  }

  const hasActiveFilter = computed(
    () => Boolean(formFilters.categoryName?.trim()) || Boolean(formFilters.status)
  )

  /**
   * 统一判断页面内按钮权限，保证主按钮和行内按钮口径一致。
   */
  const hasPermission = (permission: string) => {
    const permissions = userStore.permissions || []
    return permissions.includes('*:*:*') || permissions.includes(permission)
  }

  const showEmptyState = computed(
    () => !loading.value && !errorMessage.value && categoryList.value.length === 0
  )

  const emptyDescription = computed(() => {
    if (hasActiveFilter.value) {
      return '没有匹配的分类，请调整筛选条件后重试。'
    }
    return '还没有资产分类，先建立分类树，后续建账和盘点才不会混乱。'
  })

  const formItems = computed(() => [
    {
      label: '分类名称',
      key: 'categoryName',
      type: 'input',
      props: {
        placeholder: '请输入分类名称',
        clearable: true
      }
    },
    {
      label: '分类状态',
      key: 'status',
      type: 'select',
      props: {
        placeholder: '请选择分类状态',
        clearable: true,
        options: statusOptions.value
      }
    }
  ])

  /**
   * 渲染布尔型能力标签，避免把关键规则藏进弹窗。
   */
  const renderFlagTag = (value: string, positiveLabel: string, negativeLabel = '否') => {
    const text = value === '1' ? positiveLabel : negativeLabel
    return h(
      ElTag,
      {
        type: flagTagTypeMap[value] || 'info',
        effect: 'light'
      },
      () => text
    )
  }

  const { columnChecks, columns } = useTableColumns(() => [
    {
      prop: 'categoryCode',
      label: '分类编码',
      minWidth: 140
    },
    {
      prop: 'categoryName',
      label: '分类名称',
      minWidth: 180
    },
    {
      prop: 'depreciableFlag',
      label: '折旧',
      width: 90,
      align: 'center',
      formatter: (row: any) => renderFlagTag(row.depreciableFlag, '是')
    },
    {
      prop: 'serialRequiredFlag',
      label: '序列号',
      width: 90,
      align: 'center',
      formatter: (row: any) => renderFlagTag(row.serialRequiredFlag, '必填')
    },
    {
      prop: 'borrowableFlag',
      label: '借用',
      width: 90,
      align: 'center',
      formatter: (row: any) => renderFlagTag(row.borrowableFlag, '允许')
    },
    {
      prop: 'inventoryRequiredFlag',
      label: '盘点',
      width: 90,
      align: 'center',
      formatter: (row: any) => renderFlagTag(row.inventoryRequiredFlag, '纳入')
    },
    {
      prop: 'usefulLifeMonths',
      label: '寿命(月)',
      width: 100,
      align: 'center',
      formatter: (row: any) => row.usefulLifeMonths ?? '-'
    },
    {
      prop: 'status',
      label: '状态',
      width: 100,
      align: 'center',
      formatter: (row: any) => {
        return h(DictTag, { options: statusOptions.value, value: row.status })
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
      width: 300,
      align: 'right',
      formatter: (row: any) => {
        const actionNodes = []

        if (hasPermission('asset:category:add')) {
          actionNodes.push(
            h(ArtButtonTable, {
              type: 'add',
              onClick: () => handleAddChild(row)
            })
          )
        }

        if (hasPermission('asset:category:edit')) {
          actionNodes.push(
            h(
              ElButton,
              {
                link: true,
                type: 'primary',
                size: 'small',
                onClick: () => handleEditTemplate(row)
              },
              () => '字段模板'
            )
          )
          actionNodes.push(
            h(ArtButtonTable, {
              type: 'edit',
              onClick: () => handleEdit(row)
            })
          )
        }

        if (hasPermission('asset:category:remove')) {
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
  ])

  /**
   * 查询分类列表，并组装成树表需要的层级结构。
   */
  const getList = async () => {
    loading.value = true
    errorMessage.value = ''
    try {
      const response: any = await listCategory(formFilters)
      const data = Array.isArray(response) ? response : response?.data || response?.rows || []
      categoryList.value = handleTree(data, 'categoryId')
    } catch (error: any) {
      categoryList.value = []
      errorMessage.value = error?.message || '获取资产分类失败，请稍后重试。'
      console.error('获取资产分类失败:', error)
    } finally {
      loading.value = false
    }
  }

  /**
   * 重置筛选条件，回到默认浏览视图。
   */
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
   * 控制整棵树的展开与收起，便于快速检查层级。
   */
  const toggleExpand = () => {
    isExpanded.value = !isExpanded.value
    nextTick(() => {
      if (!tableRef.value?.elTableRef || !categoryList.value.length) return

      const processRows = (rows: any[]) => {
        rows.forEach((row) => {
          if (row.children?.length) {
            tableRef.value.elTableRef.toggleRowExpansion(row, isExpanded.value)
            processRows(row.children)
          }
        })
      }

      processRows(categoryList.value)
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
   * 字段模板依附分类配置，单独开弹窗避免和分类基础信息表单互相污染。
   */
  const handleEditTemplate = (row: any) => {
    templateTargetCategory.value = { ...row }
    templateDialogVisible.value = true
  }

  /**
   * 删除前给出明确确认，避免误删整棵分类分支。
   */
  const handleDelete = async (row: any) => {
    try {
      await ElMessageBox.confirm(
        `是否确认删除分类“${row.categoryName}”？删除后不可恢复。`,
        '提示',
        {
          type: 'warning'
        }
      )
      await delCategory(row.categoryId)
      ElMessage.success('删除成功')
      await getList()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('删除资产分类失败:', error)
      }
    }
  }

  onMounted(() => {
    getList()
    void sys_normal_disable.value
  })
</script>

<style lang="scss" scoped>
  .asset-category-page {
    padding: 12px;
  }

  .asset-category-empty {
    min-height: 360px;
    display: flex;
    align-items: center;
    justify-content: center;
  }
</style>
