<template>
  <div class="repair-detail-page art-full-height">
    <AssetPageShell
      :loading="loading"
      eyebrow="资产维修单"
      title="维修单详情"
      :description="pageDescription"
    >
      <template #tags>
        <ElSpace wrap>
          <ElTag type="info" effect="light">独立页面</ElTag>
          <ElTag :type="statusTagType" effect="light">{{ currentStatusLabel }}</ElTag>
          <ElTag type="info" effect="plain">多资产明细</ElTag>
        </ElSpace>
      </template>

      <ElAlert
        v-if="showDisposalBridgeAlert"
        class="repair-detail-page__summary"
        type="warning"
        :closable="false"
        show-icon
        :title="bridgeAlertTitle"
        :description="bridgeAlertDescription"
      >
        <template #default>
          <div class="repair-detail-page__bridge-text">
            {{ bridgeAlertDescription }}
          </div>
          <ElButton
            v-if="canCreateDisposal"
            type="warning"
            size="small"
            class="repair-detail-page__bridge-btn"
            @click="handleCreateDisposalOrder"
          >
            创建处置单
          </ElButton>
        </template>
      </ElAlert>

      <ElCard v-if="relatedDisposalOrder" shadow="never" class="mb-4">
        <template #header>关联处置单</template>
        <ElDescriptions :column="2" border>
          <ElDescriptionsItem label="单号">
            {{ displayText(relatedDisposalOrder.orderNo) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="来源业务单号">
            {{ displayText(relatedDisposalOrder.sourceBizNo) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="单据状态">
            <DictTag :options="asset_order_status" :value="relatedDisposalOrder.orderStatus" />
          </ElDescriptionsItem>
          <ElDescriptionsItem label="处置金额">
            {{ displayAmount(relatedDisposalOrder.disposalAmount) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="审批信息">
            {{
              displayApproval(
                relatedDisposalOrder.approveUserName,
                relatedDisposalOrder.approveTime
              )
            }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="处置原因" :span="2">
            {{ displayText(relatedDisposalOrder.disposalReason) }}
          </ElDescriptionsItem>
        </ElDescriptions>
      </ElCard>

      <div
        v-if="relatedDisposalOrder && canViewRelatedDisposal"
        class="repair-detail-page__linked-actions"
      >
        <ElButton type="primary" @click="handleViewRelatedDisposalOrder(relatedDisposalOrder)">
          查看处置单
        </ElButton>
      </div>

      <ElCard shadow="never" class="mb-4">
        <template #header>维修资产明细</template>
        <div class="repair-detail-page__asset-summary">
          <ElTag type="info" effect="plain">资产数量：{{ repairItems.length }}</ElTag>
        </div>
        <ElTable
          :data="repairItems"
          border
          stripe
          size="small"
          row-key="rowKey"
          empty-text="暂无维修资产"
        >
          <ElTableColumn type="index" width="56" label="#" />
          <ElTableColumn prop="assetCode" label="资产编码" min-width="140" />
          <ElTableColumn prop="assetName" label="资产名称" min-width="180" />
          <ElTableColumn label="维修前状态" width="120" align="center">
            <template #default="{ row }">
              <DictTag :options="asset_status" :value="row.beforeStatus" />
            </template>
          </ElTableColumn>
          <ElTableColumn label="维修后状态" width="120" align="center">
            <template #default="{ row }">
              <DictTag :options="asset_status" :value="row.afterStatus" />
            </template>
          </ElTableColumn>
          <ElTableColumn label="处理结果" width="140" align="center">
            <template #default="{ row }">
              {{ displayText(resultTypeMap[row.resultType] || row.resultType) }}
            </template>
          </ElTableColumn>
          <ElTableColumn prop="faultDesc" label="故障描述" min-width="220" show-overflow-tooltip />
          <ElTableColumn prop="remark" label="行级说明" min-width="180" show-overflow-tooltip />
        </ElTable>
      </ElCard>

      <ElCard shadow="never" class="mb-4">
        <template #header>维修概览</template>
        <ElDescriptions :column="2" border>
          <ElDescriptionsItem label="报修时间">
            {{ displayText(repairData?.reportTime) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="维修方式">
            {{ displayText(repairModeLabel) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="发起部门">
            {{ displayText(repairData?.applyDeptName || repairData?.applyDeptId) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="发起人">
            {{ displayText(repairData?.applyUserName || repairData?.applyUserId) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="维修厂商">
            {{ displayText(repairData?.vendorName) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="资产数量">
            {{ repairItems.length || '-' }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="主单故障描述" :span="2">
            {{ displayText(repairData?.faultDesc) }}
          </ElDescriptionsItem>
        </ElDescriptions>
      </ElCard>

      <ElCard shadow="never" class="mb-4">
        <template #header>审批与完工</template>
        <ElDescriptions :column="2" border>
          <ElDescriptionsItem label="审批人">
            {{ displayText(repairData?.approveUserName || repairData?.approveUserId) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="审批时间">
            {{ displayText(repairData?.approveTime) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="审批结果">
            {{ displayText(approveResultLabel) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="送修时间">
            {{ displayText(repairData?.sendRepairTime) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="维修费用">
            {{ displayAmount(repairData?.repairCost) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="停机时长">
            {{ displayDowntime(repairData?.downtimeHours) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="完成时间">
            {{ displayText(repairData?.finishTime) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="主单结果">
            {{ displayText(resultTypeLabel) }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="是否返修">
            {{ repairData?.reworkFlag === '1' ? '是' : '否' }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="备注" :span="2">
            {{ displayText(repairData?.remark) }}
          </ElDescriptionsItem>
        </ElDescriptions>
      </ElCard>

      <template #footer>
        <ElSpace wrap>
          <ElButton
            v-if="hasPermission('asset:repair:query')"
            type="primary"
            plain
            @click="handleOpenAttachments"
          >
            附件
          </ElButton>
          <ElButton
            v-if="canEdit && hasPermission('asset:repair:edit')"
            type="primary"
            plain
            @click="handleEdit"
          >
            编辑
          </ElButton>
          <ElButton
            v-if="canSubmit && hasPermission('asset:repair:submit')"
            type="primary"
            @click="handleSubmitRepair"
          >
            提交
          </ElButton>
          <ElButton
            v-if="canApprove && hasPermission('asset:repair:approve')"
            type="success"
            @click="openApproveDialog('approve')"
          >
            审批通过
          </ElButton>
          <ElButton
            v-if="canReject && hasPermission('asset:repair:reject')"
            type="danger"
            plain
            @click="openApproveDialog('reject')"
          >
            驳回
          </ElButton>
          <ElButton
            v-if="canFinish && hasPermission('asset:repair:finish')"
            type="warning"
            @click="openFinishDialog"
          >
            完工
          </ElButton>
          <ElButton
            v-if="canCancel && hasPermission('asset:repair:cancel')"
            type="info"
            plain
            @click="handleCancelRepair"
          >
            作废
          </ElButton>
          <ElButton @click="handleClosePage">关闭</ElButton>
        </ElSpace>
      </template>
    </AssetPageShell>

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
  import AssetPageShell from '../../shared/asset-page-shell.vue'
  import RepairApproveDialog from '../modules/repair-approve-dialog.vue'
  import RepairFinishDialog from '../modules/repair-finish-dialog.vue'
  import {
    buildRepairListRestoreQuery,
    resolveRepairListRestoreState
  } from '../modules/repair-list-query'
  import { resolveRepairItems } from '../modules/repair-item-normalize'
  import DictTag from '@/components/DictTag/index.vue'
  import { useDict } from '@/utils/dict'
  import { useUserStore } from '@/store/modules/user'

  defineOptions({ name: 'AssetRepairDetailPage' })

  const route = useRoute()
  const router = useRouter()

  const loading = ref(false)
  const repairData = ref<Record<string, any>>({})
  const approveDialogVisible = ref(false)
  const finishDialogVisible = ref(false)
  const attachmentDrawerVisible = ref(false)
  const approveActionType = ref<'approve' | 'reject'>('approve')

  const { asset_status, asset_order_status } = useDict('asset_status', 'asset_order_status')
  const userStore = useUserStore()

  const repairId = computed(() => {
    const rawRepairId = route.params.repairId || route.query.repairId
    return Number(Array.isArray(rawRepairId) ? rawRepairId[0] : rawRepairId || 0)
  })

  const buildBackQuery = () =>
    buildRepairListRestoreQuery(resolveRepairListRestoreState(route.query))

  const statusMap: Record<
    string,
    { label: string; type: 'info' | 'warning' | 'success' | 'danger' }
  > = {
    DRAFT: { label: '草稿', type: 'info' },
    SUBMITTED: { label: '待审批', type: 'warning' },
    APPROVED: { label: '维修中', type: 'warning' },
    REJECTED: { label: '已驳回', type: 'danger' },
    FINISHED: { label: '已完成', type: 'success' },
    CANCELED: { label: '已作废', type: 'info' }
  }

  const repairModeMap: Record<string, string> = {
    IN_HOUSE: '内部维修',
    VENDOR: '外部维修',
    OUTSOURCE: '外部维修',
    ONSITE: '上门维修'
  }

  const resultTypeMap: Record<string, string> = {
    RESUME_USE: '恢复使用',
    TO_IDLE: '转闲置',
    SUGGEST_DISPOSAL: '建议报废'
  }

  const approveResultMap: Record<string, string> = {
    APPROVED: '已通过',
    REJECTED: '已驳回'
  }

  const repairItems = computed(() => resolveRepairItems(repairData.value))

  const currentStatus = computed(() => repairData.value?.repairStatus || 'DRAFT')
  const currentStatusLabel = computed(
    () => statusMap[currentStatus.value]?.label || currentStatus.value || '-'
  )
  const statusTagType = computed(() => statusMap[currentStatus.value]?.type || 'info')
  const repairModeLabel = computed(
    () => repairModeMap[repairData.value?.repairMode] || repairData.value?.repairMode || '-'
  )
  const resultTypeLabel = computed(
    () => resultTypeMap[repairData.value?.resultType] || repairData.value?.resultType || '-'
  )
  const approveResultLabel = computed(() => {
    const nextApproveResult = repairData.value?.approveResult
    return approveResultMap[nextApproveResult] || nextApproveResult || '-'
  })
  const relatedDisposalOrder = computed(() => repairData.value?.relatedDisposalOrder)
  const assetSummaryText = computed(() => {
    if (!repairItems.value.length) return '维修状态、资产明细和后续流转记录'
    if (repairItems.value.length === 1) {
      const item = repairItems.value[0]
      return `${displayText(item.assetName)} / ${displayText(item.assetCode)}`
    }
    const firstItem = repairItems.value[0]
    return `${displayText(firstItem.assetName)} / ${displayText(firstItem.assetCode)} +${
      repairItems.value.length - 1
    }`
  })
  const pageDescription = computed(() =>
    repairItems.value.length
      ? `查看 ${assetSummaryText.value}，以及审批轨迹、完工结果和关联处置单。`
      : '查看维修状态、资产明细、审批轨迹和后续流转记录。'
  )
  const showDisposalBridgeAlert = computed(
    () =>
      repairData.value?.repairStatus === 'FINISHED' &&
      repairData.value?.resultType === 'SUGGEST_DISPOSAL'
  )
  const canCreateDisposal = computed(
    () =>
      showDisposalBridgeAlert.value &&
      repairItems.value.length === 1 &&
      !relatedDisposalOrder.value?.orderId &&
      hasPermission('asset:order:query') &&
      hasPermission('asset:order:add')
  )
  const canViewRelatedDisposal = computed(
    () => Boolean(relatedDisposalOrder.value?.orderId) && hasPermission('asset:order:query')
  )
  const bridgeAlertTitle = computed(() =>
    relatedDisposalOrder.value?.orderNo
      ? `已关联处置单 ${relatedDisposalOrder.value.orderNo}`
      : '当前维修结果为建议处置'
  )
  const bridgeAlertDescription = computed(() =>
    relatedDisposalOrder.value?.orderNo
      ? `来源业务单号：${relatedDisposalOrder.value.sourceBizNo || '-'}`
      : repairItems.value.length > 1
        ? '当前维修单包含多项资产，请先逐项确认维修结果后再创建处置单。'
        : '你可以继续创建处置单草稿。'
  )
  const canEdit = computed(() => ['DRAFT', 'REJECTED'].includes(currentStatus.value))
  const canSubmit = computed(() => ['DRAFT', 'REJECTED'].includes(currentStatus.value))
  const canApprove = computed(() => currentStatus.value === 'SUBMITTED')
  const canReject = computed(() => currentStatus.value === 'SUBMITTED')
  const canFinish = computed(() => currentStatus.value === 'APPROVED')
  const canCancel = computed(() =>
    ['DRAFT', 'SUBMITTED', 'REJECTED', 'APPROVED'].includes(currentStatus.value)
  )

  const hasPermission = (permission: string) => {
    const permissions = userStore.permissions || []
    return permissions.includes('*:*:*') || permissions.includes(permission)
  }

  const displayText = (value: unknown) => {
    if (value === null || value === undefined || value === '') return '-'
    return String(value)
  }

  const displayAmount = (value: unknown) => {
    if (value === null || value === undefined || value === '') return '-'
    return `${value} 元`
  }

  const displayDowntime = (value: unknown) => {
    if (value === null || value === undefined || value === '') return '-'
    return `${value} 小时`
  }

  const displayApproval = (userName?: string, approveTime?: string) => {
    if (!userName && !approveTime) return '-'
    return [userName, approveTime].filter(Boolean).join(' / ')
  }

  const getRelatedDisposalOrder = async (repair?: Record<string, any>) => {
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
      console.error('查询关联处置单失败，继续展示维修详情主体:', error)
      return undefined
    }
  }

  const loadRepairDetail = async () => {
    if (!repairId.value) {
      ElMessage.error('缺少维修单标识，无法打开详情页')
      router.push({ path: '/asset/repair', query: buildBackQuery() })
      return
    }

    loading.value = true
    try {
      const response: any = await getAssetRepair(repairId.value)
      const nextRepair = response?.data || response || {}
      const relatedDisposalOrderValue = await getRelatedDisposalOrder(nextRepair)
      repairData.value = { ...nextRepair, relatedDisposalOrder: relatedDisposalOrderValue }
    } finally {
      loading.value = false
    }
  }

  const handleEdit = () => {
    if (!repairId.value) return
    router.push({ path: `/asset/repair/edit/${repairId.value}`, query: buildBackQuery() })
  }

  const handleSubmitRepair = async () => {
    if (!repairId.value) return
    try {
      await ElMessageBox.confirm(
        `确认提交维修单「${repairData.value?.repairNo || repairId.value}」吗？`,
        '提示',
        { type: 'warning' }
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
      await finishAssetRepair(repairId.value, payload)
      ElMessage.success('完工成功')
      finishDialogVisible.value = false
      await loadRepairDetail()
    } catch (error) {
      console.error('完工维修单失败:', error)
    }
  }

  const handleCancelRepair = async () => {
    if (!repairId.value) return
    try {
      await ElMessageBox.confirm(
        `确认取消维修单「${repairData.value?.repairNo || repairId.value}」吗？`,
        '提示',
        { type: 'warning' }
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
    // 详情页作为主入口页面，关闭动作统一返回列表并带回筛选态。
    router.push({ path: '/asset/repair', query: buildBackQuery() })
  }

  const handleOpenAttachments = () => {
    attachmentDrawerVisible.value = true
  }

  const handleCreateDisposalOrder = async () => {
    if (!repairId.value) return
    // 详情页直连处置单创建，继续沿用桥接参数，避免重新挑资产。
    const bridgePayload = {
      source: 'repair',
      orderType: 'DISPOSAL',
      sourceBizType: 'ASSET_REPAIR',
      sourceBizId: repairId.value,
      repairId: repairId.value
    }
    await router.push({
      path: '/asset/order/create',
      query: {
        bridgeSource: 'repair',
        orderType: 'DISPOSAL',
        sourceBizType: 'ASSET_REPAIR',
        sourceBizId: String(repairId.value),
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
  .repair-detail-page {
    min-height: 100%;
  }

  .repair-detail-page__summary {
    border-radius: 16px;
  }

  .repair-detail-page__bridge-text {
    margin-bottom: 12px;
    line-height: 1.6;
    color: rgb(51 65 85);
  }

  .repair-detail-page__bridge-btn {
    align-self: flex-start;
  }

  .repair-detail-page__linked-actions {
    display: flex;
    justify-content: flex-start;
  }

  .repair-detail-page__asset-summary {
    margin-bottom: 12px;
  }
</style>
