import { defineStore } from 'pinia'
import {loginUserInfo} from "@/request/my.js";
import { buildPermKeysByRole } from '@/perm/permissions.js'

export const useUserStore = defineStore('user', {
    state: () => ({
        user: {},
        refreshList: 0,
    }),
    actions: {
        refreshUserList() {
            loginUserInfo().then(user => {
                this.refreshList ++
            })
        },
        refreshUserInfo() {
            loginUserInfo().then(user => {
                this.user = {
                    ...user,
                    permKeys: user?.permKeys?.length ? user.permKeys : buildPermKeysByRole(user?.role)
                }
            })
        }
    }
})
