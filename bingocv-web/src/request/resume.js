import http from '@/axios/index.js';

export function getProfiles() {
  return http.get('/profile/me');
}

export function updateProfiles(data) {
  return http.put('/profile/me', data);
}

export function getEduList() {
  return http.get('/edu/list');
}

export function addEdu(data) {
  return http.post('/edu', data);
}

export function updateEdu(id, data) {
  return http.put(`/edu/${id}`, data);
}

export function deleteEdu(id) {
  return http.delete(`/edu/${id}`);
}

export function getWorkList() {
  return http.get('/work/list');
}

export function addWork(data) {
  return http.post('/work', data);
}

export function updateWork(id, data) {
  return http.put(`/work/${id}`, data);
}

export function deleteWork(id) {
  return http.delete(`/work/${id}`);
}

export function getSkill() {
  return http.get('/skill/me');
}

export function updateSkill(data) {
  return http.put('/skill/me', data);
}

export function getSpecialtyList() {
  return http.get('/specialty/list');
}

export function addSpecialty(data) {
  return http.post('/specialty', data);
}

export function updateSpecialty(id, data) {
  return http.put(`/specialty/${id}`, data);
}

export function deleteSpecialty(id) {
  return http.delete(`/specialty/${id}`);
}

export function getMyResume() {
  return http.get('/profile/me/full');
}
