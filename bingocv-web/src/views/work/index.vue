<template>
  <div class="page">
    <div class="page-header">
      <div>
        <p class="eyebrow">Work Experience</p>
        <h2>工作经历</h2>
      </div>
      <el-button type="primary" @click="openCreate">新增</el-button>
    </div>

    <el-table v-loading="loading" :data="workList" class="table" empty-text="暂无工作经历">
      <el-table-column label="公司" prop="company" min-width="170" />
      <el-table-column label="职位" prop="job" min-width="150" />
      <el-table-column label="时间" min-width="190">
        <template #default="{ row }">{{ row.start || '-' }} 至 {{ row.end || '至今' }}</template>
      </el-table-column>
      <el-table-column label="排序" prop="priority" width="90" />
      <el-table-column label="工作描述" prop="description" min-width="260" show-overflow-tooltip />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-popconfirm title="确认删除这条工作经历？" @confirm="remove(row.id)">
            <template #reference>
              <el-button link type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑工作经历' : '新增工作经历'" width="600px">
      <el-form label-position="top" :model="formData">
        <el-form-item label="公司">
          <el-input v-model="formData.company" placeholder="请输入公司名称" />
        </el-form-item>
        <el-form-item label="职位">
          <el-input v-model="formData.job" placeholder="请输入职位名称" />
        </el-form-item>
        <div class="form-row">
          <el-form-item label="开始时间">
            <el-date-picker v-model="formData.start" type="month" value-format="YYYY-MM" placeholder="开始月份" />
          </el-form-item>
          <el-form-item label="结束时间">
            <el-date-picker v-model="formData.end" type="month" value-format="YYYY-MM" placeholder="结束月份" />
          </el-form-item>
        </div>
        <el-form-item label="排序">
          <el-input-number v-model="formData.priority" :min="0" controls-position="right" />
        </el-form-item>
        <el-form-item label="工作描述">
          <el-input v-model="formData.description" type="textarea" :rows="5" placeholder="建议使用动作 + 方法 + 结果描述成果" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { addWork, deleteWork, getWorkList, updateWork } from '@/request/resume.js';

const loading = ref(false);
const saving = ref(false);
const workList = ref([]);
const dialogVisible = ref(false);
const isEdit = ref(false);
const formData = ref(emptyForm());

function emptyForm() {
  return { id: null, company: '', job: '', start: '', end: '', description: '', priority: 0 };
}

const loadWorkList = async () => {
  loading.value = true;
  try {
    workList.value = await getWorkList() || [];
  } finally {
    loading.value = false;
  }
};

const openCreate = () => {
  isEdit.value = false;
  formData.value = emptyForm();
  dialogVisible.value = true;
};

const openEdit = (row) => {
  isEdit.value = true;
  formData.value = { ...row };
  dialogVisible.value = true;
};

const submit = async () => {
  saving.value = true;
  try {
    if (isEdit.value) {
      await updateWork(formData.value.id, formData.value);
    } else {
      await addWork(formData.value);
    }
    ElMessage.success('工作经历已保存');
    dialogVisible.value = false;
    await loadWorkList();
  } finally {
    saving.value = false;
  }
};

const remove = async (id) => {
  await deleteWork(id);
  ElMessage.success('工作经历已删除');
  await loadWorkList();
};

onMounted(loadWorkList);
</script>

<style scoped>
.page {
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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

.table {
  border: 1px solid var(--light-border);
  border-radius: 8px;
}

.form-row {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

:deep(.el-date-editor),
:deep(.el-input-number) {
  width: 100%;
}
</style>
