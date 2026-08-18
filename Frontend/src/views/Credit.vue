<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { fetchCreditMe, fetchCreditLogs, type CreditLog } from '@/api/credit'
import type { Page } from '@/types/api'

const credit = ref(100)
const logs = ref<CreditLog[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(20)
const loading = ref(true)

async function load() {
  loading.value = true
  try {
    credit.value = await fetchCreditMe()
    const result: Page<CreditLog> = await fetchCreditLogs(page.value, size.value)
    logs.value = result.list
    total.value = result.total
  } finally {
    loading.value = false
  }
}

function onPageChange(p: number) {
  page.value = p
  load()
}

onMounted(load)
</script>

<template>
  <section class="section">
    <div class="container">
      <div class="page">
        <div class="page__head">
          <h1 class="page__title">我的信誉</h1>
        </div>

        <div class="hero">
          <p class="hero__label">当前信誉分</p>
          <p class="hero__value">{{ credit }}</p>
          <p class="hero__tip">初始 100 · 成功交易 / 好评增加 · 恶意取消等行为扣减 · 低于 60 暂停购买、低于 40 冻结交易</p>
        </div>

        <div class="logs">
          <div class="logs__head">
            <h2 class="logs__title">变动记录</h2>
          </div>

          <div v-loading="loading" class="logs__body">
            <div v-if="logs.length" class="logs__list">
              <div v-for="(log, i) in logs" :key="i" class="log">
                <span class="log__delta" :class="log.changeValue >= 0 ? 'log__delta--plus' : 'log__delta--minus'">
                  {{ log.changeValue > 0 ? `+${log.changeValue}` : log.changeValue }}
                </span>
                <div class="log__info">
                  <p class="log__reason">{{ log.reason }}</p>
                  <p class="log__time">{{ log.createTime }}</p>
                </div>
              </div>
            </div>
            <div v-else-if="!loading" class="logs__empty">还没有信誉变动记录</div>
          </div>

          <div v-if="total > size" class="logs__pager">
            <el-pagination layout="prev, pager, next" :total="total" :page-size="size" :current-page="page" @current-change="onPageChange" />
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped lang="scss">
.page {
  max-width: 760px;
  margin: 0 auto;

  &__head {
    margin-bottom: var(--sp-5);
  }

  &__title {
    font-family: var(--font-serif);
    font-size: 1.5rem;
    font-weight: 700;
  }
}

.hero {
  text-align: center;
  padding: var(--sp-6);
  background: var(--card);
  border: 1px solid var(--line);
  border-radius: var(--radius);

  &__label {
    font-size: 0.9rem;
    color: var(--muted);
  }

  &__value {
    margin-top: var(--sp-2);
    font-family: var(--font-serif);
    font-size: 3.2rem;
    font-weight: 700;
    color: var(--mark);
    font-variant-numeric: tabular-nums;
  }

  &__tip {
    margin-top: var(--sp-3);
    font-size: 0.8rem;
    color: var(--muted);
    max-width: 480px;
    margin-left: auto;
    margin-right: auto;
    line-height: 1.6;
  }
}

.logs {
  margin-top: var(--sp-6);

  &__head {
    margin-bottom: var(--sp-3);
  }

  &__title {
    font-family: var(--font-serif);
    font-size: 1.2rem;
    font-weight: 700;
  }

  &__body {
    min-height: 80px;
    background: var(--card);
    border: 1px solid var(--line);
    border-radius: var(--radius);
    overflow: hidden;
  }

  &__empty {
    text-align: center;
    padding: var(--sp-7) 0;
    color: var(--muted);
  }

  &__pager {
    display: flex;
    justify-content: center;
    margin-top: var(--sp-4);
  }
}

.log {
  display: flex;
  align-items: center;
  gap: var(--sp-4);
  padding: var(--sp-3) var(--sp-5);
  border-bottom: 1px solid var(--line);

  &:last-child {
    border-bottom: none;
  }

  &__delta {
    font-weight: 700;
    font-variant-numeric: tabular-nums;
    min-width: 44px;
    text-align: center;

    &--plus {
      color: #2e9e5b;
    }

    &--minus {
      color: #d9534f;
    }
  }

  &__info {
    display: grid;
    gap: 2px;
  }

  &__reason {
    font-size: 0.95rem;
  }

  &__time {
    font-size: 0.8rem;
    color: var(--muted);
  }
}
</style>
