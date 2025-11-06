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
                    <el-input v-model="systemSettings.name" style="max-width: 400px" />
                  </el-form-item>
                  <el-form-item label="系统版本">
                    <el-tag type="success">v1.0.0</el-tag>
                  </el-form-item>
                  <el-form-item label="系统描述">
                    <el-input
                      v-model="systemSettings.description"
                      type="textarea"
                      :rows="3"
                      style="max-width: 400px"
                    />
                  </el-form-item>
                  <el-form-item>
                    <el-button type="primary" @click="handleSave">保存设置</el-button>
                  </el-form-item>
                </el-form>
              </div>
            </el-tab-pane>

            <!-- 外观设置 -->
            <el-tab-pane label="外观设置" name="appearance">
              <div class="settings-section">
                <h3 class="section-title">界面主题</h3>
                <el-form label-width="120px">
                  <el-form-item label="主题模式">
                    <el-radio-group v-model="appearanceSettings.theme">
                      <el-radio label="light">浅色</el-radio>
                      <el-radio label="dark">深色</el-radio>
                      <el-radio label="auto">跟随系统</el-radio>
                    </el-radio-group>
                  </el-form-item>
                  <el-form-item label="主题色">
                    <el-color-picker v-model="appearanceSettings.primaryColor" />
                  </el-form-item>
                  <el-form-item label="布局方式">
                    <el-radio-group v-model="appearanceSettings.layout">
                      <el-radio label="classic">经典布局</el-radio>
                      <el-radio label="modern">现代布局</el-radio>
                    </el-radio-group>
                  </el-form-item>
                  <el-form-item label="显示面包屑">
                    <el-switch v-model="appearanceSettings.showBreadcrumb" />
                  </el-form-item>
                  <el-form-item label="显示标签页">
                    <el-switch v-model="appearanceSettings.showTabs" />
                  </el-form-item>
                  <el-form-item>
                    <el-button type="primary" @click="handleSave">保存设置</el-button>
                  </el-form-item>
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
                      <div class="notification-title">库存预警通知</div>
                      <div class="notification-desc">当商品库存低于预警值时发送通知</div>
                    </div>
                    <el-switch v-model="notificationSettings.lowStock" />
                  </div>
                  <div class="notification-item">
                    <div class="notification-info">
                      <div class="notification-title">订单审核通知</div>
                      <div class="notification-desc">有新的采购订单待审核时发送通知</div>
                    </div>
                    <el-switch v-model="notificationSettings.orderAudit" />
                  </div>
                  <div class="notification-item">
                    <div class="notification-info">
                      <div class="notification-title">系统公告通知</div>
                      <div class="notification-desc">系统发布重要公告时发送通知</div>
                    </div>
                    <el-switch v-model="notificationSettings.systemNotice" />
                  </div>
                  <div class="notification-item">
                    <div class="notification-info">
                      <div class="notification-title">邮件通知</div>
                      <div class="notification-desc">通过邮件发送重要通知</div>
                    </div>
                    <el-switch v-model="notificationSettings.emailNotify" />
                  </div>
                </div>
                <el-button type="primary" @click="handleSave" style="margin-top: 20px">
                  保存设置
                </el-button>
              </div>
            </el-tab-pane>

            <!-- 安全设置 -->
            <el-tab-pane label="安全设置" name="security">
              <div class="settings-section">
                <h3 class="section-title">登录安全</h3>
                <el-form label-width="150px">
                  <el-form-item label="密码过期天数">
                    <el-input-number
                      v-model="securitySettings.passwordExpireDays"
                      :min="0"
                      :max="365"
                    />
                    <span class="form-tip">设置为0表示密码永不过期</span>
                  </el-form-item>
                  <el-form-item label="登录失败锁定">
                    <el-switch v-model="securitySettings.lockAfterFailed" />
                    <span class="form-tip">连续登录失败后锁定账号</span>
                  </el-form-item>
                  <el-form-item label="最大失败次数" v-if="securitySettings.lockAfterFailed">
                    <el-input-number
                      v-model="securitySettings.maxFailedAttempts"
                      :min="3"
                      :max="10"
                    />
                  </el-form-item>
                  <el-form-item label="锁定时间（分钟）" v-if="securitySettings.lockAfterFailed">
                    <el-input-number
                      v-model="securitySettings.lockDuration"
                      :min="5"
                      :max="60"
                    />
                  </el-form-item>
                  <el-form-item label="强制使用强密码">
                    <el-switch v-model="securitySettings.strongPassword" />
                    <span class="form-tip">密码必须包含大小写字母、数字和特殊字符</span>
                  </el-form-item>
                  <el-form-item>
                    <el-button type="primary" @click="handleSave">保存设置</el-button>
                  </el-form-item>
                </el-form>
              </div>
            </el-tab-pane>

            <!-- 数据设置 -->
            <el-tab-pane label="数据设置" name="data">
              <div class="settings-section">
                <h3 class="section-title">数据管理</h3>
                <div class="data-actions">
                  <el-card class="action-card">
                    <el-icon class="action-icon" color="#409eff"><Download /></el-icon>
                    <h4>数据导出</h4>
                    <p>导出系统数据到本地</p>
                    <el-button type="primary" plain>导出数据</el-button>
                  </el-card>
                  <el-card class="action-card">
                    <el-icon class="action-icon" color="#67c23a"><Upload /></el-icon>
                    <h4>数据导入</h4>
                    <p>从本地文件导入数据</p>
                    <el-button type="success" plain>导入数据</el-button>
                  </el-card>
                  <el-card class="action-card">
                    <el-icon class="action-icon" color="#e6a23c"><CircleCheck /></el-icon>
                    <h4>数据备份</h4>
                    <p>备份系统数据库数据</p>
                    <el-button type="warning" plain>立即备份</el-button>
                  </el-card>
                  <el-card class="action-card">
                    <el-icon class="action-icon" color="#f56c6c"><Delete /></el-icon>
                    <h4>清理缓存</h4>
                    <p>清理系统缓存数据</p>
                    <el-button type="danger" plain>清理缓存</el-button>
                  </el-card>
                </div>
              </div>
            </el-tab-pane>

            <!-- 关于系统 -->
            <el-tab-pane label="关于系统" name="about">
              <div class="settings-section">
                <div class="about-content">
                  <div class="about-logo">
                    <el-icon :size="80" color="#409eff"><ShoppingCart /></el-icon>
                  </div>
                  <h2>超市进销存管理系统</h2>
                  <p class="version">Version 1.0.0</p>
                  <el-divider />
                  <div class="about-info">
                    <div class="info-row">
                      <span class="label">技术栈：</span>
                      <span class="value">Vue 3 + Vite + Element Plus + Spring Boot</span>
                    </div>
                    <div class="info-row">
                      <span class="label">开发者：</span>
                      <span class="value">超市管理系统开发团队</span>
                    </div>
                    <div class="info-row">
                      <span class="label">发布日期：</span>
                      <span class="value">2025-11-06</span>
                    </div>
                    <div class="info-row">
                      <span class="label">许可证：</span>
                      <span class="value">MIT License</span>
                    </div>
                  </div>
                  <el-divider />
                  <div class="about-links">
                    <el-button type="primary" text>查看文档</el-button>
                    <el-button type="primary" text>检查更新</el-button>
                    <el-button type="primary" text>反馈问题</el-button>
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
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Setting,
  Download,
  Upload,
  CircleCheck,
  Delete,
  ShoppingCart
} from '@element-plus/icons-vue'

const activeTab = ref('basic')

// 系统基本设置
const systemSettings = ref({
  name: '超市进销存管理系统',
  description: '专业的超市商品进销存管理解决方案'
})

// 外观设置
const appearanceSettings = ref({
  theme: 'light',
  primaryColor: '#409eff',
  layout: 'classic',
  showBreadcrumb: true,
  showTabs: false
})

// 通知设置
const notificationSettings = ref({
  lowStock: true,
  orderAudit: true,
  systemNotice: true,
  emailNotify: false
})

// 安全设置
const securitySettings = ref({
  passwordExpireDays: 90,
  lockAfterFailed: true,
  maxFailedAttempts: 5,
  lockDuration: 30,
  strongPassword: false
})

// 保存设置
const handleSave = () => {
  ElMessage.success('设置已保存')
}
</script>

<style scoped lang="scss">
.settings-container {
  padding: 20px;

  .settings-header {
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

  .settings-section {
    padding: 20px;

    .section-title {
      font-size: 18px;
      font-weight: 600;
      color: #303133;
      margin: 0 0 24px;
      padding-bottom: 12px;
      border-bottom: 2px solid #f0f0f0;
    }

    .form-tip {
      margin-left: 12px;
      font-size: 12px;
      color: #909399;
    }
  }

  .notification-list {
    .notification-item {
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
      }

      .notification-info {
        .notification-title {
          font-size: 15px;
          font-weight: 600;
          color: #303133;
          margin-bottom: 4px;
        }

        .notification-desc {
          font-size: 13px;
          color: #909399;
        }
      }
    }
  }

  .data-actions {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 20px;

    .action-card {
      text-align: center;
      padding: 24px;
      transition: all 0.3s ease;
      cursor: pointer;

      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      }

      .action-icon {
        font-size: 48px;
        margin-bottom: 16px;
      }

      h4 {
        margin: 0 0 8px;
        font-size: 16px;
        color: #303133;
      }

      p {
        margin: 0 0 16px;
        font-size: 13px;
        color: #909399;
      }
    }
  }

  .about-content {
    text-align: center;
    padding: 40px 20px;

    .about-logo {
      margin-bottom: 20px;
    }

    h2 {
      font-size: 28px;
      font-weight: 700;
      color: #303133;
      margin: 0 0 8px;
    }

    .version {
      font-size: 14px;
      color: #909399;
      margin-bottom: 32px;
    }

    .about-info {
      max-width: 500px;
      margin: 0 auto;
      text-align: left;

      .info-row {
        display: flex;
        padding: 12px 0;
        border-bottom: 1px solid #f0f0f0;

        .label {
          width: 120px;
          font-weight: 600;
          color: #606266;
        }

        .value {
          flex: 1;
          color: #303133;
        }

        &:last-child {
          border-bottom: none;
        }
      }
    }

    .about-links {
      margin-top: 32px;
      display: flex;
      justify-content: center;
      gap: 20px;
    }
  }

  :deep(.el-tabs--left .el-tabs__nav) {
    min-width: 150px;
  }
}
</style>

