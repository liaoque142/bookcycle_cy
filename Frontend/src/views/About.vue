<script setup lang="ts">
import { Star, Lock, RefreshRight, Reading, ShoppingCart, ChatLineRound, UserFilled, Promotion, Medal, SetUp, Sell } from '@element-plus/icons-vue'

/** 功能一览 */
const features = [
  { icon: Reading, title: '书市', text: '按关键词 / 分类 / 新旧程度 / 交易方式筛选好书，价格透明、状态实时。' },
  { icon: Sell, title: '发布与流转', text: '一本书可以出售、免费赠送或以书换书，让闲置重新流动起来。' },
  { icon: ShoppingCart, title: '交易保障', text: '订单全流程跟踪，CAS 防超卖，模拟支付与退款售后覆盖完整链路。' },
  { icon: ChatLineRound, title: '论坛社区', text: '读书心得、书评、闲置交流……与同好分享阅读的乐趣。' },
  { icon: UserFilled, title: '好友与私信', text: '加书友、拉黑、双向私信，交易之外也保持人与人的连接。' },
  { icon: Promotion, title: '以书换书', text: '用你的在售书换 TA 的书，协商、双向发货、双向确认收货。' },
  { icon: Medal, title: '信誉体系', text: '初始 100 分，交易互评影响信誉，规则数据驱动、后台可配置。' },
  { icon: Star, title: '透明可查', text: '卖家信誉、交易记录、操作日志，可信度处处可见。' },
  { icon: SetUp, title: '管理后台', text: '用户 / 分类 / 书籍 / 交易 / 售后 / 统计一应俱全，运营可控。' },
]

/** 使用指南步骤 */
const steps = [
  { no: '01', title: '注册登录', text: '手机号或邮箱即可注册，游客也能浏览书市与讨论。' },
  { no: '02', title: '发现与发布', text: '逛逛书市，或把闲置书发布出来，选择出售 / 赠送 / 交换。' },
  { no: '03', title: '完成交易', text: '下单、模拟支付、卖家发货、确认收货，全程可追踪。' },
  { no: '04', title: '互评与循环', text: '交易完成后互评，积累信誉，让每一本书继续它的旅程。' },
]
</script>

<template>
  <section class="section about">
    <div class="container">
      <!-- Hero -->
      <div class="hero">
        <span class="hero__seal">书</span>
        <h1 class="hero__title">书循环 <em>BookCycle</em></h1>
        <p class="hero__slogan">让每一本书，都继续它的旅程。</p>
        <p class="hero__desc">
          一个融合二手书交易与社区论坛的 Web 应用。书籍在这里循环利用，阅读成本因此降低，
          一个可信、有温度的读书社区由此建立。
        </p>
      </div>

      <!-- 平台理念 -->
      <div class="block">
        <div class="block__head">
          <h2 class="section-title">平台理念</h2>
          <p class="section-sub">四个词，串起整个书循环</p>
        </div>
        <div class="ideas">
          <div v-for="i in [
            { icon: RefreshRight, title: '循环', text: '书不闲置。出售、交换、赠送，让知识持续流动。' },
            { icon: Medal, title: '信任', text: '信誉分与互评机制，让每一次交易都有据可依。' },
            { icon: Star, title: '温度', text: '不止是交易平台，更是书友交流的精神角落。' },
            { icon: Lock, title: '安心', text: '防超卖、售后、拉黑防护，保障双方权益。' },
          ]" :key="i.title" class="idea">
            <span class="idea__icon"><el-icon :size="20"><component :is="i.icon" /></el-icon></span>
            <div>
              <h3 class="idea__title">{{ i.title }}</h3>
              <p class="idea__text">{{ i.text }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 功能一览 -->
      <div class="block">
        <div class="block__head">
          <h2 class="section-title">功能一览</h2>
          <p class="section-sub">从发现一本书，到读完再流转，全程覆盖</p>
        </div>
        <div class="grid">
          <div v-for="f in features" :key="f.title" class="grid__item">
            <span class="grid__icon"><el-icon :size="20"><component :is="f.icon" /></el-icon></span>
            <h3 class="grid__title">{{ f.title }}</h3>
            <p class="grid__text">{{ f.text }}</p>
          </div>
        </div>
      </div>

      <!-- 信誉体系 -->
      <div class="block block--dark">
        <div class="block__head">
          <h2 class="section-title">信誉体系</h2>
          <p class="section-sub">数据驱动，后台可配置，让守信的人被看见</p>
        </div>
        <div class="credit">
          <div class="credit__card">
            <strong class="credit__num">100</strong>
            <span class="credit__label">初始信誉分</span>
          </div>
          <ul class="credit__rules">
            <li><span class="credit__plus">+2</span> 成功完成交易</li>
            <li><span class="credit__plus">+1</span> 获得好评（评分 ≥ 4）</li>
            <li><span class="credit__minus">−3</span> 恶意取消订单</li>
            <li><span class="credit__minus">−5</span> 超时未发货</li>
            <li><span class="credit__minus">−10</span> 虚假描述 / 违规行为</li>
          </ul>
          <p class="credit__note">信誉分低于 60 禁止购买，低于 40 冻结交易；管理员可调整并记录操作日志。</p>
        </div>
      </div>

      <!-- 交易与安全 -->
      <div class="block">
        <div class="block__head">
          <h2 class="section-title">交易与安全</h2>
          <p class="section-sub">模拟支付已覆盖全流程，真实支付接入预留资质</p>
        </div>
        <div class="safety">
          <div v-for="s in [
            { title: '防超卖', text: '下单时书籍状态 CAS 原子更新，多人同时下单也只成功一位。' },
            { title: '售后保障', text: '待发货仅退款、待收货退货退款，后台统一处理并原路退款。' },
            { title: '双向发货', text: '换书模式下双方各自发货、各自确认收货，状态机严格推进。' },
            { title: '隐私边界', text: '私信仅双方可见，历史与未读查询以本人为边界，第三者天然不可见。' },
          ]" :key="s.title" class="safety__item">
            <h3 class="safety__title">{{ s.title }}</h3>
            <p class="safety__text">{{ s.text }}</p>
          </div>
        </div>
      </div>

      <!-- 使用指南 -->
      <div class="block">
        <div class="block__head">
          <h2 class="section-title">开始使用</h2>
          <p class="section-sub">四步，完成一次书循环</p>
        </div>
        <div class="steps">
          <div v-for="s in steps" :key="s.no" class="steps__item">
            <span class="steps__no">{{ s.no }}</span>
            <h3 class="steps__title">{{ s.title }}</h3>
            <p class="steps__text">{{ s.text }}</p>
          </div>
        </div>
      </div>

      <!-- 免责声明 -->
      <div class="notice">
        <p class="notice__text">
          书循环目前为教学演示项目：支付为模拟实现，交易数据仅保存在本机数据库，请勿当真金交易使用。
          若你认同「让书循环起来」的理念，欢迎在这里开始你的第一本书。
        </p>
      </div>
    </div>
  </section>
</template>

<style scoped lang="scss">
.about {
  min-height: 60vh;
}

.hero {
  text-align: center;
  padding: var(--sp-8) 0 var(--sp-7);

  &__seal {
    display: inline-grid;
    place-items: center;
    width: 64px;
    height: 64px;
    border-radius: 16px;
    background: var(--ink);
    color: #fff;
    font-family: var(--font-serif);
    font-size: 32px;
    margin-bottom: var(--sp-5);
  }

  &__title {
    font-family: var(--font-serif);
    font-size: 2.2rem;
    font-weight: 700;
    letter-spacing: 0.08em;

    em {
      font-style: normal;
      font-family: var(--font-sans);
      font-size: 0.9rem;
      font-weight: 600;
      letter-spacing: 0.18em;
      color: var(--muted);
      margin-left: 10px;
    }
  }

  &__slogan {
    margin-top: var(--sp-4);
    font-family: var(--font-serif);
    font-size: 1.3rem;
    color: var(--mark);
    letter-spacing: 0.06em;
  }

  &__desc {
    max-width: 560px;
    margin: var(--sp-4) auto 0;
    font-size: 0.95rem;
    color: var(--muted);
    line-height: 1.9;
  }
}

.block {
  padding: var(--sp-7) 0;
  border-top: 1px solid var(--line);

  &__head {
    margin-bottom: var(--sp-6);
  }

  &--dark {
    background: var(--ink);
    border-radius: var(--radius);
    padding: var(--sp-7) var(--sp-6);
    margin: var(--sp-3) 0;

    .section-title,
    .section-sub {
      color: #fff;
    }

    .section-sub {
      opacity: 0.7;
    }
  }
}

.ideas {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--sp-5);
}

.idea {
  display: flex;
  gap: var(--sp-4);
  padding: var(--sp-5);
  background: var(--card);
  border: 1px solid var(--line);
  border-radius: var(--radius);

  &__icon {
    flex-shrink: 0;
    display: grid;
    place-items: center;
    width: 44px;
    height: 44px;
    border-radius: 50%;
    background: var(--paper);
    color: var(--mark);
  }

  &__title {
    font-family: var(--font-serif);
    font-size: 1.05rem;
    font-weight: 700;
  }

  &__text {
    margin-top: var(--sp-1);
    font-size: 0.88rem;
    color: var(--muted);
    line-height: 1.8;
  }
}

.grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--sp-5);
}

.grid__item {
  padding: var(--sp-5);
  background: var(--card);
  border: 1px solid var(--line);
  border-radius: var(--radius);

  &__icon {
    display: inline-grid;
    place-items: center;
    width: 40px;
    height: 40px;
    border-radius: 10px;
    background: var(--paper);
    color: var(--mark);
  }

  &__title {
    margin-top: var(--sp-3);
    font-family: var(--font-serif);
    font-size: 1.05rem;
    font-weight: 700;
  }

  &__text {
    margin-top: var(--sp-1);
    font-size: 0.88rem;
    color: var(--muted);
    line-height: 1.8;
  }
}

.credit {
  display: flex;
  align-items: center;
  gap: var(--sp-6);
  flex-wrap: wrap;

  &__card {
    flex-shrink: 0;
    text-align: center;
    padding: var(--sp-6) var(--sp-7);
    background: rgba(255, 255, 255, 0.06);
    border: 1px solid rgba(255, 255, 255, 0.12);
    border-radius: var(--radius);
  }

  &__num {
    display: block;
    font-family: var(--font-serif);
    font-size: 3rem;
    color: var(--mark);
    line-height: 1;
  }

  &__label {
    display: block;
    margin-top: var(--sp-2);
    font-size: 0.85rem;
    color: rgba(255, 255, 255, 0.7);
  }

  &__rules {
    flex: 1;
    min-width: 260px;
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: var(--sp-2) var(--sp-6);

    li {
      display: flex;
      align-items: center;
      gap: var(--sp-2);
      font-size: 0.9rem;
      color: #fff;
    }
  }

  &__plus,
  &__minus {
    display: inline-grid;
    place-items: center;
    min-width: 34px;
    padding: 2px 8px;
    border-radius: 999px;
    font-size: 0.78rem;
    font-variant-numeric: tabular-nums;
  }

  &__plus {
    background: rgba(122, 191, 153, 0.18);
    color: #7abf99;
  }

  &__minus {
    background: rgba(217, 83, 79, 0.18);
    color: #e0807c;
  }

  &__note {
    width: 100%;
    margin-top: var(--sp-3);
    font-size: 0.82rem;
    color: rgba(255, 255, 255, 0.6);
  }
}

.safety {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--sp-5);

  &__item {
    padding: var(--sp-5);
    background: var(--card);
    border: 1px solid var(--line);
    border-radius: var(--radius);
  }

  &__title {
    font-family: var(--font-serif);
    font-size: 1.05rem;
    font-weight: 700;
    color: var(--mark);
  }

  &__text {
    margin-top: var(--sp-1);
    font-size: 0.88rem;
    color: var(--muted);
    line-height: 1.8;
  }
}

.steps {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--sp-5);
}

.steps__item {
  position: relative;
  padding: var(--sp-6) var(--sp-5) var(--sp-5);
  background: var(--card);
  border: 1px solid var(--line);
  border-radius: var(--radius);

  &__no {
    position: absolute;
    top: var(--sp-4);
    right: var(--sp-5);
    font-family: var(--font-serif);
    font-size: 1.6rem;
    font-weight: 700;
    color: var(--line);
  }

  &__title {
    font-family: var(--font-serif);
    font-size: 1.05rem;
    font-weight: 700;
  }

  &__text {
    margin-top: var(--sp-1);
    font-size: 0.86rem;
    color: var(--muted);
    line-height: 1.8;
  }
}

.notice {
  margin-top: var(--sp-3);
  padding: var(--sp-4) var(--sp-5);
  background: var(--paper);
  border: 1px dashed var(--line);
  border-radius: var(--radius-sm);

  &__text {
    font-size: 0.84rem;
    color: var(--muted);
    line-height: 1.8;
  }
}

@media (max-width: 860px) {
  .ideas,
  .safety {
    grid-template-columns: 1fr;
  }

  .grid {
    grid-template-columns: 1fr 1fr;
  }

  .steps {
    grid-template-columns: 1fr 1fr;
  }

  .credit__rules {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 560px) {
  .grid,
  .steps {
    grid-template-columns: 1fr;
  }
}
</style>
