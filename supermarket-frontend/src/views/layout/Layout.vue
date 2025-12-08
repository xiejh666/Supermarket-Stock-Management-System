<template>
  <div class="layout-container" :class="{ 'sidebar-collapsed': isCollapsed }">
    <!-- 侧边栏 -->
    <aside class="sidebar">
      <div class="sidebar-header">
        <transition name="fade">
          <div v-if="!isCollapsed" class="logo">
            <i class="el-icon-shopping-cart-2"></i>
            <span class="logo-text gradient-text">超市管理系统</span>
          </div>
          <i v-else class="el-icon-shopping-cart-2 logo-icon-small"></i>
        </transition>
      </div>
      
      <el-scrollbar class="sidebar-menu-container">
        <el-menu
          :default-active="activeMenu"
          :collapse="isCollapsed"
          :collapse-transition="true"
          class="sidebar-menu"
          @select="handleMenuSelect"
        >
          <el-menu-item index="/dashboard">
            <el-icon><HomeFilled /></el-icon>
            <template #title>首页仪表盘</template>
          </el-menu-item>
          
          <el-sub-menu index="product-menu">
            <template #title>
              <el-icon><Goods /></el-icon>
              <span>商品管理</span>
            </template>
            <el-menu-item index="/category">分类管理</el-menu-item>
            <el-menu-item index="/product">商品管理</el-menu-item>
          </el-sub-menu>
          
          <el-menu-item index="/supplier">
            <el-icon><OfficeBuilding /></el-icon>
            <template #title>供应商管理</template>
          </el-menu-item>
          
          <el-menu-item index="/customer">
            <el-icon><User /></el-icon>
            <template #title>客户管理</template>
          </el-menu-item>
          
          <el-menu-item index="/purchase">
            <el-icon><ShoppingCart /></el-icon>
            <template #title>采购管理</template>
          </el-menu-item>
          
          <el-menu-item index="/sale">
            <el-icon><ShoppingBag /></el-icon>
            <template #title>销售管理</template>
          </el-menu-item>
          
          <el-menu-item index="/inventory">
            <el-icon><Box /></el-icon>
            <template #title>库存管理</template>
          </el-menu-item>
          
          <el-menu-item index="/user" v-if="userInfo.roleCode === 'ADMIN'">
            <el-icon><User /></el-icon>
            <template #title>用户管理</template>
          </el-menu-item>
        </el-menu>
      </el-scrollbar>
      
      <div class="sidebar-footer">
        <el-button
          :icon="isCollapsed ? Expand : Fold"
          circle
          @click="toggleSidebar"
          class="collapse-btn"
        />
      </div>
    </aside>
    
    <!-- 主内容区 -->
    <div class="main-container">
      <!-- 顶部导航栏 -->
      <header class="header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentRoute">{{ currentRoute }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        
        <div class="header-right">
          <!-- 通知 -->
          <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="notification-badge">
            <el-button :icon="Bell" circle @click="showNotifications = true" />
          </el-badge>
          
          <!-- 用户信息 -->
          <el-dropdown @command="handleCommand">
            <div class="user-info">
              <el-avatar
                :size="40"
                :src="userInfo.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'"
              >
                <el-icon><User /></el-icon>
              </el-avatar>
              <div class="user-details" v-if="!isCollapsed">
                <div class="user-name">{{ userInfo.realName || userInfo.username }}</div>
                <div class="user-role">{{ userInfo.roleName }}</div>
              </div>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  个人中心
                </el-dropdown-item>
                <el-dropdown-item command="settings">
                  <el-icon><Setting /></el-icon>
                  系统设置
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>
      
      <!-- 页面内容 -->
      <main class="content">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <keep-alive>
              <component :is="Component" />
            </keep-alive>
          </transition>
        </router-view>
      </main>
    </div>

    <!-- 消息通知抽屉 -->
    <el-drawer
      v-model="showNotifications"
      title="消息通知"
      direction="rtl"
      size="400px"
    >
      <div class="notifications-container">
        <!-- 消息筛选 -->
        <el-tabs v-model="notificationTab" class="notification-tabs">
          <el-tab-pane label="全部" name="all">
            <div class="notification-list">
              <div
                v-for="(item, index) in filteredNotifications"
                :key="item.id"
                class="notification-item"
                :class="{ 'unread': item.isRead === 0 }"
                @click="markAsReadHandler(item)"
              >
                <div class="notification-icon" :class="item.type">
                  <el-icon>
                    <component :is="getNotificationIcon(item.type)" />
                  </el-icon>
                </div>
                <div class="notification-content">
                  <div class="notification-title">{{ item.title }}</div>
                  <div class="notification-desc">{{ item.content }}</div>
                  <div class="notification-time">{{ formatTime(item.createTime) }}</div>
                </div>
                <el-icon v-if="item.isRead === 0" class="unread-dot" color="#409eff">
                  <SuccessFilled />
                </el-icon>
              </div>
              <el-empty v-if="filteredNotifications.length === 0" description="暂无消息" />
            </div>
          </el-tab-pane>
          <el-tab-pane label="未读" name="unread">
            <div class="notification-list">
              <div
                v-for="(item, index) in unreadNotifications"
                :key="item.id"
                class="notification-item unread"
                @click="markAsReadHandler(item)"
              >
                <div class="notification-icon" :class="item.type">
                  <el-icon>
                    <component :is="getNotificationIcon(item.type)" />
                  </el-icon>
                </div>
                <div class="notification-content">
                  <div class="notification-title">{{ item.title }}</div>
                  <div class="notification-desc">{{ item.content }}</div>
                  <div class="notification-time">{{ formatTime(item.createTime) }}</div>
                </div>
                <el-icon class="unread-dot" color="#409eff">
                  <SuccessFilled />
                </el-icon>
              </div>
              <el-empty v-if="unreadNotifications.length === 0" description="暂无未读消息" />
            </div>
          </el-tab-pane>
        </el-tabs>
        
        <!-- 底部操作 -->
        <div class="notification-footer">
          <el-button type="primary" text @click="markAllAsReadHandler">全部已读</el-button>
          <el-button type="danger" text @click="clearAll">清空消息</el-button>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  HomeFilled,
  Goods,
  OfficeBuilding,
  ShoppingCart,
  ShoppingBag,
  Box,
  User,
  Expand,
  Fold,
  Bell,
  Setting,
  SwitchButton,
  SuccessFilled,
  Warning,
  InfoFilled,
  CircleCheck
} from '@element-plus/icons-vue'
import { 
  getNotifications, 
  getUnreadCount, 
  markAsRead, 
  markAllAsRead, 
  getRecentActivities 
} from '@/api/notification'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const isCollapsed = ref(false)
const showNotifications = ref(false)
const notificationTab = ref('all')

// 消息通知数据（从API动态获取）
const notifications = ref([])
const unreadCountValue = ref(0)

const userInfo = computed(() => userStore.userInfo || {})
const activeMenu = computed(() => route.path)
const unreadCount = computed(() => unreadCountValue.value)
const filteredNotifications = computed(() => notifications.value)
const unreadNotifications = computed(() => notifications.value.filter(n => n.isRead === 0))

const currentRoute = computed(() => {
  const routeMap = {
    '/dashboard': '首页仪表盘',
    '/category': '分类管理',
    '/product': '商品管理',
    '/supplier': '供应商管理',
    '/customer': '客户管理',
    '/purchase': '采购管理',
    '/sale': '销售管理',
    '/inventory': '库存管理',
    '/user': '用户管理',
    '/profile': '个人中心',
    '/settings': '系统设置'
  }
  return routeMap[route.path]
})

const toggleSidebar = () => {
  isCollapsed.value = !isCollapsed.value
}

const handleMenuSelect = (index) => {
  router.push(index)
}

// 格式化时间显示
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour
  
  if (diff < minute) {
    return '刚刚'
  } else if (diff < hour) {
    return Math.floor(diff / minute) + '分钟前'
  } else if (diff < day) {
    return Math.floor(diff / hour) + '小时前'
  } else if (diff < 2 * day) {
    return '昨天 ' + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  } else {
    return date.toLocaleString('zh-CN', { 
      month: '2-digit', 
      day: '2-digit', 
      hour: '2-digit', 
      minute: '2-digit' 
    })
  }
}

// 获取通知图标
const getNotificationIcon = (type) => {
  const iconMap = {
    PURCHASE_AUDIT: Warning,        // 采购审核 - 警告图标
    PURCHASE_APPROVED: CircleCheck, // 采购审核通过 - 成功图标
    PURCHASE_REJECTED: Warning,     // 采购审核拒绝 - 警告图标
    PURCHASE_OPERATION: InfoFilled, // 采购操作 - 信息图标
    SUPPLIER_OPERATION: InfoFilled, // 供应商操作 - 信息图标
    CUSTOMER_OPERATION: InfoFilled, // 客户操作 - 信息图标
    SALE_OPERATION: InfoFilled,     // 销售操作 - 信息图标
    PRODUCT_OPERATION: InfoFilled,  // 商品操作 - 信息图标
    CATEGORY_OPERATION: InfoFilled, // 分类操作 - 信息图标
    INVENTORY_OPERATION: InfoFilled, // 库存操作 - 信息图标
    SALE_PAYMENT: CircleCheck,      // 销售支付 - 成功图标
    SYSTEM: InfoFilled             // 系统通知 - 信息图标
  }
  return iconMap[type] || InfoFilled
}

// 获取消息列表
const fetchMessages = async () => {
  try {
    const userId = userStore.userInfo?.userId || userStore.userInfo?.id
    if (!userId) {
      console.warn('用户ID不存在，无法获取通知')
      return
    }
    const res = await getNotifications({
      userId,
      current: 1,
      size: 50
    })
    if (res.code === 200) {
      notifications.value = res.data.records || []
    }
  } catch (error) {
    console.error('获取消息列表失败:', error)
  }
}

// 获取未读消息数量
const fetchUnreadCount = async () => {
  try {
    const userId = userStore.userInfo?.userId || userStore.userInfo?.id
    if (!userId) {
      console.warn('用户ID不存在，无法获取未读数量')
      return
    }
    const res = await getUnreadCount(userId)
    if (res.code === 200) {
      unreadCountValue.value = res.data || 0
    }
  } catch (error) {
    console.error('获取未读消息数量失败:', error)
  }
}

// 标记消息为已读（并处理跳转）
const markAsReadHandler = async (notification) => {
  // 如果消息未读，标记为已读
  if (notification.isRead === 0) {
    try {
      const userId = userStore.userInfo?.userId || userStore.userInfo?.id
      await markAsRead(notification.id, userId)
      notification.isRead = 1
      unreadCountValue.value = Math.max(0, unreadCountValue.value - 1)
    } catch (error) {
      console.error('标记消息为已读失败:', error)
      ElMessage.error('操作失败')
      return
    }
  }
  
  // 根据业务类型跳转到对应页面
  if (notification.businessType === 'PURCHASE_ORDER') {
    showNotifications.value = false // 关闭抽屉
    router.push('/purchase')
    ElMessage.success('已跳转到采购管理页面')
  } else if (notification.businessType === 'SALE_ORDER') {
    showNotifications.value = false // 关闭抽屉
    router.push('/sale')
    ElMessage.success('已跳转到销售管理页面')
  }
}

// 全部标记为已读
const markAllAsReadHandler = async () => {
  try {
    const userId = userStore.userInfo?.userId || userStore.userInfo?.id
    await markAllAsRead(userId)
    notifications.value.forEach(n => n.isRead = 1)
    unreadCountValue.value = 0
    ElMessage.success('已全部标记为已读')
  } catch (error) {
    console.error('标记全部已读失败:', error)
    ElMessage.error('操作失败')
  }
}

// 清空所有消息
const clearAll = () => {
  ElMessageBox.confirm('确定要清空所有消息吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await clearAllMessages()
      notifications.value = []
      unreadCountValue.value = 0
      ElMessage.success('已清空所有消息')
    } catch (error) {
      console.error('清空消息失败:', error)
      ElMessage.error('操作失败')
    }
  }).catch(() => {})
}

const handleCommand = async (command) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'settings':
      router.push('/settings')
      break
    case 'logout':
      try {
        await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        await userStore.logout()
        router.push('/login')
        ElMessage.success('已退出登录')
      } catch (error) {
        // 取消操作
      }
      break
  }
}

onMounted(() => {
  // 检查是否有用户信息
  if (!userInfo.value || !userInfo.value.username) {
    router.push('/login')
  }
  
  // 获取消息列表和未读数量
  fetchMessages()
  fetchUnreadCount()
  
  // 定时刷新未读消息数量（每30秒）
  setInterval(() => {
    fetchUnreadCount()
  }, 30000)
})
</script>

<style scoped>
.layout-container {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

/* 侧边栏 */
.sidebar {
  width: var(--sidebar-width);
  background: linear-gradient(180deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
  position: relative;
  z-index: 100;
}

.sidebar-collapsed .sidebar {
  width: var(--sidebar-collapsed-width);
}

.sidebar-header {
  height: var(--header-height);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 20px;
  font-weight: 700;
  animation: fadeInDown 0.5s ease;
}

.logo i {
  font-size: 32px;
}

.logo-text {
  background: linear-gradient(135deg, #fff 0%, #e0e7ff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.logo-icon-small {
  font-size: 32px;
  animation: zoomIn 0.3s ease;
}

.sidebar-menu-container {
  flex: 1;
  overflow-y: auto;
}

.sidebar-menu {
  border-right: none;
  background: transparent;
}

:deep(.el-menu-item),
:deep(.el-sub-menu__title) {
  color: rgba(255, 255, 255, 0.8);
  transition: all 0.3s ease;
  border-radius: 8px;
  margin: 4px 8px;
}

:deep(.el-menu-item:hover),
:deep(.el-sub-menu__title:hover) {
  background: rgba(255, 255, 255, 0.15) !important;
  color: white;
}

:deep(.el-menu-item.is-active) {
  background: rgba(255, 255, 255, 0.2) !important;
  color: white;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

:deep(.el-sub-menu .el-menu-item) {
  background: transparent;
  color: rgba(255, 255, 255, 0.7);
  min-height: 40px;
  line-height: 40px;
  padding-left: 48px !important;
}

:deep(.el-sub-menu .el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.1) !important;
  color: white;
}

:deep(.el-sub-menu .el-menu-item.is-active) {
  background: rgba(255, 255, 255, 0.2) !important;
  color: white;
  font-weight: 600;
}

:deep(.el-sub-menu__title) {
  color: rgba(255, 255, 255, 0.8) !important;
}

:deep(.el-sub-menu.is-opened .el-sub-menu__title) {
  color: white !important;
}

:deep(.el-menu--inline) {
  background-color: rgba(0, 0, 0, 0.1) !important;
}

.sidebar-footer {
  padding: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  justify-content: center;
}

.collapse-btn {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
  transition: all 0.3s ease;
}

.collapse-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: scale(1.1);
}

/* 主内容区 */
.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 顶部栏 */
.header {
  height: var(--header-height);
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  position: relative;
  z-index: 90;
}

.header-left :deep(.el-breadcrumb__inner) {
  color: var(--text-primary);
  font-weight: 500;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.notification-badge {
  cursor: pointer;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 12px;
  transition: all 0.3s ease;
}

.user-info:hover {
  background: var(--bg-color);
}

.user-details {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.user-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.user-role {
  font-size: 12px;
  color: var(--text-secondary);
}

/* 内容区 */
.content {
  flex: 1;
  overflow-y: auto;
  background: var(--bg-color);
  padding: 24px;
}

/* 过渡动画 */
.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.3s ease;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-30px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(30px);
}

/* 消息通知样式 */
.notifications-container {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.notification-tabs {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.notification-tabs :deep(.el-tabs__content) {
  flex: 1;
  overflow-y: auto;
}

.notification-list {
  padding: 8px 0;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: #fafafa;
}

.notification-item:hover {
  background: #f0f0f0;
  transform: translateX(-4px);
}

.notification-item.unread {
  background: #e6f7ff;
  border-left: 3px solid #409eff;
}

.notification-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}

.notification-icon.success {
  background: #f0f9ff;
  color: #67c23a;
}

.notification-icon.warning {
  background: #fef0f0;
  color: #e6a23c;
}

.notification-icon.info {
  background: #f4f4f5;
  color: #909399;
}

.notification-icon.error {
  background: #fef0f0;
  color: #f56c6c;
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.notification-desc {
  font-size: 13px;
  color: #606266;
  line-height: 1.5;
  margin-bottom: 4px;
  word-break: break-all;
}

.notification-time {
  font-size: 12px;
  color: #909399;
}

.unread-dot {
  flex-shrink: 0;
  font-size: 8px;
}

.notification-footer {
  padding: 16px;
  border-top: 1px solid #ebeef5;
  display: flex;
  justify-content: space-around;
  background: white;
}

/* 响应式 */
@media (max-width: 768px) {
  .sidebar {
    position: fixed;
    left: 0;
    top: 0;
    bottom: 0;
    z-index: 1000;
  }
  
  .user-details {
    display: none;
  }
}
</style>
