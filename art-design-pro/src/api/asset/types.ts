export type AssetFieldDataType = 'string' | 'number' | 'date' | 'boolean'

export type AssetFieldComponentType = 'input' | 'textarea' | 'number' | 'select' | 'radio' | 'date'

export interface AssetFieldOption {
  label: string
  value: string | number
}

export interface AssetCategoryFieldTemplateField {
  fieldCode: string
  fieldName: string
  dataType: AssetFieldDataType
  componentType: AssetFieldComponentType
  requiredFlag: '0' | '1'
  readonlyFlag: '0' | '1'
  dictType?: string
  defaultValue?: string
  orderNum: number
  groupName?: string
  status?: '0' | '1'
}

export interface AssetCategoryFieldTemplate {
  categoryId?: number
  templateVersion?: number
  status: '0' | '1'
  fields: AssetCategoryFieldTemplateField[]
}

export type AssetInfoDynamicFieldValues = Record<string, string | number | null | undefined>

export type AssetInfoDynamicFieldValue = AssetInfoDynamicFieldValues

export interface AssetInfoSubmitPayload {
  assetId?: number
  assetCode: string
  assetName: string
  categoryId?: number
  brand?: string
  model?: string
  specification?: string
  serialNo?: string
  assetStatus: string
  assetSource?: string
  useOrgDeptId?: number
  manageDeptId?: number
  currentUserId?: number
  currentLocationId?: number
  purchaseDate?: string
  inboundDate?: string
  startUseDate?: string
  originalValue?: number
  residualValue?: number
  warrantyExpireDate?: string
  supplierName?: string
  qrCode?: string
  versionNo?: number
  templateVersion?: number
  extraFieldsJson?: string
  status?: string
  remark?: string
}

export type AssetInfoPayload = AssetInfoSubmitPayload
