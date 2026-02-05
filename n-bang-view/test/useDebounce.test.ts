import { renderHook, act } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { useDebounce, useDebouncedCallback } from '../hooks/useDebounce'

describe('useDebounce', () => {
  beforeEach(() => {
    vi.useFakeTimers()
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  it('초기값을 즉시 반환해야 함', () => {
    const { result } = renderHook(() => useDebounce('initial', 500))

    expect(result.current).toBe('initial')
  })

  it('delay 후에 값이 업데이트되어야 함', () => {
    const { result, rerender } = renderHook(
      ({ value, delay }) => useDebounce(value, delay),
      { initialProps: { value: 'initial', delay: 500 } }
    )

    expect(result.current).toBe('initial')

    // 값 변경
    rerender({ value: 'updated', delay: 500 })

    // 아직 업데이트되지 않음
    expect(result.current).toBe('initial')

    // 500ms 경과
    act(() => {
      vi.advanceTimersByTime(500)
    })

    expect(result.current).toBe('updated')
  })

  it('delay 전에 값이 변경되면 타이머가 리셋되어야 함', () => {
    const { result, rerender } = renderHook(
      ({ value, delay }) => useDebounce(value, delay),
      { initialProps: { value: 'first', delay: 500 } }
    )

    // 첫 번째 변경
    rerender({ value: 'second', delay: 500 })

    // 300ms 후 두 번째 변경
    act(() => {
      vi.advanceTimersByTime(300)
    })

    rerender({ value: 'third', delay: 500 })

    // 300ms 더 경과 (총 600ms, 하지만 리셋되어서 아직 업데이트 안됨)
    act(() => {
      vi.advanceTimersByTime(300)
    })

    expect(result.current).toBe('first')

    // 200ms 더 경과 (두 번째 변경 후 500ms)
    act(() => {
      vi.advanceTimersByTime(200)
    })

    expect(result.current).toBe('third')
  })

  it('숫자 타입도 지원해야 함', () => {
    const { result, rerender } = renderHook(
      ({ value, delay }) => useDebounce(value, delay),
      { initialProps: { value: 100, delay: 300 } }
    )

    rerender({ value: 200, delay: 300 })

    act(() => {
      vi.advanceTimersByTime(300)
    })

    expect(result.current).toBe(200)
  })

  it('객체 타입도 지원해야 함', () => {
    const initialObj = { name: 'test' }
    const { result, rerender } = renderHook(
      ({ value, delay }) => useDebounce(value, delay),
      { initialProps: { value: initialObj, delay: 300 } }
    )

    const newObj = { name: 'updated' }
    rerender({ value: newObj, delay: 300 })

    act(() => {
      vi.advanceTimersByTime(300)
    })

    expect(result.current).toEqual({ name: 'updated' })
  })
})

describe('useDebouncedCallback', () => {
  beforeEach(() => {
    vi.useFakeTimers()
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  it('delay 후에 콜백이 호출되어야 함', () => {
    const callback = vi.fn()
    const { result } = renderHook(() => useDebouncedCallback(callback, 500))

    act(() => {
      result.current('arg1', 'arg2')
    })

    expect(callback).not.toHaveBeenCalled()

    act(() => {
      vi.advanceTimersByTime(500)
    })

    expect(callback).toHaveBeenCalledTimes(1)
    expect(callback).toHaveBeenCalledWith('arg1', 'arg2')
  })

  it('delay 내에 여러 번 호출되면 마지막 호출만 실행해야 함', () => {
    const callback = vi.fn()
    const { result } = renderHook(() => useDebouncedCallback(callback, 500))

    act(() => {
      result.current('first')
      result.current('second')
      result.current('third')
    })

    act(() => {
      vi.advanceTimersByTime(500)
    })

    expect(callback).toHaveBeenCalledTimes(1)
    expect(callback).toHaveBeenCalledWith('third')
  })

  it('delay가 지난 후 다시 호출하면 새로 실행해야 함', () => {
    const callback = vi.fn()
    const { result } = renderHook(() => useDebouncedCallback(callback, 500))

    // 첫 번째 호출
    act(() => {
      result.current('first')
    })

    act(() => {
      vi.advanceTimersByTime(500)
    })

    expect(callback).toHaveBeenCalledTimes(1)

    // 두 번째 호출
    act(() => {
      result.current('second')
    })

    act(() => {
      vi.advanceTimersByTime(500)
    })

    expect(callback).toHaveBeenCalledTimes(2)
    expect(callback).toHaveBeenLastCalledWith('second')
  })

  it('중간에 호출하면 타이머가 리셋되어야 함', () => {
    const callback = vi.fn()
    const { result } = renderHook(() => useDebouncedCallback(callback, 500))

    act(() => {
      result.current('first')
    })

    // 300ms 후 다시 호출
    act(() => {
      vi.advanceTimersByTime(300)
      result.current('second')
    })

    // 300ms 더 경과 (총 600ms, 하지만 두 번째 호출 기준으로 300ms만 경과)
    act(() => {
      vi.advanceTimersByTime(300)
    })

    expect(callback).not.toHaveBeenCalled()

    // 200ms 더 경과
    act(() => {
      vi.advanceTimersByTime(200)
    })

    expect(callback).toHaveBeenCalledTimes(1)
    expect(callback).toHaveBeenCalledWith('second')
  })
})
