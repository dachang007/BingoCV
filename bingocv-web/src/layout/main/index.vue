<template>
  <div class="main-box">
    <router-view class="main-view" v-slot="{ Component, route }">
      <keep-alive :include="['dashboard', 'profiles', 'education', 'work', 'skill', 'templates', 'resume-preview', 'ai-assistant', 'points', 'coupons', 'orders', 'share', 'user', 'coupon-admin', 'order-admin', 'refund-admin', 'settings', 'ai-usage-logs', 'operation-logs']">
        <component :is="Component" :key="route.name" />
      </keep-alive>
    </router-view>
  </div>
</template>

<script setup>
</script>

<style lang="scss" scoped>
.main-box {
  display: grid;
  grid-template-columns: 1fr;
  height: calc(100% - 60px);
}

.main-view {
  background: var(--el-bg-color);
  overflow: auto;
}
</style>
