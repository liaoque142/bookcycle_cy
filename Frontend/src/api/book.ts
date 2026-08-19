import request from './request'
import { USE_MOCK, delay, toPage } from './mock'
import { categoryIdByName } from './category'
import { books, featuredBooks, latestBooks } from '@/data/books'
import type { Book, BookQuery, PublishBookPayload, TradeType } from '@/types/book'
import type { Page } from '@/types/api'

/** 前端中文展示值 → 后端枚举编码（tradeType 1出售/2赠送/3交换，condition 1-4） */
const TRADE_TYPE_CODE: Record<TradeType, number> = { 出售: 1, 赠送: 2, 交换: 3 }
const CONDITION_CODE: Record<string, number> = { 全新: 1, 九成新: 2, 八成新: 3, 七成新及以下: 4 }

/** 书市筛选入参：分类名/中文枚举 → 后端编码字段（categoryId/tradeType/condition） */
async function toBookParams(query: BookQuery): Promise<Record<string, string | number | undefined>> {
  return {
    keyword: query.keyword?.trim() || undefined,
    categoryId: await categoryIdByName(query.category),
    tradeType: query.tradeType ? TRADE_TYPE_CODE[query.tradeType] : undefined,
    condition: query.condition ? CONDITION_CODE[query.condition] : undefined,
    sort: query.sort || undefined,
    page: query.page,
    size: query.size,
  }
}

/** 发布入参：分类名/中文枚举 → 后端编码，对齐 PublishBookRequest */
async function toPublishParams(payload: PublishBookPayload): Promise<Record<string, unknown>> {
  const categoryId = await categoryIdByName(payload.category)
  if (!categoryId) throw new Error('请选择正确的分类')
  return {
    title: payload.title,
    author: payload.author,
    isbn: payload.isbn,
    publisher: payload.publisher,
    categoryId,
    price: payload.price,
    tradeType: TRADE_TYPE_CODE[payload.tradeType],
    condition: CONDITION_CODE[payload.condition],
    description: payload.description,
  }
}

/** mock 极简过滤+排序：够演示即可，后端接管后由查询实现 */
function filterBooks(query: BookQuery): Book[] {
  const { keyword, category, tradeType, sort } = query
  let list = books
  if (keyword) {
    const kw = keyword.trim().toLowerCase()
    list = list.filter(
      (b) =>
        b.title.toLowerCase().includes(kw) ||
        b.author.toLowerCase().includes(kw) ||
        b.category.toLowerCase().includes(kw),
    )
  }
  if (category) list = list.filter((b) => b.category === category)
  if (tradeType) list = list.filter((b) => b.tradeType === tradeType)
  if (sort === 'latest') list = [...list].sort((a, b) => b.createdAt.localeCompare(a.createdAt))
  if (sort === 'price_asc') list = [...list].sort((a, b) => (a.price ?? Infinity) - (b.price ?? Infinity))
  if (sort === 'price_desc') list = [...list].sort((a, b) => (b.price ?? -Infinity) - (a.price ?? -Infinity))
  return list
}

/** 书市列表（分页） */
export async function fetchBooks(query: BookQuery = {}): Promise<Page<Book>> {
  if (USE_MOCK) {
    await delay(150)
    return toPage(filterBooks(query), query.page, query.size)
  }
  return (await request.get('/books', { params: await toBookParams(query) })) as unknown as Page<Book>
}

/** 首页精选 */
export async function fetchFeaturedBooks(): Promise<Book[]> {
  if (USE_MOCK) {
    await delay(150)
    return featuredBooks
  }
  return (await request.get('/books/featured')) as unknown as Book[]
}

/** 首页最新上架 */
export async function fetchLatestBooks(): Promise<Book[]> {
  if (USE_MOCK) {
    await delay(150)
    return latestBooks
  }
  return (await request.get('/books/latest')) as unknown as Book[]
}

/** 书籍详情 */
export async function fetchBookById(id: number): Promise<Book> {
  if (USE_MOCK) {
    await delay(150)
    const book = books.find((b) => b.id === id)
    if (!book) throw new Error('书籍不存在')
    return book
  }
  return (await request.get(`/books/${id}`)) as unknown as Book
}

/** 我发布的书籍（需登录；status 可选过滤，如交换发起选在售书） */
export async function fetchMyBooks(status?: number): Promise<Book[]> {
  if (USE_MOCK) {
    await delay(150)
    return books.filter((b) => b.seller === '书友小明')
  }
  const page = (await request.get('/books/mine', { params: { status, page: 1, size: 100 } })) as unknown as Page<Book>
  return page.list
}

/** 发布书籍（mock 生成 id/时间戳，返回新书） */
export async function publishBook(payload: PublishBookPayload): Promise<Book> {
  if (USE_MOCK) {
    await delay(300)
    const created: Book = {
      id: Math.max(0, ...books.map((b) => b.id)) + 1,
      ...payload,
      seller: '书友小明',
      credit: 100,
      coverTone: '#E8E2D8',
      coverToneText: '#1A1A1A',
      createdAt: new Date().toISOString().slice(0, 10),
    }
    books.unshift(created)
    return created
  }
  return (await request.post('/books', await toPublishParams(payload))) as unknown as Book
}
