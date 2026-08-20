<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Picture, Close } from '@element-plus/icons-vue'
import { uploadImage } from '@/api/upload'

const props = defineProps<{ modelValue?: string }>()
const emit = defineEmits<{ 'update:modelValue': [value: string] }>()

const MAX_SIZE = 5 * 1024 * 1024
const uploading = ref(false)
const inputRef = ref<HTMLInputElement>()

function pick() {
  inputRef.value?.click()
}

async function onFileChange(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  input.value = ''
  if (!file) return
  if (!file.type.startsWith('image/')) {
    ElMessage.warning('仅支持图片文件')
    return
  }
  if (file.size > MAX_SIZE) {
    ElMessage.warning('图片不能超过 5MB')
    return
  }
  uploading.value = true
  try {
    const url = await uploadImage(file)
    emit('update:modelValue', url)
  } catch (err) {
    ElMessage.error((err as Error).message || '上传失败')
  } finally {
    uploading.value = false
  }
}

function remove() {
  emit('update:modelValue', '')
}
</script>

<template>
  <div class="uploader">
    <input ref="inputRef" type="file" accept="image/*" class="uploader__input" @change="onFileChange" />
    <el-button v-if="!modelValue" size="small" :icon="Picture" :loading="uploading" @click="pick">
      图片
    </el-button>
    <div v-else class="uploader__preview">
      <img :src="modelValue" alt="待发送图片" class="uploader__img" />
      <el-button class="uploader__remove" size="small" circle :icon="Close" title="移除图片" @click="remove" />
    </div>
  </div>
</template>

<style scoped lang="scss">
.uploader {
  display: inline-flex;
  align-items: center;

  &__input {
    display: none;
  }

  &__preview {
    position: relative;
    display: inline-flex;
  }

  &__img {
    width: 72px;
    height: 72px;
    object-fit: cover;
    border-radius: var(--radius-sm);
    border: 1px solid var(--line);
  }

  &__remove {
    position: absolute;
    top: -8px;
    right: -8px;
    width: 20px;
    height: 20px;
    padding: 0;
    background: var(--ink);
    border-color: var(--ink);
    color: #fff;

    &:hover {
      background: #000;
      border-color: #000;
    }

    :deep(.el-icon) {
      font-size: 12px;
    }
  }
}
</style>
