<template>
  <div class="real-estate-page art-full-height">
    <div class="real-estate-layout">
      <!-- 列表主区域 -->
      <div class="real-estate-main">
        <ArtSearchBar
          v-model="formFilters"
          :items="formItems"
          :showExpand="false"
          @search="handleSearch"
          @reset="handleReset"
        />

        <ElCard class="art-table-card flex-1 overflow-hidden" shadow="never">
          <ArtTableHeader
            :loading="loading"
            v-model:columns="columnChecks"
            @refresh="refreshData"
          >
            <template #left>
              <ElSpace wrap>
                <ElButton v-auth="'asset:real-estate:add'" type="primary" @click="handleAdd" v-ripple>
                  新增不动产
                </ElButton>
                <ElButton
                  v-auth="'asset:real-estate:export'"
                  type="warning"
                  plain
                  :loading="exportLoading"
                  @click="handleExport"
                  v-ripple
                >
                  导出
                </ElButton>
                <ElButton
                  v-auth="'asset:real-estate:remove'"
                  type="danger"
                  plain
                  :disabled="multiple"
                  @click="handleDelete()"
                  v-ripple
                >
                  删除
                </ElButton>
              </ElSpace>
            </template>
          </ArtTableHeader>

          <ArtTable
            v-model:selection="selection"
            :loading="loading"
            :data="data"
            :columns="columns"
            :pagination="pagination"
            rowKey="assetId"
            @pagination:size-change="handleSizeChange"
            @pagination:current-change="handleCurrentChange"
          />
        </ElCard>
      </div>
    </div>

    <!-- 附件抽屉 -->
    <AssetAttachmentDrawer
      v-model="attachmentDrawerVisible"
      biz-type="ASSET_INFO"
      :biz-id="currentAsset?.assetId"
      :biz-title="currentAsset?.assetName || '不动产详情'"
      permission-prefix="asset:real-estate"
    />
  </div>
</template>

<script setup lang="ts">
  import { computed, h, onMounted, reactive, ref } from 'vue'
  import { ElButton, ElMessage, ElMessageBox, ElSpace } from 'element-plus'
  import { useRouter } from 'vue-router'
  import { listAssetInfo, delAssetInfo, exportAssetInfo } from '@/api/asset/info'
  import { useDict } from '@/utils/dict'
  import { useTable } from '@/hooks/core/useTable'
  import DictTag from '@/components/DictTag/index.vue'
  import AssetAttachmentDrawer from '../shared/asset-attachment-drawer.vue'
  import AssetRowActionBar from '../shared/asset-row-action-bar.vue'

  defineOptions({ name: 'RealEstateIndex' })

  const router = useRouter()
  const { real_estate_status } = useDict('real_estate_status')

  // 1. 强制注入不动产类型过滤器
  const formFilters = reactive({
    assetType: 'REAL_ESTATE',
    assetName: '',
    assetStatus: ''
  })

  const formItems = [
    { label: '资产名称', prop: 'assetName', type: 'input' },
    { label: '状态', prop: 'assetStatus', type: 'select', options: real_estate_status }
  ]

  const {
    loading,
    data,
    pagination,
    handleSizeChange,
    handleCurrentChange,
    handleSearch,
    handleReset,
    refreshData,
    selection,
    multiple
  } = useTable({
    api: listAssetInfo,
    initFilters: formFilters
  })

  // 2. 列定义优化
  const columns = [
    { type: 'selection', width: 50 },
    { label: '不动产编码', prop: 'assetCode', width: 140 },
    { label: '名称', prop: 'assetName', minWidth: 150, showOverflowTooltip: true },
    { 
      label: '状态', 
      prop: 'assetStatus', 
      width: 100,
      render: (row: any) => h(DictTag, { options: real_estate_status.value, value: row.assetStatus })
    },
    { label: '详细地址', prop: 'addrDetail', minWidth: 200, showOverflowTooltip: true },
    { 
      label: '地理坐标', 
      width: 150,
      render: (row: any) => row.longitude ? `${row.longitude}, ${row.latitude}` : '-'
    },
    {
      label: '操作',
      width: 180,
      fixed: 'right',
      render: (row: any) => h(AssetRowActionBar, {
        items: [
          { label: '编辑', onClick: () => handleEdit(row), auth: 'asset:real-estate:edit' },
          { label: '附件', onClick: () => handleAttachment(row), auth: 'asset:real-estate:list' },
          { label: '删除', onClick: () => handleDelete(row), auth: 'asset:real-estate:remove', danger: true }
        ]
      })
    }
  ]

  const columnChecks = ref(columns.map(c => c.label).filter(Boolean))
  const exportLoading = ref(false)
  const attachmentDrawerVisible = ref(false)
  const currentAsset = ref<any>(null)

  const handleAdd = () => router.push('/asset/real-estate/create')
  const handleEdit = (row: any) => router.push(`/asset/real-estate/create?assetId=${row.assetId}`)
  const handleAttachment = (row: any) => {
    currentAsset.value = row
    attachmentDrawerVisible.value = true
  }

  const handleDelete = (row?: any) => {
    const ids = row ? [row.assetId] : selection.value.map((i: any) => i.assetId)
    ElMessageBox.confirm('是否确认删除选中的不动产台账？', '警告', { type: 'warning' }).then(async () => {
      await delAssetInfo(ids.join(','))
      ElMessage.success('删除成功')
      refreshData()
    })
  }

  const handleExport = async () => {
    exportLoading.value = true
    try {
      // 导出也要带上 assetType
      await exportAssetInfo(formFilters)
    } finally {
      exportLoading.value = false
    }
  }

  onMounted(() => refreshData())
</script>

<style scoped>
  .real-estate-page {
    display: flex;
    flex-direction: column;
    background: var(--art-bg-color);
  }
  .real-estate-layout {
    display: flex;
    flex: 1;
    overflow: hidden;
  }
  .real-estate-main {
    flex: 1;
    display: flex;
    flex-direction: column;
    padding: 16px;
    gap: 16px;
  }
</style>
