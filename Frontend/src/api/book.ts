import request from './request'
import { USE_MOCK, delay, toPage } from './mock'
import { books, featuredBooks, latestBooks } from '@/data/books'
import type { Book, BookQuery } from '@/types/book'
import type { Page } from '@/types/api'

/** 书市列表（分页） */
export async function fetchBooks(params: BookQuery = {}): Promise<Page<Book>> {
  if (USE_MOCK) {
    await delay(150)
    return toPage(books, params.page, params.size)
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
