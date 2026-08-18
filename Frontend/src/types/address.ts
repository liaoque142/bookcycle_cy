/** 收货地址（对应后端 AddressVO） */
export interface Address {
  id: number
  receiverName: string
  phone: string
  province: string
  city: string
  district: string
  detail: string
  isDefault: boolean
}

/** 新增/编辑地址入参 */
export interface AddressPayload {
  receiverName: string
  phone: string
  province: string
  city: string
  district: string
  detail: string
  isDefault?: boolean
}
