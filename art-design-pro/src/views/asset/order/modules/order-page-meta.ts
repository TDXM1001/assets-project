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

const ORDER_TYPE_META: Record<string, Partial<OrderPageMeta>> = {
  DISPOSAL: {
    pageTitle: '新建报废单',
    pageDescription: '先补齐报废原因、处置金额和资产明细，再把整张单据提交到正式流程。',
    listScopeTip: '当前聚焦报废单，完成后会把资产落到已报废。',
    listEmptyDescription: '还没有报废单，先创建一张报废单，把处置流程跑起来。'
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

export const buildOrderPageMeta = (orderType?: string, dictOptions: any[] = []): OrderPageMeta => {
  const normalizedType = normalizeOrderType(orderType)
  const customMeta = normalizedType ? ORDER_TYPE_META[normalizedType] : undefined
  const orderTypeLabel = resolveOrderTypeLabel(normalizedType, dictOptions)
  const statusTags = [
    ...DEFAULT_META.statusTags,
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
