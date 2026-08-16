<script setup lang="ts">
import { Star, Lock, RefreshRight } from '@element-plus/icons-vue'

const features = [
  {
    icon: Star,
    title: '信誉可查',
    text: '每笔交易完成后双方互评，信誉分透明可见，谁更值得信赖一目了然。',
  },
  {
    icon: Lock,
    title: '交易安心',
    text: '订单状态全程跟踪，防超卖保障，遇到问题可以申请售后处理。',
  },
  {
    icon: RefreshRight,
    title: '循环有温度',
    text: '让闲置书再次被翻开：出售、交换或赠与他人，一起降低阅读成本。',
  },
]
</script>

<template>
  <section class="section">
    <div class="container">
      <div class="section-head">
        <div>
          <h2 class="section-title">为什么选择书循环</h2>
          <p class="section-sub">一个可信、好用的二手书社区</p>
        </div>
      </div>

      <div class="feats">
        <div v-for="f in features" :key="f.title" class="feats__item">
          <span class="feats__icon">
            <el-icon :size="22"><component :is="f.icon" /></el-icon>
          </span>
          <h3 class="feats__title">{{ f.title }}</h3>
          <p class="feats__text">{{ f.text }}</p>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped lang="scss">
.feats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--sp-5);
}

.feats__item {
  padding: var(--sp-6);
  background: var(--card);
  border: 1px solid var(--line);
  border-radius: var(--radius);
  text-align: center;

  &__icon {
    display: inline-grid;
    place-items: center;
    width: 48px;
    height: 48px;
    border-radius: 50%;
    background: var(--paper);
    color: var(--mark);
  }

  &__title {
    margin-top: var(--sp-4);
    font-family: var(--font-serif);
    font-size: 1.15rem;
    font-weight: 700;
  }

  &__text {
    margin-top: var(--sp-2);
    font-size: 0.9rem;
    color: var(--muted);
    line-height: 1.8;
  }
}

@media (max-width: 860px) {
  .feats {
    grid-template-columns: 1fr;
  }
}
</style>
