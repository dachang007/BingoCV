<template>
  <div class="interview-page">
    <header class="page-header">
      <div>
        <h2>AI 面试练习</h2>
        <p>根据目标岗位或 JD 生成面试题，并持续记录掌握状态。</p>
      </div>
      <el-segmented v-model="filterStatus" :options="statusOptions" @change="loadQuestions" />
    </header>

    <section class="panel">
      <div class="generator-form">
        <el-input v-model="form.jobTitle" placeholder="目标岗位，例如：Java 后端开发工程师" />
        <el-input-number v-model="form.count" :min="3" :max="15" controls-position="right" />
        <el-button type="primary" :loading="generating" @click="generateQuestions">生成题目</el-button>
      </div>
      <el-input
        v-model="form.jdContent"
        type="textarea"
        :rows="5"
        maxlength="3000"
        show-word-limit
        placeholder="可选：粘贴 JD，系统会生成更贴近岗位要求的问题"
      />
    </section>

    <section class="stats-grid">
      <article class="stat-card"><span>全部题目</span><strong>{{ questions.length }}</strong></article>
      <article class="stat-card"><span>待练习</span><strong>{{ countByStatus('TODO') }}</strong></article>
      <article class="stat-card"><span>练习中</span><strong>{{ countByStatus('PRACTICING') }}</strong></article>
      <article class="stat-card"><span>已掌握</span><strong>{{ countByStatus('MASTERED') }}</strong></article>
    </section>

    <section v-loading="loading" class="question-list">
      <article v-for="item in questions" :key="item.id" class="question-card">
        <div class="question-top">
          <div>
            <div class="tag-row">
              <el-tag effect="plain">{{ item.jobTitle || '目标岗位' }}</el-tag>
              <el-tag :type="difficultyType(item.difficulty)" effect="plain">{{ difficultyLabel(item.difficulty) }}</el-tag>
              <el-tag type="info" effect="plain">{{ item.category || '通用问题' }}</el-tag>
            </div>
            <h3>{{ item.question }}</h3>
          </div>
          <el-select v-model="item.masteryStatus" class="status-select" @change="status => saveQuestion(item, status)">
            <el-option label="待练习" value="TODO" />
            <el-option label="练习中" value="PRACTICING" />
            <el-option label="已掌握" value="MASTERED" />
          </el-select>
        </div>

        <div class="answer-hint">
          <span>回答要点</span>
          <p>{{ item.answerHint || '暂无回答要点' }}</p>
        </div>

        <el-input
          v-model="item.practiceNote"
          type="textarea"
          :rows="3"
          maxlength="800"
          show-word-limit
          placeholder="记录你的回答思路、薄弱点或复盘结论"
          @blur="saveQuestion(item)"
        />

        <div class="card-actions">
          <span>{{ item.createTime || '-' }}</span>
          <el-popconfirm title="确认删除这道题吗？" @confirm="removeQuestion(item.id)">
            <template #reference>
              <el-button text type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </div>
      </article>

      <el-empty v-if="!loading && !questions.length" description="暂无面试题，先生成一组练习题吧" />
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  deleteInterviewQuestion,
  generateInterviewQuestions,
  getInterviewQuestions,
  updateInterviewQuestion
} from '@/request/ai.js'

const loading = ref(false)
const generating = ref(false)
const questions = ref([])
const filterStatus = ref('')
const form = ref({ jobTitle: '', jdContent: '', count: 8 })

const statusOptions = [
  { label: '全部', value: '' },
  { label: '待练习', value: 'TODO' },
  { label: '练习中', value: 'PRACTICING' },
  { label: '已掌握', value: 'MASTERED' }
]

function countByStatus(status) {
  return questions.value.filter(item => item.masteryStatus === status).length
}

function difficultyLabel(difficulty) {
  return ({ EASY: '基础', MEDIUM: '进阶', HARD: '高频深挖' })[difficulty] || '进阶'
}

function difficultyType(difficulty) {
  return ({ EASY: 'success', MEDIUM: 'warning', HARD: 'danger' })[difficulty] || 'warning'
}

async function loadQuestions() {
  loading.value = true
  try {
    questions.value = await getInterviewQuestions({ masteryStatus: filterStatus.value || undefined }) || []
  } finally {
    loading.value = false
  }
}

async function generateQuestions() {
  generating.value = true
  try {
    const created = await generateInterviewQuestions(form.value)
    ElMessage.success(`已生成 ${created?.length || 0} 道面试题`)
    await loadQuestions()
  } catch (error) {
    ElMessage.error(error?.message || '生成面试题失败')
  } finally {
    generating.value = false
  }
}

async function saveQuestion(item, status) {
  try {
    await updateInterviewQuestion(item.id, {
      masteryStatus: status || item.masteryStatus,
      practiceNote: item.practiceNote
    })
    ElMessage.success('练习记录已保存')
  } catch (error) {
    ElMessage.error(error?.message || '保存失败')
  }
}

async function removeQuestion(id) {
  await deleteInterviewQuestion(id)
  ElMessage.success('题目已删除')
  await loadQuestions()
}

onMounted(loadQuestions)
</script>

<style lang="scss" scoped>
.interview-page {
  min-height: 100%;
  padding: 22px;
  color: var(--el-text-color-primary);
  background: var(--el-bg-color);
}

.page-header,
.panel,
.stat-card,
.question-card {
  border: 1px solid var(--el-border-color-light);
  background: var(--el-bg-color-overlay);
  box-shadow: var(--el-box-shadow-light);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  padding: 18px 20px;
  border-radius: 8px;
  margin-bottom: 16px;

  h2 {
    margin: 0 0 6px;
    font-size: 22px;
  }

  p {
    margin: 0;
    color: var(--el-text-color-secondary);
  }
}

.panel {
  padding: 18px;
  border-radius: 8px;
  margin-bottom: 16px;
}

.generator-form {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 130px 110px;
  gap: 12px;
  margin-bottom: 12px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.stat-card {
  padding: 16px;
  border-radius: 8px;

  span {
    color: var(--el-text-color-secondary);
  }

  strong {
    display: block;
    margin-top: 8px;
    font-size: 26px;
    color: var(--el-color-primary);
  }
}

.question-list {
  display: grid;
  gap: 14px;
}

.question-card {
  padding: 18px;
  border-radius: 8px;
}

.question-top {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 130px;
  gap: 14px;
  align-items: start;

  h3 {
    margin: 12px 0 0;
    line-height: 1.6;
    font-size: 17px;
  }
}

.tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.status-select {
  width: 130px;
}

.answer-hint {
  margin: 14px 0;
  padding: 12px;
  border-radius: 8px;
  background: var(--el-fill-color-light);

  span {
    color: var(--el-text-color-secondary);
    font-size: 13px;
  }

  p {
    margin: 6px 0 0;
    line-height: 1.7;
  }
}

.card-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

@media (max-width: 900px) {
  .page-header,
  .question-top {
    grid-template-columns: 1fr;
  }

  .page-header {
    align-items: flex-start;
    flex-direction: column;
  }

  .generator-form,
  .stats-grid {
    grid-template-columns: 1fr;
  }

  .status-select {
    width: 100%;
  }
}
</style>
