<template>
  <div class="asset-order-workbench-page" v-loading="loading">
    <ElAlert
      class="asset-order-workbench-page__tip"
      type="info"
      show-icon
      :closable="false"
      title="页面版单据编辑器只承载主流程表单，资产选择仍使用局部弹层。"
    />
    <ElAlert
      v-if="bridgeHintText"
      class="asset-order-workbench-page__tip"
      type="success"
      show-icon
      :closable="false"
      :title="bridgeHintText"
    />

    <ElForm
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="104px"
      class="asset-order-workbench-page__form"
    >
      <ElCard shadow="never">
        <template #header>
          <div class="asset-order-workbench-page__section-header">
            <div>
              <div class="asset-order-workbench-page__section-title">
                {{ activeTypeConfig.sectionTitle }}
              </div>
              <div class="asset-order-workbench-page__section-subtitle">
                {{ activeTypeConfig.sectionSubtitle }}
              </div>
            </div>
          </div>
        </template>

        <ElRow :gutter="16">
          <ElCol :xs="24" :md="12">
            <ElFormItem label="单据编号">
              <ElInput v-model="formData.orderNo" disabled placeholder="提交后由后端生成" />
            </ElFormItem>
          </ElCol>
          <ElCol :xs="24" :md="12">
            <ElFormItem label="单据类型" prop="orderType">
              <ElSelect v-model="formData.orderType" class="w-full">
                <ElOption
                  v-for="item in asset_order_type"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </ElSelect>
            </ElFormItem>
          </ElCol>
          <ElCol :xs="24" :md="12">
            <ElFormItem label="业务时间" prop="bizDate">
              <ElDatePicker
                v-model="formData.bizDate"
                type="datetime"
                value-format="YYYY-MM-DD HH:mm:ss"
                class="w-full"
                placeholder="请选择业务时间"
              />
            </ElFormItem>
          </ElCol>
          <ElCol :xs="24" :md="12">
            <ElFormItem label="单据状态">
              <ElInput v-model="formData.orderStatus" disabled />
            </ElFormItem>
          </ElCol>
          <ElCol :xs="24" :md="12">
            <ElFormItem label="发起部门" prop="applyDeptId">
              <ElTreeSelect
                v-model="formData.applyDeptId"
                :data="deptOptions"
                :props="treeProps"
                value-key="id"
                filterable
                clearable
                check-strictly
                class="w-full"
              />
            </ElFormItem>
          </ElCol>
          <ElCol :xs="24" :md="12">
            <ElFormItem label="发起人" prop="applyUserId">
              <ElSelect v-model="formData.applyUserId" filterable clearable class="w-full">
                <ElOption
                  v-for="item in userOptions"
                  :key="item.userId"
                  :label="item.displayName"
                  :value="item.userId"
                />
              </ElSelect>
            </ElFormItem>
          </ElCol>
          <ElCol v-if="activeTypeConfig.showTargetFields" :xs="24" :md="12">
            <ElFormItem label="目标责任人">
              <ElSelect v-model="formData.toUserId" filterable clearable class="w-full">
                <ElOption
                  v-for="item in userOptions"
                  :key="item.userId"
                  :label="item.displayName"
                  :value="item.userId"
                />
              </ElSelect>
            </ElFormItem>
          </ElCol>
          <ElCol v-if="activeTypeConfig.showTargetFields" :xs="24" :md="12">
            <ElFormItem label="目标位置">
              <ElTreeSelect
                v-model="formData.toLocationId"
                :data="locationOptions"
                :props="treeProps"
                value-key="id"
                filterable
                clearable
                check-strictly
                class="w-full"
              />
            </ElFormItem>
          </ElCol>
          <ElCol v-if="activeTypeConfig.showReturnAfterStatus" :xs="24" :md="12">
            <ElFormItem label="归还后状态" prop="returnAfterStatus">
              <ElSelect v-model="formData.returnAfterStatus" class="w-full">
                <ElOption
                  v-for="item in returnAfterStatusOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </ElSelect>
            </ElFormItem>
          </ElCol>
          <ElCol :xs="24" :md="12">
            <ElFormItem :label="activeTypeConfig.amountLabel" :prop="activeTypeConfig.amountProp">
              <ElInputNumber
                v-if="activeTypeConfig.amountFieldType === 'number'"
                v-model="formData.disposalAmount"
                :min="0"
                :precision="2"
                :step="100"
                controls-position="right"
                class="w-full"
              />
              <ElDatePicker
                v-else
                v-model="formData.expectedReturnDate"
                type="date"
                value-format="YYYY-MM-DD"
                class="w-full"
              />
            </ElFormItem>
          </ElCol>
          <ElCol :xs="24">
            <ElFormItem :label="activeTypeConfig.remarkLabel" :prop="activeTypeConfig.remarkProp">
              <ElInput
                v-model="remarkField"
                type="textarea"
                :rows="3"
                maxlength="500"
                show-word-limit
              />
            </ElFormItem>
          </ElCol>
        </ElRow>
      </ElCard>

      <ElCard shadow="never">
        <template #header>
          <div class="asset-order-workbench-page__section-header">
            <div>
              <div class="asset-order-workbench-page__section-title">
                {{ activeTypeConfig.detailSectionTitle }}
              </div>
              <div class="asset-order-workbench-page__section-subtitle">
                {{ activeTypeConfig.detailSectionSubtitle }}
              </div>
            </div>
          </div>
        </template>

        <div class="asset-order-workbench-page__toolbar">
          <ElSpace wrap>
            <ElButton type="primary" plain @click="openSelectorDialog">选择资产</ElButton>
            <ElButton :disabled="!formData.itemList.length" @click="handleClearItems"
              >清空明细</ElButton
            >
          </ElSpace>
          <ElTag type="info" effect="light">已选 {{ formData.itemList.length }} 项</ElTag>
        </div>

        <ElTable :data="formData.itemList" border>
          <ElTableColumn prop="assetCode" label="资产编码" min-width="160" />
          <ElTableColumn prop="assetName" label="资产名称" min-width="180" />
          <ElTableColumn label="变更前状态" width="120" align="center">
            <template #default="{ row }">
              <DictTag :options="asset_status" :value="row.beforeStatus" />
            </template>
          </ElTableColumn>
          <ElTableColumn label="变更后状态" width="120" align="center">
            <template #default="{ row }">
              <DictTag :options="asset_status" :value="row.afterStatus" />
            </template>
          </ElTableColumn>
          <ElTableColumn label="目标责任人" min-width="140">
            <template #default="{ row }">{{ formatUserName(row.afterUserId) }}</template>
          </ElTableColumn>
          <ElTableColumn label="目标位置" min-width="160">
            <template #default="{ row }">{{ formatLocationName(row.afterLocationId) }}</template>
          </ElTableColumn>
          <ElTableColumn label="行级信息" min-width="220">
            <template #default="{ row }">
              <ElInput
                v-model="row.itemResult"
                maxlength="200"
                placeholder="例如：需单独跟进、特殊说明"
              />
            </template>
          </ElTableColumn>
          <ElTableColumn label="操作" width="88" align="center">
            <template #default="{ row, $index }">
              <ElButton link type="danger" @click="handleRemoveItem(row.assetId, $index)"
                >移除</ElButton
              >
            </template>
          </ElTableColumn>
        </ElTable>
      </ElCard>
    </ElForm>

    <ElDialog
      v-model="selectorDialogOpen"
      title="选择资产"
      width="1080px"
      append-to-body
      destroy-on-close
      @open="handleSelectorOpen"
      @closed="handleSelectorClosed"
    >
      <ElTable
        ref="selectorTableRef"
        v-loading="selectorLoading"
        :data="selectorData"
        border
        @selection-change="handleSelectorSelectionChange"
      >
        <ElTableColumn type="selection" width="55" />
        <ElTableColumn prop="assetCode" label="资产编码" min-width="160" />
        <ElTableColumn prop="assetName" label="资产名称" min-width="180" />
        <ElTableColumn label="资产状态" width="120" align="center">
          <template #default="{ row }">
            <DictTag :options="asset_status" :value="row.assetStatus" />
          </template>
        </ElTableColumn>
      </ElTable>
      <template #footer>
        <div class="dialog-footer">
          <ElButton @click="selectorDialogOpen = false">取消</ElButton>
          <ElButton type="primary" @click="handleAppendItems">加入明细</ElButton>
        </div>
      </template>
    </ElDialog>
  </div>
</template>

<script setup lang="ts">
  import { computed, onMounted, reactive, ref, watch } from 'vue'
  import type { FormInstance, FormRules, TableInstance } from 'element-plus'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import { listUser, deptTreeSelect } from '@/api/system/user'
  import { treeLocationSelect } from '@/api/asset/location'
  import { listAssetInfo } from '@/api/asset/info'
  import { addAssetOrder, submitAssetOrder, updateAssetOrder } from '@/api/asset/order'
  import DictTag from '@/components/DictTag/index.vue'
  import { useDict } from '@/utils/dict'
  import { useUserStore } from '@/store/modules/user'
  import type { OrderWorkbenchContext } from '../modules/order-workbench-context'
  import { buildOrderTypeViewConfig } from '../modules/order-page-meta'

  defineOptions({ name: 'AssetOrderWorkbenchPage' })

  interface TreeOption {
    id: number
    label: string
    children?: TreeOption[]
  }

  interface UserOption {
    userId: number
    displayName: string
  }

  interface AssetCandidate {
    assetId: number
    assetCode: string
    assetName: string
    assetStatus: string
  }

  interface OrderItemForm {
    assetId: number
    assetCode: string
    assetName: string
    beforeStatus?: string
    afterStatus?: string
    afterUserId?: number
    afterLocationId?: number
    itemResult?: string
    orderId?: number
  }

  interface OrderFormData {
    orderId?: number
    orderNo: string
    orderType: string
    bizDate: string
    applyDeptId?: number
    applyUserId?: number
    toUserId?: number
    toLocationId?: number
    expectedReturnDate: string
    disposalAmount?: number
    disposalReason: string
    remark: string
    orderStatus: string
    returnAfterStatus: string
    itemList: OrderItemForm[]
  }

  const props = defineProps<{ context: OrderWorkbenchContext }>()
  const emit = defineEmits<{ (e: 'success'): void }>()
  const treeProps = { value: 'id', label: 'label', children: 'children' }
  const { asset_order_type, asset_status } = useDict('asset_order_type', 'asset_status')
  const userStore = useUserStore()
  const loading = ref(false)
  const selectorDialogOpen = ref(false)
  const selectorLoading = ref(false)
  const formRef = ref<FormInstance>()
  const selectorTableRef = ref<TableInstance>()
  const deptOptions = ref<TreeOption[]>([])
  const locationOptions = ref<TreeOption[]>([])
  const userOptions = ref<UserOption[]>([])
  const userLabelMap = ref<Record<string, string>>({})
  const locationLabelMap = ref<Record<string, string>>({})
  const selectorData = ref<AssetCandidate[]>([])
  const selectorSelection = ref<AssetCandidate[]>([])
  const formData = reactive<OrderFormData>({
    orderId: undefined,
    orderNo: '',
    orderType: 'INBOUND',
    bizDate: '',
    applyDeptId: undefined,
    applyUserId: undefined,
    toUserId: undefined,
    toLocationId: undefined,
    expectedReturnDate: '',
    disposalAmount: undefined,
    disposalReason: '',
    remark: '',
    orderStatus: 'DRAFT',
    returnAfterStatus: 'IDLE',
    itemList: []
  })

  const activeTypeConfig = computed(() => {
    return buildOrderTypeViewConfig(formData.orderType)
  })
  const remarkField = computed({
    get: () =>
      activeTypeConfig.value.remarkProp === 'disposalReason'
        ? formData.disposalReason
        : formData.remark,
    set: (value: string) => {
      if (activeTypeConfig.value.remarkProp === 'disposalReason') {
        formData.disposalReason = value
      } else {
        formData.remark = value
      }
    }
  })
  const returnAfterStatusOptions = computed(() =>
    (asset_status.value || []).filter((item: any) =>
      ['IDLE', 'IN_USE', 'REPAIRING'].includes(String(item.value))
    )
  )
  const bridgeHintText = computed(() =>
    props.context?.repairNo ? `已从维修流程带入上下文：${props.context.repairNo}` : ''
  )
  const formRules = computed<FormRules>(() => ({
    orderType: [{ required: true, message: '单据类型不能为空', trigger: 'change' }],
    bizDate: [{ required: true, message: '业务时间不能为空', trigger: 'change' }],
    applyDeptId: [{ required: true, message: '发起部门不能为空', trigger: 'change' }],
    applyUserId: [{ required: true, message: '发起人不能为空', trigger: 'change' }],
    disposalAmount: [
      {
        validator: (_: any, value: any, callback: (error?: Error) => void) => {
          if (!activeTypeConfig.value.requiresDisposalFields) {
            callback()
            return
          }
          if (value === null || value === undefined || value === '') {
            callback(new Error('处置金额不能为空'))
            return
          }
          callback()
        },
        trigger: ['blur', 'change']
      }
    ],
    disposalReason: [
      {
        validator: (_: any, value: any, callback: (error?: Error) => void) => {
          if (!activeTypeConfig.value.requiresDisposalFields) {
            callback()
            return
          }
          if (!String(value ?? '').trim()) {
            callback(new Error('报废原因不能为空'))
            return
          }
          callback()
        },
        trigger: ['blur', 'change']
      }
    ]
  }))

  // 页面版和弹层版共享同一套基础选项，避免人、位置、状态口径再次漂移。
  const loadOptions = async () => {
    const [deptRes, locationRes, userRes] = await Promise.all([
      deptTreeSelect(),
      treeLocationSelect(),
      listUser({ pageNum: 1, pageSize: 500 })
    ])
    const deptData = Array.isArray(deptRes) ? deptRes : (deptRes as any)?.data || []
    const locationData = Array.isArray(locationRes) ? locationRes : (locationRes as any)?.data || []
    const userData = Array.isArray(userRes)
      ? userRes
      : (userRes as any)?.rows || (userRes as any)?.data || []

    deptOptions.value = (deptData || []).map((item: any) => ({
      id: item.id ?? item.deptId ?? item.value,
      label: item.label ?? item.deptName ?? item.name,
      children: item.children || []
    }))
    locationOptions.value = (locationData || []).map((item: any) => ({
      id: item.id ?? item.locationId ?? item.value,
      label: item.label ?? item.locationName ?? item.name,
      children: item.children || []
    }))
    userOptions.value = userData.map((item: any) => ({
      userId: item.userId,
      displayName: item.nickName ? `${item.nickName} (${item.userName})` : item.userName
    }))
    userLabelMap.value = userOptions.value.reduce<Record<string, string>>((acc, item) => {
      acc[String(item.userId)] = item.displayName
      return acc
    }, {})
    locationLabelMap.value = locationOptions.value.reduce<Record<string, string>>((acc, item) => {
      acc[String(item.id)] = item.label
      return acc
    }, {})
  }

  const buildAfterStatus = () => {
    switch (activeTypeConfig.value.afterStatusStrategy) {
      case 'DISPOSED':
        return 'DISPOSED'
      case 'RETURN_AFTER':
        return formData.returnAfterStatus
      default:
        return 'IN_USE'
    }
  }

  const applyContext = () => {
    formData.orderId = undefined
    formData.orderNo = ''
    formData.bizDate = ''
    formData.expectedReturnDate = ''
    formData.disposalAmount = undefined
    formData.disposalReason = ''
    formData.remark = ''
    formData.orderStatus = 'DRAFT'
    formData.returnAfterStatus = 'IDLE'
    formData.itemList = []
    formData.orderType = String(props.context?.orderType || 'INBOUND').toUpperCase()
    formData.applyUserId = (userStore.getUserInfo as any)?.userId
    formData.applyDeptId = (userStore.getUserInfo as any)?.deptId
    if (props.context?.toUserId) formData.toUserId = Number(props.context.toUserId)
    if (props.context?.toLocationId) formData.toLocationId = Number(props.context.toLocationId)
    if (props.context?.assetId) {
      formData.itemList = [
        {
          assetId: Number(props.context.assetId),
          assetCode: String(props.context.assetCode || ''),
          assetName: String(props.context.assetName || ''),
          beforeStatus: String(props.context.beforeStatus || props.context.assetStatus || ''),
          afterStatus: buildAfterStatus(),
          afterUserId: formData.toUserId,
          afterLocationId: formData.toLocationId,
          itemResult: props.context.repairNo ? `来源维修单：${props.context.repairNo}` : ''
        }
      ]
    }
    if (props.context?.repairNo && activeTypeConfig.value.remarkProp === 'disposalReason') {
      formData.disposalReason = `来源维修单【${props.context.repairNo}】建议报废`
    }
  }

  const formatUserName = (userId?: number) => {
    if (userId === null || userId === undefined) return '-'
    return userLabelMap.value[String(userId)] || String(userId)
  }

  const formatLocationName = (locationId?: number) => {
    if (locationId === null || locationId === undefined) return '-'
    return locationLabelMap.value[String(locationId)] || String(locationId)
  }

  const openSelectorDialog = () => {
    selectorDialogOpen.value = true
  }

  const handleSelectorOpen = async () => {
    selectorSelection.value = []
    selectorLoading.value = true
    try {
      const response: any = await listAssetInfo({ pageNum: 1, pageSize: 20 })
      selectorData.value = response?.rows || response?.data?.rows || response?.data || []
    } finally {
      selectorLoading.value = false
    }
  }

  const handleSelectorClosed = () => {
    selectorSelection.value = []
  }

  const handleSelectorSelectionChange = (rows: AssetCandidate[]) => {
    selectorSelection.value = rows
  }

  const handleAppendItems = () => {
    if (!selectorSelection.value.length) {
      ElMessage.warning('请至少选择一项资产')
      return
    }

    const map = new Map<number, OrderItemForm>()
    formData.itemList.forEach((item) => map.set(item.assetId, item))
    selectorSelection.value.forEach((asset) => {
      map.set(asset.assetId, {
        assetId: asset.assetId,
        assetCode: asset.assetCode,
        assetName: asset.assetName,
        beforeStatus: asset.assetStatus,
        afterStatus: buildAfterStatus(),
        afterUserId: formData.toUserId,
        afterLocationId: formData.toLocationId,
        itemResult: ''
      })
    })
    formData.itemList = Array.from(map.values())
    selectorDialogOpen.value = false
    ElMessage.success(`已加入 ${selectorSelection.value.length} 项资产`)
  }

  const handleRemoveItem = async (assetId?: number, index?: number) => {
    if (assetId === undefined && index === undefined) return
    try {
      await ElMessageBox.confirm('确认将该资产从单据明细中移除吗？', '提示', { type: 'warning' })
      formData.itemList = formData.itemList.filter((item, currentIndex) =>
        assetId !== undefined ? item.assetId !== assetId : currentIndex !== index
      )
    } catch (error) {
      if (error !== 'cancel') {
        console.error('移除单据明细失败:', error)
      }
    }
  }

  const handleClearItems = async () => {
    try {
      await ElMessageBox.confirm('确认清空当前单据明细吗？', '提示', { type: 'warning' })
      formData.itemList = []
    } catch (error) {
      if (error !== 'cancel') {
        console.error('清空单据明细失败:', error)
      }
    }
  }

  const buildSubmitPayload = () => ({
    ...formData,
    itemList: formData.itemList.map((item) => ({ ...item }))
  })

  const getDraftPayload = () => JSON.parse(JSON.stringify(buildSubmitPayload()))

  const applyDraftPayload = (payload?: Record<string, any>) => {
    if (!payload) return
    Object.assign(formData, payload)
    formData.itemList = Array.isArray(payload.itemList) ? (payload.itemList as OrderItemForm[]) : []
  }

  const persistOrder = async (successMessage: string) => {
    if (!formRef.value) return undefined
    const valid = await formRef.value.validate().catch(() => false)
    if (!valid) return undefined
    if (!formData.itemList.length) {
      ElMessage.warning('请至少选择一项资产后再保存单据')
      return undefined
    }

    loading.value = true
    try {
      const response: any = formData.orderId
        ? await updateAssetOrder(buildSubmitPayload())
        : await addAssetOrder(buildSubmitPayload())
      const orderId = response?.orderId || response?.data?.orderId
      const orderNo = response?.orderNo || response?.data?.orderNo
      if (orderId) formData.orderId = orderId
      if (orderNo) formData.orderNo = orderNo
      ElMessage.success(successMessage)
      emit('success')
      return {
        orderId: formData.orderId,
        orderNo: formData.orderNo,
        orderStatus: formData.orderStatus
      }
    } finally {
      loading.value = false
    }
  }

  const saveDraftOrder = async () => persistOrder(formData.orderId ? '草稿已更新' : '草稿已保存')

  const submitFlowOrder = async () => {
    const saveResult = await persistOrder(
      formData.orderId ? '单据已保存，正在提交' : '单据已创建，正在提交'
    )
    if (!saveResult?.orderId) return undefined
    if (!['DRAFT', 'REJECTED'].includes(formData.orderStatus)) return saveResult

    loading.value = true
    try {
      await submitAssetOrder(saveResult.orderId)
      formData.orderStatus = 'SUBMITTED'
      ElMessage.success('提交流程成功')
      emit('success')
      return { ...saveResult, orderStatus: 'SUBMITTED' }
    } finally {
      loading.value = false
    }
  }

  watch(
    () => props.context,
    () => {
      applyContext()
    },
    { deep: true, immediate: true }
  )

  watch(
    () => [
      formData.orderType,
      formData.toUserId,
      formData.toLocationId,
      formData.returnAfterStatus
    ],
    () => {
      formData.itemList = formData.itemList.map((item) => ({
        ...item,
        afterStatus: buildAfterStatus(),
        afterUserId: formData.toUserId,
        afterLocationId: formData.toLocationId
      }))
    }
  )

  onMounted(async () => {
    loading.value = true
    try {
      await loadOptions()
      applyContext()
    } finally {
      loading.value = false
    }
  })

  defineExpose({
    saveDraftOrder,
    submitFlowOrder,
    getDraftPayload,
    applyDraftPayload
  })
</script>

<style lang="scss" scoped>
  .asset-order-workbench-page {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .asset-order-workbench-page__tip {
    border-radius: 16px;
  }

  .asset-order-workbench-page__form {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .asset-order-workbench-page__section-header {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 16px;
  }

  .asset-order-workbench-page__section-title,
  .section-title {
    font-size: 16px;
    font-weight: 600;
    line-height: 1.4;
    color: rgb(15 23 42);
  }

  .asset-order-workbench-page__section-subtitle {
    margin-top: 6px;
    font-size: 13px;
    line-height: 1.7;
    color: rgb(100 116 139);
  }

  .asset-order-workbench-page__toolbar,
  .dialog-footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
    margin-bottom: 16px;
  }

  @media (max-width: 768px) {
    .asset-order-workbench-page__toolbar,
    .dialog-footer {
      flex-direction: column;
      align-items: stretch;
    }
  }
</style>
