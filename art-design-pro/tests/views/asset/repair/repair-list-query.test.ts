import { describe, expect, it } from 'vitest'
import {
  buildRepairListRestoreQuery,
  resolveRepairListRestoreState
} from '@/views/asset/repair/modules/repair-list-query'

describe('repair-list-query', () => {
  it('resolves list restore state from route query', () => {
    const state = resolveRepairListRestoreState({
      repairStatus: 'draft',
      repairNo: ' REPAIR-001 ',
      assetCode: ' AC-001 ',
      vendorName: ' 维修供应商 ',
      resultType: 'SUGGEST_DISPOSAL'
    })

    expect(state).toEqual({
      repairStatus: 'DRAFT',
      repairNo: 'REPAIR-001',
      assetCode: 'AC-001',
      vendorName: '维修供应商',
      resultType: 'SUGGEST_DISPOSAL'
    })
  })

  it('builds a compact restore query for list return', () => {
    const query = buildRepairListRestoreQuery({
      repairStatus: 'ALL',
      repairNo: 'REPAIR-001',
      assetCode: 'AC-001',
      vendorName: '维修供应商',
      resultType: ''
    })

    expect(query).toEqual({
      repairNo: 'REPAIR-001',
      assetCode: 'AC-001',
      vendorName: '维修供应商'
    })
  })
})
