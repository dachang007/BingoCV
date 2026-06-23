<template>
  <div class="template-teacher-classic">
    <header class="resume-header">
      <h1>{{ resume.profile?.name || '姓名' }}</h1>
      <div class="contact">
        <span v-if="resume.profile?.phone">{{ resume.profile.phone }}</span>
        <span v-if="resume.profile?.email">{{ resume.profile.email }}</span>
      </div>
    </header>
    <section v-if="resume.profile?.description" class="section">
      <h2>教学理念</h2>
      <p>{{ resume.profile.description }}</p>
    </section>
    <section v-if="resume.work?.length" class="section">
      <h2>教学经历</h2>
      <div v-for="work in resume.work" :key="work.id" class="item">
        <strong>{{ work.company }}</strong> - {{ work.job }}
      </div>
    </section>
    <section v-if="resume.edu?.length" class="section">
      <h2>教育背景</h2>
      <div v-for="edu in resume.edu" :key="edu.id" class="item">
        <strong>{{ edu.school }}</strong> - {{ edu.study }}
      </div>
    </section>
  </div>
</template>
<script setup>
defineProps({ resume: { type: Object, default: () => ({}) } });
</script>
<style scoped>
.template-teacher-classic { padding: 40px; font-family: 'Times New Roman', serif; }
.resume-header { border-bottom: 2px solid #722ed1; padding-bottom: 20px; }
.resume-header h1 { margin: 0; color: #722ed1; }
.section { margin-bottom: 20px; }
.section h2 { color: #722ed1; font-size: 16px; }
</style>
