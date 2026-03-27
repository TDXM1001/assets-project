<template>
  <RepairWorkbenchPage
    :page-mode="true"
    dialog-type="edit"
    :repair-data="repairData"
    @success="handleSuccess"
    @cancel="handleCancel"
  />
</template>

<script setup lang="ts">
  import { computed } from 'vue'
  import { useRoute, useRouter } from 'vue-router'
  import RepairWorkbenchPage from '../create/repair-workbench-page.vue'

  defineOptions({ name: 'AssetRepairEditPage' })

  const route = useRoute()
  const router = useRouter()

  const repairId = computed(() => {
    const rawRepairId = route.params.repairId || route.query.repairId
    return Number(Array.isArray(rawRepairId) ? rawRepairId[0] : rawRepairId || 0)
  })

  const repairData = computed(() =>
    repairId.value ? { repairId: repairId.value } : ({} as Record<string, any>)
  )

  const handleSuccess = () => {
    if (repairId.value) {
      router.push(`/asset/repair/detail/${repairId.value}`)
      return
    }
    router.push('/asset/repair')
  }

  const handleCancel = () => {
    if (repairId.value) {
      router.push(`/asset/repair/detail/${repairId.value}`)
      return
    }
    router.push('/asset/repair')
  }
</script>
