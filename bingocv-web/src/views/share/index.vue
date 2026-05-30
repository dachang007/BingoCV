<template>
  <div class="share-page">
    <header class="page-header">
      <div>
        <p class="eyebrow">{{ $t('sharing') }}</p>
        <h2>{{ $t('myShares') }}</h2>
      </div>
      <el-button type="primary" @click="openCreate">
        {{ $t('createShare') }}
      </el-button>
    </header>

    <el-table :data="shares" v-loading="loading" class="share-table">
      <el-table-column prop="title" :label="$t('resumeTitle')" min-width="180" />
      <el-table-column prop="shareType" :label="$t('shareType')" width="110">
        <template #default="{ row }">
          <el-tag :type="row.shareType === 'PRIVATE' ? 'warning' : 'success'" effect="plain">
            {{ row.shareType === 'PUBLIC' ? $t('publicShare') : $t('privateShare') }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="accessCount" :label="$t('visitCount')" width="90" />
      <el-table-column :label="$t('visitLimit')" width="90">
        <template #default="{ row }">{{ row.accessLimit || $t('noLimit') }}</template>
      </el-table-column>
      <el-table-column prop="expireTime" :label="$t('expireTime')" min-width="170">
        <template #default="{ row }">{{ row.expireTime || $t('neverExpire') }}</template>
      </el-table-column>
      <el-table-column :label="$t('status')" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="plain">
            {{ row.status === 1 ? $t('active') : $t('closed') }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="$t('actions')" width="300" fixed="right">
        <template #default="{ row }">
          <el-button text type="primary" @click="copyLink(row)">{{ $t('copyLink') }}</el-button>
          <el-button text @click="openEdit(row)">{{ $t('editBtn') }}</el-button>
          <el-button text @click="openStats(row)">{{ $t('viewStats') }}</el-button>
          <el-button text type="danger" :disabled="row.status !== 1" @click="disableShare(row)">{{ $t('closeShare') }}</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="editingId ? $t('editShare') : $t('createShare')" width="520px">
      <el-form label-position="top">
        <el-form-item :label="$t('resumeTitle')">
          <el-input v-model="form.title" maxlength="160" :placeholder="$t('shareTitlePlaceholder')" />
        </el-form-item>
        <el-form-item :label="$t('shareType')">
          <el-segmented v-model="form.shareType" :options="[{value:'PUBLIC',label:$t('publicShare')},{value:'PRIVATE',label:$t('privateShare')}]" />
        </el-form-item>
        <el-form-item v-if="form.shareType === 'PRIVATE'" :label="$t('accessPassword')">
          <el-input v-model="form.password" maxlength="32" :placeholder="$t('passwordRequired')" show-password />
        </el-form-item>
        <el-form-item :label="$t('visitLimit')">
          <el-input-number v-model="form.accessLimit" :min="0" :max="9999" />
          <span class="field-help">{{ $t('zeroMeansNoLimit') }}</span>
        </el-form-item>
        <el-form-item :label="$t('expireTime')">
          <el-date-picker
            v-model="form.expireTime"
            type="datetime"
            value-format="YYYY-MM-DDTHH:mm:ss"
            :placeholder="$t('neverExpire')"
            clearable
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ $t('cancelBtn') }}</el-button>
        <el-button type="primary" :loading="saving" @click="saveShare">{{ $t('saveBtn') }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="statsVisible" :title="$t('accessRecords')" width="720px">
      <div class="stats-summary">{{ $t('totalVisits') }}: {{ stats?.accessCount || 0 }}</div>
      <el-table :data="stats?.recentRecords || []" height="360">
        <el-table-column prop="ip" :label="$t('ipAddress')" width="150" />
        <el-table-column prop="region" :label="$t('region')" width="120" />
        <el-table-column prop="userAgent" :label="$t('userAgent')" min-width="260" show-overflow-tooltip />
        <el-table-column prop="createTime" :label="$t('accessTime')" width="170" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { closeShare, createShare, getShareList, getShareStats, updateShare } from '@/request/share.js';

const shares = ref([]);
const loading = ref(false);
const saving = ref(false);
const dialogVisible = ref(false);
const statsVisible = ref(false);
const editingId = ref(null);
const stats = ref(null);
const shareTypes = ['PUBLIC', 'PRIVATE'];

const form = reactive({
  title: '',
  shareType: 'PUBLIC',
  password: '',
  accessLimit: 0,
  expireTime: ''
});

const loadShares = async () => {
  loading.value = true;
  try {
    shares.value = await getShareList();
  } finally {
    loading.value = false;
  }
};

const resetForm = () => {
  editingId.value = null;
  form.title = '';
  form.shareType = 'PUBLIC';
  form.password = '';
  form.accessLimit = 0;
  form.expireTime = '';
};

const openCreate = () => {
  resetForm();
  dialogVisible.value = true;
};

const openEdit = (row) => {
  editingId.value = row.id;
  form.title = row.title || '';
  form.shareType = row.shareType || 'PUBLIC';
  form.password = '';
  form.accessLimit = row.accessLimit || 0;
  form.expireTime = row.expireTime ? row.expireTime.replace(' ', 'T') : '';
  dialogVisible.value = true;
};

const saveShare = async () => {
  if (form.shareType === 'PRIVATE' && !form.password && !editingId.value) {
    ElMessage.warning($t('privatePasswordRequired'));
    return;
  }
  saving.value = true;
  try {
    const payload = {
      title: form.title,
      shareType: form.shareType,
      password: form.password,
      accessLimit: form.accessLimit || null,
      expireTime: form.expireTime || null
    };
    if (editingId.value) {
      await updateShare(editingId.value, payload);
    } else {
      await createShare(payload);
    }
    ElMessage.success($t('saveSuccessMsg'));
    dialogVisible.value = false;
    await loadShares();
  } finally {
    saving.value = false;
  }
};

const disableShare = async (row) => {
  await closeShare(row.id);
  ElMessage.success($t('closeSuccess'));
  await loadShares();
};

const openStats = async (row) => {
  stats.value = await getShareStats(row.id);
  statsVisible.value = true;
};

const copyLink = async (row) => {
  // Use the current frontend origin so copied links work in dev, preview, and production.
  const link = `${window.location.origin}/s/${row.shortCode}`;
  await navigator.clipboard.writeText(link);
  ElMessage.success($t('linkCopiedMsg'));
};

onMounted(loadShares);
</script>

<style scoped>
.share-page {
  padding: 24px;
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
  color: #667085;
  font-size: 13px;
}

h2 {
  margin: 0;
  color: #1f2937;
  font-size: 24px;
}

.share-table {
  border: 1px solid var(--light-border);
  border-radius: 8px;
}

.field-help {
  margin-left: 12px;
  color: #667085;
  font-size: 13px;
}

.stats-summary {
  margin-bottom: 12px;
  color: #1f2937;
  font-weight: 600;
}

@media (max-width: 720px) {
  .page-header {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
