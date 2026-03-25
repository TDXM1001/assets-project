<template>
  <div class="asset-dashboard-page art-full-height">
    <ElAlert
      class="mb-4"
      type="success"
      :closable="false"
      show-icon
      title="当前看板已切到真实 dashboard 聚合接口，展示口径会跟随当前角色的数据范围。"
    />

    <ElAlert
      v-if="loadError"
      class="mb-4"
      type="warning"
      :closable="false"
      show-icon
      :title="loadError"
    >
      <template #default>
        <ElButton link type="primary" @click="loadDashboardData">重新加载</ElButton>
      </template>
    </ElAlert>

    <ElRow :gutter="16" class="mb-4">
      <ElCol v-for="card in summaryCards" :key="card.key" :xs="24" :sm="12" :lg="6">
        <ElCard shadow="hover" class="summary-card">
          <div class="summary-card__label">{{ card.label }}</div>
          <div class="summary-card__value">{{ card.value }}</div>
          <div class="summary-card__tip">{{ card.tip }}</div>
        </ElCard>
      </ElCol>
    </ElRow>

    <ElRow :gutter="16" class="mb-4">
      <ElCol :xs="24" :lg="14">
        <ElCard shadow="never" class="asset-dashboard-card">
          <template #header>
            <div class="card-header">
              <span>资产状态分布</span>
              <ElTag effect="light" type="info">{{ scopeSummaryText }}</ElTag>
            </div>
          </template>

          <ElEmpty
            v-if="!statusStats.length && !loading"
            description="当前范围下还没有可统计的资产状态数据"
          />

          <div v-else v-loading="loading" class="status-list">
            <div v-for="item in statusStats" :key="item.status" class="status-item">
              <div class="status-item__top">
                <div class="status-item__label">
                  <DictTag :options="asset_status" :value="item.status" />
                </div>
                <div class="status-item__meta">
                  <span>{{ item.value }}</span>
                  <span>{{ item.percentText }}</span>
                </div>
              </div>
              <ElProgress
                :percentage="item.percent"
                :stroke-width="10"
                :show-text="false"
                :color="item.color"
              />
            </div>
          </div>
        </ElCard>
      </ElCol>

      <ElCol :xs="24" :lg="10">
        <ElCard shadow="never" class="asset-dashboard-card">
          <template #header>
            <span>当前关注事项</span>
          </template>

          <ElEmpty v-if="!todoItems.length && !loading" description="当前没有需要关注的事项" />

          <div v-else class="todo-list" v-loading="loading">
            <div v-for="item in todoItems" :key="item.key" class="todo-item">
              <div class="todo-item__content">
                <div class="todo-item__label">{{ item.label }}</div>
                <div class="todo-item__desc">{{ item.desc }}</div>
              </div>
              <div class="todo-item__action">
                <ElTag :type="item.type" effect="light">{{ item.value }}</ElTag>
                <ElButton
                  v-if="item.routePath"
                  link
                  type="primary"
                  @click="router.push(item.routePath)"
                >
                  去处理
                </ElButton>
              </div>
            </div>
          </div>
        </ElCard>
      </ElCol>
    </ElRow>

    <ElCard shadow="never" class="asset-dashboard-card mb-4">
      <template #header>
        <div class="card-header">
          <span>近 7 日业务趋势</span>
          <span class="card-tip">{{ trendDescription }}</span>
        </div>
      </template>

      <ElEmpty
        v-if="!trendRows.length && !loading"
        description="当前范围下还没有可展示的趋势数据"
      />

      <ElTable v-else v-loading="loading" :data="trendRows" stripe>
        <ElTableColumn prop="bizDate" label="日期" min-width="130" />
        <ElTableColumn prop="eventCount" label="流水数量" min-width="120" />
        <ElTableColumn prop="orderCount" label="单据数量" min-width="120" />
        <ElTableColumn prop="inventoryCount" label="盘点任务数" min-width="120" />
        <ElTableColumn prop="diffCount" label="差异处理数" min-width="120" />
        <ElTableColumn prop="summary" label="趋势说明" min-width="240" show-overflow-tooltip />
      </ElTable>
    </ElCard>

    <ElCard shadow="never" class="asset-dashboard-card">
      <template #header>
        <span>快捷入口</span>
      </template>

      <div class="quick-actions">
        <ElButton type="primary" @click="router.push('/asset/info')">资产台账</ElButton>
        <ElButton v-if="canAccess('asset:order:list')" @click="router.push('/asset/order')">
          业务单据
        </ElButton>
        <ElButton v-if="canAccess('asset:inventory:list')" @click="router.push('/asset/inventory')">
          盘点任务
        </ElButton>
        <ElButton v-if="canAccess('asset:event:list')" @click="router.push('/asset/event')">
          资产流水
        </ElButton>
      </div>

      <ElDescriptions :column="2" border class="mt-4">
        <ElDescriptionsItem label="数据口径">
          {{ scopeSummaryText }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="实现方式">
          当前页面直接消费 `summary / todo / trend` 三组聚合接口，不再由前端拼 4 个列表接口。
        </ElDescriptionsItem>
      </ElDescriptions>
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import { computed, onMounted, ref } from 'vue'
  import { useRouter } from 'vue-router'
  import DictTag from '@/components/DictTag/index.vue'
  import {
    getAssetDashboardSummary,
    getAssetDashboardTodo,
    getAssetDashboardTrend,
    type AssetDashboardSummaryResponse,
    type AssetDashboardTodoResponse,
    type AssetDashboardTrendResponse
  } from '@/api/asset/dashboard'
  import { useUserStore } from '@/store/modules/user'
  import { useDict } from '@/utils/dict'
  import { useAssetRoleScope } from '@/views/asset/shared/use-asset-role-scope'

  defineOptions({ name: 'AssetDashboard' })

  interface DashboardStatusStat {
    status: string
    value: number
  }

  interface DashboardSummary {
    assetTotal: number
    orderTotal: number
    inventoryTotal: number
    eventTotal: number
    recentEventDays: number
    statusStats: DashboardStatusStat[]
    scopeDesc: string
  }

  interface DashboardTodoItem {
    key: string
    label: string
    value: number | string
    desc: string
    type: 'primary' | 'success' | 'warning' | 'info' | 'danger'
    routePath: string
  }

  interface DashboardTrendRow {
    bizDate: string
    eventCount: number
    orderCount: number
    inventoryCount: number
    diffCount: number
    summary: string
  }

  const STATUS_COLORS = ['#3b82f6', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#14b8a6']

  const router = useRouter()
  const userStore = useUserStore()
  const { asset_status } = useDict('asset_status')
  const { isAssetAdmin, isAssetAuditor, isAssetDeptManager, isSelfScopedAssetUser } =
    useAssetRoleScope()

  const loading = ref(false)
  const loadError = ref('')
  const summaryState = ref<DashboardSummary>(createEmptySummary())
  const todoItems = ref<DashboardTodoItem[]>([])
  const trendRows = ref<DashboardTrendRow[]>([])

  function canAccess(permission: string) {
    return userStore.permissions.includes('*:*:*') || userStore.permissions.includes(permission)
  }

  const summaryCards = computed(() => [
    {
      key: 'assetTotal',
      label: '可见资产总数',
      value: summaryState.value.assetTotal,
      tip: '按当前角色的数据范围实时聚合'
    },
    {
      key: 'orderTotal',
      label: '可见业务单据',
      value: canAccess('asset:order:list') ? summaryState.value.orderTotal : '未授权',
      tip: !canAccess('asset:order:list')
        ? '当前角色没有业务单据查看权限'
        : '用于快速判断流程积压情况'
    },
    {
      key: 'inventoryTotal',
      label: '可见盘点任务',
      value: canAccess('asset:inventory:list') ? summaryState.value.inventoryTotal : '未授权',
      tip: !canAccess('asset:inventory:list')
        ? '当前角色没有盘点任务查看权限'
        : '用于识别盘点执行压力'
    },
    {
      key: 'eventTotal',
      label: `最近 ${summaryState.value.recentEventDays} 日流水记录`,
      value: canAccess('asset:event:list') ? summaryState.value.eventTotal : '未授权',
      tip: !canAccess('asset:event:list')
        ? '当前角色没有资产流水查看权限'
        : '帮助快速确认最近的资产变动'
    }
  ])

  const scopeSummaryText = computed(() => {
    if (summaryState.value.scopeDesc) {
      return summaryState.value.scopeDesc
    }
    if (isSelfScopedAssetUser.value) {
      return '当前为“我的数据”视角，聚合结果仅统计你本人可见范围。'
    }
    if (isAssetDeptManager.value && !isAssetAdmin.value && !isAssetAuditor.value) {
      return '当前为“部门数据”视角，聚合结果覆盖本部门及以下范围。'
    }
    if (isAssetAuditor.value && !isAssetAdmin.value) {
      return '当前为“全量只读”视角，聚合结果覆盖全部审计范围。'
    }
    return '按当前角色的数据范围聚合'
  })

  const trendDescription = computed(() => {
    if (!trendRows.value.length) {
      return '暂无趋势数据'
    }
    return '聚合了当前范围内近 7 天的流水、单据、盘点创建与异常差异数量'
  })

  const statusStats = computed(() => {
    const total = summaryState.value.assetTotal || 0
    return summaryState.value.statusStats.map((item, index) => {
      const percent = total > 0 ? Math.round((item.value / total) * 100) : 0
      return {
        ...item,
        percent,
        percentText: `${percent}%`,
        color: STATUS_COLORS[index % STATUS_COLORS.length]
      }
    })
  })

  function createEmptySummary(): DashboardSummary {
    return {
      assetTotal: 0,
      orderTotal: 0,
      inventoryTotal: 0,
      eventTotal: 0,
      recentEventDays: 7,
      statusStats: [],
      scopeDesc: ''
    }
  }

  function normalizeSummary(data?: AssetDashboardSummaryResponse | null): DashboardSummary {
    return {
      assetTotal: toNumber(data?.assetTotal),
      orderTotal: toNumber(data?.orderTotal),
      inventoryTotal: toNumber(data?.inventoryTaskTotal),
      eventTotal: toNumber(data?.recentEventTotal),
      recentEventDays: toNumber(data?.recentEventDays) || 7,
      statusStats: Array.isArray(data?.statusList)
        ? data.statusList
            .map((item) => ({
              status: String(item?.status ?? ''),
              value: toNumber(item?.totalCount)
            }))
            .filter((item) => item.status)
        : [],
      scopeDesc: ''
    }
  }

  function normalizeTodo(data?: AssetDashboardTodoResponse | null) {
    const rows = Array.isArray(data?.itemList) ? data.itemList : []
    return rows
      .filter((item) => item?.permitted && toNumber(item?.count) > 0)
      .map((item, index): DashboardTodoItem => {
        const todoCount = toNumber(item?.count)
        const key = String(item?.key || `todo-${index}`)
        const todoMeta = buildTodoMeta(key, todoCount)

        return {
          key,
          label: item?.label || todoMeta.label,
          value: todoCount,
          desc: todoMeta.desc,
          type: todoMeta.type,
          routePath: item?.routePath || ''
        }
      })
      .sort((left, right) => Number(right.value) - Number(left.value))
  }

  function buildTodoMeta(key: string, count: number) {
    if (key === 'pendingDiff') {
      return {
        label: '待处理差异',
        desc: `还有 ${count} 条异常差异未收口，建议优先处理。`,
        type: 'danger' as const
      }
    }
    if (key === 'unfinishedInventory') {
      return {
        label: '待完成盘点',
        desc: `还有 ${count} 个盘点任务尚未完成，建议尽快推进。`,
        type: 'warning' as const
      }
    }
    return {
      label: '待处理单据',
      desc: `还有 ${count} 张业务单据仍在流转中。`,
      type: 'primary' as const
    }
  }

  function normalizeTrend(data?: AssetDashboardTrendResponse | null) {
    const rows = Array.isArray(data?.pointList) ? data.pointList : []
    return rows.map((item): DashboardTrendRow => {
      const normalizedRow = {
        bizDate: item?.statDate || '-',
        eventCount: toNumber(item?.eventCount),
        orderCount: toNumber(item?.orderCount),
        inventoryCount: toNumber(item?.inventoryCount),
        diffCount: toNumber(item?.diffCount),
        summary: ''
      }

      return {
        ...normalizedRow,
        summary: buildTrendSummary(normalizedRow)
      }
    })
  }

  function toNumber(value: unknown) {
    const nextValue = Number(value)
    return Number.isFinite(nextValue) ? nextValue : 0
  }

  function isPermissionError(error: unknown) {
    const code = (error as { code?: number })?.code
    return code === 401 || code === 403
  }

  /**
   * 趋势区优先讲清楚“当天发生了什么”，避免用户自己再做二次换算。
   */
  function buildTrendSummary(item: Omit<DashboardTrendRow, 'summary'>) {
    return `流水 ${item.eventCount} 条 / 单据 ${item.orderCount} 条 / 盘点创建 ${item.inventoryCount} 项 / 异常差异 ${item.diffCount} 项`
  }

  async function loadDashboardData() {
    loading.value = true
    loadError.value = ''

    const [summaryResult, todoResult, trendResult] = await Promise.allSettled([
      getAssetDashboardSummary(),
      getAssetDashboardTodo(),
      getAssetDashboardTrend()
    ])

    if (summaryResult.status === 'fulfilled') {
      summaryState.value = normalizeSummary(summaryResult.value)
    } else {
      summaryState.value = createEmptySummary()
      if (!isPermissionError(summaryResult.reason)) {
        loadError.value = '资产看板加载失败，请确认 dashboard 聚合接口已经可用。'
      }
    }

    if (todoResult.status === 'fulfilled') {
      todoItems.value = normalizeTodo(todoResult.value)
    } else {
      todoItems.value = []
    }

    if (trendResult.status === 'fulfilled') {
      trendRows.value = normalizeTrend(trendResult.value)
    } else {
      trendRows.value = []
    }

    loading.value = false
  }

  onMounted(() => {
    loadDashboardData()
  })
</script>

<style scoped lang="scss">
  .asset-dashboard-page {
    padding: 16px;
    overflow: auto;
  }

  .summary-card {
    min-height: 132px;

    &__label {
      font-size: 14px;
      color: var(--art-text-gray-600);
    }

    &__value {
      margin-top: 14px;
      font-size: 30px;
      font-weight: 700;
      color: var(--art-text-gray-900);
    }

    &__tip {
      margin-top: 12px;
      font-size: 13px;
      line-height: 1.6;
      color: var(--art-text-gray-500);
    }
  }

  .asset-dashboard-card {
    height: 100%;
  }

  .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
  }

  .card-tip {
    font-size: 13px;
    color: var(--art-text-gray-500);
  }

  .status-list {
    display: flex;
    flex-direction: column;
    gap: 14px;
  }

  .status-item {
    display: flex;
    flex-direction: column;
    gap: 8px;

    &__top {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 12px;
    }

    &__meta {
      display: flex;
      gap: 12px;
      font-size: 13px;
      color: var(--art-text-gray-500);
    }
  }

  .todo-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .todo-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    padding: 14px 16px;
    background: var(--art-main-bg-color);
    border-radius: 12px;

    &__content {
      min-width: 0;
    }

    &__label {
      font-size: 14px;
      font-weight: 600;
      color: var(--art-text-gray-900);
    }

    &__desc {
      margin-top: 6px;
      font-size: 13px;
      line-height: 1.6;
      color: var(--art-text-gray-500);
    }

    &__action {
      display: flex;
      align-items: center;
      gap: 8px;
      flex-shrink: 0;
    }
  }

  .quick-actions {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
  }

  .mb-4 {
    margin-bottom: 16px;
  }

  .mt-4 {
    margin-top: 16px;
  }
  @media (max-width: 992px) {
    .todo-item {
      align-items: flex-start;
      flex-direction: column;

      &__action {
        width: 100%;
        justify-content: space-between;
      }
    }

    .card-header {
      align-items: flex-start;
      flex-direction: column;
    }
  }
</style>
