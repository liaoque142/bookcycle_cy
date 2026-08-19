<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import { fetchAdminUsers, fetchAdminUserDetail, updateAdminUserStatus, adjustAdminUserCredit } from '@/api/admin'
import type { AdminUser } from '@/types/admin'
import type { Page } from '@/types/api'

const query = reactive<{ keyword: string; status?: number }>({ keyword: '', status: undefined })
const list = ref<AdminUser[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const loading = ref(false)

// 详情抽屉
const detailVisible = ref(false)
const detail = ref<AdminUser | null>(null)
const detailLoading = ref(false)

// 调整信誉分弹窗
const creditVisible = ref(false)
const creditTarget = ref<AdminUser | null>(null)
const creditForm = reactive({ changeValue: 0, reason: '' })
const crediting = ref(false)

const statusOptions = [
  { label: '全部', value: undefined },
  { label: '正常', value: 1 },
  { label: '禁用', value: 0 },
]

async function load() {
  loading.value = true
  try {
    const r: Page<AdminUser> = await fetchAdminUsers({ ...query, page: page.value, size: size.value })
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
  search()
}

function onPageChange(p: number) {
  page.value = p
  load()
}

/** admin 账号禁止禁用（后端也会拦，前端先禁点） */
function isAdminRow(row: AdminUser) {
  return row.roleCode === 'admin'
}

async function openDetail(row: AdminUser) {
  detailVisible.value = true
  detailLoading.value = true
  detail.value = null
  try {
    detail.value = await fetchAdminUserDetail(row.id)
  } catch (e) {
    ElMessage.error((e as Error).message || '加载详情失败')
  } finally {
    detailLoading.value = false
  }
}

async function toggleStatus(row: AdminUser) {
  const target = row.status === 1 ? 0 : 1
  const action = target === 0 ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(
      `确定${action}用户「${row.nickname}」？${target === 0 ? '禁用后该用户无法登录。' : ''}`,
      `${action}用户`,
      { type: 'warning', confirmButtonText: action },
    )
  } catch {
    return
  }
  try {
    await updateAdminUserStatus(row.id, target)
    ElMessage.success(`已${action}`)
    await load()
  } catch (e) {
    ElMessage.error((e as Error).message || `${action}失败`)
  }
}

function openCredit(row: AdminUser) {
  creditTarget.value = row
  creditForm.changeValue = 0
  creditForm.reason = ''
  creditVisible.value = true
}

async function doAdjustCredit() {
  if (!creditTarget.value) return
  if (!creditForm.reason.trim()) {
    ElMessage.warning('请填写调整原因')
    return
  }
  crediting.value = true
  try {
    await adjustAdminUserCredit(creditTarget.value.id, creditForm.changeValue, creditForm.reason.trim())
    ElMessage.success('信誉分已调整')
    creditVisible.value = false
    creditTarget.value = null
    await load()
  } catch (e) {
    ElMessage.error((e as Error).message || '调整失败')
  } finally {
    crediting.value = false
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
        placeholder="用户名 / 昵称 / 手机 / 邮箱"
        clearable
        :prefix-icon="Search"
        @keyup.enter="search"
      />
      <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 120px">
        <el-option v-for="s in statusOptions" :key="String(s.value)" :label="s.label" :value="s.value" />
      </el-select>
      <el-button type="primary" @click="search">查询</el-button>
      <el-button :icon="Refresh" @click="reset">重置</el-button>
    </div>

    <el-table v-loading="loading" :data="list" stripe class="panel__table">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="username" label="账号" min-width="130" show-overflow-tooltip />
      <el-table-column prop="nickname" label="昵称" min-width="110" show-overflow-tooltip />
      <el-table-column label="联系方式" min-width="150">
        <template #default="{ row }">{{ row.phone || row.email || '—' }}</template>
      </el-table-column>
      <el-table-column prop="credit" label="信誉分" width="80" />
      <el-table-column label="角色" width="90">
        <template #default="{ row }">
          <el-tag :type="row.roleCode === 'admin' ? 'warning' : 'info'" size="small">{{ row.roleName }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '正常' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="注册时间" width="165" />
      <el-table-column label="操作" width="190" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row)">详情</el-button>
          <el-button link type="primary" @click="openCredit(row)">调整信誉</el-button>
          <el-tooltip :disabled="!isAdminRow(row)" content="管理员账号不可禁用" placement="top">
            <span>
              <el-button
                link
                :type="row.status === 1 ? 'danger' : 'success'"
                :disabled="isAdminRow(row)"
                @click="toggleStatus(row)"
              >
                {{ row.status === 1 ? '禁用' : '启用' }}
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

  <!-- 用户详情 -->
  <el-drawer v-model="detailVisible" title="用户详情" size="400px">
    <div v-loading="detailLoading" class="ud">
      <template v-if="detail">
        <div class="ud__head">
          <div class="ud__avatar">{{ detail.nickname.slice(0, 1) }}</div>
          <div>
            <p class="ud__name">{{ detail.nickname }}</p>
            <p class="ud__username">@{{ detail.username }}</p>
          </div>
        </div>
        <el-descriptions :column="1" border size="small" class="ud__des">
          <el-descriptions-item label="角色">{{ detail.roleName }}（{{ detail.roleCode }}）</el-descriptions-item>
          <el-descriptions-item label="状态">{{ detail.status === 1 ? '正常' : '禁用' }}</el-descriptions-item>
          <el-descriptions-item label="信誉分">{{ detail.credit }}</el-descriptions-item>
          <el-descriptions-item label="手机">{{ detail.phone || '—' }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ detail.email || '—' }}</el-descriptions-item>
          <el-descriptions-item label="个性签名">{{ detail.bio || '—' }}</el-descriptions-item>
          <el-descriptions-item label="最后登录">{{ detail.lastLoginTime || '—' }}</el-descriptions-item>
          <el-descriptions-item label="注册时间">{{ detail.createTime }}</el-descriptions-item>
        </el-descriptions>
      </template>
    </div>
  </el-drawer>

  <!-- 调整信誉分 -->
  <el-dialog v-model="creditVisible" title="调整信誉分" width="440px" :close-on-click-modal="false">
    <template v-if="creditTarget">
      <p class="credit__tip">为「{{ creditTarget.nickname }}」调整信誉分，当前 {{ creditTarget.credit }}</p>
      <el-form label-width="70px">
        <el-form-item label="调整值">
          <el-input-number v-model="creditForm.changeValue" :min="-100" :max="100" />
          <span class="credit__hint">正数增加，负数扣减</span>
        </el-form-item>
        <el-form-item label="原因">
          <el-input v-model="creditForm.reason" maxlength="255" show-word-limit placeholder="如：交易纠纷扣分" />
        </el-form-item>
      </el-form>
    </template>
    <template #footer>
      <el-button @click="creditVisible = false">取消</el-button>
      <el-button type="primary" :loading="crediting" @click="doAdjustCredit">确认调整</el-button>
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
    width: 260px;
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

.ud {
  &__head {
    display: flex;
    align-items: center;
    gap: 14px;
    margin-bottom: var(--sp-4);
  }

  &__avatar {
    display: grid;
    place-items: center;
    width: 52px;
    height: 52px;
    border-radius: 50%;
    background: var(--mark);
    color: #fff;
    font-family: var(--font-serif);
    font-size: 1.3rem;
  }

  &__name {
    font-family: var(--font-serif);
    font-size: 1.1rem;
    font-weight: 700;
  }

  &__username {
    font-size: 0.85rem;
    color: var(--muted);
    margin-top: 2px;
  }
}

.credit {
  &__tip {
    font-size: 0.9rem;
    color: var(--muted);
    margin-bottom: var(--sp-3);
  }

  &__hint {
    margin-left: 10px;
    font-size: 0.8rem;
    color: var(--muted);
  }
}
</style>
