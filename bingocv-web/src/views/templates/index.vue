<template>
  <div class="page">
    <div class="page-header">
      <div>
        <p class="eyebrow">模板市场</p>
        <h2>简历模板</h2>
      </div>
      <el-segmented v-model="activeCategory" :options="categories" />
    </div>

    <div class="template-grid">
      <article v-for="item in filteredTemplates" :key="item.id" class="template-card">
        <div class="cover" :class="coverClass(item)">
          <div class="cover-line wide"></div>
          <div class="cover-line"></div>
          <div class="cover-block"></div>
        </div>
        <div class="card-body">
          <div class="card-title">
            <h3>{{ item.name }}</h3>
            <el-tag :type="tagType(item.templateType)" effect="plain">
              {{ typeText(item.templateType) }}
            </el-tag>
          </div>
          <p>{{ item.industry }} · {{ item.description }}</p>
          <div class="card-footer">
            <span>{{ costText(item) }}</span>
            <el-button type="primary" plain :loading="loadingId === item.id" @click="useTemplate(item)">
              {{ actionText(item) }}
            </el-button>
          </div>
        </div>
      </article>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { acquireTemplate, activateTemplate, getTemplateMarket } from '@/request/template.js'
import { createOrder } from '@/request/order.js'

const router = useRouter()
const categories = ['全部', 'IT互联网', '金融财会', '教育培训', '医疗健康', '设计创意', '通用简约']
const activeCategory = ref('全部')
const templates = ref([])
const loadingId = ref(null)

const filteredTemplates = computed(() => templates.value)

async function loadTemplates() {
  templates.value = await getTemplateMarket({ industry: activeCategory.value })
}

async function useTemplate(item) {
  if (item.active) return
  loadingId.value = item.id
  try {
    if (!item.owned && item.templateType === 'PAID') {
      await createOrder({ orderType: 'TEMPLATE', relatedId: item.id, payChannel: 'MOCK' })
      ElMessage.success('模板购买订单已创建，请前往我的订单完成支付')
      await router.push('/admin/orders')
      return
    }
    if (!item.owned) {
      await acquireTemplate(item.id)
    }
    await activateTemplate(item.id)
    ElMessage.success('模板已启用')
    await loadTemplates()
  } finally {
    loadingId.value = null
  }
}

function actionText(item) {
  if (item.active) return '使用中'
  if (item.owned) return '启用模板'
  if (item.templateType === 'POINTS') return '积分兑换'
  if (item.templateType === 'PAID') return '付费购买'
  return '免费领取'
}

const typeText = (type) => ({ FREE: '免费', POINTS: '积分', PAID: '付费' })[type] || type
const tagType = (type) => type === 'FREE' ? 'success' : type === 'POINTS' ? 'warning' : 'danger'
const costText = (item) => {
  if (item.templateType === 'FREE') return '0 积分'
  if (item.templateType === 'POINTS') return `${item.pointsCost || 0} 积分`
  return `￥${item.price || 0}`
}
const coverClass = (item) => {
  const map = {
    'IT互联网': 'green',
    '金融财会': 'gold',
    '教育培训': 'purple',
    '医疗健康': 'teal',
    '设计创意': 'rose',
    '通用简约': 'gray'
  }
  return map[item.industry] || 'blue'
}

watch(activeCategory, loadTemplates)
onMounted(loadTemplates)
</script>

<style scoped>
.page {
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

.template-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 18px;
}

.template-card {
  overflow: hidden;
  background: var(--el-bg-color);
  border: 1px solid var(--light-border);
  border-radius: 8px;
}

.cover {
  height: 170px;
  padding: 24px;
  background: #eef6f2;
}

.cover.green { background: #e8f6ef; }
.cover.blue { background: #eaf2ff; }
.cover.gold { background: #fff6df; }
.cover.purple { background: #f1edff; }
.cover.teal { background: #e6f7f7; }
.cover.rose { background: #fff0f3; }
.cover.gray { background: #f4f6f8; }

:global(.dark) .cover.green { background: #173126; }
:global(.dark) .cover.blue { background: #172842; }
:global(.dark) .cover.gold { background: #382d16; }
:global(.dark) .cover.purple { background: #251f3a; }
:global(.dark) .cover.teal { background: #143334; }
:global(.dark) .cover.rose { background: #3a1c25; }
:global(.dark) .cover.gray { background: #25282c; }

.cover-line,
.cover-block {
  background: rgba(31, 41, 55, 0.72);
  border-radius: 4px;
}

.cover-line {
  width: 58%;
  height: 10px;
  margin-bottom: 12px;
}

.cover-line.wide {
  width: 82%;
  height: 16px;
}

.cover-block {
  width: 100%;
  height: 72px;
  margin-top: 22px;
  opacity: 0.24;
}

.card-body {
  padding: 16px;
}

.card-title,
.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.card-body p {
  min-height: 44px;
  color: var(--regular-text-color);
  line-height: 1.6;
}

.card-footer span {
  font-weight: 700;
  color: var(--el-text-color-primary);
}

@media (max-width: 720px) {
  .page-header {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
