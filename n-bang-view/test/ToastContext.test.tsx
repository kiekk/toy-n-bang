import { render, screen, act, fireEvent } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { ToastProvider, useToast } from '../contexts/ToastContext'

// 테스트용 컴포넌트 - 토스트 데이터만 표시 (메시지 중복 방지)
const TestComponent = () => {
  const { showToast, toasts, removeToast } = useToast()

  return (
    <div>
      <button onClick={() => showToast('success', '성공 메시지')}>
        성공 토스트
      </button>
      <button onClick={() => showToast('error', '에러 메시지')}>
        에러 토스트
      </button>
      <button onClick={() => showToast('warning', '경고 메시지')}>
        경고 토스트
      </button>
      <div data-testid="toast-count">{toasts.length}</div>
      <div data-testid="toast-types">
        {toasts.map(t => t.type).join(',')}
      </div>
    </div>
  )
}

describe('ToastContext', () => {
  beforeEach(() => {
    vi.useFakeTimers()
  })

  afterEach(() => {
    vi.runOnlyPendingTimers()
    vi.useRealTimers()
  })

  it('ToastProvider 없이 useToast 사용 시 에러가 발생해야 함', () => {
    const consoleError = vi.spyOn(console, 'error').mockImplementation(() => {})

    expect(() => {
      render(<TestComponent />)
    }).toThrow('useToast must be used within a ToastProvider')

    consoleError.mockRestore()
  })

  it('성공 토스트를 표시해야 함', () => {
    render(
      <ToastProvider>
        <TestComponent />
      </ToastProvider>
    )

    act(() => {
      fireEvent.click(screen.getByText('성공 토스트'))
    })

    expect(screen.getByTestId('toast-count')).toHaveTextContent('1')
    expect(screen.getByTestId('toast-types')).toHaveTextContent('success')
  })

  it('에러 토스트를 표시해야 함', () => {
    render(
      <ToastProvider>
        <TestComponent />
      </ToastProvider>
    )

    act(() => {
      fireEvent.click(screen.getByText('에러 토스트'))
    })

    expect(screen.getByTestId('toast-count')).toHaveTextContent('1')
    expect(screen.getByTestId('toast-types')).toHaveTextContent('error')
  })

  it('경고 토스트를 표시해야 함', () => {
    render(
      <ToastProvider>
        <TestComponent />
      </ToastProvider>
    )

    act(() => {
      fireEvent.click(screen.getByText('경고 토스트'))
    })

    expect(screen.getByTestId('toast-count')).toHaveTextContent('1')
    expect(screen.getByTestId('toast-types')).toHaveTextContent('warning')
  })

  it('여러 토스트를 동시에 표시해야 함', () => {
    render(
      <ToastProvider>
        <TestComponent />
      </ToastProvider>
    )

    act(() => {
      fireEvent.click(screen.getByText('성공 토스트'))
      fireEvent.click(screen.getByText('에러 토스트'))
    })

    expect(screen.getByTestId('toast-count')).toHaveTextContent('2')
    expect(screen.getByTestId('toast-types')).toHaveTextContent('success,error')
  })

  it('3초 후에 토스트가 자동으로 사라져야 함', () => {
    render(
      <ToastProvider>
        <TestComponent />
      </ToastProvider>
    )

    act(() => {
      fireEvent.click(screen.getByText('성공 토스트'))
    })

    expect(screen.getByTestId('toast-count')).toHaveTextContent('1')

    // 3초 경과
    act(() => {
      vi.advanceTimersByTime(3000)
    })

    expect(screen.getByTestId('toast-count')).toHaveTextContent('0')
  })

  it('removeToast로 토스트를 제거할 수 있어야 함', () => {
    let removeToastFn: (id: string) => void
    let toastId: string = ''

    const CaptureComponent = () => {
      const { showToast, toasts, removeToast } = useToast()
      removeToastFn = removeToast
      if (toasts.length > 0) {
        toastId = toasts[0].id
      }
      return (
        <div>
          <button onClick={() => showToast('success', '테스트')}>추가</button>
          <div data-testid="count">{toasts.length}</div>
        </div>
      )
    }

    render(
      <ToastProvider>
        <CaptureComponent />
      </ToastProvider>
    )

    act(() => {
      fireEvent.click(screen.getByText('추가'))
    })

    expect(screen.getByTestId('count')).toHaveTextContent('1')

    act(() => {
      removeToastFn(toastId)
    })

    expect(screen.getByTestId('count')).toHaveTextContent('0')
  })

  it('각 토스트는 고유한 ID를 가져야 함', () => {
    let capturedToasts: any[] = []

    const CaptureComponent = () => {
      const { showToast, toasts } = useToast()
      capturedToasts = toasts
      return (
        <button onClick={() => showToast('success', '테스트')}>추가</button>
      )
    }

    render(
      <ToastProvider>
        <CaptureComponent />
      </ToastProvider>
    )

    act(() => {
      fireEvent.click(screen.getByText('추가'))
    })

    act(() => {
      vi.advanceTimersByTime(10) // 약간의 시간 경과로 다른 ID 생성
      fireEvent.click(screen.getByText('추가'))
    })

    expect(capturedToasts).toHaveLength(2)
    expect(capturedToasts[0].id).not.toBe(capturedToasts[1].id)
  })
})
