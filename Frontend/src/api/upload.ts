import request from './request'
import { USE_MOCK, delay } from './mock'

/** 上传图片，返回可访问的 URL 路径（如 /uploads/2026/08/xxx.png） */
export async function uploadImage(file: File): Promise<string> {
  if (USE_MOCK) {
    // mock 模式无后端，直接读成本地 data URL 供预览展示
    await delay(300)
    return await readAsDataURL(file)
  }
  const form = new FormData()
  form.append('file', file)
  const res = (await request.post('/upload/image', form)) as unknown as { url: string }
  return res.url
}

function readAsDataURL(file: File): Promise<string> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(reader.result as string)
    reader.onerror = () => reject(new Error('图片读取失败'))
    reader.readAsDataURL(file)
  })
}
