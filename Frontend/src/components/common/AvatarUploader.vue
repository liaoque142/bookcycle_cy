<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Camera } from '@element-plus/icons-vue'
import { uploadImage } from '@/api/upload'
import Avatar from './Avatar.vue'

const props = withDefaults(
  defineProps<{ modelValue?: string; name: string; size?: number }>(),
  { size: 96 },
)

const emit = defineEmits<{ 'update:modelValue': [value: string] }>()

const MAX_SIZE = 5 * 1024 * 1024
const uploading = ref(false)
const inputRef = ref<HTMLInputElement>()

/** 仅接受 jpg/png：type 或扩展名任一命中即可（file.type 可能为空或非标准值） */
function isJpgPng(file: File) {
  const t = file.type.toLowerCase()
  if (t === 'image/jpeg' || t === 'image/png') return true
  return /\.(jpe?g|png)$/i.test(file.name)
}

function pick() {
  inputRef.value?.click()
}

async function onFileChange(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  input.value = ''
  if (!file) return
  if (!isJpgPng(file)) {
    ElMessage.warning('仅支持 jpg / png 格式')
    return
  }
  if (file.size > MAX_SIZE) {
    ElMessage.warning('头像图片不能超过 5MB')
    return
  }
  uploading.value = true
  try {
    const url = await uploadImage(file, 'avatar')
    emit('update:modelValue', url)
  } catch (err) {
    ElMessage.error((err as Error).message || '上传失败')
  } finally {
    uploading.value = false
  }
}
</script>

<template>
  <div class="avatar-uploader" :style="{ width: `${size}px`, height: `${size}px` }">
    <input ref="inputRef" type="file" accept="image/jpeg,image/png,.jpg,.jpeg,.png" class="avatar-uploader__input" @change="onFileChange" />
    <button type="button" class="avatar-uploader__btn" :title="modelValue ? '更换头像' : '设置头像'" @click="pick">
      <Avatar :src="modelValue" :name="name" :size="size" />
      <span class="avatar-uploader__mask">
        <el-icon v-if="!uploading"><Camera /></el-icon>
        {{ uploading ? '上传中…' : '更换' }}
      </span>
    </button>
  </div>
</template>

<style scoped lang="scss">
.avatar-uploader {
  position: relative;
  flex-shrink: 0;

  &__input {
    display: none;
  }

  &__btn {
    position: relative;
    display: block;
    padding: 0;
    border: none;
    background: none;
    cursor: pointer;
    border-radius: 50%;
  }

  &__mask {
    position: absolute;
    inset: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 2px;
    border-radius: 50%;
    background: rgba(0, 0, 0, 0.45);
    color: #fff;
    font-size: 12px;
    opacity: 0;
    transition: opacity 0.2s ease;

    .avatar-uploader__btn:hover & {
      opacity: 1;
    }
  }
}
</style>
