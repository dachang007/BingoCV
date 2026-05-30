import axios from "axios";
import router from "@/router";
import i18n from "@/i18n/index.js";
import {useSettingStore} from "@/store/setting.js";
import {ElMessage} from "element-plus";

let http = axios.create({
    baseURL: import.meta.env.VITE_BASE_URL
});

http.interceptors.request.use(config => {
    const { lang } = useSettingStore();
    const userInfo = localStorage.getItem('userInfo');
    if (userInfo) {
        config.headers['USER-INFO'] = userInfo;
    }
    config.headers['accept-language'] = lang || 'zh-CN';
    return config
})

http.interceptors.response.use((res) => {

        return new Promise((resolve, reject) => {

            const noMsg = res.config.noMsg;
            const data = res.data;

            if (res.config.responseType === 'blob' || res.headers['content-type']?.includes('image')) {
                resolve({
                    blob: res.data,
                    captchaId: res.headers['x-captcha-id'] || res.headers['X-Captcha-Id'] || ''
                });
                return;
            }

            if (data.code === 200) {
                resolve(data.data);
            } else if (noMsg) {
                reject(data);
            } else if (data.code === 600) {
                const url = res.config.url || '';
                // 登录/注册/验证码请求，直接reject让调用方处理错误提示
                if (url.includes('/login') || url.includes('/register') || url.includes('/captcha')) {
                    reject(data);
                } else {
                    // 其他请求，显示未登录提示并跳转
                    ElMessage({
                        message: data.message || '您还未登录，请先登录',
                        type: 'error',
                        plain: true,
                        grouping: true,
                        repeatNum: -4,
                    })
                    localStorage.removeItem('userInfo')
                    router.replace('/admin/login')
                    reject(data)
                }
            } else if (data.code === 401) {
                ElMessage({
                    message: data.message,
                    type: 'error',
                    plain: true,
                    grouping: true,
                    repeatNum: -4,
                })
                localStorage.removeItem('userInfo')
                router.replace('/admin/login')
                reject(data)
            } else if (data.code === 403) {
                ElMessage({
                    message: data.message,
                    type: 'warning',
                    plain: true,
                    grouping: true,
                    repeatNum: -4,
                })
                reject(data)
            } else if (data.code === 502) {
                ElMessage({
                    dangerouslyUseHTMLString: true,
                    message: data.message,
                    type: 'error',
                    plain: true,
                    grouping: true,
                    repeatNum: -4,
                })
                reject(data)
            } else {
                ElMessage({
                    message: data.message || data.msg || '操作失败',
                    type: 'error',
                    plain: true,
                    grouping: true,
                    repeatNum: -4,
                })
                reject(data)
            }
        })
    },
    (error) => {

        if (error?.status === 403) {
            location.reload();
            return;
        }

        const noMsg = error?.config?.noMsg;

        if (noMsg) {
            return Promise.reject(error)
        } else if (error?.message?.includes('Network Error')) {
            ElMessage({
                message: i18n.global.t('networkErrorMsg'),
                type: 'error',
                plain: true,
                grouping: true,
                repeatNum: -4,
            })
        } else if (error?.code === 'ECONNABORTED') {
            ElMessage({
                message: i18n.global.t('timeoutErrorMsg'),
                type: 'error',
                plain: true,
                grouping: true
            })
        } else if (error?.response) {
            ElMessage({
                message: i18n.global.t('serverBusyErrorMsg'),
                type: 'error',
                plain: true,
                grouping: true,
                repeatNum: -4,
            })
        } else {
            ElMessage({
                message: i18n.global.t('reqFailErrorMsg'),
                type: 'error',
                plain: true,
                grouping: true,
                repeatNum: -4,
            })
        }
        return Promise.reject(error)
    })

export default http
