<template>
  <RepairWorkbenchPage
    :page-mode="true"
    dialog-type="add"
    @success="handleSuccess"
    @cancel="handleCancel"
  />
</template>

<script setup lang="ts">
  import { useRouter } from 'vue-router'
  import RepairWorkbenchPage from './repair-workbench-page.vue'

  defineOptions({ name: 'AssetRepairCreatePageRoute' })

  const router = useRouter()

  const handleSuccess = () => {
    router.push('/asset/repair')
  }

  const handleCancel = () => {
    router.push('/asset/repair')
  }
</script>
