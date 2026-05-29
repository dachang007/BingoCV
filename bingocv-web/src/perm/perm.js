import { useUserStore } from "@/store/user.js";

export default {
  mounted(el, binding) {
    if (!binding.value) return;
    if (!hasAnyPerm(binding.value)) {
      el.parentNode && el.parentNode.removeChild(el);
    }
  }
};

export function hasPerm(permKey) {
  const { permKeys } = useUserStore().user || {};
  if (!permKeys) return false;
  return permKeys.includes('*') || permKeys.includes(permKey);
}

export function hasAnyPerm(value) {
  const list = Array.isArray(value) ? value : [value];
  return list.some(hasPerm);
}

export function permsToRouter() {
  return [];
}
