import request from './request'
import { USE_MOCK, delay, toPage } from './mock'
import type { MessageItem, PeerUnread, SendMessagePayload } from '@/types/message'
import type { Page } from '@/types/api'

/** 发送私信（仅好友），文字与图片至少一项 */
export async function sendMessage(payload: SendMessagePayload): Promise<void> {
  if (USE_MOCK) {
    await delay(120)
    return
  }
  await request.post('/messages', payload)
}

/** 与某好友的聊天记录（最近 100 条正序，仅参与者可见） */
export async function fetchHistory(userId: number): Promise<MessageItem[]> {
  if (USE_MOCK) {
    await delay(120)
    return []
  }
  return (await request.get(`/messages/${userId}`)) as unknown as MessageItem[]
}

/** 标记该好友发来的全部消息已读 */
export async function markPeerRead(userId: number): Promise<void> {
  if (USE_MOCK) {
    await delay(60)
    return
  }
  await request.put(`/messages/${userId}/read`)
}

/** 全部未读总数（Header 私信红点） */
export async function fetchMessageUnreadCount(): Promise<number> {
  if (USE_MOCK) {
    await delay(60)
    return 0
  }
  return (await request.get('/messages/unread-count')) as unknown as number
}

/** 按好友未读数（好友行角标） */
export async function fetchMessageUnreadSummary(): Promise<PeerUnread[]> {
  if (USE_MOCK) {
    await delay(60)
    return []
  }
  return (await request.get('/messages/unread-summary')) as unknown as PeerUnread[]
}

/** 消息中心：我收到的全部私信（跨好友，含发送者昵称/头像，分页倒序） */
export async function fetchInbox(query: { page?: number; size?: number } = {}): Promise<Page<MessageItem>> {
  if (USE_MOCK) {
    await delay(120)
    return toPage([], query.page, query.size)
  }
  return (await request.get('/messages', { params: query })) as unknown as Page<MessageItem>
}

/** 标记单条已读（仅接收者本人，幂等） */
export async function markMessageRead(id: number): Promise<void> {
  if (USE_MOCK) {
    await delay(60)
    return
  }
  await request.put(`/messages/${id}`)
}
