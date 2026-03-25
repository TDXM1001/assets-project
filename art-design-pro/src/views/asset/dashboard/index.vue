<template>
  <div class="asset-dashboard-page art-full-height">
    <ElAlert
      class="mb-4"
      type="info"
      :closable="false"
      show-icon
      title="当前是一版轻量资产看板，直接基于现有列表接口聚合，先保证资产模块入口稳定可用。"
    />

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
      <ElCol :xs="24" :lg="16">
        <ElCard shadow="never" class="asset-dashboard-card">
          <template #header>
            <div class="card-header">
              <span>最近资产流水</span>
              <ElButton
                v-if="canAccess('asset:event:list')"
                link
                type="primary"
                @click="router.push('/asset/event')"
              >
                查看全部
              </ElButton>
            </div>
          </template>

          <ElEmpty
            v-if="!canAccess('asset:event:list')"
            description="当前角色没有资产流水查看权限"
          />
          <ElEmpty v-else-if="!recentEvents.length && !loading" description="最近还没有资产流水" />
          <ElTable v-else v-loading="loading" :data="recentEvents" stripe>
            <ElTableColumn prop="assetCode" label="资产编码" min-width="150" />
            <ElTableColumn prop="assetName" label="资产名称" min-width="160" />
            <ElTableColumn label="事件类型" min-width="120">
              <template #default="{ row }">
                <DictTag :options="asset_event_type" :value="row.eventType" />
              </template>
            </ElTableColumn>
            <ElTableColumn
              prop="eventDesc"
              label="事件说明"
              min-width="220"
              show-overflow-tooltip
            />
            <ElTableColumn prop="operatorName" label="操作人" min-width="120" />
            <ElTableColumn prop="eventTime" label="操作时间" min-width="180" />
          </ElTable>
        </ElCard>
      </ElCol>

      <ElCol :xs="24" :lg="8">
        <ElCard shadow="never" class="asset-dashboard-card">
          <template #header>
            <span>当前关注事项</span>
          </template>

          <ElEmpty v-if="!todoItems.length" description="当前没有需要关注的事项" />
          <div v-else class="todo-list">
            <div v-for="item in todoItems" :key="item.label" class="todo-item">
              <div class="todo-item__content">
                <div class="todo-item__label">{{ item.label }}</div>
                <div class="todo-item__desc">{{ item.desc }}</div>
              </div>
              <ElTag :type="item.type" effect="light">{{ item.value }}</ElTag>
            </div>
          </div>
        </ElCard>
      </ElCol>
    </ElRow>

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
          按当前登录角色的数据范围聚合，管理员看全量，其他角色只看自己可见数据。
        </ElDescriptionsItem>
        <ElDescriptionsItem label="实现方式">
          当前阶段不依赖独立 dashboard API，直接复用资产台账、单据、盘点、流水接口。
        </ElDescriptionsItem>
      </ElDescriptions>
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import { computed, onMounted, ref } from 'vue'
  import { useRouter } from 'vue-router'
  import { ElMessage } from 'element-plus'
  import DictTag from '@/components/DictTag/index.vue'
  import { listAssetInfo } from '@/api/asset/info'
  import { listAssetOrder } from '@/api/asset/order'
  import { listAssetInventory } from '@/api/asset/inventory'
  import { listAssetEvent } from '@/api/asset/event'
  import { useDict } from '@/utils/dict'
  import { useUserStore } from '@/store/modules/user'

  defineOptions({ name: 'AssetDashboard' })

  interface SummaryState {
    assetTotal: number
    orderTotal: number
    inventoryTotal: number | null
    eventTotal: number | null
  }

  interface TodoItem {
    label: string
    value: string
    desc: string
    type?: 'success' | 'warning' | 'info' | 'danger'
  }

  const router = useRouter()
  const userStore = useUserStore()
  const { asset_event_type } = useDict('asset_event_type')

  const loading = ref(false)
  const recentEvents = ref<any[]>([])
  const orderRows = ref<any[]>([])
  const inventoryRows = ref<any[]>([])
  const summaryState = ref<SummaryState>({
    assetTotal: 0,
    orderTotal: 0,
    inventoryTotal: null,
    eventTotal: null
  })

  /**
   * 根据当前权限控制看板聚合范围，避免页面自身产生不必要的 403 请求。
   */
  function canAccess(permission: string) {
    return userStore.permissions.includes('*:*:*') || userStore.permissions.includes(permission)
  }

  const summaryCards = computed(() => [
    {
      key: 'assetTotal',
      label: '可见资产总数',
      value: summaryState.value.assetTotal,
      tip: '按当前角色的数据范围实时统计'
    },
    {
      key: 'orderTotal',
      label: '可见业务单据',
      value: canAccess('asset:order:list') ? summaryState.value.orderTotal : '未授权',
      tip: canAccess('asset:order:list') ? '便于快速回到流程工作台' : '当前角色未开放单据查看'
    },
    {
      key: 'inventoryTotal',
      label: '可见盘点任务',
      value:
        summaryState.value.inventoryTotal === null ? '未授权' : summaryState.value.inventoryTotal,
      tip:
        summaryState.value.inventoryTotal === null
          ? '当前角色未开放盘点任务'
          : '仅统计当前角色可见的盘点任务'
    },
    {
      key: 'eventTotal',
      label: '最近流水记录',
      value: summaryState.value.eventTotal === null ? '未授权' : summaryState.value.eventTotal,
      tip:
        summaryState.value.eventTotal === null
          ? '当前角色未开放流水查看'
          : '帮助快速确认最近的资产变化'
    }
  ])

  const todoItems = computed<TodoItem[]>(() => {
    const items: TodoItem[] = []

    if (canAccess('asset:order:list')) {
      const activeOrderCount = orderRows.value.filter(
        (item) => !['DONE', 'CANCELED', 'REJECTED'].includes(item.orderStatus)
      ).length
      items.push({
        label: '待跟进业务单据',
        value: String(activeOrderCount),
        desc: '建议优先处理未完成的资产单据，避免资产状态滞后。',
        type: activeOrderCount > 0 ? 'warning' : 'success'
      })
    }

    if (canAccess('asset:inventory:list')) {
      const activeInventoryCount = inventoryRows.value.filter(
        (item) => !['DONE', 'CANCELED'].includes(item.taskStatus)
      ).length
      items.push({
        label: '未完成盘点任务',
        value: String(activeInventoryCount),
        desc: '盘点任务结束后要及时处理差异，避免主数据和实际状态偏离。',
        type: activeInventoryCount > 0 ? 'danger' : 'success'
      })
    }

    if (canAccess('asset:event:list')) {
      items.push({
        label: '最近资产流水',
        value: String(recentEvents.value.length),
        desc: '可直接在本页查看最近的资产变更轨迹。',
        type: recentEvents.value.length > 0 ? 'info' : undefined
      })
    }

    return items
  })

  async function loadDashboardData() {
    loading.value = true

    try {
      const assetResult = (await listAssetInfo({ pageNum: 1, pageSize: 1 })) as any
      summaryState.value.assetTotal = assetResult?.total || 0

      if (canAccess('asset:order:list')) {
        const orderResult = (await listAssetOrder({ pageNum: 1, pageSize: 20 })) as any
        summaryState.value.orderTotal = orderResult?.total || 0
        orderRows.value = orderResult?.rows || []
      } else {
        summaryState.value.orderTotal = 0
        orderRows.value = []
      }

      if (canAccess('asset:inventory:list')) {
        const inventoryResult = (await listAssetInventory({ pageNum: 1, pageSize: 20 })) as any
        summaryState.value.inventoryTotal = inventoryResult?.total || 0
        inventoryRows.value = inventoryResult?.rows || []
      } else {
        summaryState.value.inventoryTotal = null
        inventoryRows.value = []
      }

      if (canAccess('asset:event:list')) {
        const eventResult = (await listAssetEvent({ pageNum: 1, pageSize: 6 })) as any
        summaryState.value.eventTotal = eventResult?.total || 0
        recentEvents.value = eventResult?.rows || []
      } else {
        summaryState.value.eventTotal = null
        recentEvents.value = []
      }
    } catch (error) {
      console.error('[AssetDashboard] 加载资产看板失败:', error)
      ElMessage.error('资产看板加载失败，请稍后重试')
    } finally {
      loading.value = false
    }
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
  }

  .quick-actions {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
  }
  .mt-4 {
    margin-top: 16px;
  }
</style>
