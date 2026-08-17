<script setup lang="ts">
import HomeHeader from '@/components/layout/HomeHeader.vue'
import HomeFooter from '@/components/layout/HomeFooter.vue'
</script>

<template>
  <HomeHeader />
  <router-view />
  <HomeFooter />
</template>
