import http from '@/axios/index.js';

export function getShareList() {
  return http.get('/share/list');
}

export function createShare(data) {
  return http.post('/share', data);
}

export function updateShare(id, data) {
  return http.put(`/share/${id}`, data);
}

export function closeShare(id) {
  return http.delete(`/share/${id}`);
}

export function getShareStats(id) {
  return http.get(`/share/${id}/stats`);
}

export function openPublicShare(shortCode, data = {}) {
  return http.post(`/public/share/${shortCode}`, data, { noMsg: true });
}
