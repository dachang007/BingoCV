import { ElMessage } from 'element-plus'

function normalizeMessage(message, fallback = '操作失败') {
  if (!message) return fallback
  if (typeof message === 'string') return message
  return message.msg || message.message || fallback
}

function show(type, message, options = {}) {
  return ElMessage({
    message: normalizeMessage(message, options.fallback),
    type,
    duration: options.duration ?? 2200,
    showClose: options.showClose ?? false,
    grouping: true,
    offset: options.offset ?? 20
  })
}

// 统一前端轻提示入口，保持 Element Plus 默认样式，避免登录页和后台操作提示风格不一致。
export const feedback = {
  success(message, options) {
    return show('success', message, { fallback: '操作成功', ...options })
  },
  error(message, options) {
    return show('error', message, { fallback: '操作失败', ...options })
  },
  warning(message, options) {
    return show('warning', message, { fallback: '请检查输入内容', ...options })
  },
  info(message, options) {
    return show('info', message, { fallback: '提示', ...options })
  }
}

export function resolveErrorMessage(error, fallback = '操作失败') {
  return error?.msg
    || error?.message
    || error?.response?.data?.msg
    || error?.response?.data?.message
    || fallback
}
