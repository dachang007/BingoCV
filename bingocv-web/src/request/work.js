import http from '@/axios/index.js'

export function listWork() {
    return http.get('/work/list')
}

export function addWork(data) {
    return http.post('/work/insert', data)
}

export function updateWork(data) {
    return http.put('/work/update', data)
}

export function deleteWork(id) {
    return http.delete(`/work/${id}`)
}