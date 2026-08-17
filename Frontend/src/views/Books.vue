<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import BookCard from '@/components/home/BookCard.vue'
import { fetchBooks } from '@/api/book'
import { categories } from '@/data/books'
import type { Book } from '@/types/book'
import type { TradeType } from '@/types/book'

const CATEGORY_OPTIONS = categories.map((c) => c.name)
const TRADE_OPTIONS: TradeType[] = ['出售', '交换', '赠送']

const keyword = ref('')
const filters = reactive<{
  category: string
  tradeType: TradeType | ''
  sort: string
}>({
  category: '',
  tradeType: '',
  sort: 'latest',
})

const page = ref(1)
const size = ref(12)
const total = ref(0)
const books = ref<Book[]>([])
const loading = ref(false)

async function loadBooks() {
  loading.value = true
  try {
    const result = await fetchBooks({
      keyword: keyword.value,
      category: filters.category || undefined,
      tradeType: filters.tradeType || undefined,
      sort: filters.sort,
      page: page.value,
      size: size.value,
    })
    books.value = result.list
    total.value = result.total
  } finally {
    loading.value = false
  }
}

function onSearch() {
  page.value = 1
  loadBooks()
}

function onFilterChange() {
  page.value = 1
  loadBooks()
}

onMounted(loadBooks)
</script>

<template>
  <section class="section">
    <div class="container">
      <div class="section-head">
        <div>
          <h1 class="section-title">书市</h1>
          <p class="section-sub">共 {{ total }} 本在流转</p>
        </div>
      </div>

      <div class="toolbar">
        <el-input
          v-model="keyword"
          class="toolbar__search"
          placeholder="搜索书名 / 作者 / 分类"
          :prefix-icon="Search"
          clearable
          @keyup.enter="onSearch"
          @clear="onSearch"
        />
        <el-select v-model="filters.category" class="toolbar__select" placeholder="全部分类" clearable @change="onFilterChange">
          <el-option v-for="c in CATEGORY_OPTIONS" :key="c" :label="c" :value="c" />
        </el-select>
        <el-select v-model="filters.tradeType" class="toolbar__select" placeholder="全部交易方式" clearable @change="onFilterChange">
          <el-option v-for="t in TRADE_OPTIONS" :key="t" :label="t" :value="t" />
        </el-select>
        <el-select v-model="filters.sort" class="toolbar__select" @change="onFilterChange">
          <el-option label="最新发布" value="latest" />
          <el-option label="价格从低到高" value="price_asc" />
          <el-option label="价格从高到低" value="price_desc" />
        </el-select>
        <el-button type="primary" round @click="onSearch">搜索</el-button>
      </div>

      <div v-loading="loading" class="grid" :class="{ 'grid--empty': books.length === 0 }">
        <BookCard v-for="book in books" :key="book.id" :book="book" />
      </div>

      <div v-if="total === 0 && !loading" class="empty">
        <p class="empty__text">没有符合条件的书，换个筛选试试</p>
      </div>

      <div v-if="total > size" class="pager">
        <el-pagination
          v-model:current-page="page"
          :page-size="size"
          :total="total"
          layout="prev, pager, next"
          background
          @current-change="loadBooks"
        />
      </div>
    </div>
  </section>
</template>

<style scoped lang="scss">
.toolbar {
  display: flex;
  align-items: center;
  gap: var(--sp-3);
  margin-bottom: var(--sp-6);
  flex-wrap: wrap;

  &__search {
    width: 260px;
  }

  &__select {
    width: 150px;
  }
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(196px, 1fr));
  gap: var(--sp-5);
  min-height: 200px;
}

.empty {
  text-align: center;
  padding: var(--sp-8) 0;
  color: var(--muted);
}

.pager {
  display: flex;
  justify-content: center;
  margin-top: var(--sp-6);
}
</style>
