import { createRouter, createWebHistory } from 'vue-router'
import NProgress from 'nprogress'
import { ElMessage } from 'element-plus'
import { useUiStore } from '@/store/ui.js'
import { getHomePathByRole, getStoredPermKeys, hasPermission, readStoredUser, ROUTE_PERMISSIONS } from '@/perm/permissions.js'

const adminChildren = [
  ['dashboard', 'dashboard', () => import('@/views/dashboard/index.vue')],
  ['profiles', 'profiles', () => import('@/views/profiles/index.vue')],
  ['education', 'education', () => import('@/views/education/index.vue')],
  ['work', 'work', () => import('@/views/work/index.vue')],
  ['skill', 'skill', () => import('@/views/skill/index.vue')],
  ['templates', 'templates', () => import('@/views/templates/index.vue')],
  ['resume-preview', 'resume-preview', () => import('@/views/resume-preview/index.vue')],
  ['ai-assistant', 'ai-assistant', () => import('@/views/ai-assistant/index.vue')],
  ['ai-interview', 'ai-interview', () => import('@/views/ai-interview/index.vue')],
  ['points', 'points', () => import('@/views/points/index.vue')],
  ['coupons', 'coupons', () => import('@/views/coupons/index.vue')],
  ['orders', 'orders', () => import('@/views/orders/index.vue')],
  ['share', 'share', () => import('@/views/share/index.vue')],
  ['user', 'user', () => import('@/views/user/index.vue')],
  ['coupon-admin', 'coupon-admin', () => import('@/views/coupon-admin/index.vue')],
  ['order-admin', 'order-admin', () => import('@/views/order-admin/index.vue')],
  ['refund-admin', 'refund-admin', () => import('@/views/refund-admin/index.vue')],
  ['settings', 'settings', () => import('@/views/settings/index.vue')],
  ['ai-usage-logs', 'ai-usage-logs', () => import('@/views/ai-usage-logs/index.vue')],
  ['operation-logs', 'operation-logs', () => import('@/views/operation-logs/index.vue')]
].map(([path, name, component]) => ({
  path,
  name,
  component,
  meta: { title: name, name, menu: true, perm: ROUTE_PERMISSIONS[name] }
}))

const routes = [
  { path: '/', redirect: '/admin' },
  {
    path: '/admin',
    name: 'layout',
    redirect: () => getHomePathByRole(readStoredUser()?.role),
    component: () => import('@/layout/index.vue'),
    children: adminChildren
  },
  { path: '/login', redirect: '/admin/login' },
  { path: '/admin/login', name: 'login', component: () => import('@/views/login/index.vue') },
  { path: '/register', name: 'register', component: () => import('@/views/register/index.vue') },
  { path: '/share/:code', name: 'public-share', component: () => import('@/views/public-share/index.vue') },
  { path: '/s/:code', redirect: to => `/share/${to.params.code}` },

  // 兼容旧后台路径，避免旧收藏或浏览器历史失效。
  ...adminChildren.map(route => ({
    path: `/${route.path}`,
    redirect: `/admin/${route.path}`
  })),

  { path: '/:pathMatch(.*)*', name: '404', component: () => import('@/views/404/index.vue') }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

NProgress.configure({
  showSpinner: false,
  trickleSpeed: 50,
  minimum: 0.1
})

let timer
let first = true

router.beforeEach((to, from, next) => {
  if (timer) clearTimeout(timer)
  if (!first) {
    timer = setTimeout(() => NProgress.start(), 100)
  }

  const userInfo = localStorage.getItem('userInfo')
  const publicPages = ['login', 'register', 'public-share']

  if (!userInfo && !publicPages.includes(to.name)) {
    return next({ name: 'login' })
  }

  const storedUser = readStoredUser()
  if (userInfo && to.name === 'login') {
    return next(getHomePathByRole(storedUser?.role))
  }

  const requiredPerm = to.meta?.perm
  if (userInfo && requiredPerm && !hasPermission(getStoredPermKeys(), requiredPerm)) {
    ElMessage.warning('暂无权限访问该页面')
    return next(getHomePathByRole(storedUser?.role))
  }

  next()
})

router.afterEach(() => {
  clearTimeout(timer)
  first ? removeLoading() : NProgress.done()

  const uiStore = useUiStore()
  uiStore.accountShow = false
  if (window.innerWidth < 1025) {
    uiStore.asideShow = false
  }
  first = false
})

function removeLoading() {
  const doc = document.getElementById('loading-first')
  if (doc) doc.remove()
}

export default router
