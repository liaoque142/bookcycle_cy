<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fetchOrders, payOrder, cancelOrder, shipOrder, confirmOrder, evaluateOrder, createRefund } from '@/api/order'
import { fetchCreditMe } from '@/api/credit'
import { useAuthStore } from '@/stores/auth'
import type { Order } from '@/types/trade'

const authStore = useAuthStore()

const role = ref<'buyer' | 'seller'>('buyer')
const statusFilter = ref<number>()
const orders = ref<Order[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const loading = ref(true)

// 支付弹窗
const payOrderRef = ref<Order | null>(null)
const paying = ref(false)
const payVisible = ref(false)

// 评价弹窗
const evalOrderRef = ref<Order | null>(null)
const evalScore = ref(5)
const evalContent = ref('')
const evaluating = ref(false)
const evalVisible = ref(false)

// 售后弹窗
const refundOrderRef = ref<Order | null>(null)
const refundReason = ref('')
const refunding = ref(false)
const refundVisible = ref(false)

const statusOptions = [
  { label: '全部', value: undefined },
  { label: '待支付', value: 1 },
  { label: '待发货', value: 2 },
  { label: '待收货', value: 3 },
  { label: '已完成', value: 4 },
  { label: '已取消', value: 5 },
]

async function load() {
  loading.value = true
  try {
    const result = await fetchOrders({ role: role.value, status: statusFilter.value, page: page.value, size: size.value })
    orders.value = result.list
    total.value = result.total
  } finally {
    loading.value = false
  }
}

/** 交易相关动作可能改变信誉分，动作后刷新 store 中的信誉值 */
async function refreshCredit() {
  try {
    const credit = await fetchCreditMe()
    if (authStore.user) authStore.user.credit = credit
  } catch {
    /* 忽略，非关键 */
  }
}

function counterparty(o: Order): string {
  return role.value === 'buyer' ? o.sellerName : o.buyerName
}

function amountLabel(o: Order): string {
  if (o.tradeType === '赠送') return '免费'
  return `¥${o.amount}`
}

/** 当前角色/状态下的可用动作 */
function actionsOf(o: Order): { key: string; label: string; danger?: boolean }[] {
  const asBuyer = role.value === 'buyer'
  // 售后申请中/处理中时，后端会拦截 ship/confirm/cancel，隐藏会报错的操作
  const afterSaleOn = o.afterSale != null && o.afterSale !== 0
  // 仅退款单（赠送金额 0）无退款金额，后端拒绝，不显示售后按钮
  const refundable = asBuyer && (o.amount ?? 0) > 0 && !afterSaleOn
  const list: { key: string; label: string; danger?: boolean }[] = []
  if (o.statusCode === 1) {
    if (asBuyer) list.push({ key: 'pay', label: '去支付' })
    list.push({ key: 'cancel', label: '取消订单', danger: true })
  } else if (o.statusCode === 2) {
    if (asBuyer) {
      if (refundable) list.push({ key: 'refund', label: '申请退款' })
      if (!afterSaleOn) list.push({ key: 'cancel', label: '取消订单', danger: true })
    } else list.push({ key: 'ship', label: '发货' })
  } else if (o.statusCode === 3 && asBuyer) {
    if (!afterSaleOn) list.push({ key: 'confirm', label: '确认收货' })
    if (refundable) list.push({ key: 'refund', label: '申请售后' })
  } else if (o.statusCode === 4) {
    if (!o.evaluatedByMe) list.push({ key: 'evaluate', label: '评价' })
    if (refundable) list.push({ key: 'refund', label: '申请售后' })
  }
  return list
}

async function run(o: Order, key: string) {
  if (key === 'pay') {
    payOrderRef.value = o
    payVisible.value = true
  } else if (key === 'cancel') {
    try {
      const { value } = await ElMessageBox.prompt('取消后书籍将重新上架；已支付订单自动退款', '取消订单', {
        inputPlaceholder: '取消原因（可留空）',
        confirmButtonText: '确认取消',
        type: 'warning',
      })
      await cancelOrder(o.orderNo, { reason: value ?? undefined })
      ElMessage.success('订单已取消')
      await load()
      await refreshCredit()
    } catch (e) {
      if (e !== 'cancel' && e !== 'close') ElMessage.error((e as Error).message || '取消失败')
    }
  } else if (key === 'ship') {
    await shipOrder(o.orderNo)
    ElMessage.success('已发货')
    await load()
  } else if (key === 'confirm') {
    await confirmOrder(o.orderNo)
    ElMessage.success('已确认收货，交易完成')
    await load()
    await refreshCredit()
  } else if (key === 'evaluate') {
    evalOrderRef.value = o
    evalScore.value = 5
    evalContent.value = ''
    evalVisible.value = true
  } else if (key === 'refund') {
    refundOrderRef.value = o
    refundReason.value = ''
    refundVisible.value = true
  }
}

async function doPay() {
  if (!payOrderRef.value) return
  paying.value = true
  try {
    await payOrder(payOrderRef.value.orderNo)
    ElMessage.success('支付成功')
    payVisible.value = false
    payOrderRef.value = null
    await load()
  } catch (e) {
    ElMessage.error((e as Error).message || '支付失败')
  } finally {
    paying.value = false
  }
}

async function doEvaluate() {
  if (!evalOrderRef.value) return
  evaluating.value = true
  try {
    await evaluateOrder(evalOrderRef.value.orderNo, {
      score: evalScore.value,
      content: evalContent.value.trim() || undefined,
    })
    ElMessage.success('评价成功')
    evalVisible.value = false
    evalOrderRef.value = null
    await load()
    await refreshCredit()
  } catch (e) {
    ElMessage.error((e as Error).message || '评价失败')
  } finally {
    evaluating.value = false
  }
}

/** 提交售后申请：待发货仅退款(type 1)，待收货/已完成退货退款(type 2) */
async function doRefund() {
  if (!refundOrderRef.value) return
  refunding.value = true
  try {
    const type = refundOrderRef.value.statusCode === 2 ? 1 : 2
    await createRefund(refundOrderRef.value.orderNo, {
      type,
      reason: refundReason.value.trim() || undefined,
    })
    ElMessage.success('售后申请已提交，等待平台处理')
    refundVisible.value = false
    refundOrderRef.value = null
    await load()
  } catch (e) {
    ElMessage.error((e as Error).message || '提交失败')
  } finally {
    refunding.value = false
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

onMounted(async () => {
  await load()
  await refreshCredit()
})
</script>

<template>
  <section class="section">
    <div class="container">
      <div class="page">
        <div class="page__head">
          <h1 class="page__title">我的订单</h1>
        </div>

        <el-tabs v-model="role" class="page__tabs">
          <el-tab-pane label="我买到的" name="buyer" />
          <el-tab-pane label="我卖出的" name="seller" />
        </el-tabs>

        <div class="page__filter">
          <el-select v-model="statusFilter" placeholder="全部状态" clearable style="width: 160px">
            <el-option v-for="s in statusOptions" :key="String(s.value)" :label="s.label" :value="s.value" />
          </el-select>
        </div>

        <div v-loading="loading" class="page__body">
          <div v-if="orders.length" class="order">
            <div v-for="o in orders" :key="o.id" class="order__card">
              <div class="order__row">
                <div class="order__main">
                  <p class="order__title">{{ o.bookTitle }}</p>
                  <p class="order__meta">
                    <span>{{ o.tradeType }}</span>
                    <span>{{ o.shipType }}</span>
                    <span class="order__amount">{{ amountLabel(o) }}</span>
                    <span>{{ role === 'buyer' ? '卖家' : '买家' }}：{{ counterparty(o) }}</span>
                  </p>
                  <p v-if="o.shipType === '邮寄' && o.statusCode !== 5" class="order__addr">
                    收件：{{ o.receiverName }} {{ o.receiverPhone }} · {{ o.receiverAddress }}
                  </p>
                  <p v-if="o.statusCode === 5 && o.cancelReason" class="order__addr">取消原因：{{ o.cancelReason }}</p>
                  <p class="order__time">下单于 {{ o.createTime }}</p>
                </div>

                <div class="order__side">
                  <span class="order__status" :class="`order__status--${o.statusCode}`">{{ o.afterSale ? '售后处理中' : o.status }}</span>
                  <div class="order__actions">
                    <el-button
                      v-for="a in actionsOf(o)"
                      :key="a.key"
                      size="small"
                      :type="a.danger ? 'danger' : 'primary'"
                      plain
                      round
                      @click="run(o, a.key)"
                    >
                      {{ a.label }}
                    </el-button>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div v-else-if="!loading" class="page__empty">
            <p>暂无订单</p>
            <router-link to="/books" class="page__empty-link">去书市逛逛</router-link>
          </div>

          <div v-if="total > size" class="page__pager">
            <el-pagination layout="prev, pager, next" :total="total" :page-size="size" :current-page="page" @current-change="onPageChange" />
          </div>
        </div>
      </div>
    </div>
  </section>

  <!-- 支付弹窗：展示卖家收款码，买家扫码转账（一期模拟支付，预留支付宝/微信） -->
  <el-dialog v-model="payVisible" title="扫码支付" width="420px" :close-on-click-modal="false" @closed="payOrderRef = null">
    <div v-if="payOrderRef" class="pay">
      <p class="pay__book">{{ payOrderRef.bookTitle }}</p>
      <p class="pay__amount">需支付 ¥{{ payOrderRef.amount }}</p>
      <p class="pay__to">向卖家「{{ payOrderRef.sellerName }}」扫码转账</p>
      <div v-if="payOrderRef.sellerPayQr" class="pay__qr">
        <el-image
          :src="payOrderRef.sellerPayQr"
          :preview-src-list="[payOrderRef.sellerPayQr]"
          preview-teleported
          fit="contain"
          class="pay__qr-img"
        />
        <p class="pay__hint">截图或扫码向卖家转账，完成后再确认</p>
      </div>
      <p v-else class="pay__tip">卖家未设置收款码，请先与卖家沟通付款方式</p>
    </div>
    <template #footer>
      <el-button @click="payVisible = false; payOrderRef = null">暂不支付</el-button>
      <el-button type="primary" :loading="paying" @click="doPay">我已转账支付</el-button>
    </template>
  </el-dialog>

  <!-- 评价弹窗 -->
  <el-dialog v-model="evalVisible" title="交易评价" width="460px" :close-on-click-modal="false" @closed="evalOrderRef = null">
    <div v-if="evalOrderRef" class="eval">
      <p class="eval__book">{{ evalOrderRef.bookTitle }}</p>
      <el-rate v-model="evalScore" :max="5" show-score score-template="{value} 分" />
      <el-input
        v-model="evalContent"
        type="textarea"
        :rows="3"
        maxlength="500"
        show-word-limit
        placeholder="评价内容（可留空）"
        class="eval__input"
      />
      <p class="eval__tip">好评（≥4 分）将给被评方增加信誉分</p>
    </div>
    <template #footer>
      <el-button @click="evalVisible = false; evalOrderRef = null">取消</el-button>
      <el-button type="primary" :loading="evaluating" @click="doEvaluate">提交评价</el-button>
    </template>
  </el-dialog>

  <!-- 售后弹窗：待发货仅退款 / 待收货、已完成退货退款 -->
  <el-dialog v-model="refundVisible" title="申请售后" width="440px" :close-on-click-modal="false" @closed="refundOrderRef = null">
    <div v-if="refundOrderRef" class="refund">
      <p class="refund__book">{{ refundOrderRef.bookTitle }}</p>
      <p class="refund__amount">交易金额 ¥{{ refundOrderRef.amount }}</p>
      <p class="refund__type">
        {{ refundOrderRef.statusCode === 2 ? '仅退款（订单待发货，未收到书籍）' : '退货退款（需寄回书籍）' }}
      </p>
      <el-input
        v-model="refundReason"
        type="textarea"
        :rows="3"
        maxlength="255"
        show-word-limit
        placeholder="售后原因（可留空，建议说明便于平台处理）"
        class="refund__input"
      />
    </div>
    <template #footer>
      <el-button @click="refundVisible = false; refundOrderRef = null">取消</el-button>
      <el-button type="primary" :loading="refunding" @click="doRefund">提交申请</el-button>
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

.order {
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

  &__amount {
    color: var(--mark);
    font-weight: 600;
    font-variant-numeric: tabular-nums;
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

.pay {
  text-align: center;
  padding: var(--sp-2) 0 var(--sp-4);

  &__book {
    color: var(--muted);
    font-size: 0.9rem;
  }

  &__amount {
    margin-top: var(--sp-3);
    font-family: var(--font-serif);
    font-size: 2rem;
    font-weight: 700;
    color: var(--mark);
  }

  &__tip {
    margin-top: var(--sp-3);
    font-size: 0.8rem;
    color: var(--muted);
  }

  &__to {
    margin-top: var(--sp-3);
    font-size: 0.9rem;
    color: var(--ink);
  }

  &__qr {
    margin-top: var(--sp-3);
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: var(--sp-2);

    &-img {
      width: 180px;
      height: 180px;
      border-radius: var(--radius-sm);
      border: 1px solid var(--line);
      background: #fff;
      cursor: zoom-in;
    }
  }

  &__hint {
    font-size: 0.8rem;
    color: var(--muted);
  }
}

.eval {
  text-align: center;
  padding: var(--sp-2) 0;

  &__book {
    color: var(--muted);
    font-size: 0.9rem;
    margin-bottom: var(--sp-4);
  }

  &__input {
    margin-top: var(--sp-4);
    text-align: left;
  }

  &__tip {
    margin-top: var(--sp-2);
    font-size: 0.8rem;
    color: var(--muted);
    text-align: left;
  }
}

.refund {
  text-align: center;
  padding: var(--sp-2) 0;

  &__book {
    color: var(--muted);
    font-size: 0.9rem;
  }

  &__amount {
    margin-top: var(--sp-3);
    font-family: var(--font-serif);
    font-size: 1.5rem;
    font-weight: 700;
    color: var(--mark);
  }

  &__type {
    margin-top: var(--sp-2);
    font-size: 0.85rem;
    color: var(--ink);
  }

  &__input {
    margin-top: var(--sp-4);
    text-align: left;
  }
}
</style>
