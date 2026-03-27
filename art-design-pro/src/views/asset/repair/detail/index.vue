<template>
  <div class="asset-repair-detail-page art-full-height" v-loading="loading">
    <ElCard class="asset-repair-detail-page__hero" shadow="never">
      <div class="asset-repair-detail-page__hero-main">
        <div>
          <div class="asset-repair-detail-page__eyebrow">资产维修单</div>
          <h1 class="asset-repair-detail-page__title">{{ pageTitle }}</h1>
          <p class="asset-repair-detail-page__desc">{{ pageDescription }}</p>
        </div>

        <ElSpace wrap>
          <ElTag v-if="repairData?.repairNo" type="success" effect="light">
            {{ repairData.repairNo }}
          </ElTag>
          <ElTag type="info" effect="light">{{ statusLabel }}</ElTag>
        </ElSpace>
      </div>
    </ElCard>

    <RepairDetailDrawer
      v-model="pageVisible"
      page-mode
      :repair-data="repairData"
      @edit="handleEdit"
      @submit="handleSubmitRepair"
      @approve="openApproveDialog('approve')"
      @reject="openApproveDialog('reject')"
      @finish="openFinishDialog"
      @cancel="handleCancelRepair"
      @close="handleClosePage"
      @attachments="handleOpenAttachments"
      @create-disposal="handleCreateDisposalOrder"
      @view-disposal="handleViewRelatedDisposalOrder"
    />

    <RepairApproveDialog
      v-model="approveDialogVisible"
      :action-type="approveActionType"
      :repair-data="repairData"
      @confirm="handleApproveConfirm"
    />

    <RepairFinishDialog
      v-model="finishDialogVisible"
      :repair-data="repairData"
      @confirm="handleFinishConfirm"
    />

    <AssetAttachmentDrawer
      v-model="attachmentDrawerVisible"
      biz-type="ASSET_REPAIR"
      :biz-id="repairData?.repairId"
      :biz-title="repairData?.repairNo || '维修单详情'"
      permission-prefix="asset:repair"
    />
  </div>
</template>

<script setup lang="ts">
  import { computed, onMounted, ref } from 'vue'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import { useRoute, useRouter } from 'vue-router'
  import { getAssetInfo } from '@/api/asset/info'
  import { getLinkedAssetOrder } from '@/api/asset/order'
  import {
    approveAssetRepair,
    cancelAssetRepair,
    finishAssetRepair,
    getAssetRepair,
    rejectAssetRepair,
    submitAssetRepair
  } from '@/api/asset/repair'
  import AssetAttachmentDrawer from '../../shared/asset-attachment-drawer.vue'
  import RepairApproveDialog from '../modules/repair-approve-dialog.vue'
  import RepairDetailDrawer from '../modules/repair-detail-drawer.vue'
  import RepairFinishDialog from '../modules/repair-finish-dialog.vue'

  defineOptions({ name: 'AssetRepairDetailPage' })

  const route = useRoute()
  const router = useRouter()

  const pageVisible = ref(true)
  const loading = ref(false)
  const repairData = ref<Record<string, any>>({})
  const approveDialogVisible = ref(false)
  const finishDialogVisible = ref(false)
  const attachmentDrawerVisible = ref(false)
  const approveActionType = ref<'approve' | 'reject'>('approve')

  const repairId = computed(() => {
    const rawRepairId = route.params.repairId || route.query.repairId
    return Number(Array.isArray(rawRepairId) ? rawRepairId[0] : rawRepairId || 0)
  })

  const pageTitle = computed(() =>
    repairData.value?.repairNo ? `维修详情 - ${repairData.value.repairNo}` : '维修详情'
  )
  const pageDescription = computed(
    () => '查看维修单完整信息、审批轨迹、完成结果和关联报废单，页面动作只保留局部轻浮层。'
  )
  const statusLabel = computed(() => repairData.value?.repairStatus || 'DRAFT')

  const extractRepairItems = (repair?: any) => {
    if (Array.isArray(repair?.itemList) && repair.itemList.length > 0) {
      return repair.itemList.filter(Boolean)
    }
    if (Array.isArray(repair?.repairItems) && repair.repairItems.length > 0) {
      return repair.repairItems.filter(Boolean)
    }
    if (repair?.assetId) {
      return [
        {
          assetId: repair.assetId,
          assetCode: repair.assetCode || '',
          assetName: repair.assetName || '',
          beforeStatus: repair.beforeStatus || '',
          afterStatus: repair.afterStatus || repair.beforeStatus || '',
          resultType: repair.resultType || '',
          faultDesc: repair.faultDesc || '',
          remark: repair.remark || ''
        }
      ]
    }
    return []
  }

  const createDisposalBridgePayload = async (repair?: any) => {
    const primaryItem = extractRepairItems(repair)[0]
    let assetSnapshot: Record<string, any> | undefined

    if (primaryItem?.assetId) {
      try {
        const response: any = await getAssetInfo(primaryItem.assetId)
        assetSnapshot = response?.data || response || undefined
      } catch (error) {
        console.error('加载报废桥接资产快照失败，继续使用维修单快照:', error)
      }
    }

    return {
      source: 'repair',
      orderType: 'DISPOSAL',
      sourceBizType: 'ASSET_REPAIR',
      sourceBizId: repair?.repairId,
      sourceBizNo: repair?.repairNo || '',
      repairId: repair?.repairId,
      repairNo: repair?.repairNo || '',
      assetId: primaryItem?.assetId || repair?.assetId,
      assetCode: primaryItem?.assetCode || repair?.assetCode || '',
      assetName: primaryItem?.assetName || repair?.assetName || '',
      assetStatus: assetSnapshot?.assetStatus || repair?.afterStatus || repair?.beforeStatus || '',
      beforeStatus: repair?.beforeStatus || assetSnapshot?.assetStatus || '',
      afterStatus: repair?.afterStatus || assetSnapshot?.assetStatus || repair?.beforeStatus || '',
      currentUserId: assetSnapshot?.currentUserId,
      useOrgDeptId: assetSnapshot?.useOrgDeptId,
      currentLocationId: assetSnapshot?.currentLocationId,
      resultType: repair?.resultType || '',
      faultDesc: repair?.faultDesc || '',
      repairRemark: repair?.remark || '',
      repairCost: repair?.repairCost,
      downtimeHours: repair?.downtimeHours,
      vendorName: repair?.vendorName || '',
      finishTime: repair?.finishTime || '',
      disposalReason:
        repair?.disposeSuggestion ||
        repair?.repairConclusion ||
        repair?.remark ||
        repair?.faultDesc ||
        ''
    }
  }

  const loadRepairDetail = async () => {
    if (!repairId.value) {
      ElMessage.error('缺少维修单标识，无法打开详情页')
      router.push('/asset/repair')
      return
    }

    loading.value = true
    try {
      const response: any = await getAssetRepair(repairId.value)
      const nextRepair = response?.data || response || {}
      const relatedDisposalOrder = await getRelatedDisposalOrder(nextRepair)
      repairData.value = { ...nextRepair, relatedDisposalOrder }
    } finally {
      loading.value = false
    }
  }

  const getRelatedDisposalOrder = async (repair?: any) => {
    const nextRepairId = Number(repair?.repairId)
    if (!nextRepairId) return undefined

    try {
      const response: any = await getLinkedAssetOrder({
        orderType: 'DISPOSAL',
        sourceBizType: 'ASSET_REPAIR',
        sourceBizId: nextRepairId
      })
      return response?.data || undefined
    } catch (error) {
      console.error('查询关联报废单失败，继续展示维修详情主体:', error)
      return undefined
    }
  }

  const handleEdit = () => {
    if (!repairId.value) return
    router.push(`/asset/repair/edit/${repairId.value}`)
  }

  const handleSubmitRepair = async () => {
    if (!repairId.value) return
    try {
      await ElMessageBox.confirm(
        `确认提交维修单「${repairData.value?.repairNo || repairId.value}」吗？`,
        '提示',
        {
          type: 'warning'
        }
      )
      await submitAssetRepair(repairId.value)
      ElMessage.success('提交成功')
      await loadRepairDetail()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('提交维修单失败:', error)
      }
    }
  }

  const openApproveDialog = (actionType: 'approve' | 'reject' = 'approve') => {
    approveActionType.value = actionType
    approveDialogVisible.value = true
  }

  const handleApproveConfirm = async (payload: {
    remark: string
    actionType: 'approve' | 'reject'
  }) => {
    if (!repairId.value) return
    try {
      if (payload.actionType === 'approve') {
        await approveAssetRepair(repairId.value, { remark: payload.remark })
        ElMessage.success('审批通过')
      } else {
        await rejectAssetRepair(repairId.value, { remark: payload.remark })
        ElMessage.success('审批驳回')
      }
      approveDialogVisible.value = false
      await loadRepairDetail()
    } catch (error) {
      console.error('审批维修单失败:', error)
    }
  }

  const openFinishDialog = () => {
    finishDialogVisible.value = true
  }

  const handleFinishConfirm = async (payload: any) => {
    if (!repairId.value) return
    try {
      await finishAssetRepair(repairId.value, {
        ...payload,
        itemList: Array.isArray(payload?.itemList)
          ? payload.itemList
          : extractRepairItems(repairData.value)
      })
      ElMessage.success('完成成功')
      finishDialogVisible.value = false
      await loadRepairDetail()
    } catch (error) {
      console.error('完成维修单失败:', error)
    }
  }

  const handleCancelRepair = async () => {
    if (!repairId.value) return
    try {
      await ElMessageBox.confirm(
        `确认取消维修单「${repairData.value?.repairNo || repairId.value}」吗？`,
        '提示',
        {
          type: 'warning'
        }
      )
      await cancelAssetRepair(repairId.value)
      ElMessage.success('取消成功')
      await loadRepairDetail()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('取消维修单失败:', error)
      }
    }
  }

  const handleClosePage = () => {
    router.push('/asset/repair')
  }

  const handleOpenAttachments = () => {
    attachmentDrawerVisible.value = true
  }

  const handleCreateDisposalOrder = async () => {
    if (!repairId.value) return
    const bridgePayload = await createDisposalBridgePayload(repairData.value)
    await router.push({
      path: '/asset/order/create',
      query: {
        bridgeSource: 'repair',
        orderType: 'DISPOSAL',
        repairId: String(repairId.value),
        bridgeData: JSON.stringify(bridgePayload)
      }
    })
  }

  const handleViewRelatedDisposalOrder = async (order?: any) => {
    const orderId = Number(order?.orderId)
    if (!orderId) return

    await router.push({
      path: '/asset/order',
      query: {
        orderType: 'DISPOSAL',
        orderId: String(orderId),
        openDetail: '1'
      }
    })
  }

  onMounted(() => {
    void loadRepairDetail()
  })
</script>

<style scoped lang="scss">
  .asset-repair-detail-page {
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

  .asset-repair-detail-page__hero {
    border-radius: 18px;
    border-color: rgb(15 23 42 / 8%);
  }

  .asset-repair-detail-page__hero-main {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 16px;
  }

  .asset-repair-detail-page__eyebrow {
    margin-bottom: 8px;
    font-size: 12px;
    font-weight: 600;
    letter-spacing: 0.12em;
    text-transform: uppercase;
    color: var(--el-color-primary);
  }

  .asset-repair-detail-page__title {
    margin: 0;
    font-size: 28px;
    line-height: 1.2;
    color: rgb(15 23 42);
  }

  .asset-repair-detail-page__desc {
    margin: 12px 0 0;
    max-width: 760px;
    line-height: 1.7;
    color: rgb(71 85 105);
  }

  @media (max-width: 768px) {
    .asset-repair-detail-page {
      padding: 12px;
    }

    .asset-repair-detail-page__hero-main {
      flex-direction: column;
      align-items: stretch;
    }

    .asset-repair-detail-page__title {
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
