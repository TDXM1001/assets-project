<template>
  <div class="asset-repair-create-page art-full-height">
    <ElCard class="asset-repair-create-page__hero" shadow="never">
      <div class="asset-repair-create-page__hero-main">
        <div>
          <div class="asset-repair-create-page__eyebrow">资产维修单</div>
          <h1 class="asset-repair-create-page__title">{{ pageTitle }}</h1>
          <p class="asset-repair-create-page__desc">{{ pageDescription }}</p>
        </div>

        <ElSpace wrap>
          <ElTag type="warning" effect="light">独立页面</ElTag>
          <ElTag type="info" effect="light">草稿阶段</ElTag>
        </ElSpace>
      </div>
    </ElCard>

    <RepairDialog
      v-model="dialogVisible"
      page-mode
      dialog-type="add"
      @success="handleSuccess"
      @cancel="handleCancel"
    />
  </div>
</template>

<script setup lang="ts">
  import { computed, ref } from 'vue'
  import { useRouter } from 'vue-router'
  import RepairDialog from '../modules/repair-dialog.vue'

  defineOptions({ name: 'AssetRepairCreatePage' })

  const router = useRouter()
  const dialogVisible = ref(true)

  const pageTitle = computed(() => '新建维修单')
  const pageDescription = computed(() => '把维修原因、资产明细和处理方向一次录稳，再进入提交流程。')

  const handleSuccess = () => {
    router.push('/asset/repair')
  }

  const handleCancel = () => {
    router.push('/asset/repair')
  }
</script>

<style scoped lang="scss">
  .asset-repair-create-page {
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

  .asset-repair-create-page__hero {
    border-radius: 18px;
    border-color: rgb(15 23 42 / 8%);
  }

  .asset-repair-create-page__hero-main {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 16px;
  }

  .asset-repair-create-page__eyebrow {
    margin-bottom: 8px;
    font-size: 12px;
    font-weight: 600;
    letter-spacing: 0.12em;
    text-transform: uppercase;
    color: var(--el-color-primary);
  }

  .asset-repair-create-page__title {
    margin: 0;
    font-size: 28px;
    line-height: 1.2;
    color: rgb(15 23 42);
  }

  .asset-repair-create-page__desc {
    margin: 12px 0 0;
    max-width: 760px;
    line-height: 1.7;
    color: rgb(71 85 105);
  }

  @media (max-width: 768px) {
    .asset-repair-create-page {
      padding: 12px;
    }

    .asset-repair-create-page__hero-main {
      flex-direction: column;
      align-items: stretch;
    }

    .asset-repair-create-page__title {
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
