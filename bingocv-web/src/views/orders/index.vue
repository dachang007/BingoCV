<template>
  <div class="orders-page">
    <header class="page-header">
      <div>
        <p class="eyebrow">支付中心</p>
        <h2>我的订单</h2>
      </div>
      <el-button type="primary" @click="rechargeVisible = true">积分充值</el-button>
    </header>

    <el-table :data="orders" v-loading="loading" class="order-table" empty-text="暂无订单">
      <el-table-column prop="orderNo" label="订单号" min-width="190" show-overflow-tooltip />
      <el-table-column label="类型" width="110" align="center">
        <template #default="{ row }">{{ typeLabel(row.orderType) }}</template>
      </el-table-column>
      <el-table-column prop="originalAmount" label="原价" width="90" align="right" />
      <el-table-column prop="discountAmount" label="优惠" width="90" align="right" />
      <el-table-column prop="amount" label="实付" width="90" align="right" />
      <el-table-column prop="points" label="积分" width="90" align="right" />
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="statusType(row.payStatus)" effect="plain">{{ statusLabel(row.payStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="渠道" width="100" align="center">
        <template #default="{ row }">{{ channelLabel(row.payChannel) }}</template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column label="操作" width="220" align="center" fixed="right">
        <template #default="{ row }">
          <el-button text type="primary" @click="openDetail(row)">详情</el-button>
          <template v-if="row.payStatus === 'PENDING'">
            <el-button text type="success" @click="pay(row)">支付</el-button>
            <el-button text type="danger" @click="close(row)">关闭</el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination" v-if="total > 0">
      <el-pagination
        v-model:current-page="params.pageNum"
        v-model:page-size="params.pageSize"
        background
        layout="total, prev, pager, next"
        :total="total"
        @current-change="loadOrders"
      />
    </div>

    <el-dialog v-model="rechargeVisible" title="积分充值" width="480px">
      <el-form label-position="top">
        <el-form-item label="充值积分">
          <el-input-number v-model="rechargeForm.points" :min="10" :max="100000" :step="100" />
          <span class="field-help">10 积分 = 1 元</span>
        </el-form-item>
        <el-form-item label="支付渠道">
          <el-segmented v-model="rechargeForm.payChannel" :options="channelOptions" />
        </el-form-item>
        <el-form-item label="优惠券码">
          <el-input v-model="rechargeForm.couponCode" clearable placeholder="没有可不填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rechargeVisible = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="createRecharge">创建订单</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="订单详情" width="560px">
      <el-descriptions v-if="detail" :column="1" border>
        <el-descriptions-item label="订单号">{{ detail.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="订单类型">{{ typeLabel(detail.orderType) }}</el-descriptions-item>
        <el-descriptions-item label="支付状态">
          <el-tag :type="statusType(detail.payStatus)" effect="plain">{{ statusLabel(detail.payStatus) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="支付渠道">{{ channelLabel(detail.payChannel) }}</el-descriptions-item>
        <el-descriptions-item label="原价">￥{{ detail.originalAmount || '0.00' }}</el-descriptions-item>
        <el-descriptions-item label="优惠">￥{{ detail.discountAmount || '0.00' }}</el-descriptions-item>
        <el-descriptions-item label="实付">￥{{ detail.amount || '0.00' }}</el-descriptions-item>
        <el-descriptions-item label="积分">{{ detail.points || 0 }}</el-descriptions-item>
        <el-descriptions-item label="优惠券ID">{{ detail.couponId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="交易流水">{{ detail.transactionId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="支付时间">{{ detail.paidTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detail.createTime || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { closeOrder, createOrder, getMyOrders, getOrderDetail, payOrder } from '@/request/order.js'

const loading = ref(false)
const creating = ref(false)
const rechargeVisible = ref(false)
const detailVisible = ref(false)
const detail = ref(null)
const orders = ref([])
const total = ref(0)
const params = reactive({ pageNum: 1, pageSize: 10 })
const rechargeForm = reactive({ points: 100, couponCode: '', payChannel: 'MOCK' })
const channelOptions = [
  { label: '模拟支付', value: 'MOCK' },
  { label: '支付宝', value: 'ALIPAY' },
  { label: '微信支付', value: 'WECHAT' }
]

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
    const data = await getMyOrders(params)
    orders.value = data.list || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

async function createRecharge() {
  creating.value = true
  try {
    await createOrder({
      orderType: 'POINTS',
      points: rechargeForm.points,
      couponCode: rechargeForm.couponCode || undefined,
      payChannel: rechargeForm.payChannel
    })
    ElMessage.success('订单已创建，请在列表中完成支付')
    rechargeVisible.value = false
    await loadOrders()
  } finally {
    creating.value = false
  }
}

async function openDetail(row) {
  detail.value = await getOrderDetail(row.id)
  detailVisible.value = true
}

async function pay(row) {
  const channel = row.payChannel || 'MOCK'
  await ElMessageBox.confirm(`确认使用${channelLabel(channel)}完成该订单吗？`, '支付确认', {
    confirmButtonText: '确认支付',
    cancelButtonText: '取消',
    type: 'warning'
  })
  const result = await payOrder(row.id, channel)
  if (result?.paid) {
    ElMessage.success(result.message || '支付成功，权益已发放')
  } else {
    ElMessage.info(result?.message || '支付已发起，请跳转第三方完成支付')
  }
  await loadOrders()
}

async function close(row) {
  await ElMessageBox.confirm('关闭后该订单不能继续支付，确认关闭吗？', '关闭订单', {
    confirmButtonText: '确认关闭',
    cancelButtonText: '取消',
    type: 'warning'
  })
  await closeOrder(row.id)
  ElMessage.success('订单已关闭')
  await loadOrders()
}

onMounted(loadOrders)
</script>

<style scoped>
.orders-page {
  min-height: 100%;
  padding: 24px;
  background: var(--el-bg-color);
  color: var(--el-text-color-primary);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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

.order-table {
  border: 1px solid var(--light-border);
  border-radius: 8px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 18px;
}

.field-help {
  margin-left: 12px;
  color: var(--secondary-text-color);
  font-size: 13px;
}

@media (max-width: 720px) {
  .orders-page {
    padding: 16px;
  }

  .page-header {
    align-items: stretch;
    flex-direction: column;
  }
}
</style>
