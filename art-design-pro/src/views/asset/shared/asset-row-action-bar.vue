<template>
  <div v-if="visibleActions.length" class="asset-row-action-bar">
    <ElSpace wrap :size="8" class="asset-row-action-bar__space">
      <template v-for="item in directActions" :key="item.key">
        <ElButton
          link
          size="small"
          :type="item.type || 'primary'"
          :disabled="item.disabled"
          @click="handleActionClick(item)"
        >
          {{ item.label }}
        </ElButton>
      </template>

      <ArtButtonMore v-if="overflowActions.length" :list="overflowItems" @click="handleMoreClick" />
    </ElSpace>
  </div>
</template>

<script setup lang="ts">
  import { computed } from 'vue'
  import { ElButton, ElSpace } from 'element-plus'
  import ArtButtonMore, {
    type ButtonMoreItem
  } from '@/components/core/forms/art-button-more/index.vue'

  defineOptions({ name: 'AssetRowActionBar' })

  export interface AssetRowActionItem {
    /** 动作唯一标识 */
    key: string | number
    /** 按钮文案 */
    label: string
    /** 文本按钮语义色 */
    type?: 'primary' | 'success' | 'warning' | 'danger' | 'info'
    /** 下拉菜单图标 */
    icon?: string
    /** 下拉菜单文案颜色 */
    color?: string
    /** 是否禁用 */
    disabled?: boolean
    /** 点击动作 */
    onClick: () => void | Promise<void>
  }

  interface Props {
    /** 当前行可见动作，按优先级从高到低排序 */
    actions: AssetRowActionItem[]
    /** 直接展示的动作数量，默认 3 个 */
    maxDirect?: number
  }

  const props = withDefaults(defineProps<Props>(), {
    maxDirect: 3
  })

  const visibleActions = computed(() => props.actions.filter(Boolean))
  const directActions = computed(() => visibleActions.value.slice(0, props.maxDirect))
  const overflowActions = computed(() => visibleActions.value.slice(props.maxDirect))

  const overflowItems = computed<ButtonMoreItem[]>(() =>
    overflowActions.value.map((item) => ({
      key: item.key,
      label: item.label,
      disabled: item.disabled,
      icon: item.icon,
      color: item.color
    }))
  )

  const handleActionClick = (item: AssetRowActionItem) => {
    if (item.disabled) return
    void item.onClick()
  }

  const handleMoreClick = (item: ButtonMoreItem) => {
    const matched = overflowActions.value.find((action) => action.key === item.key)
    if (!matched || matched.disabled) return
    void matched.onClick()
  }
</script>

<style scoped>
  .asset-row-action-bar {
    display: flex;
    justify-content: flex-end;
    width: 100%;
  }

  .asset-row-action-bar__space {
    justify-content: flex-end;
  }
</style>
