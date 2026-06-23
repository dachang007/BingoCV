<template>
  <div class="refund-admin-page">
    <header class="page-header">
      <div>
        <p class="eyebrow">交易管理</p>
        <h2>退款记录</h2>
      </div>
      <el-button :loading="loading" @click="loadRefunds">刷新</el-button>
    </header>

    <div class="toolbar">
      <el-input v-model="params.keyword" clearable placeholder="搜索订单号/退款流水/原因" @keyup.enter="search" @clear="search" />
      <el-input-number v-model="params.userId" :controls="false" clearable placeholder="用户ID" />
      <el-select v-model="params.status" clearable placeholder="退款状态" @change="search">
        <el-option label="处理中" value="PENDING" />
        <el-option label="已成功" value="SUCCESS" />
        <el-option label="已失败" value="FAILED" />
      </el-select>
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        value-format="YYYY-MM-DD"
        @change="search"
      />
      <el-button type="primary" :loading="loading" @click="search">查询</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>

    <el-table :data="refunds" v-loading="loading" class="refund-table" empty-text="暂无退款记录">
      <el-table-column prop="refundNo" label="退款流水" min-width="170" show-overflow-tooltip />
      <el-table-column prop="orderNo" label="订单号" min-width="190" show-overflow-tooltip />
      <el-table-column prop="userId" label="用户ID" width="110" align="center" />
      <el-table-column prop="refundAmount" label="退款金额" width="110" align="right">
        <template #default="{ row }">￥{{ row.refundAmount || '0.00' }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="statusType(row.refundStatus)" effect="plain">{{ statusLabel(row.refundStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="operatorId" label="操作人" width="110" align="center">
        <template #default="{ row }">{{ row.operatorId || '-' }}</template>
      </el-table-column>
      <el-table-column prop="reason" label="原因" min-width="180" show-overflow-tooltip>
        <template #default="{ row }">{{ row.reason || '-' }}</template>
      </el-table-column>
      <el-table-column prop="refundTime" label="退款时间" width="170">
        <template #default="{ row }">{{ row.refundTime || '-' }}</template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column label="操作" width="90" fixed="right" align="center">
        <template #default="{ row }">
          <el-button text type="primary" @click="openDetail(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination" v-if="total > 0">
      <el-pagination
        v-model:current-page="params.pageNum"
        v-model:page-size="params.pageSize"
        :page-sizes="[10, 20, 50]"
        background
        layout="total, sizes, prev, pager, next"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="loadRefunds"
      />
    </div>

    <el-dialog v-model="detailVisible" title="退款详情" width="560px">
      <el-descriptions v-if="currentRefund" :column="1" border>
        <el-descriptions-item label="退款流水">{{ currentRefund.refundNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="订单号">{{ currentRefund.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="订单ID">{{ currentRefund.orderId }}</el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ currentRefund.userId }}</el-descriptions-item>
        <el-descriptions-item label="退款金额">￥{{ currentRefund.refundAmount || '0.00' }}</el-descriptions-item>
        <el-descriptions-item label="退款状态">
          <el-tag :type="statusType(currentRefund.refundStatus)" effect="plain">{{ statusLabel(currentRefund.refundStatus) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="操作人">{{ currentRefund.operatorId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="退款时间">{{ currentRefund.refundTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="退款原因">{{ currentRefund.reason || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { getAdminRefunds } from '@/request/order.js'

const loading = ref(false)
const refunds = ref([])
const total = ref(0)
const dateRange = ref([])
const detailVisible = ref(false)
const currentRefund = ref(null)
const params = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  userId: undefined,
  status: ''
})

function statusLabel(status) {
  return ({ PENDING: '处理中', SUCCESS: '已成功', FAILED: '已失败' })[status] || status || '-'
}

function statusType(status) {
  return ({ PENDING: 'warning', SUCCESS: 'success', FAILED: 'danger' })[status] || 'info'
}

async function loadRefunds() {
  loading.value = true
  try {
    const data = await getAdminRefunds({
      pageNum: params.pageNum,
      pageSize: params.pageSize,
      keyword: params.keyword || undefined,
      userId: params.userId || undefined,
      status: params.status || undefined,
      startTime: dateRange.value?.[0],
      endTime: dateRange.value?.[1]
    })
    refunds.value = data.list || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function search() {
  params.pageNum = 1
  loadRefunds()
}

function resetSearch() {
  params.keyword = ''
  params.userId = undefined
  params.status = ''
  dateRange.value = []
  search()
}

function handleSizeChange(size) {
  params.pageSize = size
  params.pageNum = 1
  loadRefunds()
}

function openDetail(row) {
  currentRefund.value = row
  detailVisible.value = true
}

onMounted(loadRefunds)
</script>

<style scoped>
.refund-admin-page {
  min-height: 100%;
  padding: 24px;
  background: var(--el-bg-color);
  color: var(--el-text-color-primary);
}

.page-header,
.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-header {
  justify-content: space-between;
  margin-bottom: 18px;
}

.toolbar {
  flex-wrap: wrap;
  margin-bottom: 16px;
}

.toolbar .el-input {
  width: 260px;
}

.toolbar .el-select {
  width: 130px;
}

.toolbar :deep(.el-input-number) {
  width: 120px;
}

.eyebrow {
  margin: 0 0 4px;
  color: var(--secondary-text-color);
  font-size: 13px;
}

h2 {
  margin: 0;
}

.refund-table {
  border: 1px solid var(--light-border);
  border-radius: 8px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 18px;
}

@media (max-width: 720px) {
  .refund-admin-page {
    padding: 16px;
  }

  .page-header,
  .toolbar {
    align-items: stretch;
    flex-direction: column;
  }

  .toolbar .el-input,
  .toolbar .el-select,
  .toolbar :deep(.el-input-number),
  .toolbar :deep(.el-date-editor) {
    width: 100%;
  }
}
</style>
