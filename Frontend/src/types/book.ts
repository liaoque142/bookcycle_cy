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
  /** 卖家信誉分 0-100 */
  credit: number
  /** 书封底色 */
  coverTone: string
  /** 书封文字色 */
  coverToneText: string
  createdAt: string
}

export interface ForumPost {
  id: number
  title: string
  board: string
  author: string
  replies: number
  likes: number
  excerpt: string
}

export interface CategoryItem {
  name: string
  icon: string
  count: string
}
