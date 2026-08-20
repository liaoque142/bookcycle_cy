<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Star, StarFilled } from '@element-plus/icons-vue'
import BookCover from '@/components/common/BookCover.vue'
import ImageUploader from '@/components/common/ImageUploader.vue'
import { fetchBookById, fetchMyBooks } from '@/api/book'
import { fetchAddresses } from '@/api/address'
import { createOrder, payOrder } from '@/api/order'
import { createSwap } from '@/api/swap'
import { fetchDiscussions, createDiscussion, deleteDiscussion } from '@/api/discussion'
import { useAuthStore } from '@/stores/auth'
import type { Book } from '@/types/book'
import type { Address } from '@/types/address'
import type { Order } from '@/types/trade'
import type { DiscussionItem } from '@/types/discussion'

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
  if (!authStore.isLoggedIn) {
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  orderVisible.value = true
}

// ---- 交换流（以书换书） ----
const swapVisible = ref(false)
const offerBooks = ref<Book[]>([])
const offerBookId = ref<number>()
const swapShipType = ref(1) // 1 邮寄 / 2 自提
const swapAddressId = ref<number>()
const swapMessage = ref('')
const swapping = ref(false)

function onSwap() {
  if (!authStore.isLoggedIn) {
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  swapVisible.value = true
}

/** 打开交换弹窗：预载我的在售书 + 地址 */
async function onSwapOpen() {
  swapShipType.value = 1
  offerBookId.value = undefined
  swapAddressId.value = undefined
  swapMessage.value = ''
  try {
    const [mine, addr] = await Promise.all([fetchMyBooks(1), fetchAddresses()])
    offerBooks.value = mine
    addresses.value = addr
    swapAddressId.value = addr.find((a) => a.isDefault)?.id
  } catch {
    offerBooks.value = []
    addresses.value = []
  }
}

async function submitSwap() {
  if (!book.value) return
  if (!offerBookId.value) {
    ElMessage.warning('请选择用于交换的书籍')
    return
  }
  if (swapShipType.value === 1 && !swapAddressId.value) {
    ElMessage.warning('请选择收货地址')
    return
  }
  swapping.value = true
  try {
    await createSwap({
      bookId: book.value.id,
      offerBookId: offerBookId.value,
      shipType: swapShipType.value,
      addressId: swapAddressId.value,
      message: swapMessage.value.trim() || undefined,
    })
    swapVisible.value = false
    ElMessage.success('交换申请已发出，等待对方同意')
    router.push('/swaps')
  } catch (e) {
    ElMessage.error((e as Error).message || '发起交换失败')
  } finally {
    swapping.value = false
  }
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

// ---- 书籍讨论区 ----
const discussions = ref<DiscussionItem[]>([])
const discLoading = ref(false)
const draft = ref('')
const draftImage = ref('')
const replyTo = ref<number | null>(null)
const replyDraft = ref('')
const replyImage = ref('')
const submitting = ref(false)

function currentUserId() {
  return authStore.isLoggedIn ? authStore.user?.id : undefined
}

/** 删除按钮：仅评论者本人可见（书籍作者后端同样允许） */
function canDelete(d: DiscussionItem) {
  return !!currentUserId() && d.userId === currentUserId()
}

async function loadDiscussions() {
  if (!book.value) return
  discLoading.value = true
  try {
    discussions.value = await fetchDiscussions(book.value.id)
  } finally {
    discLoading.value = false
  }
}

function requireLogin(): boolean {
  if (!authStore.isLoggedIn) {
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return false
  }
  return true
}

async function submitTop() {
  if (!book.value || !requireLogin()) return
  const content = draft.value.trim()
  const imageUrl = draftImage.value
  if (!content && !imageUrl) {
    ElMessage.warning('请输入评论内容')
    return
  }
  submitting.value = true
  try {
    await createDiscussion(book.value.id, { content, imageUrl })
    draft.value = ''
    draftImage.value = ''
    ElMessage.success('评论已发布')
    loadDiscussions()
  } catch (e) {
    ElMessage.error((e as Error).message || '发布失败')
  } finally {
    submitting.value = false
  }
}

function openReply(d: DiscussionItem) {
  if (!requireLogin()) return
  replyTo.value = replyTo.value === d.id ? null : d.id
  replyDraft.value = ''
  replyImage.value = ''
}

async function submitReply(d: DiscussionItem) {
  if (!book.value) return
  const content = replyDraft.value.trim()
  const imageUrl = replyImage.value
  if (!content && !imageUrl) {
    ElMessage.warning('请输入回复内容')
    return
  }
  submitting.value = true
  try {
    await createDiscussion(book.value.id, { content, imageUrl, parentId: d.id })
    replyTo.value = null
    replyDraft.value = ''
    replyImage.value = ''
    ElMessage.success('回复成功')
    loadDiscussions()
  } catch (e) {
    ElMessage.error((e as Error).message || '回复失败')
  } finally {
    submitting.value = false
  }
}

async function onDelete(d: DiscussionItem) {
  if (!book.value) return
  try {
    await deleteDiscussion(book.value.id, d.id)
    ElMessage.success('已删除')
    loadDiscussions()
  } catch (e) {
    ElMessage.error((e as Error).message || '删除失败')
  }
}

onMounted(async () => {
  const id = Number(route.params.id)
  try {
    book.value = await fetchBookById(id)
    loadDiscussions()
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
              <el-button v-else type="primary" round size="large" @click="onSwap">我要交换</el-button>
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

      <!-- 书籍讨论区 -->
      <div v-if="book && !notFound" class="discuss">
        <div class="discuss__head">
          <h2 class="discuss__title">书籍讨论</h2>
          <span class="discuss__count">{{ discussions.length }} 条</span>
        </div>

        <div class="discuss__editor">
          <el-input v-model="draft" type="textarea" :rows="3" maxlength="500" show-word-limit placeholder="写点什么，让书友更了解这本书…" />
          <div class="discuss__editor-foot">
            <ImageUploader v-model="draftImage" />
            <el-button type="primary" round :loading="submitting" @click="submitTop">发表评论</el-button>
          </div>
        </div>

        <div v-loading="discLoading" class="discuss__list">
          <div v-if="discussions.length" class="discuss__items">
            <div v-for="d in discussions" :key="d.id" class="discuss__item">
              <div class="discuss__item-head">
                <span class="discuss__author">{{ d.author }}</span>
                <span class="discuss__time">{{ d.createTime.slice(0, 10) }}</span>
                <el-button v-if="canDelete(d)" class="discuss__del" size="small" text type="danger" @click="onDelete(d)">删除</el-button>
              </div>
              <div class="discuss__body">
                <img v-if="d.imageUrl" :src="d.imageUrl" class="discuss__img" alt="图片" />
                <p v-if="d.content" class="discuss__content">{{ d.content }}</p>
              </div>
              <button class="discuss__reply-btn" @click="openReply(d)">{{ replyTo === d.id ? '收起回复' : '回复' }}</button>

              <div v-if="replyTo === d.id" class="discuss__reply-editor">
                <el-input v-model="replyDraft" type="textarea" :rows="2" maxlength="500" :placeholder="`回复 @${d.author}`" />
                <div class="discuss__reply-foot">
                  <ImageUploader v-model="replyImage" />
                  <el-button type="primary" size="small" round :loading="submitting" @click="submitReply(d)">回复</el-button>
                </div>
              </div>

              <div v-if="d.replies.length" class="discuss__replies">
                <div v-for="r in d.replies" :key="r.id" class="discuss__reply">
                  <div class="discuss__reply-head">
                    <span class="discuss__author">{{ r.author }}</span>
                    <span class="discuss__time">{{ r.createTime.slice(0, 10) }}</span>
                    <el-button v-if="canDelete(r)" class="discuss__del" size="small" text type="danger" @click="onDelete(r)">删除</el-button>
                  </div>
                  <div class="discuss__body">
                    <img v-if="r.imageUrl" :src="r.imageUrl" class="discuss__img" alt="图片" />
                    <p v-if="r.content" class="discuss__content discuss__content--reply">{{ r.content }}</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div v-else-if="!discLoading" class="discuss__empty">还没有讨论，来抢沙发</div>
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

  <!-- 交换弹窗：选我的在售书 + 物流方式 + 收货地址 + 附言 -->
  <el-dialog v-model="swapVisible" title="以书换书" width="520px" :close-on-click-modal="false" @open="onSwapOpen">
    <div v-if="book" class="swap-dialog">
      <p class="swap-dialog__book">
        用我的书 换 《{{ book.title }}》
      </p>
      <template v-if="offerBooks.length">
        <div class="swap-dialog__field">
          <p class="swap-dialog__label">我的书籍</p>
          <el-select v-model="offerBookId" placeholder="选择一本在售书籍用于交换" style="width: 100%">
            <el-option v-for="b in offerBooks" :key="b.id" :value="b.id" :label="`《${b.title}》 ${b.author}`" />
          </el-select>
        </div>
        <div class="swap-dialog__field">
          <p class="swap-dialog__label">我收书的物流方式</p>
          <el-radio-group v-model="swapShipType" class="swap-dialog__ship">
            <el-radio-button :value="1">邮寄</el-radio-button>
            <el-radio-button :value="2">自提</el-radio-button>
          </el-radio-group>
        </div>
        <div v-if="swapShipType === 1" class="swap-dialog__field">
          <p class="swap-dialog__label">收货地址</p>
          <el-select v-model="swapAddressId" placeholder="请选择收货地址" style="width: 100%">
            <el-option
              v-for="a in addresses"
              :key="a.id"
              :value="a.id"
              :label="`${a.receiverName} ${a.phone} · ${a.province}${a.city}${a.district} ${a.detail}`"
            />
          </el-select>
          <p class="swap-dialog__manage">
            <router-link to="/addresses">管理收货地址</router-link>
          </p>
        </div>
        <div class="swap-dialog__field">
          <el-input v-model="swapMessage" maxlength="255" show-word-limit placeholder="给对方留个言（可留空）" />
        </div>
      </template>
      <div v-else class="swap-dialog__empty">
        <p>您还没有可交换的在售书籍</p>
        <router-link to="/publish" class="swap-dialog__publish">去发布书籍</router-link>
      </div>
    </div>
    <template #footer>
      <el-button @click="swapVisible = false">取消</el-button>
      <el-button type="primary" :loading="swapping" :disabled="!offerBooks.length" @click="submitSwap">发起交换</el-button>
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

.swap-dialog {
  &__book {
    font-family: var(--font-serif);
    font-weight: 600;
    font-size: 1.05rem;
    margin-bottom: var(--sp-4);
  }

  &__field {
    margin-top: var(--sp-3);
  }

  &__label {
    font-size: 0.85rem;
    color: var(--muted);
    margin-bottom: var(--sp-2);
  }

  &__ship {
    display: flex;
    gap: var(--sp-2);
  }

  &__manage {
    margin-top: var(--sp-2);
    font-size: 0.82rem;

    a {
      color: var(--mark);
    }
  }

  &__empty {
    text-align: center;
    padding: var(--sp-5) 0;
    color: var(--muted);
    display: grid;
    gap: var(--sp-3);
    justify-items: center;

    &-publish {
      color: var(--mark);
    }
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

.discuss {
  margin-top: var(--sp-7);
  max-width: 860px;

  &__head {
    display: flex;
    align-items: baseline;
    gap: var(--sp-3);
    margin-bottom: var(--sp-4);
  }

  &__title {
    font-family: var(--font-serif);
    font-size: 1.3rem;
    font-weight: 700;
  }

  &__count {
    font-size: 0.85rem;
    color: var(--muted);
  }

  &__editor {
    background: var(--card);
    border: 1px solid var(--line);
    border-radius: var(--radius);
    padding: var(--sp-4);
    margin-bottom: var(--sp-5);

    &-foot {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: var(--sp-2);
      margin-top: var(--sp-3);
    }
  }

  &__list {
    min-height: 80px;
  }

  &__item {
    padding: var(--sp-4) var(--sp-5);
    background: var(--card);
    border: 1px solid var(--line);
    border-radius: var(--radius);
    margin-bottom: var(--sp-3);
  }

  &__item-head,
  &__reply-head {
    display: flex;
    align-items: center;
    gap: var(--sp-3);
  }

  &__author {
    font-weight: 600;
    font-size: 0.92rem;
  }

  &__time {
    font-size: 0.78rem;
    color: var(--muted);
  }

  &__del {
    margin-left: auto;
  }

  &__body {
    margin-top: var(--sp-2);
  }

  &__img {
    max-width: 260px;
    max-height: 320px;
    border-radius: var(--radius-sm);
    border: 1px solid var(--line);
    object-fit: contain;
    margin-bottom: var(--sp-2);
  }

  &__content {
    line-height: 1.7;
    font-size: 0.95rem;

    &--reply {
      font-size: 0.9rem;
    }
  }

  &__reply-btn {
    margin-top: var(--sp-2);
    font-size: 0.82rem;
    color: var(--muted);
    cursor: pointer;

    &:hover {
      color: var(--mark);
    }
  }

  &__reply-editor {
    display: flex;
    flex-direction: column;
    gap: var(--sp-2);
    margin-top: var(--sp-3);
    padding: var(--sp-3);
    background: var(--paper);
    border-radius: var(--radius);
  }

  &__reply-foot {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: var(--sp-2);
  }

  &__replies {
    margin-top: var(--sp-3);
    padding-left: var(--sp-5);
    border-left: 2px solid var(--line);
  }

  &__reply {
    padding: var(--sp-3) 0;
    border-bottom: 1px dashed var(--line);

    &:last-child {
      border-bottom: none;
    }
  }

  &__empty {
    text-align: center;
    padding: var(--sp-7) 0;
    color: var(--muted);
  }
}

@media (max-width: 860px) {
  .detail__inner {
    grid-template-columns: 1fr;
  }
}
</style>
