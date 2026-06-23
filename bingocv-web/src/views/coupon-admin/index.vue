<template>
  <div class="coupon-admin-page">
    <header class="page-header">
      <div>
        <p class="eyebrow">营销权益</p>
        <h2>优惠券管理</h2>
      </div>
      <el-button type="primary" @click="openCreate">创建优惠券</el-button>
    </header>

    <div class="toolbar">
      <el-input v-model="params.keyword" clearable placeholder="搜索券码、名称或备注" @keyup.enter="search" @clear="search" />
      <el-select v-model="params.status" clearable placeholder="状态" @change="search">
        <el-option label="待使用" value="UNUSED" />
        <el-option label="已使用" value="USED" />
        <el-option label="已禁用" value="DISABLED" />
      </el-select>
      <el-button :loading="loading" @click="search">查询</el-button>
    </div>

    <el-table :data="coupons" v-loading="loading" class="coupon-table" empty-text="暂无优惠券">
      <el-table-column prop="couponCode" label="券码" min-width="170" show-overflow-tooltip />
      <el-table-column prop="name" label="名称" min-width="160" show-overflow-tooltip />
      <el-table-column label="优惠" width="120" align="center">
        <template #default="{ row }">{{ formatCoupon(row) }}</template>
      </el-table-column>
      <el-table-column prop="minAmount" label="门槛" width="100" align="right">
        <template #default="{ row }">满 {{ row.minAmount || 0 }}</template>
      </el-table-column>
      <el-table-column prop="targetUserId" label="指定用户" width="130" align="center">
        <template #default="{ row }">{{ row.targetUserId || '未领取' }}</template>
      </el-table-column>
      <el-table-column prop="expireTime" label="过期时间" min-width="170">
        <template #default="{ row }">{{ row.expireTime || '长期有效' }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" effect="plain">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="130" align="center">
        <template #default="{ row }">
          <el-button
            v-if="row.status !== 'USED'"
            text
            :type="row.status === 'DISABLED' ? 'success' : 'danger'"
            @click="toggleStatus(row)"
          >
            {{ row.status === 'DISABLED' ? '启用' : '禁用' }}
          </el-button>
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
        @current-change="loadCoupons"
      />
    </div>

    <el-dialog v-model="dialogVisible" title="创建优惠券" width="560px">
      <el-form label-position="top">
        <el-form-item label="优惠券名称">
          <el-input v-model="form.name" maxlength="120" placeholder="例如：开源用户专属优惠" />
        </el-form-item>
        <el-form-item label="优惠类型">
          <el-segmented v-model="form.couponType" :options="typeOptions" />
        </el-form-item>
        <el-form-item v-if="form.couponType === 'AMOUNT'" label="优惠金额">
          <el-input-number v-model="form.discountAmount" :min="0.01" :precision="2" :step="1" />
        </el-form-item>
        <el-form-item v-else label="折扣比例">
          <el-input-number v-model="form.discountRate" :min="0.01" :max="0.99" :precision="2" :step="0.01" />
          <span class="field-help">例如 0.8 表示八折</span>
        </el-form-item>
        <el-form-item label="最低使用金额">
          <el-input-number v-model="form.minAmount" :min="0" :precision="2" :step="10" />
        </el-form-item>
        <el-form-item label="指定用户ID">
          <el-input-number v-model="form.targetUserId" :min="1" :controls="false" placeholder="不填则生成可领取通用券" />
        </el-form-item>
        <el-form-item label="生成数量">
          <el-input-number v-model="form.quantity" :min="1" :max="200" />
        </el-form-item>
        <el-form-item label="过期时间">
          <el-date-picker v-model="form.expireTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" placeholder="长期有效" clearable />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" maxlength="255" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveCoupons">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { createCoupons, getAdminCoupons, updateCouponStatus } from '@/request/coupon.js'

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const coupons = ref([])
const total = ref(0)
const params = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  status: ''
})
const form = reactive({
  name: '',
  couponType: 'AMOUNT',
  discountAmount: 10,
  discountRate: 0.8,
  minAmount: 0,
  targetUserId: undefined,
  quantity: 1,
  expireTime: '',
  remark: ''
})
const typeOptions = [
  { label: '固定金额', value: 'AMOUNT' },
  { label: '折扣比例', value: 'RATE' }
]

function formatCoupon(row) {
  if (row.couponType === 'RATE') return `${Number(row.discountRate || 0) * 10} 折`
  return `减 ${row.discountAmount || 0} 元`
}

function statusLabel(status) {
  return ({ UNUSED: '待使用', USED: '已使用', DISABLED: '已禁用' })[status] || status
}

function statusType(status) {
  return ({ UNUSED: 'success', USED: 'info', DISABLED: 'danger' })[status] || 'info'
}

async function loadCoupons() {
  loading.value = true
  try {
    const data = await getAdminCoupons({
      pageNum: params.pageNum,
      pageSize: params.pageSize,
      keyword: params.keyword || undefined,
      status: params.status || undefined
    })
    coupons.value = data.list || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function search() {
  params.pageNum = 1
  loadCoupons()
}

function handleSizeChange(size) {
  params.pageSize = size
  params.pageNum = 1
  loadCoupons()
}

function openCreate() {
  dialogVisible.value = true
}

async function saveCoupons() {
  saving.value = true
  try {
    await createCoupons({
      ...form,
      targetUserId: form.targetUserId || null,
      expireTime: form.expireTime || null
    })
    ElMessage.success('优惠券创建成功')
    dialogVisible.value = false
    await loadCoupons()
  } finally {
    saving.value = false
  }
}

async function toggleStatus(row) {
  const nextStatus = row.status === 'DISABLED' ? 'UNUSED' : 'DISABLED'
  await updateCouponStatus(row.id, nextStatus)
  ElMessage.success(nextStatus === 'DISABLED' ? '优惠券已禁用' : '优惠券已启用')
  await loadCoupons()
}

onMounted(loadCoupons)
</script>

<style scoped>
.coupon-admin-page {
  min-height: 100%;
  padding: 24px;
  background: var(--el-bg-color);
  color: var(--el-text-color-primary);
}

.page-header,
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.page-header {
  margin-bottom: 18px;
}

.toolbar {
  justify-content: flex-start;
  margin-bottom: 16px;
}

.toolbar .el-input {
  max-width: 360px;
}

.toolbar .el-select {
  width: 140px;
}

.eyebrow {
  margin: 0 0 4px;
  color: var(--secondary-text-color);
  font-size: 13px;
}

h2 {
  margin: 0;
}

.coupon-table {
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
  .page-header,
  .toolbar {
    align-items: stretch;
    flex-direction: column;
  }

  .toolbar .el-input,
  .toolbar .el-select {
    width: 100%;
    max-width: none;
  }
}
</style>
