<template>
  <div class="auth-page">
    <section class="auth-card">
      <div class="auth-header">
        <span class="brand-mark">BingoCV</span>
        <h1>用户注册</h1>
        <p>创建账号后开始维护你的在线简历和求职资料</p>
      </div>

      <el-form :model="form" class="auth-form" @submit.prevent="handleRegister" label-position="top">
        <el-form-item label="用户名">
          <el-input v-model.trim="form.username" placeholder="请输入用户名" autocomplete="username" />
        </el-form-item>

        <el-form-item label="密码">
          <el-input
            v-model="form.password"
            placeholder="请输入密码"
            type="password"
            show-password
            autocomplete="new-password"
          />
        </el-form-item>

        <el-form-item label="确认密码">
          <el-input
            v-model="form.confirmPassword"
            placeholder="请再次输入密码"
            type="password"
            show-password
            autocomplete="new-password"
          />
        </el-form-item>

        <el-form-item label="验证码">
          <div class="captcha-row">
            <el-input v-model.trim="form.captcha" placeholder="请输入验证码" autocomplete="off" @keyup.enter="handleRegister" />
            <button class="captcha-box" type="button" @click="refreshCaptcha">
              <img v-if="captchaUrl" :src="captchaUrl" alt="验证码" />
              <span v-else>刷新</span>
            </button>
          </div>
        </el-form-item>

        <el-checkbox v-model="form.agree" class="agree-check">
          我已阅读并同意用户协议和隐私政策
        </el-checkbox>

        <el-button class="submit-btn" type="primary" :loading="loading" @click="handleRegister">
          注册
        </el-button>
      </el-form>

      <button class="switch-btn" type="button" @click="goToLogin">
        已有账号？<span>去登录</span>
      </button>
    </section>
  </div>
</template>

<script setup>
import { onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getCaptcha, register } from '@/request/login.js'
import { feedback, resolveErrorMessage } from '@/utils/feedback.js'

const router = useRouter()
const loading = ref(false)
const captchaUrl = ref('')
const captchaId = ref('')

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  captcha: '',
  agree: false
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

async function handleRegister() {
  if (!form.username || !form.password || !form.confirmPassword || !form.captcha) {
    feedback.warning('请填写完整注册信息')
    return
  }
  if (form.password !== form.confirmPassword) {
    feedback.warning('两次输入的密码不一致')
    return
  }
  if (!form.agree) {
    feedback.warning('请先阅读并同意用户协议和隐私政策')
    return
  }

  loading.value = true
  try {
    await register(form.username, form.password, form.captcha, captchaId.value)
    await router.push('/admin/login')
    // 注册成功后切到登录页再提示，用户能明确知道下一步是在登录页操作。
    requestAnimationFrame(() => feedback.success('注册成功，请登录'))
  } catch (error) {
    feedback.error(resolveErrorMessage(error, '注册失败，请稍后重试'))
    await refreshCaptcha()
  } finally {
    loading.value = false
  }
}

function goToLogin() {
  router.push('/admin/login')
}

onMounted(refreshCaptcha)
onBeforeUnmount(releaseCaptchaUrl)
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

.agree-check {
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
