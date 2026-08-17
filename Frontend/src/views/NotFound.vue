<script setup lang="ts">
</script>

<template>
  <section class="section">
    <div class="container">
      <div class="not-found">
        <p class="not-found__code">404</p>
        <h1 class="not-found__title">页面走丢了</h1>
        <p class="not-found__desc">你访问的页面不存在或已被移除。</p>
        <router-link to="/" class="not-found__back">返回首页</router-link>
      </div>
    </div>
  </section>
</template>

<style scoped lang="scss">
.not-found {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: var(--sp-8) 0;

  &__code {
    font-family: var(--font-serif);
    font-size: 4.5rem;
    font-weight: 700;
    color: var(--mark);
    line-height: 1;
  }

  &__title {
    margin-top: var(--sp-3);
    font-family: var(--font-serif);
    font-size: 1.6rem;
    font-weight: 700;
    color: var(--ink);
  }

  &__desc {
    margin-top: var(--sp-2);
    color: var(--muted);
  }

  &__back {
    margin-top: var(--sp-5);
    display: inline-block;
    padding: 10px 28px;
    border-radius: 999px;
    background: var(--ink);
    color: #fff;
    font-size: 0.95rem;
    transition: opacity 0.2s ease;

    &:hover {
      opacity: 0.85;
    }
  }
}
</style>
