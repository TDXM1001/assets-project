<template>
  <div class="asset-order-create-page art-full-height">
    <ElCard class="asset-order-create-page__hero" shadow="never">
      <div class="asset-order-create-page__hero-main">
        <div>
          <div class="asset-order-create-page__eyebrow">业务单据</div>
          <h1 class="asset-order-create-page__title">{{ pageTitle }}</h1>
          <p class="asset-order-create-page__desc">{{ pageDescription }}</p>
        </div>

        <ElSpace wrap>
          <ElTag type="warning" effect="light">草稿阶段</ElTag>
          <ElTag v-if="pageContext.orderType === 'DISPOSAL'" type="danger" effect="light">
            报废流程
          </ElTag>
          <ElTag v-else type="info" effect="light">
            {{ orderTypeLabel }}
          </ElTag>
        </ElSpace>
      </div>
    </ElCard>

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

    <div class="asset-order-create-page__editor">
      <OrderDialog
        ref="orderEditorRef"
        v-model="editorVisible"
        page-mode
        dialog-type="add"
        :dialog-context="pageContext"
        @success="handleEditorSuccess"
      />
    </div>

    <div class="asset-order-create-page__footer">
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
    </div>
  </div>
</template>

<script setup lang="ts">
  import { computed, ref, watch } from 'vue'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import { useRoute, useRouter } from 'vue-router'
  import { useDict } from '@/utils/dict'
  import OrderDialog from '../modules/order-dialog.vue'

  defineOptions({ name: 'AssetOrderCreate' })

  interface LocalDraftPayload {
    savedAt: string
    scope: string
    pageContext: Record<string, any>
    formData: Record<string, any>
  }

  interface PageContext extends Record<string, any> {
    orderType: string
    bridgeSource: string
    bridgeKey: string
    repairId: string
  }

  const LOCAL_DRAFT_KEY = 'asset-order-create-draft'

  const route = useRoute()
  const router = useRouter()
  const { asset_order_type } = useDict('asset_order_type')

  const editorVisible = ref(true)
  const submitLoading = ref(false)
  const draftSaving = ref(false)
  const savedDraft = ref<LocalDraftPayload | null>(null)
  const orderEditorRef = ref<any>()

  const safeParseBridgeContext = (value: string | null) => {
    if (!value) {
      return undefined
    }

    try {
      const parsed = JSON.parse(value)
      return parsed && typeof parsed === 'object' ? parsed : undefined
    } catch (error) {
      console.error('解析单据桥接上下文失败:', error)
      return undefined
    }
  }

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
  const rawRepairIdQuery = computed(() =>
    typeof route.query.repairId === 'string' ? route.query.repairId : ''
  )

  const readBridgeContextFromStorage = (bridgeKey?: string) => {
    const storageKeys = bridgeKey
      ? [
          `asset-order-disposal-bridge:${bridgeKey}`,
          `asset.order.disposal.bridge:${bridgeKey}`,
          'asset-order-disposal-bridge',
          'asset.order.disposal.bridge',
          'asset.order.bridge.payload'
        ]
      : ['asset-order-disposal-bridge', 'asset.order.disposal.bridge', 'asset.order.bridge.payload']

    for (const storageKey of storageKeys) {
      const storedValue = sessionStorage.getItem(storageKey)
      if (!storedValue) {
        continue
      }
      sessionStorage.removeItem(storageKey)
      const parsed = safeParseBridgeContext(storedValue)
      if (parsed) {
        return parsed
      }
    }

    return undefined
  }

  const normalizeBridgeContext = (context: Record<string, any> = {}): PageContext => {
    const orderType = String(context.orderType || '').toUpperCase()
    return {
      ...context,
      orderType: orderType || 'INBOUND',
      bridgeSource: String(context.bridgeSource || ''),
      bridgeKey: String(context.bridgeKey || ''),
      repairId: String(context.repairId || '')
    }
  }

  const buildDraftScope = (context: Record<string, any> = {}) => {
    const orderType = String(context.orderType || 'INBOUND').toUpperCase()
    const bridgeSource = String(context.bridgeSource || 'manual')
    const sourceBizType = String(context.sourceBizType || '')
    const sourceBizId =
      context.sourceBizId === undefined || context.sourceBizId === null
        ? ''
        : String(context.sourceBizId)
    const repairId = String(context.repairId || '')

    return [orderType, bridgeSource, sourceBizType, sourceBizId, repairId].join('|')
  }

  const pageContext = computed<PageContext>(() => {
    const queryOrderType = rawOrderTypeQuery.value
    const queryBridgeKey = rawBridgeKeyQuery.value
    const queryBridgeData = rawBridgeDataQuery.value
    const parsedBridgeContext =
      safeParseBridgeContext(queryBridgeData) || readBridgeContextFromStorage(queryBridgeKey)

    const pageRouteContext = {
      orderType: queryOrderType,
      bridgeSource: rawBridgeSourceQuery.value,
      bridgeKey: queryBridgeKey,
      repairId: rawRepairIdQuery.value
    }

    return normalizeBridgeContext({
      ...pageRouteContext,
      ...(parsedBridgeContext || {})
    })
  })

  const draftStorageKey = computed(
    () => `asset-order-create-draft:${buildDraftScope(pageContext.value)}`
  )
  const draftScope = computed(() => buildDraftScope(pageContext.value))

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
    if (!rawDraft) {
      return null
    }

    try {
      return JSON.parse(rawDraft)
    } catch (error) {
      console.error('解析单据本地草稿失败:', error)
      return null
    }
  }

  const isCompatibleDraft = (draft: LocalDraftPayload | null) => {
    if (!draft) {
      return false
    }

    if (draft.scope) {
      return draft.scope === draftScope.value
    }

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

  const handleRestoreDraft = async () => {
    if (!savedDraft.value?.formData) {
      return
    }

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
    const queryOrderType = rawOrderTypeQuery.value.trim().toUpperCase()
    const preservedOrderType =
      queryOrderType || (pageContext.value.bridgeSource ? pageContext.value.orderType : '')
    const query: Record<string, string> = {}

    if (preservedOrderType && (preservedOrderType !== 'INBOUND' || queryOrderType)) {
      query.orderType = preservedOrderType
    }

    return query
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
    // 页面模式下只同步草稿状态，不在这里主动跳转，交互由底部操作区控制。
    if (savedDraft.value) {
      clearDraftStorage()
    }
  }

  watch(
    () => editorVisible.value,
    (value) => {
      if (!value) {
        backToList()
      }
    }
  )

  readSavedDraft()
</script>

<style lang="scss" scoped>
  .asset-order-create-page {
    min-height: 100%;
    padding: 16px;
    background:
      radial-gradient(circle at top right, rgb(14 165 233 / 10%), transparent 24%),
      linear-gradient(180deg, #f8fafc 0%, #eef2ff 100%);
  }

  .asset-order-create-page__hero {
    margin-bottom: 16px;
    border: none;
    background: linear-gradient(135deg, rgb(15 23 42 / 96%), rgb(30 41 59 / 92%));
    color: #fff;
  }

  .asset-order-create-page__hero-main {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 16px;
  }

  .asset-order-create-page__eyebrow {
    margin-bottom: 8px;
    font-size: 13px;
    letter-spacing: 0.16em;
    color: rgb(191 219 254 / 88%);
  }

  .asset-order-create-page__title {
    margin: 0;
    font-size: 28px;
    line-height: 1.2;
  }

  .asset-order-create-page__desc {
    margin: 12px 0 0;
    max-width: 760px;
    line-height: 1.7;
    color: rgb(226 232 240 / 88%);
  }

  .asset-order-create-page__draft-tip {
    margin-bottom: 16px;
  }

  .asset-order-create-page__draft-actions {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
  }

  .asset-order-create-page__editor {
    margin-bottom: 16px;

    :deep(.el-overlay) {
      position: static;
      background: transparent;
      overflow: visible;
    }

    :deep(.el-overlay-dialog) {
      position: static;
      display: block;
      overflow: visible;
    }

    :deep(.el-dialog) {
      width: 100% !important;
      margin: 0 !important;
      border-radius: 20px;
      box-shadow: 0 24px 60px rgb(15 23 42 / 12%);
    }
  }

  .asset-order-create-page__footer {
    position: sticky;
    bottom: 0;
    z-index: 10;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
    padding: 16px 20px;
    border: 1px solid rgb(226 232 240 / 90%);
    border-radius: 18px;
    background: rgb(255 255 255 / 92%);
    backdrop-filter: blur(12px);
  }

  @media (max-width: 768px) {
    .asset-order-create-page {
      padding: 12px;
    }

    .asset-order-create-page__hero-main,
    .asset-order-create-page__draft-actions,
    .asset-order-create-page__footer {
      flex-direction: column;
      align-items: flex-start;
    }

    .asset-order-create-page__footer {
      position: static;
    }
  }
</style>
