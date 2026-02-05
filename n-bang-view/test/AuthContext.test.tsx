import { render, screen, waitFor, act } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { AuthProvider, useAuth } from '../contexts/AuthContext'

// API 모킹
vi.mock('../services/api', () => ({
  authApi: {
    getMe: vi.fn(),
    loginWithProvider: vi.fn(),
    logout: vi.fn(),
  },
  tokenStorage: {
    hasTokens: vi.fn(),
    getAccessToken: vi.fn(),
    getRefreshToken: vi.fn(),
    setTokens: vi.fn(),
    clearTokens: vi.fn(),
  },
}))

import { authApi, tokenStorage } from '../services/api'

const mockAuthApi = authApi as {
  getMe: ReturnType<typeof vi.fn>
  loginWithProvider: ReturnType<typeof vi.fn>
  logout: ReturnType<typeof vi.fn>
}

const mockTokenStorage = tokenStorage as {
  hasTokens: ReturnType<typeof vi.fn>
  getAccessToken: ReturnType<typeof vi.fn>
  getRefreshToken: ReturnType<typeof vi.fn>
  setTokens: ReturnType<typeof vi.fn>
  clearTokens: ReturnType<typeof vi.fn>
}

// 테스트용 컴포넌트
const TestComponent = () => {
  const { user, isLoading, isAuthenticated, login, logout } = useAuth()

  if (isLoading) {
    return <div data-testid="loading">로딩중...</div>
  }

  return (
    <div>
      <div data-testid="auth-status">
        {isAuthenticated ? '인증됨' : '미인증'}
      </div>
      {user && (
        <div data-testid="user-info">
          {user.nickname} ({user.email})
        </div>
      )}
      <button onClick={() => login('kakao')}>카카오 로그인</button>
      <button onClick={() => login('google')}>구글 로그인</button>
      <button onClick={logout}>로그아웃</button>
    </div>
  )
}

describe('AuthContext', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    // 기본적으로 URL 파라미터 없음
    Object.defineProperty(window, 'location', {
      value: {
        search: '',
        origin: 'http://localhost',
        pathname: '/',
        href: 'http://localhost/',
      },
      writable: true,
    })
    window.history.replaceState = vi.fn()
  })

  afterEach(() => {
    vi.resetAllMocks()
  })

  it('AuthProvider 없이 useAuth 사용 시 에러가 발생해야 함', () => {
    const consoleError = vi.spyOn(console, 'error').mockImplementation(() => {})

    expect(() => {
      render(<TestComponent />)
    }).toThrow('useAuth must be used within an AuthProvider')

    consoleError.mockRestore()
  })

  it('토큰이 없으면 미인증 상태여야 함', async () => {
    mockTokenStorage.hasTokens.mockReturnValue(false)

    render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>
    )

    await waitFor(() => {
      expect(screen.getByTestId('auth-status')).toHaveTextContent('미인증')
    })
  })

  it('토큰이 있고 사용자 정보 조회 성공 시 인증 상태여야 함', async () => {
    mockTokenStorage.hasTokens.mockReturnValue(true)
    mockAuthApi.getMe.mockResolvedValue({
      id: 1,
      email: 'test@example.com',
      nickname: '테스터',
      profileImage: null,
      provider: 'kakao',
    })

    render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>
    )

    await waitFor(() => {
      expect(screen.getByTestId('auth-status')).toHaveTextContent('인증됨')
    })

    expect(screen.getByTestId('user-info')).toHaveTextContent('테스터 (test@example.com)')
  })

  it('토큰이 있지만 사용자 정보 조회 실패 시 토큰을 삭제하고 미인증 상태여야 함', async () => {
    mockTokenStorage.hasTokens.mockReturnValue(true)
    mockAuthApi.getMe.mockRejectedValue(new Error('Unauthorized'))

    const consoleError = vi.spyOn(console, 'error').mockImplementation(() => {})

    render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>
    )

    await waitFor(() => {
      expect(screen.getByTestId('auth-status')).toHaveTextContent('미인증')
    })

    expect(mockTokenStorage.clearTokens).toHaveBeenCalled()

    consoleError.mockRestore()
  })

  it('카카오 로그인 버튼 클릭 시 loginWithProvider가 호출되어야 함', async () => {
    const user = userEvent.setup()
    mockTokenStorage.hasTokens.mockReturnValue(false)

    render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>
    )

    await waitFor(() => {
      expect(screen.getByTestId('auth-status')).toBeInTheDocument()
    })

    await user.click(screen.getByText('카카오 로그인'))

    expect(mockAuthApi.loginWithProvider).toHaveBeenCalledWith('kakao')
  })

  it('구글 로그인 버튼 클릭 시 loginWithProvider가 호출되어야 함', async () => {
    const user = userEvent.setup()
    mockTokenStorage.hasTokens.mockReturnValue(false)

    render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>
    )

    await waitFor(() => {
      expect(screen.getByTestId('auth-status')).toBeInTheDocument()
    })

    await user.click(screen.getByText('구글 로그인'))

    expect(mockAuthApi.loginWithProvider).toHaveBeenCalledWith('google')
  })

  it('로그아웃 버튼 클릭 시 logout이 호출되고 미인증 상태가 되어야 함', async () => {
    const user = userEvent.setup()
    mockTokenStorage.hasTokens.mockReturnValue(true)
    mockAuthApi.getMe.mockResolvedValue({
      id: 1,
      email: 'test@example.com',
      nickname: '테스터',
      profileImage: null,
      provider: 'kakao',
    })

    render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>
    )

    await waitFor(() => {
      expect(screen.getByTestId('auth-status')).toHaveTextContent('인증됨')
    })

    await user.click(screen.getByText('로그아웃'))

    expect(mockAuthApi.logout).toHaveBeenCalled()
    expect(screen.getByTestId('auth-status')).toHaveTextContent('미인증')
  })

  it('URL에 토큰 파라미터가 있으면 저장하고 URL에서 제거해야 함', async () => {
    Object.defineProperty(window, 'location', {
      value: {
        search: '?accessToken=abc123&refreshToken=xyz789',
        origin: 'http://localhost',
        pathname: '/',
        href: 'http://localhost/?accessToken=abc123&refreshToken=xyz789',
      },
      writable: true,
    })

    mockTokenStorage.hasTokens.mockReturnValue(true)
    mockAuthApi.getMe.mockResolvedValue({
      id: 1,
      email: 'test@example.com',
      nickname: '테스터',
      profileImage: null,
      provider: 'google',
    })

    render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>
    )

    await waitFor(() => {
      expect(mockTokenStorage.setTokens).toHaveBeenCalledWith('abc123', 'xyz789')
    })

    expect(window.history.replaceState).toHaveBeenCalledWith(
      {},
      expect.any(String),
      'http://localhost/'
    )
  })
})
