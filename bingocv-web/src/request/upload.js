import http from '@/axios/index.js'

/**
 * 上传头像图片。
 * @param {File} file 图片文件
 * @returns {Promise<{path: string, url: string}>}
 */
export function uploadAvatar(file) {
  const formData = new FormData()
  formData.append('file', file)
  return http.post('/upload/avatar', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 上传通用图片。
 * @param {File} file 图片文件
 * @returns {Promise<{path: string, url: string}>}
 */
export function uploadImage(file) {
  const formData = new FormData()
  formData.append('file', file)
  return http.post('/upload/image', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
