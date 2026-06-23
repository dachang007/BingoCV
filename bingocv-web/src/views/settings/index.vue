<template>
  <div class="settings-page">
    <header class="page-header">
      <div>
        <p class="eyebrow">系统运行参数</p>
        <h2>系统配置</h2>
      </div>
      <div class="toolbar">
        <el-input
          v-model="params.keyword"
          clearable
          placeholder="搜索配置键或说明"
          @keyup.enter="loadConfigs"
          @clear="loadConfigs"
        />
        <el-button type="primary" :loading="loading" @click="loadConfigs">查询</el-button>
      </div>
    </header>

    <el-tabs v-model="activeGroup" class="config-tabs">
      <el-tab-pane v-for="group in groups" :key="group.name" :label="group.label" :name="group.name" />
    </el-tabs>

    <el-table :data="visibleConfigs" v-loading="loading" class="config-table" empty-text="暂无系统配置">
      <el-table-column prop="configKey" label="配置键" min-width="240" show-overflow-tooltip />
      <el-table-column label="配置值" min-width="220" show-overflow-tooltip>
        <template #default="{ row }">
          <span>{{ formatConfigValue(row) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="说明" min-width="280" show-overflow-tooltip />
      <el-table-column prop="enabled" label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.enabled === 1 ? 'success' : 'info'" effect="plain">
            {{ row.enabled === 1 ? '启用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" align="center">
        <template #default="{ row }">
          <el-button text type="primary" @click="openEdit(row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" title="编辑系统配置" width="540px">
      <el-form label-position="top">
        <el-form-item label="配置键">
          <el-input v-model="form.configKey" disabled />
        </el-form-item>
        <el-form-item label="配置值">
          <el-input
            v-if="isSecretConfig(form.configKey)"
            v-model="form.configValue"
            type="password"
            show-password
            placeholder="请输入密钥"
          />
          <el-switch
            v-else-if="isBooleanValue(form.configValue)"
            v-model="form.configValue"
            active-value="true"
            inactive-value="false"
            active-text="开启"
            inactive-text="关闭"
          />
          <el-input v-else v-model="form.configValue" placeholder="请输入配置值" />
        </el-form-item>
        <el-form-item label="说明">
          <el-input v-model="form.description" type="textarea" :rows="3" maxlength="255" show-word-limit />
        </el-form-item>
        <el-form-item label="是否启用">
          <el-switch v-model="form.enabled" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="停用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveConfig">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getSystemConfigs, updateSystemConfig } from '@/request/admin.js'

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const configs = ref([])
const activeGroup = ref('ai')
const params = reactive({ keyword: '' })
const form = reactive({
  id: null,
  configKey: '',
  configValue: '',
  description: '',
  enabled: 1
})

const groups = [
  { name: 'ai', label: 'AI 配置', match: key => key.startsWith('ai.') },
  { name: 'runtime', label: '运行配置', match: key => key.startsWith('rate.limit') },
  { name: 'share', label: '分享配置', match: key => key.startsWith('share.') },
  { name: 'points', label: '积分配置', match: key => key.startsWith('points.') },
  { name: 'other', label: '其他配置', match: key => !['ai.', 'rate.limit', 'share.', 'points.'].some(prefix => key.startsWith(prefix)) }
]

const visibleConfigs = computed(() => {
  const group = groups.find(item => item.name === activeGroup.value)
  return configs.value.filter(item => group?.match(item.configKey || ''))
})

async function loadConfigs() {
  loading.value = true
  try {
    configs.value = await getSystemConfigs({ keyword: params.keyword || undefined })
  } finally {
    loading.value = false
  }
}

function openEdit(row) {
  form.id = row.id
  form.configKey = row.configKey
  form.configValue = row.configValue
  form.description = row.description
  form.enabled = row.enabled
  dialogVisible.value = true
}

async function saveConfig() {
  saving.value = true
  try {
    await updateSystemConfig(form.id, {
      configValue: form.configValue,
      description: form.description,
      enabled: form.enabled
    })
    ElMessage.success('系统配置已保存')
    dialogVisible.value = false
    await loadConfigs()
  } finally {
    saving.value = false
  }
}

function isSecretConfig(key) {
  return /api[-_.]?key|secret|token/i.test(key || '')
}

function isBooleanValue(value) {
  return value === 'true' || value === 'false'
}

function formatConfigValue(row) {
  if (isSecretConfig(row.configKey) && row.configValue) {
    return '已配置，内容已隐藏'
  }
  return row.configValue || '-'
}

onMounted(loadConfigs)
</script>

<style scoped>
.settings-page {
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
  margin-bottom: 14px;
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
  grid-template-columns: minmax(220px, 320px) auto;
  gap: 10px;
}

.config-tabs {
  margin-bottom: 12px;
}

.config-table {
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;
  background: var(--el-bg-color-overlay);
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
