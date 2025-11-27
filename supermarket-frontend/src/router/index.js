import { createRouter, createWebHistory } from 'vue-router'
import { getToken, isTokenValid, removeToken } from '@/utils/auth'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: () => import('@/views/layout/Layout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/Dashboard.vue'),
        meta: { title: '首页仪表盘', requiresAuth: true }
      },
      {
        path: 'category',
        name: 'Category',
        component: () => import('@/views/category/Category.vue'),
        meta: { title: '分类管理', requiresAuth: true }
      },
      {
        path: 'product',
        name: 'Product',
        component: () => import('@/views/product/Product.vue'),
        meta: { title: '商品管理', requiresAuth: true }
      },
      {
        path: 'supplier',
        name: 'Supplier',
        component: () => import('@/views/supplier/Supplier.vue'),
        meta: { title: '供应商管理', requiresAuth: true }
      },
      {
        path: 'customer',
        name: 'Customer',
        component: () => import('@/views/customer/Customer.vue'),
        meta: { title: '客户管理', requiresAuth: true }
      },
      {
        path: 'purchase',
        name: 'Purchase',
        component: () => import('@/views/purchase/Purchase.vue'),
        meta: { title: '采购管理', requiresAuth: true }
      },
      {
        path: 'sale',
        name: 'Sale',
        component: () => import('@/views/sale/Sale.vue'),
        meta: { title: '销售管理', requiresAuth: true }
      },
      {
        path: 'inventory',
        name: 'Inventory',
        component: () => import('@/views/inventory/Inventory.vue'),
        meta: { title: '库存管理', requiresAuth: true }
      },
      {
        path: 'user',
        name: 'User',
        component: () => import('@/views/user/User.vue'),
        meta: { title: '用户管理', requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/profile/Profile.vue'),
        meta: { title: '个人中心', requiresAuth: true }
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('@/views/settings/Settings.vue'),
        meta: { title: '系统设置', requiresAuth: true }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = getToken()
  
  // 检查需要认证的页面
  if (to.meta.requiresAuth) {
    if (!token || !isTokenValid()) {
      // Token不存在或已过期，清除Token并跳转登录页
      removeToken()
      next('/login')
      return
    }
  }
  
  // 如果已登录且Token有效，访问登录页时跳转到首页
  if (to.path === '/login' && token && isTokenValid()) {
    next('/')
    return
  }
  
  next()
})

export default router

