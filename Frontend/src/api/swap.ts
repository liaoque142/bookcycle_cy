import request from './request'
import { USE_MOCK, delay, toPage } from './mock'
import type { Page } from '@/types/api'
import type { Swap, SwapQuery, CreateSwapPayload, AcceptSwapPayload } from '@/types/swap'

/** mock 交换单（默认模式演示用，固定「书友小明」= initiator 视角） */
let mockSwaps: Swap[] = [
  {
    id: 1,
    swapNo: 'SWMOCK0001',
    status: '交换中',
    statusCode: 3,
    initiatorId: 1,
    initiatorName: '书友小明',
    receiverId: 2,
    receiverName: '书友小红',
    bookId: 2,
    bookTitle: '活着',
    bookAuthor: '余华',
    offerBookId: 1,
    offerBookTitle: '三体',
    offerBookAuthor: '刘慈欣',
    message: '很喜欢这本，可以用三体换吗？',
    initiatorShipType: '邮寄',
    receiverShipType: '邮寄',
    receiverShippedAt: '2026-08-18 10:00:00',
    createTime: '2026-08-17 09:00:00',
    updateTime: '2026-08-18 10:00:00',
  },
]
let nextId = 2

function roleFilter(list: Swap[], role?: string): Swap[] {
  // mock 下当前登录者固定为「书友小明」（id=1）
  if (!role || role === 'initiator') return list.filter((s) => s.initiatorId === 1)
  return list.filter((s) => s.receiverId === 1)
}

export async function fetchSwaps(query: SwapQuery = {}): Promise<Page<Swap>> {
  if (USE_MOCK) {
    await delay(200)
    let list = roleFilter(mockSwaps, query.role)
    if (query.status) list = list.filter((s) => s.statusCode === query.status)
    return toPage(list, query.page, query.size)
  }
  return (await request.get('/swaps', { params: query })) as unknown as Page<Swap>
}

export async function fetchSwapByNo(swapNo: string): Promise<Swap> {
  if (USE_MOCK) {
    await delay(150)
    const swap = mockSwaps.find((s) => s.swapNo === swapNo)
    if (!swap) throw new Error('交换单不存在')
    return swap
  }
  return (await request.get(`/swaps/${swapNo}`)) as unknown as Swap
}

export async function createSwap(payload: CreateSwapPayload): Promise<Swap> {
  if (USE_MOCK) {
    await delay(300)
    const swap: Swap = {
      id: nextId++,
      swapNo: `SWMOCK${String(nextId).padStart(4, '0')}`,
      status: '待对方同意',
      statusCode: 1,
      initiatorId: 1,
      initiatorName: '书友小明',
      receiverId: 2,
      receiverName: '书友小红',
      bookId: payload.bookId,
      bookTitle: `示例书籍 ${payload.bookId}`,
      bookAuthor: '作者',
      offerBookId: payload.offerBookId,
      offerBookTitle: `我的书 ${payload.offerBookId}`,
      offerBookAuthor: '作者',
      message: payload.message,
      initiatorShipType: payload.shipType === 2 ? '自提' : '邮寄',
      createTime: new Date().toISOString().slice(0, 19).replace('T', ' '),
      updateTime: new Date().toISOString().slice(0, 19).replace('T', ' '),
    }
    mockSwaps.unshift(swap)
    return swap
  }
  return (await request.post('/swaps', payload)) as unknown as Swap
}

async function mutate(swapNo: string, method: 'post' | 'put', path: string, body?: unknown): Promise<Swap> {
  if (USE_MOCK) {
    await delay(200)
    const swap = mockSwaps.find((s) => s.swapNo === swapNo)
    if (!swap) throw new Error('交换单不存在')
    if (path === 'accept') {
      swap.status = '已同意待发货'
      swap.statusCode = 2
    } else if (path === 'reject') {
      swap.status = '已拒绝'
      swap.statusCode = 6
    } else if (path === 'cancel') {
      swap.status = '已取消'
      swap.statusCode = 5
    } else if (path === 'ship') {
      swap.status = '交换中'
      swap.statusCode = 3
      swap.initiatorShippedAt = new Date().toISOString().slice(0, 19).replace('T', ' ')
    } else if (path === 'confirm') {
      swap.status = '已完成'
      swap.statusCode = 4
    }
    return { ...swap }
  }
  const url = `/swaps/${swapNo}/${path}`
  if (method === 'put') {
    return (await request.put(url, body ?? {})) as unknown as Swap
  }
  return (await request.post(url, body ?? {})) as unknown as Swap
}

export async function acceptSwap(swapNo: string, payload: AcceptSwapPayload): Promise<Swap> {
  return mutate(swapNo, 'put', 'accept', payload)
}

export async function rejectSwap(swapNo: string): Promise<Swap> {
  return mutate(swapNo, 'put', 'reject')
}

export async function cancelSwap(swapNo: string, reason?: string): Promise<Swap> {
  return mutate(swapNo, 'post', 'cancel', { reason: reason ?? '' })
}

export async function shipSwap(swapNo: string): Promise<Swap> {
  return mutate(swapNo, 'post', 'ship')
}

export async function confirmSwap(swapNo: string): Promise<Swap> {
  return mutate(swapNo, 'post', 'confirm')
}
