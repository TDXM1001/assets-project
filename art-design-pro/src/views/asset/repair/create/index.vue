<template>
  <AssetPageShell
    :loading="submitLoading || draftSaving"
    eyebrow="资产维修单"
    title="新建维修单"
    description="把报修时间、维修方式和供应商信息录稳，后续审批、完工和附件都会沿用这张单据。"
  >
    <template #tags>
      <ElSpace wrap>
        <ElTag type="info" effect="light">独立页面</ElTag>
        <ElTag type="warning" effect="light">草稿阶段</ElTag>
        <ElTag type="info" effect="plain">多资产明细</ElTag>
      </ElSpace>
    </template>

    <template #draftTip>
      <ElAlert
        v-if="savedDraftSummary"
        class="asset-repair-create-page__draft-tip"
        type="info"
        :closable="false"
        show-icon
        title="检测到本地草稿"
      >
        <template #default>
          <div class="asset-repair-create-page__draft-actions">
            <span>{{ savedDraftSummary }}</span>
            <ElSpace wrap>
              <ElButton link type="primary" @click="handleRestoreDraft">恢复草稿</ElButton>
              <ElButton link type="danger" @click="handleClearDraft">清空草稿</ElButton>
            </ElSpace>
          </div>
        </template>
      </ElAlert>
    </template>

    <RepairWorkbenchPage
      ref="repairEditorRef"
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
  import AssetPageShell from '../../shared/asset-page-shell.vue'
  import RepairWorkbenchPage from './repair-workbench-page.vue'
  import {
    buildRepairDraftScope,
    buildRepairDraftStorageKey,
    normalizeRepairPageContext,
    readRepairPageContextFromStorage,
    safeParseRepairPageContext,
    type RepairPageContext
  } from '../modules/repair-page-context'

  defineOptions({ name: 'AssetRepairCreate' })

  interface LocalDraftPayload {
    savedAt: string
    scope: string
    pageContext: Record<string, any>
    formData: Record<string, any>
  }

  interface RepairWorkbenchPageExpose {
    saveDraftOrder: () => Promise<any>
    submitFlowOrder: () => Promise<any>
    getDraftPayload: () => Record<string, any> | undefined
    applyDraftPayload: (payload?: Record<string, any>) => void
  }

  // 为保持兼容性，创建页认这个老键，以便用户找回未保存的内容
  const LEGACY_DRAFT_KEY = 'asset-repair-create-draft-snapshot'

  const route = useRoute()
  const router = useRouter()

  const submitLoading = ref(false)
  const draftSaving = ref(false)
  const savedDraft = ref<LocalDraftPayload | null>(null)
  const repairEditorRef = ref<RepairWorkbenchPageExpose>()

  const rawBridgeSourceQuery = computed(() =>
    typeof route.query.bridgeSource === 'string' ? route.query.bridgeSource : ''
  )
  const rawBridgeKeyQuery = computed(() =>
    typeof route.query.bridgeKey === 'string' ? route.query.bridgeKey : ''
  )
  const rawBridgeDataQuery = computed(() =>
    typeof route.query.bridgeData === 'string' ? route.query.bridgeData : ''
  )
  const rawAssetIdQuery = computed(() =>
    typeof route.query.assetId === 'string' ? route.query.assetId : ''
  )

  const pageContext = computed<RepairPageContext>(() => {
    const queryBridgeKey = rawBridgeKeyQuery.value
    const parsedBridgeContext =
      safeParseRepairPageContext(rawBridgeDataQuery.value) ||
      readRepairPageContextFromStorage(queryBridgeKey)

    return normalizeRepairPageContext({
      pageMode: 'create',
      bridgeSource: rawBridgeSourceQuery.value,
      bridgeKey: queryBridgeKey,
      assetId: rawAssetIdQuery.value,
      ...(parsedBridgeContext || {})
    })
  })

  const draftStorageKey = computed(() => buildRepairDraftStorageKey(pageContext.value))
  const draftScope = computed(() => buildRepairDraftScope(pageContext.value))

  const savedDraftSummary = computed(() =>
    savedDraft.value ? `上次本地保存：${savedDraft.value.savedAt}` : ''
  )

  const parseDraftPayload = (rawDraft: string | null) => {
    if (!rawDraft) return null
    try {
      return JSON.parse(rawDraft)
    } catch (error) {
      console.error('解析方案本地草稿失败:', error)
      return null
    }
  }

  const isCompatibleDraft = (draft: LocalDraftPayload | null) => {
    if (!draft) return false
    // scope 匹配或者旧版本无 scope 且也是创建模式
    if (draft.scope) return draft.scope === draftScope.value
    return !pageContext.value.bridgeSource
  }

  const readSavedDraft = () => {
    const scopedDraft = parseDraftPayload(window.localStorage.getItem(draftStorageKey.value))
    if (isCompatibleDraft(scopedDraft)) {
      savedDraft.value = scopedDraft
      return
    }

    const legacyDraft = parseDraftPayload(window.localStorage.getItem(LEGACY_DRAFT_KEY))
    if (isCompatibleDraft(legacyDraft)) {
      savedDraft.value = legacyDraft
      return
    }

    savedDraft.value = null
  }

  const clearDraftStorage = () => {
    window.localStorage.removeItem(draftStorageKey.value)
    window.localStorage.removeItem(LEGACY_DRAFT_KEY)
    savedDraft.value = null
  }

  const handleSaveDraft = async () => {
    draftSaving.value = true
    try {
      const payload = repairEditorRef.value?.getDraftPayload?.()
      if (!payload) {
        ElMessage.warning('当前暂无内容可保存')
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
    repairEditorRef.value?.applyDraftPayload?.(savedDraft.value.formData)
    ElMessage.success('已恢复本地草稿')
  }

  const handleClearDraft = async () => {
    try {
      await ElMessageBox.confirm('确认清空本地草稿吗？', '提示', { type: 'warning' })
    } catch {
      return
    }
    clearDraftStorage()
    ElMessage.success('草稿已清空')
  }

  const backToList = () => {
    router.push({ path: '/asset/repair' }).catch(() => undefined)
  }

  const handleSaveContinue = async () => {
    submitLoading.value = true
    try {
      await repairEditorRef.value?.saveDraftOrder?.()
    } finally {
      submitLoading.value = false
    }
  }

  const handleSubmitAndBack = async () => {
    submitLoading.value = true
    try {
      const result = await repairEditorRef.value?.submitFlowOrder?.()
      if (result?.repairId) {
        clearDraftStorage()
        backToList()
      }
    } finally {
      submitLoading.value = false
    }
  }

  const handleCancel = async () => {
    try {
      await ElMessageBox.confirm('确认取消并返回吗？未保存的内容可以先保存为本地草稿。', '提示', {
        type: 'warning',
        confirmButtonText: '确定返回',
        cancelButtonText: '继续编辑'
      })
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
  .asset-repair-create-page__draft-tip {
    border-radius: 16px;
    margin-bottom: 16px;
  }

  .asset-repair-create-page__draft-actions {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
  }
</style>
