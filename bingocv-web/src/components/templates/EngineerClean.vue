<template>
  <div class="template-engineer-clean">
    <header class="resume-header">
      <div class="header-content">
        <h1>{{ resume.profile?.name || '姓名' }}</h1>
        <div class="title-line"></div>
        <div class="contact-row">
          <span v-if="resume.profile?.phone"><el-icon><Phone /></el-icon> {{ resume.profile.phone }}</span>
          <span v-if="resume.profile?.email"><el-icon><Message /></el-icon> {{ resume.profile.email }}</span>
          <span v-if="resume.profile?.city"><el-icon><Location /></el-icon> {{ resume.profile.city }}</span>
          <span v-if="resume.profile?.weixin"><el-icon><ChatDotRound /></el-icon> {{ resume.profile.weixin }}</span>
        </div>
      </div>
      <el-avatar v-if="resume.profile?.photo" :size="90" :src="resume.profile.photo" class="avatar" />
    </header>

    <section v-if="resume.profile?.description" class="section summary">
      <h2><el-icon><Document /></el-icon> 个人简介</h2>
      <p>{{ resume.profile.description }}</p>
    </section>

    <section v-if="resume.work?.length" class="section">
      <h2><el-icon><Briefcase /></el-icon> 项目经历</h2>
      <div v-for="work in resume.work" :key="work.id" class="work-item">
        <div class="work-header">
          <div class="company-info">
            <strong class="company">{{ work.company }}</strong>
            <span class="job">{{ work.job }}</span>
          </div>
          <span class="time">{{ work.start }} - {{ work.end || '至今' }}</span>
        </div>
        <p v-if="work.description" class="work-desc">{{ work.description }}</p>
      </div>
    </section>

    <section v-if="resume.edu?.length" class="section">
      <h2><el-icon><Reading /></el-icon> 教育背景</h2>
      <div v-for="edu in resume.edu" :key="edu.id" class="edu-item">
        <div class="edu-header">
          <strong>{{ edu.school }}</strong>
          <span class="time">{{ edu.start }} - {{ edu.end }}</span>
        </div>
        <div class="major">{{ edu.study }}</div>
      </div>
    </section>

    <section v-if="resume.skill?.keywords" class="section skills-section">
      <h2><el-icon><Tools /></el-icon> 技术栈</h2>
      <div class="tech-stack">
        <span v-for="skill in skillList" :key="skill" class="tech-tag">{{ skill }}</span>
      </div>
    </section>

    <section v-if="resume.specialty?.length" class="section">
      <h2><el-icon><Star /></el-icon> 个人优势</h2>
      <ul class="specialty-list">
        <li v-for="spec in resume.specialty" :key="spec.id">
          <strong>{{ spec.name }}</strong>
          <span v-if="spec.description">：{{ spec.description }}</span>
        </li>
      </ul>
    </section>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { Phone, Message, Location, ChatDotRound, Document, Briefcase, Reading, Tools, Star } from '@element-plus/icons-vue';

const props = defineProps({
  resume: {
    type: Object,
    default: () => ({})
  }
});

const skillList = computed(() => {
  if (!props.resume.skill?.keywords) return [];
  return props.resume.skill.keywords.split(/[,，\s]+/).filter(Boolean);
});
</script>

<style scoped>
.template-engineer-clean {
  padding: 32px 40px;
  font-family: 'SF Pro Display', -apple-system, sans-serif;
  color: #1a1a1a;
  line-height: 1.6;
}

.resume-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding-bottom: 20px;
  border-bottom: 3px solid #0066cc;
  margin-bottom: 24px;
}

.header-content h1 {
  margin: 0 0 8px;
  font-size: 32px;
  font-weight: 600;
  color: #0066cc;
}

.title-line {
  width: 60px;
  height: 3px;
  background: #0066cc;
  margin-bottom: 12px;
}

.contact-row {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  color: #555;
  font-size: 13px;
}

.contact-row span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.avatar {
  border: 3px solid #0066cc;
}

.section {
  margin-bottom: 20px;
}

.section h2 {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 12px;
  font-size: 16px;
  font-weight: 600;
  color: #0066cc;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.section p {
  margin: 0;
  color: #444;
  line-height: 1.8;
}

.work-item, .edu-item {
  margin-bottom: 14px;
}

.work-header, .edu-header {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 4px;
}

.company-info {
  display: flex;
  gap: 12px;
  align-items: baseline;
}

.company {
  font-size: 15px;
  color: #1a1a1a;
}

.job {
  color: #666;
  font-size: 13px;
}

.time {
  color: #888;
  font-size: 12px;
}

.work-desc {
  margin: 4px 0 0;
  color: #555;
  font-size: 13px;
  line-height: 1.7;
}

.major {
  color: #666;
  font-size: 13px;
}

.tech-stack {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tech-tag {
  padding: 4px 12px;
  background: #e6f3ff;
  color: #0066cc;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.specialty-list {
  margin: 0;
  padding-left: 20px;
}

.specialty-list li {
  margin-bottom: 6px;
  color: #444;
  font-size: 13px;
}
</style>
