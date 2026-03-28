<template>
  <AssetPageShell
    :loading="submitLoading || draftSaving"
    eyebrow="业务单据"
    :title="pageTitle"
    :description="pageDescription"
  >
    <template #tags>
      <ElSpace wrap>
        <!-- 页面本体保持统一壳层，标签只表达当前工作台状态，不再承担页面布局职责。 -->
        <ElTag type="info" effect="light">独立页面</ElTag>
        <ElTag type="warning" effect="light">草稿阶段</ElTag>
        <ElTag type="info" effect="plain">{{ orderTypeLabel }}</ElTag>
      </ElSpace>
    </template>

    <template #draftTip>
      <ElAlert
        v-if="savedDraftSummary"
        class="asset-order-create-page__draft-tip"
        type="info"
        :closable="false"
        show-icon
        title="检测到本地草稿"
      >
        <template #default>
          <div class="asset-order-create-page__draft-actions">
            <span>{{ savedDraftSummary }}</span>
            <ElSpace wrap>
              <ElButton link type="primary" @click="handleRestoreDraft">恢复草稿</ElButton>
              <ElButton link type="danger" @click="handleClearDraft">清空草稿</ElButton>
            </ElSpace>
          </div>
        </template>
      </ElAlert>
    </template>

    <OrderWorkbenchPage
      ref="orderEditorRef"
      :context="pageContext"
      @success="handleEditorSuccess"
    />

    <template #footer>
      <ElSpace wrap>
        <ElButton @click="handleCancel">取消返回</ElButton>
        <ElButton :loading="draftSaving" @click="handleSaveDraft">保存草稿</ElButton>
      </ElSpace>
      <ElSpace wrap>
        <ElButton type="primary" plain :loading="submitLoading" @click="handleSaveContinue">
          保存并继续
        </ElButton>
        <ElButton type="primary" :loading="submitLoading" @click="handleSubmitAndBack">
          提交并返回
        </ElButton>
      </ElSpace>
    </template>
  </AssetPageShell>
</template>

<script setup lang="ts">
  import { computed, ref, watch } from 'vue'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import { useRoute, useRouter } from 'vue-router'
  import { useDict } from '@/utils/dict'
  import AssetPageShell from '../../shared/asset-page-shell.vue'
  import OrderWorkbenchPage from './order-workbench-page.vue'
  import {
    buildOrderWorkbenchDraftScope,
    buildOrderWorkbenchDraftStorageKey,
    resolveOrderWorkbenchPageContext,
    type OrderWorkbenchContext
  } from '../modules/order-workbench-context'
  import {
    buildOrderListRestoreQuery,
    resolveOrderListRestoreState
  } from '../modules/order-list-query'

  defineOptions({ name: 'AssetOrderCreate' })

  interface LocalDraftPayload {
    savedAt: string
    scope: string
    pageContext: Record<string, any>
    formData: Record<string, any>
  }

  interface OrderWorkbenchPageExpose {
    saveDraftOrder: () => Promise<any>
    submitFlowOrder: () => Promise<any>
    getDraftPayload: () => Record<string, any> | undefined
    applyDraftPayload: (payload?: Record<string, any>) => void
  }

  // 创建页继续兼容老草稿键，避免页面版上线后把已有本地草稿直接丢掉。
  const LOCAL_DRAFT_KEY = 'asset-order-create-draft'

  const route = useRoute()
  const router = useRouter()
  const { asset_order_type } = useDict('asset_order_type')

  const submitLoading = ref(false)
  const draftSaving = ref(false)
  const savedDraft = ref<LocalDraftPayload | null>(null)
  const orderEditorRef = ref<OrderWorkbenchPageExpose>()

  const rawOrderTypeQuery = computed(() =>
    typeof route.query.orderType === 'string' ? route.query.orderType : ''
  )
  const rawBridgeSourceQuery = computed(() =>
    typeof route.query.bridgeSource === 'string' ? route.query.bridgeSource : ''
  )
  const rawBridgeKeyQuery = computed(() =>
    typeof route.query.bridgeKey === 'string' ? route.query.bridgeKey : ''
  )
  const rawBridgeDataQuery = computed(() =>
    typeof route.query.bridgeData === 'string' ? route.query.bridgeData : ''
  )
  const rawSourceBizTypeQuery = computed(() =>
    typeof route.query.sourceBizType === 'string' ? route.query.sourceBizType : ''
  )
  const rawSourceBizIdQuery = computed(() =>
    typeof route.query.sourceBizId === 'string' ? route.query.sourceBizId : ''
  )
  const rawRepairIdQuery = computed(() =>
    typeof route.query.repairId === 'string' ? route.query.repairId : ''
  )

  // 页面只认这一份标准化上下文，避免不同入口把创建页带成不同语义。
  const pageContext = computed<OrderWorkbenchContext>(() =>
    resolveOrderWorkbenchPageContext({
      orderTypeQuery: rawOrderTypeQuery.value,
      bridgeSourceQuery: rawBridgeSourceQuery.value,
      bridgeKeyQuery: rawBridgeKeyQuery.value,
      bridgeDataQuery: rawBridgeDataQuery.value,
      sourceBizTypeQuery: rawSourceBizTypeQuery.value,
      sourceBizIdQuery: rawSourceBizIdQuery.value,
      repairIdQuery: rawRepairIdQuery.value
    })
  )

  const draftStorageKey = computed(() => buildOrderWorkbenchDraftStorageKey(pageContext.value))
  const draftScope = computed(() => buildOrderWorkbenchDraftScope(pageContext.value))

  const pageTitle = computed(() =>
    pageContext.value.orderType === 'DISPOSAL' ? '新建报废单' : '新建业务单据'
  )
  const pageDescription = computed(() =>
    pageContext.value.orderType === 'DISPOSAL'
      ? '先补齐报废原因、处置金额和资产明细，再把整张单据提交到正式流程。'
      : '把流转方向、归属字段和资产明细一次录稳，后续审批、完成和事件流水都会沿用这张单据。'
  )
  const orderTypeLabel = computed(() => {
    const matched = (asset_order_type.value || []).find(
      (item: any) => String(item.value) === String(pageContext.value.orderType)
    )
    return matched?.label || pageContext.value.orderType || '业务单据'
  })
  const savedDraftSummary = computed(() =>
    savedDraft.value ? `上次草稿保存时间：${savedDraft.value.savedAt}` : ''
  )

  const parseDraftPayload = (rawDraft: string | null) => {
    if (!rawDraft) return null

    try {
      return JSON.parse(rawDraft)
    } catch (error) {
      console.error('解析单据本地草稿失败:', error)
      return null
    }
  }

  const isCompatibleDraft = (draft: LocalDraftPayload | null) => {
    if (!draft) return false
    if (draft.scope) return draft.scope === draftScope.value

    const draftOrderType = String(draft.formData?.orderType || '').toUpperCase()
    return draftOrderType === pageContext.value.orderType && !pageContext.value.bridgeSource
  }

  const readSavedDraft = () => {
    const scopedDraft = parseDraftPayload(window.localStorage.getItem(draftStorageKey.value))
    if (isCompatibleDraft(scopedDraft)) {
      savedDraft.value = scopedDraft
      return
    }

    const legacyDraft = parseDraftPayload(window.localStorage.getItem(LOCAL_DRAFT_KEY))
    if (isCompatibleDraft(legacyDraft)) {
      const migratedDraft: LocalDraftPayload = {
        ...legacyDraft,
        scope: draftScope.value,
        pageContext: { ...pageContext.value }
      }
      window.localStorage.setItem(draftStorageKey.value, JSON.stringify(migratedDraft))
      savedDraft.value = migratedDraft
      return
    }

    savedDraft.value = null
  }

  const clearDraftStorage = () => {
    window.localStorage.removeItem(draftStorageKey.value)
    window.localStorage.removeItem(LOCAL_DRAFT_KEY)
    savedDraft.value = null
  }

  const handleSaveDraft = async () => {
    draftSaving.value = true
    try {
      const payload = orderEditorRef.value?.getDraftPayload?.()
      if (!payload) {
        ElMessage.warning('当前表单还没有可保存的内容')
        return
      }

      const nextDraft: LocalDraftPayload = {
        savedAt: new Date().toLocaleString(),
        scope: draftScope.value,
        pageContext: { ...pageContext.value },
        formData: payload
      }
      window.localStorage.setItem(draftStorageKey.value, JSON.stringify(nextDraft))
      savedDraft.value = nextDraft
      ElMessage.success('草稿已保存到本地')
    } finally {
      draftSaving.value = false
    }
  }

  const handleRestoreDraft = () => {
    if (!savedDraft.value?.formData) return
    orderEditorRef.value?.applyDraftPayload?.(savedDraft.value.formData)
    ElMessage.success('已恢复本地草稿')
  }

  const handleClearDraft = async () => {
    try {
      await ElMessageBox.confirm('确认清空本地草稿吗？清空后无法恢复。', '提示', {
        type: 'warning'
      })
    } catch {
      return
    }

    clearDraftStorage()
    ElMessage.success('草稿已清空')
  }

  const buildBackRouteQuery = () => {
    const restoredListQuery = buildOrderListRestoreQuery(resolveOrderListRestoreState(route.query))
    const queryOrderType = rawOrderTypeQuery.value.trim().toUpperCase()
    const preservedOrderType =
      queryOrderType || (pageContext.value.bridgeSource ? pageContext.value.orderType : '')
    const bridgeQuery: Record<string, string> = {}

    if (preservedOrderType && (preservedOrderType !== 'INBOUND' || queryOrderType)) {
      bridgeQuery.orderType = preservedOrderType
    }

    if (pageContext.value.bridgeSource) bridgeQuery.bridgeSource = pageContext.value.bridgeSource
    if (pageContext.value.bridgeKey) bridgeQuery.bridgeKey = pageContext.value.bridgeKey
    if (pageContext.value.sourceBizType) bridgeQuery.sourceBizType = pageContext.value.sourceBizType
    if (pageContext.value.sourceBizId) bridgeQuery.sourceBizId = pageContext.value.sourceBizId
    if (pageContext.value.repairId) bridgeQuery.repairId = pageContext.value.repairId

    return {
      ...bridgeQuery,
      ...restoredListQuery
    }
  }

  const backToList = () => {
    router.push({ path: '/asset/order', query: buildBackRouteQuery() }).catch(() => undefined)
  }

  const handleSaveContinue = async () => {
    submitLoading.value = true
    try {
      await orderEditorRef.value?.saveDraftOrder?.()
    } finally {
      submitLoading.value = false
    }
  }

  const handleSubmitAndBack = async () => {
    submitLoading.value = true
    try {
      const result = await orderEditorRef.value?.submitFlowOrder?.()
      if (result?.orderId) {
        clearDraftStorage()
        backToList()
      }
    } finally {
      submitLoading.value = false
    }
  }

  const handleCancel = async () => {
    try {
      await ElMessageBox.confirm(
        '确认取消并返回单据列表吗？未提交内容可以先保存为本地草稿。',
        '提示',
        {
          type: 'warning',
          confirmButtonText: '返回列表',
          cancelButtonText: '继续编辑'
        }
      )
    } catch {
      return
    }

    backToList()
  }

  const handleEditorSuccess = () => {
    if (savedDraft.value) {
      clearDraftStorage()
    }
  }

  watch(
    () => draftStorageKey.value,
    () => {
      readSavedDraft()
    },
    { immediate: true }
  )
</script>

<style lang="scss" scoped>
  .asset-order-create-page__draft-tip {
    border-radius: 16px;
  }

  .asset-order-create-page__draft-actions {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
  }
</style>
