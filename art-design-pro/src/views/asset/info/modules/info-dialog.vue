<template>
  <ElDialog
    :title="dialogType === 'add' ? '新增资产' : '修改资产'"
    v-model="visible"
    width="960px"
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
        <ElCol :span="12">
          <ElFormItem label="资产编码" prop="assetCode">
            <ElInput v-model="formData.assetCode" maxlength="64" placeholder="请输入资产编码" />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="资产名称" prop="assetName">
            <ElInput v-model="formData.assetName" maxlength="200" placeholder="请输入资产名称" />
          </ElFormItem>
        </ElCol>

        <ElCol :span="12">
          <ElFormItem label="资产分类" prop="categoryId">
            <ElTreeSelect
              v-model="formData.categoryId"
              :data="categoryOptions"
              :props="{ value: 'id', label: 'label', children: 'children' }"
              value-key="id"
              filterable
              clearable
              check-strictly
              class="w-full"
              placeholder="请选择资产分类"
            />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="资产状态" prop="assetStatus">
            <ElSelect v-model="formData.assetStatus" class="w-full" placeholder="请选择资产状态">
              <ElOption
                v-for="item in asset_status"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </ElSelect>
          </ElFormItem>
        </ElCol>

        <ElCol :span="12">
          <ElFormItem label="品牌" prop="brand">
            <ElInput v-model="formData.brand" maxlength="50" placeholder="请输入品牌" />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="型号" prop="model">
            <ElInput v-model="formData.model" maxlength="50" placeholder="请输入型号" />
          </ElFormItem>
        </ElCol>

        <ElCol :span="12">
          <ElFormItem label="规格" prop="specification">
            <ElInput v-model="formData.specification" maxlength="100" placeholder="请输入规格" />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="序列号" prop="serialNo">
            <ElInput v-model="formData.serialNo" maxlength="100" placeholder="请输入序列号" />
          </ElFormItem>
        </ElCol>

        <ElCol :span="12">
          <ElFormItem label="资产来源" prop="assetSource">
            <ElSelect v-model="formData.assetSource" class="w-full" placeholder="请选择资产来源">
              <ElOption
                v-for="item in asset_source"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </ElSelect>
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="台账状态" prop="status">
            <ElRadioGroup v-model="formData.status">
              <ElRadio v-for="item in sys_normal_disable" :key="item.value" :label="item.value">
                {{ item.label }}
              </ElRadio>
            </ElRadioGroup>
          </ElFormItem>
        </ElCol>

        <ElCol :span="12">
          <ElFormItem label="使用部门" prop="useOrgDeptId">
            <ElTreeSelect
              v-model="formData.useOrgDeptId"
              :data="deptOptions"
              :props="{ value: 'id', label: 'label', children: 'children' }"
              value-key="id"
              filterable
              clearable
              check-strictly
              class="w-full"
              placeholder="请选择使用部门"
            />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="管理部门" prop="manageDeptId">
            <ElTreeSelect
              v-model="formData.manageDeptId"
              :data="deptOptions"
              :props="{ value: 'id', label: 'label', children: 'children' }"
              value-key="id"
              filterable
              clearable
              check-strictly
              class="w-full"
              placeholder="请选择管理部门"
            />
          </ElFormItem>
        </ElCol>

        <ElCol :span="12">
          <ElFormItem label="责任人" prop="currentUserId">
            <ElSelect
              v-model="formData.currentUserId"
              filterable
              clearable
              class="w-full"
              placeholder="请选择责任人"
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
          <ElFormItem label="当前位置" prop="currentLocationId">
            <ElTreeSelect
              v-model="formData.currentLocationId"
              :data="locationOptions"
              :props="{ value: 'id', label: 'label', children: 'children' }"
              value-key="id"
              filterable
              clearable
              check-strictly
              class="w-full"
              placeholder="请选择当前位置"
            />
          </ElFormItem>
        </ElCol>

        <ElCol :span="12">
          <ElFormItem label="采购日期" prop="purchaseDate">
            <ElDatePicker
              v-model="formData.purchaseDate"
              type="date"
              value-format="YYYY-MM-DD"
              class="w-full"
              placeholder="请选择采购日期"
            />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="入库日期" prop="inboundDate">
            <ElDatePicker
              v-model="formData.inboundDate"
              type="date"
              value-format="YYYY-MM-DD"
              class="w-full"
              placeholder="请选择入库日期"
            />
          </ElFormItem>
        </ElCol>

        <ElCol :span="12">
          <ElFormItem label="启用日期" prop="startUseDate">
            <ElDatePicker
              v-model="formData.startUseDate"
              type="date"
              value-format="YYYY-MM-DD"
              class="w-full"
              placeholder="请选择启用日期"
            />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="质保到期" prop="warrantyExpireDate">
            <ElDatePicker
              v-model="formData.warrantyExpireDate"
              type="date"
              value-format="YYYY-MM-DD"
              class="w-full"
              placeholder="请选择质保到期日期"
            />
          </ElFormItem>
        </ElCol>

        <ElCol :span="12">
          <ElFormItem label="原值" prop="originalValue">
            <ElInputNumber
              v-model="formData.originalValue"
              :min="0"
              :precision="2"
              :step="100"
              controls-position="right"
              class="w-full"
            />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="残值" prop="residualValue">
            <ElInputNumber
              v-model="formData.residualValue"
              :min="0"
              :precision="2"
              :step="100"
              controls-position="right"
              class="w-full"
            />
          </ElFormItem>
        </ElCol>

        <ElCol :span="12">
          <ElFormItem label="供应商" prop="supplierName">
            <ElInput v-model="formData.supplierName" maxlength="100" placeholder="请输入供应商" />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="二维码" prop="qrCode">
            <ElInput v-model="formData.qrCode" maxlength="100" placeholder="请输入二维码标识" />
          </ElFormItem>
        </ElCol>

        <ElCol :span="12">
          <ElFormItem label="版本号" prop="versionNo">
            <ElInputNumber
              v-model="formData.versionNo"
              :min="1"
              controls-position="right"
              class="w-full"
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
              placeholder="请输入资产备注"
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
  import { addAssetInfo, getAssetInfo, updateAssetInfo } from '@/api/asset/info'

  interface TreeOption {
    id: number
    label: string
    children?: TreeOption[]
  }

  interface UserOption {
    userId: number
    userName: string
  }

  const { sys_normal_disable, asset_status, asset_source } = useDict(
    'sys_normal_disable',
    'asset_status',
    'asset_source'
  )

  const props = defineProps<{
    modelValue: boolean
    dialogType: 'add' | 'edit'
    assetData?: any
    categoryOptions: TreeOption[]
    locationOptions: TreeOption[]
    deptOptions: TreeOption[]
    userOptions: UserOption[]
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
    assetId: undefined as number | undefined,
    assetCode: '',
    assetName: '',
    categoryId: undefined as number | undefined,
    brand: '',
    model: '',
    specification: '',
    serialNo: '',
    assetStatus: 'IDLE',
    assetSource: 'PURCHASE',
    useOrgDeptId: undefined as number | undefined,
    manageDeptId: undefined as number | undefined,
    currentUserId: undefined as number | undefined,
    currentLocationId: undefined as number | undefined,
    purchaseDate: '',
    inboundDate: '',
    startUseDate: '',
    originalValue: undefined as number | undefined,
    residualValue: undefined as number | undefined,
    warrantyExpireDate: '',
    supplierName: '',
    qrCode: '',
    versionNo: 1,
    status: '0',
    remark: ''
  }

  const formData = reactive({ ...initialFormData })

  const formRules: FormRules = {
    assetCode: [{ required: true, message: '资产编码不能为空', trigger: 'blur' }],
    assetName: [{ required: true, message: '资产名称不能为空', trigger: 'blur' }],
    categoryId: [{ required: true, message: '资产分类不能为空', trigger: 'change' }],
    assetStatus: [{ required: true, message: '资产状态不能为空', trigger: 'change' }]
  }

  /**
   * 进入编辑态时补拉详情，新增态只保留默认值。
   */
  watch(
    () => props.modelValue,
    async (value) => {
      visible.value = value
      if (!value) return

      if (props.dialogType === 'edit' && props.assetData?.assetId) {
        loading.value = true
        try {
          const detail: any = await getAssetInfo(props.assetData.assetId)
          Object.assign(formData, initialFormData, detail || {})
        } finally {
          loading.value = false
        }
        return
      }

      Object.assign(formData, initialFormData)
    }
  )

  watch(
    () => visible.value,
    (value) => {
      emit('update:modelValue', value)
    }
  )

  /**
   * 提交资产表单，当前仅对接已落地的 CRUD 接口。
   */
  const handleSubmit = async () => {
    if (!formRef.value) return

    await formRef.value.validate(async (valid: boolean) => {
      if (!valid) return

      submitLoading.value = true
      try {
        if (props.dialogType === 'edit') {
          await updateAssetInfo(formData)
          ElMessage.success('修改成功')
        } else {
          await addAssetInfo(formData)
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
   * 弹窗关闭后把表单恢复干净，避免下一次打开残留旧值。
   */
  const handleClosed = () => {
    formRef.value?.resetFields()
    Object.assign(formData, initialFormData)
  }
</script>

<style scoped></style>
