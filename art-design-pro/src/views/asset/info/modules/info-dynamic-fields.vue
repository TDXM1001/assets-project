<template>
  <div v-if="activeGroups.length" class="asset-dynamic-fields">
    <ElDivider content-position="left">扩展字段</ElDivider>

    <div v-for="group in activeGroups" :key="group.groupName" class="asset-dynamic-group">
      <div class="asset-dynamic-group__title">{{ group.groupName }}</div>
      <ElRow :gutter="16">
        <ElCol
          v-for="field in group.fields"
          :key="field.fieldCode"
          :span="field.componentType === 'textarea' ? 24 : 12"
        >
          <ElFormItem :label="field.fieldName" :required="field.requiredFlag === '1'">
            <ElInput
              v-if="field.componentType === 'input'"
              :model-value="toStringValue(getFieldValue(field.fieldCode))"
              :disabled="isFieldDisabled(field)"
              :placeholder="`请输入${field.fieldName}`"
              @update:model-value="(value) => updateFieldValue(field.fieldCode, value)"
            />

            <ElInput
              v-else-if="field.componentType === 'textarea'"
              type="textarea"
              :rows="3"
              :model-value="toStringValue(getFieldValue(field.fieldCode))"
              :disabled="isFieldDisabled(field)"
              :placeholder="`请输入${field.fieldName}`"
              @update:model-value="(value) => updateFieldValue(field.fieldCode, value)"
            />

            <ElInputNumber
              v-else-if="field.componentType === 'number'"
              :model-value="toNumberValue(getFieldValue(field.fieldCode))"
              :disabled="isFieldDisabled(field)"
              controls-position="right"
              class="w-full"
              @update:model-value="(value) => updateFieldValue(field.fieldCode, value)"
            />

            <ElSelect
              v-else-if="field.componentType === 'select'"
              :model-value="toSelectableValue(getFieldValue(field.fieldCode))"
              :disabled="isFieldDisabled(field)"
              clearable
              class="w-full"
              :placeholder="`请选择${field.fieldName}`"
              @update:model-value="(value) => updateFieldValue(field.fieldCode, value)"
            >
              <ElOption
                v-for="item in dictOptionsMap[field.fieldCode] || []"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </ElSelect>

            <ElRadioGroup
              v-else-if="field.componentType === 'radio'"
              :model-value="toSelectableValue(getFieldValue(field.fieldCode))"
              :disabled="isFieldDisabled(field)"
              @update:model-value="(value) => updateFieldValue(field.fieldCode, value)"
            >
              <ElRadio
                v-for="item in dictOptionsMap[field.fieldCode] || []"
                :key="item.value"
                :label="item.value"
              >
                {{ item.label }}
              </ElRadio>
            </ElRadioGroup>

            <ElDatePicker
              v-else-if="field.componentType === 'date'"
              type="date"
              value-format="YYYY-MM-DD"
              class="w-full"
              :model-value="toStringValue(getFieldValue(field.fieldCode))"
              :disabled="isFieldDisabled(field)"
              :placeholder="`请选择${field.fieldName}`"
              @update:model-value="(value) => updateFieldValue(field.fieldCode, value)"
            />

            <ElInput
              v-else
              :model-value="toStringValue(getFieldValue(field.fieldCode))"
              :disabled="isFieldDisabled(field)"
              :placeholder="`请输入${field.fieldName}`"
              @update:model-value="(value) => updateFieldValue(field.fieldCode, value)"
            />
          </ElFormItem>
        </ElCol>
      </ElRow>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { computed, reactive, watch } from 'vue'
  import { getDicts } from '@/api/system/dict/data'
  import type {
    AssetCategoryFieldTemplate,
    AssetCategoryFieldTemplateField,
    AssetFieldOption,
    AssetInfoDynamicFieldValue
  } from '@/api/asset/types'

  const props = withDefaults(
    defineProps<{
      modelValue?: AssetInfoDynamicFieldValue
      template?: AssetCategoryFieldTemplate | null
      disabled?: boolean
    }>(),
    {
      modelValue: () => ({}),
      template: null,
      disabled: false
    }
  )

  const emit = defineEmits<{
    (e: 'update:modelValue', value: AssetInfoDynamicFieldValue): void
  }>()

  const dictOptionsMap = reactive<Record<string, AssetFieldOption[]>>({})

  const activeFields = computed(() => {
    return (props.template?.fields || [])
      .filter((item) => item.status === '0')
      .sort((prev, next) => (prev.orderNum || 0) - (next.orderNum || 0))
  })

  const activeGroups = computed(() => {
    const groupMap = new Map<string, AssetCategoryFieldTemplateField[]>()

    activeFields.value.forEach((field) => {
      const groupName = field.groupName || '默认分组'
      const groupFields = groupMap.get(groupName) || []
      groupFields.push(field)
      groupMap.set(groupName, groupFields)
    })

    return Array.from(groupMap.entries()).map(([groupName, fields]) => ({ groupName, fields }))
  })

  const ensureDefaultValues = () => {
    const nextValue = { ...(props.modelValue || {}) }
    let changed = false

    activeFields.value.forEach((field) => {
      if (nextValue[field.fieldCode] !== undefined && nextValue[field.fieldCode] !== null) {
        return
      }
      if (!field.defaultValue) {
        return
      }
      nextValue[field.fieldCode] = normalizeFieldValue(field, field.defaultValue)
      changed = true
    })

    if (changed) {
      emit('update:modelValue', nextValue)
    }
  }

  const loadDictOptions = async () => {
    for (const field of activeFields.value) {
      if (!field.dictType) {
        dictOptionsMap[field.fieldCode] = []
        continue
      }
      const response: any = await getDicts(field.dictType)
      const rawData = response?.data || response?.rows || response || []
      dictOptionsMap[field.fieldCode] = Array.isArray(rawData)
        ? rawData.map((item: any) => ({
            label: item.dictLabel || item.label,
            value: item.dictValue || item.value
          }))
        : []
    }
  }

  watch(
    () => props.template,
    async () => {
      ensureDefaultValues()
      await loadDictOptions()
    },
    { deep: true, immediate: true }
  )

  const getFieldValue = (fieldCode: string) => {
    return props.modelValue?.[fieldCode]
  }

  const updateFieldValue = (fieldCode: string, value: any) => {
    const nextValue = { ...(props.modelValue || {}) }
    if (value === undefined || value === null || value === '') {
      delete nextValue[fieldCode]
    } else {
      nextValue[fieldCode] = value
    }
    emit('update:modelValue', nextValue)
  }

  const validate = () => {
    for (const field of activeFields.value) {
      if (field.requiredFlag !== '1') {
        continue
      }
      const fieldValue = getFieldValue(field.fieldCode)
      if (fieldValue === undefined || fieldValue === null || fieldValue === '') {
        return `${field.fieldName}不能为空`
      }
    }
    return ''
  }

  const isFieldDisabled = (field: AssetCategoryFieldTemplateField) => {
    return props.disabled || field.readonlyFlag === '1'
  }

  const toStringValue = (value: any) => {
    return value === undefined || value === null ? '' : String(value)
  }

  const toNumberValue = (value: any) => {
    if (value === undefined || value === null || value === '') {
      return undefined
    }
    return Number(value)
  }

  const toSelectableValue = (value: any) => {
    return value === null ? undefined : value
  }

  const normalizeFieldValue = (field: AssetCategoryFieldTemplateField, value: any) => {
    if (field.dataType === 'number' || field.componentType === 'number') {
      return value === '' ? undefined : Number(value)
    }
    return value
  }

  defineExpose({
    validate
  })
</script>

<style scoped>
  .asset-dynamic-group {
    margin-bottom: 20px;
  }

  .asset-dynamic-group__title {
    margin-bottom: 12px;
    font-size: 14px;
    font-weight: 600;
    color: var(--el-text-color-primary);
  }
</style>
