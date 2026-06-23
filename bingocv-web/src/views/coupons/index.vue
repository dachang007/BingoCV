<template>
  <div class="coupon-page">
    <header class="page-header">
      <div>
        <p class="eyebrow">权益中心</p>
        <h2>我的优惠券</h2>
      </div>
      <div class="claim-box">
        <el-input v-model="couponCode" placeholder="输入优惠券码" clearable @keyup.enter="claim" />
        <el-button type="primary" :loading="claiming" @click="claim">领取</el-button>
      </div>
    </header>

    <el-tabs v-model="activeTab" class="coupon-tabs">
      <el-tab-pane label="可使用" name="unused">
        <div v-if="coupons.unused?.length" class="coupon-grid">
          <section v-for="item in coupons.unused" :key="item.id" class="coupon-card">
            <div class="coupon-main">
              <strong>{{ formatCoupon(item) }}</strong>
              <span>{{ item.name }}</span>
            </div>
            <div class="coupon-meta">
              <span>券码：{{ item.couponCode }}</span>
              <span>门槛：满 {{ item.minAmount || 0 }} 元可用</span>
              <span>有效期：{{ item.expireTime || '长期有效' }}</span>
            </div>
          </section>
        </div>
        <el-empty v-else description="暂无可用优惠券" />
      </el-tab-pane>

      <el-tab-pane v-for="tab in otherTabs" :key="tab.name" :label="tab.label" :name="tab.name">
        <div v-if="coupons[tab.name]?.length" class="coupon-grid">
          <section v-for="item in coupons[tab.name]" :key="item.id" class="coupon-card muted">
            <div class="coupon-main">
              <strong>{{ formatCoupon(item) }}</strong>
              <span>{{ item.name }}</span>
            </div>
            <div class="coupon-meta">
              <span>券码：{{ item.couponCode }}</span>
              <span>门槛：满 {{ item.minAmount || 0 }} 元可用</span>
              <span>有效期：{{ item.expireTime || '长期有效' }}</span>
            </div>
          </section>
        </div>
        <el-empty v-else description="暂无优惠券" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { claimCoupon, getMyCoupons } from '@/request/coupon.js'

const activeTab = ref('unused')
const couponCode = ref('')
const claiming = ref(false)
const coupons = ref({})
const otherTabs = [
  { name: 'used', label: '已使用' },
  { name: 'expired', label: '已过期' },
  { name: 'disabled', label: '已禁用' }
]

function formatCoupon(item) {
  if (item.couponType === 'RATE') return `${Number(item.discountRate || 0) * 10} 折`
  return `减 ${item.discountAmount || 0} 元`
}

async function loadCoupons() {
  coupons.value = await getMyCoupons()
}

async function claim() {
  if (!couponCode.value) {
    ElMessage.warning('请输入优惠券码')
    return
  }
  claiming.value = true
  try {
    await claimCoupon(couponCode.value)
    ElMessage.success('优惠券领取成功')
    couponCode.value = ''
    await loadCoupons()
  } finally {
    claiming.value = false
  }
}

onMounted(loadCoupons)
</script>

<style scoped>
.coupon-page {
  min-height: 100%;
  padding: 24px;
  background: var(--el-bg-color);
  color: var(--el-text-color-primary);
}

.page-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
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

.claim-box {
  display: grid;
  grid-template-columns: minmax(220px, 320px) auto;
  gap: 10px;
}

.coupon-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.coupon-card {
  display: grid;
  gap: 14px;
  padding: 18px;
  border: 1px solid var(--light-border);
  border-radius: 8px;
  background: var(--el-bg-color);
}

.coupon-card.muted {
  opacity: 0.68;
}

.coupon-main {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: baseline;
}

.coupon-main strong {
  color: var(--el-color-primary);
  font-size: 26px;
}

.coupon-main span,
.coupon-meta {
  color: var(--secondary-text-color);
}

.coupon-meta {
  display: grid;
  gap: 6px;
  font-size: 13px;
}

@media (max-width: 1000px) {
  .coupon-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 720px) {
  .page-header {
    align-items: stretch;
    flex-direction: column;
  }

  .claim-box,
  .coupon-grid {
    grid-template-columns: 1fr;
  }
}
</style>
