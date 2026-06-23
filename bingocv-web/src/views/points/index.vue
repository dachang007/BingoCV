<template>
  <div class="page">
    <div class="page-header">
      <div>
        <p class="eyebrow">Points Center</p>
        <h2>积分中心</h2>
      </div>
      <el-button type="primary" :disabled="todaySigned" @click="checkIn">
        {{ todaySigned ? '今日已签到' : '每日签到' }}
      </el-button>
    </div>

    <div class="summary-grid">
      <section class="summary-card">
        <span>当前余额</span>
        <strong>{{ points.balance || 0 }}</strong>
      </section>
      <section class="summary-card">
        <span>累计获得</span>
        <strong>{{ points.totalEarned || 0 }}</strong>
      </section>
      <section class="summary-card">
        <span>累计消费</span>
        <strong>{{ points.totalSpent || 0 }}</strong>
      </section>
    </div>

    <section class="panel">
      <div class="section-header">
        <h3>任务奖励</h3>
      </div>
      <el-table :data="tasks" empty-text="暂无任务" class="task-table">
        <el-table-column label="任务" prop="name" min-width="150" />
        <el-table-column label="说明" prop="description" min-width="220" />
        <el-table-column label="完成度" min-width="160">
          <template #default="{ row }">
            <el-progress :percentage="row.progress || 0" :stroke-width="8" />
          </template>
        </el-table-column>
        <el-table-column label="奖励" width="90" align="center">
          <template #default="{ row }">+{{ row.rewardPoints }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.rewarded" type="success" effect="plain">已领取</el-tag>
            <el-tag v-else-if="row.completed" type="warning" effect="plain">待领取</el-tag>
            <el-tag v-else type="info" effect="plain">待完成</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center">
          <template #default="{ row }">
            <el-button v-if="row.rewarded" size="small" disabled>已领取</el-button>
            <el-button v-else-if="row.completed" size="small" type="primary" :loading="claimingId === row.id" @click="claim(row)">
              领取
            </el-button>
            <el-button v-else size="small" type="primary" plain @click="goTask(row)">去完成</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <section class="panel">
      <div class="section-header">
        <h3>积分流水</h3>
      </div>
      <el-timeline>
        <el-timeline-item v-for="item in logs" :key="item.id" :timestamp="item.createTime">
          {{ item.remark || item.bizType }} <strong>{{ item.amount > 0 ? '+' : '' }}{{ item.amount }}</strong>
        </el-timeline-item>
      </el-timeline>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { claimReward, getPointsDashboard, signIn } from '@/request/points.js'

const router = useRouter()
const points = ref({})
const tasks = ref([])
const logs = ref([])
const todaySigned = ref(false)
const claimingId = ref(null)

const loadDashboard = async () => {
  const data = await getPointsDashboard()
  points.value = data.account || {}
  tasks.value = data.tasks || []
  logs.value = data.logs || []
  todaySigned.value = !!data.todaySigned
}

const checkIn = async () => {
  const data = await signIn()
  ElMessage.success(`签到成功，获得 ${data.rewardPoints} 积分`)
  await loadDashboard()
}

const claim = async (row) => {
  claimingId.value = row.id
  try {
    const earned = await claimReward(row.id)
    ElMessage.success(`领取成功，获得 ${earned} 积分`)
    await loadDashboard()
  } finally {
    claimingId.value = null
  }
}

const taskRouteMap = {
  daily_login: '/admin/points',
  complete_profile: '/admin/profiles',
  add_first_edu: '/admin/education',
  add_first_work: '/admin/work',
  select_template: '/admin/templates',
  resume_full_score: '/admin/profiles',
  share_resume: '/admin/share'
}

const goTask = (row) => {
  const path = taskRouteMap[row.taskKey]
  if (path) {
    router.push(path)
  } else {
    ElMessage.info('该任务暂未配置跳转页面')
  }
}

onMounted(loadDashboard)
</script>

<style scoped>
.page {
  min-height: 100%;
  padding: 24px;
  background: var(--el-bg-color);
  color: var(--el-text-color-primary);
}

.page-header,
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.page-header {
  margin-bottom: 20px;
}

.eyebrow {
  margin: 0 0 4px;
  color: var(--secondary-text-color);
  font-size: 13px;
}

h2,
h3 {
  margin: 0;
  color: var(--el-text-color-primary);
}

h2 {
  font-size: 24px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 18px;
}

.summary-card,
.panel {
  background: var(--el-bg-color);
  border: 1px solid var(--light-border);
  border-radius: 8px;
}

.summary-card {
  padding: 18px;
}

.summary-card span {
  display: block;
  margin-bottom: 10px;
  color: var(--secondary-text-color);
}

.summary-card strong {
  color: var(--el-text-color-primary);
  font-size: 30px;
}

.panel {
  padding: 20px;
  margin-bottom: 18px;
}

.task-table {
  margin-top: 14px;
}

@media (max-width: 780px) {
  .summary-grid {
    grid-template-columns: 1fr;
  }
}
</style>
