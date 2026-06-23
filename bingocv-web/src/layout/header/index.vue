<template>
  <div class="header">
    <div class="header-left">
      <hanburger @click="changeAside" />
      <span class="breadcrumb-item">{{ $t(route.meta.title) }}</span>
    </div>

    <div class="toolbar">
      <button class="icon-item" type="button" @click="openDark($event)">
        <Icon :icon="uiStore.dark ? 'mingcute:sun-fill' : 'solar:moon-linear'" />
      </button>
      <button v-if="canOpenShare" class="icon-item" type="button" @click="router.push('/admin/share')">
        <Icon icon="solar:share-linear" />
      </button>

      <el-dropdown ref="userinfoRef" popper-class="detail-dropdown">
        <button class="avatar" type="button">
          <span>{{ avatarText }}</span>
          <Icon icon="mingcute:down-small-fill" width="22" height="22" />
        </button>
        <template #dropdown>
          <div class="user-details">
            <div class="details-avatar">{{ avatarText }}</div>
            <div class="user-name">{{ currentUser.name }}</div>
            <button class="detail-email" type="button" @click="copyText(currentUser.email)">
              {{ currentUser.email }}
            </button>
            <el-tag>{{ roleName }}</el-tag>

            <div class="info-grid">
              <span>{{ $t('accountInfo') }}</span>
              <strong>{{ currentUser.username }}</strong>
              <span>{{ $t('currentRole') }}</span>
              <strong>{{ roleName }}</strong>
            </div>

            <el-button type="primary" :loading="logoutLoading" @click="clickLogout">
              {{ $t('logout') }}
            </el-button>
          </div>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { Icon } from '@iconify/vue'
import hanburger from '@/components/hamburger/index.vue'
import { useUiStore } from '@/store/ui.js'
import { useUserStore } from '@/store/user.js'
import { hasAnyPerm } from '@/perm/perm.js'
import { PERMISSIONS } from '@/perm/permissions.js'
import { feedback } from '@/utils/feedback.js'

const router = useRouter()
const route = useRoute()
const { t } = useI18n()
const uiStore = useUiStore()
const userStore = useUserStore()
const logoutLoading = ref(false)
const canOpenShare = computed(() => hasAnyPerm(PERMISSIONS.SHARE_VIEW))

const currentUser = computed(() => {
  const fallback = { username: 'BingoCV', name: 'BingoCV 用户', email: '未设置邮箱', role: { name: 'USER' } }
  if (userStore.user?.username) return { ...fallback, ...userStore.user }
  try {
    const raw = localStorage.getItem('userInfo')
    return raw ? { ...fallback, ...JSON.parse(decodeURIComponent(escape(atob(raw)))) } : fallback
  } catch {
    return fallback
  }
})

const roleName = computed(() => {
  const role = String(currentUser.value.role?.name || currentUser.value.role || 'USER').toUpperCase()
  if (role === 'ADMIN') return t('roleAdmin')
  return t('roleUser')
})

const avatarText = computed(() => {
  return (currentUser.value.name || currentUser.value.username || 'B').slice(0, 1).toUpperCase()
})

const copyText = async (text) => {
  try {
    await navigator.clipboard.writeText(text || '')
    feedback.success(t('copySuccessMsg'))
  } catch {
    feedback.error(t('copyFailMsg'))
  }
}

function changeAside() {
  uiStore.asideShow = !uiStore.asideShow
}

function openDark(e) {
  const nextIsDark = !uiStore.dark
  const root = document.documentElement
  if (!document.startViewTransition) {
    switchDark(nextIsDark, root)
    return
  }

  const x = e.clientX
  const y = e.clientY
  const endRadius = Math.hypot(Math.max(x, window.innerWidth - x), Math.max(y, window.innerHeight - y))
  root.setAttribute('data-theme-to', nextIsDark ? 'dark' : 'light')
  root.style.setProperty('--vt-x', `${x}px`)
  root.style.setProperty('--vt-y', `${y}px`)
  root.style.setProperty('--vt-end-radius', `${endRadius + 10}px`)

  const transition = document.startViewTransition(() => switchDark(nextIsDark, root))
  transition.finished.finally(() => root.removeAttribute('data-theme-to'))
}

function switchDark(nextIsDark, root) {
  root.setAttribute('class', nextIsDark ? 'dark' : '')
  const metaTag = document.getElementById('theme-color-meta')
  if (metaTag) metaTag.setAttribute('content', nextIsDark ? '#141414' : '#F1F1F1')
  uiStore.dark = nextIsDark
}

async function clickLogout() {
  logoutLoading.value = true
  try {
    // 当前后端没有会话退出接口，退出动作以前端本地登录态清理为准，保证点击后立即生效。
    localStorage.removeItem('userInfo')
    localStorage.removeItem('token')
    userStore.user = {}
    await router.replace('/admin/login')
    requestAnimationFrame(() => feedback.success(t('logoutSuccess')))
  } catch (error) {
    feedback.error(error?.message || t('logoutFailed'))
  } finally {
    logoutLoading.value = false
  }
}
</script>

<style>
.detail-dropdown {
  color: var(--el-text-color-primary) !important;
}
</style>

<style scoped>
.header {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.header-left,
.toolbar,
.avatar,
.icon-item {
  display: flex;
  align-items: center;
}

.header-left {
  min-width: 0;
}

.breadcrumb-item {
  margin-left: 8px;
  font-weight: 700;
  color: var(--el-text-color-primary);
}

.toolbar {
  gap: 10px;
}

.icon-item {
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 6px;
  color: var(--el-text-color-primary);
  cursor: pointer;
}

.icon-item:hover {
  background: var(--base-fill);
}

.avatar {
  gap: 4px;
  color: var(--el-text-color-primary);
  cursor: pointer;
}

.avatar span,
.details-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--extra-light-fill);
  color: var(--el-text-color-primary);
  border: 1px solid var(--light-border);
}

.avatar span {
  width: 32px;
  height: 32px;
  border-radius: 8px;
}

.user-details {
  width: 260px;
  padding: 18px 14px 14px;
  display: grid;
  justify-items: center;
  gap: 10px;
  background: var(--el-bg-color);
}

.details-avatar {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  font-size: 18px;
  font-weight: 700;
}

.user-name {
  max-width: 230px;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  font-weight: 700;
}

.detail-email {
  max-width: 230px;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  color: var(--regular-text-color);
  cursor: pointer;
}

.info-grid {
  width: 100%;
  display: grid;
  grid-template-columns: 72px 1fr;
  gap: 8px 10px;
  padding: 10px;
  border: 1px solid var(--light-border);
  border-radius: 8px;
  color: var(--regular-text-color);
}

.info-grid strong {
  min-width: 0;
  color: var(--el-text-color-primary);
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-details .el-button {
  width: 100%;
}
</style>
