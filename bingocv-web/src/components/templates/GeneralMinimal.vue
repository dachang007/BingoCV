<template>
  <div class="template-general-minimal">
    <header class="resume-header">
      <div class="header-left">
        <el-avatar v-if="resume.profile?.photo" :size="100" :src="resume.profile.photo" />
        <el-avatar v-else :size="100">
          <el-icon :size="40"><User /></el-icon>
        </el-avatar>
      </div>
      <div class="header-right">
        <h1>{{ resume.profile?.name || '姓名' }}</h1>
        <div class="contact-info">
          <span v-if="resume.profile?.phone"><el-icon><Phone /></el-icon> {{ resume.profile.phone }}</span>
          <span v-if="resume.profile?.email"><el-icon><Message /></el-icon> {{ resume.profile.email }}</span>
          <span v-if="resume.profile?.city"><el-icon><Location /></el-icon> {{ resume.profile.city }}</span>
        </div>
      </div>
    </header>

    <section v-if="resume.profile?.description" class="section">
      <h2>个人简介</h2>
      <p class="description">{{ resume.profile.description }}</p>
    </section>

    <section v-if="resume.work?.length" class="section">
      <h2>工作经历</h2>
      <div v-for="work in resume.work" :key="work.id" class="item">
        <div class="item-header">
          <strong>{{ work.company }}</strong>
          <span class="time">{{ work.start }} - {{ work.end || '至今' }}</span>
        </div>
        <div class="item-subtitle">{{ work.job }}</div>
        <p v-if="work.description" class="item-content">{{ work.description }}</p>
      </div>
    </section>

    <section v-if="resume.edu?.length" class="section">
      <h2>教育经历</h2>
      <div v-for="edu in resume.edu" :key="edu.id" class="item">
        <div class="item-header">
          <strong>{{ edu.school }}</strong>
          <span class="time">{{ edu.start }} - {{ edu.end || '至今' }}</span>
        </div>
        <div class="item-subtitle">{{ edu.study }}</div>
        <p v-if="edu.description" class="item-content">{{ edu.description }}</p>
      </div>
    </section>

    <section v-if="resume.skill?.keywords" class="section">
      <h2>专业技能</h2>
      <div class="skills">
        <el-tag v-for="skill in skillList" :key="skill" type="info">{{ skill }}</el-tag>
      </div>
    </section>

    <section v-if="resume.specialty?.length" class="section">
      <h2>个人特长</h2>
      <div v-for="spec in resume.specialty" :key="spec.id" class="item">
        <strong>{{ spec.name }}</strong>
        <p v-if="spec.description" class="item-content">{{ spec.description }}</p>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { User, Phone, Message, Location } from '@element-plus/icons-vue';

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
.template-general-minimal {
  padding: 40px;
  font-family: 'Helvetica Neue', Arial, sans-serif;
  color: #333;
  line-height: 1.6;
}

.resume-header {
  display: flex;
  gap: 24px;
  align-items: center;
  padding-bottom: 24px;
  border-bottom: 2px solid #1890ff;
  margin-bottom: 24px;
}

.header-right {
  flex: 1;
}

.header-right h1 {
  margin: 0 0 12px;
  font-size: 28px;
  color: #1f2937;
}

.contact-info {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  color: #666;
  font-size: 14px;
}

.contact-info span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.section {
  margin-bottom: 24px;
}

.section h2 {
  margin: 0 0 16px;
  padding-bottom: 8px;
  font-size: 18px;
  color: #1890ff;
  border-bottom: 1px solid #e8e8e8;
}

.description {
  margin: 0;
  color: #666;
  line-height: 1.8;
}

.item {
  margin-bottom: 16px;
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.item-header strong {
  font-size: 16px;
  color: #333;
}

.time {
  color: #999;
  font-size: 13px;
}

.item-subtitle {
  color: #666;
  font-size: 14px;
  margin-bottom: 8px;
}

.item-content {
  margin: 0;
  color: #666;
  font-size: 14px;
  line-height: 1.8;
}

.skills {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
</style>
