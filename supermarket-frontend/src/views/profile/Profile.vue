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
            <h3 class="user-name">{{ userInfo.realName || userInfo.username }}</h3>
            <el-tag :type="getRoleType(userInfo.roleCode)" class="user-role-tag">
              {{ userInfo.roleName }}
            </el-tag>
            <div class="user-stats">
              <div class="stat-item">
                <div class="stat-value">{{ loginDays }}</div>
                <div class="stat-label">连续登录（天）</div>
              </div>
              <div class="stat-item">
                <div class="stat-value">{{ lastLoginTime }}</div>
                <div class="stat-label">最后登录</div>
              </div>
            </div>
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
                    placeholder="请输入新密码"
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
                    <el-icon class="security-icon" color="#67c23a"><CircleCheck /></el-icon>
                    <div>
                      <div class="security-title">账号状态</div>
                      <div class="security-desc">您的账号状态正常</div>
                    </div>
                  </div>
                  <el-tag type="success">正常</el-tag>
                </div>
                <div class="security-item">
                  <div class="security-info">
                    <el-icon class="security-icon" color="#409eff"><Phone /></el-icon>
                    <div>
                      <div class="security-title">手机绑定</div>
                      <div class="security-desc">{{ form.phone || '未绑定手机号' }}</div>
                    </div>
                  </div>
                  <el-button type="primary" text>{{ form.phone ? '修改' : '绑定' }}</el-button>
                </div>
                <div class="security-item">
                  <div class="security-info">
                    <el-icon class="security-icon" color="#e6a23c"><Message /></el-icon>
                    <div>
                      <div class="security-title">邮箱绑定</div>
                      <div class="security-desc">{{ form.email || '未绑定邮箱' }}</div>
                    </div>
                  </div>
                  <el-button type="primary" text>{{ form.email ? '修改' : '绑定' }}</el-button>
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

const userStore = useUserStore()
const activeTab = ref('basic')
const formRef = ref(null)
const passwordFormRef = ref(null)
const saving = ref(false)
const changingPassword = ref(false)
const showAvatarDialog = ref(false)

const userInfo = computed(() => userStore.userInfo || {})
const loginDays = ref(7)
const lastLoginTime = ref('2小时前')

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
  username: '',
  realName: '',
  phone: '',
  email: '',
  roleName: ''
})

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
  } else if (value.length < 6) {
    callback(new Error('密码长度不能小于6位'))
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

// 保存基本信息
const handleSave = async () => {
  try {
    await formRef.value.validate()
    saving.value = true
    
    // TODO: 调用API保存用户信息
    setTimeout(() => {
      saving.value = false
      ElMessage.success('保存成功')
      // 更新store中的用户信息
      userStore.setUserInfo({
        ...userInfo.value,
        ...form.value
      })
    }, 1000)
  } catch (error) {
    console.error('验证失败:', error)
  }
}

// 重置基本信息表单
const handleReset = () => {
  form.value = {
    username: userInfo.value.username || '',
    realName: userInfo.value.realName || '',
    phone: userInfo.value.phone || '',
    email: userInfo.value.email || '',
    roleName: userInfo.value.roleName || ''
  }
}

// 修改密码
const handleChangePassword = async () => {
  try {
    await passwordFormRef.value.validate()
    changingPassword.value = true
    
    // TODO: 调用API修改密码
    setTimeout(() => {
      changingPassword.value = false
      ElMessage.success('密码修改成功，请重新登录')
      resetPasswordForm()
    }, 1000)
  } catch (error) {
    console.error('验证失败:', error)
  }
}

// 重置密码表单
const resetPasswordForm = () => {
  passwordForm.value = {
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  }
  passwordFormRef.value?.resetFields()
}

onMounted(() => {
  // 初始化表单数据
  handleReset()
})
</script>

<style scoped lang="scss">
.profile-container {
  padding: 20px;

  .profile-header {
    margin-bottom: 20px;

    .header-content {
      .title-section {
        h2 {
          margin: 0 0 8px 0;
          font-size: 24px;
          font-weight: 600;
          display: flex;
          align-items: center;
          gap: 8px;
        }

        .subtitle {
          margin: 0;
          color: #909399;
          font-size: 14px;
        }
      }
    }
  }

  .user-card {
    .user-profile {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 20px;

      .user-avatar {
        margin-bottom: 16px;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      }

      .user-name {
        margin: 12px 0 8px;
        font-size: 20px;
        font-weight: 600;
        color: #303133;
      }

      .user-role-tag {
        margin-bottom: 20px;
      }

      .user-stats {
        width: 100%;
        display: flex;
        justify-content: space-around;
        padding-top: 20px;
        border-top: 1px solid #ebeef5;

        .stat-item {
          text-align: center;

          .stat-value {
            font-size: 24px;
            font-weight: 700;
            color: #409eff;
            margin-bottom: 4px;
          }

          .stat-label {
            font-size: 12px;
            color: #909399;
          }
        }
      }
    }
  }

  .info-card {
    min-height: 500px;

    :deep(.el-tabs__content) {
      padding: 20px 0;
    }
  }

  .security-list {
    .security-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 20px;
      margin-bottom: 12px;
      background: #fafafa;
      border-radius: 8px;
      transition: all 0.3s ease;

      &:hover {
        background: #f0f0f0;
        transform: translateX(4px);
      }

      .security-info {
        display: flex;
        align-items: center;
        gap: 16px;

        .security-icon {
          font-size: 28px;
        }

        .security-title {
          font-size: 15px;
          font-weight: 600;
          color: #303133;
          margin-bottom: 4px;
        }

        .security-desc {
          font-size: 13px;
          color: #909399;
        }
      }
    }
  }

  .avatar-options {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 16px;
    padding: 20px;

    .avatar-option {
      cursor: pointer;
      border: 3px solid transparent;
      border-radius: 50%;
      transition: all 0.3s ease;

      &:hover {
        transform: scale(1.1);
      }

      &.active {
        border-color: #409eff;
        box-shadow: 0 0 12px rgba(64, 158, 255, 0.3);
      }
    }
  }
}
</style>

