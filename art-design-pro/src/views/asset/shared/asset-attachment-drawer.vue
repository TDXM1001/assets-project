<template>
  <ElDrawer
    v-model="visible"
    :title="drawerTitle"
    size="72%"
    append-to-body
    destroy-on-close
    @open="handleOpen"
  >
    <div class="asset-attachment-drawer">
      <ElAlert
        class="mb-4"
        type="info"
        :closable="false"
        show-icon
        title="附件支持上传、查看和删除，当前列表和业务主数据共用同一套后端权限校验。"
      />

      <div class="asset-attachment-drawer__toolbar">
        <div class="asset-attachment-drawer__meta">
          <div class="asset-attachment-drawer__biz">{{ bizLabel }}</div>
          <div class="asset-attachment-drawer__sub">{{ displayBizKey }}</div>
        </div>

        <ElSpace wrap>
          <ElUpload
            v-if="canUpload"
            :http-request="handleUploadRequest"
            :show-file-list="false"
            :disabled="!bizReady || uploading"
            :accept="uploadAccept"
          >
            <ElButton type="primary" :loading="uploading" v-ripple>上传附件</ElButton>
          </ElUpload>
          <ElButton :loading="loading" @click="loadAttachments">刷新</ElButton>
        </ElSpace>
      </div>

      <ElEmpty v-if="!loading && attachments.length === 0" description="当前业务还没有附件">
        <ElButton v-if="canUpload" type="primary" @click="triggerUploadTip"
          >上传第一份附件</ElButton
        >
      </ElEmpty>

      <ElTable v-else v-loading="loading" :data="attachments" border stripe>
        <ElTableColumn prop="fileName" label="文件名称" min-width="220" />
        <ElTableColumn prop="fileType" label="类型" width="100" />
        <ElTableColumn prop="fileSize" label="大小" width="120" :formatter="formatSize" />
        <ElTableColumn prop="uploadUserName" label="上传人" width="120" />
        <ElTableColumn prop="uploadTime" label="上传时间" width="180" />
        <ElTableColumn label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <ElSpace wrap>
              <ElButton link type="primary" @click="handlePreview(row)">查看</ElButton>
              <ElButton v-if="canDelete" link type="danger" @click="handleDelete(row)">
                删除
              </ElButton>
            </ElSpace>
          </template>
        </ElTableColumn>
      </ElTable>
    </div>

    <template #footer>
      <div class="flex justify-end">
        <ElButton @click="visible = false">关闭</ElButton>
      </div>
    </template>
  </ElDrawer>
</template>

<script setup lang="ts">
  import { computed, onBeforeUnmount, ref, watch } from 'vue'
  import { ElMessage, ElMessageBox, type UploadRequestOptions } from 'element-plus'
  import { UploadAjaxError } from 'element-plus/es/components/upload/src/ajax'
  import { useUserStore } from '@/store/modules/user'
  import {
    delAssetAttachment,
    listAssetAttachment,
    uploadAssetAttachment
  } from '@/api/asset/attachment'

  defineOptions({ name: 'AssetAttachmentDrawer' })

  interface AttachmentItem {
    attachmentId: number
    fileName?: string
    fileType?: string
    fileSize?: number
    fileUrl?: string
    uploadUserName?: string
    uploadTime?: string
  }

  const props = withDefaults(
    defineProps<{
      modelValue: boolean
      bizType?: string
      bizId?: number | string
      bizTitle?: string
      permissionPrefix?: string
    }>(),
    {
      bizTitle: '',
      permissionPrefix: ''
    }
  )

  const emit = defineEmits<{
    (e: 'update:modelValue', value: boolean): void
    (e: 'refresh'): void
  }>()

  const userStore = useUserStore()
  const loading = ref(false)
  const uploading = ref(false)
  const attachments = ref<AttachmentItem[]>([])
  const visible = computed({
    get: () => props.modelValue,
    set: (value: boolean) => emit('update:modelValue', value)
  })

  const bizReady = computed(() =>
    Boolean(props.bizType && props.bizId !== undefined && props.bizId !== null)
  )
  const displayBizKey = computed(() =>
    bizReady.value ? `${props.bizType} / ${props.bizId}` : '请选择一个业务对象'
  )
  const bizLabel = computed(() => props.bizTitle || '业务附件')
  const canUpload = computed(
    () => hasPermission(`${props.permissionPrefix}:edit`) && bizReady.value
  )
  const canDelete = computed(
    () => hasPermission(`${props.permissionPrefix}:edit`) && bizReady.value
  )
  const uploadAccept = '.png,.jpg,.jpeg,.gif,.pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.txt,.zip,.rar'

  const createUploadError = (message: string) => new UploadAjaxError(message, 0, 'POST', '')

  const hasPermission = (permission: string) => {
    if (!permission || permission === ':edit') return false
    const permissions = userStore.permissions || []
    return permissions.includes('*:*:*') || permissions.includes(permission)
  }

  const drawerTitle = computed(() => {
    if (props.bizTitle) {
      return `附件管理 - ${props.bizTitle}`
    }
    return '附件管理'
  })

  const handleOpen = () => {
    void loadAttachments()
  }

  const extractRows = (response: any): AttachmentItem[] => {
    return response?.rows || response?.data?.rows || response?.data || []
  }

  const loadAttachments = async () => {
    if (!bizReady.value) {
      attachments.value = []
      return
    }

    loading.value = true
    try {
      const response: any = await listAssetAttachment({
        bizType: props.bizType,
        bizId: props.bizId,
        pageNum: 1,
        pageSize: 100
      })
      attachments.value = extractRows(response)
    } catch (error) {
      console.error('加载附件列表失败:', error)
      attachments.value = []
    } finally {
      loading.value = false
    }
  }

  const handleUploadRequest = async (options: UploadRequestOptions) => {
    if (!props.bizType || props.bizId === undefined || props.bizId === null) {
      ElMessage.warning('请先选择业务对象')
      options.onError?.(createUploadError('业务对象未选择'))
      return
    }

    uploading.value = true
    try {
      const response: any = await uploadAssetAttachment(
        props.bizType,
        props.bizId,
        options.file as File
      )
      options.onSuccess?.(response)
      ElMessage.success('上传成功')
      await loadAttachments()
      emit('refresh')
    } catch (error) {
      console.error('上传附件失败:', error)
      const message = error instanceof Error ? error.message : '上传附件失败'
      options.onError?.(createUploadError(message))
    } finally {
      uploading.value = false
    }
  }

  const handlePreview = (row: AttachmentItem) => {
    if (!row.fileUrl) {
      ElMessage.warning('附件地址不存在')
      return
    }
    window.open(row.fileUrl, '_blank', 'noopener,noreferrer')
  }

  const handleDelete = async (row: AttachmentItem) => {
    try {
      await ElMessageBox.confirm(
        `确认删除附件「${row.fileName || row.attachmentId}」吗？`,
        '提示',
        {
          type: 'warning'
        }
      )
      await delAssetAttachment(row.attachmentId)
      ElMessage.success('删除成功')
      await loadAttachments()
      emit('refresh')
    } catch (error) {
      if (error !== 'cancel') {
        console.error('删除附件失败:', error)
      }
    }
  }

  const triggerUploadTip = () => {
    ElMessage.info('请先点击右上角“上传附件”')
  }

  const formatSize = (_row: unknown, _column: unknown, cellValue: number) => {
    if (cellValue === null || cellValue === undefined) return '-'
    if (cellValue < 1024) return `${cellValue} B`
    if (cellValue < 1024 * 1024) return `${(cellValue / 1024).toFixed(1)} KB`
    return `${(cellValue / 1024 / 1024).toFixed(1)} MB`
  }

  watch(
    () => visible.value,
    (value) => {
      if (!value) {
        attachments.value = []
      }
    }
  )

  watch(
    () => [props.bizType, props.bizId],
    () => {
      if (visible.value) {
        void loadAttachments()
      }
    }
  )

  onBeforeUnmount(() => {
    attachments.value = []
  })
</script>

<style scoped lang="scss">
  .asset-attachment-drawer {
    display: flex;
    flex-direction: column;
    gap: 16px;
    min-height: 100%;
  }

  .asset-attachment-drawer__toolbar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
  }

  .asset-attachment-drawer__meta {
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  .asset-attachment-drawer__biz {
    font-size: 16px;
    font-weight: 600;
    color: var(--art-gray-900);
  }

  .asset-attachment-drawer__sub {
    font-size: 12px;
    color: var(--art-gray-500);
  }
</style>
