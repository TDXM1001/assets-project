import { describe, expect, it } from 'vitest'
import {
  buildOrderListRestoreQuery,
  resolveOrderListRestoreState
} from '@/views/asset/order/modules/order-list-query'

describe('order list query helpers', () => {
  it('round-trips list filters and active order type', () => {
    const query = buildOrderListRestoreQuery({
      orderType: 'disposal',
      orderNo: 'OD-001',
      orderStatus: 'DRAFT',
      applyUserName: 'alice',
      applyDeptName: 'ops',
      bizDateRange: ['2026-03-01', '2026-03-28']
    })

    expect(query).toEqual({
      orderNo: 'OD-001',
      orderStatus: 'DRAFT',
      applyUserName: 'alice',
      applyDeptName: 'ops',
      bizDateStart: '2026-03-01',
      bizDateEnd: '2026-03-28',
      orderType: 'DISPOSAL'
    })

    expect(resolveOrderListRestoreState(query)).toEqual({
      orderType: 'DISPOSAL',
      orderNo: 'OD-001',
      orderStatus: 'DRAFT',
      applyUserName: 'alice',
      applyDeptName: 'ops',
      bizDateRange: ['2026-03-01', '2026-03-28'],
      pageNum: 1,
      pageSize: 10
    })
  })

  it('omits empty values and defaults the order type to ALL', () => {
    expect(buildOrderListRestoreQuery()).toEqual({})
    expect(resolveOrderListRestoreState()).toEqual({
      orderType: 'ALL',
      orderNo: '',
      orderStatus: '',
      applyUserName: '',
      applyDeptName: '',
      bizDateRange: [],
      pageNum: 1,
      pageSize: 10
    })
  })
})
