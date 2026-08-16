<script setup lang="ts">
const stats = [
  { value: '12,400+', label: '在售书籍' },
  { value: '86,000+', label: '已循环次数' },
  { value: '32,000+', label: '注册书友' },
]
</script>

<template>
  <section class="stats">
    <div class="container stats__inner">
      <div v-for="s in stats" :key="s.label" class="stats__item">
        <span class="stats__value">{{ s.value }}</span>
        <span class="stats__label">{{ s.label }}</span>
      </div>
    </div>
  </section>
</template>

<style scoped lang="scss">
.stats {
  border-top: 1px solid var(--line);
  border-bottom: 1px solid var(--line);
  background: var(--card);

  &__inner {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
  }

  &__item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 2px;
    padding: var(--sp-6) 0;
    text-align: center;

    & + & {
      border-left: 1px solid var(--line);
    }
  }

  &__value {
    font-family: var(--font-serif);
    font-size: 1.8rem;
    font-weight: 700;
    font-variant-numeric: tabular-nums;
    letter-spacing: 0.02em;
  }

  &__label {
    font-size: 0.85rem;
    color: var(--muted);
    letter-spacing: 0.1em;
  }
}

@media (max-width: 640px) {
  .stats__value {
    font-size: 1.3rem;
  }
}
</style>
