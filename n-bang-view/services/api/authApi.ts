import apiClient, { tokenStorage } from './client';
import type { MemberResponse, TokenRefreshRequest, TokenResponse } from './types';

const AUTH_BASE = '/api/v1/auth';

export const authApi = {
  /**
   * Access Token 갱신
   */
  refreshToken: async (request: TokenRefreshRequest): Promise<TokenResponse> => {
    const response = await apiClient.post<TokenResponse>(`${AUTH_BASE}/refresh`, request);
    return response.data;
  },

  /**
   * 현재 로그인한 사용자 정보 조회
   */
  getMe: async (): Promise<MemberResponse> => {
    const response = await apiClient.get<MemberResponse>(`${AUTH_BASE}/me`);
    return response.data;
  },

  /**
   * OAuth2 로그인 URL로 리다이렉트
   */
  loginWithProvider: (provider: 'google' | 'kakao'): void => {
    const baseUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080';
    window.location.href = `${baseUrl}/oauth2/authorization/${provider}`;
  },

  /**
   * 로그아웃 (토큰 삭제)
   */
  logout: (): void => {
    tokenStorage.clearTokens();
  },

  /**
   * 로그인 상태 확인
   */
  isAuthenticated: (): boolean => {
    return tokenStorage.hasTokens();
  },
};

export default authApi;
