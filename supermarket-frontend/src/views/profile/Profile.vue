<template>
  <div class="profile-container">
    <el-card class="profile-header">
      <div class="header-content">
        <div class="title-section">
          <h2>
            <el-icon><User /></el-icon>
            个人中心
          </h2>
          <p class="subtitle">查看和修改个人信息</p>
        </div>
      </div>
    </el-card>

    <el-row :gutter="20">
      <!-- 左侧：个人信息卡片 -->
      <el-col :xs="24" :sm="24" :md="8">
        <el-card class="user-card">
          <div class="user-profile">
            <el-avatar :size="100" :src="avatarUrl" class="user-avatar">
              <el-icon><UserFilled /></el-icon>
            </el-avatar>
            <el-button type="primary" size="small" @click="showAvatarDialog = true" style="margin-top: 12px">
              更换头像
            </el-button>
            <h3 class="user-name">{{ form.realName || form.username }}</h3>
            <el-tag :type="getRoleType(form.roleCode)" class="user-role-tag">
              {{ form.roleName }}
            </el-tag>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧：详细信息 -->
      <el-col :xs="24" :sm="24" :md="16">
        <el-card class="info-card">
          <el-tabs v-model="activeTab">
            <!-- 基本信息 -->
            <el-tab-pane label="基本信息" name="basic">
              <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
                <el-form-item label="用户名" prop="username">
                  <el-input v-model="form.username" disabled>
                    <template #prefix>
                      <el-icon><User /></el-icon>
                    </template>
                  </el-input>
                </el-form-item>
                <el-form-item label="真实姓名" prop="realName">
                  <el-input v-model="form.realName" placeholder="请输入真实姓名">
                    <template #prefix>
                      <el-icon><UserFilled /></el-icon>
                    </template>
                  </el-input>
                </el-form-item>
                <el-form-item label="手机号" prop="phone">
                  <el-input v-model="form.phone" placeholder="请输入手机号">
                    <template #prefix>
                      <el-icon><Phone /></el-icon>
                    </template>
                  </el-input>
                </el-form-item>
                <el-form-item label="邮箱" prop="email">
                  <el-input v-model="form.email" placeholder="请输入邮箱">
                    <template #prefix>
                      <el-icon><Message /></el-icon>
                    </template>
                  </el-input>
                </el-form-item>
                <el-form-item label="角色" prop="roleName">
                  <el-input v-model="form.roleName" disabled>
                    <template #prefix>
                      <el-icon><Avatar /></el-icon>
                    </template>
                  </el-input>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="handleSave" :loading="saving">
                    保存修改
                  </el-button>
                  <el-button @click="handleReset">重置</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>

            <!-- 修改密码 -->
            <el-tab-pane label="修改密码" name="password">
              <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px">
                <el-form-item label="原密码" prop="oldPassword">
                  <el-input
                    v-model="passwordForm.oldPassword"
                    type="password"
                    placeholder="请输入原密码"
                    show-password
                  >
                    <template #prefix>
                      <el-icon><Lock /></el-icon>
                    </template>
                  </el-input>
                </el-form-item>
                <el-form-item label="新密码" prop="newPassword">
                  <el-input
                    v-model="passwordForm.newPassword"
                    type="password"
                    placeholder="请输入新密码（6-20位）"
                    show-password
                  >
                    <template #prefix>
                      <el-icon><Lock /></el-icon>
                    </template>
                  </el-input>
                </el-form-item>
                <el-form-item label="确认密码" prop="confirmPassword">
                  <el-input
                    v-model="passwordForm.confirmPassword"
                    type="password"
                    placeholder="请再次输入新密码"
                    show-password
                  >
                    <template #prefix>
                      <el-icon><Lock /></el-icon>
                    </template>
                  </el-input>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="handleChangePassword" :loading="changingPassword">
                    修改密码
                  </el-button>
                  <el-button @click="resetPasswordForm">重置</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>

            <!-- 账号安全 -->
            <el-tab-pane label="账号安全" name="security">
              <div class="security-list">
                <div class="security-item">
                  <div class="security-info">
                    <el-icon class="security-icon" :color="form.status === 1 ? '#67c23a' : '#f56c6c'">
                      <CircleCheck />
                    </el-icon>
                    <div>
                      <div class="security-title">账号状态</div>
                      <div class="security-desc">{{ form.status === 1 ? '您的账号状态正常' : '您的账号已被禁用' }}</div>
                    </div>
                  </div>
                  <el-tag :type="form.status === 1 ? 'success' : 'danger'">
                    {{ form.status === 1 ? '正常' : '禁用' }}
                  </el-tag>
                </div>
                <div class="security-item">
                  <div class="security-info">
                    <el-icon class="security-icon" color="#409eff"><Phone /></el-icon>
                    <div>
                      <div class="security-title">手机绑定</div>
                      <div class="security-desc">{{ form.phone || '未绑定手机号' }}</div>
                    </div>
                  </div>
                  <el-button type="primary" text @click="activeTab = 'basic'">
                    {{ form.phone ? '修改' : '绑定' }}
                  </el-button>
                </div>
                <div class="security-item">
                  <div class="security-info">
                    <el-icon class="security-icon" color="#e6a23c"><Message /></el-icon>
                    <div>
                      <div class="security-title">邮箱绑定</div>
                      <div class="security-desc">{{ form.email || '未绑定邮箱' }}</div>
                    </div>
                  </div>
                  <el-button type="primary" text @click="activeTab = 'basic'">
                    {{ form.email ? '修改' : '绑定' }}
                  </el-button>
                </div>
                <div class="security-item">
                  <div class="security-info">
                    <el-icon class="security-icon" color="#f56c6c"><Lock /></el-icon>
                    <div>
                      <div class="security-title">登录密码</div>
                      <div class="security-desc">建议定期更换密码</div>
                    </div>
                  </div>
                  <el-button type="primary" text @click="activeTab = 'password'">修改</el-button>
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>

    <!-- 更换头像对话框 -->
    <el-dialog v-model="showAvatarDialog" title="更换头像" width="500px">
      <div class="avatar-options">
        <div
          v-for="(avatar, index) in avatarList"
          :key="index"
          class="avatar-option"
          :class="{ active: avatarUrl === avatar }"
          @click="selectAvatar(avatar)"
        >
          <el-avatar :size="80" :src="avatar" />
        </div>
      </div>
      <template #footer>
        <el-button @click="showAvatarDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSaveAvatar">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'
import {
  User,
  UserFilled,
  Phone,
  Message,
  Lock,
  Avatar,
  CircleCheck
} from '@element-plus/icons-vue'
import { getUserProfile, updateProfile, changePassword } from '@/api/profile'

const userStore = useUserStore()
const activeTab = ref('basic')
const formRef = ref(null)
const passwordFormRef = ref(null)
const saving = ref(false)
const changingPassword = ref(false)
const showAvatarDialog = ref(false)

// 头像列表
const avatarList = ref([
  'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
  'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png',
  'https://cube.elemecdn.com/9/c2/f0ee8a3c7c9638a54940382568c9dpng.png',
  'https://cube.elemecdn.com/e/fd/0fc7d20532fdaf769a25683617711png.png'
])

const avatarUrl = ref('https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png')

// 基本信息表单
const form = ref({
  id: null,
  username: '',
  realName: '',
  phone: '',
  email: '',
  roleName: '',
  roleCode: '',
  status: 1
})

// 原始表单数据（用于重置）
const originalForm = ref({})

const rules = {
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

// 修改密码表单
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validatePassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入新密码'))
  } else if (value.length < 6 || value.length > 20) {
    callback(new Error('密码长度必须在6-20位之间'))
  } else {
    callback()
  }
}

const validateConfirmPassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== passwordForm.value.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [{ required: true, validator: validatePassword, trigger: 'blur' }],
  confirmPassword: [{ required: true, validator: validateConfirmPassword, trigger: 'blur' }]
}

// 获取角色标签类型
const getRoleType = (roleCode) => {
  const typeMap = {
    ADMIN: 'danger',
    PURCHASER: 'warning',
    CASHIER: 'success'
  }
  return typeMap[roleCode] || 'info'
}

// 选择头像
const selectAvatar = (avatar) => {
  avatarUrl.value = avatar
}

// 保存头像
const handleSaveAvatar = () => {
  showAvatarDialog.value = false
  ElMessage.success('头像更新成功')
}

// 获取用户信息
const fetchUserProfile = async () => {
  try {
    const res = await getUserProfile()
    if (res.code === 200) {
      form.value = {
        id: res.data.id,
        username: res.data.username,
        realName: res.data.realName || '',
        phone: res.data.phone || '',
        email: res.data.email || '',
        roleName: res.data.roleName || '',
        roleCode: res.data.roleCode || '',
        status: res.data.status
      }
      // 保存原始数据
      originalForm.value = { ...form.value }
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    ElMessage.error('获取用户信息失败')
  }
}

// 保存基本信息
const handleSave = async () => {
  try {
    await formRef.value.validate()
    saving.value = true
    
    const res = await updateProfile({
      realName: form.value.realName,
      phone: form.value.phone,
      email: form.value.email
    })
    
    if (res.code === 200) {
      ElMessage.success('保存成功')
      // 更新原始数据
      originalForm.value = { ...form.value }
      // 更新store中的用户信息
      userStore.setUserInfo({
        ...userStore.userInfo,
        realName: form.value.realName,
        phone: form.value.phone,
        email: form.value.email
      })
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

// 重置基本信息表单
const handleReset = () => {
  form.value = { ...originalForm.value }
  formRef.value?.clearValidate()
}

// 修改密码
const handleChangePassword = async () => {
  try {
    await passwordFormRef.value.validate()
    changingPassword.value = true
    
    const res = await changePassword({
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword,
      confirmPassword: passwordForm.value.confirmPassword
    })
    
    if (res.code === 200) {
      ElMessage.success('密码修改成功，请重新登录')
      // 清空密码表单
      resetPasswordForm()
      // 3秒后退出登录
      setTimeout(() => {
        userStore.logout()
        window.location.href = '/login'
      }, 3000)
    } else {
      ElMessage.error(res.message || '修改密码失败')
    }
  } catch (error) {
    console.error('修改密码失败:', error)
    ElMessage.error('修改密码失败')
  } finally {
    changingPassword.value = false
  }
}

// 重置密码表单
const resetPasswordForm = () => {
  passwordForm.value = {
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  }
  passwordFormRef.value?.clearValidate()
}

onMounted(() => {
  fetchUserProfile()
})
</script>

<style scoped lang="scss">
.profile-container {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.profile-header {
  margin-bottom: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;

  :deep(.el-card__body) {
    padding: 30px;
  }

  .header-content {
    display: flex;
    justify-content: space-between;
    align-items: center;

    h2 {
      margin: 0 0 8px 0;
      font-size: 28px;
      display: flex;
      align-items: center;
      gap: 12px;

      .el-icon {
        font-size: 32px;
      }
    }

    .subtitle {
      margin: 0;
      opacity: 0.9;
      font-size: 14px;
    }
  }
}

.user-card {
  :deep(.el-card__body) {
    padding: 30px 20px;
  }
}

.user-profile {
  text-align: center;

  .user-avatar {
    margin-bottom: 8px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }

  .user-name {
    margin: 16px 0 8px 0;
    font-size: 20px;
    font-weight: 500;
  }

  .user-role-tag {
    font-size: 12px;
  }
}

.info-card {
  :deep(.el-tabs__nav-wrap::after) {
    display: none;
  }

  :deep(.el-tabs__item) {
    font-size: 15px;
    padding: 0 30px;
  }

  :deep(.el-form) {
    max-width: 600px;
    padding: 20px 0;
  }
}

.security-list {
  padding: 20px 0;
}

.security-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  margin-bottom: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  transition: all 0.3s;

  &:hover {
    background: #e8edf3;
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  }

  &:last-child {
    margin-bottom: 0;
  }
}

.security-info {
  display: flex;
  align-items: center;
  gap: 16px;

  .security-icon {
    font-size: 24px;
  }

  .security-title {
    font-size: 15px;
    font-weight: 500;
    margin-bottom: 4px;
  }

  .security-desc {
    font-size: 13px;
    color: #909399;
  }
}

.avatar-options {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  padding: 20px 0;
}

.avatar-option {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 12px;
  border: 2px solid transparent;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;

  &:hover {
    border-color: #409eff;
    background: #ecf5ff;
  }

  &.active {
    border-color: #409eff;
    background: #ecf5ff;
  }
}

@media (max-width: 768px) {
  .profile-header {
    :deep(.el-card__body) {
      padding: 20px;
    }

    h2 {
      font-size: 22px;
    }
  }

  .avatar-options {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
