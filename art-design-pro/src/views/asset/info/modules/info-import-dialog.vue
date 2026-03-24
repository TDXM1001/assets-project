<template>
  <ElDialog title="资产导入" v-model="visible" width="620px" destroy-on-close append-to-body>
    <ElResult
      icon="info"
      title="导入骨架已准备"
      sub-title="一期先保留导入口，后续接通后端批量导入接口即可继续完善。"
    >
      <template #extra>
        <ElAlert
          type="warning"
          :closable="false"
          show-icon
          title="当前状态"
          description="当前仓库里还没有资产导入后端接口，所以这里先作为一个可集成的占位组件。"
        />
      </template>
    </ElResult>

    <template #footer>
      <div class="dialog-footer">
        <ElButton @click="visible = false">我知道了</ElButton>
      </div>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import { computed } from 'vue'

  const props = defineProps<{
    modelValue: boolean
  }>()

  const emit = defineEmits<{
    (e: 'update:modelValue', value: boolean): void
  }>()

  const visible = computed({
    get: () => props.modelValue,
    set: (value: boolean) => emit('update:modelValue', value)
  })
</script>

<style scoped></style>
