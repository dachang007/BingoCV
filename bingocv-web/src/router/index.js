import {createRouter, createWebHistory} from 'vue-router'
import NProgress from 'nprogress';
import {useUiStore} from "@/store/ui.js";
import {useSettingStore} from "@/store/setting.js";

const routes = [
    {
        path: '/',
        name: 'layout',
        redirect: '/profiles',
        component: () => import('@/layout/index.vue'),
        children: [
            {
                path: '/profiles',
                name: 'profiles',
                component: () => import('@/views/profiles/index.vue'),
                meta: {
                    title: 'profiles',
                    name: 'profiles',
                    menu: true
                }
            },
            {
                path: '/education',
                name: 'education',
                component: () => import('@/views/education/index.vue'),
                meta: {
                    title: 'education',
                    name: 'education',
                    menu: true
                }
            },
            {
                path: '/work',
                name: 'work',
                component: () => import('@/views/work/index.vue'),
                meta: {
                    title: 'work',
                    name: 'work',
                    menu: true
                }
            },
            {
                path: '/skill',
                name: 'skill',
                component: () => import('@/views/skill/index.vue'),
                meta: {
                    title: 'skill',
                    name: 'skill',
                    menu: true
                }
            },
            {
                path: '/templates',
                name: 'templates',
                component: () => import('@/views/templates/index.vue'),
                meta: {
                    title: 'templates',
                    name: 'templates',
                    menu: true
                }
            },
            {
                path: '/points',
                name: 'points',
                component: () => import('@/views/points/index.vue'),
                meta: {
                    title: 'points',
                    name: 'points',
                    menu: true
                }
            },
            {
                path: '/share',
                name: 'share',
                component: () => import('@/views/share/index.vue'),
                meta: {
                    title: 'share',
                    name: 'share',
                    menu: true
                }
            },
            {
                path: '/user',
                name: 'user',
                component: () => import('@/views/user/index.vue'),
                meta: {
                    title: 'user',
                    name: 'user',
                    menu: true
                }
            }
        ]
    },
    {
        path: '/login',
        name: 'login',
        component: () => import('@/views/login/index.vue')
    },
    {
        path: '/register',
        name: 'register',
        component: () => import('@/views/register/index.vue')
    },
    {
        path: '/s/:code',
        name: 'public-share',
        component: () => import('@/views/public-share/index.vue')
    },
    {
        path: '/:pathMatch(.*)*',
        name: '404',
        component: () => import('@/views/404/index.vue')
    }
]

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes
})

NProgress.configure({
    showSpinner: false,
    trickleSpeed: 50,
    minimum: 0.1
});

let timer
let first = true

router.beforeEach((to, from, next) => {

    if (timer) {
        clearTimeout(timer)
    }

    if (!first) {
        timer = setTimeout(() => {
            NProgress.start()
        }, 100)
    }

    const userInfo = localStorage.getItem('userInfo')
    const publicPages = ['login', 'register', 'public-share']

    if (!userInfo && !publicPages.includes(to.name)) {
        return next({name: 'login'})
    }

    if (!userInfo && (to.name === 'login' || to.name === 'register')) {
        next()
        return
    }

    if (userInfo && to.name === 'login') {
        return next(from.path || '/profiles')
    }

    next()
})

router.afterEach((to) => {

    clearTimeout(timer)
    if (first) {
        removeLoading()
    } else {
        NProgress.done();
    }

    const uiStore = useUiStore()
    uiStore.accountShow = false

    if (window.innerWidth < 1025) {
        uiStore.asideShow = false
    }

    first = false
})

function removeLoading() {
    const doc = document.getElementById('loading-first');
    if (!doc) {
        return;
    }
    doc.remove()
}

export default router
