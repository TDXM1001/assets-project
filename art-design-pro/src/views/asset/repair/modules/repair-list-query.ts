export interface RepairListRestoreState {
  repairStatus: string
  repairNo: string
  assetCode: string
  vendorName: string
  resultType: string
}

const DEFAULT_REPAIR_STATUS = 'ALL'

const toStringValue = (value: unknown) => {
  if (value === null || value === undefined) return ''
  return String(value).trim()
}

export const resolveRepairListRestoreState = (
  query: Record<string, any> = {}
): RepairListRestoreState => {
  return {
    repairStatus: toStringValue(query.repairStatus).toUpperCase() || DEFAULT_REPAIR_STATUS,
    repairNo: toStringValue(query.repairNo),
    assetCode: toStringValue(query.assetCode),
    vendorName: toStringValue(query.vendorName),
    resultType: toStringValue(query.resultType)
  }
}

export const buildRepairListRestoreQuery = (state: Partial<RepairListRestoreState> = {}) => {
  const repairStatus = toStringValue(state.repairStatus).toUpperCase()
  const query: Record<string, string> = {}

  if (state.repairNo?.trim()) query.repairNo = state.repairNo.trim()
  if (state.assetCode?.trim()) query.assetCode = state.assetCode.trim()
  if (state.vendorName?.trim()) query.vendorName = state.vendorName.trim()
  if (state.resultType?.trim()) query.resultType = state.resultType.trim()
  if (repairStatus && repairStatus !== DEFAULT_REPAIR_STATUS) query.repairStatus = repairStatus

  return query
}
