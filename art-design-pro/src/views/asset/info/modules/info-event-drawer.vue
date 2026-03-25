<template>
  <AssetEventDrawer v-model="visible" :asset-data="assetData" />
</template>

<script setup lang="ts">
  import { computed } from 'vue'
  import AssetEventDrawer from '../../shared/asset-event-drawer.vue'

  defineOptions({ name: 'InfoEventDrawer' })

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
</script>
