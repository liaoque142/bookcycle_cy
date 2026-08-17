import request from './request'
import { USE_MOCK, delay, toPage } from './mock'
import { books, featuredBooks, latestBooks } from '@/data/books'
import type { Book, BookQuery, PublishBookPayload } from '@/types/book'
import type { Page } from '@/types/api'

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
export async function fetchBooks(params: BookQuery = {}): Promise<Page<Book>> {
  if (USE_MOCK) {
    await delay(150)
    return toPage(filterBooks(params), params.page, params.size)
  }
  return (await request.get('/books', { params })) as unknown as Page<Book>
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
  return (await request.post('/books', payload)) as unknown as Book
}
