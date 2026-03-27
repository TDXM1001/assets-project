<template>
  <ElDialog
    :title="dialogType === 'add' ? '新增业务单据' : '编辑业务单据'"
    v-model="visible"
    width="1080px"
    destroy-on-close
    append-to-body
    @closed="handleClosed"
  >
    <ElAlert
      class="mb-4"
      type="info"
      show-icon
      :closable="false"
      title="业务单据会根据类型驱动后续流程，当前先保留完整表单骨架，后端联调时再逐步收窄字段。"
    />

    <ElForm
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="112px"
      v-loading="loading"
    >
      <ElDivider content-position="left">基础信息</ElDivider>
      <ElRow :gutter="16">
        <ElCol :span="12">
          <ElFormItem label="单据编号" prop="orderNo">
            <ElInput
              v-model="formData.orderNo"
              placeholder="提交后由后端生成"
              maxlength="64"
              disabled
            />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="单据类型" prop="orderType">
            <ElSelect v-model="formData.orderType" class="w-full" placeholder="请选择单据类型">
              <ElOption
                v-for="item in asset_order_type"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </ElSelect>
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
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
        <ElCol :span="12">
          <ElFormItem label="单据状态" prop="orderStatus">
            <ElSelect v-model="formData.orderStatus" class="w-full" disabled>
              <ElOption
                v-for="item in asset_order_status"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </ElSelect>
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
      </ElRow>

      <ElDivider content-position="left">流转范围</ElDivider>
      <ElRow :gutter="16">
        <ElCol :span="12">
          <ElFormItem label="来源部门" prop="fromDeptId">
            <ElTreeSelect
              v-model="formData.fromDeptId"
              :data="deptOptions"
              :props="{ value: 'id', label: 'label', children: 'children' }"
              value-key="id"
              filterable
              clearable
              check-strictly
              class="w-full"
              placeholder="请选择来源部门"
            />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="目标部门" prop="toDeptId">
            <ElTreeSelect
              v-model="formData.toDeptId"
              :data="deptOptions"
              :props="{ value: 'id', label: 'label', children: 'children' }"
              value-key="id"
              filterable
              clearable
              check-strictly
              class="w-full"
              placeholder="请选择目标部门"
            />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="来源责任人" prop="fromUserId">
            <ElSelect v-model="formData.fromUserId" filterable clearable class="w-full">
              <ElOption
                v-for="item in userOptions"
                :key="item.userId"
                :label="item.displayName"
                :value="item.userId"
              />
            </ElSelect>
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="目标责任人" prop="toUserId">
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
        <ElCol :span="12">
          <ElFormItem label="来源位置" prop="fromLocationId">
            <ElTreeSelect
              v-model="formData.fromLocationId"
              :data="locationOptions"
              :props="{ value: 'id', label: 'label', children: 'children' }"
              value-key="id"
              filterable
              clearable
              check-strictly
              class="w-full"
              placeholder="请选择来源位置"
            />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="目标位置" prop="toLocationId">
            <ElTreeSelect
              v-model="formData.toLocationId"
              :data="locationOptions"
              :props="{ value: 'id', label: 'label', children: 'children' }"
              value-key="id"
              filterable
              clearable
              check-strictly
              class="w-full"
              placeholder="请选择目标位置"
            />
          </ElFormItem>
        </ElCol>
      </ElRow>

      <ElDivider content-position="left">业务补充</ElDivider>
      <ElRow :gutter="16">
        <ElCol :span="12">
          <ElFormItem label="预计归还日" prop="expectedReturnDate">
            <ElDatePicker
              v-model="formData.expectedReturnDate"
              type="date"
              value-format="YYYY-MM-DD"
              class="w-full"
              placeholder="请选择预计归还日"
            />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="处置金额" prop="disposalAmount">
            <ElInputNumber
              v-model="formData.disposalAmount"
              :min="0"
              :precision="2"
              :step="100"
              controls-position="right"
              class="w-full"
            />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="附件数量" prop="attachmentCount">
            <ElInputNumber
              v-model="formData.attachmentCount"
              :min="0"
              controls-position="right"
              class="w-full"
            />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="审批结果" prop="approveResult">
            <ElInput v-model="formData.approveResult" disabled placeholder="审批通过后回填" />
          </ElFormItem>
        </ElCol>
        <ElCol :span="24">
          <ElFormItem label="报废原因" prop="disposalReason">
            <ElInput
              v-model="formData.disposalReason"
              type="textarea"
              :rows="2"
              maxlength="500"
              show-word-limit
              placeholder="当单据类型为报废时填写原因"
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
              placeholder="请输入备注说明"
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
  </ElDialog>
</template>

<script setup lang="ts">
  import { computed, reactive, ref, watch } from 'vue'
  import type { FormRules } from 'element-plus'
  import { ElMessage } from 'element-plus'
  import { listUser, deptTreeSelect } from '@/api/system/user'
  import { treeLocationSelect } from '@/api/asset/location'
  import { addAssetOrder, getAssetOrder, updateAssetOrder } from '@/api/asset/order'
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

  const { asset_order_type, asset_order_status } = useDict('asset_order_type', 'asset_order_status')
  const userStore = useUserStore()
  const currentUserInfo = computed(() => userStore.getUserInfo)

  const props = defineProps<{
    modelValue: boolean
    dialogType: 'add' | 'edit'
    orderData?: any
  }>()

  const emit = defineEmits<{
    (e: 'update:modelValue', value: boolean): void
    (e: 'success'): void
  }>()

  const visible = ref(false)
  const loading = ref(false)
  const submitLoading = ref(false)
  const formRef = ref()
  const deptOptions = ref<TreeOption[]>([])
  const locationOptions = ref<TreeOption[]>([])
  const userOptions = ref<UserOption[]>([])

  const createInitialFormData = () => ({
    orderId: undefined as number | undefined,
    orderNo: '',
    orderType: 'INBOUND',
    bizDate: '',
    applyUserId: undefined as number | undefined,
    applyDeptId: undefined as number | undefined,
    fromDeptId: undefined as number | undefined,
    toDeptId: undefined as number | undefined,
    fromUserId: undefined as number | undefined,
    toUserId: undefined as number | undefined,
    fromLocationId: undefined as number | undefined,
    toLocationId: undefined as number | undefined,
    expectedReturnDate: '',
    disposalReason: '',
    disposalAmount: undefined as number | undefined,
    attachmentCount: 0,
    orderStatus: 'DRAFT',
    approveResult: '',
    remark: ''
  })

  const formData = reactive(createInitialFormData())

  const formRules: FormRules = {
    orderType: [{ required: true, message: '单据类型不能为空', trigger: 'change' }],
    bizDate: [{ required: true, message: '业务时间不能为空', trigger: 'change' }],
    applyUserId: [{ required: true, message: '发起人不能为空', trigger: 'change' }],
    applyDeptId: [{ required: true, message: '发起部门不能为空', trigger: 'change' }]
  }

  /**
   * 将树形返回值统一成 TreeSelect 可用的数据结构。
   */
  const normalizeTreeOptions = (list: any[]): TreeOption[] => {
    return (list || []).map((item) => ({
      id: item.id ?? item.value ?? item.deptId ?? item.locationId,
      label: item.label ?? item.deptName ?? item.locationName ?? item.name ?? String(item.id),
      children: item.children ? normalizeTreeOptions(item.children) : []
    }))
  }

  /**
   * 加载弹窗需要的部门、位置和用户候选项。
   * 这里保留兼容式解析，避免后端返回结构稍有差异就打不开页面。
   */
  const loadRelationOptions = async () => {
    try {
      const [deptRes, locationRes, userRes] = await Promise.all([
        deptTreeSelect(),
        treeLocationSelect(),
        listUser({ pageNum: 1, pageSize: 500 })
      ])

      const deptResponse = deptRes as any
      const locationResponse = locationRes as any
      const userResponse = userRes as any

      const deptData = Array.isArray(deptResponse) ? deptResponse : deptResponse?.data || []
      const locationData = Array.isArray(locationResponse)
        ? locationResponse
        : locationResponse?.data || []
      const userData = Array.isArray(userResponse)
        ? userResponse
        : userResponse?.rows || userResponse?.data || []

      deptOptions.value = normalizeTreeOptions(deptData)
      locationOptions.value = normalizeTreeOptions(locationData)
      userOptions.value = userData.map((item: any) => ({
        userId: item.userId,
        displayName: item.nickName ? `${item.nickName} (${item.userName})` : item.userName
      }))
    } catch (error) {
      console.error('加载单据弹窗关联选项失败:', error)
      deptOptions.value = []
      locationOptions.value = []
      userOptions.value = []
    }
  }

  /**
   * 给新增场景准备一个合理的默认发起人。
   * 如果当前登录用户信息可用，就先帮用户填上，减少重复输入。
   */
  const fillDefaultApplicant = () => {
    const userInfo = currentUserInfo.value || {}
    if (userInfo.userId) {
      formData.applyUserId = userInfo.userId
    }
  }

  const resetForm = () => {
    Object.assign(formData, createInitialFormData())
    fillDefaultApplicant()
  }

  watch(
    () => props.modelValue,
    async (value) => {
      visible.value = value
      if (!value) {
        return
      }

      await loadRelationOptions()

      if (props.dialogType === 'edit' && props.orderData?.orderId) {
        loading.value = true
        try {
          const detail: any = await getAssetOrder(props.orderData.orderId)
          Object.assign(
            formData,
            createInitialFormData(),
            detail?.data || detail || props.orderData
          )
        } catch (error) {
          console.error('获取业务单据详情失败，改为使用列表行数据:', error)
          Object.assign(formData, createInitialFormData(), props.orderData || {})
        } finally {
          loading.value = false
        }
        return
      }

      resetForm()
    },
    { immediate: true }
  )

  watch(
    () => visible.value,
    (value) => {
      emit('update:modelValue', value)
    }
  )

  /**
   * 提交表单。
   * 新增和修改共用同一套骨架，后端联调完成后直接走真实接口即可。
   */
  const handleSubmit = async () => {
    if (!formRef.value) return

    await formRef.value.validate(async (valid: boolean) => {
      if (!valid) {
        return
      }

      submitLoading.value = true
      try {
        const payload = { ...formData }
        if (props.dialogType === 'add') {
          await addAssetOrder(payload)
        } else {
          await updateAssetOrder(payload)
        }
        ElMessage.success(props.dialogType === 'add' ? '新增成功' : '修改成功')
        visible.value = false
        emit('success')
      } catch (error) {
        console.error('提交业务单据失败:', error)
      } finally {
        submitLoading.value = false
      }
    })
  }

  const handleClosed = () => {
    resetForm()
    loading.value = false
    submitLoading.value = false
    formRef.value?.clearValidate?.()
  }
</script>
