import type { TradeType } from './book'

export type ShipType = '邮寄' | '自提'
export type OrderStatus = '待支付' | '待发货' | '待收货' | '已完成' | '已取消' | '售后中'

/** 订单（对应后端 OrderVO） */
export interface Order {
  id: number
  orderNo: string
  bookId: number
  bookTitle: string
  buyerId: number
  buyerName: string
  sellerId: number
  sellerName: string
  amount: number
  tradeType: TradeType
  shipType: ShipType
  status: OrderStatus
  statusCode: number
  receiverName?: string
  receiverPhone?: string
  receiverAddress?: string
  cancelReason?: string
  /** 支付状态：0 未支付 / 1 已支付 / 2 已退款 */
  payStatus?: number
  payTime?: string
  evaluatedByMe?: boolean
  createTime: string
  updateTime: string
}

/** 交易评价（对应后端 EvaluationVO） */
export interface Evaluation {
  id: number
  orderId: number
  evaluatorId: number
  evaluatorName: string
  targetId: number
  score: number
  content?: string
  createTime: string
}

/** 下单入参（shipType 1 邮寄 / 2 自提） */
export interface CreateOrderPayload {
  bookId: number
  shipType: number
  addressId?: number
}

export interface CancelOrderPayload {
  reason?: string
}

export interface PayPayload {
  payMethod?: number
}

export interface EvaluationPayload {
  score: number
  content?: string
}

export interface OrderQuery {
  role?: 'buyer' | 'seller'
  status?: number
  page?: number
  size?: number
}
