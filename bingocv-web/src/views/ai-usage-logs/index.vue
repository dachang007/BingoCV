<template>
  <div class="ai-logs-page">
    <header class="page-header">
      <div>
        <p class="eyebrow">AI 审计</p>
        <h2>AI 使用记录</h2>
      </div>
      <div class="toolbar">
        <el-select v-model="params.actionType" clearable placeholder="动作类型" @change="search" @clear="search">
          <el-option label="简历分析" value="ANALYZE" />
          <el-option label="单段润色" value="POLISH" />
        </el-select>
        <el-button type="primary" :loading="loading" @click="search">查询</el-button>
      </div>
    </header>

    <el-table :data="logs" v-loading="loading" class="logs-table" empty-text="暂无 AI 使用记录">
      <el-table-column prop="userId" label="用户ID" width="120" />
      <el-table-column prop="actionType" label="动作" width="120" align="center">
        <template #default="{ row }">
          <el-tag effect="plain">{{ actionLabel(row.actionType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="provider" label="提供方" width="160" show-overflow-tooltip />
      <el-table-column prop="model" label="模型" min-width="170" show-overflow-tooltip />
      <el-table-column prop="success" label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.success === 1 ? 'success' : 'warning'" effect="plain">
            {{ row.success === 1 ? '模型成功' : '本地兜底' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="fallbackUsed" label="回退" width="90" align="center">
        <template #default="{ row }">{{ row.fallbackUsed === 1 ? '是' : '否' }}</template>
      </el-table-column>
      <el-table-column prop="costMs" label="耗时" width="110" align="right">
        <template #default="{ row }">{{ row.costMs || 0 }} ms</template>
      </el-table-column>
      <el-table-column prop="createTime" label="时间" width="170" />
    </el-table>

    <div class="pagination" v-if="total > 0">
      <el-pagination
        v-model:current-page="params.pageNum"
        v-model:page-size="params.pageSize"
        :page-sizes="[10, 20, 30, 50]"
        background
        layout="total, sizes, prev, pager, next"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="loadLogs"
      />
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { getAiUsageLogs } from '@/request/admin.js'

const loading = ref(false)
const logs = ref([])
const total = ref(0)
const params = reactive({
  pageNum: 1,
  pageSize: 10,
  actionType: ''
})

async function loadLogs() {
  loading.value = true
  try {
    const data = await getAiUsageLogs({
      pageNum: params.pageNum,
      pageSize: params.pageSize,
      actionType: params.actionType || undefined
    })
    logs.value = data.list || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function search() {
  params.pageNum = 1
  loadLogs()
}

function handleSizeChange(size) {
  params.pageSize = size
  params.pageNum = 1
  loadLogs()
}

function actionLabel(type) {
  return ({ ANALYZE: '简历分析', POLISH: '单段润色' })[type] || type || '-'
}

onMounted(loadLogs)
</script>

<style scoped>
.ai-logs-page {
  min-height: 100%;
  padding: 24px;
  background: var(--el-bg-color);
  color: var(--el-text-color-primary);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 16px;
  margin-bottom: 18px;
}

.eyebrow {
  margin: 0 0 4px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

h2 {
  margin: 0;
}

.toolbar {
  display: grid;
  grid-template-columns: 180px auto;
  gap: 10px;
}

.logs-table {
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;
  background: var(--el-bg-color-overlay);
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 18px;
}

@media (max-width: 720px) {
  .page-header {
    align-items: stretch;
    flex-direction: column;
  }

  .toolbar {
    grid-template-columns: 1fr;
  }
}
</style>
