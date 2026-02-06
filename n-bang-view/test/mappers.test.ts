import { describe, it, expect } from 'vitest'
import {
  mapGatheringFromApi,
  mapGatheringToCreateRequest,
  mapParticipantFromApi,
  mapRoundFromApi,
  mapRoundToCreateRequest,
  mapRoundToUpdateRequest,
  mapExclusionFromApi,
  mapExclusionToRequest,
} from '../utils/mappers'
import type {
  GatheringResponse,
  ParticipantResponse,
  RoundResponse,
  ExclusionResponse,
} from '../services/api/types'

describe('mappers', () => {
  describe('mapGatheringFromApi', () => {
    it('API 응답을 프론트엔드 타입으로 변환해야 함', () => {
      const apiResponse: GatheringResponse = {
        id: 1,
        name: '제주도 여행',
        startDate: '2024-01-15',
        endDate: '2024-01-17',
        participants: [
          { id: 1, name: '철수' },
          { id: 2, name: '영희' },
        ],
        rounds: [
          {
            id: 1,
            title: '1차 고기집',
            amount: 50000,
            payerId: 1,
            payerName: '철수',
            receiptImageUrl: null,
            exclusions: [],
          },
        ],
      }

      const result = mapGatheringFromApi(apiResponse)

      expect(result.id).toBe('1')
      expect(result.name).toBe('제주도 여행')
      expect(result.startDate).toBeTypeOf('number')
      expect(result.endDate).toBeTypeOf('number')
      expect(result.participants).toHaveLength(2)
      expect(result.participants[0].id).toBe('1')
      expect(result.participants[0].name).toBe('철수')
      expect(result.rounds).toHaveLength(1)
      expect(result.rounds[0].id).toBe('1')
      expect(result.rounds[0].payerId).toBe('1')
      expect(result.color).toBeDefined()
    })

    it('날짜를 timestamp로 변환해야 함', () => {
      const apiResponse: GatheringResponse = {
        id: 1,
        name: '테스트',
        startDate: '2024-01-15',
        endDate: '2024-01-17',
        participants: [],
        rounds: [],
      }

      const result = mapGatheringFromApi(apiResponse)

      const startDate = new Date(result.startDate)
      const endDate = new Date(result.endDate)

      expect(startDate.getFullYear()).toBe(2024)
      expect(startDate.getMonth()).toBe(0) // January
      expect(startDate.getDate()).toBe(15)

      expect(endDate.getDate()).toBe(17)
    })
  })

  describe('mapGatheringToCreateRequest', () => {
    it('생성 요청 객체를 만들어야 함', () => {
      const result = mapGatheringToCreateRequest(
        '송년회',
        '2024-12-25',
        '2024-12-25',
        ['철수', '영희']
      )

      expect(result.name).toBe('송년회')
      expect(result.startDate).toBe('2024-12-25')
      expect(result.endDate).toBe('2024-12-25')
      expect(result.participantNames).toEqual(['철수', '영희'])
    })

    it('참여자 없이도 요청 객체를 만들 수 있어야 함', () => {
      const result = mapGatheringToCreateRequest('테스트', '2024-01-01', '2024-01-02')

      expect(result.name).toBe('테스트')
      expect(result.participantNames).toBeUndefined()
    })
  })

  describe('mapParticipantFromApi', () => {
    it('참여자 ID를 문자열로 변환해야 함', () => {
      const apiResponse: ParticipantResponse = {
        id: 123,
        name: '민수',
      }

      const result = mapParticipantFromApi(apiResponse)

      expect(result.id).toBe('123')
      expect(result.name).toBe('민수')
    })
  })

  describe('mapRoundFromApi', () => {
    it('라운드 데이터를 변환해야 함', () => {
      const apiResponse: RoundResponse = {
        id: 1,
        title: '2차 술집',
        amount: 80000,
        payerId: 2,
        payerName: '영희',
        receiptImageUrl: 'https://example.com/receipt.jpg',
        exclusions: [
          { id: 1, participantId: 3, participantName: '민수', reason: '먼저 감' },
        ],
      }

      const result = mapRoundFromApi(apiResponse)

      expect(result.id).toBe('1')
      expect(result.title).toBe('2차 술집')
      expect(result.amount).toBe(80000)
      expect(result.payerId).toBe('2')
      expect(result.receiptImage).toBe('https://example.com/receipt.jpg')
      expect(result.excluded).toHaveLength(1)
      expect(result.excluded[0].participantId).toBe('3')
      expect(result.excluded[0].reason).toBe('먼저 감')
    })

    it('영수증 이미지가 없으면 undefined여야 함', () => {
      const apiResponse: RoundResponse = {
        id: 1,
        title: '테스트',
        amount: 10000,
        payerId: 1,
        payerName: '철수',
        receiptImageUrl: null,
        exclusions: [],
      }

      const result = mapRoundFromApi(apiResponse)

      expect(result.receiptImage).toBeUndefined()
    })
  })

  describe('mapRoundToCreateRequest', () => {
    it('라운드 생성 요청을 만들어야 함', () => {
      const exclusions = [
        { participantId: '3', reason: '늦게 옴' },
      ]

      const result = mapRoundToCreateRequest('1차', 30000, 1, exclusions)

      expect(result.title).toBe('1차')
      expect(result.amount).toBe(30000)
      expect(result.payerId).toBe(1)
      expect(result.exclusions).toHaveLength(1)
      expect(result.exclusions![0].participantId).toBe(3)
      expect(result.exclusions![0].reason).toBe('늦게 옴')
    })
  })

  describe('mapRoundToUpdateRequest', () => {
    it('라운드 수정 요청을 만들어야 함', () => {
      const result = mapRoundToUpdateRequest('수정된 제목', 50000, 2, [])

      expect(result.title).toBe('수정된 제목')
      expect(result.amount).toBe(50000)
      expect(result.payerId).toBe(2)
      expect(result.exclusions).toEqual([])
    })
  })

  describe('mapExclusionFromApi', () => {
    it('제외 데이터를 변환해야 함', () => {
      const apiResponse: ExclusionResponse = {
        id: 1,
        participantId: 5,
        participantName: '지수',
        reason: '술 안마심',
      }

      const result = mapExclusionFromApi(apiResponse)

      expect(result.participantId).toBe('5')
      expect(result.reason).toBe('술 안마심')
    })
  })

  describe('mapExclusionToRequest', () => {
    it('제외 요청 데이터를 변환해야 함', () => {
      const exclusion = {
        participantId: '10',
        reason: '2차에서 합류',
      }

      const result = mapExclusionToRequest(exclusion)

      expect(result.participantId).toBe(10)
      expect(result.reason).toBe('2차에서 합류')
    })
  })
})
