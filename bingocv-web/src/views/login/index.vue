<template>
  <div class="auth-page">
    <section class="auth-card">
      <div class="auth-header">
        <span class="brand-mark">BingoCV</span>
        <h1>后台登录</h1>
        <p>登录后继续管理你的在线简历、模板和分享链接</p>
      </div>

      <el-form :model="form" class="auth-form" @submit.prevent="submit" label-position="top">
        <el-form-item label="用户名">
          <el-input v-model.trim="form.username" placeholder="请输入用户名" autocomplete="username" />
        </el-form-item>

        <el-form-item label="密码">
          <el-input
            v-model="form.password"
            placeholder="请输入密码"
            type="password"
            show-password
            autocomplete="current-password"
          />
        </el-form-item>

        <el-form-item label="验证码">
          <div class="captcha-row">
            <el-input v-model.trim="form.captcha" placeholder="请输入验证码" autocomplete="off" @keyup.enter="submit" />
            <button class="captcha-box" type="button" @click="refreshCaptcha">
              <img v-if="captchaUrl" :src="captchaUrl" alt="验证码" />
              <span v-else>刷新</span>
            </button>
          </div>
        </el-form-item>

        <div class="form-extra">
          <el-checkbox v-model="form.rememberMe">记住账号</el-checkbox>
        </div>

        <el-button class="submit-btn" type="primary" :loading="loginLoading" @click="submit">
          登录
        </el-button>
      </el-form>

      <button class="switch-btn" type="button" @click="goToRegister">
        还没有账号？<span>去注册</span>
      </button>
    </section>
  </div>
</template>

<script setup>
import { onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getCaptcha, login } from '@/request/login.js'
import { useUserStore } from '@/store/user.js'
import { buildPermKeysByRole, getHomePathByRole } from '@/perm/permissions.js'
import { feedback, resolveErrorMessage } from '@/utils/feedback.js'

const router = useRouter()
const userStore = useUserStore()
const loginLoading = ref(false)
const captchaUrl = ref('')
const captchaId = ref('')

const form = reactive({
  username: '',
  password: '',
  captcha: '',
  rememberMe: false
})

function releaseCaptchaUrl() {
  if (captchaUrl.value) {
    URL.revokeObjectURL(captchaUrl.value)
    captchaUrl.value = ''
  }
}

async function refreshCaptcha() {
  try {
    const data = await getCaptcha()
    releaseCaptchaUrl()
    captchaUrl.value = URL.createObjectURL(new Blob([data.blob], { type: 'image/png' }))
    captchaId.value = data.captchaId
  } catch (error) {
    feedback.error('验证码加载失败')
  }
}

onMounted(() => {
  refreshCaptcha()
  if (localStorage.getItem('rememberMe') === 'true') {
    form.rememberMe = true
    form.username = localStorage.getItem('savedUsername') || ''
  }
})

onBeforeUnmount(releaseCaptchaUrl)

function goToRegister() {
  router.push('/register')
}

async function submit() {
  if (!form.username) {
    feedback.warning('请输入用户名')
    return
  }
  if (!form.password) {
    feedback.warning('请输入密码')
    return
  }
  if (!form.captcha) {
    feedback.warning('请输入验证码')
    return
  }

  loginLoading.value = true
  try {
    const data = await login(form.username, form.password, form.captcha, captchaId.value)
    const user = data.user || data
    const profile = data.profile || {}
    const role = user.role || 'USER'
    const userData = {
      id: user.userid || user.id,
      username: user.username,
      email: profile.email || user.username,
      name: profile.name || user.nickname || user.username,
      avatar: user.avatar || '',
      role: { name: role },
      permKeys: buildPermKeysByRole(role)
    }

    const userInfo = btoa(unescape(encodeURIComponent(JSON.stringify(userData))))
    localStorage.setItem('userInfo', userInfo)
    userStore.user = userData

    if (form.rememberMe) {
      localStorage.setItem('rememberMe', 'true')
      localStorage.setItem('savedUsername', form.username)
    } else {
      localStorage.removeItem('rememberMe')
      localStorage.removeItem('savedUsername')
    }

    await router.replace(getHomePathByRole(role))
    // 路由跳转结束后再弹提示，避免登录组件卸载时消息被样式或时机影响。
    requestAnimationFrame(() => feedback.success('登录成功'))
  } catch (error) {
    await refreshCaptcha()
    feedback.error(resolveErrorMessage(error, '登录失败，请检查账号、密码或验证码'))
  } finally {
    loginLoading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32px 16px;
  background:
    radial-gradient(circle at top left, var(--el-color-primary-light-9), transparent 34%),
    var(--el-bg-color-page);
}

.auth-card {
  width: min(100%, 420px);
  padding: 34px;
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;
  background: var(--el-bg-color);
  box-shadow: var(--el-box-shadow-light);
}

.auth-header {
  margin-bottom: 26px;
  text-align: center;
}

.brand-mark {
  display: inline-flex;
  margin-bottom: 12px;
  color: var(--el-color-primary);
  font-size: 16px;
  font-weight: 700;
}

.auth-header h1 {
  margin: 0 0 8px;
  color: var(--el-text-color-primary);
  font-size: 26px;
  font-weight: 700;
  letter-spacing: 0;
}

.auth-header p {
  margin: 0;
  color: var(--el-text-color-secondary);
  font-size: 14px;
  line-height: 1.6;
}

.auth-form {
  width: 100%;
}

:deep(.el-form-item__label) {
  color: var(--el-text-color-primary);
  font-weight: 500;
}

:deep(.el-input__wrapper) {
  min-height: 42px;
}

.captcha-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 112px;
  gap: 12px;
  width: 100%;
}

.captcha-box {
  height: 42px;
  padding: 0;
  border: 1px solid var(--el-border-color);
  border-radius: 6px;
  background: var(--el-fill-color-light);
  color: var(--el-text-color-secondary);
  cursor: pointer;
  overflow: hidden;
}

.captcha-box img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.form-extra {
  display: flex;
  justify-content: flex-start;
  margin: -2px 0 18px;
}

.submit-btn {
  width: 100%;
  height: 42px;
  font-size: 15px;
}

.switch-btn {
  display: block;
  width: 100%;
  margin-top: 22px;
  color: var(--el-text-color-secondary);
  text-align: center;
  font-size: 14px;
  cursor: pointer;
}

.switch-btn span {
  color: var(--el-color-primary);
  font-weight: 600;
}

@media (max-width: 520px) {
  .auth-card {
    padding: 26px 18px;
  }

  .captcha-row {
    grid-template-columns: minmax(0, 1fr) 96px;
  }
}
</style>
