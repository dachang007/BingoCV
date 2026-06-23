<template>
  <div class="share-page">
    <header class="page-header">
      <div>
        <p class="eyebrow">分享管理</p>
        <h2>我的简历分享</h2>
      </div>
      <el-button type="primary" @click="openCreate">创建分享</el-button>
    </header>

    <el-table :data="shares" v-loading="loading" class="share-table" empty-text="暂无分享">
      <el-table-column label="分享标题" min-width="220">
        <template #default="{ row }">
          <div class="title-cell">
            <strong>{{ row.title || '未命名分享' }}</strong>
            <span>{{ buildShareLink(row) }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="分享类型" width="110" align="center">
        <template #default="{ row }">
          <el-tag :type="row.shareType === 'PRIVATE' ? 'warning' : 'success'" effect="plain">
            {{ row.shareType === 'PRIVATE' ? '私密' : '公开' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="访问次数" width="105" align="center">
        <template #default="{ row }">{{ row.accessCount || 0 }}</template>
      </el-table-column>
      <el-table-column label="访问限制" width="105" align="center">
        <template #default="{ row }">{{ row.accessLimit || '不限' }}</template>
      </el-table-column>
      <el-table-column label="过期时间" min-width="170">
        <template #default="{ row }">{{ row.expireTime || '永不过期' }}</template>
      </el-table-column>
      <el-table-column label="状态" width="95" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="plain">
            {{ row.status === 1 ? '启用中' : '已关闭' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="{ row }">
          <div class="action-group">
            <el-button size="small" @click="copyLink(row)">复制</el-button>
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" @click="openStats(row)">统计</el-button>
            <el-button size="small" type="danger" :disabled="row.status !== 1" @click="disableShare(row)">关闭</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑分享' : '创建分享'" width="520px">
      <el-form label-position="top">
        <el-form-item label="分享标题">
          <el-input v-model="form.title" maxlength="160" placeholder="我的 BingoCV 简历" />
        </el-form-item>
        <el-form-item label="分享类型">
          <el-segmented
            v-model="form.shareType"
            :options="[
              { value: 'PUBLIC', label: '公开分享' },
              { value: 'PRIVATE', label: '私密分享' }
            ]"
          />
        </el-form-item>
        <el-form-item v-if="form.shareType === 'PRIVATE'" label="访问密码">
          <el-input v-model="form.password" maxlength="32" placeholder="编辑时留空表示沿用旧密码" show-password />
        </el-form-item>
        <el-form-item label="访问限制">
          <el-input-number v-model="form.accessLimit" :min="0" :max="9999" />
          <span class="field-help">0 表示不限制访问次数</span>
        </el-form-item>
        <el-form-item label="过期时间">
          <el-date-picker
            v-model="form.expireTime"
            type="datetime"
            value-format="YYYY-MM-DDTHH:mm:ss"
            placeholder="永不过期"
            clearable
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveShare">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="statsVisible" title="访问统计" width="720px">
      <div class="stats-grid">
        <section class="stats-card">
          <span>总访问量</span>
          <strong>{{ stats?.accessCount || 0 }}</strong>
        </section>
        <section class="stats-card">
          <span>今日访问</span>
          <strong>{{ stats?.todayCount || 0 }}</strong>
        </section>
        <section class="stats-card">
          <span>独立访客</span>
          <strong>{{ stats?.uniqueVisitorCount || 0 }}</strong>
        </section>
      </div>
      <el-table :data="stats?.recentRecords || []" height="360" empty-text="暂无访问记录">
        <el-table-column prop="ip" label="IP 地址" width="150" />
        <el-table-column prop="region" label="地区" width="120" />
        <el-table-column prop="userAgent" label="浏览器信息" min-width="260" show-overflow-tooltip />
        <el-table-column prop="createTime" label="访问时间" width="170" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { closeShare, createShare, getShareList, getShareStats, updateShare } from '@/request/share.js'

const shares = ref([])
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const statsVisible = ref(false)
const editingId = ref(null)
const stats = ref(null)

const form = reactive({
  title: '',
  shareType: 'PUBLIC',
  password: '',
  accessLimit: 0,
  expireTime: ''
})

const buildShareLink = (row) => `${window.location.origin}/share/${row.shortCode}`

const loadShares = async () => {
  loading.value = true
  try {
    shares.value = await getShareList()
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  editingId.value = null
  form.title = ''
  form.shareType = 'PUBLIC'
  form.password = ''
  form.accessLimit = 0
  form.expireTime = ''
}

const openCreate = () => {
  resetForm()
  dialogVisible.value = true
}

const openEdit = (row) => {
  editingId.value = row.id
  form.title = row.title || ''
  form.shareType = row.shareType || 'PUBLIC'
  form.password = ''
  form.accessLimit = row.accessLimit || 0
  form.expireTime = row.expireTime ? row.expireTime.replace(' ', 'T') : ''
  dialogVisible.value = true
}

const saveShare = async () => {
  if (form.shareType === 'PRIVATE' && !form.password && !editingId.value) {
    ElMessage.warning('请先设置私密分享密码')
    return
  }

  saving.value = true
  try {
    const payload = {
      title: form.title,
      shareType: form.shareType,
      password: form.password,
      accessLimit: form.accessLimit || null,
      expireTime: form.expireTime || null
    }

    // 编辑私密分享时，密码留空表示沿用旧密码，避免误清空。
    if (editingId.value && form.shareType === 'PRIVATE' && !form.password) {
      delete payload.password
    }

    if (editingId.value) {
      await updateShare(editingId.value, payload)
    } else {
      await createShare(payload)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    await loadShares()
  } finally {
    saving.value = false
  }
}

const disableShare = async (row) => {
  await ElMessageBox.confirm('关闭后该分享链接将不能继续访问，确定关闭吗？', '关闭分享', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
  await closeShare(row.id)
  ElMessage.success('分享已关闭')
  await loadShares()
}

const openStats = async (row) => {
  stats.value = await getShareStats(row.id)
  statsVisible.value = true
}

const copyLink = async (row) => {
  await navigator.clipboard.writeText(buildShareLink(row))
  ElMessage.success('分享链接已复制')
}

onMounted(loadShares)
</script>

<style scoped>
.share-page {
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

h2 {
  margin: 0;
  color: var(--el-text-color-primary);
  font-size: 24px;
}

.share-table {
  border: 1px solid var(--light-border);
  border-radius: 8px;
  background: var(--el-bg-color);
}

.title-cell {
  display: grid;
  gap: 4px;
  min-width: 0;
}

.title-cell strong {
  color: var(--el-text-color-primary);
  font-weight: 600;
}

.title-cell span {
  overflow: hidden;
  color: var(--secondary-text-color);
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.action-group {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 6px;
}

.action-group :deep(.el-button) {
  width: 100%;
  margin: 0;
}

.field-help {
  margin-left: 12px;
  color: var(--secondary-text-color);
  font-size: 13px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.stats-card {
  padding: 14px;
  background: var(--light-fill);
  border: 1px solid var(--light-border);
  border-radius: 8px;
}

.stats-card span {
  display: block;
  margin-bottom: 8px;
  color: var(--secondary-text-color);
  font-size: 13px;
}

.stats-card strong {
  color: var(--el-text-color-primary);
  font-size: 24px;
}

@media (max-width: 720px) {
  .page-header {
    align-items: flex-start;
    flex-direction: column;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>
