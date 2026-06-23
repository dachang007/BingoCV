import { useUserStore } from '@/store/user.js'
import { getStoredPermKeys, hasPermission } from '@/perm/permissions.js'

export default {
  mounted(el, binding) {
    if (!binding.value) return
    if (!hasAnyPerm(binding.value)) {
      el.parentNode && el.parentNode.removeChild(el)
    }
  }
}

export function hasPerm(permKey) {
  const storeUser = useUserStore().user || {}
  const permKeys = Array.isArray(storeUser.permKeys) && storeUser.permKeys.length > 0
    ? storeUser.permKeys
    : getStoredPermKeys()
  return hasPermission(permKeys, permKey)
}

export function hasAnyPerm(value) {
  const list = Array.isArray(value) ? value : [value]
  return list.some(hasPerm)
}

export function permsToRouter() {
  return []
}
