import React, { createContext, useContext, useState, useEffect, useCallback, ReactNode } from 'react';
import { authApi, tokenStorage, MemberResponse } from '../services/api';

// ============================================
// Types
// ============================================

interface AuthState {
  user: MemberResponse | null;
  isLoading: boolean;
  isAuthenticated: boolean;
}

interface AuthContextType extends AuthState {
  login: (provider: 'google' | 'kakao') => void;
  logout: () => void;
  refreshUser: () => Promise<void>;
}

// ============================================
// Context
// ============================================

const AuthContext = createContext<AuthContextType | null>(null);

// ============================================
// Provider
// ============================================

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [user, setUser] = useState<MemberResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  // OAuth2 콜백 처리 (URL 파라미터에서 토큰 추출)
  const handleOAuth2Callback = useCallback(() => {
    const urlParams = new URLSearchParams(window.location.search);
    const accessToken = urlParams.get('accessToken');
    const refreshToken = urlParams.get('refreshToken');

    if (accessToken && refreshToken) {
      // 토큰 저장
      tokenStorage.setTokens(accessToken, refreshToken);

      // URL을 루트로 정리 (oauth2/redirect 경로 제거)
      const cleanUrl = window.location.origin + '/';
      window.history.replaceState({}, document.title, cleanUrl);

      return true;
    }
    return false;
  }, []);

  // 사용자 정보 조회
  const fetchUser = useCallback(async () => {
    if (!tokenStorage.hasTokens()) {
      setUser(null);
      setIsLoading(false);
      return;
    }

    try {
      const userData = await authApi.getMe();
      setUser(userData);
    } catch (error) {
      console.error('Failed to fetch user:', error);
      tokenStorage.clearTokens();
      setUser(null);
    } finally {
      setIsLoading(false);
    }
  }, []);

  // 초기화
  useEffect(() => {
    const init = async () => {
      // OAuth2 콜백 처리
      handleOAuth2Callback();

      // 사용자 정보 조회
      await fetchUser();
    };

    init();
  }, [handleOAuth2Callback, fetchUser]);

  // 로그인
  const login = useCallback((provider: 'google' | 'kakao') => {
    authApi.loginWithProvider(provider);
  }, []);

  // 로그아웃
  const logout = useCallback(() => {
    authApi.logout();
    setUser(null);
  }, []);

  // 사용자 정보 새로고침
  const refreshUser = useCallback(async () => {
    await fetchUser();
  }, [fetchUser]);

  const value: AuthContextType = {
    user,
    isLoading,
    isAuthenticated: !!user,
    login,
    logout,
    refreshUser,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

// ============================================
// Hook
// ============================================

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export default AuthContext;
