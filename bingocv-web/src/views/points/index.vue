<template>
  <div class="page">
    <div class="page-header">
      <div>
        <p class="eyebrow">Points Center</p>
        <h2>积分中心</h2>
      </div>
      <el-button type="primary" :disabled="todaySigned" @click="checkIn">{{ todaySigned ? '今日已签到' : '每日签到' }}</el-button>
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
        <el-tag effect="plain">MVP 规则</el-tag>
      </div>
      <el-table :data="tasks" empty-text="暂无任务">
        <el-table-column label="任务" prop="name" min-width="180" />
        <el-table-column label="说明" prop="description" min-width="260" />
        <el-table-column label="奖励" width="120">
          <template #default="{ row }">+{{ row.rewardPoints }} 积分</template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="row.completed ? 'success' : 'info'" effect="plain">{{ row.completed ? '已完成' : '待完成' }}</el-tag>
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
import { onMounted, ref } from 'vue';
import { getPointsDashboard, signIn } from '@/request/points.js';

const points = ref({});
const tasks = ref([]);
const logs = ref([]);
const todaySigned = ref(false);

const loadDashboard = async () => {
  const data = await getPointsDashboard();
  points.value = data.account || {};
  tasks.value = data.tasks || [];
  logs.value = data.logs || [];
  todaySigned.value = !!data.todaySigned;
};

const checkIn = async () => {
  const data = await signIn();
  ElMessage.success(`签到成功，获得 ${data.rewardPoints} 积分`);
  await loadDashboard();
};

onMounted(loadDashboard);
</script>

<style scoped>
.page {
  padding: 24px;
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
  color: #667085;
  font-size: 13px;
}

h2,
h3 {
  margin: 0;
  color: #1f2937;
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
  color: #667085;
  margin-bottom: 10px;
}

.summary-card strong {
  color: #1f2937;
  font-size: 30px;
}

.panel {
  padding: 20px;
  margin-bottom: 18px;
}

@media (max-width: 780px) {
  .summary-grid {
    grid-template-columns: 1fr;
  }
}
</style>
