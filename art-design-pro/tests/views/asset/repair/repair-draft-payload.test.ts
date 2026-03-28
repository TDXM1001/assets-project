import { describe, expect, it } from 'vitest'
import {
  normalizeRepairDraftItems,
  resolveRepairDraftState,
  buildRepairSubmitPayload,
  buildRepairDraftCachePayload,
  buildRepairHeaderStateFromItems
} from '@/views/asset/repair/modules/repair-draft-payload'

describe('repair-draft-payload', () => {
  it('restores stable row keys when draft items do not store one', () => {
    const items = normalizeRepairDraftItems([
      { repairItemId: 11, assetId: 920006, assetCode: 'QA-ASSET-1' },
      { assetId: 920007, assetCode: 'QA-ASSET-2' }
    ])

    expect(items[0].rowKey).toBe('draft-11-0')
    expect(items[1].rowKey).toBe('draft-920007-1')
  })

  it('keeps an existing row key when one is already present', () => {
    const items = normalizeRepairDraftItems([
      { rowKey: 'existing-key', assetId: 920006, assetCode: 'QA-ASSET-1' }
    ])

    expect(items[0].rowKey).toBe('existing-key')
  })

  it('restores draft items from itemList payloads too', () => {
    const state = resolveRepairDraftState({
      repairNo: 'DRAFT-001',
      itemList: [{ assetId: 920006, assetCode: 'QA-ASSET-1', faultDesc: 'demo' }]
    })

    expect(state.repairItems).toHaveLength(1)
    expect(state.repairItems[0].rowKey).toBe('draft-920006-0')
    expect(state.assetId).toBe(920006)
    expect(state.assetCode).toBe('QA-ASSET-1')
    expect(state.faultDesc).toBe('demo')
  })

  it('prefers repairItems over legacy itemList when both exist', () => {
    const state = resolveRepairDraftState({
      repairItems: [{ assetId: 920101, assetCode: 'QA-RI-101', faultDesc: 'from-repair-items' }],
      itemList: [{ assetId: 920202, assetCode: 'QA-IL-202', faultDesc: 'from-item-list' }]
    })

    expect(state.repairItems).toHaveLength(1)
    expect(state.repairItems[0].assetId).toBe(920101)
    expect(state.assetCode).toBe('QA-RI-101')
    expect(state.faultDesc).toBe('from-repair-items')
  })

  it('builds unified header fields from primary item for submit payload', () => {
    const payload = buildRepairSubmitPayload({
      faultDesc: 'outer fault',
      repairItems: [
        {
          rowKey: 'keep-me',
          assetId: 930001,
          assetCode: 'QA-SUBMIT-1',
          assetName: 'Submit Asset',
          beforeStatus: 'IDLE',
          afterStatus: 'IN_USE',
          faultDesc: 'inner fault',
          currentUserId: 3001,
          currentDeptId: 4001,
          currentLocationId: 5001
        }
      ]
    })

    expect(payload.assetId).toBe(930001)
    expect(payload.assetCode).toBe('QA-SUBMIT-1')
    expect(payload.assetName).toBe('Submit Asset')
    expect(payload.beforeStatus).toBe('IDLE')
    expect(payload.afterStatus).toBe('IN_USE')
    expect(payload.faultDesc).toBe('inner fault')
    expect(payload.itemList[0].rowKey).toBeUndefined()
  })

  it('keeps header snapshot alignment in draft cache payload', () => {
    const payload = buildRepairDraftCachePayload({
      faultDesc: 'cache-desc',
      repairItems: [
        { rowKey: 'draft-row', assetId: 940001, assetCode: 'QA-CACHE-1', faultDesc: 'line fault' }
      ]
    })

    expect(payload.assetId).toBe(940001)
    expect(payload.assetCode).toBe('QA-CACHE-1')
    expect(payload.faultDesc).toBe('line fault')
    expect(payload.repairItems).toHaveLength(1)
    expect(payload.repairItems[0].rowKey).toBeUndefined()
  })

  it('falls back to provided fault description when item is missing one', () => {
    const header = buildRepairHeaderStateFromItems(
      [{ assetId: 950001, assetCode: 'QA-HDR-1' }],
      'fallback fault'
    )

    expect(header.assetId).toBe(950001)
    expect(header.faultDesc).toBe('fallback fault')
  })
})
