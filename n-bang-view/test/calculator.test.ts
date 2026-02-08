import { describe, it, expect } from 'vitest'
import { calculateBalances, resolveDebts } from '../utils/calculator'
import type { Participant, SettlementRound } from '../types'

describe('calculateBalances', () => {
  it('참여자별 지불/부담 금액을 계산해야 함', () => {
    const participants: Participant[] = [
      { id: '1', name: '철수' },
      { id: '2', name: '영희' },
      { id: '3', name: '민수' },
    ]

    const rounds: SettlementRound[] = [
      { id: 'r1', title: '1차', amount: 30000, payerId: '1', excluded: [] },
    ]

    const balances = calculateBalances(participants, rounds)

    // 철수가 30000원 지불, 모두 10000원씩 부담
    expect(balances).toHaveLength(3)

    const 철수 = balances.find(b => b.name === '철수')!
    expect(철수.totalPaid).toBe(30000)
    expect(철수.totalOwed).toBe(10000)
    expect(철수.netBalance).toBe(20000) // 받아야 할 돈

    const 영희 = balances.find(b => b.name === '영희')!
    expect(영희.totalPaid).toBe(0)
    expect(영희.totalOwed).toBe(10000)
    expect(영희.netBalance).toBe(-10000) // 줘야 할 돈
  })

  it('제외된 참여자는 부담금에서 제외해야 함', () => {
    const participants: Participant[] = [
      { id: '1', name: '철수' },
      { id: '2', name: '영희' },
      { id: '3', name: '민수' },
    ]

    const rounds: SettlementRound[] = [
      {
        id: 'r1',
        title: '1차',
        amount: 20000,
        payerId: '1',
        excluded: [{ participantId: '3', reason: '미참여' }]
      },
    ]

    const balances = calculateBalances(participants, rounds)

    // 철수가 20000원 지불, 철수&영희만 10000원씩 부담
    const 철수 = balances.find(b => b.name === '철수')!
    expect(철수.totalOwed).toBe(10000)
    expect(철수.netBalance).toBe(10000)

    const 영희 = balances.find(b => b.name === '영희')!
    expect(영희.totalOwed).toBe(10000)
    expect(영희.netBalance).toBe(-10000)

    const 민수 = balances.find(b => b.name === '민수')!
    expect(민수.totalOwed).toBe(0)
    expect(민수.netBalance).toBe(0)
  })
})

describe('resolveDebts', () => {
  it('송금 내역을 계산해야 함', () => {
    const participants: Participant[] = [
      { id: '1', name: '철수' },
      { id: '2', name: '영희' },
      { id: '3', name: '민수' },
    ]

    const rounds: SettlementRound[] = [
      { id: 'r1', title: '1차', amount: 30000, payerId: '1', excluded: [] },
    ]

    const balances = calculateBalances(participants, rounds)
    const debts = resolveDebts(balances)

    console.log('Balances:', balances)
    console.log('Debts:', debts)

    // 영희와 민수가 철수에게 각각 10000원 보내야 함
    expect(debts).toHaveLength(2)

    const 영희송금 = debts.find(d => d.from === '영희')
    expect(영희송금).toBeDefined()
    expect(영희송금!.to).toBe('철수')
    expect(영희송금!.amount).toBe(10000)

    const 민수송금 = debts.find(d => d.from === '민수')
    expect(민수송금).toBeDefined()
    expect(민수송금!.to).toBe('철수')
    expect(민수송금!.amount).toBe(10000)
  })

  it('여러 라운드에서 복잡한 송금 내역을 계산해야 함', () => {
    const participants: Participant[] = [
      { id: '1', name: '철수' },
      { id: '2', name: '영희' },
    ]

    const rounds: SettlementRound[] = [
      { id: 'r1', title: '1차', amount: 20000, payerId: '1', excluded: [] },
      { id: 'r2', title: '2차', amount: 10000, payerId: '2', excluded: [] },
    ]

    const balances = calculateBalances(participants, rounds)
    const debts = resolveDebts(balances)

    console.log('Balances:', balances)
    console.log('Debts:', debts)

    // 총 30000원, 각자 15000원 부담
    // 철수: 20000 지불, 15000 부담 → +5000
    // 영희: 10000 지불, 15000 부담 → -5000
    // 영희가 철수에게 5000원 보내야 함
    expect(debts).toHaveLength(1)
    expect(debts[0].from).toBe('영희')
    expect(debts[0].to).toBe('철수')
    expect(debts[0].amount).toBe(5000)
  })

  it('모두 균등하게 지불한 경우 송금 내역이 없어야 함', () => {
    const participants: Participant[] = [
      { id: '1', name: '철수' },
      { id: '2', name: '영희' },
    ]

    const rounds: SettlementRound[] = [
      { id: 'r1', title: '1차', amount: 10000, payerId: '1', excluded: [] },
      { id: 'r2', title: '2차', amount: 10000, payerId: '2', excluded: [] },
    ]

    const balances = calculateBalances(participants, rounds)
    const debts = resolveDebts(balances)

    console.log('Balances:', balances)
    console.log('Debts:', debts)

    // 각자 10000원씩 지불, 각자 10000원 부담 → 모두 0
    expect(debts).toHaveLength(0)
  })
})
