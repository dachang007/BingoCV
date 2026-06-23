import axios from 'axios'
import router from '@/router'
import i18n from '@/i18n/index.js'
import { useSettingStore } from '@/store/setting.js'
import { feedback } from '@/utils/feedback.js'

const http = axios.create({
  baseURL: import.meta.env.VITE_BASE_URL
})

http.interceptors.request.use(config => {
  const { lang } = useSettingStore()
  const userInfo = localStorage.getItem('userInfo')
  if (userInfo) {
    config.headers['USER-INFO'] = userInfo
  }
  config.headers['accept-language'] = lang || 'zh-CN'
  return config
})

http.interceptors.response.use((res) => {
  const data = res.data
  const noMsg = res.config.noMsg

  if (res.config.responseType === 'blob' || res.headers['content-type']?.includes('image')) {
    return {
      blob: res.data,
      captchaId: res.headers['x-captcha-id'] || res.headers['X-Captcha-Id'] || ''
    }
  }

  if (data.code === 200) {
    return data.data
  }

  const message = data.msg || data.message || '操作失败'
  if (noMsg) {
    return Promise.reject(data)
  }

  if (data.code === 600 || data.code === 401 || data.code === 40003) {
    const url = res.config.url || ''
    if (url.includes('/login') || url.includes('/register') || url.includes('/captcha')) {
      return Promise.reject(data)
    }
    feedback.error(message || '您还未登录，请先登录')
    localStorage.removeItem('userInfo')
    router.replace('/admin/login')
    return Promise.reject(data)
  }

  if (data.code === 403) {
    feedback.warning(message)
  } else {
    feedback.error(message)
  }
  return Promise.reject(data)
}, (error) => {
  if (error?.status === 403) {
    return Promise.reject(error)
  }

  if (error?.config?.noMsg) {
    return Promise.reject(error)
  }

  if (error?.message?.includes('Network Error')) {
    feedback.error(i18n.global.t('networkErrorMsg'))
  } else if (error?.code === 'ECONNABORTED') {
    feedback.error(i18n.global.t('timeoutErrorMsg'))
  } else if (error?.response) {
    feedback.error(i18n.global.t('serverBusyErrorMsg'))
  } else {
    feedback.error(i18n.global.t('reqFailErrorMsg'))
  }
  return Promise.reject(error)
})

export default http
