import { describe, expect, it } from 'vitest'
import {
  formatRepairAssetCode,
  formatRepairAssetName,
  createRepairItemFromAsset,
  resolveRepairFormState,
  resolveRepairDetailRecord,
  resolveRepairItems,
  serializeRepairItems
} from '@/views/asset/repair/modules/repair-item-normalize'

describe('repair-item-normalize', () => {
  it('normalizes itemList items with stable row keys', () => {
    const items = resolveRepairItems({
      itemList: [
        { repairItemId: 11, assetId: 920006, assetCode: 'QA-ASSET-1' },
        { rowKey: 'keep-me', assetId: 920007, assetCode: 'QA-ASSET-2' }
      ]
    })

    expect(items[0].rowKey).toBe('repair-11-0')
    expect(items[1].rowKey).toBe('keep-me')
  })

  it('supports legacy repairItems payloads', () => {
    const items = resolveRepairItems({
      repairItems: [{ assetId: 920009, assetCode: 'QA-ASSET-3' }]
    })

    expect(items).toHaveLength(1)
    expect(items[0].rowKey).toBe('repair-920009-0')
  })

  it('falls back to a single asset snapshot when no detail list exists', () => {
    const items = resolveRepairItems({
      assetId: 920010,
      assetCode: 'QA-ASSET-4',
      assetName: 'Demo Asset'
    })

    expect(items).toHaveLength(1)
    expect(items[0].rowKey).toBe('repair-920010-0')
    expect(items[0].assetName).toBe('Demo Asset')
  })

  it('keeps default fallback behavior when strict mode is not enabled', () => {
    const items = resolveRepairItems({
      assetId: 920099,
      assetCode: 'QA-ASSET-DEFAULT',
      itemCount: 3
    })

    expect(items).toHaveLength(1)
    expect(items[0].assetCode).toBe('QA-ASSET-DEFAULT')
  })

  it('returns empty in strict list mode when only header snapshot exists for multi items', () => {
    const items = resolveRepairItems(
      {
        assetId: 920100,
        assetCode: 'QA-ASSET-STRICT',
        itemCount: 3
      },
      { strictListMode: true }
    )

    expect(items).toHaveLength(0)
  })

  it('keeps current asset context fields from the source payload', () => {
    const items = resolveRepairItems({
      itemList: [
        {
          assetId: 920011,
          assetCode: 'QA-ASSET-5',
          currentUserId: 1001,
          currentDeptId: 2001,
          currentLocationId: 3001
        }
      ]
    })

    expect(items[0].currentUserId).toBe(1001)
    expect(items[0].currentDeptId).toBe(2001)
    expect(items[0].currentLocationId).toBe(3001)
  })

  it('creates a new repair item snapshot from an asset candidate', () => {
    const item = createRepairItemFromAsset({
      assetId: 920012,
      assetCode: 'QA-ASSET-6',
      assetName: 'Demo Asset 6',
      assetStatus: 'IN_USE',
      currentUserId: 1002,
      useOrgDeptId: 2002,
      currentLocationId: 3002
    })

    expect(item.rowKey).toBe('repair-920012-0')
    expect(item.beforeStatus).toBe('IN_USE')
    expect(item.currentDeptId).toBe(2002)
    expect(item.currentLocationId).toBe(3002)
  })

  it('builds a single header snapshot from the primary repair item', () => {
    const state = resolveRepairFormState({
      itemList: [
        {
          assetId: 920013,
          assetCode: 'QA-ASSET-7',
          assetName: 'Demo Asset 7',
          beforeStatus: 'IDLE',
          afterStatus: 'IN_USE',
          faultDesc: 'primary fault'
        }
      ]
    })

    expect(state.repairItems).toHaveLength(1)
    expect(state.assetId).toBe(920013)
    expect(state.assetCode).toBe('QA-ASSET-7')
    expect(state.beforeStatus).toBe('IDLE')
    expect(state.afterStatus).toBe('IN_USE')
    expect(state.faultDesc).toBe('primary fault')
  })

  it('serializes repair items without row keys for payloads', () => {
    const items = serializeRepairItems([
      { rowKey: 'keep-out', assetId: 920014, assetCode: 'QA-ASSET-8' }
    ])

    expect(items[0].rowKey).toBeUndefined()
    expect(items[0].assetCode).toBe('QA-ASSET-8')
  })

  it('normalizes repair detail records for list rendering', () => {
    const record = resolveRepairDetailRecord({
      assetId: 920015,
      assetCode: 'QA-ASSET-9',
      assetName: 'Demo Asset 9',
      itemList: [{ assetId: 920016, assetCode: 'QA-ASSET-10', assetName: 'Child Asset' }]
    })

    expect(record.itemList).toHaveLength(1)
    expect(record.itemCount).toBe(1)
    expect(record.assetId).toBe(920015)
    expect(record.assetCode).toBe('QA-ASSET-9')
    expect(record.assetName).toBe('Demo Asset 9')
  })

  it('formats asset summary text from item lists and legacy headers', () => {
    expect(
      formatRepairAssetCode({
        itemList: [{ assetCode: 'QA-ASSET-11' }, { assetCode: 'QA-ASSET-12' }]
      })
    ).toBe('QA-ASSET-11 等 2 项')

    expect(
      formatRepairAssetName({
        assetName: 'Legacy Asset',
        itemCount: 3
      })
    ).toBe('Legacy Asset 等 3 项')
  })
})
