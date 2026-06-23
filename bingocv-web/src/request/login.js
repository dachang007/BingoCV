import http from '@/axios/index.js';

// ============ bingoCV 适配 ============

/**
 * bingoCV 用户登录
 * @param username 用户名
 * @param password 密码
 * @param captcha 验证码
 * @param captchaId 验证码ID
 */
export function login(username, password, captcha, captchaId) {
    return http.post('/user/login', {
        username: username,
        password: password,
        captcha: captcha,
        captchaId: captchaId
    })
}

/**
 * 获取验证码图片（返回图片流和验证码ID）
 * @returns Promise<{blob: Blob, captchaId: string}>
 */
export function getCaptcha() {
    return http.get('/captcha/image', {
        responseType: 'blob',
        noMsg: true
    })
}

/**
 * bingoCV 用户注册
 * @param username 用户名
 * @param password 密码
 * @param captcha 验证码
 * @param captchaId 验证码ID
 */
export function register(username, password, captcha, captchaId) {
    return http.post('/user/register', {
        username: username,
        password: password,
        captcha: captcha,
        captchaId: captchaId
    })
}

/**
 * 用户登出
 */
export function logout() {
    // 后端当前没有真正的会话退出接口，前端退出只需要清理本地登录态。
    return Promise.resolve()
}
