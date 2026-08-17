<script setup lang="ts">
import { ref, onMounted } from 'vue'
import HomeHero from '@/components/home/HomeHero.vue'
import HomeStats from '@/components/home/HomeStats.vue'
import HomeCategories from '@/components/home/HomeCategories.vue'
import HomeBookSection from '@/components/home/HomeBookSection.vue'
import HomeForum from '@/components/home/HomeForum.vue'
import HomeFeatures from '@/components/home/HomeFeatures.vue'
import { fetchFeaturedBooks, fetchLatestBooks } from '@/api/book'
import { fetchForumPosts } from '@/api/forum'
import type { Book, ForumPost } from '@/types/book'

const featuredBooks = ref<Book[]>([])
const latestBooks = ref<Book[]>([])
const posts = ref<ForumPost[]>([])

onMounted(async () => {
  const [f, l, p] = await Promise.all([
    fetchFeaturedBooks(),
    fetchLatestBooks(),
    fetchForumPosts(),
  ])
  featuredBooks.value = f
  latestBooks.value = l
  posts.value = p
})
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
