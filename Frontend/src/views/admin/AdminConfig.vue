<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fetchAdminCreditRules, updateAdminCreditRuleStatus } from '@/api/admin'
import type { AdminCreditRule } from '@/types/admin'

const rules = ref<AdminCreditRule[]>([])
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    rules.value = await fetchAdminCreditRules()
  } finally {
    loading.value = false
  }
}

async function toggle(r: AdminCreditRule, status: number) {
  const action = status === 1 ? '启用' : '禁用'
  try {
    await ElMessageBox.confirm(`确定${action}「${r.name}」？${action === '禁用' ? '禁用后该规则不再触发信誉变动。' : '启用后按规则正常触发。'}`, `${action}规则`, {
      type: 'warning',
      confirmButtonText: action,
    })
  } catch {
    r.status = status === 1 ? 0 : 1 // 取消则回滚开关
    return
  }
  try {
    await updateAdminCreditRuleStatus(r.id, status)
    ElMessage.success(`已${action}`)
  } catch (e) {
    r.status = status === 1 ? 0 : 1
    ElMessage.error((e as Error).message || `${action}失败`)
  }
}

onMounted(load)
</script>

<template>
  <div class="panel">
    <el-alert
      class="panel__tip"
      title="信誉规则数据驱动：启用/禁用即时生效，用户信誉变动按规则自动触发（成功交易 +2、好评 +1、恶意取消 -3 等）。"
      type="info"
      :closable="false"
      show-icon
    />
    <el-table v-loading="loading" :data="rules" stripe class="panel__table">
      <el-table-column prop="name" label="规则名称" min-width="140" />
      <el-table-column prop="ruleCode" label="编码" min-width="150" show-overflow-tooltip />
      <el-table-column prop="triggerCondition" label="触发条件" min-width="240" show-overflow-tooltip />
      <el-table-column label="分值" width="90">
        <template #default="{ row }">
          <el-tag :type="row.changeValue >= 0 ? 'success' : 'danger'" size="small" effect="plain">
            {{ row.changeValue >= 0 ? '+' : '' }}{{ row.changeValue }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="priority" label="优先级" width="90" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="90" fixed="right">
        <template #default="{ row }">
          <el-switch
            :model-value="row.status === 1"
            inline-prompt
            active-text="开"
            inactive-text="关"
            @change="(v: string | number | boolean) => toggle(row, v ? 1 : 0)"
          />
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<style scoped lang="scss">
.panel {
  background: var(--card);
  border: 1px solid var(--line);
  border-radius: var(--radius);
  padding: var(--sp-4);

  &__tip {
    margin-bottom: var(--sp-4);
  }

  &__table {
    width: 100%;
  }
}
</style>
