/**
 * API 공통 타입 정의
 * Backend DTO와 1:1 매핑
 */

// ============================================
// API Response Wrapper
// ============================================

export type ApiResult = 'SUCCESS' | 'FAIL';

export interface ApiMetadata {
  result: ApiResult;
  errorCode?: string;
  message?: string;
}

export interface ApiResponse<T> {
  meta: ApiMetadata;
  data: T | null;
}

// ============================================
// Auth Types
// ============================================

export interface TokenResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
}

export interface MemberResponse {
  id: number;
  email: string;
  nickname: string;
  profileImage: string | null;
  provider: string;
}

export interface TokenRefreshRequest {
  refreshToken: string;
}

// ============================================
// Gathering Types
// ============================================

export interface GatheringResponse {
  id: number;
  name: string;
  type?: string; // 모임 타입 (travel, dinner, meeting, date, event, hobby, other)
  startDate: string; // ISO date string (YYYY-MM-DD)
  endDate: string;
  participants: ParticipantResponse[];
  rounds: RoundResponse[];
}

export interface GatheringCreateRequest {
  name: string;
  type: string; // 모임 타입
  startDate: string;
  endDate: string;
  participantNames?: string[];
}

export interface GatheringUpdateRequest {
  name: string;
  startDate: string;
  endDate: string;
}

// ============================================
// Participant Types
// ============================================

export interface ParticipantResponse {
  id: number;
  name: string;
}

export interface ParticipantCreateRequest {
  name: string;
}

// ============================================
// Round Types
// ============================================

export interface RoundResponse {
  id: number;
  title: string;
  amount: number;
  payerId: number;
  payerName: string;
  receiptImageUrl: string | null;
  exclusions: ExclusionResponse[];
}

export interface ExclusionResponse {
  id: number;
  participantId: number;
  participantName: string;
  reason: string;
}

export interface RoundCreateRequest {
  title: string;
  amount: number;
  payerId: number;
  exclusions?: ExclusionRequest[];
}

export interface RoundUpdateRequest {
  title: string;
  amount: number;
  payerId: number;
  exclusions?: ExclusionRequest[];
}

export interface ExclusionRequest {
  participantId: number;
  reason: string;
}

// ============================================
// Calculation Types
// ============================================

export interface CalculationResponse {
  gatheringId: number;
  gatheringName: string;
  totalAmount: number;
  balances: UserBalanceResponse[];
  debts: DebtResponse[];
}

export interface UserBalanceResponse {
  participantId: number;
  name: string;
  totalPaid: number;
  totalOwed: number;
  netBalance: number;
}

export interface DebtResponse {
  from: string;
  to: string;
  amount: number;
}
