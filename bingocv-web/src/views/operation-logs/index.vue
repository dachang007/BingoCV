<template>
  <div class="logs-page">
    <header class="page-header">
      <div>
        <p class="eyebrow">后台审计</p>
        <h2>操作日志</h2>
      </div>
      <div class="toolbar">
        <el-input
          v-model="params.keyword"
          clearable
          placeholder="搜索用户、接口路径或模块"
          @keyup.enter="search"
          @clear="search"
        />
        <el-button type="primary" :loading="loading" @click="search">查询</el-button>
      </div>
    </header>

    <el-table :data="logs" v-loading="loading" class="logs-table" empty-text="暂无操作日志">
      <el-table-column prop="username" label="用户" width="130">
        <template #default="{ row }">{{ row.username || '游客' }}</template>
      </el-table-column>
      <el-table-column prop="module" label="模块" width="170" show-overflow-tooltip />
      <el-table-column prop="requestMethod" label="方法" width="90" align="center">
        <template #default="{ row }">
          <el-tag effect="plain">{{ row.requestMethod }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="requestUri" label="请求路径" min-width="260" show-overflow-tooltip />
      <el-table-column prop="ip" label="IP 地址" width="150" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" effect="plain">
            {{ row.status === 1 ? '成功' : '失败' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="costMs" label="耗时" width="100" align="right">
        <template #default="{ row }">{{ row.costMs || 0 }} ms</template>
      </el-table-column>
      <el-table-column prop="createTime" label="操作时间" width="170" />
      <el-table-column label="详情" width="90" align="center">
        <template #default="{ row }">
          <el-button text type="primary" @click="openDetail(row)">查看</el-button>
        </template>
      </el-table-column>
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

    <el-dialog v-model="detailVisible" title="日志详情" width="620px">
      <div v-if="currentLog" class="detail-list">
        <div><span>用户</span><strong>{{ currentLog.username || '游客' }}</strong></div>
        <div><span>模块</span><strong>{{ currentLog.module || '-' }}</strong></div>
        <div><span>动作</span><strong>{{ currentLog.action || '-' }}</strong></div>
        <div><span>请求</span><strong>{{ currentLog.requestMethod }} {{ currentLog.requestUri }}</strong></div>
        <div><span>IP</span><strong>{{ currentLog.ip || '-' }}</strong></div>
        <div><span>耗时</span><strong>{{ currentLog.costMs || 0 }} ms</strong></div>
        <div><span>错误信息</span><strong>{{ currentLog.errorMsg || '无' }}</strong></div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { getOperationLogs } from '@/request/admin.js'

const loading = ref(false)
const logs = ref([])
const total = ref(0)
const detailVisible = ref(false)
const currentLog = ref(null)
const params = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: ''
})

async function loadLogs() {
  loading.value = true
  try {
    const data = await getOperationLogs({
      pageNum: params.pageNum,
      pageSize: params.pageSize,
      keyword: params.keyword || undefined
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

function openDetail(row) {
  currentLog.value = row
  detailVisible.value = true
}

onMounted(loadLogs)
</script>

<style scoped>
.logs-page {
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
  color: var(--secondary-text-color);
  font-size: 13px;
}

h2 {
  margin: 0;
}

.toolbar {
  display: grid;
  grid-template-columns: minmax(240px, 360px) auto;
  gap: 10px;
}

.logs-table {
  border: 1px solid var(--light-border);
  border-radius: 8px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 18px;
}

.detail-list {
  display: grid;
  gap: 12px;
}

.detail-list div {
  display: grid;
  grid-template-columns: 90px 1fr;
  gap: 14px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--light-border);
}

.detail-list span {
  color: var(--secondary-text-color);
}

.detail-list strong {
  min-width: 0;
  word-break: break-all;
  color: var(--el-text-color-primary);
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
