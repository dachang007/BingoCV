import http from '@/axios/index.js';

/**
 * 获取用户列表
 * @param {Object} params - 查询参数
 * @param {number} params.pageNum - 页码
 * @param {number} params.pageSize - 每页数量
 * @param {string} params.username - 用户名搜索
 * @param {number} params.status - 状态筛选
 */
export function getUserList(params) {
    return http.get('/user/list', { params });
}

/**
 * 更新用户状态
 * @param {number} userId - 用户ID
 * @param {number} status - 状态（0-正常，1-禁用）
 */
export function updateUserStatus(userId, status) {
    return http.put(`/user/${userId}/status`, null, { params: { status } });
}
