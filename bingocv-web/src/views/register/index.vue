<template>
    <div class="register-container">
        <div class="register-box">
            <div class="logo-section">
                <div class="logo-icon">👤</div>
                <h1>{{ t('register') }}</h1>
            </div>

            <div class="form-group">
                <label>{{ t('username') }}</label>
                <input v-model="form.username" type="text" placeholder="请输入用户名">
            </div>

            <div class="form-group">
                <label>{{ t('password') }}</label>
                <div class="password-input">
                    <input v-model="form.password" :type="showPassword ? 'text' : 'password'" placeholder="请输入密码">
                    <span class="password-toggle" @click="showPassword = !showPassword">
                        {{ showPassword ? '🙈' : '👁️' }}
                    </span>
                </div>
            </div>

            <div class="form-group">
                <label>{{ t('confirmPassword') }}</label>
                <div class="password-input">
                    <input v-model="form.confirmPassword" :type="showPassword ? 'text' : 'password'" placeholder="请确认密码">
                    <span class="password-toggle" @click="showPassword = !showPassword">
                        {{ showPassword ? '🙈' : '👁️' }}
                    </span>
                </div>
            </div>

            <div class="form-group captcha-group">
                <label>{{ t('captcha') }}</label>
                <div class="captcha-input">
                    <input v-model="form.captcha" type="text" placeholder="请输入验证码">
                    <img :src="captchaUrl" @click="refreshCaptcha" class="captcha-img" alt="验证码">
                </div>
            </div>

            <div class="agree-group">
                <label class="agree-label">
                    <input type="checkbox" v-model="form.agree">
                    <span>{{ t('agreeProtocol') }}</span>
                </label>
            </div>

            <button class="register-btn" @click="handleRegister" :disabled="loading">
                <span v-if="loading" class="loading-icon">⏳</span>
                {{ t('register') }}
            </button>

            <p class="login-link">
                {{ t('haveAccount') }}? <a @click="goToLogin">{{ t('login') }}</a>
            </p>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { ElMessage } from 'element-plus';
import { register, getCaptcha } from '@/request/login.js';

const { t } = useI18n();
const router = useRouter();

const form = ref({
    username: '',
    password: '',
    confirmPassword: '',
    captcha: '',
    agree: false
});

const showPassword = ref(false);
const loading = ref(false);
const captchaUrl = ref('');
const captchaId = ref('');

const refreshCaptcha = () => {
    getCaptcha().then(res => {
        const blob = new Blob([res.blob], { type: 'image/png' });
        captchaUrl.value = URL.createObjectURL(blob);
        captchaId.value = res.captchaId;
    }).catch(e => {
        console.error('获取验证码失败:', e);
    });
};

const handleRegister = async () => {
    if (!form.value.username || !form.value.password || !form.value.captcha) {
        ElMessage.warning(t('fillAllFields'));
        return;
    }

    if (form.value.password !== form.value.confirmPassword) {
        ElMessage.warning(t('passwordNotMatch'));
        return;
    }

    if (!form.value.agree) {
        ElMessage.warning(t('pleaseAgree'));
        return;
    }

    loading.value = true;
    try {
        await register(form.value.username, form.value.password, form.value.captcha, captchaId.value);
        ElMessage.success(t('registerSuccess'));
        router.push('/login');
    } catch (error) {
        ElMessage.error(error.msg || error.message || t('registerFailed'));
        refreshCaptcha();
    } finally {
        loading.value = false;
    }
};

const goToLogin = () => {
    router.push('/login');
};

onMounted(() => {
    refreshCaptcha();
});
</script>

<style scoped>
.register-container {
    min-height: 100vh;
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
}

.register-box {
    background: #fff;
    border-radius: 16px;
    padding: 40px;
    width: 100%;
    max-width: 420px;
    box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
}

.logo-section {
    text-align: center;
    margin-bottom: 30px;
}

.logo-icon {
    font-size: 56px;
    margin-bottom: 15px;
}

.logo-section h1 {
    font-size: 28px;
    color: #333;
    margin: 0;
}

.form-group {
    margin-bottom: 20px;
}

.form-group label {
    display: block;
    font-size: 14px;
    color: #555;
    margin-bottom: 8px;
    font-weight: 500;
}

.form-group input {
    width: 100%;
    padding: 12px 15px;
    border: 1px solid #ddd;
    border-radius: 8px;
    font-size: 14px;
    box-sizing: border-box;
    transition: border-color 0.3s;
}

.form-group input:focus {
    outline: none;
    border-color: #11998e;
}

.password-input {
    position: relative;
}

.password-toggle {
    position: absolute;
    right: 12px;
    top: 50%;
    transform: translateY(-50%);
    font-size: 18px;
    cursor: pointer;
}

.captcha-group {
    margin-bottom: 15px;
}

.captcha-input {
    display: flex;
    gap: 10px;
}

.captcha-input input {
    flex: 1;
}

.captcha-img {
    width: 120px;
    height: 40px;
    border-radius: 8px;
    cursor: pointer;
    object-fit: contain;
    background: #f8f9fa;
}

.agree-group {
    margin-bottom: 20px;
}

.agree-label {
    display: flex;
    align-items: flex-start;
    gap: 8px;
    font-size: 13px;
    color: #666;
    cursor: pointer;
    line-height: 1.5;
}

.agree-label input {
    width: auto;
    margin-top: 2px;
}

.register-btn {
    width: 100%;
    padding: 14px;
    background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
    color: #fff;
    border: none;
    border-radius: 8px;
    font-size: 16px;
    cursor: pointer;
    transition: all 0.3s;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10px;
}

.register-btn:hover:not(:disabled) {
    transform: translateY(-2px);
    box-shadow: 0 4px 15px rgba(17, 153, 142, 0.4);
}

.register-btn:disabled {
    opacity: 0.7;
    cursor: not-allowed;
}

.loading-icon {
    font-size: 18px;
}

.login-link {
    text-align: center;
    color: #666;
    font-size: 14px;
    margin-top: 20px;
}

.login-link a {
    color: #11998e;
    text-decoration: none;
    cursor: pointer;
}

.login-link a:hover {
    text-decoration: underline;
}
</style>
