<template>
  <ElDrawer v-model="visible" :title="drawerTitle" size="640px" append-to-body destroy-on-close>
    <template v-if="assetData">
      <ElDescriptions :column="1" border class="mb-4">
        <ElDescriptionsItem label="资产编码">{{
          displayText(assetData.assetCode)
        }}</ElDescriptionsItem>
        <ElDescriptionsItem label="资产名称">{{
          displayText(assetData.assetName)
        }}</ElDescriptionsItem>
        <ElDescriptionsItem label="当前状态">
          <DictTag :options="asset_status" :value="assetData.assetStatus" />
        </ElDescriptionsItem>
      </ElDescriptions>

      <ElAlert
        v-if="loadError"
        class="mb-4"
        type="error"
        :closable="false"
        show-icon
        :title="loadError"
      >
        <template #default>
          <ElButton link type="primary" @click="loadEvents">重新加载</ElButton>
        </template>
      </ElAlert>

      <ElSkeleton v-if="loading" :rows="6" animated />

      <ElEmpty v-else-if="!eventList.length && !loadError" description="这项资产还没有流水记录">
        <ElButton type="primary" link @click="openFullPage">前往资产流水页</ElButton>
      </ElEmpty>

      <div v-else class="asset-event-drawer">
        <div class="asset-event-drawer__toolbar">
          <div class="asset-event-drawer__tip">按时间倒序展示最近 {{ EVENT_LIMIT }} 条流水</div>
          <ElButton link type="primary" @click="openFullPage">查看更多</ElButton>
        </div>

        <ElTimeline>
          <ElTimelineItem
            v-for="item in eventList"
            :key="item.eventId"
            :timestamp="displayText(item.operateTime)"
            placement="top"
          >
            <ElCard shadow="hover" class="asset-event-drawer__card">
              <div class="asset-event-drawer__meta">
                <ElSpace wrap>
                  <DictTag :options="asset_event_type" :value="item.eventType" />
                  <ElTag size="small" effect="plain">{{
                    formatSourceType(item.sourceOrderType)
                  }}</ElTag>
                  <ElTag v-if="item.sourceOrderNo" size="small" type="info" effect="plain">
                    {{ item.sourceOrderNo }}
                  </ElTag>
                  <span class="asset-event-drawer__operator">
                    {{ item.operatorUserName || '系统' }}
                  </span>
                </ElSpace>
              </div>

              <div class="asset-event-drawer__desc">
                {{ item.eventDesc || '未填写事件说明' }}
              </div>

              <div v-if="item.changeItems.length" class="asset-event-drawer__changes">
                <div class="asset-event-drawer__changes-title">关键字段变化</div>
                <div
                  v-for="change in item.changeItems"
                  :key="`${item.eventId}-${change.key}`"
                  class="asset-event-drawer__change-row"
                >
                  <div class="asset-event-drawer__change-label">{{ change.label }}</div>
                  <div class="asset-event-drawer__change-value">
                    <span>{{ change.beforeValue }}</span>
                    <span class="asset-event-drawer__arrow">→</span>
                    <span>{{ change.afterValue }}</span>
                  </div>
                </div>
              </div>
            </ElCard>
          </ElTimelineItem>
        </ElTimeline>

        <ElAlert
          v-if="showMoreHint"
          type="info"
          :closable="false"
          show-icon
          title="抽屉只展示最近流水，完整审计记录请进入资产流水页查看。"
        />
      </div>
    </template>

    <ElEmpty v-else description="请先选择一条资产记录" />
  </ElDrawer>
</template>

<script setup lang="ts">
  import { computed, ref, watch } from 'vue'
  import { useRouter } from 'vue-router'
  import DictTag from '@/components/DictTag/index.vue'
  import { treeLocationSelect } from '@/api/asset/location'
  import { getAssetEventByAssetId } from '@/api/asset/event'
  import { deptTreeSelect, listUser } from '@/api/system/user'
  import { useDict } from '@/utils/dict'

  defineOptions({ name: 'AssetEventDrawer' })

  interface TreeOption {
    id: number
    label: string
    children?: TreeOption[]
  }

  interface TimelineChangeItem {
    key: string
    label: string
    beforeValue: string
    afterValue: string
  }

  interface AssetEventRow {
    eventId: number
    eventType: string
    sourceOrderType?: string
    sourceOrderNo?: string
    eventDesc?: string
    operatorUserName?: string
    operateTime?: string
    beforeSnapshot?: string
    afterSnapshot?: string
    changeItems: TimelineChangeItem[]
  }

  const EVENT_LIMIT = 20

  const props = defineProps<{
    modelValue: boolean
    assetData?: {
      assetId?: number | string
      assetCode?: string
      assetName?: string
      assetStatus?: string
    }
  }>()

  const emit = defineEmits<{
    (e: 'update:modelValue', value: boolean): void
  }>()

  const router = useRouter()
  const { asset_event_type, asset_order_type, asset_status } = useDict(
    'asset_event_type',
    'asset_order_type',
    'asset_status'
  )

  const loading = ref(false)
  const loadError = ref('')
  const eventList = ref<AssetEventRow[]>([])
  const lookupReady = ref(false)
  const deptLabelMap = ref<Record<string, string>>({})
  const locationLabelMap = ref<Record<string, string>>({})
  const userLabelMap = ref<Record<string, string>>({})

  const snapshotFieldLabels: Record<string, string> = {
    assetCode: '资产编码',
    assetName: '资产名称',
    assetStatus: '资产状态',
    useOrgDeptId: '使用部门',
    manageDeptId: '管理部门',
    currentUserId: '责任人',
    currentLocationId: '当前位置',
    inboundDate: '入库日期',
    startUseDate: '启用日期',
    versionNo: '版本号'
  }

  const visible = computed({
    get: () => props.modelValue,
    set: (value: boolean) => emit('update:modelValue', value)
  })

  const drawerTitle = computed(() => {
    if (!props.assetData?.assetName) {
      return '资产流水'
    }
    return `资产流水 - ${props.assetData.assetName}`
  })

  const showMoreHint = computed(() => eventList.value.length >= EVENT_LIMIT)

  /**
   * 扁平化树节点，便于把快照里的 ID 翻译成当前可读名称。
   */
  const flattenTreeLabels = (nodes: TreeOption[], map: Record<string, string>) => {
    nodes.forEach((node) => {
      map[String(node.id)] = node.label
      if (node.children?.length) {
        flattenTreeLabels(node.children, map)
      }
    })
  }

  const displayText = (value: unknown) => {
    if (value === null || value === undefined || value === '') {
      return '-'
    }
    return String(value)
  }

  const formatDictLabel = (options: any[], value: unknown) => {
    const target = value === null || value === undefined ? '' : String(value)
    return options.find((item) => String(item.value) === target)?.label || displayText(value)
  }

  const formatSourceType = (sourceOrderType?: string) => {
    if (!sourceOrderType) {
      return '系统事件'
    }
    if (sourceOrderType === 'INVENTORY_TASK') {
      return '盘点任务'
    }
    return formatDictLabel(asset_order_type.value, sourceOrderType)
  }

  const parseSnapshot = (snapshot?: string) => {
    if (!snapshot) {
      return {}
    }
    if (typeof snapshot !== 'string') {
      return snapshot as Record<string, unknown>
    }
    try {
      return JSON.parse(snapshot) as Record<string, unknown>
    } catch (error) {
      console.error('解析资产流水快照失败:', error)
      return {}
    }
  }

  const formatSnapshotValue = (key: string, value: unknown) => {
    if (value === null || value === undefined || value === '') {
      return '-'
    }

    if (key === 'assetStatus') {
      return formatDictLabel(asset_status.value, value)
    }

    if (key === 'useOrgDeptId' || key === 'manageDeptId') {
      return deptLabelMap.value[String(value)] || displayText(value)
    }

    if (key === 'currentUserId') {
      return userLabelMap.value[String(value)] || displayText(value)
    }

    if (key === 'currentLocationId') {
      return locationLabelMap.value[String(value)] || displayText(value)
    }

    return displayText(value)
  }

  /**
   * 把快照差异压缩成可扫读的关键字段变化，抽屉里重点看这一段即可。
   */
  const buildChangeItems = (row: Partial<AssetEventRow>) => {
    const beforeSnapshot = parseSnapshot(row.beforeSnapshot)
    const afterSnapshot = parseSnapshot(row.afterSnapshot)
    const keys = Array.from(
      new Set([...Object.keys(beforeSnapshot), ...Object.keys(afterSnapshot)])
    )

    return keys
      .filter((key) => snapshotFieldLabels[key])
      .filter((key) => {
        const beforeValue = beforeSnapshot[key]
        const afterValue = afterSnapshot[key]
        return JSON.stringify(beforeValue ?? null) !== JSON.stringify(afterValue ?? null)
      })
      .map((key) => ({
        key,
        label: snapshotFieldLabels[key],
        beforeValue: formatSnapshotValue(key, beforeSnapshot[key]),
        afterValue: formatSnapshotValue(key, afterSnapshot[key])
      }))
  }

  /**
   * 抽屉打开后只加载一次组织、人和位置映射，后续资产切换直接复用。
   */
  const ensureLookupReady = async () => {
    if (lookupReady.value) {
      return
    }

    try {
      const [deptRes, userRes, locationRes] = await Promise.all([
        deptTreeSelect(),
        listUser({ pageNum: 1, pageSize: 200 }),
        treeLocationSelect()
      ])

      const deptData = Array.isArray(deptRes) ? deptRes : (deptRes as any)?.data || []
      const locationData = Array.isArray(locationRes)
        ? locationRes
        : (locationRes as any)?.data || []
      const userData = Array.isArray(userRes)
        ? userRes
        : (userRes as any)?.rows || (userRes as any)?.data || []

      const deptMap: Record<string, string> = {}
      const locationMap: Record<string, string> = {}
      flattenTreeLabels(deptData, deptMap)
      flattenTreeLabels(locationData, locationMap)

      deptLabelMap.value = deptMap
      locationLabelMap.value = locationMap
      userLabelMap.value = userData.reduce((result: Record<string, string>, item: any) => {
        result[String(item.userId)] = item.nickName || item.userName || String(item.userId)
        return result
      }, {})
      lookupReady.value = true
    } catch (error) {
      console.error('加载资产流水映射数据失败:', error)
      deptLabelMap.value = {}
      locationLabelMap.value = {}
      userLabelMap.value = {}
      lookupReady.value = true
    }
  }

  const loadEvents = async () => {
    const assetId = props.assetData?.assetId
    if (!assetId) {
      eventList.value = []
      loadError.value = ''
      return
    }

    loading.value = true
    loadError.value = ''

    try {
      await ensureLookupReady()
      const response: any = await getAssetEventByAssetId(assetId, { limit: EVENT_LIMIT })
      const rows = Array.isArray(response) ? response : response?.data || []
      eventList.value = rows.map((item: any) => ({
        ...item,
        changeItems: buildChangeItems(item)
      }))
    } catch (error: any) {
      console.error('加载资产流水失败:', error)
      eventList.value = []
      loadError.value = error?.message || '加载资产流水失败，请稍后重试'
    } finally {
      loading.value = false
    }
  }

  /**
   * 抽屉内只看最近记录，完整审计链路统一跳到独立流水页继续筛选。
   */
  const openFullPage = async () => {
    if (!props.assetData?.assetId) {
      return
    }

    visible.value = false
    await router.push({
      path: '/asset/event',
      query: {
        assetId: String(props.assetData.assetId),
        assetCode: props.assetData.assetCode || '',
        assetName: props.assetData.assetName || ''
      }
    })
  }

  watch(
    () => [props.modelValue, props.assetData?.assetId],
    ([opened]) => {
      if (opened) {
        void loadEvents()
      }
    },
    { immediate: true }
  )
</script>

<style lang="scss" scoped>
  .asset-event-drawer {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .asset-event-drawer__toolbar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    margin-bottom: 4px;
  }

  .asset-event-drawer__tip {
    color: var(--el-text-color-secondary);
    font-size: 12px;
  }

  .asset-event-drawer__card {
    border-radius: 12px;
  }

  .asset-event-drawer__meta {
    margin-bottom: 10px;
  }

  .asset-event-drawer__operator {
    color: var(--el-text-color-secondary);
    font-size: 12px;
  }

  .asset-event-drawer__desc {
    line-height: 1.7;
    color: var(--el-text-color-primary);
  }

  .asset-event-drawer__changes {
    margin-top: 12px;
    padding-top: 12px;
    border-top: 1px solid var(--art-gray-200);
  }

  .asset-event-drawer__changes-title {
    margin-bottom: 8px;
    font-weight: 600;
    color: var(--el-text-color-primary);
  }

  .asset-event-drawer__change-row {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 12px;
    padding: 6px 0;
  }

  .asset-event-drawer__change-label {
    min-width: 76px;
    color: var(--el-text-color-secondary);
  }

  .asset-event-drawer__change-value {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: flex-end;
    gap: 8px;
    text-align: right;
    color: var(--el-text-color-primary);
    word-break: break-all;
  }

  .asset-event-drawer__arrow {
    color: var(--el-color-primary);
  }
</style>
