export const ADMIN_ROLE = 'ADMIN'
export const USER_ROLE = 'USER'

export const PERMISSIONS = {
  DASHBOARD_VIEW: 'dashboard:view',
  PROFILE_VIEW: 'profile:view',
  EDUCATION_VIEW: 'education:view',
  WORK_VIEW: 'work:view',
  SKILL_VIEW: 'skill:view',
  TEMPLATE_VIEW: 'template:view',
  RESUME_PREVIEW: 'resume:preview',
  AI_ASSISTANT: 'ai:assistant',
  AI_INTERVIEW: 'ai:interview',
  POINTS_VIEW: 'points:view',
  COUPON_VIEW: 'coupon:view',
  ORDER_VIEW: 'order:view',
  SHARE_VIEW: 'share:view',
  USER_MANAGE: 'user:manage',
  COUPON_MANAGE: 'coupon:manage',
  ORDER_MANAGE: 'order:manage',
  REFUND_MANAGE: 'refund:manage',
  SYSTEM_CONFIG: 'system:config',
  OPERATION_LOG: 'operation:log',
  AI_USAGE_LOG: 'ai:usage-log'
}

export const ROUTE_PERMISSIONS = {
  dashboard: PERMISSIONS.DASHBOARD_VIEW,
  profiles: PERMISSIONS.PROFILE_VIEW,
  education: PERMISSIONS.EDUCATION_VIEW,
  work: PERMISSIONS.WORK_VIEW,
  skill: PERMISSIONS.SKILL_VIEW,
  templates: PERMISSIONS.TEMPLATE_VIEW,
  'resume-preview': PERMISSIONS.RESUME_PREVIEW,
  'ai-assistant': PERMISSIONS.AI_ASSISTANT,
  'ai-interview': PERMISSIONS.AI_INTERVIEW,
  points: PERMISSIONS.POINTS_VIEW,
  coupons: PERMISSIONS.COUPON_VIEW,
  orders: PERMISSIONS.ORDER_VIEW,
  share: PERMISSIONS.SHARE_VIEW,
  user: PERMISSIONS.USER_MANAGE,
  'coupon-admin': PERMISSIONS.COUPON_MANAGE,
  'order-admin': PERMISSIONS.ORDER_MANAGE,
  'refund-admin': PERMISSIONS.REFUND_MANAGE,
  settings: PERMISSIONS.SYSTEM_CONFIG,
  'operation-logs': PERMISSIONS.OPERATION_LOG,
  'ai-usage-logs': PERMISSIONS.AI_USAGE_LOG
}

const USER_PERMISSIONS = [
  PERMISSIONS.PROFILE_VIEW,
  PERMISSIONS.EDUCATION_VIEW,
  PERMISSIONS.WORK_VIEW,
  PERMISSIONS.SKILL_VIEW,
  PERMISSIONS.TEMPLATE_VIEW,
  PERMISSIONS.RESUME_PREVIEW,
  PERMISSIONS.AI_ASSISTANT,
  PERMISSIONS.AI_INTERVIEW,
  PERMISSIONS.POINTS_VIEW,
  PERMISSIONS.COUPON_VIEW,
  PERMISSIONS.ORDER_VIEW,
  PERMISSIONS.SHARE_VIEW
]

const ADMIN_PERMISSIONS = ['*']

export function normalizeRole(role) {
  const roleName = typeof role === 'object' ? role?.name : role
  return String(roleName || USER_ROLE).toUpperCase()
}

export function buildPermKeysByRole(role) {
  return normalizeRole(role) === ADMIN_ROLE ? ADMIN_PERMISSIONS : USER_PERMISSIONS
}

export function getHomePathByRole(role) {
  return normalizeRole(role) === ADMIN_ROLE ? '/admin/dashboard' : '/admin/profiles'
}

export function hasPermission(permKeys = [], permKey) {
  if (!permKey) return true
  return permKeys.includes('*') || permKeys.includes(permKey)
}

export function readStoredUser() {
  try {
    const raw = localStorage.getItem('userInfo')
    if (!raw) return null
    return JSON.parse(decodeURIComponent(escape(atob(raw))))
  } catch {
    return null
  }
}

export function getStoredPermKeys() {
  const user = readStoredUser()
  if (!user) return []
  return Array.isArray(user.permKeys) && user.permKeys.length > 0
    ? user.permKeys
    : buildPermKeysByRole(user.role)
}
