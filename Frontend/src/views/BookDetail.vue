<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Star, StarFilled } from '@element-plus/icons-vue'
import BookCover from '@/components/common/BookCover.vue'
import { fetchBookById } from '@/api/book'
import type { Book } from '@/types/book'

const route = useRoute()
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

function onBuy() {
  ElMessage.info('交易功能开发中，敬请期待')
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
                <dd>{{ book.createdAt }}</dd>
              </div>
            </dl>

            <p class="detail__desc">{{ book.description }}</p>

            <div class="detail__actions">
              <el-button type="primary" round size="large" @click="onBuy">立即购买</el-button>
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

@media (max-width: 860px) {
  .detail__inner {
    grid-template-columns: 1fr;
  }
}
</style>
