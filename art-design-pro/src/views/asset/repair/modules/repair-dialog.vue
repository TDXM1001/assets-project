<template>
  <AssetPageShell
    v-if="pageMode"
    :loading="loading"
    eyebrow="资产维修单"
    :title="dialogType === 'add' ? '新建维修单' : '编辑维修单'"
    :description="
      dialogType === 'add'
        ? '把报修时间、维修方式和供应商信息录稳，后续审批、完工和附件都会沿用这张单据。'
        : '继续补齐当前维修单后，再提交审批和完工处理。'
    "
  >
    <template #tags>
      <ElSpace wrap>
        <ElTag type="info" effect="light">独立页面</ElTag>
        <ElTag :type="dialogType === 'add' ? 'warning' : 'success'" effect="light">
          {{ dialogType === 'add' ? '草稿阶段' : '编辑模式' }}
        </ElTag>
        <ElTag type="info" effect="plain">多资产明细</ElTag>
      </ElSpace>
    </template>

    <div class="repair-dialog-page" v-loading="loading">
      <ElAlert
        class="repair-dialog-page__tip"
        type="info"
        show-icon
        :closable="false"
        title="维修单现在支持一单多资产明细。为兼容旧接口，首条明细会同步回填到维修单头信息。"
      />

      <ElForm
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="110px"
        class="repair-dialog-page__form"
      >
        <ElCard shadow="never">
          <template #header>
            <div class="repair-dialog-page__section-header">
              <div>
                <div class="repair-dialog-page__section-title">基础信息</div>
                <div class="repair-dialog-page__section-subtitle">
                  先把报修时间、维修方式和供应商信息录稳，再继续录入资产明细。
                </div>
              </div>
            </div>
          </template>

          <ElRow :gutter="16">
            <ElCol :xs="24" :md="12">
              <ElFormItem label="维修单号" prop="repairNo">
                <ElInput v-model="formData.repairNo" disabled placeholder="提交后由系统生成" />
              </ElFormItem>
            </ElCol>
            <ElCol :xs="24" :md="12">
              <ElFormItem label="报修时间" prop="reportTime">
                <ElDatePicker
                  v-model="formData.reportTime"
                  type="datetime"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  class="w-full"
                  placeholder="请选择报修时间"
                />
              </ElFormItem>
            </ElCol>
            <ElCol :xs="24" :md="12">
              <ElFormItem label="维修方式" prop="repairMode">
                <ElSelect v-model="formData.repairMode" class="w-full" placeholder="请选择维修方式">
                  <ElOption
                    v-for="item in asset_repair_mode"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                  />
                </ElSelect>
              </ElFormItem>
            </ElCol>
            <ElCol :xs="24" :md="12">
              <ElFormItem label="外部供应商" prop="vendorName">
                <ElInput
                  v-model="formData.vendorName"
                  maxlength="100"
                  placeholder="请输入供应商名称，内部维修可留空"
                />
              </ElFormItem>
            </ElCol>
          </ElRow>
        </ElCard>

        <ElCard shadow="never">
          <template #header>
            <div class="repair-dialog-page__section-header">
              <div>
                <div class="repair-dialog-page__section-title">发起信息</div>
                <div class="repair-dialog-page__section-subtitle">
                  这里记录是谁在什么部门发起这张维修单，后续审批和完工会持续沿用。
                </div>
              </div>
            </div>
          </template>

          <ElRow :gutter="16">
            <ElCol :xs="24" :md="12">
              <ElFormItem label="发起部门" prop="applyDeptId">
                <ElTreeSelect
                  v-model="formData.applyDeptId"
                  :data="deptOptions"
                  :props="{ value: 'id', label: 'label', children: 'children' }"
                  value-key="id"
                  filterable
                  clearable
                  check-strictly
                  class="w-full"
                  placeholder="请选择发起部门"
                />
              </ElFormItem>
            </ElCol>
            <ElCol :xs="24" :md="12">
              <ElFormItem label="发起人" prop="applyUserId">
                <ElSelect
                  v-model="formData.applyUserId"
                  filterable
                  clearable
                  class="w-full"
                  placeholder="请选择发起人"
                >
                  <ElOption
                    v-for="item in userOptions"
                    :key="item.userId"
                    :label="item.displayName"
                    :value="item.userId"
                  />
                </ElSelect>
              </ElFormItem>
            </ElCol>
          </ElRow>
        </ElCard>

        <ElCard shadow="never">
          <template #header>
            <div class="repair-dialog-page__section-header">
              <div>
                <div class="repair-dialog-page__section-title">维修资产明细</div>
                <div class="repair-dialog-page__section-subtitle">
                  多资产维修会逐条记录故障描述和状态。旧流程仍可只选一条资产，不会中断原有录入方式。
                </div>
              </div>
            </div>
          </template>

          <ElFormItem prop="repairItems" class="repair-items-form-item">
            <ElAlert
              class="mb-4"
              type="warning"
              show-icon
              :closable="false"
              title="多资产维修会逐条记录故障描述和状态。旧流程仍可只选一条资产，不会中断原有录入方式。"
            />

            <div class="repair-asset-toolbar">
              <ElSpace wrap>
                <ElButton type="primary" plain @click="selectorVisible = true">选择资产</ElButton>
                <ElButton :disabled="!formData.repairItems.length" @click="handleClearAssets">
                  清空明细
                </ElButton>
              </ElSpace>
              <div class="repair-asset-toolbar__summary">
                已选择 {{ formData.repairItems.length }} 项资产
              </div>
            </div>

            <ElTable
              :data="formData.repairItems"
              row-key="rowKey"
              border
              stripe
              max-height="420"
              class="repair-item-table"
              empty-text="请先选择维修资产"
            >
              <ElTableColumn type="index" width="56" label="#" />
              <ElTableColumn prop="assetCode" label="资产编码" min-width="140" />
              <ElTableColumn prop="assetName" label="资产名称" min-width="180" />
              <ElTableColumn label="维修前状态" width="120" align="center">
                <template #default="{ row }">
                  <DictTag :options="asset_status" :value="row.beforeStatus" />
                </template>
              </ElTableColumn>
              <ElTableColumn label="维修后状态" width="150">
                <template #default="{ row }">
                  <ElSelect
                    v-model="row.afterStatus"
                    clearable
                    placeholder="请选择"
                    style="width: 100%"
                  >
                    <ElOption
                      v-for="item in asset_status"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                    />
                  </ElSelect>
                </template>
              </ElTableColumn>
              <ElTableColumn label="维修结果" width="150">
                <template #default="{ row }">
                  <ElSelect
                    v-model="row.resultType"
                    clearable
                    placeholder="请选择"
                    style="width: 100%"
                  >
                    <ElOption
                      v-for="item in asset_repair_result_type"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                    />
                  </ElSelect>
                </template>
              </ElTableColumn>
              <ElTableColumn label="故障描述" min-width="220">
                <template #default="{ row }">
                  <ElInput
                    v-model="row.faultDesc"
                    type="textarea"
                    :rows="2"
                    maxlength="300"
                    show-word-limit
                    placeholder="请输入该资产的故障现象"
                  />
                </template>
              </ElTableColumn>
              <ElTableColumn label="行级说明" min-width="180">
                <template #default="{ row }">
                  <ElInput v-model="row.remark" maxlength="200" placeholder="配件、送修说明等" />
                </template>
              </ElTableColumn>
              <ElTableColumn label="当前责任链" min-width="220">
                <template #default="{ row }">
                  <div class="repair-item-meta">
                    <div>{{ resolveUserName(row.currentUserId, row.currentUserName) }}</div>
                    <div>{{ resolveDeptName(row.currentDeptId, row.currentDeptName) }}</div>
                    <div>{{
                      resolveLocationName(row.currentLocationId, row.currentLocationName)
                    }}</div>
                  </div>
                </template>
              </ElTableColumn>
              <ElTableColumn label="操作" width="90" align="center" fixed="right">
                <template #default="{ $index }">
                  <ElButton link type="danger" @click="handleRemoveRepairItem($index)">
                    移除
                  </ElButton>
                </template>
              </ElTableColumn>
            </ElTable>
          </ElFormItem>
        </ElCard>

        <ElCard shadow="never">
          <template #header>
            <div class="repair-dialog-page__section-header">
              <div>
                <div class="repair-dialog-page__section-title">整体说明</div>
                <div class="repair-dialog-page__section-subtitle">
                  这里补充整单层面的报修背景和备注，后续审批和处置单桥接都会沿用。
                </div>
              </div>
            </div>
          </template>

          <ElRow :gutter="16">
            <ElCol :span="24">
              <ElFormItem label="报修概述" prop="faultDesc">
                <ElInput
                  v-model="formData.faultDesc"
                  type="textarea"
                  :rows="3"
                  maxlength="500"
                  show-word-limit
                  placeholder="请输入整单报修背景，例如批量送修原因、现场影响范围和统一处理诉求"
                />
              </ElFormItem>
            </ElCol>
            <ElCol :span="24">
              <ElFormItem label="备注" prop="remark">
                <ElInput
                  v-model="formData.remark"
                  type="textarea"
                  :rows="3"
                  maxlength="500"
                  show-word-limit
                  placeholder="请输入补充说明，例如联系人、外部工单号或配件准备情况"
                />
              </ElFormItem>
            </ElCol>
          </ElRow>
        </ElCard>
      </ElForm>
    </div>
  </AssetPageShell>

  <component
    v-else
    :is="EpDialog"
    v-model="visible"
    :title="dialogType === 'add' ? '新增维修单' : '编辑维修单'"
    width="1280px"
    destroy-on-close
    append-to-body
    modal
    show-close
    @closed="handleClosed"
  >
    <ElAlert
      class="mb-4"
      type="info"
      show-icon
      :closable="false"
      title="维修单现在支持一单多资产明细。为兼容旧接口，首条明细会同步回填到维修单头信息。"
    />

    <ElForm
      ref="formRef"
      v-loading="loading"
      :model="formData"
      :rules="formRules"
      label-width="110px"
    >
      <ElDivider content-position="left">基础信息</ElDivider>
      <ElRow :gutter="16">
        <ElCol :span="12">
          <ElFormItem label="维修单号" prop="repairNo">
            <ElInput v-model="formData.repairNo" disabled placeholder="提交后由系统生成" />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="报修时间" prop="reportTime">
            <ElDatePicker
              v-model="formData.reportTime"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
              class="w-full"
              placeholder="请选择报修时间"
            />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="维修方式" prop="repairMode">
            <ElSelect v-model="formData.repairMode" class="w-full" placeholder="请选择维修方式">
              <ElOption
                v-for="item in asset_repair_mode"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </ElSelect>
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="外部供应商" prop="vendorName">
            <ElInput
              v-model="formData.vendorName"
              maxlength="100"
              placeholder="请输入供应商名称，内部维修可留空"
            />
          </ElFormItem>
        </ElCol>
      </ElRow>

      <ElDivider content-position="left">发起信息</ElDivider>
      <ElRow :gutter="16">
        <ElCol :span="12">
          <ElFormItem label="发起部门" prop="applyDeptId">
            <ElTreeSelect
              v-model="formData.applyDeptId"
              :data="deptOptions"
              :props="{ value: 'id', label: 'label', children: 'children' }"
              value-key="id"
              filterable
              clearable
              check-strictly
              class="w-full"
              placeholder="请选择发起部门"
            />
          </ElFormItem>
        </ElCol>
        <ElCol :span="12">
          <ElFormItem label="发起人" prop="applyUserId">
            <ElSelect
              v-model="formData.applyUserId"
              filterable
              clearable
              class="w-full"
              placeholder="请选择发起人"
            >
              <ElOption
                v-for="item in userOptions"
                :key="item.userId"
                :label="item.displayName"
                :value="item.userId"
              />
            </ElSelect>
          </ElFormItem>
        </ElCol>
      </ElRow>

      <ElDivider content-position="left">维修资产明细</ElDivider>
      <ElFormItem prop="repairItems" class="repair-items-form-item">
        <ElAlert
          class="mb-4"
          type="warning"
          show-icon
          :closable="false"
          title="多资产维修会逐条记录故障描述和状态。旧流程仍可只选一条资产，不会中断原有录入方式。"
        />

        <div class="repair-asset-toolbar">
          <ElSpace wrap>
            <ElButton type="primary" plain @click="selectorVisible = true">选择资产</ElButton>
            <ElButton :disabled="!formData.repairItems.length" @click="handleClearAssets">
              清空明细
            </ElButton>
          </ElSpace>
          <div class="repair-asset-toolbar__summary">
            已选择 {{ formData.repairItems.length }} 项资产
          </div>
        </div>

        <ElTable
          :data="formData.repairItems"
          row-key="rowKey"
          border
          stripe
          max-height="420"
          class="repair-item-table"
          empty-text="请先选择维修资产"
        >
          <ElTableColumn type="index" width="56" label="#" />
          <ElTableColumn prop="assetCode" label="资产编码" min-width="140" />
          <ElTableColumn prop="assetName" label="资产名称" min-width="180" />
          <ElTableColumn label="维修前状态" width="120" align="center">
            <template #default="{ row }">
              <DictTag :options="asset_status" :value="row.beforeStatus" />
            </template>
          </ElTableColumn>
          <ElTableColumn label="维修后状态" width="150">
            <template #default="{ row }">
              <ElSelect
                v-model="row.afterStatus"
                clearable
                placeholder="请选择"
                style="width: 100%"
              >
                <ElOption
                  v-for="item in asset_status"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </ElSelect>
            </template>
          </ElTableColumn>
          <ElTableColumn label="维修结果" width="150">
            <template #default="{ row }">
              <ElSelect v-model="row.resultType" clearable placeholder="请选择" style="width: 100%">
                <ElOption
                  v-for="item in asset_repair_result_type"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </ElSelect>
            </template>
          </ElTableColumn>
          <ElTableColumn label="故障描述" min-width="220">
            <template #default="{ row }">
              <ElInput
                v-model="row.faultDesc"
                type="textarea"
                :rows="2"
                maxlength="300"
                show-word-limit
                placeholder="请输入该资产的故障现象"
              />
            </template>
          </ElTableColumn>
          <ElTableColumn label="行级说明" min-width="180">
            <template #default="{ row }">
              <ElInput v-model="row.remark" maxlength="200" placeholder="配件、送修说明等" />
            </template>
          </ElTableColumn>
          <ElTableColumn label="当前责任链" min-width="220">
            <template #default="{ row }">
              <div class="repair-item-meta">
                <div>{{ resolveUserName(row.currentUserId, row.currentUserName) }}</div>
                <div>{{ resolveDeptName(row.currentDeptId, row.currentDeptName) }}</div>
                <div>{{ resolveLocationName(row.currentLocationId, row.currentLocationName) }}</div>
              </div>
            </template>
          </ElTableColumn>
          <ElTableColumn label="操作" width="90" align="center" fixed="right">
            <template #default="{ $index }">
              <ElButton link type="danger" @click="handleRemoveRepairItem($index)"> 移除 </ElButton>
            </template>
          </ElTableColumn>
        </ElTable>
      </ElFormItem>

      <ElDivider content-position="left">整体说明</ElDivider>
      <ElRow :gutter="16">
        <ElCol :span="24">
          <ElFormItem label="报修概述" prop="faultDesc">
            <ElInput
              v-model="formData.faultDesc"
              type="textarea"
              :rows="3"
              maxlength="500"
              show-word-limit
              placeholder="请输入整单报修背景，例如批量送修原因、现场影响范围和统一处理诉求"
            />
          </ElFormItem>
        </ElCol>
        <ElCol :span="24">
          <ElFormItem label="备注" prop="remark">
            <ElInput
              v-model="formData.remark"
              type="textarea"
              :rows="3"
              maxlength="500"
              show-word-limit
              placeholder="请输入补充说明，例如联系人、外部工单号或配件准备情况"
            />
          </ElFormItem>
        </ElCol>
      </ElRow>
    </ElForm>

    <template #footer>
      <div class="dialog-footer">
        <ElButton @click="handleCancel">取消</ElButton>
        <ElButton type="primary" :loading="submitLoading" @click="handleSubmit">确定</ElButton>
      </div>
    </template>
  </component>
  <div v-if="pageMode" class="dialog-footer repair-dialog-page-footer">
    <ElButton @click="handleCancel">取消返回</ElButton>
    <ElButton type="primary" :loading="submitLoading" @click="handleSubmit">保存维修单</ElButton>
  </div>

  <ElDialog
    v-model="selectorVisible"
    title="选择维修资产"
    width="1120px"
    append-to-body
    destroy-on-close
    @open="handleSelectorOpen"
    @closed="handleSelectorClosed"
  >
    <ElForm :model="selectorQuery" inline class="repair-selector-search">
      <ElFormItem label="资产编码">
        <ElInput v-model="selectorQuery.assetCode" clearable placeholder="请输入资产编码" />
      </ElFormItem>
      <ElFormItem label="资产名称">
        <ElInput v-model="selectorQuery.assetName" clearable placeholder="请输入资产名称" />
      </ElFormItem>
      <ElFormItem label="资产状态">
        <ElSelect
          v-model="selectorQuery.assetStatus"
          clearable
          placeholder="请选择资产状态"
          style="width: 160px"
        >
          <ElOption
            v-for="item in asset_status"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </ElSelect>
      </ElFormItem>
      <ElFormItem>
        <ElSpace>
          <ElButton type="primary" @click="handleSelectorSearch">查询</ElButton>
          <ElButton @click="handleSelectorReset">重置</ElButton>
        </ElSpace>
      </ElFormItem>
    </ElForm>

    <ElTable
      v-loading="selectorLoading"
      :data="selectorData"
      row-key="assetId"
      border
      stripe
      height="420"
      @selection-change="handleAssetSelectionChange"
    >
      <ElTableColumn type="selection" width="52" reserve-selection />
      <ElTableColumn prop="assetCode" label="资产编码" min-width="140" />
      <ElTableColumn prop="assetName" label="资产名称" min-width="180" />
      <ElTableColumn label="资产状态" width="120" align="center">
        <template #default="{ row }">
          <DictTag :options="asset_status" :value="row.assetStatus" />
        </template>
      </ElTableColumn>
      <ElTableColumn label="责任人" min-width="140">
        <template #default="{ row }">
          {{ resolveUserName(row.currentUserId, row.currentUserName) }}
        </template>
      </ElTableColumn>
      <ElTableColumn label="使用部门" min-width="150">
        <template #default="{ row }">
          {{ resolveDeptName(row.useOrgDeptId, row.useOrgDeptName) }}
        </template>
      </ElTableColumn>
      <ElTableColumn label="当前位置" min-width="160">
        <template #default="{ row }">
          {{ resolveLocationName(row.currentLocationId, row.currentLocationName) }}
        </template>
      </ElTableColumn>
    </ElTable>

    <div class="repair-selector-pagination">
      <ElPagination
        background
        layout="total, sizes, prev, pager, next"
        :current-page="selectorQuery.pageNum"
        :page-size="selectorQuery.pageSize"
        :page-sizes="[10, 20, 50]"
        :total="selectorTotal"
        @current-change="handleSelectorPageChange"
        @size-change="handleSelectorSizeChange"
      />
    </div>

    <template #footer>
      <div class="dialog-footer">
        <ElButton @click="selectorVisible = false">取消</ElButton>
        <ElButton type="primary" @click="handleApplySelectedAssets">确认选择</ElButton>
      </div>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import { computed, nextTick, reactive, ref, watch } from 'vue'
  import type { FormInstance, FormRules } from 'element-plus'
  import { ElDialog as EpDialog, ElMessage, ElMessageBox } from 'element-plus'
  import { deptTreeSelect, listUser } from '@/api/system/user'
  import { treeLocationSelect } from '@/api/asset/location'
  import { listAssetInfo } from '@/api/asset/info'
  import {
    addAssetRepair,
    getAssetRepair,
    updateAssetRepair,
    type AssetRepairItemPayload,
    type AssetRepairPayload
  } from '@/api/asset/repair'
  import DictTag from '@/components/DictTag/index.vue'
  import { useDict } from '@/utils/dict'
  import { useUserStore } from '@/store/modules/user'
  import AssetPageShell from '../../shared/asset-page-shell.vue'
  import { buildRepairHeaderStateFromItems, buildRepairSubmitPayload } from './repair-draft-payload'
  import { createRepairItemFromAsset, resolveRepairFormState } from './repair-item-normalize'

  interface TreeOption {
    id: number
    label: string
    children?: TreeOption[]
  }

  interface UserOption {
    userId: number
    displayName: string
  }

  interface AssetCandidate {
    assetId: number
    assetCode: string
    assetName: string
    assetStatus?: string
    currentUserId?: number
    currentUserName?: string
    useOrgDeptId?: number
    useOrgDeptName?: string
    currentLocationId?: number
    currentLocationName?: string
  }

  interface RepairItemFormData extends AssetRepairItemPayload {
    rowKey: string
  }


  const { asset_status, asset_repair_mode, asset_repair_result_type } = useDict(
    'asset_status',
    'asset_repair_mode',
    'asset_repair_result_type'
  )
  const userStore = useUserStore()
  const currentUserInfo = computed(() => userStore.getUserInfo)

  const props = defineProps<{
    modelValue: boolean
    pageMode?: boolean
    dialogType: 'add' | 'edit'
    repairData?: Record<string, any>
  }>()

  const emit = defineEmits<{
    (e: 'update:modelValue', value: boolean): void
    (e: 'success'): void
    (e: 'cancel'): void
  }>()

  const visible = ref(false)
  const loading = ref(false)
  const submitLoading = ref(false)
  const selectorVisible = ref(false)
  const selectorLoading = ref(false)
  const formRef = ref<FormInstance>()

  const deptOptions = ref<TreeOption[]>([])
  const locationOptions = ref<TreeOption[]>([])
  const userOptions = ref<UserOption[]>([])
  const deptLabelMap = ref<Record<string, string>>({})
  const locationLabelMap = ref<Record<string, string>>({})
  const userLabelMap = ref<Record<string, string>>({})

  const selectorData = ref<AssetCandidate[]>([])
  const selectorTotal = ref(0)
  const selectedAssets = ref<AssetCandidate[]>([])

  const createInitialFormData = () => ({
    repairId: undefined as number | undefined,
    repairNo: '',
    reportTime: '',
    repairStatus: 'DRAFT',
    repairMode: 'IN_HOUSE',
    applyDeptId: undefined as number | undefined,
    applyUserId: undefined as number | undefined,
    assetId: undefined as number | undefined,
    assetCode: '',
    assetName: '',
    beforeStatus: '',
    afterStatus: '',
    currentUserId: undefined as number | undefined,
    currentDeptId: undefined as number | undefined,
    currentLocationId: undefined as number | undefined,
    faultDesc: '',
    vendorName: '',
    repairCost: undefined as number | undefined,
    downtimeHours: undefined as number | undefined,
    reworkFlag: '0',
    remark: '',
    repairItems: [] as RepairItemFormData[]
  })

  const formData = reactive(createInitialFormData())

  const selectorQuery = reactive({
    pageNum: 1,
    pageSize: 10,
    assetCode: '',
    assetName: '',
    assetStatus: ''
  })

  const validateRepairItems = (_rule: unknown, value: RepairItemFormData[], callback: any) => {
    if (!Array.isArray(value) || value.length === 0) {
      callback(new Error('请至少选择一条维修资产明细'))
      return
    }

    const seenIds = new Set<number>()
    for (const item of value) {
      if (!item.assetId) {
        callback(new Error('维修明细里存在未绑定资产的行，请重新选择'))
        return
      }
      if (seenIds.has(item.assetId)) {
        callback(new Error('同一资产不能重复加入同一张维修单'))
        return
      }
      seenIds.add(item.assetId)

      if (!String(item.faultDesc || '').trim()) {
        callback(
          new Error(`请补充资产【${item.assetName || item.assetCode || item.assetId}】的故障描述`)
        )
        return
      }
    }

    callback()
  }

  const formRules: FormRules = {
    reportTime: [{ required: true, message: '请选择报修时间', trigger: 'change' }],
    repairMode: [{ required: true, message: '请选择维修方式', trigger: 'change' }],
    applyDeptId: [{ required: true, message: '请选择发起部门', trigger: 'change' }],
    applyUserId: [{ required: true, message: '请选择发起人', trigger: 'change' }],
    repairItems: [{ validator: validateRepairItems, trigger: 'change' }]
  }

  const flattenTreeLabels = (nodes: TreeOption[], map: Record<string, string>) => {
    nodes.forEach((node) => {
      map[String(node.id)] = node.label
      if (node.children?.length) {
        flattenTreeLabels(node.children, map)
      }
    })
  }

  const normalizeTreeOptions = (list: any[]): TreeOption[] =>
    (list || []).map((item) => ({
      id: item.id ?? item.value ?? item.deptId ?? item.locationId,
      label: item.label ?? item.deptName ?? item.locationName ?? item.name ?? String(item.id),
      children: item.children ? normalizeTreeOptions(item.children) : []
    }))

  const loadRelationOptions = async () => {
    const [deptRes, locationRes, userRes] = await Promise.all([
      deptTreeSelect(),
      treeLocationSelect(),
      listUser({ pageNum: 1, pageSize: 500 })
    ])

    const deptData = Array.isArray(deptRes) ? deptRes : (deptRes as any)?.data || []
    const locationData = Array.isArray(locationRes) ? locationRes : (locationRes as any)?.data || []
    const userData = Array.isArray(userRes)
      ? userRes
      : (userRes as any)?.rows || (userRes as any)?.data || []

    deptOptions.value = normalizeTreeOptions(deptData)
    locationOptions.value = normalizeTreeOptions(locationData)
    userOptions.value = userData.map((item: any) => ({
      userId: item.userId,
      displayName: item.nickName ? `${item.nickName} (${item.userName})` : item.userName
    }))

    const nextDeptMap: Record<string, string> = {}
    const nextLocationMap: Record<string, string> = {}
    flattenTreeLabels(deptOptions.value, nextDeptMap)
    flattenTreeLabels(locationOptions.value, nextLocationMap)
    deptLabelMap.value = nextDeptMap
    locationLabelMap.value = nextLocationMap
    userLabelMap.value = userOptions.value.reduce<Record<string, string>>((acc, item) => {
      acc[String(item.userId)] = item.displayName
      return acc
    }, {})
  }

  const fillDefaultApplicant = () => {
    const userInfo = (currentUserInfo.value || {}) as Record<string, any>
    if (userInfo.userId) formData.applyUserId = userInfo.userId
    if (userInfo.deptId) formData.applyDeptId = userInfo.deptId
  }

  const resetForm = () => {
    Object.assign(formData, createInitialFormData())
    fillDefaultApplicant()
  }

  const resolveDeptName = (deptId?: number, fallback?: string) => {
    if (fallback) return fallback
    if (deptId === null || deptId === undefined) return '-'
    return deptLabelMap.value[String(deptId)] || String(deptId)
  }

  const resolveLocationName = (locationId?: number, fallback?: string) => {
    if (fallback) return fallback
    if (locationId === null || locationId === undefined) return '-'
    return locationLabelMap.value[String(locationId)] || String(locationId)
  }

  const resolveUserName = (userId?: number, fallback?: string) => {
    if (fallback) return fallback
    if (userId === null || userId === undefined) return '-'
    return userLabelMap.value[String(userId)] || String(userId)
  }

  const applyPrimaryItemSnapshot = () => {
    Object.assign(
      formData,
      buildRepairHeaderStateFromItems(formData.repairItems, formData.faultDesc)
    )
  }

  const loadSelectorData = async () => {
    selectorLoading.value = true
    try {
      const response: any = await listAssetInfo({ ...selectorQuery })
      selectorData.value = response?.rows || response?.data?.rows || response?.data || []
      selectorTotal.value = Number(response?.total || response?.data?.total || 0)
    } finally {
      selectorLoading.value = false
    }
  }

  const handleSelectorSearch = () => {
    selectorQuery.pageNum = 1
    void loadSelectorData()
  }

  const handleSelectorReset = () => {
    selectorQuery.pageNum = 1
    selectorQuery.assetCode = ''
    selectorQuery.assetName = ''
    selectorQuery.assetStatus = ''
    void loadSelectorData()
  }

  const handleSelectorPageChange = (pageNum: number) => {
    selectorQuery.pageNum = pageNum
    void loadSelectorData()
  }

  const handleSelectorSizeChange = (pageSize: number) => {
    selectorQuery.pageSize = pageSize
    selectorQuery.pageNum = 1
    void loadSelectorData()
  }

  const handleAssetSelectionChange = (rows: AssetCandidate[]) => {
    selectedAssets.value = rows
  }

  const handleApplySelectedAssets = async () => {
    if (!selectedAssets.value.length) {
      ElMessage.warning('请先选择至少一条资产')
      return
    }

    const selectedIds = new Set(formData.repairItems.map((item) => item.assetId).filter(Boolean))
    const nextItems = selectedAssets.value
      .filter((asset) => !selectedIds.has(asset.assetId))
      .map((asset) => createRepairItemFromAsset(asset))

    if (!nextItems.length) {
      ElMessage.warning('所选资产都已经在当前维修单里了')
      return
    }

    formData.repairItems.push(...nextItems)
    applyPrimaryItemSnapshot()
    selectorVisible.value = false
    await nextTick()
    formRef.value?.validateField?.('repairItems').catch(() => undefined)
  }

  const handleRemoveRepairItem = async (index: number) => {
    const item = formData.repairItems[index]
    if (!item) return

    try {
      await ElMessageBox.confirm(
        `确认移除资产“${item.assetName || item.assetCode || item.assetId}”吗？`,
        '提示',
        { type: 'warning' }
      )
      formData.repairItems.splice(index, 1)
      applyPrimaryItemSnapshot()
      await nextTick()
      formRef.value?.validateField?.('repairItems').catch(() => undefined)
    } catch (error) {
      if (error !== 'cancel') {
        console.error('移除维修明细失败:', error)
      }
    }
  }

  const handleClearAssets = async () => {
    try {
      await ElMessageBox.confirm('确认清空当前维修明细吗？', '提示', { type: 'warning' })
      formData.repairItems = []
      applyPrimaryItemSnapshot()
      await nextTick()
      formRef.value?.validateField?.('repairItems').catch(() => undefined)
    } catch (error) {
      if (error !== 'cancel') {
        console.error('清空维修明细失败:', error)
      }
    }
  }

  const handleSelectorOpen = async () => {
    selectedAssets.value = []
    await loadSelectorData()
  }

  const handleSelectorClosed = () => {
    selectedAssets.value = []
  }

  watch(
    () => props.modelValue,
    async (value) => {
      visible.value = value
      if (!value) return

      loading.value = true
      try {
        await loadRelationOptions()
        if (props.dialogType === 'edit' && props.repairData?.repairId) {
          const detail: any = await getAssetRepair(props.repairData.repairId)
          const repair = detail?.data || detail || props.repairData
          Object.assign(formData, createInitialFormData(), repair, resolveRepairFormState(repair))
        } else {
          resetForm()
        }
      } catch (error) {
        console.error('加载维修单详情失败:', error)
        if (props.dialogType === 'edit') {
          Object.assign(
            formData,
            createInitialFormData(),
            props.repairData || {},
            resolveRepairFormState(props.repairData)
          )
        } else {
          resetForm()
        }
      } finally {
        loading.value = false
      }
    },
    { immediate: true }
  )

  watch(
    () => props.pageMode,
    (value) => {
      if (value) {
        // 页面模式下不再依赖弹层开关，始终保持可见，避免页面主体被关闭事件折叠掉。
        visible.value = true
      }
    },
    { immediate: true }
  )

  watch(
    () => visible.value,
    (value) => {
      if (!props.pageMode) {
        emit('update:modelValue', value)
      } else if (value) {
        // 页面模式下不允许把主流程关掉，关闭动作由页面壳自己接管。
        emit('update:modelValue', true)
      }
    }
  )

  const handleCancel = () => {
    if (props.pageMode) {
      emit('cancel')
      return
    }
    visible.value = false
  }

  const buildPayload = (): AssetRepairPayload =>
    buildRepairSubmitPayload(formData) as AssetRepairPayload

  const handleSubmit = async () => {
    if (!formRef.value) return
    const valid = await formRef.value.validate().catch(() => false)
    if (!valid) return

    submitLoading.value = true
    try {
      const payload = buildPayload()
      if (props.dialogType === 'add') {
        await addAssetRepair(payload)
      } else {
        await updateAssetRepair(payload)
      }
      ElMessage.success(props.dialogType === 'add' ? '新增成功' : '修改成功')
      visible.value = false
      emit('success')
    } catch (error) {
      console.error('提交维修单失败:', error)
    } finally {
      submitLoading.value = false
    }
  }

  const handleClosed = () => {
    resetForm()
    selectorVisible.value = false
    selectedAssets.value = []
    selectorData.value = []
    selectorTotal.value = 0
    selectorQuery.pageNum = 1
    selectorQuery.pageSize = 10
    selectorQuery.assetCode = ''
    selectorQuery.assetName = ''
    selectorQuery.assetStatus = ''
    loading.value = false
    submitLoading.value = false
    formRef.value?.clearValidate?.()
  }
</script>

<style scoped lang="scss">
  .repair-dialog-page {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .repair-dialog-page :deep(.el-card) {
    border-radius: 18px;
    border-color: rgb(15 23 42 / 8%);
  }

  .repair-dialog-page__tip {
    border-radius: 16px;
  }

  .repair-dialog-page__form {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .repair-dialog-page__section-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
  }

  .repair-dialog-page__section-title {
    font-size: 16px;
    font-weight: 700;
    color: rgb(15 23 42);
  }

  .repair-dialog-page__section-subtitle {
    margin-top: 4px;
    font-size: 13px;
    color: rgb(100 116 139);
    line-height: 1.6;
  }

  .repair-items-form-item {
    display: block;
  }

  .repair-asset-toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 12px;
    margin-bottom: 16px;
  }

  .repair-asset-toolbar__summary {
    color: var(--el-text-color-secondary);
    font-size: 13px;
  }

  .repair-item-table {
    width: 100%;
  }

  .repair-item-meta {
    display: flex;
    flex-direction: column;
    gap: 4px;
    color: var(--el-text-color-secondary);
    font-size: 12px;
    line-height: 1.5;
  }

  .repair-selector-search {
    margin-bottom: 16px;
  }

  .repair-selector-pagination {
    display: flex;
    justify-content: flex-end;
    margin-top: 16px;
  }

  .repair-dialog--page {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .repair-dialog-page-footer {
    position: sticky;
    bottom: 0;
    z-index: 10;
    padding: 14px 18px;
    display: flex;
    align-items: center;
    justify-content: flex-end;
    gap: 16px;
    border: 1px solid rgb(15 23 42 / 8%);
    border-radius: 18px;
    background: rgb(255 255 255 / 92%);
    backdrop-filter: blur(14px);
  }
</style>
