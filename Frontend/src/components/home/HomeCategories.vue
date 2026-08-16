<script setup lang="ts">
import { Reading, Notebook, Collection, Cpu, Picture, Brush, Document, MoreFilled } from '@element-plus/icons-vue'
import { categories } from '@/data/books'

const iconMap: Record<string, unknown> = {
  Reading,
  Notebook,
  Collection,
  Cpu,
  Picture,
  Brush,
  Document,
  MoreFilled,
}
</script>

<template>
  <section class="section">
    <div class="container">
      <div class="section-head">
        <div>
          <h2 class="section-title">按分类逛逛</h2>
          <p class="section-sub">从你喜欢的类型开始</p>
        </div>
        <a href="#" class="section-more">全部分类 →</a>
      </div>

      <ul class="cats">
        <li v-for="c in categories" :key="c.name" class="cats__item">
          <span class="cats__icon">
            <el-icon :size="22">
              <component :is="iconMap[c.icon]" />
            </el-icon>
          </span>
          <span class="cats__name">{{ c.name }}</span>
          <span class="cats__count">{{ c.count }}</span>
        </li>
      </ul>
    </div>
  </section>
</template>

<style scoped lang="scss">
.cats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--sp-4);
}

.cats__item {
  display: flex;
  align-items: center;
  gap: var(--sp-3);
  padding: var(--sp-4);
  background: var(--card);
  border: 1px solid var(--line);
  border-radius: var(--radius);
  transition: box-shadow 0.2s ease, transform 0.2s ease;
  cursor: pointer;

  &:hover {
    box-shadow: var(--shadow-lift);
    transform: translateY(-3px);
  }

  &__icon {
    display: grid;
    place-items: center;
    width: 42px;
    height: 42px;
    flex-shrink: 0;
    border-radius: 10px;
    background: var(--paper);
    color: var(--mark);
  }

  &__name {
    font-size: 0.98rem;
    font-weight: 600;
  }

  &__count {
    margin-left: auto;
    font-size: 0.8rem;
    color: var(--muted);
    font-variant-numeric: tabular-nums;
  }
}

@media (max-width: 960px) {
  .cats {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
