<template>
  <div class="asset-order-workbench art-full-height" v-loading="loading">
    <ElCard class="asset-order-workbench__hero" shadow="never">
      <div class="asset-order-workbench__hero-main">
        <div>
          <div class="asset-order-workbench__eyebrow">{{ eyebrow }}</div>
          <h1 class="asset-order-workbench__title">{{ title }}</h1>
          <p class="asset-order-workbench__desc">{{ description }}</p>
        </div>

        <div class="asset-order-workbench__hero-status">
          <!-- 顶部状态区只承载流程标签，不再耦合内部编辑器行为。 -->
          <slot name="status" />
        </div>
      </div>
    </ElCard>

    <div v-if="$slots['draft-tip']" class="asset-order-workbench__draft-tip">
      <slot name="draft-tip" />
    </div>

    <div class="asset-order-workbench__editor">
      <!-- 主编辑区由父级注入，壳组件只负责统一骨架和节奏。 -->
      <slot name="editor" />
    </div>

    <div class="asset-order-workbench__footer">
      <!-- 底部动作区统一收口，避免不同入口各自拼按钮布局。 -->
      <slot name="footer" />
    </div>
  </div>
</template>

<script setup lang="ts">
  defineOptions({ name: 'OrderWorkbenchShell' })

  defineProps<{
    loading?: boolean
    eyebrow: string
    title: string
    description: string
  }>()
</script>

<style lang="scss" scoped>
  .asset-order-workbench {
    min-height: 100%;
    padding: 16px;
    display: flex;
    flex-direction: column;
    gap: 16px;
    background:
      radial-gradient(circle at top right, rgb(17 24 39 / 5%), transparent 35%),
      linear-gradient(180deg, rgb(248 250 252), rgb(255 255 255));
  }

  .asset-order-workbench__hero {
    border-radius: 18px;
    border-color: rgb(15 23 42 / 8%);
  }

  .asset-order-workbench__hero-main {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 16px;
  }

  .asset-order-workbench__eyebrow {
    margin-bottom: 8px;
    font-size: 12px;
    font-weight: 600;
    letter-spacing: 0.12em;
    text-transform: uppercase;
    color: var(--el-color-primary);
  }

  .asset-order-workbench__title {
    margin: 0;
    font-size: 28px;
    line-height: 1.2;
    color: rgb(15 23 42);
  }

  .asset-order-workbench__desc {
    margin: 12px 0 0;
    max-width: 760px;
    line-height: 1.7;
    color: rgb(71 85 105);
  }

  .asset-order-workbench__draft-tip {
    border-radius: 16px;
  }

  .asset-order-workbench__editor {
    margin-bottom: 0;
  }

  .asset-order-workbench__footer {
    position: sticky;
    bottom: 0;
    z-index: 10;
    padding: 14px 18px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
    border: 1px solid rgb(15 23 42 / 8%);
    border-radius: 18px;
    background: rgb(255 255 255 / 92%);
    backdrop-filter: blur(14px);
  }

  @media (max-width: 768px) {
    .asset-order-workbench {
      padding: 12px;
    }

    .asset-order-workbench__hero-main,
    .asset-order-workbench__footer {
      flex-direction: column;
      align-items: flex-start;
    }

    .asset-order-workbench__footer {
      position: static;
    }
  }
</style>
