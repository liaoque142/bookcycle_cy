<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Search, Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { fetchAdminRefunds, handleAdminRefund } from '@/api/admin'
import type { AdminRefund } from '@/types/admin'
import type { Page } from '@/types/api'

const query = reactive<{ keyword: string; status?: number; type?: number }>({
  keyword: '',
  status: undefined,
  type: undefined,
})
const list = ref<AdminRefund[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const loading = ref(false)

const statusOptions = [
  { label: '全部状态', value: undefined },
  { label: '待处理', value: 1 },
  { label: '同意', value: 2 },
  { label: '拒绝', value: 3 },
  { label: '已完成', value: 4 },
]
const typeOptions = [
  { label: '全部类型', value: undefined },
  { label: '仅退款', value: 1 },
  { label: '退货退款', value: 2 },
]

/** 处理弹窗 */
const dialog = reactive({
  visible: false,
  submitting: false,
  row: null as AdminRefund | null,
  agree: true as boolean,
  result: '' as string,
})

function statusType(status: number): 'warning' | 'success' | 'danger' | 'info' {
  return status === 1 ? 'warning' : status === 2 ? 'success' : status === 3 ? 'danger' : 'info'
}

async function load() {
  loading.value = true
  try {
    const r: Page<AdminRefund> = await fetchAdminRefunds({ ...query, page: page.value, size: size.value })
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
  query.type = undefined
  search()
}

function onPageChange(p: number) {
  page.value = p
  load()
}

function openHandle(row: AdminRefund) {
  dialog.row = row
  dialog.agree = true
  dialog.result = ''
  dialog.visible = true
}

async function submitHandle() {
  if (!dialog.row) return
  const label = dialog.agree ? '同意退款' : '拒绝退款'
  dialog.submitting = true
  try {
    await handleAdminRefund(dialog.row.id, dialog.agree, dialog.result)
    ElMessage.success(`已${label}`)
    dialog.visible = false
    load()
  } catch (e) {
    // 错误提示由全局拦截器统一弹出
  } finally {
    dialog.submitting = false
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
        placeholder="订单号 / 书名 / 申请人"
        clearable
        :prefix-icon="Search"
        @keyup.enter="search"
      />
      <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 120px">
        <el-option v-for="s in statusOptions" :key="String(s.value)" :label="s.label" :value="s.value" />
      </el-select>
      <el-select v-model="query.type" placeholder="全部类型" clearable style="width: 120px">
        <el-option v-for="t in typeOptions" :key="String(t.value)" :label="t.label" :value="t.value" />
      </el-select>
      <el-button type="primary" @click="search">查询</el-button>
      <el-button :icon="Refresh" @click="reset">重置</el-button>
    </div>

    <el-table v-loading="loading" :data="list" stripe class="panel__table">
      <el-table-column prop="orderNo" label="订单号" min-width="200" show-overflow-tooltip />
      <el-table-column prop="bookTitle" label="书籍" min-width="160" show-overflow-tooltip />
      <el-table-column prop="applicantName" label="申请人" width="110" show-overflow-tooltip />
      <el-table-column label="退款金额" width="100">
        <template #default="{ row }">
          <span v-if="row.amount">¥{{ row.amount }}</span>
          <span v-else>—</span>
        </template>
      </el-table-column>
      <el-table-column prop="type" label="类型" width="90" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="statusType(row.statusCode)" size="small">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="reason" label="申请原因" min-width="140" show-overflow-tooltip />
      <el-table-column prop="createTime" label="申请时间" width="165" />
      <el-table-column label="操作" width="90" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.statusCode === 1" link type="primary" @click="openHandle(row)">处理</el-button>
          <span v-else class="panel__done">{{ row.handlerName || '—' }}</span>
        </template>
      </el-table-column>
    </el-table>

    <div v-if="total > size" class="panel__pager">
      <el-pagination layout="prev, pager, next" :total="total" :page-size="size" :current-page="page" @current-change="onPageChange" />
    </div>
  </div>

  <el-dialog v-model="dialog.visible" title="处理售后申请" width="460px">
    <template v-if="dialog.row">
      <div class="meta">
        <div class="meta__row"><span class="meta__label">订单</span>{{ dialog.row.orderNo }}</div>
        <div class="meta__row"><span class="meta__label">书籍</span>{{ dialog.row.bookTitle }}</div>
        <div class="meta__row"><span class="meta__label">申请人</span>{{ dialog.row.applicantName }}</div>
        <div class="meta__row"><span class="meta__label">金额</span>¥{{ dialog.row.amount }}</div>
        <div class="meta__row"><span class="meta__label">类型</span>{{ dialog.row.type }}</div>
        <div v-if="dialog.row.reason" class="meta__row"><span class="meta__label">原因</span>{{ dialog.row.reason }}</div>
      </div>
      <el-form label-width="72px" class="dialog__form">
        <el-form-item label="处理结果">
          <el-radio-group v-model="dialog.agree">
            <el-radio :value="true">同意退款</el-radio>
            <el-radio :value="false">拒绝退款</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="处理说明">
          <el-input v-model="dialog.result" type="textarea" :rows="3" maxlength="255" show-word-limit placeholder="说明退款/拒绝原因（选填）" />
        </el-form-item>
      </el-form>
    </template>
    <template #footer>
      <el-button @click="dialog.visible = false">取消</el-button>
      <el-button type="primary" :loading="dialog.submitting" @click="submitHandle">确认</el-button>
    </template>
  </el-dialog>
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

  &__done {
    color: var(--muted);
    font-size: 0.85rem;
  }
}

.meta {
  display: flex;
  flex-direction: column;
  gap: var(--sp-2);
  margin-bottom: var(--sp-4);
  font-size: 0.92rem;

  &__row {
    display: flex;
    gap: var(--sp-3);
    align-items: baseline;

    & > :not(.meta__label) {
      flex: 1;
      word-break: break-all;
    }
  }

  &__label {
    width: 56px;
    flex-shrink: 0;
    color: var(--muted);
  }
}
</style>
