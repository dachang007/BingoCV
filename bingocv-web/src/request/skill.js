import http from '@/axios/index.js'

export function getSkill() {
    return http.get('/skill/get')
}

export function addSkill(data) {
    return http.post('/skill/insert', data)
}

export function updateSkill(data) {
    return http.put('/skill/update', data)
}