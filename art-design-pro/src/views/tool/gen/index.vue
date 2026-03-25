<template>
  <div class="tool-page art-full-height">
    <ElAlert
      class="mb-4"
      type="info"
      :closable="false"
      show-icon
      title="当前是轻量代码生成管理页，先把后端已有生成能力接成可读可查入口。"
    />

    <ElRow :gutter="16" class="mb-4">
      <ElCol :xs="24" :sm="12" :lg="6">
        <ElCard shadow="hover" class="summary-card">
          <div class="summary-card__label">已纳入生成管理</div>
          <div class="summary-card__value">{{ pagination.total }}</div>
          <div class="summary-card__tip">来自 /tool/gen/list</div>
        </ElCard>
      </ElCol>
      <ElCol :xs="24" :sm="12" :lg="6">
        <ElCard shadow="hover" class="summary-card">
          <div class="summary-card__label">数据库可导入表</div>
          <div class="summary-card__value">{{ dbTableTotal }}</div>
          <div class="summary-card__tip">来自 /tool/gen/db/list</div>
        </ElCard>
      </ElCol>
      <ElCol :xs="24" :sm="12" :lg="6">
        <ElCard shadow="hover" class="summary-card">
          <div class="summary-card__label">当前说明</div>
          <div class="summary-card__value">只读</div>
          <div class="summary-card__tip">先保留查询能力，避免菜单空挂</div>
        </ElCard>
      </ElCol>
      <ElCol :xs="24" :sm="12" :lg="6">
        <ElCard shadow="hover" class="summary-card">
          <div class="summary-card__label">更推荐的方向</div>
          <div class="summary-card__value">手写业务</div>
          <div class="summary-card__tip">资产模块当前主线仍是手写实现，不依赖生成器</div>
        </ElCard>
      </ElCol>
    </ElRow>

    <ArtSearchBar
      v-model="filters"
      :items="formItems"
      :showExpand="false"
      @search="handleSearch"
      @reset="handleReset"
    />

    <ElCard shadow="never" class="tool-card">
      <template #header>
        <div class="card-header">
          <span>代码生成业务表</span>
          <ElButton type="primary" link @click="loadData">刷新</ElButton>
        </div>
      </template>

      <ElAlert
        v-if="tableError"
        class="mb-4"
        type="error"
        :closable="false"
        show-icon
        :title="tableError"
      />

      <ElTable v-loading="loading" :data="rows" stripe>
        <ElTableColumn prop="tableName" label="表名" min-width="180" />
        <ElTableColumn prop="tableComment" label="表说明" min-width="180" />
        <ElTableColumn prop="className" label="类名" min-width="180" />
        <ElTableColumn prop="tplCategory" label="模板类型" min-width="120" />
        <ElTableColumn prop="packageName" label="包路径" min-width="220" show-overflow-tooltip />
        <ElTableColumn prop="moduleName" label="模块名" min-width="120" />
        <ElTableColumn prop="businessName" label="业务名" min-width="120" />
        <ElTableColumn prop="functionName" label="功能名" min-width="160" show-overflow-tooltip />
        <ElTableColumn prop="createTime" label="创建时间" min-width="180" />
      </ElTable>

      <div class="pagination-wrap">
        <ElPagination
          background
          layout="total, sizes, prev, pager, next"
          :current-page="pagination.current"
          :page-size="pagination.size"
          :page-sizes="[10, 20, 50]"
          :total="pagination.total"
          @current-change="handleCurrentChange"
          @size-change="handleSizeChange"
        />
      </div>
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import { computed, onMounted, reactive, ref } from 'vue'
  import { ElMessage } from 'element-plus'
  import { listGenDbTable, listGenTable } from '@/api/tool/gen'

  defineOptions({ name: 'ToolGen' })

  const loading = ref(false)
  const tableError = ref('')
  const rows = ref<any[]>([])
  const dbTableTotal = ref(0)
  const pagination = reactive({
    current: 1,
    size: 10,
    total: 0
  })

  const initialFilters = {
    tableName: '',
    tableComment: ''
  }

  const filters = reactive({ ...initialFilters })

  const formItems = computed(() => [
    {
      label: '表名',
      key: 'tableName',
      type: 'input',
      props: { placeholder: '请输入表名', clearable: true }
    },
    {
      label: '表说明',
      key: 'tableComment',
      type: 'input',
      props: { placeholder: '请输入表说明', clearable: true }
    }
  ])

  async function loadData() {
    loading.value = true
    tableError.value = ''

    try {
      const [tableResult, dbTableResult] = (await Promise.all([
        listGenTable({
          pageNum: pagination.current,
          pageSize: pagination.size,
          tableName: filters.tableName,
          tableComment: filters.tableComment
        }),
        listGenDbTable({ pageNum: 1, pageSize: 1 })
      ])) as [any, any]

      rows.value = tableResult?.rows || []
      pagination.total = tableResult?.total || 0
      dbTableTotal.value = dbTableResult?.total || 0
    } catch (error) {
      console.error('[ToolGen] 加载代码生成列表失败:', error)
      tableError.value = '代码生成列表加载失败，请确认当前账号具备代码生成查看权限。'
      rows.value = []
      pagination.total = 0
      dbTableTotal.value = 0
      ElMessage.error('代码生成列表加载失败')
    } finally {
      loading.value = false
    }
  }

  function handleSearch() {
    pagination.current = 1
    loadData()
  }

  function handleReset() {
    Object.assign(filters, initialFilters)
    pagination.current = 1
    loadData()
  }

  function handleCurrentChange(page: number) {
    pagination.current = page
    loadData()
  }

  function handleSizeChange(size: number) {
    pagination.size = size
    pagination.current = 1
    loadData()
  }

  onMounted(() => {
    loadData()
  })
</script>

<style scoped lang="scss">
  .tool-page {
    padding: 16px;
    overflow: auto;
  }

  .tool-card {
    min-height: 480px;
  }

  .summary-card {
    min-height: 124px;

    &__label {
      font-size: 14px;
      color: var(--art-text-gray-600);
    }

    &__value {
      margin-top: 14px;
      font-size: 28px;
      font-weight: 700;
      color: var(--art-text-gray-900);
    }

    &__tip {
      margin-top: 12px;
      font-size: 13px;
      color: var(--art-text-gray-500);
      line-height: 1.6;
    }
  }

  .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .pagination-wrap {
    display: flex;
    justify-content: flex-end;
    margin-top: 16px;
  }

  .mb-4 {
    margin-bottom: 16px;
  }
</style>
