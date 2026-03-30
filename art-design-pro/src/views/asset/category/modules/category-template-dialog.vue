<template>
  <ElDialog
    v-model="visible"
    :title="`${categoryName || '当前分类'}字段模板`"
    width="1120px"
    destroy-on-close
    append-to-body
    @closed="handleClosed"
  >
    <div v-loading="loading">
      <ElAlert
        class="mb-4"
        type="info"
        :closable="false"
        title="模板用于驱动固定资产台账的扩展字段，本轮先支持受控字段，不做完全自由表单引擎。"
      />

      <ElForm label-width="110px">
        <ElRow :gutter="16">
          <ElCol :span="8">
            <ElFormItem label="模板版本">
              <ElInput :model-value="String(formData.templateVersion || 1)" disabled />
            </ElFormItem>
          </ElCol>
          <ElCol :span="8">
            <ElFormItem label="模板状态">
              <ElRadioGroup v-model="formData.status">
                <ElRadio label="0">启用</ElRadio>
                <ElRadio label="1">停用</ElRadio>
              </ElRadioGroup>
            </ElFormItem>
          </ElCol>
          <ElCol :span="8" class="flex items-center justify-end">
            <ElButton type="primary" @click="handleAddField">新增字段</ElButton>
          </ElCol>
        </ElRow>
      </ElForm>

      <ElTable :data="formData.fields" border max-height="460">
        <ElTableColumn label="排序" width="90">
          <template #default="{ row }">
            <ElInputNumber
              v-model="row.orderNum"
              :min="1"
              controls-position="right"
              class="w-full"
            />
          </template>
        </ElTableColumn>
        <ElTableColumn label="字段编码" min-width="150">
          <template #default="{ row }">
            <ElInput v-model="row.fieldCode" maxlength="64" placeholder="如：deviceSn" />
          </template>
        </ElTableColumn>
        <ElTableColumn label="字段名称" min-width="150">
          <template #default="{ row }">
            <ElInput v-model="row.fieldName" maxlength="100" placeholder="请输入字段名称" />
          </template>
        </ElTableColumn>
        <ElTableColumn label="数据类型" width="120">
          <template #default="{ row }">
            <ElSelect v-model="row.dataType" class="w-full">
              <ElOption
                v-for="item in dataTypeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </ElSelect>
          </template>
        </ElTableColumn>
        <ElTableColumn label="组件类型" width="130">
          <template #default="{ row }">
            <ElSelect v-model="row.componentType" class="w-full">
              <ElOption
                v-for="item in componentTypeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </ElSelect>
          </template>
        </ElTableColumn>
        <ElTableColumn label="字典类型" min-width="140">
          <template #default="{ row }">
            <ElInput v-model="row.dictType" maxlength="100" placeholder="下拉/单选可填写字典类型" />
          </template>
        </ElTableColumn>
        <ElTableColumn label="默认值" min-width="140">
          <template #default="{ row }">
            <ElInput v-model="row.defaultValue" maxlength="100" placeholder="可选" />
          </template>
        </ElTableColumn>
        <ElTableColumn label="分组" min-width="120">
          <template #default="{ row }">
            <ElInput v-model="row.groupName" maxlength="50" placeholder="默认分组" />
          </template>
        </ElTableColumn>
        <ElTableColumn label="必填" width="90" align="center">
          <template #default="{ row }">
            <ElSwitch v-model="row.requiredFlag" active-value="1" inactive-value="0" />
          </template>
        </ElTableColumn>
        <ElTableColumn label="只读" width="90" align="center">
          <template #default="{ row }">
            <ElSwitch v-model="row.readonlyFlag" active-value="1" inactive-value="0" />
          </template>
        </ElTableColumn>
        <ElTableColumn label="状态" width="90" align="center">
          <template #default="{ row }">
            <ElSwitch v-model="row.status" active-value="0" inactive-value="1" />
          </template>
        </ElTableColumn>
        <ElTableColumn label="操作" width="90" align="center" fixed="right">
          <template #default="{ $index }">
            <ElButton link type="danger" @click="handleRemoveField($index)">删除</ElButton>
          </template>
        </ElTableColumn>
      </ElTable>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <ElButton @click="visible = false">取消</ElButton>
        <ElButton type="primary" :loading="submitLoading" @click="handleSubmit">保存模板</ElButton>
      </div>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import { reactive, ref, watch } from 'vue'
  import { ElMessage } from 'element-plus'
  import { getCategoryFieldTemplate, updateCategoryFieldTemplate } from '@/api/asset/category'
  import type {
    AssetCategoryFieldTemplate,
    AssetCategoryFieldTemplateField
  } from '@/api/asset/types'

  const props = defineProps<{
    modelValue: boolean
    categoryId?: number
    categoryName?: string
  }>()

  const emit = defineEmits<{
    (e: 'update:modelValue', value: boolean): void
    (e: 'success'): void
  }>()

  const visible = ref(false)
  const loading = ref(false)
  const submitLoading = ref(false)

  const dataTypeOptions = [
    { label: '文本', value: 'string' },
    { label: '数字', value: 'number' },
    { label: '日期', value: 'date' },
    { label: '布尔', value: 'boolean' }
  ]

  const componentTypeOptions = [
    { label: '输入框', value: 'input' },
    { label: '多行文本', value: 'textarea' },
    { label: '数字输入', value: 'number' },
    { label: '下拉选择', value: 'select' },
    { label: '单选组', value: 'radio' },
    { label: '日期选择', value: 'date' }
  ]

  const createDefaultTemplate = (): AssetCategoryFieldTemplate => ({
    categoryId: props.categoryId,
    templateVersion: 1,
    status: '1',
    fields: []
  })

  const createFieldItem = (orderNum: number): AssetCategoryFieldTemplateField => ({
    fieldCode: '',
    fieldName: '',
    dataType: 'string',
    componentType: 'input',
    requiredFlag: '0',
    readonlyFlag: '0',
    dictType: '',
    defaultValue: '',
    orderNum,
    groupName: '默认分组',
    status: '0'
  })

  const formData = reactive<AssetCategoryFieldTemplate>(createDefaultTemplate())

  const normalizeTemplate = (payload?: Partial<AssetCategoryFieldTemplate>) => {
    const next = createDefaultTemplate()
    Object.assign(next, payload || {})
    next.categoryId = props.categoryId
    next.status = next.status || '1'
    next.fields = Array.isArray(next.fields)
      ? next.fields.map((item, index) => ({
          ...createFieldItem(index + 1),
          ...item,
          orderNum: item.orderNum ?? index + 1
        }))
      : []
    return next
  }

  const loadFieldTemplate = async () => {
    if (!props.categoryId) {
      Object.assign(formData, createDefaultTemplate())
      return
    }

    loading.value = true
    try {
      const response: any = await getCategoryFieldTemplate(props.categoryId)
      Object.assign(formData, normalizeTemplate(response?.data || response))
    } finally {
      loading.value = false
    }
  }

  watch(
    () => props.modelValue,
    async (value) => {
      visible.value = value
      if (!value) {
        return
      }
      await loadFieldTemplate()
    }
  )

  watch(
    () => visible.value,
    (value) => {
      emit('update:modelValue', value)
    }
  )

  const handleAddField = () => {
    formData.fields.push(createFieldItem(formData.fields.length + 1))
  }

  const handleRemoveField = (index: number) => {
    formData.fields.splice(index, 1)
    formData.fields.forEach((item, itemIndex) => {
      item.orderNum = itemIndex + 1
    })
  }

  /**
   * 模板先做受控校验，避免前端把无效组件类型和重复字段编码落到后端。
   */
  const validateTemplate = () => {
    const fieldCodeSet = new Set<string>()

    for (const item of formData.fields) {
      const fieldCode = item.fieldCode?.trim()
      const fieldName = item.fieldName?.trim()

      if (!fieldCode) {
        return '字段编码不能为空'
      }
      if (!fieldName) {
        return '字段名称不能为空'
      }
      if (fieldCodeSet.has(fieldCode)) {
        return `字段编码[${fieldCode}]重复`
      }
      if (
        (item.componentType === 'select' || item.componentType === 'radio') &&
        !item.dictType?.trim()
      ) {
        return `字段[${fieldName}]使用下拉或单选时必须填写字典类型`
      }
      fieldCodeSet.add(fieldCode)
    }

    return ''
  }

  const handleSubmit = async () => {
    if (!props.categoryId) {
      ElMessage.error('当前分类不存在')
      return
    }

    const errorMessage = validateTemplate()
    if (errorMessage) {
      ElMessage.error(errorMessage)
      return
    }

    submitLoading.value = true
    try {
      const payload = normalizeTemplate(formData)
      await updateCategoryFieldTemplate(props.categoryId, payload)
      ElMessage.success('模板保存成功')
      visible.value = false
      emit('success')
    } finally {
      submitLoading.value = false
    }
  }

  const handleClosed = () => {
    Object.assign(formData, createDefaultTemplate())
  }
</script>
