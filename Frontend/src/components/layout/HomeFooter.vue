<script setup lang="ts">
const year = new Date().getFullYear()
const cols = [
  { title: '书市', links: ['浏览书籍', '发布闲置', '交易规则', '信誉体系'] },
  { title: '社区', links: ['读书心得', '书评', '闲置交流', '帮助中心'] },
  { title: '关于', links: ['关于书循环', '加入我们', '隐私政策', '用户协议'] },
]
</script>

<template>
  <footer class="footer">
    <div class="container footer__inner">
      <div class="footer__brand">
        <span class="footer__seal">书</span>
        <p class="footer__name">书循环 <em>BookCycle</em></p>
        <p class="footer__slogan">让每一本书，都继续它的旅程。</p>
      </div>

      <div v-for="col in cols" :key="col.title" class="footer__col">
        <h4>{{ col.title }}</h4>
        <ul>
          <li v-for="link in col.links" :key="link">
            <a href="#">{{ link }}</a>
          </li>
        </ul>
      </div>
    </div>
    <div class="footer__bar">
      <div class="container footer__bar-inner">
        <span>© {{ year }} 书循环 BookCycle</span>
        <span>书循环，让阅读更轻</span>
      </div>
    </div>
  </footer>
</template>

<style scoped lang="scss">
.footer {
  background: var(--ink);
  color: rgba(255, 255, 255, 0.82);
  padding-top: var(--sp-7);

  &__inner {
    display: grid;
    grid-template-columns: 1.6fr 1fr 1fr 1fr;
    gap: var(--sp-6);
    padding-bottom: var(--sp-7);
  }

  &__seal {
    display: grid;
    place-items: center;
    width: 40px;
    height: 40px;
    border-radius: 10px;
    background: #fff;
    color: var(--ink);
    font-family: var(--font-serif);
    font-size: 20px;
  }

  &__name {
    margin-top: var(--sp-3);
    font-family: var(--font-serif);
    font-size: 1.25rem;
    font-weight: 700;
    color: #fff;

    em {
      font-style: normal;
      font-size: 0.62rem;
      font-weight: 600;
      letter-spacing: 0.14em;
      color: rgba(255, 255, 255, 0.5);
      margin-left: 6px;
    }
  }

  &__slogan {
    margin-top: var(--sp-2);
    font-size: 0.85rem;
    color: rgba(255, 255, 255, 0.55);
  }

  &__col {
    h4 {
      font-size: 0.92rem;
      font-weight: 600;
      color: #fff;
      margin-bottom: var(--sp-4);
    }

    li {
      margin-bottom: var(--sp-2);

      a {
        font-size: 0.86rem;
        color: rgba(255, 255, 255, 0.62);
        transition: color 0.2s ease;

        &:hover {
          color: #fff;
        }
      }
    }
  }

  &__bar {
    border-top: 1px solid rgba(255, 255, 255, 0.12);

    &-inner {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding-top: var(--sp-4);
      padding-bottom: var(--sp-4);
      font-size: 0.8rem;
      color: rgba(255, 255, 255, 0.45);
    }
  }
}

@media (max-width: 860px) {
  .footer__inner {
    grid-template-columns: 1fr 1fr;
  }
}
</style>
