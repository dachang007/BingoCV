<template>
  <div id="login-box">
    <div id="background-wrap">
      <div class="x1 cloud"></div>
      <div class="x2 cloud"></div>
      <div class="x3 cloud"></div>
      <div class="x4 cloud"></div>
      <div class="x5 cloud"></div>
    </div>
    <div class="form-wrapper">
      <div class="container">
        <span class="form-title">BingoCV</span>
        <span class="form-desc">欢迎登录-BingoCV</span>
        <div>
          <el-input v-model="form.username" type="text" :placeholder="$t('username')" autocomplete="off">
          </el-input>
          <el-input v-model="form.password" :placeholder="$t('password')" type="password" autocomplete="off" show-password>
          </el-input>
          <div class="captcha-row">
            <el-input v-model="form.captcha" :placeholder="$t('captcha')" type="text" autocomplete="off" style="width: 60%">
            </el-input>
            <div class="captcha-wrapper" @click="refreshCaptcha">
              <img v-if="captchaUrl" :src="captchaUrl" class="captcha-img" alt="验证码" />
              <div v-else class="captcha-loading">
                <svg class="loading-icon" viewBox="0 0 24 24" fill="none">
                  <circle class="loading-ring" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" stroke-linecap="round" stroke-dasharray="10 5" opacity="0.3"/>
                  <circle class="loading-spinner" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" stroke-linecap="round" stroke-dasharray="10 5">
                    <animateTransform attributeName="transform" type="rotate" from="0 12 12" to="360 12 12" dur="1s" repeatCount="indefinite"/>
                  </circle>
                </svg>
              </div>
            </div>
          </div>
          <div class="remember-row">
            <el-checkbox v-model="form.rememberMe" label="记住我" />
          </div>
          <el-button class="btn" type="primary" @click="submit" :loading="loginLoading">
            {{ $t('loginBtn') }}
          </el-button>
          <div class="switch" @click="goToRegister">{{ $t('noAccount') }} <span>{{ $t('regSwitch') }}</span></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import router from "@/router";
import {reactive, ref, onMounted} from "vue";
import {login, getCaptcha} from "@/request/login.js";
import {useUserStore} from "@/store/user.js";
import {useI18n} from "vue-i18n";
import {ElMessage} from "element-plus";

const {t} = useI18n();
const userStore = useUserStore();
const loginLoading = ref(false)

const form = reactive({
  username: '',
  password: '',
  captcha: '',
  rememberMe: false
});

const captchaUrl = ref('');
const captchaLoading = ref(false);
const captchaId = ref('');

const refreshCaptcha = () => {
  captchaLoading.value = true;
  getCaptcha().then(data => {
    const blob = new Blob([data.blob], { type: 'image/png' });
    captchaUrl.value = URL.createObjectURL(blob);
    captchaId.value = data.captchaId;
  }).catch(e => {
    console.error('获取验证码失败:', e);
  }).finally(() => {
    captchaLoading.value = false;
  });
};

onMounted(() => {
  refreshCaptcha();
  
  const rememberMe = localStorage.getItem('rememberMe');
  if (rememberMe === 'true') {
    form.rememberMe = true;
    form.username = localStorage.getItem('savedUsername') || '';
  }
});

const goToRegister = () => {
  router.push('/register');
};

const submit = () => {
  if (!form.username) {
    ElMessage({
      message: '请输入用户名',
      type: 'error',
      plain: true,
    })
    return
  }

  if (!form.password) {
    ElMessage({
      message: '请输入密码',
      type: 'error',
      plain: true,
    })
    return
  }

  if (!form.captcha) {
    ElMessage({
      message: '请输入验证码',
      type: 'error',
      plain: true,
    })
    return
  }

    loginLoading.value = true
  login(form.username, form.password, form.captcha, captchaId.value).then(async data => {
    const user = data.user || data;
    const profile = data.profile || {};
    const userData = {
      id: user.userid || user.id,
      username: user.username,
      email: user.username,
      name: profile.name || user.username,
      avatar: user.avatar || '',
      role: {
        name: user.role || 'user',
        accountCount: 1,
        sendType: 'ban',
        sendCount: 0
      },
      sendCount: 0,
      permKeys: ['*']
    };
    const userInfo = btoa(unescape(encodeURIComponent(JSON.stringify(userData))));
    localStorage.setItem('userInfo', userInfo);
    
    userStore.user = userData;
    
    if (form.rememberMe) {
      localStorage.setItem('rememberMe', 'true');
      localStorage.setItem('savedUsername', form.username);
    } else {
      localStorage.removeItem('rememberMe');
      localStorage.removeItem('savedUsername');
    }
    
    ElMessage({
      message: '登录成功',
      type: 'success',
      plain: true,
    })
    
    try {
      await router.replace('/profiles')
    } catch (err) {
      console.error('跳转失败:', err)
      window.location.href = '/profiles'
    }
  }).catch(e => {
    refreshCaptcha();
    // 提取错误信息，优先使用后端返回的msg，其次是message
    const errorMsg = e.msg || e.message || '登录失败';
    ElMessage({
      message: errorMsg,
      type: 'error',
      plain: true,
    })
  }).finally(() => {
    loginLoading.value = false
  })
}



</script>


<style lang="scss" scoped>

.form-wrapper {
  position: fixed;
  right: 0;
  height: 100%;
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: center;
  @media (max-width: 767px) {
    width: 100%;
  }
}

.container {
  background: rgba(255, 255, 255, 0.95);
  padding-left: 40px;
  padding-right: 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  width: 450px;
  height: 100%;
  border-left: 1px solid rgba(0, 0, 0, 0.1);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  @media (max-width: 1024px) {
    padding: 20px 18px;
    width: 384px;
    margin-left: 18px;
  }
  @media (max-width: 767px) {
    border: 1px solid rgba(0, 0, 0, 0.1);
    padding: 20px 18px;
    border-radius: 6px;
    height: fit-content;
    width: 100%;
    margin-right: 18px;
    margin-left: 18px;
  }

  .btn {
    height: 36px;
    width: 100%;
    border-radius: 6px;
  }

  .form-desc {
    margin-top: 5px;
    margin-bottom: 18px;
    color: #999;
  }

  .form-title {
    font-weight: bold;
    font-size: 22px !important;
  }

  .switch {
    margin-top: 20px;
    text-align: center;

    span {
      color: var(--login-switch-color);
      cursor: pointer;
    }
  }

  :deep(.el-input__wrapper) {
    border-radius: 6px;
    background: var(--el-bg-color);
  }

  .email-input :deep(.el-input__wrapper) {
    border-radius: 6px 0 0 6px;
    background: var(--el-bg-color);
  }

  .el-input {
    height: 38px;
    width: 100%;
    margin-bottom: 18px;

    :deep(.el-input__inner) {
      height: 36px;
    }
  }
}

:deep(.el-select-dropdown__item) {
  padding: 0 10px;
}

:deep(.bind-dialog) {
  width: 400px !important;
  @media (max-width: 440px) {
    width: calc(100% - 40px) !important;
    margin-right: 20px !important;
    margin-left: 20px !important;
  }
}

.bind-container {
  display: grid;
  grid-template-columns: 1fr;
  gap: 15px;
}

.setting-icon {
  position: relative;
  top: 6px;
}

.github {
  position: fixed;
  width: 35px;
  height: 35px;
  display: flex;
  justify-content: center;
  align-items: center;
  border-radius: 50%;
  background: var(--el-bg-color);
  bottom: 10px;
  right: 10px;
  z-index: 1000;
  border: 1px solid var(--el-border-color-light);
  box-shadow: var(--el-box-shadow-light);
  cursor: pointer;
}

:deep(.el-input-group__append) {
  padding: 0 !important;
  padding-left: 8px !important;
  padding-right: 4px !important;
  background: var(--el-bg-color);
  border-radius: 0 8px 8px 0;
}

:deep(.el-button+.el-button) {
  margin: 0;
}

.register-turnstile {
  margin-bottom: 18px;
}

.captcha-row {
  display: flex;
  gap: 10px;
  margin-bottom: 18px;
  
  .captcha-wrapper {
    width: 120px;
    height: 36px;
    border-radius: 6px;
    cursor: pointer;
    background: #f8f9fa;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  
  .captcha-img {
    width: 100%;
    height: 100%;
    border-radius: 6px;
    object-fit: contain;
  }
  
  .captcha-loading {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #999;
  }
  
  .loading-icon {
    width: 20px;
    height: 20px;
  }
  
  .loading-spinner {
    animation: spin 1s linear infinite;
  }
  
  @keyframes spin {
    from {
      transform: rotate(0deg);
    }
    to {
      transform: rotate(360deg);
    }
  }
}

.remember-row {
  margin-bottom: 18px;
  text-align: left;
  
  :deep(.el-checkbox) {
    --el-checkbox-label-color: var(--el-text-color-regular);
  }
}

.select {
  position: absolute;
  right: 30px;
  width: 100px;
  opacity: 0;
  pointer-events: none;
}

.custom-style {
  margin-bottom: 10px;
}

.custom-style .el-segmented {
  --el-border-radius-base: 6px;
  width: 180px;
}


#login-box {
  background: linear-gradient(to bottom, #2980b9, #6dd5fa, #fff);
  font: 100% Arial, sans-serif;
  height: 100%;
  margin: 0;
  padding: 0;
  overflow-x: hidden;
  display: grid;
  grid-template-columns: 1fr;
}


#background-wrap {
  height: 100%;
  z-index: 0;
}

@keyframes animateCloud {
  0% {
    margin-left: -500px;
  }

  100% {
    margin-left: 100%;
  }
}

.x1 {
  animation: animateCloud 30s linear infinite;
  transform: scale(0.65);
}

.x2 {
  animation: animateCloud 15s linear infinite;
  transform: scale(0.3);
}

.x3 {
  animation: animateCloud 25s linear infinite;
  transform: scale(0.5);
}

.x4 {
  animation: animateCloud 13s linear infinite;
  transform: scale(0.4);
}

.x5 {
  animation: animateCloud 20s linear infinite;
  transform: scale(0.55);
}

.cloud {
  background: linear-gradient(to bottom, #fff 5%, #f1f1f1 100%);
  border-radius: 100px;
  box-shadow: 0 8px 5px rgba(0, 0, 0, 0.1);
  height: 120px;
  width: 350px;
  position: relative;
}

.cloud:after,
.cloud:before {
  content: "";
  position: absolute;
  background: #fff;
  z-index: -1;
}

.cloud:after {
  border-radius: 100px;
  height: 100px;
  left: 50px;
  top: -50px;
  width: 100px;
}

.cloud:before {
  border-radius: 200px;
  height: 180px;
  width: 180px;
  right: 50px;
  top: -90px;
}

</style>
