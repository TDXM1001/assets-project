<template>
  <OrderWorkbenchShell eyebrow="业务单据" :title="pageTitle" :description="pageDescription">
    <template #status>
      <ElSpace wrap>
        <ElTag type="warning" effect="light">草稿阶段</ElTag>
        <ElTag v-if="pageContext.orderType === 'DISPOSAL'" type="danger" effect="light">
          报废流程
        </ElTag>
        <ElTag v-else type="info" effect="light">
          {{ orderTypeLabel }}
        </ElTag>
      </ElSpace>
    </template>

    <template #draft-tip>
      <ElAlert
        v-if="savedDraftSummary"
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

    <template #editor>
      <OrderDialog
        ref="orderEditorRef"
        v-model="editorVisible"
        page-mode
        dialog-type="add"
        :dialog-context="pageContext"
        @success="handleEditorSuccess"
      />
    </template>

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
  </OrderWorkbenchShell>
</template>

<script setup lang="ts">
  import { computed, ref, watch } from 'vue'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import { useRoute, useRouter } from 'vue-router'
  import { useDict } from '@/utils/dict'
  import OrderDialog from '../modules/order-dialog.vue'
  import OrderWorkbenchShell from '../modules/order-workbench-shell.vue'
  import {
    buildOrderWorkbenchDraftScope,
    buildOrderWorkbenchDraftStorageKey,
    normalizeOrderWorkbenchContext,
    readOrderWorkbenchContextFromStorage,
    safeParseOrderWorkbenchContext,
    type OrderWorkbenchContext
  } from '../modules/order-workbench-context'

  defineOptions({ name: 'AssetOrderCreate' })

  interface LocalDraftPayload {
    savedAt: string
    scope: string
    pageContext: Record<string, any>
    formData: Record<string, any>
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

  // 页面模式下把路由参数与 sessionStorage 桥接数据合并成统一工作台上下文。
  const pageContext = computed<OrderWorkbenchContext>(() => {
    const queryBridgeKey = rawBridgeKeyQuery.value
    const parsedBridgeContext =
      safeParseOrderWorkbenchContext(rawBridgeDataQuery.value) ||
      readOrderWorkbenchContextFromStorage(queryBridgeKey)

    return normalizeOrderWorkbenchContext({
      orderType: rawOrderTypeQuery.value,
      bridgeSource: rawBridgeSourceQuery.value,
      bridgeKey: queryBridgeKey,
      repairId: rawRepairIdQuery.value,
      ...(parsedBridgeContext || {})
    })
  })

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

  // 返回列表时保留原始单据类型，避免用户丢失当前筛选视角。
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
