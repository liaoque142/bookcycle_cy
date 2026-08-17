<script setup lang="ts">
import HomeHero from '@/components/home/HomeHero.vue'
import HomeStats from '@/components/home/HomeStats.vue'
import HomeCategories from '@/components/home/HomeCategories.vue'
import HomeBookSection from '@/components/home/HomeBookSection.vue'
import HomeForum from '@/components/home/HomeForum.vue'
import HomeFeatures from '@/components/home/HomeFeatures.vue'
import { featuredBooks, latestBooks } from '@/data/books'
import { posts } from '@/data/posts'
</script>

<template>
  <main>
    <HomeHero />
    <HomeStats />
    <HomeCategories />
    <HomeBookSection title="精选书市" subtitle="书友们正在流转的好书" :books="featuredBooks" />
    <HomeBookSection title="最新上架" subtitle="刚刚进入循环的书" :books="latestBooks" />
    <HomeForum :posts="posts" />
    <HomeFeatures />
  </main>
</template>
