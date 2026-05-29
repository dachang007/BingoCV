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
          v-for="item in resumeMenus"
          :key="item.name"
          :index="item.name"
          :class="route.meta.name === item.name ? 'choose-item' : ''"
          @click="router.push({ name: item.name })"
        >
          <Icon :icon="item.icon" width="20" height="20" />
          <span class="menu-name">{{ item.label }}</span>
        </el-menu-item>

        <div class="manage-title">
          <div>运营管理</div>
        </div>

        <el-menu-item
          v-for="item in manageMenus"
          :key="item.name"
          :index="item.name"
          :class="route.meta.name === item.name ? 'choose-item' : ''"
          @click="router.push({ name: item.name })"
        >
          <Icon :icon="item.icon" width="20" height="20" />
          <span class="menu-name">{{ item.label }}</span>
        </el-menu-item>
      </el-menu>
    </div>
  </el-scrollbar>
</template>

<script setup>
import router from "@/router/index.js";
import { useRoute } from "vue-router";
import { Icon } from "@iconify/vue";
import { useSettingStore } from "@/store/setting.js";

const settingStore = useSettingStore();
const route = useRoute();

const resumeMenus = [
  { name: 'profiles', label: '个人简历', icon: 'solar:user-id-linear' },
  { name: 'education', label: '教育经历', icon: 'solar:notebook-bookmark-linear' },
  { name: 'work', label: '工作经历', icon: 'solar:case-round-linear' },
  { name: 'skill', label: '技能特长', icon: 'solar:star-shine-linear' },
  { name: 'templates', label: '简历模板', icon: 'solar:documents-linear' },
  { name: 'points', label: '积分中心', icon: 'solar:medal-ribbon-linear' },
  { name: 'share', label: '简历分享', icon: 'solar:share-linear' }
];

const manageMenus = [
  { name: 'user', label: '用户管理', icon: 'solar:users-group-rounded-linear' }
];
</script>

<style lang="scss" scoped>
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
  max-width: 240px;
  padding: 0 10px;

  > div {
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
    max-width: calc(240px - 50px);
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
  padding-left: 20px;
  color: var(--aside-text-secondary);
  font-size: 12px;
}

.el-menu-item {
  margin: 5px 10px !important;
  border-radius: 6px;
  height: 38px;
  padding: 10px !important;
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
  margin-left: 18px;
  user-select: none;
}

:deep(.el-scrollbar__wrap--hidden-default) {
  background: var(--aside-backgound) !important;
}

:deep(.el-menu-item),
:deep(.el-menu) {
  background: var(--aside-backgound);
  color: var(--aside-text-color);
}

.el-menu {
  border-right: 0;
  width: 260px;
}
</style>
