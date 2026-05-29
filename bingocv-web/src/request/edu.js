import http from '@/axios/index.js'

export function listEdu() {
    return http.get('/edu/list')
}

export function addEdu(data) {
    return http.post('/edu/insert', data)
}

export function updateEdu(data) {
    return http.put('/edu/update', data)
}

export function deleteEdu(id) {
    return http.delete(`/edu/${id}`)
}