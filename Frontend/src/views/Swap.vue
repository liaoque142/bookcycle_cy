<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fetchSwaps, acceptSwap, rejectSwap, cancelSwap, shipSwap, confirmSwap } from '@/api/swap'
import { fetchAddresses } from '@/api/address'
import { useAuthStore } from '@/stores/auth'
import type { Swap } from '@/types/swap'
import type { Address } from '@/types/address'

const authStore = useAuthStore()
const myId = computed(() => authStore.user?.id)

const role = ref<'initiator' | 'receiver'>('initiator')
const statusFilter = ref<number>()
const swaps = ref<Swap[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const loading = ref(true)

// 同意弹窗（接收方）
const acceptRef = ref<Swap | null>(null)
const acceptShipType = ref(1)
const addresses = ref<Address[]>([])
const acceptAddressId = ref<number>()
const accepting = ref(false)

const acceptVisible = computed({
  get: () => !!acceptRef.value,
  set: (v: boolean) => {
    if (!v) acceptRef.value = null
  },
})

const statusOptions = [
  { label: '全部', value: undefined },
  { label: '待对方同意', value: 1 },
  { label: '已同意待发货', value: 2 },
  { label: '交换中', value: 3 },
  { label: '已完成', value: 4 },
  { label: '已取消', value: 5 },
  { label: '已拒绝', value: 6 },
]

async function load() {
  loading.value = true
  try {
    const result = await fetchSwaps({ role: role.value, status: statusFilter.value, page: page.value, size: size.value })
    swaps.value = result.list
    total.value = result.total
  } finally {
    loading.value = false
  }
}

// ---- 视角助手 ----
function isInitiator(s: Swap): boolean {
  return s.initiatorId === myId.value
}
function myShipped(s: Swap): boolean {
  return isInitiator(s) ? !!s.initiatorShippedAt : !!s.receiverShippedAt
}
function myConfirmed(s: Swap): boolean {
  return isInitiator(s) ? !!s.initiatorConfirmedAt : !!s.receiverConfirmedAt
}
function otherShipped(s: Swap): boolean {
  return isInitiator(s) ? !!s.receiverShippedAt : !!s.initiatorShippedAt
}
function counterparty(s: Swap): string {
  return isInitiator(s) ? s.receiverName : s.initiatorName
}
/** 我收书的地址（发起人收书 A / 接收方收书 B） */
function myAddress(s: Swap): string {
  const mine = isInitiator(s) ? s.initiatorReceiverAddress : s.receiverReceiverAddress
  if (!mine) return ''
  const name = isInitiator(s) ? s.initiatorReceiverName : s.receiverReceiverName
  const phone = isInitiator(s) ? s.initiatorReceiverPhone : s.receiverReceiverPhone
  return `${name} ${phone} · ${mine}`
}
function myShipType(s: Swap): string {
  return isInitiator(s) ? (s.initiatorShipType || '') : (s.receiverShipType || '')
}

/** 当前角色/状态下的可用动作 */
function actionsOf(s: Swap): { key: string; label: string; danger?: boolean }[] {
  const list: { key: string; label: string; danger?: boolean }[] = []
  if (s.statusCode === 1) {
    if (role.value === 'receiver') list.push({ key: 'accept', label: '同意交换' }, { key: 'reject', label: '拒绝', danger: true })
    if (!myShipped(s)) list.push({ key: 'cancel', label: '取消', danger: true })
  } else if (s.statusCode === 2) {
    if (!myShipped(s)) {
      list.push({ key: 'ship', label: '发货' }, { key: 'cancel', label: '取消', danger: true })
    }
    if (otherShipped(s) && !myConfirmed(s)) list.push({ key: 'confirm', label: '确认收货' })
  } else if (s.statusCode === 3 && !myConfirmed(s)) {
    list.push({ key: 'confirm', label: '确认收货' })
  }
  return list
}

async function run(s: Swap, key: string) {
  if (key === 'accept') {
    acceptShipType.value = 1
    acceptAddressId.value = undefined
    try {
      addresses.value = await fetchAddresses()
      acceptAddressId.value = addresses.value.find((a) => a.isDefault)?.id
    } catch {
      addresses.value = []
    }
    acceptRef.value = s
  } else if (key === 'reject') {
    try {
      await ElMessageBox.confirm('拒绝后双方书籍将重新上架，确认拒绝该交换？', '拒绝交换', { type: 'warning' })
      await rejectSwap(s.swapNo)
      ElMessage.success('已拒绝交换')
      await load()
    } catch (e) {
      if (e !== 'cancel' && e !== 'close') ElMessage.error((e as Error).message || '操作失败')
    }
  } else if (key === 'cancel') {
    try {
      const { value } = await ElMessageBox.prompt('取消后双方书籍将重新上架', '取消交换', {
        inputPlaceholder: '取消原因（可留空）',
        confirmButtonText: '确认取消',
        type: 'warning',
      })
      await cancelSwap(s.swapNo, value ?? undefined)
      ElMessage.success('已取消交换')
      await load()
    } catch (e) {
      if (e !== 'cancel' && e !== 'close') ElMessage.error((e as Error).message || '操作失败')
    }
  } else if (key === 'ship') {
    await shipSwap(s.swapNo)
    ElMessage.success('已发货')
    await load()
  } else if (key === 'confirm') {
    await confirmSwap(s.swapNo)
    ElMessage.success('已确认收货，交换完成')
    await load()
  }
}

async function doAccept() {
  if (!acceptRef.value) return
  if (acceptShipType.value === 1 && !acceptAddressId.value) {
    ElMessage.warning('请选择收货地址')
    return
  }
  accepting.value = true
  try {
    await acceptSwap(acceptRef.value.swapNo, { shipType: acceptShipType.value, addressId: acceptAddressId.value })
    ElMessage.success('已同意交换，请等待双方发货')
    acceptRef.value = null
    await load()
  } catch (e) {
    ElMessage.error((e as Error).message || '操作失败')
  } finally {
    accepting.value = false
  }
}

function onPageChange(p: number) {
  page.value = p
  load()
}

watch([role, statusFilter], () => {
  page.value = 1
  load()
})

onMounted(load)
</script>

<template>
  <section class="section">
    <div class="container">
      <div class="page">
        <div class="page__head">
          <h1 class="page__title">我的换书</h1>
        </div>

        <el-tabs v-model="role" class="page__tabs">
          <el-tab-pane label="我发起的" name="initiator" />
          <el-tab-pane label="我收到的" name="receiver" />
        </el-tabs>

        <div class="page__filter">
          <el-select v-model="statusFilter" placeholder="全部状态" clearable style="width: 160px">
            <el-option v-for="s in statusOptions" :key="String(s.value)" :label="s.label" :value="s.value" />
          </el-select>
        </div>

        <div v-loading="loading" class="page__body">
          <div v-if="swaps.length" class="swap">
            <div v-for="s in swaps" :key="s.id" class="swap__card">
              <div class="swap__row">
                <div class="swap__main">
                  <p class="swap__title">《{{ s.offerBookTitle }}》 换 《{{ s.bookTitle }}》</p>
                  <p class="swap__meta">
                    <span>{{ s.offerBookAuthor }} ⇄ {{ s.bookAuthor }}</span>
                    <span>对方：{{ counterparty(s) }}</span>
                    <span v-if="s.message">附言：{{ s.message }}</span>
                  </p>
                  <p v-if="myShipType(s) && s.statusCode !== 5" class="swap__addr">
                    我收书：{{ myShipType(s) }}<template v-if="myShipType(s) === '邮寄'"> · {{ myAddress(s) }}</template>
                  </p>
                  <p v-if="s.statusCode === 5 && s.cancelReason" class="swap__addr">取消原因：{{ s.cancelReason }}</p>
                  <p v-if="s.statusCode === 6" class="swap__addr">对方拒绝后，双方书籍已重新上架</p>
                  <p class="swap__time">发起于 {{ s.createTime }}</p>
                </div>

                <div class="swap__side">
                  <span class="swap__status" :class="`swap__status--${s.statusCode}`">{{ s.status }}</span>
                  <div class="swap__actions">
                    <el-button
                      v-for="a in actionsOf(s)"
                      :key="a.key"
                      size="small"
                      :type="a.danger ? 'danger' : 'primary'"
                      plain
                      round
                      @click="run(s, a.key)"
                    >
                      {{ a.label }}
                    </el-button>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div v-else-if="!loading" class="page__empty">
            <p>暂无换书记录</p>
            <router-link to="/books" class="page__empty-link">去书市看看</router-link>
          </div>

          <div v-if="total > size" class="page__pager">
            <el-pagination layout="prev, pager, next" :total="total" :page-size="size" :current-page="page" @current-change="onPageChange" />
          </div>
        </div>
      </div>
    </div>
  </section>

  <!-- 同意交换弹窗：接收方填收书地址 -->
  <el-dialog v-model="acceptVisible" title="同意交换" width="520px" :close-on-click-modal="false">
    <div v-if="acceptRef" class="accept">
      <p class="accept__book">你将收到《{{ acceptRef.bookTitle }}》，并寄出《{{ acceptRef.offerBookTitle }}》</p>
      <el-radio-group v-model="acceptShipType" class="accept__ship">
        <el-radio-button :value="1">邮寄</el-radio-button>
        <el-radio-button :value="2">自提</el-radio-button>
      </el-radio-group>
      <div v-if="acceptShipType === 1" class="accept__addr">
        <el-select v-model="acceptAddressId" placeholder="请选择收货地址" style="width: 100%">
          <el-option
            v-for="a in addresses"
            :key="a.id"
            :value="a.id"
            :label="`${a.receiverName} ${a.phone} · ${a.province}${a.city}${a.district} ${a.detail}`"
          />
        </el-select>
        <p class="accept__manage">
          <router-link to="/addresses">管理收货地址</router-link>
        </p>
      </div>
      <p class="accept__tip">同意后需双方各自发货、确认收货完成交换</p>
    </div>
    <template #footer>
      <el-button @click="acceptRef = null">取消</el-button>
      <el-button type="primary" :loading="accepting" @click="doAccept">同意交换</el-button>
    </template>
  </el-dialog>
</template>

<style scoped lang="scss">
.page {
  max-width: 860px;
  margin: 0 auto;

  &__head {
    margin-bottom: var(--sp-3);
  }

  &__title {
    font-family: var(--font-serif);
    font-size: 1.5rem;
    font-weight: 700;
  }

  &__tabs {
    margin-bottom: var(--sp-2);
  }

  &__filter {
    margin-bottom: var(--sp-4);
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

    &-link {
      color: var(--mark);
    }
  }

  &__pager {
    display: flex;
    justify-content: center;
    margin-top: var(--sp-5);
  }
}

.swap {
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

  &__row {
    display: flex;
    gap: var(--sp-4);
    justify-content: space-between;
  }

  &__title {
    font-family: var(--font-serif);
    font-size: 1.05rem;
    font-weight: 600;
  }

  &__meta {
    margin-top: var(--sp-2);
    display: flex;
    gap: var(--sp-3);
    font-size: 0.85rem;
    color: var(--muted);
    flex-wrap: wrap;
  }

  &__addr {
    margin-top: var(--sp-2);
    font-size: 0.82rem;
    color: var(--muted);
  }

  &__time {
    margin-top: var(--sp-2);
    font-size: 0.78rem;
    color: var(--muted);
  }

  &__side {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    gap: var(--sp-3);
    flex-shrink: 0;
  }

  &__status {
    padding: 3px 12px;
    border-radius: 999px;
    font-size: 0.8rem;
    border: 1px solid var(--line);
    background: var(--paper);
    color: var(--muted);

    &--1 {
      background: var(--ink);
      border-color: var(--ink);
      color: #fff;
      font-weight: 600;
    }

    &--2 {
      border-color: var(--mark);
      color: var(--mark);
    }

    &--4 {
      border-color: var(--mark);
      color: var(--mark);
      font-weight: 600;
    }

    &--5 {
      background: var(--paper);
      color: var(--muted);
    }
  }

  &__actions {
    display: flex;
    gap: var(--sp-2);
  }
}

.accept {
  &__book {
    font-family: var(--font-serif);
    font-weight: 600;
    font-size: 1.05rem;
    margin-bottom: var(--sp-4);
  }

  &__ship {
    display: flex;
    gap: var(--sp-2);
  }

  &__addr {
    margin-top: var(--sp-4);
  }

  &__manage {
    margin-top: var(--sp-2);
    font-size: 0.82rem;

    a {
      color: var(--mark);
    }
  }

  &__tip {
    margin-top: var(--sp-4);
    font-size: 0.8rem;
    color: var(--muted);
  }
}
</style>
