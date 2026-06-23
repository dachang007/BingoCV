<template>
  <div class="template-designer-portfolio">
    <header class="resume-header">
      <h1>{{ resume.profile?.name || '姓名' }}</h1>
      <div class="contact">
        <span v-if="resume.profile?.phone">{{ resume.profile.phone }}</span>
        <span v-if="resume.profile?.email">{{ resume.profile.email }}</span>
      </div>
    </header>
    <section v-if="resume.profile?.description" class="section">
      <h2>设计理念</h2>
      <p>{{ resume.profile.description }}</p>
    </section>
    <section v-if="resume.work?.length" class="section">
      <h2>项目作品</h2>
      <div v-for="work in resume.work" :key="work.id" class="item">
        <strong>{{ work.company }}</strong> - {{ work.job }}
        <p v-if="work.description">{{ work.description }}</p>
      </div>
    </section>
    <section v-if="resume.skill?.keywords" class="section">
      <h2>设计技能</h2>
      <p>{{ resume.skill.keywords }}</p>
    </section>
  </div>
</template>
<script setup>
defineProps({ resume: { type: Object, default: () => ({}) } });
</script>
<style scoped>
.template-designer-portfolio { padding: 40px; font-family: 'Helvetica Neue', Arial, sans-serif; }
.resume-header { border-bottom: 3px solid #eb2f96; padding-bottom: 20px; }
.resume-header h1 { margin: 0; color: #eb2f96; }
.section { margin-bottom: 20px; }
.section h2 { color: #eb2f96; font-size: 16px; }
</style>
