import http from '@/axios/index.js';

export function getTemplateMarket(params = {}) {
  return http.get('/template/market', { params });
}

export function getMyTemplates() {
  return http.get('/template/mine');
}

export function acquireTemplate(templateId) {
  return http.post(`/template/${templateId}/acquire`);
}

export function activateTemplate(templateId) {
  return http.post(`/template/${templateId}/activate`);
}

export function listTemplates() {
  return http.get('/template/market');
}
