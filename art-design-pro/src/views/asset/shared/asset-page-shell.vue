<template>
  <div class="asset-info-create-page art-full-height" v-loading="!!loading">
    <!-- 顶部英雄区：展示标题、描述和状态标签 -->
    <ElCard class="asset-info-create-page__hero" shadow="never">
      <div class="asset-info-create-page__hero-main">
        <div>
          <!--眉页：模块导航标记 -->
          <div class="asset-info-create-page__eyebrow">{{ eyebrow }}</div>
          <!--主标题 -->
          <h1 class="asset-info-create-page__title">{{ title }}</h1>
          <!--副描述 -->
          <p class="asset-info-create-page__desc">{{ description }}</p>
        </div>

        <!-- 状态标签插槽 -->
        <ElSpace wrap>
          <slot name="tags" />
        </ElSpace>
      </div>
    </ElCard>

    <!-- 草稿/异常提醒插槽 -->
    <slot name="draftTip" />

    <!-- 核心内容区 -->
    <div class="asset-info-create-page__content">
      <slot />
    </div>

    <!-- 吸底动作页脚 -->
    <div v-if="$slots.footer" class="asset-info-create-page__footer">
      <slot name="footer" />
    </div>
  </div>
</template>

<script setup lang="ts">
  /**
   * 资产页面统一壳组件 (AssetPageShell)
   * 
   * 提供资产管理模块内创建、详情、审批等页面的统一风格容器。
   */
  defineProps<{
    /** 是否正在加载 */
    loading?: boolean
    /** 眉头小标题 */
    eyebrow: string
    /** 页面主标题 */
    title: string
    /** 页面业务描述 */
    description: string
  }>()
</script>

<style scoped lang="scss">
  .asset-info-create-page {
    min-height: 100%;
    padding: 16px;
    display: flex;
    flex-direction: column;
    gap: 16px;
    background:
      radial-gradient(circle at top right, rgb(17 24 39 / 5%), transparent 35%),
      linear-gradient(180deg, rgb(248 250 252), rgb(255 255 255));
    animation: asset-page-enter 180ms ease-out both;
  }

  .asset-info-create-page__hero,
  .asset-info-create-page__content :deep(.el-card) {
    border-radius: 18px;
    border-color: rgb(15 23 42 / 8%);
  }

  .asset-info-create-page__hero-main {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 16px;
  }

  .asset-info-create-page__eyebrow {
    margin-bottom: 8px;
    font-size: 12px;
    font-weight: 600;
    letter-spacing: 0.12em;
    text-transform: uppercase;
    color: var(--el-color-primary);
  }

  .asset-info-create-page__title {
    margin: 0;
    font-size: 28px;
    line-height: 1.2;
    color: rgb(15 23 42);
  }

  .asset-info-create-page__desc {
    margin: 10px 0 0;
    max-width: 680px;
    color: rgb(71 85 105);
    line-height: 1.7;
  }

  .asset-info-create-page__content {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .asset-info-create-page__footer {
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
    .asset-info-create-page {
      padding: 12px;
    }

    .asset-info-create-page__hero-main,
    .asset-info-create-page__footer {
      flex-direction: column;
      align-items: stretch;
    }

    .asset-info-create-page__title {
      font-size: 24px;
    }
  }

  @keyframes asset-page-enter {
    from {
      opacity: 0;
      transform: translateY(8px);
    }

    to {
      opacity: 1;
      transform: translateY(0);
    }
  }
</style>
