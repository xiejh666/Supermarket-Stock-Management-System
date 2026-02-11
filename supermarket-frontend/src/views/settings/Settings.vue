<template>
  <div class="settings-container glass-page">
    <el-card class="settings-header page-header">
      <div class="header-content">
        <div class="title-section">
          <h2>
            <el-icon><Setting /></el-icon>
            系统设置
          </h2>
          <p class="subtitle">配置系统参数和偏好设置</p>
        </div>
      </div>
    </el-card>

    <el-row :gutter="20">
      <el-col :span="24">
        <el-card class="main-settings-card">
          <el-tabs v-model="activeTab" tab-position="left" class="custom-tabs">
            <!-- 基本设置 -->
            <el-tab-pane label="基本设置" name="basic">
              <div class="settings-section">
                <h3 class="section-title">系统信息</h3>
                <el-form label-width="120px" class="settings-form">
                  <el-form-item label="系统名称">
                    <el-input 
                      v-model="settings.systemName" 
                      placeholder="请输入系统名称"
                      style="max-width: 400px"
                      :disabled="!isAdmin"
                    />
                  </el-form-item>
                  <el-form-item label="系统版本">
                    <el-tag type="success" effect="light" class="round-tag">v1.0.0</el-tag>
                  </el-form-item>
                  <el-form-item label="系统描述">
                    <el-input
                      v-model="settings.systemDescription"
                      type="textarea"
                      placeholder="请输入系统描述"
                      :rows="3"
                      style="max-width: 400px"
                      :disabled="!isAdmin"
                    />
                  </el-form-item>
                  <el-form-item v-if="isAdmin" class="form-actions">
                    <el-button type="primary" @click="handleSave" class="glass-btn primary">保存设置</el-button>
                    <el-button @click="loadSettings" class="glass-btn plain">重置</el-button>
                  </el-form-item>
                  <el-alert v-else type="info" :closable="false" show-icon style="max-width: 400px;" title="仅管理员可修改系统基本设置" />
                </el-form>
              </div>
            </el-tab-pane>

            <!-- 通知设置 -->
            <el-tab-pane label="通知设置" name="notification">
              <div class="settings-section">
                <h3 class="section-title">消息通知</h3>
                <div class="notification-list">
                  <div class="notification-item glass-item">
                    <div class="notification-info">
                      <div class="notification-title">
                        <el-icon color="#3b82f6"><Bell /></el-icon>
                        库存预警通知
                      </div>
                      <div class="notification-desc">当商品库存低于预警值时发送通知</div>
                    </div>
                    <el-switch v-model="settings.inventoryWarning" :disabled="!isAdmin" />
                  </div>
                  <div class="notification-item glass-item">
                    <div class="notification-info">
                      <div class="notification-title">
                        <el-icon color="#10b981"><Document /></el-icon>
                        订单审核通知
                      </div>
                      <div class="notification-desc">有新的采购订单待审核时发送通知</div>
                    </div>
                    <el-switch v-model="settings.orderAudit" :disabled="!isAdmin" />
                  </div>
                  <div class="notification-item glass-item">
                    <div class="notification-info">
                      <div class="notification-title">
                        <el-icon color="#f59e0b"><InfoFilled /></el-icon>
                        系统公告通知
                      </div>
                      <div class="notification-desc">系统发布重要公告时发送通知</div>
                    </div>
                    <el-switch v-model="settings.systemNotice" :disabled="!isAdmin" />
                  </div>
                </div>
                <div v-if="isAdmin" style="margin-top: 32px; padding-left: 20px;">
                  <el-button type="primary" @click="handleSave" class="glass-btn primary">保存设置</el-button>
                </div>
                <el-alert v-else type="info" :closable="false" show-icon style="margin-top: 20px;" title="仅管理员可修改通知设置" />
              </div>
            </el-tab-pane>

            <!-- 安全设置 -->
            <el-tab-pane label="安全设置" name="security">
              <div class="settings-section">
                <h3 class="section-title">账号安全</h3>
                <el-form label-width="150px" class="settings-form">
                  <el-form-item label="密码过期时间">
                    <el-input-number 
                      v-model="settings.passwordExpireDays" 
                      :min="30" 
                      :max="365"
                      style="width: 180px"
                      :disabled="!isAdmin"
                    />
                    <span class="form-tip">天（建议 90 天）</span>
                  </el-form-item>
                  <el-form-item label="登录失败锁定次数">
                    <el-input-number 
                      v-model="settings.loginFailTimes" 
                      :min="3" 
                      :max="10"
                      style="width: 180px"
                      :disabled="!isAdmin"
                    />
                    <span class="form-tip">次（建议 5 次）</span>
                  </el-form-item>
                  <el-form-item label="会话超时时间">
                    <el-input-number 
                      v-model="settings.sessionTimeout" 
                      :min="10" 
                      :max="120"
                      style="width: 180px"
                      :disabled="!isAdmin"
                    />
                    <span class="form-tip">分钟（建议 30 分钟）</span>
                  </el-form-item>
                  <el-form-item label="强密码策略">
                    <el-switch v-model="settings.strongPassword" :disabled="!isAdmin" />
                    <span class="form-tip">启用后密码必须包含大小写字母、数字和特殊字符</span>
                  </el-form-item>
                  <el-form-item v-if="isAdmin" class="form-actions">
                    <el-button type="primary" @click="handleSave" class="glass-btn primary">保存设置</el-button>
                  </el-form-item>
                  <el-alert v-else type="info" :closable="false" show-icon style="max-width: 500px;" title="仅管理员可修改安全设置" />
                </el-form>
              </div>
            </el-tab-pane>

            <!-- 关于系统 -->
            <el-tab-pane label="关于系统" name="about">
              <div class="settings-section">
                <h3 class="section-title">系统信息</h3>
                <div class="about-content">
                  <div class="about-logo">
                    <div class="logo-wrapper">
                      <el-icon :size="60" color="#3b82f6"><ShoppingCart /></el-icon>
                    </div>
                  </div>
                  <h2 class="system-name">{{ systemStore.systemName || '超市进销存管理系统' }}</h2>
                  <p class="version">版本：v1.0.0</p>
                  <p class="description">
                    {{ systemStore.systemDescription || '专业的超市进销存管理解决方案，提供商品、采购、销售、库存全流程管理' }}
                  </p>
                  
                  <el-divider />
                  
                  <div class="tech-stack">
                    <h4>技术栈</h4>
                    <el-row :gutter="20">
                      <el-col :span="12">
                        <div class="tech-item glass-item">
                          <strong>前端：</strong>
                          <div class="tags-row">
                            <el-tag size="small" effect="light">Vue 3</el-tag>
                            <el-tag size="small" effect="light">Element Plus</el-tag>
                            <el-tag size="small" effect="light">Vite</el-tag>
                          </div>
                        </div>
                      </el-col>
                      <el-col :span="12">
                        <div class="tech-item glass-item">
                          <strong>后端：</strong>
                          <div class="tags-row">
                            <el-tag size="small" effect="light">Spring Boot</el-tag>
                            <el-tag size="small" effect="light">MyBatis Plus</el-tag>
                            <el-tag size="small" effect="light">MySQL</el-tag>
                          </div>
                        </div>
                      </el-col>
                    </el-row>
                  </div>
                  
                  <el-divider />
                  
                  <div class="copyright">
                    <p class="team">开发团队：xiejh666</p>
                  </div>
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Setting, Bell, Document, InfoFilled, ShoppingCart } from '@element-plus/icons-vue'
import settingsApi from '@/api/settings'
import { useUserStore } from '@/store/user'
import { useSystemStore } from '@/store/system'

const activeTab = ref('basic')
const userStore = useUserStore()
const systemStore = useSystemStore()

// 判断是否为管理员
const isAdmin = computed(() => {
  return userStore.userInfo?.roleCode === 'ADMIN'
})

// 系统设置数据
const settings = ref({
  // 基本设置
  systemName: '',
  systemDescription: '',
  
  // 通知设置
  inventoryWarning: true,
  orderAudit: true,
  systemNotice: true,
  
  // 安全设置
  passwordExpireDays: 90,
  loginFailTimes: 5,
  sessionTimeout: 30,
  strongPassword: true
})

// 加载设置
const loadSettings = async () => {
  try {
    const response = await settingsApi.getSettings()
    if (response.code === 200) {
      // 合并服务器返回的数据
      Object.assign(settings.value, response.data)
    }
  } catch (error) {
    ElMessage.error('加载设置失败')
  }
}

// 保存设置
const handleSave = async () => {
  try {
    const response = await settingsApi.saveSettings(settings.value)
    if (response.code === 200) {
      ElMessage.success('保存成功')
      // 更新全局 store 中的系统名称，实现实时同步
      if (settings.value.systemName) {
        systemStore.setSystemName(settings.value.systemName)
      }
      if (settings.value.systemDescription) {
        systemStore.setSystemDescription(settings.value.systemDescription)
      }
      // 重新加载设置
      await loadSettings()
    } else {
      ElMessage.error(response.message || '保存失败')
    }
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

// 页面加载时获取设置
onMounted(() => {
  loadSettings()
})
</script>

<style lang="scss" scoped>
@use "@/styles/glass-theme.scss" as *;

.settings-container {
  padding: 0;
  
  .settings-header {
    margin-bottom: 24px;
    border: none;
    background: linear-gradient(135deg, rgba(255, 255, 255, 0.9) 0%, rgba(255, 255, 255, 0.7) 100%);
    
    .title-section {
      h2 {
        margin: 0;
        font-size: 24px;
        font-weight: 800;
        color: #1e293b;
        display: flex;
        align-items: center;
        gap: 12px;
      }
      
      .subtitle {
        margin: 8px 0 0 0;
        color: #64748b;
        font-size: 14px;
      }
    }
  }

  .main-settings-card {
    border-radius: 20px;
    border: 1px solid rgba(226, 232, 240, 0.8);
    box-shadow: 0 8px 32px -12px rgba(15, 23, 42, 0.08);
    backdrop-filter: blur(10px);
    background: rgba(255, 255, 255, 0.8);
    
    :deep(.el-card__body) {
      padding: 0;
    }
  }
  
  .custom-tabs {
    height: 600px;
    
    :deep(.el-tabs__header) {
      margin-right: 0;
      background: rgba(248, 250, 252, 0.4);
      border-right: 1px solid rgba(226, 232, 240, 0.6);
      padding: 20px 0;
    }

    :deep(.el-tabs__nav-wrap::after) {
      display: none;
    }

    :deep(.el-tabs__item) {
      height: 54px;
      line-height: 54px;
      padding: 0 32px;
      font-weight: 600;
      color: #64748b;
      font-size: 15px;
      transition: all 0.3s;

      &.is-active {
        color: #3b82f6;
        background: rgba(59, 130, 246, 0.06);
      }
    }

    :deep(.el-tabs__active-bar) {
      width: 3px;
      border-radius: 3px;
    }

    :deep(.el-tabs__content) {
      padding: 10px;
      overflow-y: auto;
    }
  }
  
  .settings-section {
    padding: 30px 40px;
    
    .section-title {
      margin: 0 0 24px 0;
      font-size: 18px;
      font-weight: 700;
      color: #1e293b;
      padding-bottom: 12px;
      border-bottom: 1px solid rgba(226, 232, 240, 0.6);
    }
    
    .form-tip {
      margin-left: 12px;
      color: #94a3b8;
      font-size: 13px;
    }
  }

  .settings-form {
    .form-actions {
      margin-top: 40px;
    }
  }
  
  .glass-item {
    background: rgba(248, 250, 252, 0.6);
    border: 1px solid rgba(226, 232, 240, 0.6);
    border-radius: 16px;
    padding: 20px;
    margin-bottom: 16px;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

    &:hover {
      background: rgba(255, 255, 255, 0.9);
      transform: translateY(-2px);
      box-shadow: 0 8px 20px -8px rgba(0, 0, 0, 0.08);
    }
  }

  .notification-list {
    .notification-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      .notification-info {
        flex: 1;
        
        .notification-title {
          font-size: 15px;
          color: #1e293b;
          font-weight: 700;
          margin-bottom: 4px;
          display: flex;
          align-items: center;
          gap: 10px;

          .el-icon {
            padding: 8px;
            background: #fff;
            border-radius: 10px;
            box-shadow: 0 4px 10px rgba(0, 0, 0, 0.02);
          }
        }
        
        .notification-desc {
          color: #64748b;
          font-size: 13px;
          padding-left: 42px;
        }
      }
    }
  }
  
  .about-content {
    text-align: center;
    padding: 20px 0;
    
    .about-logo {
      margin-bottom: 24px;
      
      .logo-wrapper {
        display: inline-flex;
        padding: 20px;
        background: rgba(59, 130, 246, 0.06);
        border-radius: 24px;
        border: 1px solid rgba(59, 130, 246, 0.1);
      }
    }
    
    .system-name {
      margin: 12px 0;
      font-size: 24px;
      font-weight: 800;
      color: #1e293b;
    }
    
    .version {
      color: #94a3b8;
      font-size: 14px;
      margin-bottom: 16px;
      font-weight: 600;
    }
    
    .description {
      color: #64748b;
      font-size: 14px;
      margin: 20px auto;
      max-width: 500px;
      line-height: 1.8;
    }
    
    .tech-stack {
      text-align: left;
      max-width: 600px;
      margin: 0 auto;
      
      h4 {
        margin-bottom: 16px;
        color: #1e293b;
        font-weight: 700;
        text-align: center;
      }
      
      .tech-item {
        height: 100%;
        display: flex;
        flex-direction: column;
        gap: 12px;

        strong {
          color: #475569;
          font-size: 14px;
        }

        .tags-row {
          display: flex;
          flex-wrap: wrap;
          gap: 8px;
        }
      }
    }
    
    .copyright {
      margin-top: 40px;
      color: #94a3b8;
      font-size: 13px;
      
      p {
        margin: 10px 0;
      }

      .team {
        color: #475569;
        font-weight: 700;
        font-size: 14px;
      }

      .footer-copy {
        font-size: 12px;
        opacity: 0.8;
      }
    }
  }
}

.round-tag {
  border-radius: 8px;
  font-weight: 600;
}
</style>
