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
