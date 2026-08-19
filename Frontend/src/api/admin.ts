import request from './request'
import { USE_MOCK, delay, toPage } from './mock'
import type { Page } from '@/types/api'
import type { AdminUser, AdminCategory, AdminCategoryPayload, AdminBook, OperationLog } from '@/types/admin'

/** 管理后台：用户管理 */
export interface AdminUserQuery {
  keyword?: string
  status?: number
  page: number
  size: number
}

export async function fetchAdminUsers(q: AdminUserQuery): Promise<Page<AdminUser>> {
  if (USE_MOCK) {
    await delay(120)
    return toPage<AdminUser>([])
  }
  return (await request.get('/admin/users', { params: q })) as unknown as Page<AdminUser>
}

export async function fetchAdminUserDetail(id: number): Promise<AdminUser> {
  if (USE_MOCK) {
    await delay(120)
    throw new Error('mock 模式无管理接口')
  }
  return (await request.get(`/admin/users/${id}`)) as unknown as AdminUser
}

/** 启用/禁用用户（status: 1 正常 / 0 禁用） */
export async function updateAdminUserStatus(id: number, status: number): Promise<void> {
  if (USE_MOCK) {
    await delay(120)
    return
  }
  await request.put(`/admin/users/${id}/status`, { status })
}

/** 调整信誉分（changeValue -100~100，reason 必填） */
export async function adjustAdminUserCredit(id: number, changeValue: number, reason: string): Promise<void> {
  if (USE_MOCK) {
    await delay(120)
    return
  }
  await request.put(`/admin/users/${id}/credit`, { changeValue, reason })
}

/** 管理后台：分类管理（返回扁平列表，前端组树） */
export async function fetchAdminCategories(): Promise<AdminCategory[]> {
  if (USE_MOCK) {
    await delay(120)
    return [
      { id: 1, parentId: 0, name: '文学', sort: 1, status: 1 },
      { id: 2, parentId: 0, name: '科技', sort: 2, status: 1 },
      { id: 11, parentId: 1, name: '小说', sort: 1, status: 1 },
      { id: 12, parentId: 1, name: '散文', sort: 2, status: 1 },
    ]
  }
  return (await request.get('/admin/categories')) as unknown as AdminCategory[]
}

export async function createAdminCategory(payload: AdminCategoryPayload): Promise<AdminCategory> {
  if (USE_MOCK) {
    await delay(120)
    return { id: Date.now(), parentId: payload.parentId ?? 0, name: payload.name, sort: payload.sort ?? 0, status: payload.status ?? 1 }
  }
  return (await request.post('/admin/categories', payload)) as unknown as AdminCategory
}

export async function updateAdminCategory(id: number, payload: AdminCategoryPayload): Promise<void> {
  if (USE_MOCK) {
    await delay(120)
    return
  }
  await request.put(`/admin/categories/${id}`, payload)
}

export async function deleteAdminCategory(id: number): Promise<void> {
  if (USE_MOCK) {
    await delay(120)
    return
  }
  await request.delete(`/admin/categories/${id}`)
}

/** 管理后台：书籍管理 */
export interface AdminBookQuery {
  keyword?: string
  /** 书籍状态 1-4 */
  status?: number
  /** 交易方式 1出售/2赠送/3交换 */
  tradeType?: number
  categoryId?: number
  page: number
  size: number
}

export async function fetchAdminBooks(q: AdminBookQuery): Promise<Page<AdminBook>> {
  if (USE_MOCK) {
    await delay(120)
    return toPage<AdminBook>([])
  }
  return (await request.get('/admin/books', { params: q })) as unknown as Page<AdminBook>
}

/** 下架（status=4）/ 上架（status=1） */
export async function updateAdminBookStatus(id: number, status: 1 | 4): Promise<void> {
  if (USE_MOCK) {
    await delay(120)
    return
  }
  await request.put(`/admin/books/${id}/status`, { status })
}

/** 管理后台：操作日志 */
export interface AdminOperationLogQuery {
  action?: string
  page: number
  size: number
}

export async function fetchAdminOperationLogs(q: AdminOperationLogQuery): Promise<Page<OperationLog>> {
  if (USE_MOCK) {
    await delay(120)
    return toPage<OperationLog>([])
  }
  return (await request.get('/admin/operation-logs', { params: q })) as unknown as Page<OperationLog>
}
