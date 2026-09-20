<template>
  <div class="login-container">
    <!-- 背景浮动光斑 -->
    <div class="bg-orb orb-1"></div>
    <div class="bg-orb orb-2"></div>
    <div class="bg-orb orb-3"></div>
    <div class="bg-grid"></div>

    <div class="login-box fade-in-up">
      <div class="login-header">
        <div class="logo-icon">💎</div>
        <h1 class="title">蓝水晶 BLOG</h1>
        <p class="subtitle">{{ isRegister ? '创建新账号' : '欢迎回来，请登录' }}</p>
      </div>
      <el-form :model="form" class="login-form">
        <el-form-item>
          <el-input v-model="form.username" placeholder="用户名" size="large" prefix-icon="User"/>
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" placeholder="密码" size="large" prefix-icon="Lock"
                    show-password/>
        </el-form-item>
        <el-form-item v-if="isRegister">
          <el-input v-model="form.nickname" placeholder="昵称（可选）" size="large" prefix-icon="UserFilled"/>
        </el-form-item>
        <el-button type="primary" size="large" class="login-btn" @click="handleSubmit" :loading="loading">
          {{ isRegister ? '注 册' : '登 录' }}
        </el-button>
      </el-form>
      <div class="switch-mode">
        <span @click="toggleMode">
          {{ isRegister ? '已有账号？去登录' : '没有账号？去注册' }}
        </span>
      </div>
    </div>
  </div>
</template>

<script setup>
import {ref} from 'vue'
import {useRouter} from 'vue-router'
import {login, register} from '../api/user'
import {setToken, setUser} from '../utils/request'
import {ElMessage} from 'element-plus'

const router = useRouter()
const loading = ref(false)
const isRegister = ref(false)
const form = ref({username: '', password: '', nickname: ''})

const toggleMode = () => {
  isRegister.value = !isRegister.value
  form.value.nickname = ''
}

const handleSubmit = async () => {
  if (!form.value.username || !form.value.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  if (form.value.username.length < 3) {
    ElMessage.warning('用户名至少3个字符')
    return
  }
  if (form.value.password.length < 6) {
    ElMessage.warning('密码至少6个字符')
    return
  }
  loading.value = true
  try {
    if (isRegister.value) {
      // 注册
      const res = await register({
        username: form.value.username,
        password: form.value.password,
        nickname: form.value.nickname || undefined
      })
      ElMessage.success('注册成功，请登录')
      isRegister.value = false
      form.value.password = ''
    } else {
      // 登录：返回 { token, user }
      const res = await login({
        username: form.value.username,
        password: form.value.password
      })
      setToken(res.data.token)
      setUser(res.data.user)
      ElMessage.success('登录成功')
      router.push('/')
    }
  } catch (e) {
    // 错误信息已在拦截器中提示
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  background: var(--bg-primary);
}

/* 背景浮动光斑 */
.bg-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.4;
  pointer-events: none;
}

.orb-1 {
  width: 400px;
  height: 400px;
  background: var(--primary);
  top: -100px;
  left: -100px;
  animation: float 8s ease-in-out infinite;
}

.orb-2 {
  width: 350px;
  height: 350px;
  background: var(--accent);
  bottom: -80px;
  right: -80px;
  animation: float 10s ease-in-out infinite reverse;
}

.orb-3 {
  width: 250px;
  height: 250px;
  background: var(--primary-dark);
  top: 50%;
  left: 60%;
  animation: float 12s ease-in-out infinite;
  opacity: 0.25;
}

/* 背景网格 */
.bg-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(139, 92, 246, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(139, 92, 246, 0.03) 1px, transparent 1px);
  background-size: 50px 50px;
  pointer-events: none;
}

.login-box {
  width: 420px;
  padding: 50px 40px;
  background: var(--card-bg);
  border: 1px solid var(--border-strong);
  border-radius: 20px;
  backdrop-filter: blur(20px);
  box-shadow: 0 0 60px var(--glow), 0 20px 60px rgba(0, 0, 0, 0.3);
  position: relative;
  z-index: 1;
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.logo-icon {
  font-size: 56px;
  filter: drop-shadow(0 0 20px var(--glow));
  margin-bottom: 12px;
  animation: pulse-glow 3s ease-in-out infinite;
  display: inline-block;
}

.title {
  font-size: 30px;
  font-weight: 800;
  background: linear-gradient(135deg, var(--primary-light), var(--accent));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: 4px;
  margin-bottom: 10px;
}

.subtitle {
  color: var(--text-muted);
  font-size: 14px;
}

.login-form {
  margin-top: 30px;
}

.login-form :deep(.el-input__wrapper) {
  border-radius: 12px !important;
  padding: 6px 15px !important;
}

.login-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  letter-spacing: 6px;
  margin-top: 10px;
  border-radius: 12px !important;
  font-weight: 600;
}

.switch-mode {
  text-align: center;
  margin-top: 24px;
  color: var(--text-muted);
  font-size: 13px;
}

.switch-mode span {
  cursor: pointer;
  color: var(--primary-light);
  transition: all 0.2s;
  position: relative;
}

.switch-mode span::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 0;
  height: 1px;
  background: var(--primary-light);
  transition: width 0.3s;
}

.switch-mode span:hover {
  color: var(--accent);
}

.switch-mode span:hover::after {
  width: 100%;
}
</style>
