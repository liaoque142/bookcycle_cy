export type TradeType = '出售' | '交换' | '赠送'

export interface Book {
  id: number
  title: string
  author: string
  category: string
  /** 售价；交换/赠送为 null */
  price: number | null
  tradeType: TradeType
  /** 新旧程度，如「九成新」 */
  condition: string
  seller: string
  /** 卖家头像（可空，前端回退首字） */
  sellerAvatar?: string
  /** 卖家信誉分 0-100 */
  credit: number
  /** 书封底色 */
  coverTone: string
  /** 书封文字色 */
  coverToneText: string
  createdAt: string
  /** 出版社 */
  publisher?: string
  /** ISBN */
  isbn?: string
  /** 描述 */
  description: string
  /** 分类ID（后端 BookVO 返回） */
  categoryId?: number
  /** 卖家ID（后端 BookVO 返回） */
  sellerId?: number
  /** 书籍状态：1 在售 / 2 已预订 / 3 已售出 / 4 已下架 */
  status?: number
  statusName?: string
}

/** 发布书籍提交参数 */
export interface PublishBookPayload {
  title: string
  author: string
  isbn?: string
  publisher?: string
  category: string
  price: number | null
  tradeType: TradeType
  condition: string
  description: string
}

export interface CategoryItem {
  name: string
  icon: string
  count: string
}

/** 书市列表查询参数（plan.md §8：page, size, sort） */
export interface BookQuery {
  keyword?: string
  category?: string
  tradeType?: TradeType
  condition?: string
  page?: number
  size?: number
  sort?: string
}
