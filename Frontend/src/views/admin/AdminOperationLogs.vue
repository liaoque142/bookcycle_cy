<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Search, Refresh } from '@element-plus/icons-vue'
import { fetchAdminOperationLogs } from '@/api/admin'
import type { OperationLog } from '@/types/admin'
import type { Page } from '@/types/api'

const query = reactive<{ action: string }>({ action: '' })
const list = ref<OperationLog[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(20)
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const r: Page<OperationLog> = await fetchAdminOperationLogs({
      action: query.action.trim() || undefined,
      page: page.value,
      size: size.value,
    })
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
  query.action = ''
  search()
}

function onPageChange(p: number) {
  page.value = p
  load()
}

onMounted(load)
</script>

<template>
  <div class="panel">
    <div class="panel__bar">
      <el-input
        v-model="query.action"
        class="panel__keyword"
        placeholder="操作编码（如 user.status）"
        clearable
        :prefix-icon="Search"
        @keyup.enter="search"
      />
      <el-button type="primary" @click="search">查询</el-button>
      <el-button :icon="Refresh" @click="reset">重置</el-button>
    </div>

    <el-table v-loading="loading" :data="list" stripe class="panel__table">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="action" label="操作" width="140">
        <template #default="{ row }">
          <el-tag size="small" type="info">{{ row.action }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="detail" label="详情" min-width="260" show-overflow-tooltip />
      <el-table-column prop="operatorName" label="操作人" width="120" />
      <el-table-column prop="createTime" label="时间" width="165" />
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
</style>
