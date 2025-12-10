<template>
  <div class="settings-container">
    <el-card class="settings-header">
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
        <el-card>
          <el-tabs v-model="activeTab" tab-position="left">
            <!-- 基本设置 -->
            <el-tab-pane label="基本设置" name="basic">
              <div class="settings-section">
                <h3 class="section-title">系统信息</h3>
                <el-form label-width="120px">
                  <el-form-item label="系统名称">
                    <el-input 
                      v-model="settings.systemName" 
                      placeholder="请输入系统名称"
                      style="max-width: 400px"
                      :disabled="!isAdmin"
                    />
                  </el-form-item>
                  <el-form-item label="系统版本">
                    <el-tag type="success">v1.0.0</el-tag>
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
                  <el-form-item v-if="isAdmin">
                    <el-button type="primary" @click="handleSave">保存设置</el-button>
                    <el-button @click="loadSettings">重置</el-button>
                  </el-form-item>
                  <el-alert v-else type="info" :closable="false" style="max-width: 400px;">
                    仅管理员可修改系统基本设置
                  </el-alert>
                </el-form>
              </div>
            </el-tab-pane>

            <!-- 通知设置 -->
            <el-tab-pane label="通知设置" name="notification">
              <div class="settings-section">
                <h3 class="section-title">消息通知</h3>
                <div class="notification-list">
                  <div class="notification-item">
                    <div class="notification-info">
                      <div class="notification-title">
                        <el-icon color="#409eff"><Bell /></el-icon>
                        库存预警通知
                      </div>
                      <div class="notification-desc">当商品库存低于预警值时发送通知</div>
                    </div>
                    <el-switch v-model="settings.inventoryWarning" :disabled="!isAdmin" />
                  </div>
                  <div class="notification-item">
                    <div class="notification-info">
                      <div class="notification-title">
                        <el-icon color="#67c23a"><Document /></el-icon>
                        订单审核通知
                      </div>
                      <div class="notification-desc">有新的采购订单待审核时发送通知</div>
                    </div>
                    <el-switch v-model="settings.orderAudit" :disabled="!isAdmin" />
                  </div>
                  <div class="notification-item">
                    <div class="notification-info">
                      <div class="notification-title">
                        <el-icon color="#e6a23c"><InfoFilled /></el-icon>
                        系统公告通知
                      </div>
                      <div class="notification-desc">系统发布重要公告时发送通知</div>
                    </div>
                    <el-switch v-model="settings.systemNotice" :disabled="!isAdmin" />
                  </div>
                </div>
                <el-form label-width="120px" style="margin-top: 20px;">
                  <el-form-item v-if="isAdmin">
                    <el-button type="primary" @click="handleSave">保存设置</el-button>
                  </el-form-item>
                  <el-alert v-else type="info" :closable="false">
                    仅管理员可修改通知设置
                  </el-alert>
                </el-form>
              </div>
            </el-tab-pane>

            <!-- 安全设置 -->
            <el-tab-pane label="安全设置" name="security">
              <div class="settings-section">
                <h3 class="section-title">账号安全</h3>
                <el-form label-width="150px">
                  <el-form-item label="密码过期时间">
                    <el-input-number 
                      v-model="settings.passwordExpireDays" 
                      :min="30" 
                      :max="365"
                      style="width: 200px"
                      :disabled="!isAdmin"
                    />
                    <span class="form-tip">天（建议90天）</span>
                  </el-form-item>
                  <el-form-item label="登录失败锁定次数">
                    <el-input-number 
                      v-model="settings.loginFailTimes" 
                      :min="3" 
                      :max="10"
                      style="width: 200px"
                      :disabled="!isAdmin"
                    />
                    <span class="form-tip">次（建议5次）</span>
                  </el-form-item>
                  <el-form-item label="会话超时时间">
                    <el-input-number 
                      v-model="settings.sessionTimeout" 
                      :min="10" 
                      :max="120"
                      style="width: 200px"
                      :disabled="!isAdmin"
                    />
                    <span class="form-tip">分钟（建议30分钟）</span>
                  </el-form-item>
                  <el-form-item label="强密码策略">
                    <el-switch v-model="settings.strongPassword" :disabled="!isAdmin" />
                    <span class="form-tip">启用后密码必须包含大小写字母、数字和特殊字符</span>
                  </el-form-item>
                  <el-form-item v-if="isAdmin">
                    <el-button type="primary" @click="handleSave">保存设置</el-button>
                  </el-form-item>
                  <el-alert v-else type="info" :closable="false" style="max-width: 500px;">
                    仅管理员可修改安全设置
                  </el-alert>
                </el-form>
              </div>
            </el-tab-pane>

            <!-- 关于系统 -->
            <el-tab-pane label="关于系统" name="about">
              <div class="settings-section">
                <h3 class="section-title">系统信息</h3>
                <div class="about-content">
                  <div class="about-logo">
                    <el-icon :size="80" color="#409eff"><ShoppingCart /></el-icon>
                  </div>
                  <h2>{{ systemStore.systemName || '超市进销存管理系统' }}</h2>
                  <p class="version">版本：v1.0.0</p>
                  <p class="description">
                    {{ systemStore.systemDescription || '专业的超市进销存管理解决方案，提供商品、采购、销售、库存全流程管理' }}
                  </p>
                  
                  <el-divider />
                  
                  <div class="tech-stack">
                    <h4>技术栈</h4>
                    <el-row :gutter="10">
                      <el-col :span="12">
                        <div class="tech-item">
                          <strong>前端：</strong>
                          <el-tag size="small" style="margin-left: 5px;">Vue 3</el-tag>
                          <el-tag size="small" style="margin-left: 5px;">Element Plus</el-tag>
                          <el-tag size="small" style="margin-left: 5px;">Vite</el-tag>
                        </div>
                      </el-col>
                      <el-col :span="12">
                        <div class="tech-item">
                          <strong>后端：</strong>
                          <el-tag size="small" style="margin-left: 5px;">Spring Boot</el-tag>
                          <el-tag size="small" style="margin-left: 5px;">MyBatis Plus</el-tag>
                          <el-tag size="small" style="margin-left: 5px;">MySQL</el-tag>
                        </div>
                      </el-col>
                    </el-row>
                  </div>
                  
                  <el-divider />
                  
                  <div class="copyright">
                    <p>开发团队：超市管理系统开发组</p>
                    <p>许可证：MIT License</p>
                    <p>© 2024 超市进销存管理系统. All Rights Reserved.</p>
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
.settings-container {
  padding: 20px;
  
  .settings-header {
    margin-bottom: 20px;
    
    .header-content {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      .title-section {
        h2 {
          margin: 0;
          font-size: 24px;
          color: #303133;
          display: flex;
          align-items: center;
          gap: 8px;
        }
        
        .subtitle {
          margin: 8px 0 0 0;
          color: #909399;
          font-size: 14px;
        }
      }
    }
  }
  
  .settings-section {
    padding: 20px;
    
    .section-title {
      margin: 0 0 20px 0;
      font-size: 18px;
      color: #303133;
      padding-bottom: 10px;
      border-bottom: 1px solid #EBEEF5;
    }
    
    .form-tip {
      margin-left: 10px;
      color: #909399;
      font-size: 12px;
    }
  }
  
  .notification-list {
    .notification-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 20px;
      margin-bottom: 15px;
      background: #f5f7fa;
      border-radius: 8px;
      transition: all 0.3s;
      
      &:hover {
        background: #ecf5ff;
      }
      
      .notification-info {
        flex: 1;
        
        .notification-title {
          font-size: 16px;
          color: #303133;
          font-weight: 500;
          margin-bottom: 8px;
          display: flex;
          align-items: center;
          gap: 8px;
        }
        
        .notification-desc {
          color: #606266;
          font-size: 14px;
        }
      }
    }
  }
  
  .about-content {
    text-align: center;
    padding: 40px;
    
    .about-logo {
      margin-bottom: 20px;
    }
    
    h2 {
      margin: 10px 0;
      color: #303133;
    }
    
    .version {
      color: #909399;
      font-size: 14px;
      margin: 10px 0;
    }
    
    .description {
      color: #606266;
      font-size: 14px;
      margin: 20px auto;
      max-width: 600px;
      line-height: 1.8;
    }
    
    .tech-stack {
      text-align: left;
      max-width: 600px;
      margin: 0 auto;
      
      h4 {
        margin-bottom: 15px;
        color: #303133;
      }
      
      .tech-item {
        margin-bottom: 15px;
        
        strong {
          color: #606266;
        }
      }
    }
    
    .copyright {
      margin-top: 30px;
      color: #909399;
      font-size: 12px;
      
      p {
        margin: 8px 0;
      }
    }
  }
}
</style>
