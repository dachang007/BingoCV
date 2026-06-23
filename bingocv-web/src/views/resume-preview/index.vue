<template>
  <div class="preview-page">
    <div class="preview-header">
      <div>
        <p class="eyebrow">Resume Preview</p>
        <h2>简历预览</h2>
      </div>
      <div class="header-actions">
        <el-select v-model="selectedTemplate" placeholder="选择模板" class="template-select" @change="handleTemplateChange">
          <el-option v-for="item in templates" :key="item.templateKey" :label="item.name" :value="item.templateKey">
            <span>{{ item.name }}</span>
            <span class="template-type">{{ getTemplateTypeLabel(item.templateType) }}</span>
          </el-option>
        </el-select>
        <el-dropdown @command="handleExport">
          <el-button type="primary">
            导出简历<el-icon class="el-icon--right"><arrow-down /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="pdf">导出 PDF</el-dropdown-item>
              <el-dropdown-item command="word">导出 Word</el-dropdown-item>
              <el-dropdown-item command="markdown">导出 Markdown</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <div class="preview-container">
      <div class="resume-wrapper" ref="resumeRef">
        <component :is="currentTemplate" v-if="resumeData && currentTemplate" :resume="resumeData" />
        <el-empty v-else description="暂无简历数据" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, shallowRef } from 'vue'
import { ArrowDown } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getMyResume } from '@/request/resume.js'
import { getMyTemplates, listTemplates } from '@/request/template.js'
import { exportResume } from '@/request/export.js'

import EngineerClean from '@/components/templates/EngineerClean.vue'
import ProductGrowth from '@/components/templates/ProductGrowth.vue'
import FinanceStable from '@/components/templates/FinanceStable.vue'
import TeacherClassic from '@/components/templates/TeacherClassic.vue'
import MedicalProfessional from '@/components/templates/MedicalProfessional.vue'
import DesignerPortfolio from '@/components/templates/DesignerPortfolio.vue'
import GeneralMinimal from '@/components/templates/GeneralMinimal.vue'

const templateMap = {
  'engineer-clean': EngineerClean,
  'product-growth': ProductGrowth,
  'finance-stable': FinanceStable,
  'teacher-classic': TeacherClassic,
  'medical-professional': MedicalProfessional,
  'designer-portfolio': DesignerPortfolio,
  'general-minimal': GeneralMinimal
}

const resumeRef = ref(null)
const resumeData = ref(null)
const templates = ref([])
const myTemplates = ref([])
const selectedTemplate = ref('general-minimal')
const currentTemplate = shallowRef(GeneralMinimal)

const fileBaseName = computed(() => resumeData.value?.profile?.name || 'BingoCV简历')

const getTemplateTypeLabel = (type) => {
  const labels = { FREE: '免费', POINTS: '积分', PAID: '付费' }
  return labels[type] || type
}

const handleTemplateChange = (key) => {
  currentTemplate.value = templateMap[key] || GeneralMinimal
}

const handleExport = async (format) => {
  if (!resumeData.value) {
    ElMessage.warning('暂无简历数据可导出')
    return
  }
  try {
    const blob = await exportResume({ format, templateKey: selectedTemplate.value })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `${fileBaseName.value}.${format === 'markdown' ? 'md' : format === 'word' ? 'docx' : 'pdf'}`
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error(error?.msg || error?.message || '导出失败')
  }
}

const loadResume = async () => {
  try {
    resumeData.value = await getMyResume()
  } catch (error) {
    ElMessage.error('加载简历数据失败')
  }
}

const loadTemplates = async () => {
  try {
    const [allTemplates, ownedTemplates] = await Promise.all([
      listTemplates(),
      getMyTemplates()
    ])
    templates.value = allTemplates || []
    myTemplates.value = ownedTemplates || []

    const activeTemplate = myTemplates.value.find(item => item.active)
    if (activeTemplate) {
      selectedTemplate.value = activeTemplate.templateKey
      currentTemplate.value = templateMap[activeTemplate.templateKey] || GeneralMinimal
    }
  } catch (error) {
    ElMessage.error('加载模板失败')
  }
}

onMounted(() => {
  loadResume()
  loadTemplates()
})
</script>

<style scoped>
.preview-page {
  min-height: 100%;
  padding: 24px;
  background: var(--light-fill);
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
  padding: 16px 20px;
  background: var(--el-bg-color);
  border: 1px solid var(--light-border);
  border-radius: 8px;
}

.eyebrow {
  margin: 0 0 4px;
  color: var(--secondary-text-color);
  font-size: 13px;
}

h2 {
  margin: 0;
  color: var(--el-text-color-primary);
  font-size: 24px;
}

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.template-select {
  width: 210px;
}

.template-type {
  margin-left: 8px;
  color: var(--secondary-text-color);
  font-size: 12px;
}

.preview-container {
  display: flex;
  justify-content: center;
  padding: 20px;
}

.resume-wrapper {
  width: 210mm;
  min-height: 297mm;
  overflow: hidden;
  background: #fff;
  border-radius: 4px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

@media (max-width: 900px) {
  .preview-header {
    align-items: flex-start;
    flex-direction: column;
  }

  .header-actions {
    width: 100%;
    flex-wrap: wrap;
  }

  .template-select {
    width: 100%;
  }

  .preview-container {
    padding: 0;
  }

  .resume-wrapper {
    width: 100%;
    min-height: auto;
  }
}
</style>
