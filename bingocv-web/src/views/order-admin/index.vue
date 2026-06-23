<template>
  <div class="order-admin-page">
    <header class="page-header">
      <div>
        <p class="eyebrow">交易管理</p>
        <h2>订单管理</h2>
      </div>
      <el-button :loading="loading" @click="loadOrders">刷新</el-button>
    </header>

    <div class="toolbar">
      <el-input v-model="params.keyword" clearable placeholder="搜索订单号/交易流水/备注" @keyup.enter="search" @clear="search" />
      <el-input-number v-model="params.userId" :controls="false" clearable placeholder="用户ID" />
      <el-select v-model="params.orderType" clearable placeholder="订单类型" @change="search">
        <el-option label="积分充值" value="POINTS" />
        <el-option label="模板购买" value="TEMPLATE" />
      </el-select>
      <el-select v-model="params.status" clearable placeholder="订单状态" @change="search">
        <el-option label="待支付" value="PENDING" />
        <el-option label="已支付" value="PAID" />
        <el-option label="已关闭" value="CLOSED" />
        <el-option label="已退款" value="REFUNDED" />
      </el-select>
      <el-select v-model="params.payChannel" clearable placeholder="支付渠道" @change="search">
        <el-option label="模拟支付" value="MOCK" />
        <el-option label="支付宝" value="ALIPAY" />
        <el-option label="微信支付" value="WECHAT" />
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

    <el-table :data="orders" v-loading="loading" class="order-table" empty-text="暂无订单">
      <el-table-column prop="orderNo" label="订单号" min-width="190" show-overflow-tooltip />
      <el-table-column prop="userId" label="用户ID" width="110" align="center" />
      <el-table-column label="类型" width="110" align="center">
        <template #default="{ row }">{{ typeLabel(row.orderType) }}</template>
      </el-table-column>
      <el-table-column prop="amount" label="实付" width="90" align="right" />
      <el-table-column prop="discountAmount" label="优惠" width="90" align="right" />
      <el-table-column prop="points" label="积分" width="90" align="right" />
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="statusType(row.payStatus)" effect="plain">{{ statusLabel(row.payStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="渠道" width="100" align="center">
        <template #default="{ row }">{{ channelLabel(row.payChannel) }}</template>
      </el-table-column>
      <el-table-column prop="paidTime" label="支付时间" width="170">
        <template #default="{ row }">{{ row.paidTime || '-' }}</template>
      </el-table-column>
      <el-table-column prop="refundTime" label="退款时间" width="170">
        <template #default="{ row }">{{ row.refundTime || '-' }}</template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column label="操作" width="240" fixed="right" align="center">
        <template #default="{ row }">
          <el-button text type="primary" @click="openDetail(row)">详情</el-button>
          <el-button v-if="row.payStatus === 'PENDING'" text type="warning" @click="openAction(row, 'close')">关闭</el-button>
          <el-button v-if="row.payStatus === 'PAID'" text type="danger" @click="openAction(row, 'refund')">退款</el-button>
          <el-button text @click="openAction(row, 'remark')">备注</el-button>
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
        @current-change="loadOrders"
      />
    </div>

    <el-dialog v-model="detailVisible" title="订单详情" width="620px">
      <el-descriptions v-if="currentOrder" :column="1" border>
        <el-descriptions-item label="订单号">{{ currentOrder.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ currentOrder.userId }}</el-descriptions-item>
        <el-descriptions-item label="订单类型">{{ typeLabel(currentOrder.orderType) }}</el-descriptions-item>
        <el-descriptions-item label="订单状态">
          <el-tag :type="statusType(currentOrder.payStatus)" effect="plain">{{ statusLabel(currentOrder.payStatus) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="支付渠道">{{ channelLabel(currentOrder.payChannel) }}</el-descriptions-item>
        <el-descriptions-item label="原价">￥{{ currentOrder.originalAmount || '0.00' }}</el-descriptions-item>
        <el-descriptions-item label="优惠">￥{{ currentOrder.discountAmount || '0.00' }}</el-descriptions-item>
        <el-descriptions-item label="实付">￥{{ currentOrder.amount || '0.00' }}</el-descriptions-item>
        <el-descriptions-item label="交易流水">{{ currentOrder.transactionId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="后台备注">
          <pre class="remark-text">{{ currentOrder.adminRemark || '暂无备注' }}</pre>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="actionVisible" :title="actionTitle" width="520px">
      <el-form label-position="top">
        <el-form-item v-if="actionType === 'refund'" label="退款金额">
          <el-input-number v-model="actionForm.refundAmount" :min="0.01" :precision="2" :step="1" />
          <span class="field-help">留空时按订单实付金额退款</span>
        </el-form-item>
        <el-form-item :label="actionType === 'remark' ? '备注内容' : '处理原因'">
          <el-input
            v-model="actionForm.text"
            type="textarea"
            :rows="4"
            maxlength="300"
            show-word-limit
            placeholder="请输入处理说明"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="actionVisible = false">取消</el-button>
        <el-button type="primary" :loading="actionLoading" @click="submitAction">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { adminCloseOrder, adminRefundOrder, getAdminOrders, updateAdminOrderRemark } from '@/request/order.js'

const loading = ref(false)
const actionLoading = ref(false)
const orders = ref([])
const total = ref(0)
const dateRange = ref([])
const detailVisible = ref(false)
const actionVisible = ref(false)
const actionType = ref('')
const currentOrder = ref(null)
const actionForm = reactive({ text: '', refundAmount: undefined })
const params = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  userId: undefined,
  orderType: '',
  status: '',
  payChannel: ''
})

const actionTitle = computed(() => ({
  close: '关闭订单',
  refund: '订单退款',
  remark: '追加备注'
})[actionType.value] || '订单处理')

function typeLabel(type) {
  return ({ POINTS: '积分充值', TEMPLATE: '模板购买' })[type] || type || '-'
}

function statusLabel(status) {
  return ({ PENDING: '待支付', PAID: '已支付', CLOSED: '已关闭', REFUNDED: '已退款' })[status] || status || '-'
}

function statusType(status) {
  return ({ PENDING: 'warning', PAID: 'success', CLOSED: 'info', REFUNDED: 'danger' })[status] || 'info'
}

function channelLabel(channel) {
  return ({ MOCK: '模拟支付', ALIPAY: '支付宝', WECHAT: '微信支付' })[channel] || channel || '-'
}

async function loadOrders() {
  loading.value = true
  try {
    const data = await getAdminOrders({
      pageNum: params.pageNum,
      pageSize: params.pageSize,
      keyword: params.keyword || undefined,
      userId: params.userId || undefined,
      orderType: params.orderType || undefined,
      status: params.status || undefined,
      payChannel: params.payChannel || undefined,
      startTime: dateRange.value?.[0],
      endTime: dateRange.value?.[1]
    })
    orders.value = data.list || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function search() {
  params.pageNum = 1
  loadOrders()
}

function resetSearch() {
  params.keyword = ''
  params.userId = undefined
  params.orderType = ''
  params.status = ''
  params.payChannel = ''
  dateRange.value = []
  search()
}

function handleSizeChange(size) {
  params.pageSize = size
  params.pageNum = 1
  loadOrders()
}

function openDetail(row) {
  currentOrder.value = row
  detailVisible.value = true
}

function openAction(row, type) {
  currentOrder.value = row
  actionType.value = type
  actionForm.text = ''
  actionForm.refundAmount = type === 'refund' ? Number(row.amount || 0) : undefined
  actionVisible.value = true
}

async function submitAction() {
  if (!currentOrder.value) return
  actionLoading.value = true
  try {
    if (actionType.value === 'close') {
      await adminCloseOrder(currentOrder.value.id, { reason: actionForm.text })
      ElMessage.success('订单已关闭')
    } else if (actionType.value === 'refund') {
      await adminRefundOrder(currentOrder.value.id, {
        reason: actionForm.text,
        refundAmount: actionForm.refundAmount
      })
      ElMessage.success('退款记录已生成')
    } else {
      await updateAdminOrderRemark(currentOrder.value.id, { remark: actionForm.text })
      ElMessage.success('备注已保存')
    }
    actionVisible.value = false
    await loadOrders()
  } finally {
    actionLoading.value = false
  }
}

onMounted(loadOrders)
</script>

<style scoped>
.order-admin-page {
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
  width: 250px;
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

.order-table {
  border: 1px solid var(--light-border);
  border-radius: 8px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 18px;
}

.remark-text {
  margin: 0;
  white-space: pre-wrap;
  color: var(--el-text-color-regular);
  font-family: inherit;
}

.field-help {
  margin-left: 12px;
  color: var(--secondary-text-color);
  font-size: 13px;
}

@media (max-width: 720px) {
  .order-admin-page {
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
