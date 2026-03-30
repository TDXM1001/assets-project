<template>
  <div class="asset-info-create-page art-full-height" v-loading="loading">
    <!-- 头部 Hero 区域 -->
    <ElCard class="asset-info-create-page__hero" shadow="never">
      <div class="asset-info-create-page__hero-main">
        <div>
          <div class="asset-info-create-page__eyebrow">不动产登记</div>
          <h1 class="asset-info-create-page__title">{{ pageTitle }}</h1>
          <p class="asset-info-create-page__desc">
            先把不动产主档录稳，后续单据、事件和附件都沿用这套基础字段继续流转。
          </p>
        </div>
        <ElSpace wrap>
          <ElTag v-if="currentAssetId" type="success" effect="light">已生成主档</ElTag>
          <ElTag v-else type="warning" effect="light">草稿阶段</ElTag>
        </ElSpace>
      </div>
    </ElCard>

    <!-- 表单区域 -->
    <ElForm
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="110px"
      class="asset-info-create-page__form"
    >
      <!-- 1. 基础信息 -->
      <ElCard shadow="never">
        <template #header>
          <div class="asset-info-create-page__section-header">
            <div>
              <div class="asset-info-create-page__section-title">基础信息</div>
              <div class="asset-info-create-page__section-subtitle">
                描述不动产的核心地理及分类属性，确权的基础。
              </div>
            </div>
          </div>
        </template>
        <ElRow :gutter="16">
          <ElCol :span="12">
            <ElFormItem label="不动产编码" prop="assetCode">
              <ElInput v-model="formData.assetCode" placeholder="系统自动生成或手动录入" />
            </ElFormItem>
          </ElCol>
          <ElCol :span="12">
            <ElFormItem label="不动产名称" prop="assetName">
              <ElInput v-model="formData.assetName" placeholder="如：XX大厦 302 室" />
            </ElFormItem>
          </ElCol>
          <ElCol :span="12">
            <ElFormItem label="不动产分类" prop="categoryId">
              <ElTreeSelect
                v-model="formData.categoryId"
                :data="categoryOptions"
                :props="{ value: 'id', label: 'label', children: 'children' }"
                value-key="id"
                filterable
                clearable
                check-strictly
                class="w-full"
                placeholder="请选择分类 (如：房屋、土地)"
              />
            </ElFormItem>
          </ElCol>
          <ElCol :span="12">
            <ElFormItem label="当前状态" prop="assetStatus">
              <ElSelect v-model="formData.assetStatus" class="w-full">
                <ElOption
                  v-for="item in real_estate_status"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </ElSelect>
            </ElFormItem>
          </ElCol>
        </ElRow>
      </ElCard>

      <!-- 2. 空间与地理 -->
      <ElCard shadow="never">
        <template #header>
          <div class="asset-info-create-page__section-header">
            <div>
              <div class="asset-info-create-page__section-title">空间与地理</div>
              <div class="asset-info-create-page__section-subtitle">
                界定不动产的物理边界与精确坐标，包含高精度地图标注。
              </div>
            </div>
          </div>
        </template>
        <ElRow :gutter="24">
          <ElCol :md="14" :sm="24">
            <ElFormItem label="详细地址" prop="addrDetail">
              <ElInput
                v-model="formData.addrDetail"
                type="textarea"
                :rows="3"
                placeholder="请输入详细的座落地及门牌号"
              />
            </ElFormItem>
            <ElRow :gutter="16">
              <ElCol :span="12">
                <ElFormItem label="经度" prop="longitude">
                  <ElInput v-model="formData.longitude" placeholder="可通过地图自动获取" />
                </ElFormItem>
              </ElCol>
              <ElCol :span="12">
                <ElFormItem label="纬度" prop="latitude">
                  <ElInput v-model="formData.latitude" placeholder="可通过地图自动获取" />
                </ElFormItem>
              </ElCol>
            </ElRow>
          </ElCol>
          <ElCol :md="10" :sm="24">
            <div class="asset-info-create-page__map-wrapper">
              <RealEstateMap
                v-model:longitude="formData.longitude"
                v-model:latitude="formData.latitude"
                height="150px"
              />
            </div>
          </ElCol>
        </ElRow>
      </ElCard>

      <!-- 3. 归属与管理 -->
      <ElCard shadow="never">
        <template #header>
          <div class="asset-info-create-page__section-header">
            <div>
              <div class="asset-info-create-page__section-title">归属与管理</div>
              <div class="asset-info-create-page__section-subtitle">
                界定不动产的管辖权与使用权。
              </div>
            </div>
          </div>
        </template>
        <ElRow :gutter="16">
          <ElCol :xs="24" :md="12">
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
          <ElCol :xs="24" :md="12">
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
          <ElCol :xs="24" :md="12">
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
          <ElCol :xs="24" :md="12">
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
        </ElRow>
      </ElCard>

      <!-- 4. 价值与补充信息 -->
      <ElCard shadow="never">
        <template #header>
          <div class="asset-info-create-page__section-header">
            <div>
              <div class="asset-info-create-page__section-title">价值与补充信息</div>
              <div class="asset-info-create-page__section-subtitle">
                不动产的财务原值与补充描述。
              </div>
            </div>
          </div>
        </template>
        <ElRow :gutter="16">
          <ElCol :xs="24" :md="12">
            <ElFormItem label="原值" prop="originalValue">
              <ElInputNumber
                v-model="formData.originalValue"
                :min="0"
                :precision="2"
                :step="1000"
                controls-position="right"
                class="w-full"
              />
            </ElFormItem>
          </ElCol>
          <ElCol :xs="24" :md="12">
            <ElFormItem label="残值" prop="residualValue">
              <ElInputNumber
                v-model="formData.residualValue"
                :min="0"
                :precision="2"
                :step="1000"
                controls-position="right"
                class="w-full"
              />
            </ElFormItem>
          </ElCol>
          <ElCol :xs="24" :md="12">
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
          <ElCol :xs="24" :md="12">
            <ElFormItem label="供应商" prop="supplierName">
              <ElInput v-model="formData.supplierName" placeholder="请输入相关机构或供应商" />
            </ElFormItem>
          </ElCol>
          <ElCol :xs="24" :md="24">
            <ElFormItem label="备注" prop="remark">
              <ElInput
                v-model="formData.remark"
                type="textarea"
                :rows="3"
                maxlength="255"
                show-word-limit
                placeholder="请输入备注说明"
              />
            </ElFormItem>
          </ElCol>
        </ElRow>
      </ElCard>

      <!-- 5. 不动产专业指标 -->
      <ElCard shadow="never">
        <template #header>
          <div class="asset-info-create-page__section-header">
            <div>
              <div class="asset-info-create-page__section-title">不动产专业指标</div>
              <div class="asset-info-create-page__section-subtitle">
                根据不动产分类自动加载专业字段（如产证面积、宗地号等）。
              </div>
            </div>
          </div>
        </template>
        <InfoDynamicFields
          ref="dynamicFieldsRef"
          v-model="formData.extraFieldValues"
          :template="fieldTemplate"
        />
      </ElCard>

      <!-- 6. 附件管理 -->
      <ElCard shadow="never">
        <template #header>
          <div class="asset-info-create-page__section-header">
            <div>
              <div class="asset-info-create-page__section-title">附件管理</div>
              <div class="asset-info-create-page__section-subtitle">
                权利证书、地形图或现场照片附件。
              </div>
            </div>
          </div>
        </template>
        <div class="asset-info-create-page__attachment">
          <div class="asset-info-create-page__attachment-meta">
            <div class="asset-info-create-page__attachment-key">
              {{ currentAssetId ? `RE_ASSET / ${currentAssetId}` : '尚未生成业务 ID' }}
            </div>
            <div class="asset-info-create-page__attachment-tip">
              {{ currentAssetId ? '点击右侧按钮管理不动产附件清单。' : '请先保存不动产主档，再补录附件。' }}
            </div>
          </div>
          <ElButton type="primary" :disabled="!currentAssetId" @click="attachmentDrawerVisible = true">
            管理附件
          </ElButton>
        </div>
      </ElCard>
    </ElForm>

    <!-- 底部操作栏 -->
    <div class="asset-info-create-page__footer">
      <ElSpace size="large">
        <ElButton type="primary" plain @click="handleCancel" v-ripple>返回</ElButton>
        <ElButton
          type="primary"
          :loading="submitLoading"
          @click="() => handleSubmit(false)"
          v-ripple
        >
          保存
        </ElButton>
      </ElSpace>
      <ElSpace>
        <ElButton
          type="success"
          :loading="submitLoading"
          @click="() => handleSubmit(true)"
          v-ripple
        >
          保存并继续
        </ElButton>
      </ElSpace>
    </div>

    <AssetAttachmentDrawer
      v-model="attachmentDrawerVisible"
      biz-type="ASSET_INFO"
      :biz-id="currentAssetId"
      :biz-title="formData.assetName || formData.assetCode || '不动产台账'"
      permission-prefix="asset:real-estate"
    />
  </div>
</template>

<script setup lang="ts">
  import { computed, onMounted, reactive, ref, watch } from 'vue'
  import { ElMessage, type FormRules } from 'element-plus'
  import { useRoute, useRouter } from 'vue-router'
  import { getCategoryFieldTemplate, treeCategorySelect } from '@/api/asset/category'
  import { addAssetInfo, getAssetInfo, updateAssetInfo } from '@/api/asset/info'
  import { listUser, deptTreeSelect } from '@/api/system/user'
  import { useDict } from '@/utils/dict'
  import RealEstateMap from '../components/RealEstateMap.vue'
  import InfoDynamicFields from '../../info/modules/info-dynamic-fields.vue'
  import AssetAttachmentDrawer from '../../shared/asset-attachment-drawer.vue'

  defineOptions({ name: 'RealEstateCreate' })

  interface UserOption {
    userId: number
    userName: string
  }

  const route = useRoute()
  const router = useRouter()
  const { real_estate_status } = useDict('real_estate_status')

  const loading = ref(false)
  const submitLoading = ref(false)
  const attachmentDrawerVisible = ref(false)
  const currentAssetId = ref<number | undefined>(
    route.query.assetId ? Number(route.query.assetId) : undefined
  )

  const categoryOptions = ref([])
  const deptOptions = ref([])
  const userOptions = ref<UserOption[]>([])
  const fieldTemplate = ref(null)
  const formRef = ref()

  const formData = reactive({
    assetId: currentAssetId.value,
    assetCode: '',
    assetName: '',
    assetType: 'REAL_ESTATE',
    categoryId: undefined,
    assetStatus: 'VACANT',
    addrDetail: '',
    longitude: undefined,
    latitude: undefined,
    useOrgDeptId: undefined,
    manageDeptId: undefined,
    currentUserId: undefined,
    purchaseDate: '',
    inboundDate: '',
    startUseDate: '',
    originalValue: undefined as number | undefined,
    residualValue: undefined as number | undefined,
    supplierName: '',
    qrCode: '',
    versionNo: 1,
    extraFieldValues: {},
    status: '0',
    remark: ''
  })

  const formRules: FormRules = {
    assetCode: [{ required: true, message: '编码不能为空', trigger: 'blur' }],
    assetName: [{ required: true, message: '名称不能为空', trigger: 'blur' }],
    categoryId: [{ required: true, message: '分类不能为空', trigger: 'change' }]
  }

  const pageTitle = computed(() => (currentAssetId.value ? '编辑不动产' : '不动产登记'))

  const loadData = async () => {
    loading.value = true
    try {
      const [catRes, deptRes, userRes] = await Promise.all([
        treeCategorySelect({ assetType: 'REAL_ESTATE' }),
        deptTreeSelect(),
        listUser({ pageNum: 1, pageSize: 500 })
      ])
      categoryOptions.value = (catRes as any).data || catRes
      deptOptions.value = (deptRes as any).data || deptRes
      userOptions.value = (userRes as any).rows || []

      if (currentAssetId.value) {
        const detailRes = await getAssetInfo(currentAssetId.value)
        const detail = (detailRes as any).data || detailRes
        Object.assign(formData, detail)

        // 【关键修复】解析动态字段 JSON 字符串到对象
        if (detail.extraFieldsJson) {
          try {
            formData.extraFieldValues = JSON.parse(detail.extraFieldsJson)
          } catch (e) {
            console.error('解析动态字段失败', e)
            formData.extraFieldValues = {}
          }
        }

        if (formData.categoryId) {
          loadFieldTemplate(formData.categoryId)
        }
      }
    } finally {
      loading.value = false
    }
  }

  const loadFieldTemplate = async (categoryId: number) => {
    const res = await getCategoryFieldTemplate(categoryId)
    fieldTemplate.value = (res as any).data || res
  }

  watch(() => formData.categoryId, (val) => {
    if (val) loadFieldTemplate(val)
  })

  const handleSubmit = async (continueAdd = false) => {
    await formRef.value.validate()
    submitLoading.value = true
    try {
      const payload = {
        ...formData,
        extraFieldsJson: JSON.stringify(formData.extraFieldValues)
      }
      if (currentAssetId.value) {
        await updateAssetInfo(payload)
      } else {
        await addAssetInfo(payload)
      }
      ElMessage.success('操作成功')
      if (continueAdd) {
        resetForm()
      } else {
        router.back()
      }
    } finally {
      submitLoading.value = false
    }
  }

  const resetForm = () => {
    Object.assign(formData, {
      assetId: undefined,
      assetCode: '',
      assetName: '',
      categoryId: undefined,
      addrDetail: '',
      longitude: undefined,
      latitude: undefined,
      useOrgDeptId: undefined,
      manageDeptId: undefined,
      currentUserId: undefined,
      purchaseDate: '',
      inboundDate: '',
      startUseDate: '',
      originalValue: undefined,
      residualValue: undefined,
      supplierName: '',
      qrCode: '',
      versionNo: 1,
      extraFieldValues: {},
      remark: ''
    })
    currentAssetId.value = undefined
  }

  const handleCancel = () => router.back()

  onMounted(() => loadData())
</script>

<style lang="scss" scoped>
  .asset-info-create-page {
    min-height: calc(100vh - 84px); /* 对标 RuoYi 头部高度后的视口填充 */
    padding: 16px;
    display: flex;
    flex-direction: column;
    gap: 16px;
    background-color: var(--art-bg-color);
    animation: asset-page-enter 180ms ease-out both;
  }

  .asset-info-create-page__hero,
  .asset-info-create-page__form :deep(.el-card) {
    border-radius: 18px;
    border-color: rgb(15 23 42 / 8%);
  }

  .asset-info-create-page__hero-main {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 16px;
  }

  .asset-info-create-page__eyebrow {
    margin-bottom: 8px;
    font-size: 12px;
    font-weight: 600;
    letter-spacing: 0.12em;
    text-transform: uppercase;
    color: var(--el-color-primary);
  }

  .asset-info-create-page__title {
    margin: 0;
    font-size: 28px;
    font-weight: 700;
    line-height: 1.2;
    color: rgb(15 23 42);
  }

  .asset-info-create-page__desc {
    margin: 10px 0 0;
    max-width: 680px;
    color: rgb(71 85 105);
    line-height: 1.7;
    font-size: 14px;
  }

  .asset-info-create-page__form {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .asset-info-create-page__section-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
  }

  .asset-info-create-page__section-title {
    font-size: 16px;
    font-weight: 700;
    color: rgb(15 23 42);
  }

  .asset-info-create-page__section-subtitle {
    margin-top: 4px;
    font-size: 13px;
    color: rgb(100 116 139);
    line-height: 1.6;
  }

  .asset-info-create-page__attachment {
    margin-top: 16px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
  }

  .asset-info-create-page__attachment-meta {
    flex: 1;
  }

  .asset-info-create-page__attachment-key {
    font-size: 14px;
    font-weight: 700;
    color: rgb(15 23 42);
  }

  .asset-info-create-page__attachment-tip {
    margin-top: 4px;
    font-size: 13px;
    color: rgb(100 116 139);
  }

  .asset-info-create-page__footer {
    position: sticky;
    bottom: 0;
    z-index: 10;
    padding: 14px 18px;
    margin-top: auto; /* 核心：在内容不足时推到容器底部 */
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
    border: 1px solid rgb(15 23 42 / 8%);
    border-radius: 18px;
    background: rgb(255 255 255 / 92%);
    backdrop-filter: blur(14px);
    box-shadow: 0 -4px 12px rgb(15 23 42 / 3%);
  }

  .asset-info-create-page__map-wrapper {
    border: 1px solid var(--art-gray-200);
    border-radius: 12px;
    overflow: hidden;

    @media (max-width: 768px) {
      margin-top: 16px;
    }
  }

  @keyframes asset-page-enter {
    from {
      opacity: 0;
      transform: translateY(8px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }

  @media (max-width: 768px) {
    .asset-info-create-page {
      padding: 12px;
    }
    .asset-info-create-page__hero-main,
    .asset-info-create-page__attachment,
    .asset-info-create-page__footer {
      flex-direction: column;
      align-items: stretch;
    }
    .asset-info-create-page__title {
      font-size: 24px;
    }
  }
</style>
