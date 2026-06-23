import http from '@/axios/index.js'

export function getMyCoupons() {
  return http.get('/coupon/mine')
}

export function claimCoupon(couponCode) {
  return http.post('/coupon/claim', { couponCode })
}

export function getAdminCoupons(params) {
  return http.get('/coupon/admin/list', { params })
}

export function createCoupons(data) {
  return http.post('/coupon/admin/create', data)
}

export function updateCouponStatus(id, status) {
  return http.put(`/coupon/admin/${id}/status`, null, { params: { status } })
}
