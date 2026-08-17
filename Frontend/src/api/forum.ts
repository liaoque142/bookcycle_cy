import request from './request'
import { USE_MOCK, delay } from './mock'
import { posts } from '@/data/posts'
import type { ForumPost } from '@/types/book'

/** 论坛帖子列表（首页/论坛页） */
export async function fetchForumPosts(): Promise<ForumPost[]> {
  if (USE_MOCK) {
    await delay(150)
    return posts
  }
  return (await request.get('/forum/posts')) as unknown as ForumPost[]
}
