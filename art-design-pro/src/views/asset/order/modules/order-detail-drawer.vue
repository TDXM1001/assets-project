<template>
  <ElDrawer
    v-model="visible"
    :title="drawerTitle"
    size="78%"
    append-to-body
    destroy-on-close
    @closed="handleClosed"
  >
    <OrderWorkbenchShell eyebrow="业务单据" :title="shellTitle" :description="shellDescription">
      <template #status>
        <ElSpace wrap>
          <ElTag effect="light" type="info">单据状态</ElTag>
          <DictTag :options="asset_order_status" :value="currentStatus" />
          <DictTag :options="asset_order_type" :value="orderData?.orderType" />
          <ElTag effect="plain" type="primary">明细 {{ detailItems.length }} 项</ElTag>
        </ElSpace>
      </template>

      <template #editor>
        <div class="order-detail-drawer">
          <ElAlert
            v-if="isDisposalOrder"
            class="mb-4"
            type="warning"
            show-icon
            :closable="false"
            title="报废单完成后，资产状态会落到已报废，后续不再参与领用和流转。"
          />

          <ElCard shadow="never" class="mb-4">
            <template #header>基础信息</template>
            <ElDescriptions :column="2" border>
              <ElDescriptionsItem label="单据编号">
                {{ displayText(orderData?.orderNo) }}
              </ElDescriptionsItem>
              <ElDescriptionsItem label="单据类型">
                <DictTag :options="asset_order_type" :value="orderData?.orderType" />
              </ElDescriptionsItem>
              <ElDescriptionsItem label="业务时间">
                {{ displayText(orderData?.bizDate) }}
              </ElDescriptionsItem>
              <ElDescriptionsItem label="发起部门">
                {{ displayText(orderData?.applyDeptName || orderData?.applyDeptId) }}
              </ElDescriptionsItem>
              <ElDescriptionsItem label="发起人">
                {{ displayText(orderData?.applyUserName || orderData?.applyUserId) }}
              </ElDescriptionsItem>
              <ElDescriptionsItem label="附件数量">
                {{ displayText(orderData?.attachmentCount ?? 0) }}
              </ElDescriptionsItem>
            </ElDescriptions>
          </ElCard>

          <ElCard shadow="never" class="mb-4">
            <template #header>{{ isDisposalOrder ? '报废信息' : '流转范围' }}</template>
            <ElDescriptions :column="2" border>
              <template v-if="isDisposalOrder">
                <ElDescriptionsItem label="处置金额">
                  {{ displayText(orderData?.disposalAmount ?? '-') }}
                </ElDescriptionsItem>
                <ElDescriptionsItem label="报废原因" :span="2">
                  {{ displayText(orderData?.disposalReason) }}
                </ElDescriptionsItem>
                <ElDescriptionsItem label="备注" :span="2">
                  {{ displayText(orderData?.remark) }}
                </ElDescriptionsItem>
              </template>
              <template v-else>
                <ElDescriptionsItem label="来源部门">
                  {{ displayText(orderData?.fromDeptName || orderData?.fromDeptId) }}
                </ElDescriptionsItem>
                <ElDescriptionsItem label="目标部门">
                  {{ displayText(orderData?.toDeptName || orderData?.toDeptId) }}
                </ElDescriptionsItem>
                <ElDescriptionsItem label="来源责任人">
                  {{ displayText(orderData?.fromUserName || orderData?.fromUserId) }}
                </ElDescriptionsItem>
                <ElDescriptionsItem label="目标责任人">
                  {{ displayText(orderData?.toUserName || orderData?.toUserId) }}
                </ElDescriptionsItem>
                <ElDescriptionsItem label="来源位置">
                  {{ displayText(orderData?.fromLocationName || orderData?.fromLocationId) }}
                </ElDescriptionsItem>
                <ElDescriptionsItem label="目标位置">
                  {{ displayText(orderData?.toLocationName || orderData?.toLocationId) }}
                </ElDescriptionsItem>
                <ElDescriptionsItem label="预计归还日">
                  {{ displayText(orderData?.expectedReturnDate) }}
                </ElDescriptionsItem>
                <ElDescriptionsItem label="处置金额">
                  {{ displayText(orderData?.disposalAmount ?? '-') }}
                </ElDescriptionsItem>
                <ElDescriptionsItem label="报废原因" :span="2">
                  {{ displayText(orderData?.disposalReason) }}
                </ElDescriptionsItem>
              </template>
            </ElDescriptions>
          </ElCard>

          <ElCard shadow="never" class="mb-4">
            <template #header>审批信息</template>
            <ElDescriptions :column="2" border>
              <ElDescriptionsItem label="审批人">
                {{ displayText(orderData?.approveUserName || orderData?.approveUserId) }}
              </ElDescriptionsItem>
              <ElDescriptionsItem label="审批时间">
                {{ displayText(orderData?.approveTime) }}
              </ElDescriptionsItem>
              <ElDescriptionsItem label="审批结果">
                {{ displayText(orderData?.approveResult) }}
              </ElDescriptionsItem>
              <ElDescriptionsItem label="备注" :span="2">
                {{ displayText(orderData?.remark) }}
              </ElDescriptionsItem>
            </ElDescriptions>
          </ElCard>

          <ElCard shadow="never">
            <template #header>
              <div class="flex items-center justify-between">
                <span>单据明细</span>
                <ElTag effect="plain" type="success">
                  明细已经接通，流程动作会以这些资产为落账对象
                </ElTag>
              </div>
            </template>

            <template v-if="detailItems.length">
              <ElTable :data="detailItems" border stripe>
                <ElTableColumn prop="assetCode" label="资产编码" min-width="140" />
                <ElTableColumn prop="assetName" label="资产名称" min-width="180" />
                <ElTableColumn label="变更前状态" width="130" align="center">
                  <template #default="{ row }">
                    <DictTag :options="asset_status" :value="row.beforeStatus" />
                  </template>
                </ElTableColumn>
                <ElTableColumn label="变更后状态" width="130" align="center">
                  <template #default="{ row }">
                    <DictTag :options="asset_status" :value="row.afterStatus" />
                  </template>
                </ElTableColumn>
                <ElTableColumn label="变更前部门" min-width="140">
                  <template #default="{ row }">
                    {{ displayText(row.beforeDeptName || row.beforeDeptId) }}
                  </template>
                </ElTableColumn>
                <ElTableColumn label="变更前责任人" min-width="140">
                  <template #default="{ row }">
                    {{ displayText(row.beforeUserName || row.beforeUserId) }}
                  </template>
                </ElTableColumn>
                <ElTableColumn label="变更前位置" min-width="160">
                  <template #default="{ row }">
                    {{ displayText(row.beforeLocationName || row.beforeLocationId) }}
                  </template>
                </ElTableColumn>
                <ElTableColumn label="变更后部门" min-width="140">
                  <template #default="{ row }">
                    {{ displayText(row.afterDeptName || row.afterDeptId) }}
                  </template>
                </ElTableColumn>
                <ElTableColumn label="变更后责任人" min-width="140">
                  <template #default="{ row }">
                    {{ displayText(row.afterUserName || row.afterUserId) }}
                  </template>
                </ElTableColumn>
                <ElTableColumn label="变更后位置" min-width="160">
                  <template #default="{ row }">
                    {{ displayText(row.afterLocationName || row.afterLocationId) }}
                  </template>
                </ElTableColumn>
                <ElTableColumn prop="itemStatus" label="处理状态" width="120" align="center" />
                <ElTableColumn prop="itemResult" label="处理说明" min-width="180" />
              </ElTable>
            </template>
            <template v-else>
              <ElEmpty description="当前单据还没有资产明细，完成前需要先补齐 itemList。" />
            </template>
          </ElCard>
        </div>
      </template>

      <template #footer>
        <div class="order-detail-drawer__footer">
          <ElSpace wrap>
            <ElButton
              v-if="hasPermission('asset:order:query')"
              type="primary"
              plain
              @click="emit('attachments')"
            >
              附件
            </ElButton>
            <ElButton
              v-if="canEdit && hasPermission('asset:order:edit')"
              type="primary"
              plain
              @click="emit('edit')"
            >
              编辑
            </ElButton>
            <ElButton
              v-if="canSubmit && hasPermission('asset:order:submit')"
              type="primary"
              @click="emit('submit')"
            >
              提交
            </ElButton>
            <ElButton
              v-if="canApprove && hasPermission('asset:order:approve')"
              type="success"
              @click="emit('approve')"
            >
              通过
            </ElButton>
            <ElButton
              v-if="canReject && hasPermission('asset:order:reject')"
              type="danger"
              plain
              @click="emit('reject')"
            >
              驳回
            </ElButton>
            <ElButton
              v-if="canFinish && hasPermission('asset:order:finish')"
              type="warning"
              @click="emit('finish')"
            >
              {{ finishButtonText }}
            </ElButton>
            <ElButton
              v-if="canCancel && hasPermission('asset:order:cancel')"
              type="info"
              plain
              @click="emit('cancel')"
            >
              取消
            </ElButton>
            <ElButton @click="visible = false">关闭</ElButton>
          </ElSpace>
        </div>
      </template>
    </OrderWorkbenchShell>
  </ElDrawer>
</template>

<script setup lang="ts">
  import { computed, ref, watch } from 'vue'
  import { useDict } from '@/utils/dict'
  import { useUserStore } from '@/store/modules/user'
  import DictTag from '@/components/DictTag/index.vue'
  import OrderWorkbenchShell from './order-workbench-shell.vue'
  import { buildOrderTypeScopeSummary, buildOrderTypeViewConfig } from './order-page-meta'

  const { asset_order_type, asset_order_status, asset_status } = useDict(
    'asset_order_type',
    'asset_order_status',
    'asset_status'
  )
  const userStore = useUserStore()

  const props = defineProps<{
    modelValue: boolean
    orderData?: any
  }>()

  const emit = defineEmits<{
    (e: 'update:modelValue', value: boolean): void
    (e: 'edit'): void
    (e: 'submit'): void
    (e: 'approve'): void
    (e: 'reject'): void
    (e: 'finish'): void
    (e: 'cancel'): void
    (e: 'attachments'): void
  }>()

  const visible = ref(false)

  const currentStatus = computed(() => props.orderData?.orderStatus || 'DRAFT')
  const detailTypeConfig = computed(() => buildOrderTypeViewConfig(props.orderData?.orderType))
  const isDisposalOrder = computed(() => detailTypeConfig.value.requiresDisposalFields)
  const finishButtonText = computed(() => (isDisposalOrder.value ? '执行报废' : '完成'))

  const detailItems = computed(() => {
    const source =
      props.orderData?.itemList || props.orderData?.items || props.orderData?.detailList
    return Array.isArray(source) ? source : []
  })

  const drawerTitle = computed(() => {
    const prefix = isDisposalOrder.value ? '报废单详情' : '单据详情'
    return props.orderData?.orderNo ? `${prefix} - ${props.orderData.orderNo}` : prefix
  })
  const shellTitle = computed(() => {
    if (props.orderData?.orderNo) {
      return String(props.orderData.orderNo)
    }
    return drawerTitle.value
  })
  const shellDescription = computed(() => buildOrderTypeScopeSummary(props.orderData || {}))

  const canEdit = computed(() => ['DRAFT', 'REJECTED'].includes(currentStatus.value))
  const canSubmit = computed(() => ['DRAFT', 'REJECTED'].includes(currentStatus.value))
  const canApprove = computed(() => ['SUBMITTED', 'APPROVING'].includes(currentStatus.value))
  const canReject = computed(() => ['SUBMITTED', 'APPROVING'].includes(currentStatus.value))
  const canFinish = computed(() => ['APPROVED'].includes(currentStatus.value))
  const canCancel = computed(() =>
    ['DRAFT', 'SUBMITTED', 'APPROVING', 'APPROVED'].includes(currentStatus.value)
  )

  const displayText = (value: unknown) => {
    if (value === null || value === undefined || value === '') return '-'
    return String(value)
  }

  const hasPermission = (permission: string) => {
    const permissions = userStore.permissions || []
    return permissions.includes('*:*:*') || permissions.includes(permission)
  }

  watch(
    () => props.modelValue,
    (value) => {
      visible.value = value
    }
  )

  watch(
    () => visible.value,
    (value) => {
      emit('update:modelValue', value)
    }
  )

  const handleClosed = () => {
    visible.value = false
  }
</script>
