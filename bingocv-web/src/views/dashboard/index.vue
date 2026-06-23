<template>
  <div class="dashboard-page">
    <header class="page-header">
      <div>
        <p class="eyebrow">运营概览</p>
        <h2>数据看板</h2>
      </div>
      <el-button :loading="loading" @click="loadDashboard">刷新数据</el-button>
    </header>

    <section class="metric-grid">
      <div v-for="item in metrics" :key="item.key" class="metric-card">
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
      </div>
    </section>

    <section class="chart-grid">
      <div class="panel chart-panel">
        <div class="section-header">
          <h3>用户增长曲线</h3>
          <span>最近 14 天</span>
        </div>
        <div ref="userChartRef" class="chart"></div>
      </div>
      <div class="panel chart-panel">
        <div class="section-header">
          <h3>分享访问曲线</h3>
          <span>最近 14 天</span>
        </div>
        <div ref="shareChartRef" class="chart"></div>
      </div>
      <div class="panel chart-panel chart-wide">
        <div class="section-header">
          <h3>后台操作趋势</h3>
          <span>最近 14 天</span>
        </div>
        <div ref="operationChartRef" class="chart"></div>
      </div>
    </section>

    <div class="content-grid">
      <section class="panel">
        <div class="section-header">
          <h3>最近操作</h3>
          <el-button text type="primary" @click="router.push('/admin/operation-logs')">查看全部</el-button>
        </div>
        <el-table :data="dashboard.recentOperations || []" v-loading="loading" empty-text="暂无操作日志">
          <el-table-column prop="username" label="用户" width="120">
            <template #default="{ row }">{{ row.username || '游客' }}</template>
          </el-table-column>
          <el-table-column prop="requestUri" label="请求路径" min-width="220" show-overflow-tooltip />
          <el-table-column prop="status" label="状态" width="90" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'danger'" effect="plain">
                {{ row.status === 1 ? '成功' : '失败' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="costMs" label="耗时" width="90" align="right">
            <template #default="{ row }">{{ row.costMs || 0 }} ms</template>
          </el-table-column>
        </el-table>
      </section>

      <section class="panel">
        <div class="section-header">
          <h3>最近分享</h3>
          <el-button text type="primary" @click="router.push('/admin/share')">管理分享</el-button>
        </div>
        <el-table :data="dashboard.recentShares || []" v-loading="loading" empty-text="暂无分享">
          <el-table-column prop="title" label="分享标题" min-width="180" show-overflow-tooltip>
            <template #default="{ row }">{{ row.title || '未命名分享' }}</template>
          </el-table-column>
          <el-table-column prop="shareType" label="类型" width="90" align="center">
            <template #default="{ row }">{{ row.shareType === 'PRIVATE' ? '私密' : '公开' }}</template>
          </el-table-column>
          <el-table-column prop="accessCount" label="访问" width="80" align="right" />
          <el-table-column prop="status" label="状态" width="90" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="plain">
                {{ row.status === 1 ? '启用' : '关闭' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { getAdminDashboard } from '@/request/admin.js'

const router = useRouter()
const loading = ref(false)
const dashboard = ref({})
const userChartRef = ref(null)
const shareChartRef = ref(null)
const operationChartRef = ref(null)
let userChart
let shareChart
let operationChart

const metrics = computed(() => [
  { key: 'userCount', label: '用户总数', value: dashboard.value.userCount || 0 },
  { key: 'profileCount', label: '简历数量', value: dashboard.value.profileCount || 0 },
  { key: 'shareCount', label: '分享数量', value: dashboard.value.shareCount || 0 },
  { key: 'todayShareAccessCount', label: '今日分享访问', value: dashboard.value.todayShareAccessCount || 0 },
  { key: 'todayOperationCount', label: '今日操作日志', value: dashboard.value.todayOperationCount || 0 },
  { key: 'totalEarnedPoints', label: '累计发放积分', value: dashboard.value.totalEarnedPoints || 0 },
  { key: 'templateCount', label: '模板数量', value: dashboard.value.templateCount || 0 },
  { key: 'experienceCount', label: '经历条目', value: (dashboard.value.educationCount || 0) + (dashboard.value.workCount || 0) }
])

async function loadDashboard() {
  loading.value = true
  try {
    dashboard.value = await getAdminDashboard()
    await nextTick()
    renderCharts()
  } finally {
    loading.value = false
  }
}

function renderCharts() {
  const trends = dashboard.value.trends || {}
  const dates = trends.dates || []
  userChart = renderLineChart(userChart, userChartRef.value, '新增用户', dates, trends.userGrowth || [], '#2f80ed')
  shareChart = renderLineChart(shareChart, shareChartRef.value, '分享访问', dates, trends.shareAccess || [], '#13a37f')
  operationChart = renderLineChart(operationChart, operationChartRef.value, '后台操作', dates, trends.operationCount || [], '#f59f00')
}

function renderLineChart(chart, el, name, dates, values, color) {
  if (!el) return chart
  const instance = chart || echarts.init(el)
  const styles = getComputedStyle(document.documentElement)
  const borderColor = styles.getPropertyValue('--light-border').trim() || '#e4e7ed'
  const textColor = styles.getPropertyValue('--secondary-text-color').trim() || '#909399'
  instance.setOption({
    color: [color],
    tooltip: { trigger: 'axis' },
    grid: { left: 34, right: 18, top: 28, bottom: 28 },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates,
      axisLine: { lineStyle: { color: borderColor } },
      axisLabel: { color: textColor }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      splitLine: { lineStyle: { color: borderColor } },
      axisLabel: { color: textColor }
    },
    series: [{
      name,
      type: 'line',
      smooth: true,
      symbolSize: 7,
      data: values,
      areaStyle: { opacity: 0.12 },
      lineStyle: { width: 3 }
    }]
  })
  return instance
}

function resizeCharts() {
  userChart?.resize()
  shareChart?.resize()
  operationChart?.resize()
}

onMounted(() => {
  loadDashboard()
  window.addEventListener('resize', resizeCharts)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeCharts)
  userChart?.dispose()
  shareChart?.dispose()
  operationChart?.dispose()
})
</script>

<style scoped>
.dashboard-page {
  min-height: 100%;
  padding: 24px;
  background: var(--el-bg-color);
  color: var(--el-text-color-primary);
}

.page-header,
.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
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
}

.section-header span {
  color: var(--secondary-text-color);
  font-size: 13px;
}

.metric-grid,
.chart-grid,
.content-grid {
  display: grid;
  gap: 16px;
}

.metric-grid {
  grid-template-columns: repeat(4, minmax(0, 1fr));
  margin-bottom: 18px;
}

.chart-grid,
.content-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
  margin-bottom: 18px;
}

.metric-card,
.panel {
  background: var(--el-bg-color);
  border: 1px solid var(--light-border);
  border-radius: 8px;
}

.metric-card {
  padding: 18px;
}

.metric-card span {
  display: block;
  margin-bottom: 10px;
  color: var(--secondary-text-color);
}

.metric-card strong {
  font-size: 28px;
  color: var(--el-text-color-primary);
}

.panel {
  min-width: 0;
  padding: 18px;
}

.chart-panel {
  min-height: 340px;
}

.chart-wide {
  grid-column: 1 / -1;
}

.chart {
  width: 100%;
  height: 280px;
  margin-top: 10px;
}

.section-header {
  margin-bottom: 12px;
}

@media (max-width: 1100px) {
  .metric-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 860px) {
  .chart-grid,
  .content-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .metric-grid {
    grid-template-columns: 1fr;
  }
}
</style>
