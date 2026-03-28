import { describe, expect, it } from 'vitest'
import {
  buildOrderPageMeta,
  buildOrderTypeScopeSummary,
  buildOrderTypeViewConfig
} from '@/views/asset/order/modules/order-page-meta'

describe('order page meta helpers', () => {
  it('builds type-specific meta for disposal orders', () => {
    const meta = buildOrderPageMeta('DISPOSAL', [{ value: 'DISPOSAL', label: '报废单' }])

    expect(meta.orderType).toBe('DISPOSAL')
    expect(meta.orderTypeLabel).toBe('报废单')
    expect(meta.pageTitle).toBe('新建报废单')
    expect(meta.listScopeTip).toContain('报废单')
    expect(meta.statusTags.map((tag) => tag.text)).toEqual(['处置流转', '已报废', '报废单'])
  })

  it('returns reusable type view config by order type', () => {
    const config = buildOrderTypeViewConfig('ASSIGN')

    expect(config.orderType).toBe('DEFAULT')
    expect(config.showTargetFields).toBe(true)
    expect(config.afterStatusStrategy).toBe('IN_USE')
    expect(config.detailSectionTitle).toBe('资产明细')
  })

  it('builds readable scope summaries from shared row fields', () => {
    expect(
      buildOrderTypeScopeSummary({
        orderType: 'INBOUND',
        toDeptName: '仓储部',
        toLocationName: 'A01'
      })
    ).toBe('入库至 仓储部 / A01')

    expect(
      buildOrderTypeScopeSummary({
        orderType: 'ASSIGN',
        fromDeptName: '行政部',
        fromUserName: 'Alice',
        toDeptName: '业务部',
        toUserName: 'Bob'
      })
    ).toBe('行政部 / Alice 领用到 业务部 / Bob')

    expect(
      buildOrderTypeScopeSummary({
        orderType: 'DISPOSAL',
        disposalReason: '设备老旧',
        disposalAmount: 1200
      })
    ).toBe('报废原因：设备老旧 / 处置金额：1200')
  })
})
