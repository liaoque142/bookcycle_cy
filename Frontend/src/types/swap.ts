export type SwapStatus = '待对方同意' | '已同意待发货' | '交换中' | '已完成' | '已取消' | '已拒绝'

/** 交换单（对应后端 SwapVO） */
export interface Swap {
  id: number
  swapNo: string
  status: SwapStatus
  statusCode: number
  initiatorId: number
  initiatorName: string
  receiverId: number
  receiverName: string
  /** 被交换书籍（卖家书 A） */
  bookId: number
  bookTitle: string
  bookAuthor: string
  /** 发起人提供的书籍（买家书 B） */
  offerBookId: number
  offerBookTitle: string
  offerBookAuthor: string
  message?: string
  initiatorShipType?: string
  initiatorShipTypeCode?: number
  receiverShipType?: string
  receiverShipTypeCode?: number
  initiatorReceiverName?: string
  initiatorReceiverPhone?: string
  initiatorReceiverAddress?: string
  receiverReceiverName?: string
  receiverReceiverPhone?: string
  receiverReceiverAddress?: string
  initiatorShippedAt?: string
  receiverShippedAt?: string
  initiatorConfirmedAt?: string
  receiverConfirmedAt?: string
  cancelReason?: string
  createTime: string
  updateTime: string
}

/** 发起交换入参 */
export interface CreateSwapPayload {
  /** 被交换书籍（卖家书 A） */
  bookId: number
  /** 我提供的书籍 */
  offerBookId: number
  shipType: number
  addressId?: number
  message?: string
}

/** 同意交换入参 */
export interface AcceptSwapPayload {
  shipType: number
  addressId?: number
}

export interface SwapQuery {
  role?: 'initiator' | 'receiver'
  status?: number
  page?: number
  size?: number
}
