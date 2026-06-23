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
          accept="image/jpeg,image/jpg,image/png,image/gif,image/webp"
          :on-change="handlePhotoChange"
          :before-upload="beforePhotoUpload"
        >
          <el-avatar :size="116" :src="avatarUrl">
            <Icon icon="solar:user-circle-linear" width="56" />
          </el-avatar>
          <el-button plain :loading="uploading">上传头像</el-button>
        </el-upload>
        <p>支持 JPG、PNG、GIF、WebP 格式，文件大小不超过 10MB。上传后记得点击保存。</p>
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
              <el-input v-model="formData.weixin" placeholder="选填" />
            </el-form-item>
            <el-form-item label="QQ">
              <el-input v-model="formData.qq" placeholder="选填" />
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
import { computed, onMounted, ref } from 'vue'
import { Icon } from '@iconify/vue'
import { ElMessage } from 'element-plus'
import { getProfiles, updateProfiles } from '@/request/resume.js'
import { uploadAvatar } from '@/request/upload.js'

const saving = ref(false)
const uploading = ref(false)
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
})

const avatarUrl = computed(() => formData.value.photo || '')

const loadProfiles = async () => {
  const data = await getProfiles()
  if (data) {
    formData.value = { ...formData.value, ...data }
  }
}

const beforePhotoUpload = (file) => {
  const isImage = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp'].includes(file.type)
  const isLt10M = file.size / 1024 / 1024 < 10

  if (!isImage) {
    ElMessage.error('只能上传 JPG/PNG/GIF/WebP 格式的图片')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('图片大小不能超过 10MB')
    return false
  }
  return true
}

const handlePhotoChange = async (uploadFile) => {
  const file = uploadFile.raw
  if (!file || !beforePhotoUpload(file)) return

  uploading.value = true
  try {
    const result = await uploadAvatar(file)
    formData.value.photo = result.url
    ElMessage.success('头像上传成功')
  } catch (error) {
    ElMessage.error(error?.msg || error?.message || '头像上传失败')
  } finally {
    uploading.value = false
  }
}

const saveProfiles = async () => {
  saving.value = true
  try {
    const data = await updateProfiles(formData.value)
    formData.value = { ...formData.value, ...data }
    ElMessage.success('个人简历已保存')
  } finally {
    saving.value = false
  }
}

onMounted(loadProfiles)
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
  color: var(--secondary-text-color);
  font-size: 13px;
  line-height: 1.7;
}

.form-row {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

:deep(.el-input-number),
:deep(.el-select) {
  width: 100%;
}

@media (max-width: 900px) {
  .profile-grid,
  .form-row {
    grid-template-columns: 1fr;
  }
}
</style>
