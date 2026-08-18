import request from './request'
import { USE_MOCK, delay } from './mock'
import type { Address, AddressPayload } from '@/types/address'

/** mock 地址（默认模式演示用） */
let mockAddresses: Address[] = [
  {
    id: 1,
    receiverName: '书友小明',
    phone: '13800000001',
    province: '浙江省',
    city: '杭州市',
    district: '西湖区',
    detail: '文一西路 100 号',
    isDefault: true,
  },
]
let nextId = 2

export async function fetchAddresses(): Promise<Address[]> {
  if (USE_MOCK) {
    await delay(150)
    return [...mockAddresses].sort((a, b) => Number(b.isDefault) - Number(a.isDefault))
  }
  return (await request.get('/addresses')) as unknown as Address[]
}

export async function createAddress(payload: AddressPayload): Promise<Address> {
  if (USE_MOCK) {
    await delay(200)
    const first = mockAddresses.length === 0
    const asDefault = !!payload.isDefault || first
    if (asDefault) mockAddresses.forEach((a) => (a.isDefault = false))
    const addr: Address = { id: nextId++, ...payload, isDefault: asDefault }
    mockAddresses.unshift(addr)
    return addr
  }
  return (await request.post('/addresses', payload)) as unknown as Address
}

export async function updateAddress(id: number, payload: AddressPayload): Promise<Address> {
  if (USE_MOCK) {
    await delay(200)
    const addr = mockAddresses.find((a) => a.id === id)
    if (!addr) throw new Error('地址不存在')
    if (payload.isDefault) mockAddresses.forEach((a) => (a.isDefault = a.id === id))
    Object.assign(addr, payload)
    return addr
  }
  return (await request.put(`/addresses/${id}`, payload)) as unknown as Address
}

export async function deleteAddress(id: number): Promise<void> {
  if (USE_MOCK) {
    await delay(200)
    mockAddresses = mockAddresses.filter((a) => a.id !== id)
    return
  }
  await request.delete(`/addresses/${id}`)
}

export async function setDefaultAddress(id: number): Promise<void> {
  if (USE_MOCK) {
    await delay(200)
    mockAddresses.forEach((a) => (a.isDefault = a.id === id))
    return
  }
  await request.put(`/addresses/${id}/default`)
}
