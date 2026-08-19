<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Search, Refresh } from '@element-plus/icons-vue'
import { fetchAdminOrders, fetchAdminOrderDetail } from '@/api/admin'
import type { AdminOrder } from '@/types/admin'
import type { Page } from '@/types/api'

const query = reactive<{ keyword: string; status?: number; tradeType?: number }>({
  keyword: '',
  status: undefined,
  tradeType: undefined,
})
const list = ref<AdminOrder[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const loading = ref(false)

const drawer = reactive({ visible: false, loading: false, order: null as AdminOrder | null })

const statusOptions = [
  { label: '全部', value: undefined },
  { label: '待支付', value: 1 },
  { label: '待发货', value: 2 },
  { label: '待收货', value: 3 },
  { label: '已完成', value: 4 },
  { label: '已取消', value: 5 },
  { label: '售后中', value: 6 },
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
    const r: Page<AdminOrder> = await fetchAdminOrders({ ...query, page: page.value, size: size.value })
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
  search()
}

function onPageChange(p: number) {
  page.value = p
  load()
}

function statusType(status: number): 'warning' | 'primary' | 'success' | 'info' | 'danger' {
  return status === 1 ? 'warning' : status === 2 || status === 3 ? 'primary' : status === 4 ? 'success' : status === 6 ? 'danger' : 'info'
}

function payStatusLabel(o: AdminOrder): string {
  return o.payStatus === 1 ? `已支付${o.payTime ? ' ' + o.payTime : ''}` : o.payStatus === 2 ? '已退款' : '未支付'
}

async function openDetail(row: AdminOrder) {
  drawer.visible = true
  drawer.loading = true
  try {
    drawer.order = await fetchAdminOrderDetail(row.orderNo)
  } catch (e) {
    drawer.order = row
  } finally {
    drawer.loading = false
  }
}

onMounted(load)
</script>

<template>
  <div class="panel">
    <div class="panel__bar">
      <el-input
        v-model="query.keyword"
        class="panel__keyword"
        placeholder="订单号 / 书名 / 买家 / 卖家"
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
      <el-button type="primary" @click="search">查询</el-button>
      <el-button :icon="Refresh" @click="reset">重置</el-button>
    </div>

    <el-table v-loading="loading" :data="list" stripe class="panel__table">
      <el-table-column prop="orderNo" label="订单号" min-width="200" show-overflow-tooltip />
      <el-table-column prop="bookTitle" label="书籍" min-width="160" show-overflow-tooltip />
      <el-table-column prop="buyerName" label="买家" width="100" show-overflow-tooltip />
      <el-table-column prop="sellerName" label="卖家" width="100" show-overflow-tooltip />
      <el-table-column label="金额" width="90">
        <template #default="{ row }">
          <span v-if="row.tradeType === '出售' && row.amount">¥{{ row.amount }}</span>
          <span v-else>免费</span>
        </template>
      </el-table-column>
      <el-table-column prop="tradeType" label="方式" width="80" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="statusType(row.statusCode)" size="small">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="下单时间" width="165" />
      <el-table-column label="操作" width="90" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div v-if="total > size" class="panel__pager">
      <el-pagination layout="prev, pager, next" :total="total" :page-size="size" :current-page="page" @current-change="onPageChange" />
    </div>
  </div>

  <el-drawer v-model="drawer.visible" title="订单详情" size="440px">
    <div v-loading="drawer.loading" class="detail">
      <template v-if="drawer.order">
        <div class="detail__row"><span class="detail__label">订单号</span>{{ drawer.order.orderNo }}</div>
        <div class="detail__row"><span class="detail__label">书籍</span>{{ drawer.order.bookTitle }}</div>
        <div class="detail__row"><span class="detail__label">买家</span>{{ drawer.order.buyerName }}</div>
        <div class="detail__row"><span class="detail__label">卖家</span>{{ drawer.order.sellerName }}</div>
        <div class="detail__row"><span class="detail__label">金额</span>{{ drawer.order.tradeType === '出售' && drawer.order.amount ? '¥' + drawer.order.amount : '免费' }}</div>
        <div class="detail__row"><span class="detail__label">交易方式</span>{{ drawer.order.tradeType }}</div>
        <div class="detail__row"><span class="detail__label">物流方式</span>{{ drawer.order.shipType }}</div>
        <div class="detail__row"><span class="detail__label">状态</span>
          <el-tag :type="statusType(drawer.order.statusCode)" size="small">{{ drawer.order.status }}</el-tag>
        </div>
        <template v-if="drawer.order.shipType === '邮寄'">
          <div class="detail__row"><span class="detail__label">收货人</span>{{ drawer.order.receiverName }}</div>
          <div class="detail__row"><span class="detail__label">联系电话</span>{{ drawer.order.receiverPhone }}</div>
          <div class="detail__row"><span class="detail__label">收货地址</span>{{ drawer.order.receiverAddress }}</div>
        </template>
        <div class="detail__row"><span class="detail__label">支付</span>{{ payStatusLabel(drawer.order) }}</div>
        <div v-if="drawer.order.cancelReason" class="detail__row"><span class="detail__label">取消原因</span>{{ drawer.order.cancelReason }}</div>
        <div class="detail__row"><span class="detail__label">下单时间</span>{{ drawer.order.createTime }}</div>
      </template>
      <el-empty v-else-if="!drawer.loading" description="暂无数据" />
    </div>
  </el-drawer>
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
    width: 240px;
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

.detail {
  display: flex;
  flex-direction: column;
  gap: var(--sp-3);
  font-size: 0.92rem;

  &__row {
    display: flex;
    gap: var(--sp-3);
    align-items: baseline;

    & > :not(.detail__label) {
      flex: 1;
      word-break: break-all;
    }
  }

  &__label {
    width: 76px;
    flex-shrink: 0;
    color: var(--muted);
  }
}
</style>
