/** 后端统一响应（plan.md §8：{ code, message, data }） */
export interface Result<T> {
  code: number
  message: string
  data: T
}

/** 分页结果（plan.md §8：page, size, sort） */
export interface Page<T> {
  list: T[]
  total: number
  page: number
  size: number
}
