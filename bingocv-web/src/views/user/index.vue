<template>
  <div class="user-box">
    <div class="header-actions">
      <div class="search">
        <el-input
            v-model="params.username"
            class="search-input"
            placeholder="搜索用户名"
            @keyup.enter="search"
        >
        </el-input>
      </div>
      <el-select v-model="params.status" placeholder="状态" class="status-select">
        <el-option :key="-1" label="全部" :value="-1"/>
        <el-option :key="0" label="正常" :value="0"/>
        <el-option :key="1" label="禁用" :value="1"/>
      </el-select>
      <Icon class="icon" icon="iconoir:search" @click="search" width="20" height="20"/>
      <Icon class="icon" icon="ion:reload" width="18" height="18" @click="refresh"/>
    </div>
    <el-scrollbar ref="scrollbarRef" class="scrollbar">
      <div>
        <div class="loading" :class="tableLoading ? 'loading-show' : 'loading-hide'">
          <loading/>
        </div>
        <el-table
            :data="users"
            style="width: 100%;"
            v-loading="tableLoading"
        >
          <el-table-column label="用户ID" prop="userid" width="180"/>
          <el-table-column label="用户名" prop="username" min-width="150"/>
          <el-table-column label="昵称" prop="nickname" min-width="120"/>
          <el-table-column label="角色" prop="role" width="100">
            <template #default="props">
              <el-tag :type="props.row.role === 'ADMIN' ? 'danger' : 'primary'">
                {{ props.row.role === 'ADMIN' ? '管理员' : '普通用户' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" prop="status" width="100">
            <template #default="props">
              <el-tag :type="props.row.status === 0 ? 'success' : 'danger'">
                {{ props.row.status === 0 ? '正常' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="注册时间" prop="createTime" min-width="180">
            <template #default="props">
              {{ formatTime(props.row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="最后登录" prop="lastLoginTime" min-width="180">
            <template #default="props">
              {{ props.row.lastLoginTime ? formatTime(props.row.lastLoginTime) : '-' }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="160" align="center">
            <template #default="props">
              <div class="action-group">
                <el-button text size="small" @click="viewUser(props.row)">查看</el-button>
                <el-button 
                  text 
                  size="small" 
                  :type="props.row.status === 0 ? 'danger' : 'success'" 
                  @click="toggleStatus(props.row)"
                >
                  {{ props.row.status === 0 ? '禁用' : '启用' }}
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
        <div class="pagination" v-if="total > 10">
          <el-pagination
              :current-page="params.pageNum"
              :page-size="params.pageSize"
              :page-sizes="[10, 20, 30, 50]"
              background
              layout="total, sizes, prev, pager, next"
              :total="total"
              @size-change="sizeChange"
              @current-change="pageNumChange"
          />
        </div>
      </div>
    </el-scrollbar>

    <el-dialog title="用户详情" v-model="showDetail" width="500px">
      <div v-if="currentUser" class="detail-form">
        <div class="detail-row">
          <span class="label">用户ID</span>
          <span>{{ currentUser.userid }}</span>
        </div>
        <div class="detail-row">
          <span class="label">用户名</span>
          <span>{{ currentUser.username }}</span>
        </div>
        <div class="detail-row">
          <span class="label">昵称</span>
          <span>{{ currentUser.nickname || '-' }}</span>
        </div>
        <div class="detail-row">
          <span class="label">角色</span>
          <span>{{ currentUser.role === 'ADMIN' ? '管理员' : '普通用户' }}</span>
        </div>
        <div class="detail-row">
          <span class="label">状态</span>
          <el-tag :type="currentUser.status === 0 ? 'success' : 'danger'">
            {{ currentUser.status === 0 ? '正常' : '禁用' }}
          </el-tag>
        </div>
        <div class="detail-row">
          <span class="label">注册时间</span>
          <span>{{ formatTime(currentUser.createTime) }}</span>
        </div>
        <div class="detail-row">
          <span class="label">最后登录</span>
          <span>{{ currentUser.lastLoginTime ? formatTime(currentUser.lastLoginTime) : '-' }}</span>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import {reactive, ref, onMounted} from "vue";
import {Icon} from "@iconify/vue";
import {ElMessage} from "element-plus";
import dayjs from "dayjs";
import {getUserList, updateUserStatus} from "@/request/user.js";

const users = ref([]);
const total = ref(0);
const tableLoading = ref(false);
const showDetail = ref(false);
const currentUser = ref(null);
const params = reactive({
  pageNum: 1,
  pageSize: 10,
  username: '',
  status: -1
});

onMounted(() => {
  loadUserList();
});

async function loadUserList() {
  tableLoading.value = true;
  try {
    const data = await getUserList({
      pageNum: params.pageNum,
      pageSize: params.pageSize,
      username: params.username || undefined,
      status: params.status >= 0 ? params.status : undefined
    });
    
    users.value = data.list || [];
    total.value = data.total || 0;
  } catch (error) {
    console.error('加载用户列表失败:', error);
  } finally {
    tableLoading.value = false;
  }
}

function search() {
  params.pageNum = 1;
  loadUserList();
}

function refresh() {
  loadUserList();
}

function sizeChange(size) {
  params.pageSize = size;
  params.pageNum = 1;
  loadUserList();
}

function pageNumChange(page) {
  params.pageNum = page;
  loadUserList();
}

function formatTime(time) {
  if (!time) return '-';
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss');
}

function viewUser(row) {
  currentUser.value = row;
  showDetail.value = true;
}

async function toggleStatus(row) {
  const newStatus = row.status === 0 ? 1 : 0;
  const actionText = newStatus === 0 ? '启用' : '禁用';
  
  try {
    await updateUserStatus(row.userid, newStatus);
    row.status = newStatus;
    ElMessage.success(`${actionText}成功`);
  } catch (error) {
    ElMessage.error(`${actionText}失败`);
  }
}
</script>

<style lang="scss" scoped>
.user-box {
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 20px;
}

.header-actions {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  align-items: center;
  
  .search {
    flex: 1;
    max-width: 400px;
  }
  
  .status-select {
    width: 120px;
  }
  
  .icon {
    cursor: pointer;
    padding: 5px;
    border-radius: 4px;
    
    &:hover {
      background: var(--el-fill-color-light);
    }
  }
}

.scrollbar {
  flex: 1;
}

.loading {
  &.loading-hide {
    display: none;
  }
  
  &.loading-show {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    z-index: 10;
  }
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.action-group {
  display: flex;
  gap: 4px;
  justify-content: center;
}

.detail-form {
  padding: 10px 0;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid var(--el-border-color-light);
  
  .label {
    color: var(--el-text-color-secondary);
    font-weight: 500;
  }
  
  &:last-child {
    border-bottom: none;
  }
}
</style>