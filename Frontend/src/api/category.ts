import request from './request'
import { USE_MOCK, delay } from './mock'
import { categories as mockCategories } from '@/data/books'
import type { Page } from '@/types/api'

export interface CategoryOption {
  id: number
  name: string
}

let cache: Promise<CategoryOption[]> | null = null

/** 分类字典（id + name）：真实模式拉 GET /categories，mock 模式用静态数据（id 与 seed 一致 1-8） */
export function fetchCategories(): Promise<CategoryOption[]> {
  if (!cache) cache = loadCategories()
  return cache
}

async function loadCategories(): Promise<CategoryOption[]> {
  if (USE_MOCK) {
    await delay(80)
    return mockCategories.map((c, i) => ({ id: i + 1, name: c.name }))
  }
  const page = (await request.get('/categories', { params: { page: 1, size: 100 } })) as unknown as Page<CategoryOption>
  return page.list.map((c) => ({ id: Number(c.id), name: c.name }))
}

/** 分类名 → id（发布/筛选入参映射用） */
export async function categoryIdByName(name?: string): Promise<number | undefined> {
  if (!name) return undefined
  const list = await fetchCategories()
  return list.find((c) => c.name === name)?.id
}
