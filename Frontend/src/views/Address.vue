<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fetchAddresses, createAddress, updateAddress, deleteAddress, setDefaultAddress } from '@/api/address'
import type { Address, AddressPayload } from '@/types/address'

const addresses = ref<Address[]>([])
const loading = ref(true)

const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const form = reactive<AddressPayload>({
  receiverName: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  detail: '',
  isDefault: false,
})

function openCreate() {
  editingId.value = null
  Object.assign(form, { receiverName: '', phone: '', province: '', city: '', district: '', detail: '', isDefault: false })
  dialogVisible.value = true
}

function openEdit(addr: Address) {
  editingId.value = addr.id
  Object.assign(form, {
    receiverName: addr.receiverName,
    phone: addr.phone,
    province: addr.province,
    city: addr.city,
    district: addr.district,
    detail: addr.detail,
    isDefault: addr.isDefault,
  })
  dialogVisible.value = true
}

async function load() {
  loading.value = true
  try {
    addresses.value = await fetchAddresses()
  } finally {
    loading.value = false
  }
}

async function submit() {
  if (!form.receiverName.trim() || !form.phone.trim()) {
    ElMessage.warning('请填写收货人和电话')
    return
  }
  const payload: AddressPayload = { ...form, receiverName: form.receiverName.trim(), phone: form.phone.trim() }
  if (editingId.value) {
    await updateAddress(editingId.value, payload)
    ElMessage.success('地址已更新')
  } else {
    await createAddress(payload)
    ElMessage.success('地址已新增')
  }
  dialogVisible.value = false
  await load()
}

async function onSetDefault(addr: Address) {
  await setDefaultAddress(addr.id)
  ElMessage.success('已设为默认地址')
  await load()
}

async function onDelete(addr: Address) {
  await ElMessageBox.confirm('确定删除该收货地址？', '删除地址', { type: 'warning' })
  await deleteAddress(addr.id)
  ElMessage.success('已删除')
  await load()
}

onMounted(load)
</script>

<template>
  <section class="section">
    <div class="container">
      <div class="page">
        <div class="page__head">
          <div>
            <h1 class="page__title">收货地址</h1>
            <p class="page__desc">用于邮寄订单的收货信息，可设置默认地址</p>
          </div>
          <el-button type="primary" round @click="openCreate">+ 新增地址</el-button>
        </div>

        <div v-loading="loading" class="page__body">
          <div v-if="addresses.length" class="addr">
            <div v-for="addr in addresses" :key="addr.id" class="addr__card">
              <div class="addr__top">
                <span class="addr__name">{{ addr.receiverName }}</span>
                <span class="addr__phone">{{ addr.phone }}</span>
                <span v-if="addr.isDefault" class="addr__badge">默认</span>
              </div>
              <p class="addr__detail">{{ addr.province }}{{ addr.city }}{{ addr.district }} {{ addr.detail }}</p>
              <div class="addr__actions">
                <el-button v-if="!addr.isDefault" link type="primary" @click="onSetDefault(addr)">设为默认</el-button>
                <el-button link type="primary" @click="openEdit(addr)">编辑</el-button>
                <el-button link type="danger" @click="onDelete(addr)">删除</el-button>
              </div>
            </div>
          </div>
          <div v-else-if="!loading" class="page__empty">
            <p>还没有收货地址</p>
            <el-button type="primary" round @click="openCreate">+ 新增地址</el-button>
          </div>
        </div>
      </div>
    </div>
  </section>

  <el-dialog v-model="dialogVisible" :title="editingId ? '编辑地址' : '新增地址'" width="520px">
    <el-form label-width="80px">
      <el-form-item label="收货人"><el-input v-model="form.receiverName" placeholder="姓名" /></el-form-item>
      <el-form-item label="电话"><el-input v-model="form.phone" placeholder="手机号" /></el-form-item>
      <el-form-item label="省份"><el-input v-model="form.province" placeholder="如：浙江省" /></el-form-item>
      <el-form-item label="城市"><el-input v-model="form.city" placeholder="如：杭州市" /></el-form-item>
      <el-form-item label="区县"><el-input v-model="form.district" placeholder="如：西湖区" /></el-form-item>
      <el-form-item label="详细地址"><el-input v-model="form.detail" placeholder="街道、门牌号等" /></el-form-item>
      <el-form-item label="默认地址"><el-switch v-model="form.isDefault" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="submit">保存</el-button>
    </template>
  </el-dialog>
</template>

<style scoped lang="scss">
.page {
  max-width: 760px;
  margin: 0 auto;

  &__head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: var(--sp-5);
  }

  &__title {
    font-family: var(--font-serif);
    font-size: 1.5rem;
    font-weight: 700;
  }

  &__desc {
    margin-top: 4px;
    font-size: 0.85rem;
    color: var(--muted);
  }

  &__body {
    min-height: 120px;
  }

  &__empty {
    text-align: center;
    padding: var(--sp-7) 0;
    color: var(--muted);
    display: grid;
    gap: var(--sp-4);
    justify-items: center;
  }
}

.addr {
  display: grid;
  gap: var(--sp-3);

  &__card {
    padding: var(--sp-4) var(--sp-5);
    background: var(--card);
    border: 1px solid var(--line);
    border-radius: var(--radius);
    transition: box-shadow 0.2s ease;

    &:hover {
      box-shadow: var(--shadow-card);
    }
  }

  &__top {
    display: flex;
    align-items: center;
    gap: var(--sp-3);
  }

  &__name {
    font-weight: 600;
  }

  &__phone {
    color: var(--muted);
    font-size: 0.9rem;
  }

  &__badge {
    padding: 2px 10px;
    border-radius: 999px;
    background: var(--mark);
    color: #fff;
    font-size: 0.75rem;
  }

  &__detail {
    margin-top: var(--sp-2);
    font-size: 0.9rem;
    color: var(--muted);
  }

  &__actions {
    margin-top: var(--sp-2);
    display: flex;
    gap: var(--sp-2);
  }
}
</style>
