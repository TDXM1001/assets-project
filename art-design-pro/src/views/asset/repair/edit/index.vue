<template>
  <AssetPageShell
    :loading="submitLoading"
    eyebrow="资产维修单"
    title="编辑维修单"
    description="继续补齐当前维修单后，再提交审批和完工处理。"
  >
    <template #tags>
      <ElSpace wrap>
        <ElTag type="info" effect="light">独立页面</ElTag>
        <ElTag type="success" effect="light">编辑模式</ElTag>
        <ElTag type="info" effect="plain">多资产明细</ElTag>
      </ElSpace>
    </template>

    <RepairWorkbenchPage
      ref="repairEditorRef"
      :context="pageContext"
      @success="handleEditorSuccess"
    />

    <template #footer>
      <ElSpace wrap>
        <ElButton @click="handleCancel">取消返回</ElButton>
      </ElSpace>
      <ElSpace wrap>
        <ElButton type="primary" plain :loading="submitLoading" @click="handleSaveContinue">
          保存修改
        </ElButton>
        <ElButton type="primary" :loading="submitLoading" @click="handleSubmitAndBack">
          提交并返回
        </ElButton>
      </ElSpace>
    </template>
  </AssetPageShell>
</template>

<script setup lang="ts">
  import { computed, ref } from 'vue'
  import { ElMessageBox } from 'element-plus'
  import { useRoute, useRouter } from 'vue-router'
  import AssetPageShell from '../../shared/asset-page-shell.vue'
  import RepairWorkbenchPage from '../create/repair-workbench-page.vue'
  import { normalizeRepairPageContext, type RepairPageContext } from '../modules/repair-page-context'

  defineOptions({ name: 'AssetRepairEdit' })

  interface RepairWorkbenchPageExpose {
    saveDraftOrder: () => Promise<any>
    submitFlowOrder: () => Promise<any>
  }

  const route = useRoute()
  const router = useRouter()

  const submitLoading = ref(false)
  const repairEditorRef = ref<RepairWorkbenchPageExpose>()

  const repairId = computed(() => {
    const rawRepairId = route.params.repairId || route.query.repairId
    return String(Array.isArray(rawRepairId) ? rawRepairId[0] : rawRepairId || '')
  })

  const pageContext = computed<RepairPageContext>(() =>
    normalizeRepairPageContext({
      pageMode: 'edit',
      repairId: repairId.value
    })
  )

  const backToDetailOrList = () => {
    if (repairId.value) {
      router.push(`/asset/repair/detail/${repairId.value}`).catch(() => undefined)
      return
    }
    router.push('/asset/repair').catch(() => undefined)
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
        backToDetailOrList()
      }
    } finally {
      submitLoading.value = false
    }
  }

  const handleCancel = async () => {
    try {
      await ElMessageBox.confirm('确认取消并返回吗？未保存的修改将会丢失。', '提示', {
        type: 'warning',
        confirmButtonText: '确定返回',
        cancelButtonText: '继续编辑'
      })
    } catch {
      return
    }
    backToDetailOrList()
  }

  const handleEditorSuccess = () => {
    // 基础保存成功通常由内部处理提示，这里可以做全局后续动作
  }
</script>
