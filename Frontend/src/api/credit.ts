import request from './request'
import { USE_MOCK, delay, toPage } from './mock'
import type { Page } from '@/types/api'

/** 信誉变动日志（对应后端 CreditLogVO） */
export interface CreditLog {
  changeValue: number
  reason: string
  createTime: string
}

/** mock 日志：与 mock 订单状态机大致呼应 */
let mockLogs: CreditLog[] = [
  { changeValue: 2, reason: '成功交易', createTime: '2026-08-16 12:00:00' },
  { changeValue: 1, reason: '获得好评', createTime: '2026-08-16 12:05:00' },
]

/** 当前信誉分（对应后端 GET /credit/me） */
export async function fetchCreditMe(): Promise<number> {
  if (USE_MOCK) {
    await delay(100)
    return 100
  }
  return (await request.get('/credit/me')) as unknown as number
}

export async function fetchCreditLogs(page = 1, size = 20): Promise<Page<CreditLog>> {
  if (USE_MOCK) {
    await delay(150)
    return toPage(mockLogs, page, size)
  }
  return (await request.get('/credit/logs', { params: { page, size } })) as unknown as Page<CreditLog>
}
