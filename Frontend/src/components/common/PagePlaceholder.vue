<script setup lang="ts">
defineProps<{
  title: string
  desc: string
}>()
</script>

<template>
  <section class="section">
    <div class="container">
      <div class="placeholder">
        <h1 class="placeholder__title">{{ title }}</h1>
        <p class="placeholder__desc">{{ desc }}</p>
        <span class="placeholder__badge">建设中</span>
      </div>
    </div>
  </section>
</template>

<style scoped lang="scss">
.placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: var(--sp-8) 0;

  &__title {
    font-family: var(--font-serif);
    font-size: 2rem;
    font-weight: 700;
    color: var(--ink);
  }

  &__desc {
    margin-top: var(--sp-3);
    max-width: 480px;
    color: var(--muted);
  }

  &__badge {
    margin-top: var(--sp-5);
    padding: 6px 16px;
    border-radius: 999px;
    border: 1px solid var(--line);
    color: var(--muted);
    font-size: 0.85rem;
  }
}
</style>
