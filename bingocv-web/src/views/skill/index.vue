<template>
  <div class="page">
    <div class="page-header">
      <div>
        <p class="eyebrow">Skills</p>
        <h2>技能特长</h2>
      </div>
      <el-button type="primary" :loading="savingSkill" @click="saveSkill">保存技能</el-button>
    </div>

    <section class="panel">
      <el-form label-position="top">
        <el-form-item label="技能标签">
          <el-select
            v-model="skillTags"
            multiple
            filterable
            allow-create
            default-first-option
            placeholder="输入技能后回车，例如 Java、Vue、MySQL"
          />
        </el-form-item>
      </el-form>
      <div class="tag-preview">
        <el-tag v-for="tag in skillTags" :key="tag" effect="plain">{{ tag }}</el-tag>
      </div>
    </section>

    <section class="panel">
      <div class="section-header">
        <h3>特长与亮点</h3>
        <el-button @click="openCreate">新增</el-button>
      </div>

      <el-table v-loading="loading" :data="specialtyList" empty-text="暂无特长">
        <el-table-column label="名称" prop="name" min-width="160" />
        <el-table-column label="描述" prop="description" min-width="260" show-overflow-tooltip />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-popconfirm title="确认删除这条特长？" @confirm="remove(row.id)">
              <template #reference>
                <el-button link type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑特长' : '新增特长'" width="520px">
      <el-form label-position="top" :model="formData">
        <el-form-item label="名称">
          <el-input v-model="formData.name" placeholder="例如：技术写作、团队协作、开源贡献" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="formData.description" type="textarea" :rows="4" placeholder="描述这个特长如何支撑你的岗位竞争力" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingSpecialty" @click="submitSpecialty">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import {
  addSpecialty,
  deleteSpecialty,
  getSkill,
  getSpecialtyList,
  updateSkill,
  updateSpecialty
} from '@/request/resume.js';

const loading = ref(false);
const savingSkill = ref(false);
const savingSpecialty = ref(false);
const skillData = ref({ keywords: '' });
const specialtyList = ref([]);
const dialogVisible = ref(false);
const isEdit = ref(false);
const formData = ref(emptyForm());

const skillTags = computed({
  get() {
    return skillData.value.keywords ? skillData.value.keywords.split(/\s+/).filter(Boolean) : [];
  },
  set(tags) {
    skillData.value.keywords = tags.join(' ');
  }
});

function emptyForm() {
  return { id: null, name: '', description: '' };
}

const loadSkill = async () => {
  const data = await getSkill();
  skillData.value = { keywords: '', ...(data || {}) };
};

const loadSpecialtyList = async () => {
  loading.value = true;
  try {
    specialtyList.value = await getSpecialtyList() || [];
  } finally {
    loading.value = false;
  }
};

const saveSkill = async () => {
  savingSkill.value = true;
  try {
    skillData.value = await updateSkill(skillData.value);
    ElMessage.success('技能已保存');
  } finally {
    savingSkill.value = false;
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

const submitSpecialty = async () => {
  savingSpecialty.value = true;
  try {
    if (isEdit.value) {
      await updateSpecialty(formData.value.id, formData.value);
    } else {
      await addSpecialty(formData.value);
    }
    ElMessage.success('特长已保存');
    dialogVisible.value = false;
    await loadSpecialtyList();
  } finally {
    savingSpecialty.value = false;
  }
};

const remove = async (id) => {
  await deleteSpecialty(id);
  ElMessage.success('特长已删除');
  await loadSpecialtyList();
};

onMounted(() => {
  loadSkill();
  loadSpecialtyList();
});
</script>

<style scoped>
.page {
  padding: 24px;
}

.page-header,
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-header {
  margin-bottom: 20px;
}

.eyebrow {
  margin: 0 0 4px;
  color: #667085;
  font-size: 13px;
}

h2,
h3 {
  margin: 0;
  color: #1f2937;
}

h2 {
  font-size: 24px;
}

h3 {
  font-size: 18px;
}

.panel {
  background: var(--el-bg-color);
  border: 1px solid var(--light-border);
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 18px;
}

.tag-preview {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

:deep(.el-select) {
  width: 100%;
}
</style>
