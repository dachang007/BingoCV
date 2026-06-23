<template>
  <el-scrollbar class="scroll">
    <div>
      <div class="title">
        <img src="/bingo.png" alt="BingoCV" class="title-logo" />
        <div>{{ settingStore.settings.title }}</div>
      </div>

      <el-menu
        :default-active="route.name"
        :collapse="false"
        :text-color="'var(--aside-text-color)'"
        :active-text-color="'var(--aside-active-color)'"
        class="resume-menu"
      >
        <el-menu-item
          v-for="item in visibleResumeMenus"
          :key="item.name"
          :index="item.name"
          :class="route.meta.name === item.name ? 'choose-item' : ''"
          @click="router.push({ name: item.name })"
        >
          <Icon :icon="item.icon" width="20" height="20" />
          <span class="menu-name">{{ item.label }}</span>
        </el-menu-item>

        <template v-if="visibleManageMenus.length">
          <div class="manage-title">运营管理</div>

          <el-menu-item
            v-for="item in visibleManageMenus"
            :key="item.name"
            :index="item.name"
            :class="route.meta.name === item.name ? 'choose-item' : ''"
            @click="router.push({ name: item.name })"
          >
            <Icon :icon="item.icon" width="20" height="20" />
            <span class="menu-name">{{ item.label }}</span>
          </el-menu-item>
        </template>
      </el-menu>
    </div>
  </el-scrollbar>
</template>

<script setup>
import { computed } from 'vue'
import router from '@/router/index.js'
import { useRoute } from 'vue-router'
import { Icon } from '@iconify/vue'
import { useSettingStore } from '@/store/setting.js'
import { hasAnyPerm } from '@/perm/perm.js'
import { PERMISSIONS } from '@/perm/permissions.js'

const settingStore = useSettingStore()
const route = useRoute()

const resumeMenus = [
  { name: 'profiles', label: '个人简历', icon: 'solar:user-id-linear', perm: PERMISSIONS.PROFILE_VIEW },
  { name: 'education', label: '教育经历', icon: 'solar:notebook-bookmark-linear', perm: PERMISSIONS.EDUCATION_VIEW },
  { name: 'work', label: '工作经历', icon: 'solar:case-round-linear', perm: PERMISSIONS.WORK_VIEW },
  { name: 'skill', label: '技能特长', icon: 'solar:star-shine-linear', perm: PERMISSIONS.SKILL_VIEW },
  { name: 'templates', label: '简历模板', icon: 'solar:documents-linear', perm: PERMISSIONS.TEMPLATE_VIEW },
  { name: 'resume-preview', label: '预览导出', icon: 'solar:document-text-linear', perm: PERMISSIONS.RESUME_PREVIEW },
  { name: 'ai-assistant', label: 'AI 简历助手', icon: 'solar:magic-stick-3-linear', perm: PERMISSIONS.AI_ASSISTANT },
  { name: 'ai-interview', label: 'AI 面试练习', icon: 'solar:question-circle-linear', perm: PERMISSIONS.AI_INTERVIEW },
  { name: 'points', label: '积分中心', icon: 'solar:medal-ribbon-linear', perm: PERMISSIONS.POINTS_VIEW },
  { name: 'coupons', label: '我的优惠券', icon: 'solar:ticket-sale-linear', perm: PERMISSIONS.COUPON_VIEW },
  { name: 'orders', label: '我的订单', icon: 'solar:bill-list-linear', perm: PERMISSIONS.ORDER_VIEW },
  { name: 'share', label: '简历分享', icon: 'solar:share-linear', perm: PERMISSIONS.SHARE_VIEW }
]

const manageMenus = [
  { name: 'dashboard', label: '数据看板', icon: 'solar:chart-square-linear', perm: PERMISSIONS.DASHBOARD_VIEW },
  { name: 'user', label: '用户管理', icon: 'solar:users-group-rounded-linear', perm: PERMISSIONS.USER_MANAGE },
  { name: 'coupon-admin', label: '优惠券管理', icon: 'solar:ticket-linear', perm: PERMISSIONS.COUPON_MANAGE },
  { name: 'order-admin', label: '订单管理', icon: 'solar:bill-check-linear', perm: PERMISSIONS.ORDER_MANAGE },
  { name: 'refund-admin', label: '退款记录', icon: 'solar:undo-left-round-linear', perm: PERMISSIONS.REFUND_MANAGE },
  { name: 'settings', label: '系统配置', icon: 'solar:settings-linear', perm: PERMISSIONS.SYSTEM_CONFIG },
  { name: 'ai-usage-logs', label: 'AI 使用记录', icon: 'solar:magic-stick-2-linear', perm: PERMISSIONS.AI_USAGE_LOG },
  { name: 'operation-logs', label: '操作日志', icon: 'solar:document-add-linear', perm: PERMISSIONS.OPERATION_LOG }
]

// 菜单只做体验层隐藏，真正权限由路由守卫和后端接口兜底。
const visibleResumeMenus = computed(() => resumeMenus.filter(item => hasAnyPerm(item.perm)))
const visibleManageMenus = computed(() => manageMenus.filter(item => hasAnyPerm(item.perm)))
</script>

<style lang="scss" scoped>
.scroll {
  height: 100%;
}

.title {
  margin: 15px 10px;
  height: 45px;
  border-radius: 6px;
  display: flex;
  position: relative;
  font-size: 16px;
  font-weight: bold;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: var(--aside-active-color);
  background: transparent;
  transition: all 0.3s ease;
  max-width: calc(100% - 20px);
  padding: 0 10px;

  > div {
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
    max-width: calc(100% - 40px);
  }

  .title-logo {
    width: 24px;
    height: 24px;
    object-fit: contain;
    flex-shrink: 0;
  }
}

.manage-title {
  margin-top: 14px;
  padding: 8px 20px 4px;
  color: var(--aside-text-secondary);
  font-size: 12px;
}

.el-menu-item {
  margin: 5px 10px !important;
  border-radius: 6px;
  height: 38px;
  padding: 10px 14px !important;
  color: var(--aside-text-color) !important;
}

.choose-item {
  font-weight: 600;
  color: var(--aside-active-color) !important;
  background: var(--aside-active-bg) !important;
}

@media (hover: hover) {
  .el-menu-item:hover {
    background: var(--aside-hover-bg) !important;
    color: var(--aside-text-color) !important;
  }
}

.menu-name {
  min-width: 0;
  margin-left: 16px;
  user-select: none;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.el-menu {
  border: none;
  background: transparent;
}
</style>
