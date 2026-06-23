<template>
  <main class="public-page">
    <section v-if="needPassword" class="state-panel">
      <h1>私密简历</h1>
      <p>该简历受密码保护，请输入访问密码后继续查看。</p>
      <div class="password-row">
        <el-input v-model="password" show-password placeholder="访问密码" @keyup.enter="loadResume" />
        <el-button type="primary" :loading="loading" @click="loadResume">打开</el-button>
      </div>
      <p v-if="errorMessage" class="error-msg">{{ errorMessage }}</p>
    </section>

    <section v-else-if="errorMessage" class="state-panel">
      <h1>暂时无法查看</h1>
      <p>{{ errorMessage }}</p>
      <el-button v-if="canRetry" type="primary" :loading="loading" @click="loadResume">重新加载</el-button>
    </section>

    <section v-else-if="resume" class="resume-sheet">
      <header class="resume-header">
        <div>
          <h1>{{ resume.profile?.name || resume.share?.title || 'BingoCV 简历' }}</h1>
          <p>{{ resume.profile?.description || '通过 BingoCV 分享' }}</p>
        </div>
        <div class="contact">
          <span v-if="resume.profile?.city">{{ resume.profile.city }}</span>
          <span v-if="resume.profile?.phone">{{ resume.profile.phone }}</span>
          <span v-if="resume.profile?.email">{{ resume.profile.email }}</span>
        </div>
      </header>

      <section-block title="工作经历" :items="resume.workList">
        <template #default="{ item }">
          <h3>{{ item.company }} / {{ item.job || item.office }}</h3>
          <p class="time">{{ item.start || item.startDate }} - {{ item.end || item.endDate }}</p>
          <p>{{ item.description }}</p>
        </template>
      </section-block>

      <section-block title="教育经历" :items="resume.educationList">
        <template #default="{ item }">
          <h3>{{ item.school }} / {{ item.study || item.major }}</h3>
          <p class="time">{{ item.start || item.startDate }} - {{ item.end || item.endDate }}</p>
          <p>{{ item.description }}</p>
        </template>
      </section-block>

      <section v-if="resume.skill?.keywords" class="section">
        <h2>技能</h2>
        <div class="tags">
          <el-tag v-for="item in skillTags" :key="item" effect="plain">{{ item }}</el-tag>
        </div>
      </section>

      <section-block title="特长亮点" :items="resume.specialtyList">
        <template #default="{ item }">
          <h3>{{ item.name }}</h3>
          <p>{{ item.description }}</p>
        </template>
      </section-block>
    </section>

    <section v-else class="state-panel">
      <h1>正在加载简历</h1>
      <p>请稍候...</p>
    </section>
  </main>
</template>

<script setup>
import { computed, defineComponent, h, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { openPublicShare } from '@/request/share.js'

const route = useRoute()
const loading = ref(false)
const needPassword = ref(false)
const password = ref('')
const resume = ref(null)
const errorMessage = ref('')
const canRetry = ref(false)

const skillTags = computed(() => (resume.value?.skill?.keywords || '').split(/\s+/).filter(Boolean))

const SectionBlock = defineComponent({
  props: {
    title: String,
    items: {
      type: Array,
      default: () => []
    }
  },
  setup(props, { slots }) {
    return () => props.items?.length
      ? h('section', { class: 'section' }, [
        h('h2', props.title),
        ...props.items.map(item => h('article', { class: 'entry', key: item.id }, slots.default?.({ item })))
      ])
      : null
  }
})

const loadResume = async () => {
  loading.value = true
  errorMessage.value = ''
  canRetry.value = false
  try {
    resume.value = await openPublicShare(route.params.code, { password: password.value })
    needPassword.value = false
  } catch (error) {
    const code = error?.code || error?.response?.data?.code
    const message = error?.msg || error?.message || error?.response?.data?.msg || '分享暂时不可访问'

    // 403 表示私密分享需要密码或密码错误，此时展示密码输入面板。
    if (code === 403) {
      needPassword.value = true
      errorMessage.value = password.value ? '访问密码不正确，请重新输入。' : ''
      return
    }

    resume.value = null
    needPassword.value = false
    errorMessage.value = message
    canRetry.value = code >= 500
  } finally {
    loading.value = false
  }
}

onMounted(loadResume)
</script>

<style scoped>
.public-page {
  min-height: 100vh;
  padding: 40px 20px;
  background: var(--light-fill);
  color: var(--el-text-color-primary);
}

.resume-sheet,
.state-panel {
  max-width: 920px;
  margin: 0 auto;
  background: var(--el-bg-color);
  border: 1px solid var(--light-border);
  border-radius: 8px;
  box-shadow: 0 16px 40px rgba(15, 23, 42, 0.08);
}

.resume-sheet {
  padding: 42px;
}

.state-panel {
  max-width: 520px;
  padding: 32px;
}

.resume-header {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid var(--light-border);
}

h1,
h2,
h3,
p {
  margin: 0;
}

h1 {
  color: var(--el-text-color-primary);
  font-size: 32px;
}

h2 {
  margin-bottom: 16px;
  color: var(--el-text-color-primary);
  font-size: 20px;
}

h3 {
  color: var(--el-text-color-primary);
  font-size: 16px;
}

p {
  color: var(--regular-text-color);
  line-height: 1.7;
}

.state-panel p {
  margin-top: 10px;
}

.state-panel .el-button {
  margin-top: 20px;
}

.contact {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 6px;
  color: var(--regular-text-color);
  font-size: 14px;
}

.section {
  margin-top: 28px;
}

.entry {
  padding: 16px 0;
  border-top: 1px solid var(--light-border);
}

.entry:first-of-type {
  border-top: 0;
  padding-top: 0;
}

.time {
  margin: 4px 0 8px;
  color: var(--secondary-text-color);
  font-size: 13px;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.password-row {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 12px;
  margin-top: 20px;
}

.error-msg {
  margin-top: 12px;
  color: var(--el-color-danger);
  font-size: 14px;
}

@media (max-width: 720px) {
  .resume-sheet {
    padding: 24px;
  }

  .resume-header {
    flex-direction: column;
  }

  .contact {
    align-items: flex-start;
  }

  .password-row {
    grid-template-columns: 1fr;
  }
}
</style>
