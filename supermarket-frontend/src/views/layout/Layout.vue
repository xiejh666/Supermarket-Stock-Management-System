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
          
          <el-sub-menu index="product">
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
          <el-badge :value="notifications" :hidden="notifications === 0" class="notification-badge">
            <el-button :icon="Bell" circle />
          </el-badge>
          
          <!-- 用户信息 -->
          <el-dropdown @command="handleCommand">
            <div class="user-info">
              <el-avatar
                :size="40"
                src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png"
              />
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
  SwitchButton
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const isCollapsed = ref(false)
const notifications = ref(3)

const userInfo = computed(() => userStore.userInfo || {})
const activeMenu = computed(() => route.path)

const currentRoute = computed(() => {
  const routeMap = {
    '/dashboard': '首页仪表盘',
    '/category': '分类管理',
    '/product': '商品管理',
    '/supplier': '供应商管理',
    '/purchase': '采购管理',
    '/sale': '销售管理',
    '/inventory': '库存管理',
    '/user': '用户管理'
  }
  return routeMap[route.path]
})

const toggleSidebar = () => {
  isCollapsed.value = !isCollapsed.value
}

const handleMenuSelect = (index) => {
  router.push(index)
}

const handleCommand = async (command) => {
  switch (command) {
    case 'profile':
      ElMessage.info('个人中心功能开发中...')
      break
    case 'settings':
      ElMessage.info('系统设置功能开发中...')
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
}

:deep(.el-sub-menu .el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.1) !important;
  color: white;
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
