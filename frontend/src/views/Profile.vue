<template>
  <div class="profile fade-in">
    <div class="page-header">
      <h2 class="page-title">个人中心</h2>
      <p class="page-desc">管理你的账户信息</p>
    </div>

    <!-- 用户信息卡片 -->
    <el-card class="profile-card">
      <div class="avatar-section">
        <div class="avatar">
          {{ (user?.nickname || user?.username || 'U').charAt(0).toUpperCase() }}
        </div>
        <div class="user-basic">
          <h3 class="nickname">{{ user?.nickname || user?.username }}</h3>
          <p class="user-intro">{{ user?.intro || '这个人很懒，什么都没写' }}</p>
          <div class="user-tags">
            <span class="user-tag">
              <el-icon><User /></el-icon>
              {{ user?.username }}
            </span>
            <span class="user-tag" v-if="user?.email">
              <el-icon><Message /></el-icon>
              {{ user?.email }}
            </span>
          </div>
        </div>
      </div>
      <el-divider />
      <el-descriptions :column="2" border>
        <el-descriptions-item label="用户名">{{ user?.username }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ user?.nickname || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ user?.email || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="手机">{{ user?.phone || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="用户ID">
          <span class="user-id">{{ user?.id }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="user?.status === 1 ? 'success' : 'danger'" size="small" effect="dark">
            {{ user?.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { User, Message } from '@element-plus/icons-vue'
import { getUser } from '../utils/request'

const user = ref(getUser())
</script>

<style scoped>
.profile { max-width: 900px; margin: 0 auto; }
.page-header { margin-bottom: 24px; }
.page-title {
  font-size: 30px;
  font-weight: 800;
  background: linear-gradient(135deg, var(--primary-light), var(--accent));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 6px;
  letter-spacing: 1px;
}
.page-desc { color: var(--text-muted); font-size: 14px; }
.profile-card {
  border-radius: 20px !important;
  padding: 24px !important;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2) !important;
}
.avatar-section { display: flex; align-items: center; gap: 28px; }
.avatar {
  width: 90px;
  height: 90px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--primary), var(--accent));
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  font-weight: 700;
  color: #fff;
  box-shadow: 0 0 30px var(--glow);
  flex-shrink: 0;
  animation: pulse-glow 3s ease-in-out infinite;
}
.user-basic { flex: 1; }
.nickname {
  font-size: 26px;
  color: var(--text-primary);
  margin-bottom: 8px;
  font-weight: 700;
}
.user-intro { color: var(--text-secondary); font-size: 14px; margin-bottom: 12px; }
.user-tags { display: flex; gap: 10px; flex-wrap: wrap; }
.user-tag {
  display: flex;
  align-items: center;
  gap: 5px;
  background: var(--gradient-1);
  border: 1px solid var(--border-strong);
  color: var(--primary-light);
  font-size: 12px;
  padding: 4px 12px;
  border-radius: 14px;
}
.user-tag :deep(.el-icon) { font-size: 13px; }
.user-id {
  font-family: 'Courier New', monospace;
  font-size: 13px;
  color: var(--primary-light);
  background: var(--card-bg-solid);
  padding: 2px 8px;
  border-radius: 6px;
}
:deep(.el-descriptions) {
  --el-descriptions-table-border: var(--border);
  --el-descriptions-item-label-bg: var(--gradient-1);
  --el-descriptions-text-color: #cbd5e1;
  --el-descriptions-item-label-color: var(--primary-light);
}
:deep(.el-descriptions__body) {
  border-radius: 12px;
  overflow: hidden;
}
</style>
