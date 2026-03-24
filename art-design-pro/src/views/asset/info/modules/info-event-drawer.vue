<template>
  <ElDrawer v-model="visible" :title="drawerTitle" size="520px" append-to-body destroy-on-close>
    <template v-if="assetData">
      <ElDescriptions :column="1" border class="mb-4">
        <ElDescriptionsItem label="资产编码">{{ assetData.assetCode || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="资产名称">{{ assetData.assetName || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="当前状态">
          <DictTag :options="asset_status" :value="assetData.assetStatus" />
        </ElDescriptionsItem>
      </ElDescriptions>

      <ElEmpty description="资产流水接口待接入，当前先保留阅读结构。">
        <template #image>
          <ElIcon size="48">
            <Document />
          </ElIcon>
        </template>
      </ElEmpty>
    </template>

    <ElEmpty v-else description="请先选择一条资产记录" />
  </ElDrawer>
</template>

<script setup lang="ts">
  import { computed } from 'vue'
  import { Document } from '@element-plus/icons-vue'
  import DictTag from '@/components/DictTag/index.vue'
  import { useDict } from '@/utils/dict'

  const { asset_status } = useDict('asset_status')

  const props = defineProps<{
    modelValue: boolean
    assetData?: any
  }>()

  const emit = defineEmits<{
    (e: 'update:modelValue', value: boolean): void
  }>()

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
</script>

<style scoped></style>
