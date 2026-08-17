<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const activeTab = ref<'login' | 'register'>('login')
const loginLoading = ref(false)
const registerLoading = ref(false)

const loginForm = reactive({ account: '', password: '' })
const registerForm = reactive({ account: '', password: '', confirm: '', nickname: '' })

const loginFormRef = ref<FormInstance>()
const registerFormRef = ref<FormInstance>()

const accountValidator = (_rule: unknown, value: string, callback: (err?: Error) => void) => {
  if (!value) return callback(new Error('请输入手机号或邮箱'))
  const isPhone = /^1[3-9]\d{9}$/.test(value)
  const isEmail = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)
  if (!isPhone && !isEmail) return callback(new Error('请输入正确的手机号或邮箱'))
  callback()
}

const loginRules: FormRules = {
  account: [{ validator: accountValidator, trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 6, message: '密码至少 6 位', trigger: 'blur' }],
}

const registerRules: FormRules = {
  account: [{ validator: accountValidator, trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' },
  ],
  confirm: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (_r, value: string, callback: (err?: Error) => void) => {
        if (value !== registerForm.password) return callback(new Error('两次密码不一致'))
        callback()
      },
      trigger: 'blur',
    },
  ],
  nickname: [{ max: 20, message: '昵称最多 20 个字符', trigger: 'blur' }],
}

function afterAuth(message: string) {
  ElMessage.success(message)
  const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/'
  router.push(redirect)
}

async function onLogin() {
  const valid = await loginFormRef.value?.validate().catch(() => false)
  if (!valid) return
  loginLoading.value = true
  try {
    await authStore.login(loginForm)
    afterAuth('登录成功')
  } catch (e) {
    ElMessage.error((e as Error).message || '登录失败')
  } finally {
    loginLoading.value = false
  }
}

async function onRegister() {
  const valid = await registerFormRef.value?.validate().catch(() => false)
  if (!valid) return
  registerLoading.value = true
  try {
    await authStore.register({
      account: registerForm.account,
      password: registerForm.password,
      nickname: registerForm.nickname || undefined,
    })
    afterAuth('注册成功，已自动登录')
  } catch (e) {
    ElMessage.error((e as Error).message || '注册失败')
  } finally {
    registerLoading.value = false
  }
}
</script>

<template>
  <section class="section login">
    <div class="container">
      <div class="login__card">
        <div class="login__brand">
          <span class="login__seal">书</span>
          <h1 class="login__title">书循环</h1>
          <p class="login__slogan">让每一本书，都继续它的旅程。</p>
        </div>

        <el-tabs v-model="activeTab" class="login__tabs" stretch>
          <el-tab-pane label="登录" name="login">
            <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" label-position="top" @submit.prevent="onLogin">
              <el-form-item label="手机号 / 邮箱" prop="account">
                <el-input v-model="loginForm.account" placeholder="请输入手机号或邮箱" size="large" />
              </el-form-item>
              <el-form-item label="密码" prop="password">
                <el-input v-model="loginForm.password" type="password" show-password placeholder="请输入密码" size="large" @keyup.enter="onLogin" />
              </el-form-item>
              <el-button type="primary" round size="large" class="login__submit" :loading="loginLoading" @click="onLogin">
                登 录
              </el-button>
            </el-form>
          </el-tab-pane>

          <el-tab-pane label="注册" name="register">
            <el-form ref="registerFormRef" :model="registerForm" :rules="registerRules" label-position="top" @submit.prevent="onRegister">
              <el-form-item label="手机号 / 邮箱" prop="account">
                <el-input v-model="registerForm.account" placeholder="请输入手机号或邮箱" size="large" />
              </el-form-item>
              <el-form-item label="昵称（选填）" prop="nickname">
                <el-input v-model="registerForm.nickname" placeholder="设置一个昵称" size="large" />
              </el-form-item>
              <el-form-item label="密码" prop="password">
                <el-input v-model="registerForm.password" type="password" show-password placeholder="至少 6 位" size="large" />
              </el-form-item>
              <el-form-item label="确认密码" prop="confirm">
                <el-input v-model="registerForm.confirm" type="password" show-password placeholder="再次输入密码" size="large" @keyup.enter="onRegister" />
              </el-form-item>
              <el-button type="primary" round size="large" class="login__submit" :loading="registerLoading" @click="onRegister">
                注 册
              </el-button>
            </el-form>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </section>
</template>

<style scoped lang="scss">
.login {
  &__card {
    max-width: 420px;
    margin: 0 auto;
    padding: var(--sp-7) var(--sp-6);
    background: var(--card);
    border: 1px solid var(--line);
    border-radius: var(--radius);
    box-shadow: var(--shadow-card);
  }

  &__brand {
    text-align: center;
    margin-bottom: var(--sp-6);
  }

  &__seal {
    display: inline-grid;
    place-items: center;
    width: 52px;
    height: 52px;
    border-radius: 12px;
    background: var(--ink);
    color: #fff;
    font-family: var(--font-serif);
    font-size: 26px;
  }

  &__title {
    margin-top: var(--sp-3);
    font-family: var(--font-serif);
    font-size: 1.5rem;
    font-weight: 700;
  }

  &__slogan {
    margin-top: 4px;
    font-size: 0.85rem;
    color: var(--muted);
  }

  &__tabs {
    :deep(.el-tabs__item) {
      font-size: 1rem;
    }
  }

  &__submit {
    width: 100%;
    margin-top: var(--sp-2);
  }
}
</style>
