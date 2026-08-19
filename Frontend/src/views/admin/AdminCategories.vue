<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { fetchAdminCategories, createAdminCategory, updateAdminCategory, deleteAdminCategory } from '@/api/admin'
import type { AdminCategory } from '@/types/admin'

const flatList = ref<AdminCategory[]>([])
const treeData = ref<AdminCategory[]>([])
const loading = ref(false)

/** 扁平列表按 parentId 组装树（el-table tree 需要 children） */
function buildTree(list: AdminCategory[]): AdminCategory[] {
  const map = new Map<number, AdminCategory>()
  list.forEach((c) => map.set(c.id, { ...c, children: [] }))
  const roots: AdminCategory[] = []
  map.forEach((node) => {
    const parent = node.parentId ? map.get(node.parentId) : undefined
    if (parent) parent.children!.push(node)
    else roots.push(node)
  })
  return roots
}

async function load() {
  loading.value = true
  try {
    flatList.value = await fetchAdminCategories()
    treeData.value = buildTree(flatList.value)
  } finally {
    loading.value = false
  }
}

// 新增/编辑弹窗
interface FormState {
  parentId: number
  name: string
  sort: number
  status: number
}
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const submitting = ref(false)
const form = reactive<FormState>({ parentId: 0, name: '', sort: 0, status: 1 })

/** 父分类下拉：所有分类 + 根分类（编辑时排除自己） */
const parentOptions = computed(() =>
  flatList.value
    .filter((c) => c.id !== editingId.value)
    .map((c) => ({ label: c.name, value: c.id })),
)

const dialogTitle = computed(() => {
  if (editingId.value) return '编辑分类'
  return form.parentId ? '新增子分类' : '新增根分类'
})

function openCreate(parent?: AdminCategory) {
  editingId.value = null
  form.parentId = parent ? parent.id : 0
  form.name = ''
  form.sort = 0
  form.status = 1
  dialogVisible.value = true
}

function openEdit(row: AdminCategory) {
  editingId.value = row.id
  form.parentId = row.parentId || 0
  form.name = row.name
  form.sort = row.sort
  form.status = row.status
  dialogVisible.value = true
}

async function submit() {
  if (!form.name.trim()) {
    ElMessage.warning('请填写分类名')
    return
  }
  submitting.value = true
  try {
    const payload = { parentId: form.parentId || 0, name: form.name.trim(), sort: form.sort, status: form.status }
    if (editingId.value) {
      await updateAdminCategory(editingId.value, payload)
      ElMessage.success('已保存')
    } else {
      await createAdminCategory(payload)
      ElMessage.success('已新增')
    }
    dialogVisible.value = false
    await load()
  } catch (e) {
    ElMessage.error((e as Error).message || '保存失败')
  } finally {
    submitting.value = false
  }
}

async function remove(row: AdminCategory) {
  try {
    await ElMessageBox.confirm(
      `确定删除分类「${row.name}」？存在子分类或书籍占用时将无法删除。`,
      '删除分类',
      { type: 'warning', confirmButtonText: '删除' },
    )
  } catch {
    return
  }
  try {
    await deleteAdminCategory(row.id)
    ElMessage.success('已删除')
    await load()
  } catch (e) {
    ElMessage.error((e as Error).message || '删除失败')
  }
}

onMounted(load)
</script>

<template>
  <div class="panel">
    <div class="panel__bar">
      <el-button type="primary" :icon="Plus" @click="openCreate()">新增根分类</el-button>
    </div>

    <el-table v-loading="loading" :data="treeData" row-key="id" :tree-props="{ children: 'children' }" default-expand-all class="panel__table">
      <el-table-column prop="name" label="分类名" min-width="200" />
      <el-table-column label="父分类" min-width="120">
        <template #default="{ row }">{{ row.parentId ? flatList.find((c) => c.id === row.parentId)?.name || '—' : '根分类' }}</template>
      </el-table-column>
      <el-table-column prop="sort" label="排序" width="80" />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="210" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openCreate(row)">新增子分类</el-button>
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>

  <!-- 新增/编辑分类 -->
  <el-dialog v-model="dialogVisible" :title="dialogTitle" width="440px" :close-on-click-modal="false">
    <el-form label-width="80px">
      <el-form-item label="父分类">
        <el-select v-model="form.parentId" placeholder="作为根分类">
          <el-option label="作为根分类" :value="0" />
          <el-option v-for="p in parentOptions" :key="p.value" :label="p.label" :value="p.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="分类名">
        <el-input v-model="form.name" maxlength="50" show-word-limit placeholder="如：文学" />
      </el-form-item>
      <el-form-item label="排序">
        <el-input-number v-model="form.sort" :min="0" :max="999" />
        <span class="form__hint">越小越靠前</span>
      </el-form-item>
      <el-form-item label="状态">
        <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="禁用" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="submit">保存</el-button>
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
  }

  &__table {
    width: 100%;
  }
}

.form__hint {
  margin-left: 10px;
  font-size: 0.8rem;
  color: var(--muted);
}
</style>
