<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts/core'
import type { EChartsType } from 'echarts/core'
import { LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { fetchAdminStatsOverview, fetchAdminStatsTrend } from '@/api/admin'
import type { StatsOverview, StatsTrend } from '@/types/admin'

echarts.use([LineChart, GridComponent, TooltipComponent, LegendComponent, CanvasRenderer])

const overview = ref<StatsOverview | null>(null)
const loading = ref(false)
const trendEl = ref<HTMLDivElement | null>(null)
let chart: EChartsType | null = null

const cards = [
  { key: 'userCount', label: '用户总数', icon: '🧑', color: '#4f7cff' },
  { key: 'bookCount', label: '书籍总数', icon: '📚', color: '#ff8f5a' },
  { key: 'orderCount', label: '订单总数', icon: '📦', color: '#7ac05a' },
  { key: 'todayOrders', label: '今日订单', icon: '🕘', color: '#9a7cff' },
  { key: 'todayNewUsers', label: '今日新增书友', icon: '✨', color: '#3ec6b0' },
  { key: 'totalAmount', label: '累计交易额', icon: '💰', color: '#4f7cff' },
  { key: 'pendingRefund', label: '待处理售后', icon: '🛠', color: '#f56c6c' },
]

function cardValue(key: string): string {
  const o = overview.value
  if (!o) return '—'
  if (key === 'totalAmount') return `¥${o.totalAmount}`
  return String((o as unknown as Record<string, number>)[key] ?? 0)
}

function renderTrend(list: StatsTrend[]) {
  if (!chart || !trendEl.value) return
  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['订单数', '新增用户'] },
    grid: { left: 40, right: 20, top: 40, bottom: 30 },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: list.map((i) => i.date.slice(5)),
    },
    yAxis: { type: 'value', minInterval: 1 },
    series: [
      {
        name: '订单数',
        type: 'line',
        smooth: true,
        data: list.map((i) => i.orderCount),
        itemStyle: { color: '#4f7cff' },
        areaStyle: { opacity: 0.08 },
      },
      {
        name: '新增用户',
        type: 'line',
        smooth: true,
        data: list.map((i) => i.userCount),
        itemStyle: { color: '#ff8f5a' },
        areaStyle: { opacity: 0.08 },
      },
    ],
  })
}

function onResize() {
  chart?.resize()
}

onMounted(async () => {
  loading.value = true
  try {
    const [ov, trend] = await Promise.all([fetchAdminStatsOverview(), fetchAdminStatsTrend(7)])
    overview.value = ov
    if (trendEl.value) {
      chart = echarts.init(trendEl.value)
      renderTrend(trend)
    }
    window.addEventListener('resize', onResize)
  } finally {
    loading.value = false
  }
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', onResize)
  chart?.dispose()
  chart = null
})
</script>

<template>
  <div v-loading="loading" class="stats">
    <div class="stats__cards">
      <div v-for="c in cards" :key="c.key" class="card">
        <span class="card__icon" :style="{ background: c.color + '1a', color: c.color }">{{ c.icon }}</span>
        <div class="card__meta">
          <span class="card__label">{{ c.label }}</span>
          <span class="card__value">{{ cardValue(c.key) }}</span>
        </div>
      </div>
    </div>

    <div class="stats__chart">
      <h3 class="stats__title">近 7 日趋势</h3>
      <div ref="trendEl" class="stats__canvas" />
    </div>
  </div>
</template>

<style scoped lang="scss">
.stats {
  &__cards {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
    gap: var(--sp-4);
    margin-bottom: var(--sp-4);
  }

  &__chart {
    background: var(--card);
    border: 1px solid var(--line);
    border-radius: var(--radius);
    padding: var(--sp-4);
  }

  &__title {
    font-family: var(--font-serif);
    font-size: 1rem;
    margin-bottom: var(--sp-3);
  }

  &__canvas {
    height: 320px;
  }
}

.card {
  display: flex;
  align-items: center;
  gap: var(--sp-3);
  background: var(--card);
  border: 1px solid var(--line);
  border-radius: var(--radius);
  padding: var(--sp-4);

  &__icon {
    display: grid;
    place-items: center;
    width: 44px;
    height: 44px;
    border-radius: 12px;
    font-size: 20px;
  }

  &__meta {
    display: flex;
    flex-direction: column;
    gap: 2px;
  }

  &__label {
    color: var(--muted);
    font-size: 0.85rem;
  }

  &__value {
    font-size: 1.35rem;
    font-weight: 700;
    font-variant-numeric: tabular-nums;
  }
}
</style>
