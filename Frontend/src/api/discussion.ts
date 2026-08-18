import request from './request'
import { USE_MOCK, delay } from './mock'
import type { DiscussionItem } from '@/types/discussion'

/** 书籍讨论列表（游客可看） */
export async function fetchDiscussions(bookId: number): Promise<DiscussionItem[]> {
  if (USE_MOCK) {
    await delay(120)
    return []
  }
  return (await request.get(`/books/${bookId}/discussions`)) as unknown as DiscussionItem[]
}

/** 发表讨论 / 回复（parentId 为空为顶级） */
export async function createDiscussion(
  bookId: number,
  payload: { content: string; parentId?: number | null },
): Promise<void> {
  if (USE_MOCK) {
    await delay(120)
    return
  }
  await request.post(`/books/${bookId}/discussions`, payload)
}

/** 删除讨论（本人或书籍作者） */
export async function deleteDiscussion(bookId: number, id: number): Promise<void> {
  if (USE_MOCK) {
    await delay(120)
    return
  }
  await request.delete(`/books/${bookId}/discussions/${id}`)
}
