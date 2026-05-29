import http from '@/axios/index.js'

export function getMyProfile() {
    return http.get('/profile/me')
}

export function updateMyProfile(data) {
    return http.put('/profile/me', data)
}

export function getProfileById(id) {
    return http.get(`/profile/${id}`)
}