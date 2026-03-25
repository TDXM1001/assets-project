<template>
  <ElDialog
    v-model="visible"
    title="资产导入"
    width="720px"
    destroy-on-close
    append-to-body
    @closed="handleClosed"
  >
    <div class="asset-import-dialog">
      <ElAlert
        type="info"
        :closable="false"
        show-icon
        title="支持导入资产台账 Excel 文件，模板字段与当前资产台账表单保持一致。"
      />

      <div class="asset-import-dialog__toolbar">
        <ElSpace wrap>
          <ElButton :loading="templateLoading" @click="handleDownloadTemplate"> 下载模板 </ElButton>
          <ElSwitch
            v-model="updateSupport"
            inline-prompt
            active-text="更新已存在"
            inactive-text="仅新增"
          />
        </ElSpace>
      </div>

      <ElUpload
        drag
        :auto-upload="false"
        :show-file-list="false"
        accept=".xlsx,.xls"
        :limit="1"
        :disabled="submitting"
        :on-change="handleFileChange"
      >
        <ElIcon class="el-icon--upload">
          <UploadFilled />
        </ElIcon>
        <div class="el-upload__text"> 拖拽 Excel 到这里，或者 <em>点击选择文件</em> </div>
        <template #tip>
          <div class="el-upload__tip">仅支持 xlsx / xls，建议先下载模板再填写。</div>
        </template>
      </ElUpload>

      <ElCard shadow="never" class="mt-4">
        <template #header>当前文件</template>
        <template v-if="selectedFile">
          <ElDescriptions :column="1" border>
            <ElDescriptionsItem label="文件名">{{ selectedFile.name }}</ElDescriptionsItem>
            <ElDescriptionsItem label="文件大小">{{
              formatFileSize(selectedFile.size)
            }}</ElDescriptionsItem>
          </ElDescriptions>
        </template>
        <ElEmpty v-else description="尚未选择文件" />
      </ElCard>

      <ElAlert
        v-if="resultMessage"
        class="mt-4"
        :type="resultType"
        :closable="false"
        show-icon
        :title="resultMessage"
      />
    </div>

    <template #footer>
      <div class="flex justify-end gap-3">
        <ElButton @click="visible = false">取消</ElButton>
        <ElButton
          type="primary"
          :loading="submitting"
          :disabled="!selectedFile"
          @click="handleSubmit"
        >
          开始导入
        </ElButton>
      </div>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import { computed, ref, watch } from 'vue'
  import FileSaver from 'file-saver'
  import { ElMessage, type UploadFile } from 'element-plus'
  import { UploadFilled } from '@element-plus/icons-vue'
  import { downloadAssetInfoTemplate, importAssetInfoFile } from '@/api/asset/info'

  defineOptions({ name: 'InfoImportDialog' })

  const props = defineProps<{
    modelValue: boolean
  }>()

  const emit = defineEmits<{
    (e: 'update:modelValue', value: boolean): void
    (e: 'success'): void
  }>()

  const selectedFile = ref<File | null>(null)
  const updateSupport = ref(false)
  const submitting = ref(false)
  const templateLoading = ref(false)
  const resultMessage = ref('')
  const resultType = ref<'success' | 'warning' | 'error' | 'info'>('success')

  const visible = computed({
    get: () => props.modelValue,
    set: (value: boolean) => emit('update:modelValue', value)
  })

  const formatFileSize = (size?: number) => {
    if (!size) return '-'
    if (size < 1024) return `${size} B`
    if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`
    return `${(size / 1024 / 1024).toFixed(1)} MB`
  }

  const handleFileChange = (uploadFile: UploadFile) => {
    selectedFile.value = (uploadFile.raw as File) || null
    resultMessage.value = ''
  }

  const handleDownloadTemplate = async () => {
    templateLoading.value = true
    try {
      const blob = await downloadAssetInfoTemplate()
      FileSaver.saveAs(blob as Blob, '资产台账导入模板.xlsx')
      ElMessage.success('模板下载成功')
    } catch (error) {
      console.error('下载导入模板失败:', error)
      ElMessage.error('下载模板失败')
    } finally {
      templateLoading.value = false
    }
  }

  const handleSubmit = async () => {
    if (!selectedFile.value) {
      ElMessage.warning('请先选择导入文件')
      return
    }

    submitting.value = true
    try {
      const formData = new FormData()
      formData.append('file', selectedFile.value)
      formData.append('updateSupport', String(updateSupport.value))
      const response: any = await importAssetInfoFile(formData)
      resultType.value = 'success'
      resultMessage.value = response?.msg || response?.message || '导入成功'
      ElMessage.success(resultMessage.value)
      emit('success')
    } catch (error: any) {
      console.error('导入资产台账失败:', error)
      resultType.value = 'error'
      resultMessage.value =
        error?.response?.data?.msg || error?.message || '导入失败，请检查 Excel 内容后重试'
      ElMessage.error(resultMessage.value)
    } finally {
      submitting.value = false
    }
  }

  const handleClosed = () => {
    selectedFile.value = null
    updateSupport.value = false
    resultMessage.value = ''
    resultType.value = 'success'
  }

  watch(
    () => visible.value,
    (value) => {
      if (!value) {
        handleClosed()
      }
    }
  )
</script>

<style scoped lang="scss">
  .asset-import-dialog {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .asset-import-dialog__toolbar {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }
</style>
