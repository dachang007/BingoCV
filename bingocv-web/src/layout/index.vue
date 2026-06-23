<template>
  <el-container class="layout">
    <el-aside
        class="aside"
        :style="asideStyle"
        :class="uiStore.asideShow ? 'aside-show' : 'el-aside-hide'">
      <Aside />
      <button
          v-if="!isMobile"
          class="aside-resizer"
          type="button"
          aria-label="拖拽调整侧边栏宽度"
          @mousedown="startResize"
      ></button>
    </el-aside>
    <div
        :class="(uiStore.asideShow && isMobile)? 'overlay-show':'overlay-hide'"
        @click="uiStore.asideShow = false"
    ></div>
    <el-container class="main-container">
      <el-main>
        <el-header>
            <Header />
        </el-header>
        <Main />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import Aside from '@/layout/aside/index.vue'
import Header from '@/layout/header/index.vue'
import Main from '@/layout/main/index.vue'
import { computed, ref, onMounted, onBeforeUnmount } from 'vue'
import {useUiStore} from "@/store/ui.js";

const uiStore = useUiStore();
const isMobile = ref(window.innerWidth < 1025)
const MIN_ASIDE_WIDTH = 240
const MAX_ASIDE_WIDTH = 340

const asideStyle = computed(() => ({
  width: isMobile.value ? '260px' : `${uiStore.asideWidth}px`
}))

const handleResize = () => {
  isMobile.value = window.innerWidth < 1025
  uiStore.asideShow = window.innerWidth > 1024;
}

function startResize(event) {
  event.preventDefault()
  const startX = event.clientX
  const startWidth = uiStore.asideWidth
  document.body.classList.add('aside-resizing')

  const handleMouseMove = (moveEvent) => {
    const nextWidth = startWidth + moveEvent.clientX - startX
    uiStore.asideWidth = Math.min(MAX_ASIDE_WIDTH, Math.max(MIN_ASIDE_WIDTH, nextWidth))
  }

  const stopResize = () => {
    document.body.classList.remove('aside-resizing')
    window.removeEventListener('mousemove', handleMouseMove)
    window.removeEventListener('mouseup', stopResize)
  }

  window.addEventListener('mousemove', handleMouseMove)
  window.addEventListener('mouseup', stopResize)
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
  handleResize()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style lang="scss" scoped>
.el-aside-hide {
  position: fixed;
  left: 0;
  height: 100%;
  z-index: 100;
  transform: translateX(-100%);
  transition: all 100ms ease;
}

.aside-show {
  position: relative;
  -webkit-box-shadow: var(--aside-right-border);
  box-shadow: var(--aside-right-border);
  transform: translateX(0);
  transition: all 100ms ease;
  z-index: 101;
  @media (max-width: 1025px) {
    position: fixed;
    top: 0;
    left: 0;
    z-index: 101;
    height: 100%;
    background: var(--el-bg-color);
  }
}

.el-aside {
  transition: all 100ms ease;
  flex-shrink: 0;
  background: var(--aside-backgound);
}

.aside-resizer {
  position: absolute;
  top: 0;
  right: -4px;
  width: 8px;
  height: 100%;
  cursor: col-resize;
  z-index: 5;
}

.aside-resizer::after {
  content: "";
  position: absolute;
  top: 14px;
  bottom: 14px;
  left: 3px;
  width: 2px;
  border-radius: 999px;
  background: transparent;
  transition: background 0.2s ease;
}

.aside-resizer:hover::after,
:global(body.aside-resizing) .aside-resizer::after {
  background: var(--el-color-primary);
}

.layout {
  height: 100%;
  position: fixed;
  width: 100%;
  top: 0;
  left: 0;
  overflow: hidden;
}

.main-container {
  min-height: 100%;
  background: var(--el-bg-color);
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}

.el-main {
  padding: 0;
}

.el-header {
  background: var(--el-bg-color);
  border-bottom: solid 1px var(--el-border-color);
  padding: 0 0 0 0;
}

.overlay-show {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.4);
  z-index: 99;
  transition: all 0.3s;
}

.overlay-hide {
  display: flex;
  pointer-events: none;
  opacity: 0;
}
</style>
