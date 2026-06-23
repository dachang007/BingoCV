import http from '@/axios/index.js'

// 后台数据看板，聚合用户、简历、分享、积分和最近操作。
export function getAdminDashboard() {
  return http.get('/admin/api/dashboard')
}

// 系统配置列表，keyword 可按配置键或说明过滤。
export function getSystemConfigs(params) {
  return http.get('/admin/api/configs', { params })
}

// 保存单项系统配置，主要用于 AI、限流等运行参数。
export function updateSystemConfig(id, data) {
  return http.put(`/admin/api/configs/${id}`, data)
}

// 操作日志分页列表，keyword 可搜索用户、路径和模块。
export function getOperationLogs(params) {
  return http.get('/admin/api/operation-logs', { params })
}

export function getAiUsageLogs(params) {
  return http.get('/admin/api/ai-usage-logs', { params })
}
