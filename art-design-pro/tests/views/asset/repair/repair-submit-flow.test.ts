import { describe, expect, it } from 'vitest'
import { isSuccessfulRepairSubmit } from '@/views/asset/repair/modules/repair-submit-flow'

describe('repair-submit-flow', () => {
  it('treats submit results without repairId as successful when the call returned a payload', () => {
    expect(isSuccessfulRepairSubmit({ repairNo: 'RP20260326134012198' })).toBe(true)
  })

  it('treats empty results as unsuccessful', () => {
    expect(isSuccessfulRepairSubmit(undefined)).toBe(false)
    expect(isSuccessfulRepairSubmit(null)).toBe(false)
  })
})
