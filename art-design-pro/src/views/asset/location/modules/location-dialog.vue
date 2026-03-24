<template>
  <ElDialog
    :title="dialogType === 'add' ? '新增存放位置' : '修改存放位置'"
    v-model="visible"
    width="760px"
    destroy-on-close
    append-to-body
    @closed="handleClosed"
  >
    <ElForm
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="110px"
      v-loading="loading"
    >
      <ElRow :gutter="16">
        <ElCol :span="24">
          <ElFormItem label="上级位置" prop="parentId">
            <ElTreeSelect
              v-model="formData.parentId"
              :data="locationOptions"
              :props="{ value: 'id', label: 'label', children: 'children', disabled: 'disabled' }"
              value-key="id"
              check-strictly
              filterable
              clearable
              :render-after-expand="false"
              class="w-full"
              placeholder="请选择上级位置"
            />
          </ElFormItem>
        </ElCol>

        <ElCol :span="12">
          <ElFormItem label="位置编码" prop="locationCode">
            <ElInput v-model="formData.locationCode" maxlength="64" placeholder="请输入位置编码" />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="位置名称" prop="locationName">
            <ElInput v-model="formData.locationName" maxlength="100" placeholder="请输入位置名称" />
          </ElFormItem>
        </ElCol>

        <ElCol :span="12">
          <ElFormItem label="位置类型" prop="locationType">
            <ElSelect v-model="formData.locationType" class="w-full" placeholder="请选择位置类型">
              <ElOption
                v-for="item in asset_location_type"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </ElSelect>
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="显示排序" prop="orderNum">
            <ElInputNumber
              v-model="formData.orderNum"
              :min="0"
              controls-position="right"
              class="w-full"
            />
          </ElFormItem>
        </ElCol>

        <ElCol :span="12">
          <ElFormItem label="所属部门" prop="deptId">
            <ElTreeSelect
              v-model="formData.deptId"
              :data="deptOptions"
              :props="{ value: 'id', label: 'label', children: 'children' }"
              value-key="id"
              filterable
              clearable
              check-strictly
              class="w-full"
              placeholder="请选择所属部门"
            />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="管理员" prop="managerUserId">
            <ElSelect
              v-model="formData.managerUserId"
              filterable
              clearable
              class="w-full"
              placeholder="请选择位置管理员"
            >
              <ElOption
                v-for="item in userOptions"
                :key="item.userId"
                :label="item.userName"
                :value="item.userId"
              />
            </ElSelect>
          </ElFormItem>
        </ElCol>

        <ElCol :span="12">
          <ElFormItem label="位置状态" prop="status">
            <ElRadioGroup v-model="formData.status">
              <ElRadio v-for="item in sys_normal_disable" :key="item.value" :label="item.value">
                {{ item.label }}
              </ElRadio>
            </ElRadioGroup>
          </ElFormItem>
        </ElCol>

        <ElCol :span="24">
          <ElFormItem label="备注" prop="remark">
            <ElInput
              v-model="formData.remark"
              type="textarea"
              :rows="3"
              maxlength="255"
              show-word-limit
              placeholder="请输入位置说明"
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
  import { reactive, ref, watch } from 'vue'
  import type { FormRules } from 'element-plus'
  import { ElMessage } from 'element-plus'
  import { listUser } from '@/api/system/user'
  import { deptTreeSelect } from '@/api/system/user'
  import { useDict } from '@/utils/dict'
  import {
    addLocation,
    getLocation,
    treeLocationSelect,
    updateLocation
  } from '@/api/asset/location'

  interface TreeOption {
    id: number
    label: string
    disabled?: boolean
    children?: TreeOption[]
  }

  interface UserOption {
    userId: number
    userName: string
  }

  const { sys_normal_disable, asset_location_type } = useDict(
    'sys_normal_disable',
    'asset_location_type'
  )

  const props = defineProps<{
    modelValue: boolean
    dialogType: 'add' | 'edit'
    locationData?: any
  }>()

  const emit = defineEmits<{
    (e: 'update:modelValue', value: boolean): void
    (e: 'success'): void
  }>()

  const visible = ref(false)
  const loading = ref(false)
  const submitLoading = ref(false)
  const formRef = ref()
  const locationOptions = ref<TreeOption[]>([])
  const deptOptions = ref<TreeOption[]>([])
  const userOptions = ref<UserOption[]>([])

  const initialFormData = {
    locationId: undefined as number | undefined,
    parentId: 0,
    locationCode: '',
    locationName: '',
    locationType: '',
    deptId: undefined as number | undefined,
    managerUserId: undefined as number | undefined,
    orderNum: 0,
    status: '0',
    remark: ''
  }

  const formData = reactive({ ...initialFormData })

  const formRules: FormRules = {
    parentId: [{ required: true, message: '上级位置不能为空', trigger: 'change' }],
    locationCode: [{ required: true, message: '位置编码不能为空', trigger: 'blur' }],
    locationName: [{ required: true, message: '位置名称不能为空', trigger: 'blur' }],
    orderNum: [{ required: true, message: '显示排序不能为空', trigger: 'blur' }],
    status: [{ required: true, message: '位置状态不能为空', trigger: 'change' }]
  }

  const buildRootOption = (label: string, list: TreeOption[]) => [
    {
      id: 0,
      label,
      children: list || []
    }
  ]

  /**
   * 预加载弹窗依赖数据，避免用户打开弹窗后还要等待多次请求。
   */
  const loadBaseOptions = async () => {
    try {
      const [locationRes, deptRes, userRes] = await Promise.all([
        treeLocationSelect(),
        deptTreeSelect(),
        listUser({ pageNum: 1, pageSize: 200 })
      ])

      const locationResponse = locationRes as any
      const deptResponse = deptRes as any
      const userResponse = userRes as any
      const locationData = Array.isArray(locationResponse)
        ? locationResponse
        : locationResponse?.data || []
      const deptData = Array.isArray(deptResponse) ? deptResponse : deptResponse?.data || []
      const userData = Array.isArray(userResponse)
        ? userResponse
        : userResponse?.rows || userResponse?.data || []

      locationOptions.value = buildRootOption('顶级位置', locationData)
      deptOptions.value = buildRootOption('全部部门', deptData)
      userOptions.value = userData.map((item: any) => ({
        userId: item.userId,
        userName: item.nickName ? `${item.nickName} (${item.userName})` : item.userName
      }))
    } catch (error) {
      console.error('加载位置弹窗基础数据失败:', error)
      locationOptions.value = buildRootOption('顶级位置', [])
      deptOptions.value = buildRootOption('全部部门', [])
      userOptions.value = []
    }
  }

  watch(
    () => props.modelValue,
    async (value) => {
      visible.value = value
      if (!value) return

      await loadBaseOptions()

      if (props.dialogType === 'edit' && props.locationData?.locationId) {
        loading.value = true
        try {
          const detail: any = await getLocation(props.locationData.locationId)
          Object.assign(formData, initialFormData, detail || {})
          if (formData.parentId === null || formData.parentId === undefined) {
            formData.parentId = 0
          }
        } finally {
          loading.value = false
        }
        return
      }

      Object.assign(formData, initialFormData)
      if (props.locationData?.locationId) {
        formData.parentId = props.locationData.locationId
      }
    }
  )

  watch(
    () => visible.value,
    (value) => {
      emit('update:modelValue', value)
    }
  )

  /**
   * 提交位置表单。
   */
  const handleSubmit = async () => {
    if (!formRef.value) return

    await formRef.value.validate(async (valid: boolean) => {
      if (!valid) return

      submitLoading.value = true
      try {
        if (props.dialogType === 'edit') {
          await updateLocation(formData)
          ElMessage.success('修改成功')
        } else {
          await addLocation(formData)
          ElMessage.success('新增成功')
        }
        visible.value = false
        emit('success')
      } finally {
        submitLoading.value = false
      }
    })
  }

  /**
   * 关闭后清理表单和选项，保证下一次打开仍然从干净状态开始。
   */
  const handleClosed = () => {
    formRef.value?.resetFields()
    Object.assign(formData, initialFormData)
    locationOptions.value = []
    deptOptions.value = []
    userOptions.value = []
  }
</script>

<style scoped></style>
