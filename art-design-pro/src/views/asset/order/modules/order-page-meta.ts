export interface OrderPageTagMeta {
  text: string
  type?: 'primary' | 'success' | 'warning' | 'danger' | 'info'
  effect?: 'dark' | 'light' | 'plain'
}

export interface OrderPageMeta {
  orderType: string
  orderTypeLabel: string
  pageTitle: string
  pageDescription: string
  listScopeTip: string
  listEmptyDescription: string
  statusTags: OrderPageTagMeta[]
}

const DEFAULT_META: Omit<OrderPageMeta, 'orderType' | 'orderTypeLabel'> = {
  pageTitle: '新建业务单据',
  pageDescription:
    '把流转方向、归属字段和资产明细一次录稳，后续审批、完成和事件流水都会沿用这张单据。',
  listScopeTip: '这里不是普通表格页，而是单据流转工作台，先用类型切换把流程边界收住。',
  listEmptyDescription: '还没有业务单据，先新增一张单据，把流程跑起来。',
  statusTags: [
    { text: '独立页面', type: 'info', effect: 'light' },
    { text: '草稿阶段', type: 'warning', effect: 'light' }
  ]
}

export type OrderAfterStatusStrategy = 'DISPOSED' | 'RETURN_AFTER' | 'IN_USE'

export interface OrderTypeViewConfig {
  orderType: string
  showTargetFields: boolean
  showReturnAfterStatus: boolean
  amountLabel: string
  amountProp: 'disposalAmount' | 'expectedReturnDate'
  amountFieldType: 'number' | 'date'
  remarkLabel: string
  remarkProp: 'disposalReason' | 'remark'
  requiresDisposalFields: boolean
  afterStatusStrategy: OrderAfterStatusStrategy
  sectionTitle: string
  sectionSubtitle: string
  detailSectionTitle: string
  detailSectionSubtitle: string
}

interface OrderPageContextRow {
  orderType?: string
  fromDeptName?: string
  fromDeptId?: string | number
  toDeptName?: string
  toDeptId?: string | number
  fromUserName?: string
  fromUserId?: string | number
  toUserName?: string
  toUserId?: string | number
  fromLocationName?: string
  fromLocationId?: string | number
  toLocationName?: string
  toLocationId?: string | number
  expectedReturnDate?: string
  disposalReason?: string
  disposalAmount?: unknown
}

const normalizeOrderType = (orderType?: string) => {
  const normalized = String(orderType || '')
    .trim()
    .toUpperCase()
  if (!normalized || normalized === 'ALL') return ''
  return normalized
}

const resolveOrderTypeLabel = (orderType: string, options: any[] = []) => {
  const matched = options.find((item: any) => String(item.value) === orderType)
  return matched?.label || orderType || '业务单据'
}

const displayText = (value: unknown, fallback = '-') => {
  if (value === null || value === undefined || value === '') return fallback
  return String(value)
}

const resolveTypeFieldText = (primary: unknown, secondary: unknown, fallback: string) =>
  displayText(primary ?? secondary, fallback)

const buildScopeSummary = (row: OrderPageContextRow, fallbackLabel = '待补充') => {
  const fromDept = resolveTypeFieldText(
    row.fromDeptName,
    row.fromDeptId,
    `来源部门${fallbackLabel}`
  )
  const toDept = resolveTypeFieldText(row.toDeptName, row.toDeptId, `目标部门${fallbackLabel}`)
  const fromUser = resolveTypeFieldText(
    row.fromUserName,
    row.fromUserId,
    `来源责任人${fallbackLabel}`
  )
  const toUser = resolveTypeFieldText(row.toUserName, row.toUserId, `目标责任人${fallbackLabel}`)
  const fromLocation = resolveTypeFieldText(
    row.fromLocationName,
    row.fromLocationId,
    `来源位置${fallbackLabel}`
  )
  const toLocation = resolveTypeFieldText(
    row.toLocationName,
    row.toLocationId,
    `目标位置${fallbackLabel}`
  )

  switch (normalizeOrderType(row.orderType)) {
    case 'INBOUND':
      return `入库至 ${toDept} / ${toLocation}`
    case 'ASSIGN':
      return `${fromDept} / ${fromUser} 领用到 ${toDept} / ${toUser}`
    case 'BORROW':
      return `借用给 ${toUser}，预计归还日 ${displayText(row.expectedReturnDate, '待补充')}`
    case 'RETURN':
      return `归还到 ${toDept} / ${toLocation}`
    case 'TRANSFER':
      return `${fromDept} / ${fromLocation} 调拨到 ${toDept} / ${toLocation}`
    case 'DISPOSAL':
      return `报废原因：${displayText(
        row.disposalReason,
        '待补充'
      )} / 处置金额：${displayText(row.disposalAmount, '待补充')}`
    default:
      return `${fromDept} -> ${toDept}`
  }
}

const ORDER_TYPE_META: Record<string, Partial<OrderPageMeta>> = {
  INBOUND: {
    pageTitle: '新建入库单',
    pageDescription: '把入库目标、来源信息和资产明细一次录稳，完成后资产会进入在库流程。',
    listScopeTip: '当前聚焦入库单，完成后资产会进入在库。',
    listEmptyDescription: '还没有入库单，先创建一张入库单，把资产收进来。',
    statusTags: [
      { text: '入库流转', type: 'primary', effect: 'light' },
      { text: '在库待分配', type: 'info', effect: 'plain' }
    ]
  },
  ASSIGN: {
    pageTitle: '新建领用单',
    pageDescription: '把领用来源、目标责任人和资产明细一次录稳，完成后资产会流向指定使用人。',
    listScopeTip: '当前聚焦领用单，完成后资产会交到指定使用人手上。',
    listEmptyDescription: '还没有领用单，先创建一张领用单，把资产交付出去。',
    statusTags: [
      { text: '领用流转', type: 'success', effect: 'light' },
      { text: '待审批', type: 'warning', effect: 'plain' }
    ]
  },
  BORROW: {
    pageTitle: '新建借用单',
    pageDescription: '把借用责任人、预计归还日和资产明细一次录稳，完成后进入借用流转。',
    listScopeTip: '当前聚焦借用单，完成后会按照预计归还日继续流转。',
    listEmptyDescription: '还没有借用单，先创建一张借用单，把借用流程跑起来。',
    statusTags: [
      { text: '借用流转', type: 'warning', effect: 'light' },
      { text: '待归还', type: 'info', effect: 'plain' }
    ]
  },
  RETURN: {
    pageTitle: '新建归还单',
    pageDescription: '把归还目标、业务时间和资产明细一次录稳，完成后资产回到归还后的状态。',
    listScopeTip: '当前聚焦归还单，完成后资产会回到归还后的状态。',
    listEmptyDescription: '还没有归还单，先创建一张归还单，把回收流程跑起来。',
    statusTags: [
      { text: '归还流转', type: 'primary', effect: 'light' },
      { text: '回收中', type: 'info', effect: 'plain' }
    ]
  },
  TRANSFER: {
    pageTitle: '新建调拨单',
    pageDescription: '把调拨来源、目标位置和资产明细一次录稳，完成后资产会转移到新归属。',
    listScopeTip: '当前聚焦调拨单，完成后资产会转移到新归属。',
    listEmptyDescription: '还没有调拨单，先创建一张调拨单，把流转方向跑通。',
    statusTags: [
      { text: '调拨流转', type: 'success', effect: 'light' },
      { text: '待执行', type: 'warning', effect: 'plain' }
    ]
  },
  DISPOSAL: {
    pageTitle: '新建报废单',
    pageDescription: '先补齐报废原因、处置金额和资产明细，再把整张单据提交到正式流程。',
    listScopeTip: '当前聚焦报废单，完成后会把资产落到已报废。',
    listEmptyDescription: '还没有报废单，先创建一张报废单，把处置流程跑起来。',
    statusTags: [
      { text: '处置流转', type: 'danger', effect: 'light' },
      { text: '已报废', type: 'warning', effect: 'plain' }
    ]
  }
}

const ORDER_TYPE_VIEW_CONFIGS: Record<string, OrderTypeViewConfig> = {
  DISPOSAL: {
    orderType: 'DISPOSAL',
    showTargetFields: false,
    showReturnAfterStatus: false,
    amountLabel: '处置金额',
    amountProp: 'disposalAmount',
    amountFieldType: 'number',
    remarkLabel: '报废原因',
    remarkProp: 'disposalReason',
    requiresDisposalFields: true,
    afterStatusStrategy: 'DISPOSED',
    sectionTitle: '基础信息',
    sectionSubtitle: '先补齐报废原因、处置金额和发起信息，再补资产明细。',
    detailSectionTitle: '资产明细',
    detailSectionSubtitle: '单据头定义处置结果，明细记录资产状态变化和行级备注。'
  },
  RETURN: {
    orderType: 'RETURN',
    showTargetFields: true,
    showReturnAfterStatus: true,
    amountLabel: '预计归还日',
    amountProp: 'expectedReturnDate',
    amountFieldType: 'date',
    remarkLabel: '备注',
    remarkProp: 'remark',
    requiresDisposalFields: false,
    afterStatusStrategy: 'RETURN_AFTER',
    sectionTitle: '基础信息',
    sectionSubtitle: '先把单据类型、业务时间和发起信息录稳，再补资产明细。',
    detailSectionTitle: '资产明细',
    detailSectionSubtitle: '单据头定义流转方向，明细记录每个资产的状态变化和行级备注。'
  },
  DEFAULT: {
    orderType: 'DEFAULT',
    showTargetFields: true,
    showReturnAfterStatus: false,
    amountLabel: '预计归还日',
    amountProp: 'expectedReturnDate',
    amountFieldType: 'date',
    remarkLabel: '备注',
    remarkProp: 'remark',
    requiresDisposalFields: false,
    afterStatusStrategy: 'IN_USE',
    sectionTitle: '基础信息',
    sectionSubtitle: '先把单据类型、业务时间和发起信息录稳，再补资产明细。',
    detailSectionTitle: '资产明细',
    detailSectionSubtitle: '单据头定义流转方向，明细记录每个资产的状态变化和行级备注。'
  }
}

export const buildOrderPageMeta = (orderType?: string, dictOptions: any[] = []): OrderPageMeta => {
  const normalizedType = normalizeOrderType(orderType)
  const customMeta = normalizedType ? ORDER_TYPE_META[normalizedType] : undefined
  const orderTypeLabel = resolveOrderTypeLabel(normalizedType, dictOptions)
  const statusTags = [
    ...(customMeta?.statusTags || DEFAULT_META.statusTags),
    { text: orderTypeLabel, type: 'info', effect: 'plain' }
  ]

  return {
    orderType: normalizedType || 'INBOUND',
    orderTypeLabel,
    ...DEFAULT_META,
    ...(customMeta || {}),
    statusTags
  }
}

export const buildOrderTypeViewConfig = (orderType?: string): OrderTypeViewConfig => {
  const normalizedType = normalizeOrderType(orderType)
  return ORDER_TYPE_VIEW_CONFIGS[normalizedType] || ORDER_TYPE_VIEW_CONFIGS.DEFAULT
}

export const buildOrderTypeScopeSummary = (row: OrderPageContextRow = {}) => buildScopeSummary(row)
