<script setup lang="ts">
import { computed, ref } from 'vue'

const props = withDefaults(
  defineProps<{ src?: string; name: string; size?: number; muted?: boolean }>(),
  { size: 40, muted: false },
)

const broken = ref(false)
const showImg = computed(() => !!props.src && !broken.value)
const fallback = computed(() => (props.name || '?').slice(0, 1))
</script>

<template>
  <span
    class="avatar"
    :class="{ 'avatar--muted': muted }"
    :style="{ width: `${size}px`, height: `${size}px`, fontSize: `${Math.round(size * 0.45)}px` }"
  >
    <img v-if="showImg" :src="src" alt="" class="avatar__img" @error="broken = true" />
    <template v-else>{{ fallback }}</template>
  </span>
</template>

<style scoped lang="scss">
.avatar {
  display: inline-grid;
  place-items: center;
  width: 40px;
  height: 40px;
  flex-shrink: 0;
  border-radius: 50%;
  background: var(--mark);
  color: #fff;
  font-family: var(--font-serif);
  overflow: hidden;

  &--muted {
    background: var(--muted);
  }

  &__img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}
</style>
