<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Star, StarFilled } from '@element-plus/icons-vue'
import BookCover from '@/components/common/BookCover.vue'
import { fetchBookById } from '@/api/book'
import { fetchAddresses } from '@/api/address'
import { createOrder, payOrder } from '@/api/order'
import { useAuthStore } from '@/stores/auth'
import type { Book } from '@/types/book'
import type { Address } from '@/types/address'
import type { Order } from '@/types/trade'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const book = ref<Book | null>(null)
const loading = ref(true)
const notFound = ref(false)
const favorite = ref(false)

const priceLabel = computed(() => {
  if (!book.value) return ''
  if (book.value.price !== null) return `¥${book.value.price}`
  if (book.value.tradeType === '交换') return '交换'
  return '赠送'
})

/** 上架时间：后端 LocalDateTime 为 ISO 串，只展示日期部分 */
const createdLabel = computed(() => (book.value?.createdAt || '').slice(0, 10))

// ---- 下单流（出售/赠送；交换二期） ----
const orderVisible = ref(false)
const shipType = ref(1) // 1 邮寄 / 2 自提
const addresses = ref<Address[]>([])
const addressId = ref<number>()
const creating = ref(false)
const payVisible = ref(false)
const pendingPay = ref<Order | null>(null)
const paying = ref(false)

function onBuy() {
  if (book.value?.tradeType === '交换') {
    ElMessage.info('交换功能二期上线，敬请期待')
    return
  }
  if (!authStore.isLoggedIn) {
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  orderVisible.value = true
}

/** 打开下单弹窗时预载地址（邮寄需要） */
async function onOrderOpen() {
  shipType.value = 1
  addressId.value = undefined
  try {
    addresses.value = await fetchAddresses()
    addressId.value = addresses.value.find((a) => a.isDefault)?.id
  } catch {
    addresses.value = []
  }
}

async function submitOrder() {
  if (!book.value) return
  if (shipType.value === 1 && !addressId.value) {
    ElMessage.warning('请选择收货地址')
    return
  }
  creating.value = true
  try {
    const order = await createOrder({ bookId: book.value.id, shipType: shipType.value, addressId: addressId.value })
    orderVisible.value = false
    if (order.amount > 0) {
      pendingPay.value = order
      payVisible.value = true
    } else {
      ElMessage.success('下单成功')
      router.push('/orders')
    }
  } catch (e) {
    ElMessage.error((e as Error).message || '下单失败')
  } finally {
    creating.value = false
  }
}

async function doPay() {
  if (!pendingPay.value) return
  paying.value = true
  try {
    await payOrder(pendingPay.value.orderNo)
    ElMessage.success('支付成功')
    payVisible.value = false
    router.push('/orders')
  } catch (e) {
    ElMessage.error((e as Error).message || '支付失败')
  } finally {
    paying.value = false
  }
}

function onFavorite() {
  favorite.value = !favorite.value
  ElMessage.success(favorite.value ? '已加入收藏' : '已取消收藏')
}

onMounted(async () => {
  const id = Number(route.params.id)
  try {
    book.value = await fetchBookById(id)
  } catch {
    notFound.value = true
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <section class="section">
    <div class="container">
      <div v-loading="loading" class="detail">
        <div v-if="book" class="detail__inner">
          <div class="detail__left">
            <div class="detail__cover">
              <BookCover :book="book" height="100%" />
            </div>
          </div>

          <div class="detail__right">
            <nav class="detail__crumb">
              <router-link to="/books" class="detail__crumb-link">书市</router-link>
              <span class="detail__crumb-sep">/</span>
              <span class="detail__crumb-current">{{ book.title }}</span>
            </nav>

            <h1 class="detail__title">{{ book.title }}</h1>
            <p class="detail__author">{{ book.author }}</p>

            <div class="detail__badges">
              <span class="detail__badge detail__badge--type">{{ book.tradeType }}</span>
              <span class="detail__badge">{{ book.condition }}</span>
              <span class="detail__badge">{{ book.category }}</span>
            </div>

            <p class="detail__price">{{ priceLabel }}</p>

            <dl class="detail__attrs">
              <div v-if="book.publisher" class="detail__attr">
                <dt>出版社</dt>
                <dd>{{ book.publisher }}</dd>
              </div>
              <div v-if="book.isbn" class="detail__attr">
                <dt>ISBN</dt>
                <dd>{{ book.isbn }}</dd>
              </div>
              <div class="detail__attr">
                <dt>上架时间</dt>
                <dd>{{ createdLabel }}</dd>
              </div>
            </dl>

            <p class="detail__desc">{{ book.description }}</p>

            <div class="detail__actions">
              <el-button v-if="book.tradeType !== '交换'" type="primary" round size="large" @click="onBuy">
                {{ book.tradeType === '赠送' ? '免费领取' : '立即购买' }}
              </el-button>
              <el-button v-else type="primary" round size="large" @click="onBuy">交换（二期）</el-button>
              <el-button round size="large" @click="onFavorite">
                <el-icon class="detail__fav" :class="{ 'detail__fav--on': favorite }">
                  <component :is="favorite ? StarFilled : Star" />
                </el-icon>
                {{ favorite ? '已收藏' : '收藏' }}
              </el-button>
            </div>

            <div class="detail__seller">
              <span class="detail__seller-avatar">{{ book.seller.slice(0, 1) }}</span>
              <div>
                <p class="detail__seller-name">{{ book.seller }}</p>
                <p class="detail__seller-credit">信誉 {{ book.credit }}</p>
              </div>
            </div>
          </div>
        </div>

        <div v-else-if="notFound" class="detail__none">
          <h2 class="detail__none-title">书籍不存在或已下架</h2>
          <router-link to="/books" class="detail__none-back">返回书市</router-link>
        </div>
      </div>
    </div>
  </section>

  <!-- 下单弹窗：物流方式 + 邮寄地址 -->
  <el-dialog v-model="orderVisible" title="确认下单" width="520px" :close-on-click-modal="false" @open="onOrderOpen">
    <div v-if="book" class="order">
      <p class="order__book">{{ book.title }}</p>
      <p class="order__amount">{{ priceLabel }}</p>
      <el-radio-group v-model="shipType" class="order__ship">
        <el-radio-button :value="1">邮寄</el-radio-button>
        <el-radio-button :value="2">自提</el-radio-button>
      </el-radio-group>
      <div v-if="shipType === 1" class="order__addr">
        <el-select v-model="addressId" placeholder="请选择收货地址" style="width: 100%">
          <el-option
            v-for="a in addresses"
            :key="a.id"
            :value="a.id"
            :label="`${a.receiverName} ${a.phone} · ${a.province}${a.city}${a.district} ${a.detail}`"
          />
        </el-select>
        <p class="order__manage">
          <router-link to="/addresses">管理收货地址</router-link>
        </p>
      </div>
      <p class="order__tip">{{ book.tradeType === '赠送' ? '免费赠送，无需支付' : '提交后需完成模拟支付' }}</p>
    </div>
    <template #footer>
      <el-button @click="orderVisible = false">取消</el-button>
      <el-button type="primary" :loading="creating" @click="submitOrder">提交订单</el-button>
    </template>
  </el-dialog>

  <!-- 模拟支付弹窗 -->
  <el-dialog v-model="payVisible" title="模拟支付" width="420px" :close-on-click-modal="false">
    <div v-if="pendingPay" class="pay">
      <p class="pay__book">{{ pendingPay.bookTitle }}</p>
      <p class="pay__amount">需支付 ¥{{ pendingPay.amount }}</p>
      <p class="pay__tip">一期为模拟余额支付，预留支付宝/微信</p>
    </div>
    <template #footer>
      <el-button @click="payVisible = false">稍后支付</el-button>
      <el-button type="primary" :loading="paying" @click="doPay">模拟余额支付</el-button>
    </template>
  </el-dialog>
</template>

<style scoped lang="scss">
.detail {
  min-height: 300px;

  &__inner {
    display: grid;
    grid-template-columns: 360px 1fr;
    gap: var(--sp-7);
    align-items: start;
  }

  &__cover {
    aspect-ratio: 3 / 3.8;
    background: var(--paper);
    border: 1px solid var(--line);
    border-radius: var(--radius);
    padding: var(--sp-5);
  }

  &__crumb {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 0.85rem;
    color: var(--muted);
    margin-bottom: var(--sp-4);

    &-link:hover {
      color: var(--ink);
    }
  }

  &__title {
    font-family: var(--font-serif);
    font-size: 2rem;
    font-weight: 700;
    line-height: 1.4;
  }

  &__author {
    margin-top: 4px;
    font-size: 0.95rem;
    color: var(--muted);
  }

  &__badges {
    display: flex;
    gap: 8px;
    margin-top: var(--sp-4);
  }

  &__badge {
    padding: 3px 12px;
    border-radius: 999px;
    background: var(--paper);
    border: 1px solid var(--line);
    font-size: 0.8rem;
    color: var(--muted);

    &--type {
      background: var(--ink);
      border-color: var(--ink);
      color: #fff;
      font-weight: 600;
    }
  }

  &__price {
    margin-top: var(--sp-5);
    font-family: var(--font-serif);
    font-size: 2.2rem;
    font-weight: 700;
    color: var(--mark);
  }

  &__attrs {
    margin-top: var(--sp-5);
    padding: var(--sp-4) 0;
    border-top: 1px dashed var(--line);
    border-bottom: 1px dashed var(--line);
    display: grid;
    gap: var(--sp-2);
  }

  &__attr {
    display: flex;
    gap: var(--sp-3);
    font-size: 0.9rem;

    dt {
      width: 72px;
      color: var(--muted);
      flex-shrink: 0;
    }
  }

  &__desc {
    margin-top: var(--sp-5);
    line-height: 1.8;
    color: var(--ink);
  }

  &__actions {
    display: flex;
    gap: var(--sp-3);
    margin-top: var(--sp-6);
  }

  &__fav {
    &--on {
      color: var(--mark);
    }
  }

  &__seller {
    display: flex;
    align-items: center;
    gap: var(--sp-3);
    margin-top: var(--sp-6);
    padding: var(--sp-4);
    background: var(--card);
    border: 1px solid var(--line);
    border-radius: var(--radius);
  }

  &__seller-avatar {
    display: grid;
    place-items: center;
    width: 44px;
    height: 44px;
    border-radius: 50%;
    background: var(--mark);
    color: #fff;
    font-family: var(--font-serif);
    font-size: 1.1rem;
  }

  &__seller-name {
    font-weight: 600;
  }

  &__seller-credit {
    font-size: 0.82rem;
    color: var(--mark);
    margin-top: 2px;
  }

  &__none {
    text-align: center;
    padding: var(--sp-8) 0;

    &-title {
      font-family: var(--font-serif);
      font-size: 1.5rem;
      font-weight: 700;
    }

    &-back {
      display: inline-block;
      margin-top: var(--sp-5);
      padding: 10px 28px;
      border-radius: 999px;
      background: var(--ink);
      color: #fff;
    }
  }
}

.order {
  &__book {
    font-family: var(--font-serif);
    font-weight: 600;
    font-size: 1.05rem;
  }

  &__amount {
    margin: var(--sp-2) 0 var(--sp-4);
    font-family: var(--font-serif);
    font-size: 1.6rem;
    font-weight: 700;
    color: var(--mark);
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
}

@media (max-width: 860px) {
  .detail__inner {
    grid-template-columns: 1fr;
  }
}
</style>
