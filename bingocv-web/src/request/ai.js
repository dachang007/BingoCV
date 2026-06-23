import http from '@/axios/index.js'

export function analyzeResume() {
  return http.get('/ai/resume/analyze')
}

export function getResumeAgentPlan() {
  return http.get('/ai/resume/agent-plan')
}

export function matchResumeJd(data) {
  return http.post('/ai/resume/jd-match', data)
}

export function polishResumeText(data) {
  return http.post('/ai/resume/polish', data)
}

export function getInterviewQuestions(params = {}) {
  return http.get('/ai/interview/questions', { params })
}

export function generateInterviewQuestions(data) {
  return http.post('/ai/interview/questions/generate', data)
}

export function updateInterviewQuestion(id, data) {
  return http.put(`/ai/interview/questions/${id}`, data)
}

export function deleteInterviewQuestion(id) {
  return http.delete(`/ai/interview/questions/${id}`)
}
