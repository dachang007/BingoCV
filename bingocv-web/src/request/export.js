import http from '@/axios/index.js'

/**
 * 导出当前用户简历。
 * @param {Object} params 导出参数
 * @param {string} params.format 导出格式：pdf、word、markdown
 * @param {string} params.templateKey 模板标识
 * @returns {Promise<Blob>}
 */
export async function exportResume(params) {
  const data = await http.post('/export/resume', params, {
    responseType: 'blob'
  })
  return data?.blob || data
}

/**
 * 管理员导出指定用户简历。
 * @param {Object} params 导出参数
 * @param {number|string} params.userId 用户ID
 * @param {string} params.format 导出格式
 * @param {string} params.templateKey 模板标识
 * @returns {Promise<Blob>}
 */
export async function exportResumeByUserId(params) {
  const data = await http.post('/export/resume/user', params, {
    responseType: 'blob'
  })
  return data?.blob || data
}
