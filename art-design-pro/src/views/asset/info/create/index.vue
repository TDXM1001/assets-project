<template>
  <div class="asset-info-create-page art-full-height" v-loading="loading">
    <ElCard class="asset-info-create-page__hero" shadow="never">
      <div class="asset-info-create-page__hero-main">
        <div>
          <div class="asset-info-create-page__eyebrow">资产台账</div>
          <h1 class="asset-info-create-page__title">{{ pageTitle }}</h1>
          <p class="asset-info-create-page__desc">{{ pageDescription }}</p>
        </div>

        <ElSpace wrap>
          <ElTag v-if="currentAssetId" type="success" effect="light">已生成主档</ElTag>
          <ElTag v-else type="warning" effect="light">草稿阶段</ElTag>
          <ElTag v-if="templateVersionLabel" type="info" effect="light">
            模板版本 {{ templateVersionLabel }}
          </ElTag>
        </ElSpace>
      </div>
    </ElCard>

    <ElAlert
      v-if="savedDraftSummary"
      class="asset-info-create-page__draft-tip"
      type="info"
      :closable="false"
      show-icon
      title="检测到本地草稿"
    >
      <template #default>
        <div class="asset-info-create-page__draft-actions">
          <span>{{ savedDraftSummary }}</span>
          <ElSpace wrap>
            <ElButton link type="primary" @click="handleRestoreDraft">恢复草稿</ElButton>
            <ElButton link type="danger" @click="handleClearDraft">清空草稿</ElButton>
          </ElSpace>
        </div>
      </template>
    </ElAlert>

    <ElForm
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="110px"
      class="asset-info-create-page__form"
    >
      <ElCard shadow="never">
        <template #header>
          <div class="asset-info-create-page__section-header">
            <div>
              <div class="asset-info-create-page__section-title">基础信息</div>
              <div class="asset-info-create-page__section-subtitle">
                先把资产主档身份录稳，后续单据、事件和附件都沿用这套基础字段继续流转。
              </div>
            </div>
          </div>
        </template>

        <ElRow :gutter="16">
          <ElCol :xs="24" :md="12">
            <ElFormItem label="资产编码" prop="assetCode">
              <ElInput v-model="formData.assetCode" maxlength="64" placeholder="请输入资产编码" />
            </ElFormItem>
          </ElCol>
          <ElCol :xs="24" :md="12">
            <ElFormItem label="资产名称" prop="assetName">
              <ElInput v-model="formData.assetName" maxlength="200" placeholder="请输入资产名称" />
            </ElFormItem>
          </ElCol>
          <ElCol :xs="24" :md="24">
            <ElFormItem label="资产类型" prop="assetType">
              <ElRadioGroup v-model="formData.assetType">
                <ElRadioButton
                  v-for="item in asset_type"
                  :key="item.value"
                  :label="item.value"
                >
                  {{ item.label }}
                </ElRadioButton>
              </ElRadioGroup>
            </ElFormItem>
          </ElCol>

          <ElCol :xs="24" :md="12">
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
          <ElCol :xs="24" :md="12">
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

          <ElCol :xs="24" :md="12">
            <ElFormItem label="品牌" prop="brand">
              <ElInput v-model="formData.brand" maxlength="50" placeholder="请输入品牌" />
            </ElFormItem>
          </ElCol>
          <ElCol :xs="24" :md="12">
            <ElFormItem label="型号" prop="model">
              <ElInput v-model="formData.model" maxlength="50" placeholder="请输入型号" />
            </ElFormItem>
          </ElCol>

          <ElCol :xs="24" :md="12">
            <ElFormItem label="规格" prop="specification">
              <ElInput v-model="formData.specification" maxlength="100" placeholder="请输入规格" />
            </ElFormItem>
          </ElCol>
          <ElCol :xs="24" :md="12">
            <ElFormItem label="序列号" prop="serialNo">
              <ElInput v-model="formData.serialNo" maxlength="100" placeholder="请输入序列号" />
            </ElFormItem>
          </ElCol>

          <ElCol :xs="24" :md="12">
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
          <ElCol :xs="24" :md="12">
            <ElFormItem label="台账状态" prop="status">
              <ElRadioGroup v-model="formData.status">
                <ElRadio v-for="item in sys_normal_disable" :key="item.value" :label="item.value">
                  {{ item.label }}
                </ElRadio>
              </ElRadioGroup>
            </ElFormItem>
          </ElCol>
        </ElRow>
      </ElCard>

      <ElCard shadow="never">
        <template #header>
          <div class="asset-info-create-page__section-header">
            <div>
              <div class="asset-info-create-page__section-title">归属信息</div>
              <div class="asset-info-create-page__section-subtitle">
                这里决定资产在哪、归谁管，后续领用、调拨、盘点会直接复用这组归属字段。
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
          <ElCol :xs="24" :md="12">
            <ElFormItem label="质保到期" prop="warrantyExpireDate">
              <ElDatePicker
                v-model="formData.warrantyExpireDate"
                type="date"
                value-format="YYYY-MM-DD"
                class="w-full"
                placeholder="请选择质保到期"
              />
            </ElFormItem>
          </ElCol>
        </ElRow>
      </ElCard>

      <ElCard shadow="never">
        <template #header>
          <div class="asset-info-create-page__section-header">
            <div>
              <div class="asset-info-create-page__section-title">价值与补充信息</div>
              <div class="asset-info-create-page__section-subtitle">
                价值字段继续保留为结构化列，避免后续统计和导出再回头拆 JSON。
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
                :step="100"
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
                :step="100"
                controls-position="right"
                class="w-full"
              />
            </ElFormItem>
          </ElCol>

          <ElCol :xs="24" :md="12">
            <ElFormItem label="供应商" prop="supplierName">
              <ElInput
                v-model="formData.supplierName"
                maxlength="100"
                placeholder="请输入供应商名称"
              />
            </ElFormItem>
          </ElCol>
          <ElCol :xs="24" :md="12">
            <ElFormItem label="二维码" prop="qrCode">
              <ElInput v-model="formData.qrCode" maxlength="100" placeholder="请输入二维码标识" />
            </ElFormItem>
          </ElCol>

          <ElCol :xs="24" :md="12">
            <ElFormItem label="版本号" prop="versionNo">
              <ElInputNumber
                v-model="formData.versionNo"
                :min="1"
                controls-position="right"
                class="w-full"
              />
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
                placeholder="请输入备注"
              />
            </ElFormItem>
          </ElCol>
        </ElRow>
      </ElCard>

      <ElCard shadow="never">
        <template #header>
          <div class="asset-info-create-page__section-header">
            <div>
              <div class="asset-info-create-page__section-title">扩展字段</div>
              <div class="asset-info-create-page__section-subtitle">
                分类切换后重新拉取模板，并清空旧分类字段值，避免动态字段串到另一类资产。
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

      <ElCard shadow="never">
        <template #header>
          <div class="asset-info-create-page__section-header">
            <div>
              <div class="asset-info-create-page__section-title">附件区域</div>
              <div class="asset-info-create-page__section-subtitle">
                附件统一走资产附件抽屉，先保存主档再补录，能避免附件挂在临时对象上。
              </div>
            </div>
          </div>
        </template>

        <ElAlert :title="attachmentTitle" type="info" :closable="false" show-icon />

        <div class="asset-info-create-page__attachment">
          <div class="asset-info-create-page__attachment-meta">
            <div class="asset-info-create-page__attachment-key">
              {{ currentAssetId ? `ASSET_INFO / ${currentAssetId}` : '尚未生成业务主键' }}
            </div>
            <div class="asset-info-create-page__attachment-tip">
              {{
                currentAssetId
                  ? '可以继续补录发票、照片、验收单等附件。'
                  : '请先保存资产主档，再补录附件。'
              }}
            </div>
          </div>

          <ElButton
            type="primary"
            :disabled="!currentAssetId"
            @click="attachmentDrawerVisible = true"
          >
            管理附件
          </ElButton>
        </div>
      </ElCard>
    </ElForm>

    <div class="asset-info-create-page__footer">
      <ElSpace wrap>
        <ElButton @click="handleCancel">取消返回</ElButton>
        <ElButton :loading="draftSaving" @click="handleSaveDraft">保存草稿</ElButton>
      </ElSpace>
      <ElSpace wrap>
        <ElButton type="primary" plain :loading="submitLoading" @click="handleSubmit('back')">
          保存
        </ElButton>
        <ElButton type="primary" :loading="submitLoading" @click="handleSubmit('continue')">
          保存并继续
        </ElButton>
      </ElSpace>
    </div>

    <AssetAttachmentDrawer
      v-model="attachmentDrawerVisible"
      biz-type="ASSET_INFO"
      :biz-id="currentAssetId"
      :biz-title="formData.assetName || formData.assetCode || '资产台账'"
      permission-prefix="asset:info"
    />
  </div>
</template>

<script setup lang="ts">
  import { computed, onMounted, reactive, ref, watch } from 'vue'
  import { ElMessage, ElMessageBox, type FormRules } from 'element-plus'
  import { useRoute, useRouter } from 'vue-router'
  import { getCategoryFieldTemplate, treeCategorySelect } from '@/api/asset/category'
  import { addAssetInfo, getAssetInfo, listAssetInfo, updateAssetInfo } from '@/api/asset/info'
  import { treeLocationSelect } from '@/api/asset/location'
  import type { AssetInfoPayload } from '@/api/asset/info'
  import type { AssetCategoryFieldTemplate, AssetInfoDynamicFieldValue } from '@/api/asset/types'
  import { listUser, deptTreeSelect } from '@/api/system/user'
  import { useDict } from '@/utils/dict'
  import AssetAttachmentDrawer from '../../shared/asset-attachment-drawer.vue'
  import InfoDynamicFields from '../modules/info-dynamic-fields.vue'

  defineOptions({ name: 'AssetInfoCreate' })

  interface TreeOption {
    id: number
    label: string
    children?: TreeOption[]
  }

  interface UserOption {
    userId: number
    userName: string
  }

  interface LocalDraftPayload {
    savedAt: string
    formData: AssetInfoPayload
  }

  const LOCAL_DRAFT_KEY = 'asset-info-create-draft'

  const route = useRoute()
  const router = useRouter()
  const { sys_normal_disable, asset_status, asset_source, asset_type } = useDict(
    'sys_normal_disable',
    'asset_status',
    'asset_source',
    'asset_type'
  )

  const loading = ref(false)
  const submitLoading = ref(false)
  const draftSaving = ref(false)
  const attachmentDrawerVisible = ref(false)
  const formRef = ref()
  const dynamicFieldsRef = ref<any>()
  const fieldTemplate = ref<AssetCategoryFieldTemplate | null>(null)
  const categoryWatchEnabled = ref(false)
  const currentAssetId = ref<number>()
  const savedDraft = ref<LocalDraftPayload | null>(null)

  const categoryOptions = ref<TreeOption[]>([])
  const locationOptions = ref<TreeOption[]>([])
  const deptOptions = ref<TreeOption[]>([])
  const userOptions = ref<UserOption[]>([])

  const initialFormData = {
    assetId: undefined as number | undefined,
    assetCode: '',
    assetName: '',
    assetType: 'FIXED_ASSET',
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
    templateVersion: undefined as number | undefined,
    extraFieldsJson: null as string | null,
    extraFieldValues: {} as AssetInfoDynamicFieldValue,
    status: '0',
    remark: ''
  }

  const formData = reactive({ ...initialFormData })

  const formRules: FormRules = {
    assetCode: [{ required: true, message: '资产编码不能为空', trigger: 'blur' }],
    assetName: [{ required: true, message: '资产名称不能为空', trigger: 'blur' }],
    categoryId: [{ required: true, message: '资产分类不能为空', trigger: 'change' }],
    assetType: [{ required: true, message: '资产类型不能为空', trigger: 'change' }],
    assetStatus: [{ required: true, message: '资产状态不能为空', trigger: 'change' }]
  }

  const pageTitle = computed(() => (currentAssetId.value ? '继续完善资产' : '新增资产'))
  const pageDescription = computed(() =>
    currentAssetId.value
      ? '主档已经生成，可以继续补录动态字段、附件和补充说明。'
      : '先把资产主档录稳，再通过保存并继续把附件和后续补录接上。'
  )
  const templateVersionLabel = computed(() => fieldTemplate.value?.templateVersion || '')
  const savedDraftSummary = computed(() =>
    savedDraft.value ? `上次草稿保存时间：${savedDraft.value.savedAt}` : ''
  )
  const attachmentTitle = computed(() =>
    currentAssetId.value
      ? '当前资产已经生成主档，可以直接打开附件抽屉继续补录。'
      : '附件能力已经预留在页面里，但需要先保存主档拿到 assetId。'
  )

  const normalizeTemplate = (payload?: any): AssetCategoryFieldTemplate | null => {
    if (!payload) {
      return null
    }
    return {
      categoryId: payload.categoryId,
      templateVersion: payload.templateVersion,
      status: payload.status || '1',
      fields: Array.isArray(payload.fields) ? payload.fields : []
    }
  }

  const extractDetail = (response: any) => response?.data || response
  const extractRows = (response: any) =>
    response?.rows || response?.data?.rows || response?.data || []

  /**
   * 新增页直接复用列表页的基础选项加载，避免新增和编辑形成两套口径。
   */
  const loadPageOptions = async () => {
    const [categoryRes, locationRes, deptRes, userRes] = await Promise.all([
      treeCategorySelect(),
      treeLocationSelect(),
      deptTreeSelect(),
      listUser({ pageNum: 1, pageSize: 200 })
    ])

    const categoryResponse = categoryRes as any
    const locationResponse = locationRes as any
    const deptResponse = deptRes as any
    const userResponse = userRes as any

    categoryOptions.value = Array.isArray(categoryResponse)
      ? categoryResponse
      : categoryResponse?.data || []
    locationOptions.value = Array.isArray(locationResponse)
      ? locationResponse
      : locationResponse?.data || []
    deptOptions.value = Array.isArray(deptResponse) ? deptResponse : deptResponse?.data || []

    const userData = Array.isArray(userResponse)
      ? userResponse
      : userResponse?.rows || userResponse?.data || []
    userOptions.value = userData.map((item: any) => ({
      userId: item.userId,
      userName: item.nickName ? `${item.nickName} (${item.userName})` : item.userName
    }))
  }

  /**
   * 分类模板由后端统一维护，这里只做消费，避免页面自己再维护一套动态字段规则。
   */
  const loadFieldTemplate = async (categoryId?: number, templateVersion?: number) => {
    if (!categoryId) {
      fieldTemplate.value = null
      formData.templateVersion = undefined
      return
    }

    const response: any = await getCategoryFieldTemplate(categoryId, templateVersion)
    fieldTemplate.value = normalizeTemplate(response?.data || response)
    formData.templateVersion = fieldTemplate.value?.templateVersion
  }

  const applyAssetDetail = async (detail?: any) => {
    categoryWatchEnabled.value = false
    Object.assign(formData, initialFormData, detail || {})
    formData.extraFieldValues = { ...(detail?.extraFieldValues || {}) }
    currentAssetId.value = detail?.assetId
    await loadFieldTemplate(formData.categoryId, formData.templateVersion)
    categoryWatchEnabled.value = true
  }

  const loadAssetDetail = async (assetId: number) => {
    loading.value = true
    try {
      const response: any = await getAssetInfo(assetId)
      await applyAssetDetail(extractDetail(response))
    } finally {
      loading.value = false
    }
  }

  const readSavedDraft = () => {
    if (currentAssetId.value) {
      savedDraft.value = null
      return
    }

    const rawDraft = window.localStorage.getItem(LOCAL_DRAFT_KEY)
    if (!rawDraft) {
      savedDraft.value = null
      return
    }

    try {
      savedDraft.value = JSON.parse(rawDraft)
    } catch (error) {
      console.error('解析本地草稿失败:', error)
      window.localStorage.removeItem(LOCAL_DRAFT_KEY)
      savedDraft.value = null
    }
  }

  const buildSubmitPayload = (): AssetInfoPayload => {
    const extraFieldValues = { ...(formData.extraFieldValues || {}) }
    const extraFieldsJson = Object.keys(extraFieldValues).length
      ? JSON.stringify(extraFieldValues)
      : null

    return {
      ...formData,
      assetId: currentAssetId.value,
      extraFieldValues,
      extraFieldsJson,
      templateVersion: fieldTemplate.value?.templateVersion
    }
  }

  const persistDraft = () => {
    const payload: LocalDraftPayload = {
      savedAt: new Date().toLocaleString(),
      formData: buildSubmitPayload()
    }
    window.localStorage.setItem(LOCAL_DRAFT_KEY, JSON.stringify(payload))
    savedDraft.value = payload
  }

  const clearDraftStorage = () => {
    window.localStorage.removeItem(LOCAL_DRAFT_KEY)
    savedDraft.value = null
  }

  const handleRestoreDraft = async () => {
    if (!savedDraft.value) {
      return
    }
    await applyAssetDetail(savedDraft.value.formData)
    ElMessage.success('已恢复本地草稿')
  }

  const handleClearDraft = async () => {
    try {
      await ElMessageBox.confirm('确认清空本地草稿吗？清空后无法恢复。', '提示', {
        type: 'warning'
      })
    } catch {
      return
    }

    clearDraftStorage()
    ElMessage.success('草稿已清空')
  }

  const handleSaveDraft = async () => {
    draftSaving.value = true
    try {
      persistDraft()
      ElMessage.success('草稿已保存到本地')
    } finally {
      draftSaving.value = false
    }
  }

  const extractAssetId = (response: any) => {
    const candidates = [
      response?.data?.assetId,
      response?.data?.id,
      response?.assetId,
      response?.id,
      typeof response?.data === 'number' ? response.data : undefined
    ]
    return candidates.find((item) => typeof item === 'number' && item > 0)
  }

  /**
   * 旧接口暂时不回 assetId 时，前端按资产编码做一次精确回查，先把继续编辑链路接起来。
   */
  const resolveAssetIdByCode = async (assetCode: string) => {
    if (!assetCode) {
      return undefined
    }

    const response: any = await listAssetInfo({ assetCode, pageNum: 1, pageSize: 10 })
    const rows = extractRows(response)
    const matchedRow = rows.find((item: any) => item.assetCode === assetCode)
    return matchedRow?.assetId
  }

  const syncCurrentAsset = async (assetId: number) => {
    currentAssetId.value = assetId
    await router.replace({
      path: '/asset/info/create',
      query: { assetId: String(assetId) }
    })
    await loadAssetDetail(assetId)
  }

  const handleSubmit = async (mode: 'back' | 'continue') => {
    if (!formRef.value) {
      return
    }

    const dynamicError = dynamicFieldsRef.value?.validate?.()
    if (dynamicError) {
      ElMessage.error(dynamicError)
      return
    }

    await formRef.value.validate(async (valid: boolean) => {
      if (!valid) {
        return
      }

      submitLoading.value = true
      try {
        const payload = buildSubmitPayload()

        if (currentAssetId.value) {
          await updateAssetInfo(payload)
          clearDraftStorage()
          ElMessage.success(mode === 'continue' ? '保存成功，已停留当前页面' : '保存成功')
          if (mode === 'back') {
            router.push('/asset/info')
            return
          }
          await loadAssetDetail(currentAssetId.value)
          return
        }

        const response: any = await addAssetInfo(payload)
        const assetId = extractAssetId(response) || (await resolveAssetIdByCode(payload.assetCode))
        clearDraftStorage()
        ElMessage.success('新增成功')

        if (!assetId) {
          if (mode === 'continue') {
            ElMessage.warning('当前接口未返回资产ID，暂时无法继续补录附件，已返回列表页。')
          }
          router.push('/asset/info')
          return
        }

        if (mode === 'back') {
          router.push('/asset/info')
          return
        }

        await syncCurrentAsset(assetId)
      } finally {
        submitLoading.value = false
      }
    })
  }

  const handleCancel = async () => {
    try {
      await ElMessageBox.confirm(
        '确定取消并返回资产台账列表吗？未保存内容会保留为本地草稿。',
        '提示',
        {
          type: 'warning',
          confirmButtonText: '返回列表',
          cancelButtonText: '继续编辑'
        }
      )
    } catch {
      return
    }

    router.push('/asset/info')
  }

  watch(
    () => formData.categoryId,
    async (value, oldValue) => {
      if (!categoryWatchEnabled.value || value === oldValue) {
        return
      }

      // 分类切换后清掉旧模板值，避免上一类资产的扩展字段误写到当前主档。
      formData.extraFieldValues = {}
      formData.extraFieldsJson = null
      await loadFieldTemplate(value)
    }
  )

  onMounted(async () => {
    await loadPageOptions()

    const routeAssetId = Number(route.query.assetId)
    if (routeAssetId) {
      await loadAssetDetail(routeAssetId)
      return
    }

    await applyAssetDetail()
    readSavedDraft()
  })
</script>

<style lang="scss" scoped>
  .asset-info-create-page {
    min-height: 100%;
    padding: 16px;
    display: flex;
    flex-direction: column;
    gap: 16px;
    // background:
    //   radial-gradient(circle at top right, rgb(17 24 39 / 5%), transparent 35%),
    //   linear-gradient(180deg, rgb(248 250 252), rgb(255 255 255));
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
    line-height: 1.2;
    color: rgb(15 23 42);
  }

  .asset-info-create-page__desc {
    margin: 10px 0 0;
    max-width: 680px;
    color: rgb(71 85 105);
    line-height: 1.7;
  }

  .asset-info-create-page__draft-tip {
    border-radius: 16px;
  }

  .asset-info-create-page__draft-actions {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
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
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
    border: 1px solid rgb(15 23 42 / 8%);
    border-radius: 18px;
    background: rgb(255 255 255 / 92%);
    backdrop-filter: blur(14px);
  }

  @media (max-width: 768px) {
    .asset-info-create-page {
      padding: 12px;
    }

    .asset-info-create-page__hero-main,
    .asset-info-create-page__draft-actions,
    .asset-info-create-page__attachment,
    .asset-info-create-page__footer {
      flex-direction: column;
      align-items: stretch;
    }

    .asset-info-create-page__title {
      font-size: 24px;
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
</style>
