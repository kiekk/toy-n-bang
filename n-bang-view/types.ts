/**
 * Frontend 도메인 타입 정의
 *
 * 이 파일의 타입들은 프론트엔드에서 사용하는 도메인 모델입니다.
 * API 응답 타입(services/api/types.ts)과는 다를 수 있으며,
 * mappers.ts에서 변환됩니다.
 */

// ============================================
// Participant (참여자)
// ============================================

export interface Participant {
  id: string;
  name: string;
  /** 은행명 (로컬 directory에서 관리) */
  bankName?: string;
  /** 계좌번호 (로컬 directory에서 관리) */
  accountNumber?: string;
}

// ============================================
// Exclusion (제외 설정)
// ============================================

export interface Exclusion {
  participantId: string;
  reason: string;
}

// ============================================
// SettlementRound (정산 라운드)
// ============================================

export interface SettlementRound {
  id: string;
  title: string;
  amount: number;
  payerId: string;
  excluded: Exclusion[];
  /** 영수증 이미지 URL */
  receiptImage?: string;
}

// ============================================
// GatheringType (모임 타입)
// ============================================

export type GatheringType = 'travel' | 'dinner' | 'meeting' | 'date' | 'event' | 'hobby' | 'other';

export interface GatheringTypeInfo {
  type: GatheringType;
  label: string;
  icon: string;
}

export const GATHERING_TYPES: GatheringTypeInfo[] = [
  { type: 'travel', label: '여행', icon: '✈️' },
  { type: 'dinner', label: '회식', icon: '🍻' },
  { type: 'meeting', label: '모임', icon: '👥' },
  { type: 'date', label: '데이트', icon: '💕' },
  { type: 'event', label: '경조사', icon: '🎁' },
  { type: 'hobby', label: '취미', icon: '⚽' },
  { type: 'other', label: '기타', icon: '📝' },
];

// ============================================
// Gathering (모임)
// ============================================

export interface Gathering {
  id: string;
  name: string;
  /** 모임 타입 */
  type: GatheringType;
  /** Unix timestamp (milliseconds) */
  startDate: number;
  /** Unix timestamp (milliseconds) */
  endDate: number;
  /** 생성 시각 (프론트엔드 전용) */
  createdAt: number;
  participants: Participant[];
  rounds: SettlementRound[];
  /** Tailwind 색상 클래스 (예: 'bg-indigo-500') */
  color?: string;
}

// ============================================
// Calculation Results (정산 계산 결과)
// ============================================

export interface UserBalance {
  participantId: string;
  name: string;
  /** 총 지불 금액 */
  totalPaid: number;
  /** 총 부담해야 할 금액 */
  totalOwed: number;
  /** 순 잔액 (양수: 받을 돈, 음수: 줄 돈) */
  netBalance: number;
}

export interface DebtLink {
  /** 보내는 사람 이름 */
  from: string;
  /** 받는 사람 이름 */
  to: string;
  /** 송금 금액 */
  amount: number;
  /** 받는 사람 ID (계좌 정보 조회용) */
  toParticipantId: string;
}

// ============================================
// Directory (계좌 정보 저장소)
// ============================================

export interface DirectoryEntry {
  bankName?: string;
  accountNumber?: string;
}

export type Directory = Record<string, DirectoryEntry>;
