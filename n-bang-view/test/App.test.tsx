import { render, screen } from '@testing-library/react'
import { describe, it, expect, vi } from 'vitest'

// Mock contexts
vi.mock('../contexts/AuthContext', () => ({
  useAuth: () => ({
    user: null,
    isLoading: false,
    isAuthenticated: false,
    login: vi.fn(),
    logout: vi.fn(),
  }),
}))

vi.mock('../contexts/ToastContext', () => ({
  useToast: () => ({
    showToast: vi.fn(),
  }),
}))

vi.mock('../hooks', () => ({
  useGatherings: () => ({
    gatherings: [],
    isLoading: false,
    error: null,
    createGathering: vi.fn(),
    deleteGathering: vi.fn(),
    addParticipant: vi.fn(),
    removeParticipant: vi.fn(),
    addRound: vi.fn(),
    updateRound: vi.fn(),
    deleteRound: vi.fn(),
    updateLocalGathering: vi.fn(),
    refreshGathering: vi.fn(),
  }),
}))

import App from '../App'

describe('App', () => {
  it('로그인 화면이 표시되어야 함', () => {
    render(<App />)

    expect(screen.getByText('N-Bang')).toBeInTheDocument()
    expect(screen.getByText('카카오로 계속하기')).toBeInTheDocument()
    expect(screen.getByText('Google로 계속하기')).toBeInTheDocument()
  })
})
