<template>
  <ElDialog
    :title="dialogType === 'add' ? '新增资产分类' : '修改资产分类'"
    v-model="visible"
    width="720px"
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
          <ElFormItem label="上级分类" prop="parentId">
            <ElTreeSelect
              v-model="formData.parentId"
              :data="categoryOptions"
              :props="{ value: 'id', label: 'label', children: 'children', disabled: 'disabled' }"
              value-key="id"
              check-strictly
              filterable
              clearable
              :render-after-expand="false"
              class="w-full"
              placeholder="请选择上级分类"
            />
          </ElFormItem>
        </ElCol>

        <ElCol :span="12">
          <ElFormItem label="分类编码" prop="categoryCode">
            <ElInput v-model="formData.categoryCode" maxlength="64" placeholder="请输入分类编码" />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="分类名称" prop="categoryName">
            <ElInput v-model="formData.categoryName" maxlength="100" placeholder="请输入分类名称" />
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
          <ElFormItem label="使用寿命(月)" prop="usefulLifeMonths">
            <ElInputNumber
              v-model="formData.usefulLifeMonths"
              :min="0"
              controls-position="right"
              class="w-full"
              placeholder="请输入使用寿命"
            />
          </ElFormItem>
        </ElCol>

        <ElCol :span="12">
          <ElFormItem label="可折旧" prop="depreciableFlag">
            <ElRadioGroup v-model="formData.depreciableFlag">
              <ElRadio label="1">是</ElRadio>
              <ElRadio label="0">否</ElRadio>
            </ElRadioGroup>
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="需序列号" prop="serialRequiredFlag">
            <ElRadioGroup v-model="formData.serialRequiredFlag">
              <ElRadio label="1">是</ElRadio>
              <ElRadio label="0">否</ElRadio>
            </ElRadioGroup>
          </ElFormItem>
        </ElCol>

        <ElCol :span="12">
          <ElFormItem label="允许借用" prop="borrowableFlag">
            <ElRadioGroup v-model="formData.borrowableFlag">
              <ElRadio label="1">是</ElRadio>
              <ElRadio label="0">否</ElRadio>
            </ElRadioGroup>
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="纳入盘点" prop="inventoryRequiredFlag">
            <ElRadioGroup v-model="formData.inventoryRequiredFlag">
              <ElRadio label="1">是</ElRadio>
              <ElRadio label="0">否</ElRadio>
            </ElRadioGroup>
          </ElFormItem>
        </ElCol>

        <ElCol :span="12">
          <ElFormItem label="分类状态" prop="status">
            <ElRadioGroup v-model="formData.status">
              <ElRadio v-for="item in sys_normal_disable" :key="item.value" :label="item.value">
                {{ item.label }}
              </ElRadio>
            </ElRadioGroup>
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="资产类型" prop="assetType">
            <ElSelect
              v-model="formData.assetType"
              :disabled="!!route.query.assetType"
              class="w-full"
              placeholder="请选择资产类型"
            >
              <ElOption
                v-for="item in asset_type"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </ElSelect>
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
              placeholder="请输入分类说明"
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
  import {
    addCategory,
    getCategory,
    treeCategorySelect,
    updateCategory
  } from '@/api/asset/category'
  import { useRoute } from 'vue-router'
  import { useDict } from '@/utils/dict'

  interface CategoryTreeOption {
    id: number
    label: string
    disabled?: boolean
    children?: CategoryTreeOption[]
  }

  const { sys_normal_disable, asset_type } = useDict('sys_normal_disable', 'asset_type')
  const route = useRoute()

  const props = defineProps<{
    modelValue: boolean
    dialogType: 'add' | 'edit'
    categoryData?: any
  }>()

  const emit = defineEmits<{
    (e: 'update:modelValue', value: boolean): void
    (e: 'success'): void
  }>()

  const visible = ref(false)
  const loading = ref(false)
  const submitLoading = ref(false)
  const formRef = ref()
  const categoryOptions = ref<CategoryTreeOption[]>([])

  // 【动态路由兼容】自动识别当前模块类型
  const getBaseAssetType = () => {
    if (route.query.assetType) return route.query.assetType as string
    const path = route.path.toLowerCase()
    if (path.includes('realestate')) return 'REAL_ESTATE'
    if (path.includes('fixed')) return 'FIXED_ASSET'
    return 'FIXED_ASSET'
  }

  const initialFormData = {
    categoryId: undefined as number | undefined,
    parentId: 0,
    categoryCode: '',
    categoryName: '',
    orderNum: 0,
    depreciableFlag: '1',
    serialRequiredFlag: '0',
    borrowableFlag: '0',
    inventoryRequiredFlag: '1',
    usefulLifeMonths: 36,
    assetType: getBaseAssetType(),
    status: '0',
    remark: ''
  }

  const formData = reactive({ ...initialFormData })

  const formRules: FormRules = {
    parentId: [{ required: true, message: '上级分类不能为空', trigger: 'change' }],
    categoryCode: [{ required: true, message: '分类编码不能为空', trigger: 'blur' }],
    categoryName: [{ required: true, message: '分类名称不能为空', trigger: 'blur' }],
    assetType: [{ required: true, message: '资产类型不能为空', trigger: 'change' }],
    orderNum: [{ required: true, message: '显示排序不能为空', trigger: 'blur' }],
    status: [{ required: true, message: '分类状态不能为空', trigger: 'change' }]
  }

  /**
   * 构造带“顶级分类”入口的树选项。
   */
  const buildCategoryOptions = (list: CategoryTreeOption[]) => {
    categoryOptions.value = [
      {
        id: 0,
        label: '顶级分类',
        children: list || []
      }
    ]
  }

  /**
   * 加载分类树，供弹窗选择父级节点。
   */
  const loadCategoryOptions = async () => {
    try {
      const response: any = await treeCategorySelect({ assetType: getBaseAssetType() })
      const data = Array.isArray(response) ? response : response?.data || []
      buildCategoryOptions(data)
    } catch (error) {
      console.error('获取分类树失败:', error)
      buildCategoryOptions([])
    }
  }

  watch(
    () => props.modelValue,
    async (value) => {
      visible.value = value
      if (!value) return

      await loadCategoryOptions()

      if (props.dialogType === 'edit' && props.categoryData?.categoryId) {
        loading.value = true
        try {
          const detailRes: any = await getCategory(props.categoryData.categoryId)
          const detail = detailRes.data || detailRes
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
      if (props.categoryData?.categoryId) {
        formData.parentId = props.categoryData.categoryId
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
   * 提交分类表单。
   */
  const handleSubmit = async () => {
    if (!formRef.value) return

    await formRef.value.validate(async (valid: boolean) => {
      if (!valid) return

      submitLoading.value = true
      try {
        if (props.dialogType === 'edit') {
          await updateCategory(formData)
          ElMessage.success('修改成功')
        } else {
          await addCategory(formData)
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
   * 关闭后重置表单，避免上一次编辑残留。
   */
  const handleClosed = () => {
    formRef.value?.resetFields()
    Object.assign(formData, initialFormData)
    categoryOptions.value = []
  }
</script>

<style scoped></style>
