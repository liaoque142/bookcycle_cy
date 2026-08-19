<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import { fetchAdminBooks, updateAdminBookStatus } from '@/api/admin'
import { fetchCategories, type CategoryOption } from '@/api/category'
import type { AdminBook } from '@/types/admin'
import type { Page } from '@/types/api'

const query = reactive<{ keyword: string; status?: number; tradeType?: number; categoryId?: number }>({
  keyword: '',
  status: undefined,
  tradeType: undefined,
  categoryId: undefined,
})
const list = ref<AdminBook[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const loading = ref(false)
const categories = ref<CategoryOption[]>([])

const statusOptions = [
  { label: '全部', value: undefined },
  { label: '在售', value: 1 },
  { label: '已预订', value: 2 },
  { label: '已售出', value: 3 },
  { label: '已下架', value: 4 },
]
const tradeTypeOptions = [
  { label: '全部', value: undefined },
  { label: '出售', value: 1 },
  { label: '赠送', value: 2 },
  { label: '交换', value: 3 },
]

async function load() {
  loading.value = true
  try {
    const r: Page<AdminBook> = await fetchAdminBooks({ ...query, page: page.value, size: size.value })
    list.value = r.list
    total.value = r.total
  } finally {
    loading.value = false
  }
}

function search() {
  page.value = 1
  load()
}

function reset() {
  query.keyword = ''
  query.status = undefined
  query.tradeType = undefined
  query.categoryId = undefined
  search()
}

function onPageChange(p: number) {
  page.value = p
  load()
}

function priceLabel(b: AdminBook): string {
  if (b.tradeType === '出售' && b.price != null) return `¥${b.price}`
  return '免费'
}

function statusType(status: number): 'success' | 'primary' | 'info' | 'danger' {
  return status === 1 ? 'success' : status === 2 ? 'primary' : status === 4 ? 'danger' : 'info'
}

/** 交易中（已预订/已售出）不可下架上架 */
function canToggle(b: AdminBook) {
  return b.status === 1 || b.status === 4
}

async function changeStatus(b: AdminBook, target: 1 | 4) {
  const action = target === 4 ? '下架' : '上架'
  try {
    await ElMessageBox.confirm(
      `确定${action}《${b.title}》？${target === 4 ? '下架后书籍从书市隐藏。' : '上架后书籍回到在售状态。'}`,
      `${action}书籍`,
      { type: 'warning', confirmButtonText: action },
    )
  } catch {
    return
  }
  try {
    await updateAdminBookStatus(b.id, target)
    ElMessage.success(`已${action}`)
    await load()
  } catch (e) {
    ElMessage.error((e as Error).message || `${action}失败`)
  }
}

onMounted(async () => {
  load()
  categories.value = await fetchCategories()
})
</script>

<template>
  <div class="panel">
    <div class="panel__bar">
      <el-input
        v-model="query.keyword"
        class="panel__keyword"
        placeholder="书名 / 作者 / ISBN"
        clearable
        :prefix-icon="Search"
        @keyup.enter="search"
      />
      <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 120px">
        <el-option v-for="s in statusOptions" :key="String(s.value)" :label="s.label" :value="s.value" />
      </el-select>
      <el-select v-model="query.tradeType" placeholder="全部方式" clearable style="width: 120px">
        <el-option v-for="t in tradeTypeOptions" :key="String(t.value)" :label="t.label" :value="t.value" />
      </el-select>
      <el-select v-model="query.categoryId" placeholder="全部分类" clearable style="width: 140px">
        <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
      </el-select>
      <el-button type="primary" @click="search">查询</el-button>
      <el-button :icon="Refresh" @click="reset">重置</el-button>
    </div>

    <el-table v-loading="loading" :data="list" stripe class="panel__table">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="title" label="书名" min-width="180" show-overflow-tooltip />
      <el-table-column prop="author" label="作者" min-width="100" show-overflow-tooltip />
      <el-table-column prop="category" label="分类" width="90" />
      <el-table-column prop="tradeType" label="方式" width="80" />
      <el-table-column label="价格" width="80">
        <template #default="{ row }">{{ priceLabel(row) }}</template>
      </el-table-column>
      <el-table-column label="卖家" min-width="110">
        <template #default="{ row }">
          <span>{{ row.seller }}</span>
          <span class="cell__credit">（{{ row.credit }}）</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" size="small">{{ row.statusName }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="发布时间" width="165" />
      <el-table-column label="操作" width="110" fixed="right">
        <template #default="{ row }">
          <el-tooltip :disabled="canToggle(row)" content="交易中的书籍不可操作" placement="top">
            <span>
              <el-button link :type="row.status === 1 ? 'danger' : 'success'" :disabled="!canToggle(row)" @click="changeStatus(row, row.status === 1 ? 4 : 1)">
                {{ row.status === 1 ? '下架' : '上架' }}
              </el-button>
            </span>
          </el-tooltip>
        </template>
      </el-table-column>
    </el-table>

    <div v-if="total > size" class="panel__pager">
      <el-pagination layout="prev, pager, next" :total="total" :page-size="size" :current-page="page" @current-change="onPageChange" />
    </div>
  </div>
</template>

<style scoped lang="scss">
.panel {
  background: var(--card);
  border: 1px solid var(--line);
  border-radius: var(--radius);
  padding: var(--sp-4);

  &__bar {
    display: flex;
    gap: var(--sp-3);
    margin-bottom: var(--sp-4);
    flex-wrap: wrap;
  }

  &__keyword {
    width: 220px;
  }

  &__table {
    width: 100%;
  }

  &__pager {
    display: flex;
    justify-content: flex-end;
    margin-top: var(--sp-4);
  }
}

.cell__credit {
  color: var(--muted);
  font-size: 0.85rem;
}
</style>
