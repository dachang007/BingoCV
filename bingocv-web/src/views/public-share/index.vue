<template>
  <main class="public-page">
    <section v-if="needPassword" class="password-panel">
      <h1>Private Resume</h1>
      <p>This resume is protected. Enter the access password to continue.</p>
      <div class="password-row">
        <el-input v-model="password" show-password placeholder="Access password" @keyup.enter="loadResume" />
        <el-button type="primary" :loading="loading" @click="loadResume">Open</el-button>
      </div>
    </section>

    <section v-else-if="resume" class="resume-sheet">
      <header class="resume-header">
        <div>
          <h1>{{ resume.profile?.name || resume.share?.title || 'BingoCV Resume' }}</h1>
          <p>{{ resume.profile?.description || 'Shared with BingoCV' }}</p>
        </div>
        <div class="contact">
          <span v-if="resume.profile?.city">{{ resume.profile.city }}</span>
          <span v-if="resume.profile?.phone">{{ resume.profile.phone }}</span>
          <span v-if="resume.profile?.email">{{ resume.profile.email }}</span>
        </div>
      </header>

      <section-block title="Work Experience" :items="resume.workList">
        <template #default="{ item }">
          <h3>{{ item.company }} · {{ item.job }}</h3>
          <p class="time">{{ item.start }} - {{ item.end }}</p>
          <p>{{ item.description }}</p>
        </template>
      </section-block>

      <section-block title="Education" :items="resume.educationList">
        <template #default="{ item }">
          <h3>{{ item.school }} · {{ item.study }}</h3>
          <p class="time">{{ item.start }} - {{ item.end }}</p>
          <p>{{ item.description }}</p>
        </template>
      </section-block>

      <section v-if="resume.skill?.keywords" class="section">
        <h2>Skills</h2>
        <div class="tags">
          <el-tag v-for="item in skillTags" :key="item" effect="plain">{{ item }}</el-tag>
        </div>
      </section>

      <section-block title="Highlights" :items="resume.specialtyList">
        <template #default="{ item }">
          <h3>{{ item.name }}</h3>
          <p>{{ item.description }}</p>
        </template>
      </section-block>
    </section>

    <section v-else class="password-panel">
      <h1>Loading Resume</h1>
      <p>Please wait a moment.</p>
    </section>
  </main>
</template>

<script setup>
import { computed, defineComponent, h, onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';
import { openPublicShare } from '@/request/share.js';

const route = useRoute();
const loading = ref(false);
const needPassword = ref(false);
const password = ref('');
const resume = ref(null);

const skillTags = computed(() => {
  return (resume.value?.skill?.keywords || '').split(/\s+/).filter(Boolean);
});

const SectionBlock = defineComponent({
  props: {
    title: String,
    items: {
      type: Array,
      default: () => []
    }
  },
  setup(props, { slots }) {
    return () => props.items?.length ? h('section', { class: 'section' }, [
      h('h2', props.title),
      ...props.items.map((item) => h('article', { class: 'entry', key: item.id }, slots.default?.({ item })))
    ]) : null;
  }
});

const loadResume = async () => {
  loading.value = true;
  try {
    resume.value = await openPublicShare(route.params.code, { password: password.value });
    needPassword.value = false;
  } catch (error) {
    // A private share intentionally returns 403 until the visitor provides a password.
    if (error?.code === 403 || error?.response?.data?.code === 403) {
      needPassword.value = true;
    }
  } finally {
    loading.value = false;
  }
};

onMounted(loadResume);
</script>

<style scoped>
.public-page {
  min-height: 100vh;
  padding: 40px 20px;
  background: #f5f7fb;
}

.resume-sheet,
.password-panel {
  max-width: 920px;
  margin: 0 auto;
  background: #fff;
  border: 1px solid #e6eaf0;
  border-radius: 8px;
  box-shadow: 0 16px 40px rgba(15, 23, 42, 0.08);
}

.resume-sheet {
  padding: 42px;
}

.password-panel {
  max-width: 520px;
  padding: 32px;
}

.resume-header {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid #edf0f5;
}

h1,
h2,
h3,
p {
  margin: 0;
}

h1 {
  color: #111827;
  font-size: 32px;
}

h2 {
  margin-bottom: 16px;
  color: #1f2937;
  font-size: 20px;
}

h3 {
  color: #111827;
  font-size: 16px;
}

p {
  color: #4b5563;
  line-height: 1.7;
}

.contact {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 6px;
  color: #4b5563;
  font-size: 14px;
}

.section {
  margin-top: 28px;
}

.entry {
  padding: 16px 0;
  border-top: 1px solid #edf0f5;
}

.entry:first-of-type {
  border-top: 0;
  padding-top: 0;
}

.time {
  margin: 4px 0 8px;
  color: #667085;
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
