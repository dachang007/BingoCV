<template>
  <div class="page">
    <div class="page-header">
      <div>
        <p class="eyebrow">Resume Profile</p>
        <h2>个人简历</h2>
      </div>
      <el-button type="primary" :loading="saving" @click="saveProfiles">保存</el-button>
    </div>

    <div class="profile-grid">
      <section class="photo-panel">
        <el-upload
          class="avatar-uploader"
          :auto-upload="false"
          :show-file-list="false"
          accept="image/*"
          :on-change="handlePhotoChange"
        >
          <el-avatar :size="116" :src="formData.photo">
            <Icon icon="solar:user-circle-linear" width="56" />
          </el-avatar>
          <el-button plain>上传头像</el-button>
        </el-upload>
        <p>建议使用清晰正面照。当前版本先保存为浏览器可预览的数据地址，后续可接入对象存储。</p>
      </section>

      <section class="form-panel">
        <el-form label-position="top" :model="formData">
          <div class="form-row">
            <el-form-item label="姓名">
              <el-input v-model="formData.name" placeholder="请输入姓名" />
            </el-form-item>
            <el-form-item label="性别">
              <el-select v-model="formData.sex" placeholder="请选择">
                <el-option label="男" value="male" />
                <el-option label="女" value="female" />
                <el-option label="其他" value="other" />
              </el-select>
            </el-form-item>
          </div>

          <div class="form-row">
            <el-form-item label="年龄">
              <el-input-number v-model="formData.age" :min="0" :max="100" controls-position="right" />
            </el-form-item>
            <el-form-item label="城市">
              <el-input v-model="formData.city" placeholder="例如：上海" />
            </el-form-item>
          </div>

          <div class="form-row">
            <el-form-item label="邮箱">
              <el-input v-model="formData.email" placeholder="name@example.com" />
            </el-form-item>
            <el-form-item label="手机">
              <el-input v-model="formData.phone" placeholder="请输入联系方式" />
            </el-form-item>
          </div>

          <div class="form-row">
            <el-form-item label="微信">
              <el-input v-model="formData.weixin" placeholder="可选" />
            </el-form-item>
            <el-form-item label="QQ">
              <el-input v-model="formData.qq" placeholder="可选" />
            </el-form-item>
          </div>

          <el-form-item label="详细地址">
            <el-input v-model="formData.address" placeholder="请输入详细地址" />
          </el-form-item>

          <el-form-item label="个人简介">
            <el-input
              v-model="formData.description"
              type="textarea"
              :rows="5"
              maxlength="500"
              show-word-limit
              placeholder="用 2-4 句话描述你的职业方向、核心能力和代表成果"
            />
          </el-form-item>
        </el-form>
      </section>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { Icon } from '@iconify/vue';
import { getProfiles, updateProfiles } from '@/request/resume.js';

const saving = ref(false);
const formData = ref({
  id: null,
  name: '',
  photo: '',
  sex: 'male',
  age: null,
  city: '',
  address: '',
  email: '',
  phone: '',
  weixin: '',
  qq: '',
  weibo: '',
  description: ''
});

const loadProfiles = async () => {
  const data = await getProfiles();
  if (data) {
    formData.value = { ...formData.value, ...data };
  }
};

const handlePhotoChange = (uploadFile) => {
  const file = uploadFile.raw;
  if (!file) return;
  const reader = new FileReader();
  reader.onload = () => {
    formData.value.photo = reader.result;
  };
  reader.readAsDataURL(file);
};

const saveProfiles = async () => {
  saving.value = true;
  try {
    const data = await updateProfiles(formData.value);
    formData.value = { ...formData.value, ...data };
    ElMessage.success('个人简历已保存');
  } finally {
    saving.value = false;
  }
};

onMounted(loadProfiles);
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

.profile-grid {
  display: grid;
  grid-template-columns: 280px minmax(0, 1fr);
  gap: 18px;
  align-items: start;
}

.photo-panel,
.form-panel {
  background: var(--el-bg-color);
  border: 1px solid var(--light-border);
  border-radius: 8px;
  padding: 20px;
}

.avatar-uploader {
  display: flex;
  flex-direction: column;
  gap: 16px;
  align-items: center;
}

.photo-panel p {
  margin: 18px 0 0;
  color: #667085;
  font-size: 13px;
  line-height: 1.7;
}

.form-row {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

:deep(.el-input-number) {
  width: 100%;
}

@media (max-width: 900px) {
  .profile-grid,
  .form-row {
    grid-template-columns: 1fr;
  }
}
</style>
