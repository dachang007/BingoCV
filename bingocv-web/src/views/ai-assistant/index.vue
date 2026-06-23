<template>
  <div class="ai-page">
    <header class="page-header">
      <div>
        <h2>AI 简历助手</h2>
        <p>自动诊断简历完整度，生成行动计划、JD 匹配、润色建议和面试准备问题。</p>
      </div>
      <div class="header-actions">
        <el-button :loading="agentLoading" @click="loadAgentPlan">刷新计划</el-button>
        <el-button type="primary" :loading="loading" @click="loadAnalysis">重新分析</el-button>
      </div>
    </header>

    <el-skeleton v-if="(loading || agentLoading) && !analysis && !agentPlan" :rows="8" animated />

    <template v-else>
      <section v-if="agentPlan" class="agent-panel">
        <div class="agent-score">
          <el-progress type="dashboard" :percentage="agentPlan.readiness || 0" :stroke-width="12" />
          <strong>{{ agentPlan.stage }}</strong>
        </div>
        <div class="agent-main">
          <div class="panel-title-row">
            <div>
              <p class="eyebrow">AI Agent 行动计划</p>
              <h3>{{ agentPlan.summary }}</h3>
            </div>
          </div>

          <div class="action-list">
            <article v-for="item in agentPlan.actions" :key="item.title" class="action-card">
              <div class="action-head">
                <strong>{{ item.title }}</strong>
                <el-tag :type="priorityType(item.priority)" effect="plain">{{ priorityLabel(item.priority) }}</el-tag>
              </div>
              <p>{{ item.description }}</p>
              <div class="action-meta">
                <span>原因：{{ item.reason }}</span>
                <span>收益：{{ item.expectedImpact }}</span>
              </div>
              <el-button type="primary" plain @click="goAction(item)">{{ item.buttonText || '去完成' }}</el-button>
            </article>
          </div>

          <ul class="reminder-list">
            <li v-for="item in agentPlan.reminders" :key="item">{{ item }}</li>
          </ul>
        </div>
      </section>

      <section class="panel jd-panel">
        <div class="panel-title-row">
          <div>
            <h3>JD 匹配分析</h3>
            <p>粘贴目标岗位 JD，AI 会对比你的简历关键词、优势和风险点。</p>
          </div>
          <el-button type="primary" :loading="jdLoading" @click="submitJdMatch">开始匹配</el-button>
        </div>

        <div class="jd-grid">
          <div class="jd-form">
            <el-input
              v-model="jdForm.jobTitle"
              maxlength="60"
              show-word-limit
              placeholder="目标岗位，例如：Java 后端开发工程师"
            />
            <el-input
              v-model="jdForm.jdContent"
              type="textarea"
              :rows="9"
              maxlength="4000"
              show-word-limit
              placeholder="粘贴招聘 JD、岗位职责、任职要求等内容"
            />
          </div>

          <div class="jd-result">
            <template v-if="jdResult">
              <div class="jd-score">
                <el-progress type="dashboard" :percentage="jdResult.matchScore || 0" :stroke-width="12" />
                <div>
                  <el-tag type="success" effect="plain">{{ jdResult.level }}</el-tag>
                  <p>{{ jdResult.summary }}</p>
                </div>
              </div>

              <div class="keyword-block">
                <span>已匹配关键词</span>
                <div class="tag-list">
                  <el-tag v-for="item in jdResult.matchedKeywords" :key="item" type="success" effect="plain">
                    {{ item }}
                  </el-tag>
                  <el-empty v-if="!jdResult.matchedKeywords?.length" description="暂无匹配关键词" :image-size="50" />
                </div>
              </div>

              <div class="keyword-block">
                <div class="keyword-head">
                  <span>待补齐关键词</span>
                  <el-button
                    text
                    type="primary"
                    :disabled="!jdResult.missingKeywords?.length"
                    :loading="writingSkills"
                    @click="writeMissingKeywordsToSkill"
                  >
                    一键补充到技能
                  </el-button>
                </div>
                <div class="tag-list">
                  <el-tag v-for="item in jdResult.missingKeywords" :key="item" type="warning" effect="plain">
                    {{ item }}
                  </el-tag>
                  <el-empty v-if="!jdResult.missingKeywords?.length" description="暂无明显缺口" :image-size="50" />
                </div>
              </div>
            </template>
            <el-empty v-else description="输入 JD 后查看匹配结果" :image-size="92" />
          </div>
        </div>

        <div v-if="jdResult" class="jd-detail-grid">
          <article class="mini-panel">
            <h4>匹配优势</h4>
            <ul>
              <li v-for="item in jdResult.strengths" :key="item">{{ item }}</li>
            </ul>
          </article>
          <article class="mini-panel warning">
            <h4>风险提醒</h4>
            <ul>
              <li v-for="item in jdResult.risks" :key="item">{{ item }}</li>
            </ul>
          </article>
          <article class="mini-panel">
            <h4>改写建议</h4>
            <ul>
              <li v-for="item in jdResult.rewriteSuggestions" :key="item">{{ item }}</li>
            </ul>
          </article>
          <article class="mini-panel">
            <h4>面试准备</h4>
            <ul>
              <li v-for="item in jdResult.interviewFocus" :key="item">{{ item }}</li>
            </ul>
          </article>
        </div>
      </section>

      <template v-if="analysis">
        <section class="score-panel">
          <div class="score-ring">
            <el-progress type="dashboard" :percentage="analysis.score || 0" :stroke-width="12" />
            <strong>{{ analysis.level }}</strong>
          </div>
          <div class="score-copy">
            <h3>{{ analysis.summary }}</h3>
            <p>建议优先处理低分模块，再补充成果数据和岗位关键词。</p>
          </div>
        </section>

        <section class="section-grid">
          <article v-for="item in analysis.sectionScores" :key="item.name" class="metric-card">
            <div class="metric-title">
              <span>{{ item.name }}</span>
              <strong>{{ item.score }} 分</strong>
            </div>
            <el-progress :percentage="item.score" :stroke-width="8" />
            <p>{{ item.comment }}</p>
          </article>
        </section>

        <section class="content-grid">
          <article class="panel">
            <h3>亮点</h3>
            <ul>
              <li v-for="item in analysis.highlights" :key="item">{{ item }}</li>
            </ul>
          </article>

          <article class="panel warning">
            <h3>待优化问题</h3>
            <ul>
              <li v-for="item in analysis.problems" :key="item">{{ item }}</li>
            </ul>
          </article>

          <article class="panel">
            <h3>优化建议</h3>
            <ul>
              <li v-for="item in analysis.suggestions" :key="item">{{ item }}</li>
            </ul>
          </article>

          <article class="panel">
            <h3>润色参考</h3>
            <ul>
              <li v-for="item in analysis.polishedBullets" :key="item">{{ item }}</li>
            </ul>
          </article>
        </section>

        <section class="panel interview-panel">
          <div class="panel-title-row">
            <h3>面试题练习</h3>
            <el-button text type="primary" :disabled="!analysis.polishedBullets?.length" @click="applyAnalysisToProfile">
              一键写回简介
            </el-button>
          </div>
          <div class="question-list">
            <div v-for="(item, index) in analysis.interviewQuestions" :key="item" class="question-item">
              <span>{{ index + 1 }}</span>
              <p>{{ item }}</p>
            </div>
          </div>
        </section>
      </template>

      <section class="panel polish-panel">
        <div class="panel-title-row">
          <div>
            <h3>单段润色</h3>
            <p>将一段简历内容润色后，可以直接写回个人简介。</p>
          </div>
          <el-button type="primary" :loading="polishing" @click="submitPolish">开始润色</el-button>
        </div>
        <div class="polish-grid">
          <div class="polish-inputs">
            <el-select v-model="polishForm.scene" placeholder="选择润色场景">
              <el-option label="个人简介" value="个人简介" />
              <el-option label="工作经历" value="工作经历" />
              <el-option label="项目经历" value="项目经历" />
              <el-option label="技能描述" value="技能描述" />
            </el-select>
            <el-input
              v-model="polishForm.content"
              type="textarea"
              :rows="7"
              maxlength="1200"
              show-word-limit
              placeholder="粘贴需要润色的简历内容"
            />
          </div>
          <div class="polish-result">
            <div class="result-header">
              <span>润色结果</span>
              <div class="result-actions">
                <el-button text type="primary" :disabled="!polishResult" @click="copyPolishResult">复制</el-button>
                <el-button text type="primary" :disabled="!polishResult" :loading="writingProfile" @click="writePolishToProfile">
                  写回简介
                </el-button>
              </div>
            </div>
            <p v-if="polishResult">{{ polishResult }}</p>
            <el-empty v-else description="暂无润色结果" :image-size="70" />
          </div>
        </div>
      </section>
    </template>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { analyzeResume, getResumeAgentPlan, matchResumeJd, polishResumeText } from '@/request/ai.js'
import { getProfiles, getSkill, updateProfiles, updateSkill } from '@/request/resume.js'

const router = useRouter()
const loading = ref(false)
const agentLoading = ref(false)
const jdLoading = ref(false)
const polishing = ref(false)
const writingProfile = ref(false)
const writingSkills = ref(false)
const analysis = ref(null)
const agentPlan = ref(null)
const jdResult = ref(null)
const polishResult = ref('')
const jdForm = ref({
  jobTitle: '',
  jdContent: ''
})
const polishForm = ref({
  scene: '工作经历',
  content: ''
})

function priorityLabel(priority) {
  return ({ HIGH: '高优先级', MEDIUM: '中优先级', LOW: '低优先级' })[priority] || '建议'
}

function priorityType(priority) {
  return ({ HIGH: 'danger', MEDIUM: 'warning', LOW: 'info' })[priority] || 'info'
}

async function loadAgentPlan() {
  agentLoading.value = true
  try {
    agentPlan.value = await getResumeAgentPlan()
  } catch (error) {
    ElMessage.error(error?.message || '生成行动计划失败，请稍后重试')
  } finally {
    agentLoading.value = false
  }
}

async function loadAnalysis() {
  loading.value = true
  try {
    analysis.value = await analyzeResume()
  } catch (error) {
    ElMessage.error(error?.message || '分析失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

function goAction(item) {
  if (!item.routeName) return
  router.push({ name: item.routeName })
}

async function submitJdMatch() {
  if (!jdForm.value.jdContent.trim()) {
    ElMessage.warning('请先粘贴目标岗位 JD')
    return
  }
  jdLoading.value = true
  try {
    jdResult.value = await matchResumeJd(jdForm.value)
  } catch (error) {
    ElMessage.error(error?.message || 'JD 匹配失败，请稍后重试')
  } finally {
    jdLoading.value = false
  }
}

async function submitPolish() {
  if (!polishForm.value.content.trim()) {
    ElMessage.warning('请先输入需要润色的内容')
    return
  }
  polishing.value = true
  try {
    polishResult.value = await polishResumeText(polishForm.value)
  } catch (error) {
    ElMessage.error(error?.message || '润色失败，请稍后重试')
  } finally {
    polishing.value = false
  }
}

async function copyPolishResult() {
  try {
    await navigator.clipboard.writeText(polishResult.value)
    ElMessage.success('已复制润色结果')
  } catch {
    ElMessage.error('复制失败')
  }
}

async function applyAnalysisToProfile() {
  const intro = (analysis.value?.polishedBullets || []).slice(0, 3).join('；')
  if (!intro) {
    ElMessage.warning('没有可写回的简介内容')
    return
  }
  writingProfile.value = true
  try {
    const profile = await getProfiles()
    await updateProfiles({
      ...profile,
      description: intro
    })
    ElMessage.success('已写回个人简介')
    await loadAnalysis()
  } catch (error) {
    ElMessage.error(error?.message || '写回简介失败')
  } finally {
    writingProfile.value = false
  }
}

async function writePolishToProfile() {
  if (!polishResult.value.trim()) {
    ElMessage.warning('请先生成润色结果')
    return
  }
  writingProfile.value = true
  try {
    const profile = await getProfiles()
    await updateProfiles({
      ...profile,
      description: polishResult.value
    })
    ElMessage.success('已将润色结果写回个人简介')
    await loadAnalysis()
  } catch (error) {
    ElMessage.error(error?.message || '写回失败')
  } finally {
    writingProfile.value = false
  }
}

async function writeMissingKeywordsToSkill() {
  const missing = jdResult.value?.missingKeywords || []
  if (!missing.length) {
    ElMessage.warning('没有可补充的关键词')
    return
  }
  writingSkills.value = true
  try {
    const skill = await getSkill()
    const current = skill?.keywords ? skill.keywords.split(/[\s,，、]+/).filter(Boolean) : []
    const merged = Array.from(new Set([...current, ...missing])).join(' ')
    await updateSkill({
      ...skill,
      keywords: merged
    })
    ElMessage.success('已补充到技能关键词')
    await loadAnalysis()
  } catch (error) {
    ElMessage.error(error?.message || '补充技能失败')
  } finally {
    writingSkills.value = false
  }
}

onMounted(() => {
  loadAgentPlan()
  loadAnalysis()
})
</script>

<style lang="scss" scoped>
.ai-page {
  padding: 22px;
  min-height: 100%;
  color: var(--el-text-color-primary);
  background: var(--el-bg-color);
}

.page-header,
.agent-panel,
.score-panel,
.panel,
.metric-card {
  border: 1px solid var(--el-border-color-light);
  background: var(--el-bg-color-overlay);
  box-shadow: var(--el-box-shadow-light);
}

.page-header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: center;
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

.header-actions {
  display: flex;
  gap: 10px;
}

.agent-panel,
.score-panel {
  display: grid;
  grid-template-columns: 170px 1fr;
  gap: 18px;
  align-items: center;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 16px;
}

.agent-score,
.score-ring {
  display: grid;
  justify-items: center;
  gap: 8px;

  strong {
    color: var(--el-color-primary);
    font-size: 18px;
    text-align: center;
  }
}

.agent-main {
  min-width: 0;
}

.eyebrow {
  margin: 0 0 4px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.panel-title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;

  h3 {
    margin: 0;
  }

  p {
    margin: 5px 0 0;
    color: var(--el-text-color-secondary);
  }
}

.action-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 12px;
}

.action-card {
  display: grid;
  gap: 10px;
  padding: 14px;
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;
  background: var(--el-fill-color-light);

  p {
    margin: 0;
    color: var(--el-text-color-regular);
    line-height: 1.6;
  }
}

.action-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}

.action-meta {
  display: grid;
  gap: 4px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
  line-height: 1.5;
}

.reminder-list {
  margin: 14px 0 0;
  padding-left: 18px;
  color: var(--el-text-color-secondary);
  line-height: 1.7;
}

.jd-panel {
  margin-bottom: 16px;
}

.jd-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(320px, 0.9fr);
  gap: 14px;
}

.jd-form {
  display: grid;
  gap: 10px;
}

.jd-result {
  min-height: 260px;
  padding: 14px;
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;
  background: var(--el-fill-color-light);
}

.jd-score {
  display: grid;
  grid-template-columns: 130px 1fr;
  gap: 12px;
  align-items: center;
  margin-bottom: 14px;

  p {
    margin: 10px 0 0;
    color: var(--el-text-color-regular);
    line-height: 1.7;
  }
}

.keyword-block {
  display: grid;
  gap: 8px;
  margin-top: 12px;
}

.keyword-head,
.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.keyword-block > span,
.keyword-head > span {
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  min-height: 32px;
}

.jd-detail-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-top: 14px;
}

.mini-panel {
  padding: 14px;
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;
  background: var(--el-fill-color-light);

  h4 {
    margin: 0 0 10px;
  }

  ul {
    margin: 0;
    padding-left: 18px;
  }

  li {
    margin: 7px 0;
    color: var(--el-text-color-regular);
    line-height: 1.7;
  }
}

.score-copy {
  h3 {
    margin: 0 0 10px;
    font-size: 18px;
  }

  p {
    margin: 0;
    color: var(--el-text-color-secondary);
  }
}

.section-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(210px, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.metric-card {
  padding: 14px;
  border-radius: 8px;

  p {
    margin: 8px 0 0;
    color: var(--el-text-color-secondary);
    line-height: 1.6;
  }
}

.metric-title {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;

  strong {
    color: var(--el-color-primary);
  }
}

.content-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 16px;
}

.panel {
  padding: 18px;
  border-radius: 8px;

  h3 {
    margin: 0 0 12px;
    font-size: 16px;
  }

  ul {
    margin: 0;
    padding-left: 18px;
  }

  li {
    margin: 8px 0;
    line-height: 1.7;
    color: var(--el-text-color-regular);
  }
}

.warning {
  border-color: var(--el-color-warning-light-5);
}

.question-list {
  display: grid;
  gap: 10px;
}

.question-item {
  display: grid;
  grid-template-columns: 28px 1fr;
  gap: 10px;
  align-items: start;
  padding: 12px;
  border-radius: 8px;
  background: var(--el-fill-color-light);

  span {
    width: 28px;
    height: 28px;
    display: grid;
    place-items: center;
    border-radius: 50%;
    color: var(--el-color-primary);
    background: var(--el-color-primary-light-9);
    font-weight: 700;
  }

  p {
    margin: 3px 0 0;
    line-height: 1.7;
  }
}

.polish-panel {
  margin-top: 16px;
}

.polish-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(280px, 0.9fr);
  gap: 14px;
}

.polish-inputs {
  display: grid;
  gap: 10px;
}

.polish-result {
  min-height: 220px;
  padding: 14px;
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;
  background: var(--el-fill-color-light);

  p {
    margin: 10px 0 0;
    line-height: 1.8;
    white-space: pre-wrap;
  }
}

.result-actions {
  display: flex;
  gap: 8px;
}

@media (max-width: 768px) {
  .page-header,
  .agent-panel,
  .score-panel {
    grid-template-columns: 1fr;
  }

  .page-header {
    align-items: flex-start;
    flex-direction: column;
  }

  .header-actions {
    width: 100%;
    flex-wrap: wrap;
  }

  .content-grid,
  .jd-grid,
  .jd-detail-grid,
  .polish-grid {
    grid-template-columns: 1fr;
  }

  .jd-score {
    grid-template-columns: 1fr;
  }

  .result-header {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
