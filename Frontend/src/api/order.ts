import request from './request'
import { USE_MOCK, delay, toPage } from './mock'
import type { Page } from '@/types/api'
import type {
  Order,
  OrderQuery,
  CreateOrderPayload,
  CancelOrderPayload,
  EvaluationPayload,
  CreateRefundPayload,
} from '@/types/trade'

/** mock 订单（默认模式演示用） */
let mockOrders: Order[] = [
  {
    id: 1,
    orderNo: 'BCMOCK0001',
    bookId: 1,
    bookTitle: '三体',
    buyerId: 2,
    buyerName: '书友小红',
    sellerId: 1,
    sellerName: '书友小明',
    amount: 25.5,
    tradeType: '出售',
    shipType: '邮寄',
    status: '已完成',
    statusCode: 4,
    receiverName: '书友小红',
    receiverPhone: '13800000002',
    receiverAddress: '浙江省杭州市西湖区文一西路 200 号',
    payStatus: 1,
    evaluatedByMe: false,
    createTime: '2026-08-15 10:00:00',
    updateTime: '2026-08-15 12:00:00',
  },
  {
    id: 2,
    orderNo: 'BCMOCK0002',
    bookId: 2,
    bookTitle: '活着',
    buyerId: 1,
    buyerName: '书友小明',
    sellerId: 2,
    sellerName: '书友小红',
    amount: 0,
    tradeType: '赠送',
    shipType: '自提',
    status: '待收货',
    statusCode: 3,
    evaluatedByMe: false,
    createTime: '2026-08-16 09:00:00',
    updateTime: '2026-08-16 09:00:00',
  },
]
let nextId = 3

function roleFilter(list: Order[], role?: string): Order[] {
  // mock 下当前登录者视角固定为「书友小明」（id=1）
  if (!role || role === 'buyer') return list.filter((o) => o.buyerId === 1)
  return list.filter((o) => o.sellerId === 1)
}

export async function fetchOrders(query: OrderQuery = {}): Promise<Page<Order>> {
  if (USE_MOCK) {
    await delay(200)
    let list = roleFilter(mockOrders, query.role)
    if (query.status) list = list.filter((o) => o.statusCode === query.status)
    return toPage(list, query.page, query.size)
  }
  return (await request.get('/orders', { params: query })) as unknown as Page<Order>
}

export async function fetchOrderByNo(orderNo: string): Promise<Order> {
  if (USE_MOCK) {
    await delay(150)
    const order = mockOrders.find((o) => o.orderNo === orderNo)
    if (!order) throw new Error('订单不存在')
    return order
  }
  return (await request.get(`/orders/${orderNo}`)) as unknown as Order
}

export async function createOrder(payload: CreateOrderPayload): Promise<Order> {
  if (USE_MOCK) {
    await delay(300)
    const order: Order = {
      id: nextId++,
      orderNo: `BCMOCK${String(nextId).padStart(4, '0')}`,
      bookId: payload.bookId,
      bookTitle: `示例书籍 ${payload.bookId}`,
      buyerId: 1,
      buyerName: '书友小明',
      sellerId: 2,
      sellerName: '书友小红',
      amount: payload.shipType === 2 ? 0 : 25.5,
      tradeType: '出售',
      shipType: payload.shipType === 2 ? '自提' : '邮寄',
      status: '待支付',
      statusCode: 1,
      payStatus: 0,
      evaluatedByMe: false,
      createTime: new Date().toISOString().slice(0, 19).replace('T', ' '),
      updateTime: new Date().toISOString().slice(0, 19).replace('T', ' '),
    }
    mockOrders.unshift(order)
    return order
  }
  return (await request.post('/orders', payload)) as unknown as Order
}

async function mutate(orderNo: string, path: string, body?: unknown): Promise<Order> {
  if (USE_MOCK) {
    await delay(200)
    const order = mockOrders.find((o) => o.orderNo === orderNo)
    if (!order) throw new Error('订单不存在')
    if (path === 'pay') {
      order.status = '待发货'
      order.statusCode = 2
      order.payStatus = 1
    } else if (path === 'cancel') {
      order.status = '已取消'
      order.statusCode = 5
    } else if (path === 'ship') {
      order.status = '待收货'
      order.statusCode = 3
    } else if (path === 'confirm') {
      order.status = '已完成'
      order.statusCode = 4
    }
    return { ...order }
  }
  const url = `/orders/${orderNo}/${path}`
  return (await request.post(url, body ?? {})) as unknown as Order
}

export async function payOrder(orderNo: string): Promise<Order> {
  return mutate(orderNo, 'pay', { payMethod: 1 })
}

export async function cancelOrder(orderNo: string, payload?: CancelOrderPayload): Promise<Order> {
  return mutate(orderNo, 'cancel', payload ?? {})
}

export async function shipOrder(orderNo: string): Promise<Order> {
  return mutate(orderNo, 'ship')
}

export async function confirmOrder(orderNo: string): Promise<Order> {
  return mutate(orderNo, 'confirm')
}

export async function evaluateOrder(orderNo: string, payload: EvaluationPayload): Promise<void> {
  if (USE_MOCK) {
    await delay(200)
    const order = mockOrders.find((o) => o.orderNo === orderNo)
    if (order) order.evaluatedByMe = true
    return
  }
  await request.post(`/orders/${orderNo}/evaluate`, payload)
}

/** 申请售后（买家）：type 1 仅退款 / 2 退货退款，reason 可选 */
export async function createRefund(orderNo: string, payload: CreateRefundPayload): Promise<void> {
  if (USE_MOCK) {
    await delay(200)
    return
  }
  await request.post(`/orders/${orderNo}/refund`, payload)
}
