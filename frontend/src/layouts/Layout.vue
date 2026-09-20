<template>
  <div class="layout">
    <header class="header">
      <div class="logo">
        <span class="logo-icon">💎</span>
        <span class="logo-text">蓝水晶 BLOG</span>
      </div>
      <el-menu mode="horizontal" :default-active="activeIndex" router class="nav-menu">
        <el-menu-item index="/">首页</el-menu-item>
        <el-menu-item index="/knowledge" v-if="isLoggedIn">知识库</el-menu-item>
        <el-menu-item index="/publish" v-if="isLoggedIn">发布文章</el-menu-item>
        <el-menu-item index="/profile" v-if="isLoggedIn">个人中心</el-menu-item>
      </el-menu>
      <div class="header-right">
        <el-dropdown trigger="click" @command="changeTheme">
          <el-button circle class="theme-btn" title="切换主题">
            <el-icon :size="18"><Brush /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="purple">🟣 紫色科幻</el-dropdown-item>
              <el-dropdown-item command="blue">🔵 蓝色科技</el-dropdown-item>
              <el-dropdown-item command="green">🟢 绿色赛博</el-dropdown-item>
              <el-dropdown-item command="pink">🩷 粉色霓虹</el-dropdown-item>
              <el-dropdown-item command="orange">🟠 橙色活力</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <template v-if="isLoggedIn">
          <span class="username">{{ user?.nickname || user?.username }}</span>
          <el-button type="primary" size="small" @click="logout">退出</el-button>
        </template>
        <el-button v-else type="primary" size="small" @click="goLogin">登录</el-button>
      </div>
    </header>
    <main class="main">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Brush } from '@element-plus/icons-vue'
import { useRoute, useRouter } from 'vue-router'
import { getToken, getUser, clearAuthAndRedirect } from '../utils/request'

const route = useRoute()
const router = useRouter()
const user = ref(getUser())
const isLoggedIn = computed(() => !!getToken())
const activeIndex = computed(() => {
    if (route.path.startsWith('/knowledge')) {
        return '/knowledge'
    }
    if (route.path.startsWith('/publish')) {
        return '/publish'
    }
    if (route.path.startsWith('/profile')) {
        return '/profile'
    }
    return route.path
})
const currentTheme = ref('purple')

const changeTheme = (theme) => {
  document.body.setAttribute('data-theme', theme)
  localStorage.setItem('theme', theme)
}

onMounted(() => {
  const saved = localStorage.getItem('theme') || 'purple'
  currentTheme.value = saved
  document.body.setAttribute('data-theme', saved)
})

const logout = () => {
  clearAuthAndRedirect()
}

const goLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.layout { min-height: 100vh; }
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 40px;
  height: 64px;
  background: var(--header-bg);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid var(--border);
  position: sticky;
  top: 0;
  z-index: 100;
}
.logo { display: flex; align-items: center; gap: 10px; }
.logo-icon {
  font-size: 24px;
  filter: drop-shadow(0 0 8px var(--glow));
}
.logo-text {
  font-size: 20px;
  font-weight: 700;
  background: linear-gradient(135deg, var(--primary-light), var(--accent));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  letter-spacing: 2px;
}
.nav-menu { flex: 1; margin-left: 60px; }
.header-right { display: flex; align-items: center; gap: 16px; }
.theme-btn {
  background: var(--card-bg-solid) !important;
  border: 1px solid var(--border-strong) !important;
  color: var(--primary-light) !important;
}
.theme-btn:hover {
  border-color: var(--primary) !important;
  box-shadow: 0 0 12px var(--glow) !important;
}
.username { color: var(--primary-light); font-size: 14px; }
.main { max-width: 1200px; margin: 0 auto; padding: 30px 20px; }
</style>
