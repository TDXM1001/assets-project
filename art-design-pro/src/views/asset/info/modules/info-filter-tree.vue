<template>
  <div class="info-filter-tree">
    <div class="info-filter-tree__header">
      <div>
        <div class="info-filter-tree__title">筛选范围</div>
        <div class="info-filter-tree__sub">在分类与位置之间切换浏览维度</div>
      </div>
      <ElTag size="small" effect="light" type="info">
        {{ mode === 'category' ? '按分类' : '按位置' }}
      </ElTag>
    </div>

    <div class="info-filter-tree__switch">
      <ElButton
        :type="mode === 'category' ? 'primary' : 'default'"
        @click="emit('switch-mode', 'category')"
      >
        按分类
      </ElButton>
      <ElButton
        :type="mode === 'location' ? 'primary' : 'default'"
        @click="emit('switch-mode', 'location')"
      >
        按位置
      </ElButton>
    </div>

    <ElInput
      v-model="filterText"
      clearable
      prefix-icon="Search"
      class="info-filter-tree__search"
      :placeholder="mode === 'category' ? '搜索分类节点' : '搜索位置节点'"
    />

    <div class="info-filter-tree__body">
      <ElScrollbar class="h-full">
        <ElTree
          ref="treeRef"
          :data="data"
          :props="{ label: 'label', children: 'children' }"
          node-key="id"
          highlight-current
          default-expand-all
          :expand-on-click-node="false"
          :filter-node-method="filterNode"
          :empty-text="mode === 'category' ? '暂无分类节点' : '暂无位置节点'"
          @node-click="handleNodeClick"
        />
      </ElScrollbar>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { ref, watch } from 'vue'

  interface TreeNode {
    id: number | string
    label: string
    children?: TreeNode[]
  }

  const props = defineProps<{
    mode: 'category' | 'location'
    data: TreeNode[]
    currentNodeKey?: number | string | null
  }>()

  const emit = defineEmits<{
    (e: 'switch-mode', value: 'category' | 'location'): void
    (e: 'node-click', value: TreeNode): void
    (e: 'update:currentNodeKey', value: number | string | null): void
  }>()

  const filterText = ref('')
  const treeRef = ref()

  watch(filterText, (value) => {
    treeRef.value?.filter(value)
  })

  watch(
    () => props.currentNodeKey,
    (value) => {
      if (value === null || value === undefined) {
        treeRef.value?.setCurrentKey(undefined)
        return
      }
      treeRef.value?.setCurrentKey(value)
    }
  )

  /**
   * 切换维度时清空搜索词，避免分类和位置两个维度互相串词。
   */
  watch(
    () => props.mode,
    () => {
      filterText.value = ''
    }
  )

  const filterNode = (value: string, data: TreeNode) => {
    if (!value) return true
    return data.label.includes(value)
  }

  const handleNodeClick = (node: TreeNode) => {
    treeRef.value?.setCurrentKey(node.id)
    emit('update:currentNodeKey', node.id)
    emit('node-click', node)
  }
</script>

<style lang="scss" scoped>
  .info-filter-tree {
    height: 100%;
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .info-filter-tree__header {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 12px;
  }

  .info-filter-tree__title {
    font-size: 15px;
    font-weight: 700;
    color: var(--art-text-gray-800);
  }

  .info-filter-tree__sub {
    margin-top: 4px;
    font-size: 12px;
    color: var(--art-text-gray-500);
  }

  .info-filter-tree__switch {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 8px;
  }

  .info-filter-tree__search {
    :deep(.el-input__wrapper) {
      background-color: var(--art-gray-200) !important;
      box-shadow: none !important;
      border: 1px solid transparent;
      border-radius: 8px;

      &.is-focus {
        border-color: var(--theme-color);
        background-color: var(--default-box-color) !important;
      }
    }
  }

  .info-filter-tree__body {
    flex: 1;
    overflow: hidden;

    :deep(.el-tree) {
      background: transparent;

      .el-tree-node__content {
        height: 38px;
        border-radius: 6px;

        &:hover {
          background-color: var(--art-gray-200) !important;
        }
      }

      .el-tree-node.is-current > .el-tree-node__content {
        background-color: var(--theme-color-light) !important;
        color: var(--theme-color);
      }
    }
  }
</style>
