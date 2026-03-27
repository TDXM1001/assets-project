<template>
  <div class="asset-repair-edit-page art-full-height" v-loading="loading">
    <ElCard class="asset-repair-edit-page__hero" shadow="never">
      <div class="asset-repair-edit-page__hero-main">
        <div>
          <div class="asset-repair-edit-page__eyebrow">资产维修单</div>
          <h1 class="asset-repair-edit-page__title">{{ pageTitle }}</h1>
          <p class="asset-repair-edit-page__desc">{{ pageDescription }}</p>
        </div>

        <ElSpace wrap>
          <ElTag v-if="repairData?.repairNo" type="success" effect="light">
            {{ repairData.repairNo }}
          </ElTag>
          <ElTag type="warning" effect="light">独立页面</ElTag>
        </ElSpace>
      </div>
    </ElCard>

    <RepairDialog
      v-model="dialogVisible"
      page-mode
      dialog-type="edit"
      :repair-data="repairData"
      @success="handleSuccess"
      @cancel="handleCancel"
    />
  </div>
</template>

<script setup lang="ts">
  import { computed, onMounted, ref } from 'vue'
  import { useRoute, useRouter } from 'vue-router'
  import { ElMessage } from 'element-plus'
  import { getAssetRepair } from '@/api/asset/repair'
  import RepairDialog from '../modules/repair-dialog.vue'

  defineOptions({ name: 'AssetRepairEditPage' })

  const route = useRoute()
  const router = useRouter()
  const dialogVisible = ref(true)
  const loading = ref(false)
  const repairData = ref<Record<string, any>>({})

  const repairId = computed(() => {
    const rawRepairId = route.params.repairId || route.query.repairId
    return Number(Array.isArray(rawRepairId) ? rawRepairId[0] : rawRepairId || 0)
  })

  const pageTitle = computed(() => '编辑维修单')
  const pageDescription = computed(
    () => '保留和新增页一致的页面骨架，只把当前草稿或可编辑状态的数据重新载入。'
  )

  const loadRepairData = async () => {
    if (!repairId.value) {
      ElMessage.error('缺少维修单标识，无法打开编辑页')
      router.push('/asset/repair')
      return
    }

    loading.value = true
    try {
      const response: any = await getAssetRepair(repairId.value)
      repairData.value = response?.data || response || {}
    } finally {
      loading.value = false
    }
  }

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

  onMounted(() => {
    void loadRepairData()
  })
</script>

<style scoped lang="scss">
  .asset-repair-edit-page {
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

  .asset-repair-edit-page__hero {
    border-radius: 18px;
    border-color: rgb(15 23 42 / 8%);
  }

  .asset-repair-edit-page__hero-main {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 16px;
  }

  .asset-repair-edit-page__eyebrow {
    margin-bottom: 8px;
    font-size: 12px;
    font-weight: 600;
    letter-spacing: 0.12em;
    text-transform: uppercase;
    color: var(--el-color-primary);
  }

  .asset-repair-edit-page__title {
    margin: 0;
    font-size: 28px;
    line-height: 1.2;
    color: rgb(15 23 42);
  }

  .asset-repair-edit-page__desc {
    margin: 12px 0 0;
    max-width: 760px;
    line-height: 1.7;
    color: rgb(71 85 105);
  }

  @media (max-width: 768px) {
    .asset-repair-edit-page {
      padding: 12px;
    }

    .asset-repair-edit-page__hero-main {
      flex-direction: column;
      align-items: stretch;
    }

    .asset-repair-edit-page__title {
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
