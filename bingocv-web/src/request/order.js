import http from '@/axios/index.js'

export function createOrder(data) {
  return http.post('/order/create', data)
}

export function payOrder(orderId, payChannel = 'MOCK') {
  return http.post(`/order/${orderId}/pay`, null, { params: { payChannel } })
}

export function closeOrder(orderId) {
  return http.post(`/order/${orderId}/close`)
}

export function getOrderDetail(orderId) {
  return http.get(`/order/${orderId}`)
}

export function getMyOrders(params) {
  return http.get('/order/mine', { params })
}

export function getAdminOrders(params) {
  return http.get('/order/admin/list', { params })
}

export function getAdminRefunds(params) {
  return http.get('/order/admin/refunds', { params })
}

export function adminCloseOrder(orderId, data) {
  return http.post(`/order/admin/${orderId}/close`, data)
}

export function adminRefundOrder(orderId, data) {
  return http.post(`/order/admin/${orderId}/refund`, data)
}

export function updateAdminOrderRemark(orderId, data) {
  return http.put(`/order/admin/${orderId}/remark`, data)
}
