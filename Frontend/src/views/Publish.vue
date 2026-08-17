<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { publishBook } from '@/api/book'
import { categories } from '@/data/books'
import type { PublishBookPayload, TradeType } from '@/types/book'

const router = useRouter()
const formRef = ref<FormInstance>()
const submitting = ref(false)

const CONDITION_OPTIONS = ['全新', '九成新', '八成新', '七成新及以下']
const TRADE_OPTIONS: { label: string; value: TradeType }[] = [
  { label: '出售', value: '出售' },
  { label: '交换', value: '交换' },
  { label: '免费赠送', value: '赠送' },
]
const CATEGORY_OPTIONS = categories.map((c) => c.name)

const form = reactive<PublishBookPayload>({
  title: '',
  author: '',
  isbn: '',
  publisher: '',
  category: '',
  price: null,
  tradeType: '出售',
  condition: '',
  description: '',
})

const rules: FormRules = {
  title: [{ required: true, message: '请输入书名', trigger: 'blur' }],
  author: [{ required: true, message: '请输入作者', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  condition: [{ required: true, message: '请选择新旧程度', trigger: 'change' }],
  description: [{ required: true, message: '请填写描述', trigger: 'blur' }, { max: 500, message: '描述不超过 500 字', trigger: 'blur' }],
}

async function onSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    const payload: PublishBookPayload = {
      ...form,
      isbn: form.isbn?.trim() || undefined,
      publisher: form.publisher?.trim() || undefined,
      price: form.tradeType === '出售' ? form.price : null,
    }
    const created = await publishBook(payload)
    ElMessage.success('发布成功')
    router.push(`/books/${created.id}`)
  } catch (e) {
    ElMessage.error((e as Error).message || '发布失败')
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <section class="section">
    <div class="container">
      <div class="section-head">
        <div>
          <h1 class="section-title">发布书籍</h1>
          <p class="section-sub">让闲置的书，遇到下一位读者</p>
        </div>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px" class="publish">
        <el-form-item label="书名" prop="title">
          <el-input v-model="form.title" placeholder="请输入书名" maxlength="60" />
        </el-form-item>

        <el-form-item label="作者" prop="author">
          <el-input v-model="form.author" placeholder="请输入作者" maxlength="40" />
        </el-form-item>

        <el-form-item label="ISBN">
          <el-input v-model="form.isbn" placeholder="选填" maxlength="20" />
        </el-form-item>

        <el-form-item label="出版社">
          <el-input v-model="form.publisher" placeholder="选填" maxlength="40" />
        </el-form-item>

        <el-form-item label="分类" prop="category">
          <el-select v-model="form.category" placeholder="请选择分类">
            <el-option v-for="c in CATEGORY_OPTIONS" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>

        <el-form-item label="交易方式" prop="tradeType">
          <el-radio-group v-model="form.tradeType">
            <el-radio v-for="t in TRADE_OPTIONS" :key="t.value" :value="t.value">{{ t.label }}</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item v-if="form.tradeType === '出售'" label="售价">
          <el-input-number v-model="form.price" :min="0" :max="9999" :precision="2" placeholder="¥" />
          <span class="publish__hint">元</span>
        </el-form-item>

        <el-form-item label="新旧程度" prop="condition">
          <el-select v-model="form.condition" placeholder="请选择新旧程度">
            <el-option v-for="c in CONDITION_OPTIONS" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>

        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="4" maxlength="500" show-word-limit placeholder="描述书籍状况、卖点，方便买家判断" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" round size="large" :loading="submitting" @click="onSubmit">发布书籍</el-button>
          <el-button round size="large" @click="router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </div>
  </section>
</template>

<style scoped lang="scss">
.publish {
  max-width: 640px;

  &__hint {
    margin-left: 8px;
    color: var(--muted);
    font-size: 0.9rem;
  }
}
</style>
