import type { Page } from '@/types/api'

/** mock 开关：显式设置 VITE_API_MOCK=false 时走真实后端 */
export const USE_MOCK = import.meta.env.VITE_API_MOCK !== 'false'

/** 模拟网络延迟 */
export function delay(ms: number): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, ms))
}

/** 极简切片分页（过滤/排序逻辑留给后端） */
export function toPage<T>(list: T[], page = 1, size = 10): Page<T> {
  const start = (page - 1) * size
  return {
    list: list.slice(start, start + size),
    total: list.length,
    page,
    size,
  }
}
