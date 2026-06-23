<template>
  <div class="template-product-growth">
    <header class="resume-header">
      <h1>{{ resume.profile?.name || '姓名' }}</h1>
      <div class="contact">
        <span v-if="resume.profile?.phone">{{ resume.profile.phone }}</span>
        <span v-if="resume.profile?.email">{{ resume.profile.email }}</span>
        <span v-if="resume.profile?.city">{{ resume.profile.city }}</span>
      </div>
    </header>
    <section v-if="resume.profile?.description" class="section">
      <h2>个人简介</h2>
      <p>{{ resume.profile.description }}</p>
    </section>
    <section v-if="resume.work?.length" class="section">
      <h2>工作经历</h2>
      <div v-for="work in resume.work" :key="work.id" class="item">
        <strong>{{ work.company }}</strong> - {{ work.job }}
        <span class="time">{{ work.start }} - {{ work.end || '至今' }}</span>
        <p v-if="work.description">{{ work.description }}</p>
      </div>
    </section>
    <section v-if="resume.edu?.length" class="section">
      <h2>教育经历</h2>
      <div v-for="edu in resume.edu" :key="edu.id" class="item">
        <strong>{{ edu.school }}</strong> - {{ edu.study }}
        <span class="time">{{ edu.start }} - {{ edu.end }}</span>
      </div>
    </section>
    <section v-if="resume.skill?.keywords" class="section">
      <h2>核心技能</h2>
      <p>{{ resume.skill.keywords }}</p>
    </section>
  </div>
</template>
<script setup>
defineProps({ resume: { type: Object, default: () => ({}) } });
</script>
<style scoped>
.template-product-growth { padding: 40px; font-family: Arial, sans-serif; }
.resume-header { border-bottom: 2px solid #52c41a; padding-bottom: 20px; margin-bottom: 20px; }
.resume-header h1 { margin: 0; color: #52c41a; }
.contact { color: #666; margin-top: 8px; }
.contact span { margin-right: 16px; }
.section { margin-bottom: 20px; }
.section h2 { color: #52c41a; font-size: 16px; margin-bottom: 10px; }
.item { margin-bottom: 10px; }
.time { color: #999; margin-left: 10px; }
</style>
