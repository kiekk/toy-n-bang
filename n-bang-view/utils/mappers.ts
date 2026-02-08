/**
 * API 응답 타입 ↔ Frontend 타입 변환 유틸리티
 */

import type {
  GatheringResponse,
  GatheringCreateRequest,
  ParticipantResponse,
  RoundResponse,
  RoundCreateRequest,
  RoundUpdateRequest,
  ExclusionResponse,
  ExclusionRequest,
} from '../services/api';
import type { Gathering, Participant, SettlementRound, Exclusion, GatheringType } from '../types';

// Color palette for gatherings
const GATHERING_COLORS = [
  'bg-indigo-500',
  'bg-rose-500',
  'bg-amber-500',
  'bg-emerald-500',
  'bg-violet-500',
  'bg-sky-500',
  'bg-fuchsia-500',
  'bg-orange-500',
  'bg-teal-500',
];

// ============================================
// Gathering Mappers
// ============================================

export const mapGatheringFromApi = (response: GatheringResponse): Gathering => {
  return {
    id: String(response.id),
    name: response.name,
    type: (response.type as GatheringType) || 'OTHER',
    startDate: new Date(response.startDate).getTime(),
    endDate: new Date(response.endDate).getTime(),
    createdAt: Date.now(), // API doesn't return this
    participants: response.participants.map(mapParticipantFromApi),
    rounds: response.rounds.map(mapRoundFromApi),
    color: GATHERING_COLORS[response.id % GATHERING_COLORS.length],
  };
};

export const mapGatheringToCreateRequest = (
  name: string,
  type: GatheringType,
  startDate: string,
  endDate: string,
  participantNames?: string[]
): GatheringCreateRequest => {
  return {
    name,
    type,
    startDate,
    endDate,
    participantNames,
  };
};

// ============================================
// Participant Mappers
// ============================================

export const mapParticipantFromApi = (response: ParticipantResponse): Participant => {
  return {
    id: String(response.id),
    name: response.name,
    // bankName and accountNumber are not in API response
    // They will be stored locally in directory
  };
};

// ============================================
// Round Mappers
// ============================================

export const mapRoundFromApi = (response: RoundResponse): SettlementRound => {
  return {
    id: String(response.id),
    title: response.title,
    amount: response.amount,
    payerId: String(response.payerId),
    excluded: response.exclusions.map(mapExclusionFromApi),
    receiptImage: response.receiptImageUrl || undefined,
  };
};

export const mapRoundToCreateRequest = (
  title: string,
  amount: number,
  payerId: number,
  exclusions?: Exclusion[]
): RoundCreateRequest => {
  return {
    title,
    amount,
    payerId,
    exclusions: exclusions?.map(mapExclusionToRequest),
  };
};

export const mapRoundToUpdateRequest = (
  title: string,
  amount: number,
  payerId: number,
  exclusions?: Exclusion[]
): RoundUpdateRequest => {
  return {
    title,
    amount,
    payerId,
    exclusions: exclusions?.map(mapExclusionToRequest),
  };
};

// ============================================
// Exclusion Mappers
// ============================================

export const mapExclusionFromApi = (response: ExclusionResponse): Exclusion => {
  return {
    participantId: String(response.participantId),
    reason: response.reason,
  };
};

export const mapExclusionToRequest = (exclusion: Exclusion): ExclusionRequest => {
  return {
    participantId: Number(exclusion.participantId),
    reason: exclusion.reason,
  };
};
