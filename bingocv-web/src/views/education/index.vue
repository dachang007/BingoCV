<template>
  <div class="page">
    <div class="page-header">
      <div>
        <p class="eyebrow">Education</p>
        <h2>教育经历</h2>
      </div>
      <el-button type="primary" @click="openCreate">新增</el-button>
    </div>

    <el-table v-loading="loading" :data="eduList" class="table" empty-text="暂无教育经历">
      <el-table-column label="学校" prop="school" min-width="160" />
      <el-table-column label="专业" prop="study" min-width="150" />
      <el-table-column label="时间" min-width="190">
        <template #default="{ row }">{{ row.start || '-' }} 至 {{ row.end || '至今' }}</template>
      </el-table-column>
      <el-table-column label="排序" prop="priority" width="90" />
      <el-table-column label="描述" prop="description" min-width="220" show-overflow-tooltip />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-popconfirm title="确认删除这条教育经历？" @confirm="remove(row.id)">
            <template #reference>
              <el-button link type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑教育经历' : '新增教育经历'" width="560px">
      <el-form label-position="top" :model="formData">
        <el-form-item label="学校">
          <el-input v-model="formData.school" placeholder="请输入学校名称" />
        </el-form-item>
        <el-form-item label="专业">
          <el-input v-model="formData.study" placeholder="请输入专业" />
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
        <el-form-item label="描述">
          <el-input v-model="formData.description" type="textarea" :rows="4" placeholder="例如：主修课程、奖项、项目实践" />
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
import { addEdu, deleteEdu, getEduList, updateEdu } from '@/request/resume.js';

const loading = ref(false);
const saving = ref(false);
const eduList = ref([]);
const dialogVisible = ref(false);
const isEdit = ref(false);
const formData = ref(emptyForm());

function emptyForm() {
  return { id: null, school: '', study: '', start: '', end: '', description: '', priority: 0 };
}

const loadEduList = async () => {
  loading.value = true;
  try {
    eduList.value = await getEduList() || [];
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
      await updateEdu(formData.value.id, formData.value);
    } else {
      await addEdu(formData.value);
    }
    ElMessage.success('教育经历已保存');
    dialogVisible.value = false;
    await loadEduList();
  } finally {
    saving.value = false;
  }
};

const remove = async (id) => {
  await deleteEdu(id);
  ElMessage.success('教育经历已删除');
  await loadEduList();
};

onMounted(loadEduList);
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
  border: 1px solid #edf0f5;
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
