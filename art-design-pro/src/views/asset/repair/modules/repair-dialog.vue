<template>
  <ElDialog
    v-model="visible"
    :title="dialogType === 'add' ? '新增维修单' : '编辑维修单'"
    width="1160px"
    destroy-on-close
    append-to-body
    @closed="handleClosed"
  >
    <ElAlert
      class="mb-4"
      type="info"
      show-icon
      :closable="false"
      title="维修单用于记录资产报修、审批和维修完成过程。一期先聚焦单资产维修主线，确保状态联动和维修流水真实可追踪。"
    />

    <ElForm
      ref="formRef"
      v-loading="loading"
      :model="formData"
      :rules="formRules"
      label-width="110px"
    >
      <ElDivider content-position="left">基础信息</ElDivider>
      <ElRow :gutter="16">
        <ElCol :span="12">
          <ElFormItem label="维修单号" prop="repairNo">
            <ElInput v-model="formData.repairNo" disabled placeholder="提交后由系统生成" />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="报修时间" prop="reportTime">
            <ElDatePicker
              v-model="formData.reportTime"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
              class="w-full"
              placeholder="请选择报修时间"
            />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="维修方式" prop="repairMode">
            <ElSelect v-model="formData.repairMode" class="w-full" placeholder="请选择维修方式">
              <ElOption
                v-for="item in repairModeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </ElSelect>
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="外部供应商" prop="vendorName">
            <ElInput
              v-model="formData.vendorName"
              maxlength="100"
              placeholder="请输入供应商名称，内部维修可留空"
            />
          </ElFormItem>
        </ElCol>
      </ElRow>

      <ElDivider content-position="left">发起信息</ElDivider>
      <ElRow :gutter="16">
        <ElCol :span="12">
          <ElFormItem label="发起部门" prop="applyDeptId">
            <ElTreeSelect
              v-model="formData.applyDeptId"
              :data="deptOptions"
              :props="{ value: 'id', label: 'label', children: 'children' }"
              value-key="id"
              filterable
              clearable
              check-strictly
              class="w-full"
              placeholder="请选择发起部门"
            />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="发起人" prop="applyUserId">
            <ElSelect
              v-model="formData.applyUserId"
              filterable
              clearable
              class="w-full"
              placeholder="请选择发起人"
            >
              <ElOption
                v-for="item in userOptions"
                :key="item.userId"
                :label="item.displayName"
                :value="item.userId"
              />
            </ElSelect>
          </ElFormItem>
        </ElCol>
      </ElRow>

      <ElDivider content-position="left">维修资产</ElDivider>
      <ElAlert
        class="mb-4"
        type="warning"
        show-icon
        :closable="false"
        title="一期每张维修单只绑定一条资产，先把报修、审批、完成和状态联动跑通。"
      />
      <div class="repair-asset-toolbar">
        <ElSpace wrap>
          <ElButton type="primary" plain @click="selectorVisible = true">选择资产</ElButton>
          <ElButton :disabled="!formData.assetId" @click="handleClearAsset">清空资产</ElButton>
        </ElSpace>
      </div>
      <ElDescriptions :column="2" border>
        <ElDescriptionsItem label="资产编码">{{
          displayText(formData.assetCode)
        }}</ElDescriptionsItem>
        <ElDescriptionsItem label="资产名称">{{
          displayText(formData.assetName)
        }}</ElDescriptionsItem>
        <ElDescriptionsItem label="维修前状态">
          <DictTag :options="asset_status" :value="formData.beforeStatus" />
        </ElDescriptionsItem>
        <ElDescriptionsItem label="当前责任人">
          {{ displayText(resolveUserName(formData.currentUserId)) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="当前部门">
          {{ displayText(resolveDeptName(formData.currentDeptId)) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="当前位置">
          {{ displayText(resolveLocationName(formData.currentLocationId)) }}
        </ElDescriptionsItem>
      </ElDescriptions>

      <ElDivider content-position="left">故障信息</ElDivider>
      <ElRow :gutter="16">
        <ElCol :span="24">
          <ElFormItem label="故障描述" prop="faultDesc">
            <ElInput
              v-model="formData.faultDesc"
              type="textarea"
              :rows="4"
              maxlength="500"
              show-word-limit
              placeholder="请输入故障现象、影响范围和现场情况"
            />
          </ElFormItem>
        </ElCol>
        <ElCol :span="24">
          <ElFormItem label="备注" prop="remark">
            <ElInput
              v-model="formData.remark"
              type="textarea"
              :rows="3"
              maxlength="500"
              show-word-limit
              placeholder="请输入补充说明，例如报修来源、现场联系人或配件情况"
            />
          </ElFormItem>
        </ElCol>
      </ElRow>
    </ElForm>

    <template #footer>
      <div class="dialog-footer">
        <ElButton @click="visible = false">取消</ElButton>
        <ElButton type="primary" :loading="submitLoading" @click="handleSubmit">确定</ElButton>
      </div>
    </template>

    <ElDialog
      v-model="selectorVisible"
      title="选择维修资产"
      width="1080px"
      append-to-body
      destroy-on-close
      @open="handleSelectorOpen"
      @closed="handleSelectorClosed"
    >
      <ElForm :model="selectorQuery" inline class="repair-selector-search">
        <ElFormItem label="资产编码">
          <ElInput v-model="selectorQuery.assetCode" clearable placeholder="请输入资产编码" />
        </ElFormItem>
        <ElFormItem label="资产名称">
          <ElInput v-model="selectorQuery.assetName" clearable placeholder="请输入资产名称" />
        </ElFormItem>
        <ElFormItem label="资产状态">
          <ElSelect
            v-model="selectorQuery.assetStatus"
            clearable
            placeholder="请选择资产状态"
            style="width: 160px"
          >
            <ElOption
              v-for="item in asset_status"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </ElSelect>
        </ElFormItem>
        <ElFormItem>
          <ElSpace>
            <ElButton type="primary" @click="handleSelectorSearch">查询</ElButton>
            <ElButton @click="handleSelectorReset">重置</ElButton>
          </ElSpace>
        </ElFormItem>
      </ElForm>

      <ElTable
        v-loading="selectorLoading"
        :data="selectorData"
        row-key="assetId"
        border
        stripe
        height="420"
        highlight-current-row
        @current-change="handleCurrentAssetChange"
      >
        <ElTableColumn prop="assetCode" label="资产编码" min-width="140" />
        <ElTableColumn prop="assetName" label="资产名称" min-width="180" />
        <ElTableColumn label="资产状态" width="120" align="center">
          <template #default="{ row }">
            <DictTag :options="asset_status" :value="row.assetStatus" />
          </template>
        </ElTableColumn>
        <ElTableColumn label="责任人" min-width="140">
          <template #default="{ row }">{{ resolveUserName(row.currentUserId) }}</template>
        </ElTableColumn>
        <ElTableColumn label="使用部门" min-width="150">
          <template #default="{ row }">{{ resolveDeptName(row.useOrgDeptId) }}</template>
        </ElTableColumn>
        <ElTableColumn label="当前位置" min-width="160">
          <template #default="{ row }">{{ resolveLocationName(row.currentLocationId) }}</template>
        </ElTableColumn>
      </ElTable>

      <div class="repair-selector-pagination">
        <ElPagination
          background
          layout="total, sizes, prev, pager, next"
          :current-page="selectorQuery.pageNum"
          :page-size="selectorQuery.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="selectorTotal"
          @current-change="handleSelectorPageChange"
          @size-change="handleSelectorSizeChange"
        />
      </div>

      <template #footer>
        <div class="dialog-footer">
          <ElButton @click="selectorVisible = false">取消</ElButton>
          <ElButton type="primary" @click="handleApplySelectedAsset">确认选择</ElButton>
        </div>
      </template>
    </ElDialog>
  </ElDialog>
</template>

<script setup lang="ts">
  import { computed, reactive, ref, watch } from 'vue'
  import type { FormInstance, FormRules } from 'element-plus'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import { deptTreeSelect, listUser } from '@/api/system/user'
  import { treeLocationSelect } from '@/api/asset/location'
  import { listAssetInfo } from '@/api/asset/info'
  import { addAssetRepair, getAssetRepair, updateAssetRepair } from '@/api/asset/repair'
  import DictTag from '@/components/DictTag/index.vue'
  import { useDict } from '@/utils/dict'
  import { useUserStore } from '@/store/modules/user'

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
    assetStatus?: string
    currentUserId?: number
    useOrgDeptId?: number
    currentLocationId?: number
  }

  const repairModeOptions = [
    { label: '内部维修', value: 'IN_HOUSE' },
    { label: '外部送修', value: 'VENDOR' },
    { label: '上门维修', value: 'ONSITE' }
  ]

  const { asset_status } = useDict('asset_status')
  const userStore = useUserStore()
  const currentUserInfo = computed(() => userStore.getUserInfo)

  const props = defineProps<{
    modelValue: boolean
    dialogType: 'add' | 'edit'
    repairData?: any
  }>()

  const emit = defineEmits<{
    (e: 'update:modelValue', value: boolean): void
    (e: 'success'): void
  }>()

  const visible = ref(false)
  const loading = ref(false)
  const submitLoading = ref(false)
  const selectorVisible = ref(false)
  const selectorLoading = ref(false)
  const formRef = ref<FormInstance>()

  const deptOptions = ref<TreeOption[]>([])
  const locationOptions = ref<TreeOption[]>([])
  const userOptions = ref<UserOption[]>([])
  const deptLabelMap = ref<Record<string, string>>({})
  const locationLabelMap = ref<Record<string, string>>({})
  const userLabelMap = ref<Record<string, string>>({})

  const selectorData = ref<AssetCandidate[]>([])
  const selectorTotal = ref(0)
  const selectedAsset = ref<AssetCandidate>()

  const createInitialFormData = () => ({
    repairId: undefined as number | undefined,
    repairNo: '',
    reportTime: '',
    repairStatus: 'DRAFT',
    repairMode: 'IN_HOUSE',
    applyDeptId: undefined as number | undefined,
    applyUserId: undefined as number | undefined,
    assetId: undefined as number | undefined,
    assetCode: '',
    assetName: '',
    beforeStatus: '',
    afterStatus: '',
    currentUserId: undefined as number | undefined,
    currentDeptId: undefined as number | undefined,
    currentLocationId: undefined as number | undefined,
    faultDesc: '',
    vendorName: '',
    repairCost: undefined as number | undefined,
    downtimeHours: undefined as number | undefined,
    reworkFlag: '0',
    remark: ''
  })

  const formData = reactive(createInitialFormData())

  const selectorQuery = reactive({
    pageNum: 1,
    pageSize: 10,
    assetCode: '',
    assetName: '',
    assetStatus: ''
  })

  const formRules: FormRules = {
    reportTime: [{ required: true, message: '请选择报修时间', trigger: 'change' }],
    repairMode: [{ required: true, message: '请选择维修方式', trigger: 'change' }],
    applyDeptId: [{ required: true, message: '请选择发起部门', trigger: 'change' }],
    applyUserId: [{ required: true, message: '请选择发起人', trigger: 'change' }],
    assetId: [{ required: true, message: '请选择维修资产', trigger: 'change' }],
    faultDesc: [{ required: true, message: '请输入故障描述', trigger: 'blur' }]
  }

  const flattenTreeLabels = (nodes: TreeOption[], map: Record<string, string>) => {
    nodes.forEach((node) => {
      map[String(node.id)] = node.label
      if (node.children?.length) {
        flattenTreeLabels(node.children, map)
      }
    })
  }

  const normalizeTreeOptions = (list: any[]): TreeOption[] =>
    (list || []).map((item) => ({
      id: item.id ?? item.value ?? item.deptId ?? item.locationId,
      label: item.label ?? item.deptName ?? item.locationName ?? item.name ?? String(item.id),
      children: item.children ? normalizeTreeOptions(item.children) : []
    }))

  const loadRelationOptions = async () => {
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

    deptOptions.value = normalizeTreeOptions(deptData)
    locationOptions.value = normalizeTreeOptions(locationData)
    userOptions.value = userData.map((item: any) => ({
      userId: item.userId,
      displayName: item.nickName ? `${item.nickName} (${item.userName})` : item.userName
    }))

    const nextDeptMap: Record<string, string> = {}
    const nextLocationMap: Record<string, string> = {}
    flattenTreeLabels(deptOptions.value, nextDeptMap)
    flattenTreeLabels(locationOptions.value, nextLocationMap)
    deptLabelMap.value = nextDeptMap
    locationLabelMap.value = nextLocationMap
    userLabelMap.value = userOptions.value.reduce<Record<string, string>>((acc, item) => {
      acc[String(item.userId)] = item.displayName
      return acc
    }, {})
  }

  const fillDefaultApplicant = () => {
    const userInfo = (currentUserInfo.value || {}) as Record<string, any>
    if (userInfo.userId) formData.applyUserId = userInfo.userId
    if (userInfo.deptId) formData.applyDeptId = userInfo.deptId
  }

  const resetForm = () => {
    Object.assign(formData, createInitialFormData())
    fillDefaultApplicant()
  }

  const displayText = (value: unknown) => {
    if (value === null || value === undefined || value === '') return '-'
    return String(value)
  }

  const resolveDeptName = (deptId?: number, fallback?: string) => {
    if (fallback) return fallback
    if (deptId === null || deptId === undefined) return '-'
    return deptLabelMap.value[String(deptId)] || String(deptId)
  }

  const resolveLocationName = (locationId?: number, fallback?: string) => {
    if (fallback) return fallback
    if (locationId === null || locationId === undefined) return '-'
    return locationLabelMap.value[String(locationId)] || String(locationId)
  }

  const resolveUserName = (userId?: number, fallback?: string) => {
    if (fallback) return fallback
    if (userId === null || userId === undefined) return '-'
    return userLabelMap.value[String(userId)] || String(userId)
  }

  const assignAsset = (asset?: AssetCandidate) => {
    if (!asset) return
    formData.assetId = asset.assetId
    formData.assetCode = asset.assetCode
    formData.assetName = asset.assetName
    formData.beforeStatus = asset.assetStatus || ''
    formData.afterStatus = asset.assetStatus || ''
    formData.currentUserId = asset.currentUserId
    formData.currentDeptId = asset.useOrgDeptId
    formData.currentLocationId = asset.currentLocationId
  }

  const loadSelectorData = async () => {
    selectorLoading.value = true
    try {
      const response: any = await listAssetInfo({ ...selectorQuery })
      selectorData.value = response?.rows || response?.data?.rows || response?.data || []
      selectorTotal.value = Number(response?.total || response?.data?.total || 0)
    } finally {
      selectorLoading.value = false
    }
  }

  const handleSelectorSearch = () => {
    selectorQuery.pageNum = 1
    void loadSelectorData()
  }

  const handleSelectorReset = () => {
    selectorQuery.pageNum = 1
    selectorQuery.assetCode = ''
    selectorQuery.assetName = ''
    selectorQuery.assetStatus = ''
    void loadSelectorData()
  }

  const handleSelectorPageChange = (pageNum: number) => {
    selectorQuery.pageNum = pageNum
    void loadSelectorData()
  }

  const handleSelectorSizeChange = (pageSize: number) => {
    selectorQuery.pageSize = pageSize
    selectorQuery.pageNum = 1
    void loadSelectorData()
  }

  const handleCurrentAssetChange = (row?: AssetCandidate) => {
    selectedAsset.value = row
  }

  const handleApplySelectedAsset = () => {
    if (!selectedAsset.value) {
      ElMessage.warning('请先选择一条资产')
      return
    }
    assignAsset(selectedAsset.value)
    selectorVisible.value = false
  }

  const handleClearAsset = async () => {
    try {
      await ElMessageBox.confirm('确认清空当前维修资产吗？', '提示', { type: 'warning' })
      formData.assetId = undefined
      formData.assetCode = ''
      formData.assetName = ''
      formData.beforeStatus = ''
      formData.afterStatus = ''
      formData.currentUserId = undefined
      formData.currentDeptId = undefined
      formData.currentLocationId = undefined
    } catch (error) {
      if (error !== 'cancel') {
        console.error('清空维修资产失败:', error)
      }
    }
  }

  const handleSelectorOpen = async () => {
    selectedAsset.value = undefined
    await loadSelectorData()
  }

  const handleSelectorClosed = () => {
    selectedAsset.value = undefined
  }

  watch(
    () => props.modelValue,
    async (value) => {
      visible.value = value
      if (!value) return

      loading.value = true
      try {
        await loadRelationOptions()
        if (props.dialogType === 'edit' && props.repairData?.repairId) {
          const detail: any = await getAssetRepair(props.repairData.repairId)
          const repair = detail?.data || detail || props.repairData
          Object.assign(formData, createInitialFormData(), repair)
        } else {
          resetForm()
        }
      } catch (error) {
        console.error('加载维修单详情失败:', error)
        if (props.dialogType === 'edit') {
          Object.assign(formData, createInitialFormData(), props.repairData || {})
        } else {
          resetForm()
        }
      } finally {
        loading.value = false
      }
    },
    { immediate: true }
  )

  watch(
    () => visible.value,
    (value) => {
      emit('update:modelValue', value)
    }
  )

  const handleSubmit = async () => {
    if (!formRef.value) return
    const valid = await formRef.value.validate().catch(() => false)
    if (!valid) return

    submitLoading.value = true
    try {
      const payload = { ...formData }
      if (props.dialogType === 'add') {
        await addAssetRepair(payload)
      } else {
        await updateAssetRepair(payload)
      }
      ElMessage.success(props.dialogType === 'add' ? '新增成功' : '修改成功')
      visible.value = false
      emit('success')
    } catch (error) {
      console.error('提交维修单失败:', error)
    } finally {
      submitLoading.value = false
    }
  }

  const handleClosed = () => {
    resetForm()
    selectorVisible.value = false
    selectedAsset.value = undefined
    selectorData.value = []
    selectorTotal.value = 0
    selectorQuery.pageNum = 1
    selectorQuery.pageSize = 10
    selectorQuery.assetCode = ''
    selectorQuery.assetName = ''
    selectorQuery.assetStatus = ''
    loading.value = false
    submitLoading.value = false
    formRef.value?.clearValidate?.()
  }
</script>

<style scoped lang="scss">
  .repair-asset-toolbar {
    display: flex;
    justify-content: space-between;
    margin-bottom: 16px;
  }

  .repair-selector-search {
    margin-bottom: 16px;
  }

  .repair-selector-pagination {
    display: flex;
    justify-content: flex-end;
    margin-top: 16px;
  }
</style>
