<template>
  <ElDialog
    :title="dialogType === 'add' ? '新建盘点任务' : '编辑盘点任务'"
    v-model="visible"
    width="980px"
    destroy-on-close
    append-to-body
    @closed="handleClosed"
  >
    <ElAlert
      class="mb-4"
      type="info"
      :closable="false"
      show-icon
      title="盘点任务的流程状态由列表页按钮驱动，表单只维护基础信息和盘点范围。"
    />

    <ElForm
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="120px"
      v-loading="loading"
    >
      <ElRow :gutter="16">
        <ElCol :span="12">
          <ElFormItem label="任务编号" prop="taskNo">
            <ElInput v-model="formData.taskNo" maxlength="64" placeholder="请输入盘点任务编号" />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="任务名称" prop="taskName">
            <ElInput v-model="formData.taskName" maxlength="200" placeholder="请输入盘点任务名称" />
          </ElFormItem>
        </ElCol>

        <ElCol :span="12">
          <ElFormItem label="盘点范围" prop="taskScopeType">
            <ElSelect
              v-model="formData.taskScopeType"
              class="w-full"
              placeholder="请选择盘点范围"
              @change="handleScopeTypeChange"
            >
              <ElOption
                v-for="item in scopeTypeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </ElSelect>
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="任务状态" prop="status">
            <ElRadioGroup v-model="formData.status">
              <ElRadio v-for="item in sys_normal_disable" :key="item.value" :label="item.value">
                {{ item.label }}
              </ElRadio>
            </ElRadioGroup>
          </ElFormItem>
        </ElCol>

        <ElCol :span="12">
          <ElFormItem label="目标部门" prop="targetDeptId">
            <ElTreeSelect
              v-model="formData.targetDeptId"
              :data="deptOptions"
              :props="{ value: 'id', label: 'label', children: 'children' }"
              value-key="id"
              filterable
              clearable
              check-strictly
              class="w-full"
              placeholder="按部门盘点时请选择目标部门"
            />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="目标位置" prop="targetLocationId">
            <ElTreeSelect
              v-model="formData.targetLocationId"
              :data="locationOptions"
              :props="{ value: 'id', label: 'label', children: 'children' }"
              value-key="id"
              filterable
              clearable
              check-strictly
              class="w-full"
              placeholder="按位置盘点时请选择目标位置"
            />
          </ElFormItem>
        </ElCol>

        <ElCol :span="12">
          <ElFormItem label="目标分类" prop="targetCategoryId">
            <ElTreeSelect
              v-model="formData.targetCategoryId"
              :data="categoryOptions"
              :props="{ value: 'id', label: 'label', children: 'children' }"
              value-key="id"
              filterable
              clearable
              check-strictly
              class="w-full"
              placeholder="按分类盘点时请选择目标分类"
            />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="负责人" prop="ownerUserId">
            <ElSelect
              v-model="formData.ownerUserId"
              filterable
              clearable
              class="w-full"
              placeholder="请选择负责人"
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
          <ElFormItem label="执行人" prop="executeUserId">
            <ElSelect
              v-model="formData.executeUserId"
              filterable
              clearable
              class="w-full"
              placeholder="请选择执行人"
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
          <ElFormItem label="计划开始" prop="planStartTime">
            <ElDatePicker
              v-model="formData.planStartTime"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
              class="w-full"
              placeholder="请选择计划开始时间"
            />
          </ElFormItem>
        </ElCol>

        <ElCol :span="12">
          <ElFormItem label="计划结束" prop="planEndTime">
            <ElDatePicker
              v-model="formData.planEndTime"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
              class="w-full"
              placeholder="请选择计划结束时间"
            />
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
              placeholder="请输入盘点任务备注"
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
  import { useDict } from '@/utils/dict'
  import { addAssetInventory, getAssetInventory, updateAssetInventory } from '@/api/asset/inventory'

  interface TreeOption {
    id: number
    label: string
    children?: TreeOption[]
  }

  interface UserOption {
    userId: number
    userName: string
  }

  interface ScopeOption {
    label: string
    value: string
  }

  const { sys_normal_disable } = useDict('sys_normal_disable')

  const props = defineProps<{
    modelValue: boolean
    dialogType: 'add' | 'edit'
    taskData?: any
    categoryOptions: TreeOption[]
    locationOptions: TreeOption[]
    deptOptions: TreeOption[]
    userOptions: UserOption[]
    scopeTypeOptions: ScopeOption[]
  }>()

  const emit = defineEmits<{
    (e: 'update:modelValue', value: boolean): void
    (e: 'success'): void
  }>()

  const visible = ref(false)
  const loading = ref(false)
  const submitLoading = ref(false)
  const formRef = ref()

  const initialFormData = {
    taskId: undefined as number | undefined,
    taskNo: '',
    taskName: '',
    taskScopeType: 'ALL',
    targetDeptId: undefined as number | undefined,
    targetLocationId: undefined as number | undefined,
    targetCategoryId: undefined as number | undefined,
    planStartTime: '',
    planEndTime: '',
    ownerUserId: undefined as number | undefined,
    executeUserId: undefined as number | undefined,
    status: '0',
    remark: ''
  }

  const formData = reactive({ ...initialFormData })

  const formRules: FormRules = {
    taskNo: [{ required: true, message: '任务编号不能为空', trigger: 'blur' }],
    taskName: [{ required: true, message: '任务名称不能为空', trigger: 'blur' }],
    taskScopeType: [{ required: true, message: '盘点范围不能为空', trigger: 'change' }],
    ownerUserId: [{ required: true, message: '负责人不能为空', trigger: 'change' }],
    executeUserId: [{ required: true, message: '执行人不能为空', trigger: 'change' }],
    planStartTime: [{ required: true, message: '计划开始时间不能为空', trigger: 'change' }],
    planEndTime: [{ required: true, message: '计划结束时间不能为空', trigger: 'change' }],
    status: [{ required: true, message: '任务状态不能为空', trigger: 'change' }],
    targetDeptId: [{ validator: validateScopeTarget, trigger: 'change' }],
    targetLocationId: [{ validator: validateScopeTarget, trigger: 'change' }],
    targetCategoryId: [{ validator: validateScopeTarget, trigger: 'change' }]
  }

  /**
   * 盘点范围切换时，只保留当前维度对应的目标字段。
   */
  const handleScopeTypeChange = () => {
    if (formData.taskScopeType === 'ALL') {
      formData.targetDeptId = undefined
      formData.targetLocationId = undefined
      formData.targetCategoryId = undefined
      return
    }

    if (formData.taskScopeType !== 'DEPT') {
      formData.targetDeptId = undefined
    }
    if (formData.taskScopeType !== 'LOCATION') {
      formData.targetLocationId = undefined
    }
    if (formData.taskScopeType !== 'CATEGORY') {
      formData.targetCategoryId = undefined
    }
  }

  /**
   * 盘点范围与目标对象做联动校验，避免只填了任务但没有真正的盘点边界。
   */
  function validateScopeTarget(_: unknown, __: unknown, callback: (error?: Error) => void): void {
    if (formData.taskScopeType === 'ALL') {
      callback()
      return
    }

    if (formData.taskScopeType === 'DEPT' && !formData.targetDeptId) {
      callback(new Error('按部门盘点时请选择目标部门'))
      return
    }

    if (formData.taskScopeType === 'LOCATION' && !formData.targetLocationId) {
      callback(new Error('按位置盘点时请选择目标位置'))
      return
    }

    if (formData.taskScopeType === 'CATEGORY' && !formData.targetCategoryId) {
      callback(new Error('按分类盘点时请选择目标分类'))
      return
    }

    callback()
  }

  watch(
    () => props.modelValue,
    async (value) => {
      visible.value = value
      if (!value) return

      if (props.dialogType === 'edit' && props.taskData?.taskId) {
        loading.value = true
        try {
          const detail: any = await getAssetInventory(props.taskData.taskId)
          const record = detail?.data || detail || {}
          Object.assign(formData, initialFormData, record)
        } finally {
          loading.value = false
        }
        return
      }

      Object.assign(formData, initialFormData)
      formData.taskScopeType = props.taskData?.taskScopeType || 'ALL'
      handleScopeTypeChange()
    }
  )

  watch(
    () => visible.value,
    (value) => {
      emit('update:modelValue', value)
    }
  )

  /**
   * 提交盘点任务表单。
   */
  const handleSubmit = async () => {
    if (!formRef.value) return

    await formRef.value.validate(async (valid: boolean) => {
      if (!valid) return

      submitLoading.value = true
      try {
        if (props.dialogType === 'edit') {
          await updateAssetInventory(formData)
          ElMessage.success('修改成功')
        } else {
          await addAssetInventory(formData)
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
   * 关闭后恢复干净状态，避免上一次编辑残留到下一次。
   */
  const handleClosed = () => {
    formRef.value?.resetFields()
    Object.assign(formData, initialFormData)
  }
</script>
