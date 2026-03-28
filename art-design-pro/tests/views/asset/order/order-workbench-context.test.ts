import { describe, expect, it } from 'vitest'
import { resolveOrderWorkbenchPageContext } from '@/views/asset/order/modules/order-workbench-context'

describe('resolveOrderWorkbenchPageContext', () => {
  it('keeps bridge source business fields when building create-page context', () => {
    const context = resolveOrderWorkbenchPageContext({
      orderTypeQuery: 'disposal',
      bridgeSourceQuery: 'repair',
      bridgeKeyQuery: 'bridge-001',
      bridgeDataQuery: JSON.stringify({
        sourceBizType: 'ASSET_REPAIR',
        sourceBizId: 88,
        repairId: 88
      })
    })

    expect(context.orderType).toBe('DISPOSAL')
    expect(context.bridgeSource).toBe('repair')
    expect(context.bridgeKey).toBe('bridge-001')
    expect(context.sourceBizType).toBe('ASSET_REPAIR')
    expect(context.sourceBizId).toBe('88')
    expect(context.repairId).toBe('88')
  })
})
