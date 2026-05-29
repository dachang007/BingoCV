import http from '@/axios/index.js';

export function getPointsDashboard() {
  return http.get('/points/dashboard');
}

export function signIn() {
  return http.post('/points/sign-in');
}
