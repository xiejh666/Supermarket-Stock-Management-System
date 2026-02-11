<template>
  <div class="login-wrapper">
    <div class="login-container">
      <!-- 左侧：品牌展示区 -->
      <div class="login-brand">
        <div class="brand-content">
          <div class="brand-logo">
            <div class="logo-icon">M</div>
            <span class="logo-text">SuperMarket</span>
          </div>
          <h1 class="brand-title">超市进销存<br/>管理系统</h1>
          <p class="brand-desc">专业、高效、可靠的零售业务全链路协同平台</p>
          
          <ul class="brand-features">
            <li><span class="dot"></span> 实时库存监控与预警</li>
            <li><span class="dot"></span> 多维度销售数据分析</li>
            <li><span class="dot"></span> 智能化采购流程管理</li>
            <li><span class="dot"></span> 灵活的权限安全控制</li>
          </ul>
        </div>
        <div class="brand-bg-decoration"></div>
      </div>

      <!-- 右侧：登录操作区 -->
      <div class="login-form-area">
        <div class="form-inner">
          <div class="form-header">
            <h2>欢迎回来</h2>
            <p>请输入您的账号信息登录系统</p>
          </div>

          <el-form
            ref="loginFormRef"
            :model="loginForm"
            :rules="loginRules"
            label-position="top"
            class="custom-form"
          >
            <el-form-item label="用户名" prop="username">
              <el-input 
                v-model="loginForm.username" 
                placeholder="请输入用户名"
                :prefix-icon="User"
              />
            </el-form-item>
            
            <el-form-item label="密码" prop="password">
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                show-password
                :prefix-icon="Lock"
                @keyup.enter="handleLogin"
              />
            </el-form-item>

            <el-form-item label="验证码" prop="code">
              <div class="captcha-wrapper">
                <el-input 
                  v-model="loginForm.code" 
                  placeholder="验证码" 
                  class="captcha-input"
                  :prefix-icon="CircleCheck"
                  @keyup.enter="handleLogin"
                />
                <div class="captcha-img-box" @click="getCaptcha" title="点击刷新验证码">
                  <img v-if="captchaImg" :src="captchaImg" alt="验证码" />
                  <div v-else class="captcha-loading">加载中...</div>
                </div>
              </div>
            </el-form-item>

            <div class="form-actions">
              <el-button 
                type="primary" 
                @click="handleLogin" 
                :loading="loading" 
                class="login-btn"
              >
                登 录
              </el-button>
            </div>
          </el-form>

          <div class="form-footer">
            <p>© 2026 超市进销存管理系统 · 毕业设计项目</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'
import { User, Lock, CircleCheck } from '@element-plus/icons-vue'
import { removeToken } from '@/utils/auth'
import request from '@/utils/request'

const router = useRouter()
const userStore = useUserStore()

const loginFormRef = ref(null)
const loading = ref(false)
const captchaImg = ref('')

const loginForm = reactive({
  username: '',
  password: '',
  code: '',
  uuid: ''
})

const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { min: 4, max: 4, message: '请输入4位验证码', trigger: 'blur' }
  ]
}

// 获取验证码
const getCaptcha = async () => {
  try {
    const res = await request.get('/auth/captcha')
    if (res.code === 200) {
      captchaImg.value = res.data.img
      loginForm.uuid = res.data.uuid
    }
  } catch (error) {
    console.error('获取验证码失败:', error)
    ElMessage.error('获取验证码失败，请刷新重试')
  }
}

onMounted(() => {
  removeToken()
  getCaptcha()
})

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await request.post('/auth/login', loginForm)
        if (res.code === 200) {
          userStore.setToken(res.data.token)
          userStore.setUserInfo(res.data)
          ElMessage.success('登录成功')
          router.push('/')
        }
      } catch (error) {
        console.error('登录错误:', error)
        ElMessage.error(error.response?.data?.message || error.message || '登录失败')
        loginForm.code = ''
        getCaptcha()
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
/* 容器布局 */
.login-wrapper {
  height: 100vh;
  width: 100vw;
  display: flex;
  justify-content: center;
  align-items: center;
  background:
    radial-gradient(900px 420px at 18% 18%, rgba(59, 130, 246, 0.10), transparent 55%),
    radial-gradient(760px 380px at 85% 75%, rgba(16, 185, 129, 0.08), transparent 60%),
    radial-gradient(520px 260px at 65% 18%, rgba(148, 163, 184, 0.10), transparent 55%),
    #f6f8fb;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  overflow: hidden;
}

.login-container {
  display: flex;
  width: 1040px;
  height: 620px;
  background: rgba(255, 255, 255, 0.78);
  border-radius: 22px;
  box-shadow: 0 35px 80px -35px rgba(15, 23, 42, 0.25);
  border: 1px solid rgba(226, 232, 240, 0.8);
  overflow: hidden;
  backdrop-filter: blur(10px);
}

/* 左侧品牌区样式 */
.login-brand {
  flex: 1.1;
  background:
    radial-gradient(820px 380px at 12% 15%, rgba(59, 130, 246, 0.14), transparent 60%),
    radial-gradient(700px 340px at 88% 80%, rgba(16, 185, 129, 0.10), transparent 62%),
    linear-gradient(180deg, rgba(255,255,255,0.75), rgba(255,255,255,0.58));
  padding: 64px 60px;
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: center;
  color: #0f172a;
}

.brand-content {
  position: relative;
  z-index: 2;
}

.brand-logo {
  display: flex;
  align-items: center;
  margin-bottom: 40px;
}

.logo-icon {
  width: 32px;
  height: 32px;
  background: rgba(59, 130, 246, 0.12);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  font-size: 16px;
  margin-right: 12px;
  color: #1d4ed8;
  border: 1px solid rgba(59, 130, 246, 0.18);
}

.logo-text {
  font-size: 18px;
  font-weight: 700;
  letter-spacing: -0.4px;
  color: #0f172a;
}

.brand-title {
  font-size: 36px;
  font-weight: 800;
  line-height: 1.15;
  margin-bottom: 18px;
  color: #0f172a;
}

.brand-desc {
  font-size: 15px;
  color: rgba(15, 23, 42, 0.72);
  margin-bottom: 38px;
  max-width: 320px;
}

.brand-features {
  list-style: none;
  padding: 0;
}

.brand-features li {
  display: flex;
  align-items: center;
  margin-bottom: 14px;
  font-size: 14px;
  color: rgba(15, 23, 42, 0.78);
}

.dot {
  width: 6px;
  height: 6px;
  background: rgba(59, 130, 246, 0.9);
  border-radius: 50%;
  margin-right: 12px;
}

.brand-bg-decoration {
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  background-image:
    radial-gradient(circle at 2px 2px, rgba(15, 23, 42, 0.06) 1px, transparent 0);
  background-size: 26px 26px;
  z-index: 1;
  mask-image: radial-gradient(80% 70% at 20% 20%, black 20%, transparent 70%);
  opacity: 0.9;
}

/* 右侧表单区样式 */
.login-form-area {
  flex: 1;
  padding: 60px;
  display: flex;
  align-items: center;
}

.form-inner {
  width: 100%;
}

.form-header {
  margin-bottom: 40px;
}

.form-header h2 {
  font-size: 28px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 8px;
}

.form-header p {
  color: #64748b;
  font-size: 14px;
}

/* 表单组件深度美化 */
:deep(.el-form-item__label) {
  font-weight: 600;
  color: #475569;
  padding-bottom: 8px !important;
}

:deep(.el-input__wrapper) {
  background-color: #f8fafc;
  box-shadow: none !important;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 8px 12px;
  transition: all 0.2s;
}

:deep(.el-input__wrapper.is-focus) {
  background-color: #ffffff;
  border-color: #3b82f6;
  box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.1) !important;
}

.captcha-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
}

.captcha-img-box {
  width: 130px;
  height: 42px;
  cursor: pointer;
  border-radius: 10px;
  overflow: hidden;
  border: 1px solid rgba(226, 232, 240, 0.8);
  background: #ffffff;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.captcha-img-box img {
  width: 100%;
  height: 100%;
  object-fit: fill;
}

.captcha-loading {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #94a3b8;
}

.login-btn {
  width: 100%;
  height: 48px;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 600;
  background: #3b82f6;
  border: none;
  margin-top: 10px;
  transition: all 0.3s;
}

.login-btn:hover {
  background: #2563eb;
  transform: translateY(-1px);
  box-shadow: 0 10px 15px -3px rgba(37, 99, 235, 0.3);
}

.form-footer {
  margin-top: 40px;
  text-align: center;
  font-size: 12px;
  color: #94a3b8;
}

/* 响应式适配 */
@media (max-width: 1024px) {
  .login-container {
    width: 90%;
    height: auto;
    min-height: 500px;
  }
  .login-brand {
    display: none;
  }
}
</style>

