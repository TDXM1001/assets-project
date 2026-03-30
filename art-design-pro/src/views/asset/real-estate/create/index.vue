<template>
  <div class="real-estate-create-page art-full-height" v-loading="loading">
    <!-- 头部 Hero 区域 -->
    <ElCard class="real-estate-create-page__hero" shadow="never">
      <div class="real-estate-create-page__hero-main">
        <div>
          <div class="real-estate-create-page__eyebrow">不动产管理</div>
          <h1 class="real-estate-create-page__title">{{ pageTitle }}</h1>
          <p class="real-estate-create-page__desc">{{ pageDescription }}</p>
        </div>
        <ElSpace wrap>
          <ElTag v-if="currentAssetId" type="success" effect="light">已入库</ElTag>
          <ElTag v-else type="warning" effect="light">草稿/待登记</ElTag>
        </ElSpace>
      </div>
    </ElCard>

    <!-- 表单区域 -->
    <ElForm
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="120px"
      class="real-estate-create-page__form"
    >
      <!-- 1. 基础信息 -->
      <ElCard shadow="never" class="mb-4">
        <template #header>
          <div class="section-header">
            <span class="section-title">基础信息</span>
          </div>
        </template>
        <ElRow :gutter="20">
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

      <!-- 2. 地理与归属信息 -->
      <ElCard shadow="never" class="mb-4">
        <template #header>
          <div class="section-header">
            <span class="section-title">地理与归属信息</span>
          </div>
        </template>
        <ElRow :gutter="20">
          <ElCol :span="24">
            <ElFormItem label="详细地址" prop="addrDetail">
              <ElInput v-model="formData.addrDetail" type="textarea" :rows="2" placeholder="请输入完整详细地址" />
            </ElFormItem>
          </ElCol>
          <ElCol :span="24">
            <RealEstateMap
              v-model:longitude="formData.longitude"
              v-model:latitude="formData.latitude"
            />
          </ElCol>
          <ElCol :span="12" class="mt-4">
            <ElFormItem label="使用部门" prop="useOrgDeptId">
              <ElTreeSelect
                v-model="formData.useOrgDeptId"
                :data="deptOptions"
                :props="{ value: 'id', label: 'label', children: 'children' }"
                value-key="id"
                class="w-full"
              />
            </ElFormItem>
          </ElCol>
          <ElCol :span="12" class="mt-4">
            <ElFormItem label="责任人" prop="currentUserId">
              <ElSelect v-model="formData.currentUserId" filterable clearable class="w-full">
                <ElOption
                  v-for="item in userOptions"
                  :key="item.userId"
                  :label="item.userName"
                  :value="item.userId"
                />
              </ElSelect>
            </ElFormItem>
          </ElCol>
        </ElRow>
      </ElCard>

      <!-- 3. 扩展字段 (attr_json) -->
      <ElCard shadow="never" class="mb-4">
        <template #header>
          <div class="section-header">
            <span class="section-title">不动产专业指标</span>
            <span class="section-subtitle">根据分类自动加载专业字段 (如面积、宗地号等)</span>
          </div>
        </template>
        <InfoDynamicFields
          ref="dynamicFieldsRef"
          v-model="formData.extraFieldValues"
          :template="fieldTemplate"
        />
      </ElCard>
    </ElForm>

    <!-- 底部操作栏 -->
    <div class="real-estate-create-page__footer">
      <ElSpace>
        <ElButton @click="handleCancel">取消</ElButton>
        <ElButton type="primary" :loading="submitLoading" @click="handleSubmit">保存不动产</ElButton>
      </ElSpace>
    </div>
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

  defineOptions({ name: 'RealEstateCreate' })

  const route = useRoute()
  const router = useRouter()
  const { real_estate_status } = useDict('real_estate_status')

  const loading = ref(false)
  const submitLoading = ref(false)
  const currentAssetId = ref<number | undefined>(
    route.query.assetId ? Number(route.query.assetId) : undefined
  )

  const categoryOptions = ref([])
  const deptOptions = ref([])
  const userOptions = ref([])
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
    currentUserId: undefined,
    extraFieldValues: {},
    status: '0'
  })

  const formRules: FormRules = {
    assetCode: [{ required: true, message: '编码不能为空', trigger: 'blur' }],
    assetName: [{ required: true, message: '名称不能为空', trigger: 'blur' }],
    categoryId: [{ required: true, message: '分类不能为空', trigger: 'change' }]
  }

  const pageTitle = computed(() => (currentAssetId.value ? '编辑不动产' : '不动产登记'))
  const pageDescription = computed(() => '录入不动产地理信息、实物资产主档及专业指标。')

  const loadData = async () => {
    loading.value = true
    try {
      const [catRes, deptRes, userRes] = await Promise.all([
        treeCategorySelect(),
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
        // 加载模板
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

  const handleSubmit = async () => {
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
      router.back()
    } finally {
      submitLoading.value = false
    }
  }

  const handleCancel = () => router.back()

  onMounted(() => loadData())
</script>

<style scoped>
  .real-estate-create-page {
    background-color: #f0f2f5;
    padding-bottom: 80px;
  }
  .real-estate-create-page__hero {
    background: linear-gradient(135deg, #1890ff 0%, #001529 100%);
    color: white;
    border: none;
    border-radius: 0;
    margin-bottom: 20px;
  }
  .real-estate-create-page__hero-main {
    padding: 20px;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  .real-estate-create-page__eyebrow {
    font-size: 14px;
    opacity: 0.8;
  }
  .real-estate-create-page__title {
    margin: 8px 0;
    font-size: 24px;
  }
  .real-estate-create-page__form {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 20px;
  }
  .section-title {
    font-weight: bold;
    font-size: 16px;
    border-left: 4px solid #1890ff;
    padding-left: 12px;
  }
  .section-subtitle {
    font-size: 12px;
    color: #909399;
    margin-left: 12px;
  }
  .real-estate-create-page__footer {
    position: fixed;
    bottom: 0;
    right: 0;
    left: 0;
    background: white;
    padding: 16px 40px;
    box-shadow: 0 -2px 8px rgba(0,0,0,0.1);
    display: flex;
    justify-content: flex-end;
    z-index: 100;
  }
</style>
