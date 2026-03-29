/**
 * 资产维修列表查询状态恢复模块
 * 
 * 专门处理从创建/详情页返回列表时，对原始筛选条件的构建与重置。
 */

/** 列表恢复状态接口 */
export interface RepairListRestoreState {
  /** 维修状态 */
  repairStatus: string
  /** 维修单号 */
  repairNo: string
  /** 资产编码 */
  assetCode: string
  /** 供应商名称 */
  vendorName: string
  /** 完成结果 */
  resultType: string
}

/** 默认显示的维修状态（全部） */
const DEFAULT_REPAIR_STATUS = 'ALL'

/** 安全转换为字符串并去除空格 */
const toStringValue = (value: unknown) => {
  if (value === null || value === undefined) return ''
  return String(value).trim()
}

/**
 * 解析路由 Query 参数为列表恢复状态
 * @param query 原始路由 Query 对象
 */
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

/**
 * 将列表状态构建为可透传的路由 Query 对象
 * @param state 当前列表筛选状态
 */
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
