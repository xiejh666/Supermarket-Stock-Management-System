import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'

/**
 * 权限控制工具
 */

// 角色权限配置
const ROLE_PERMISSIONS = {
  ADMIN: {
    // 管理员拥有所有权限
    canView: ['*'],
    canCreate: ['*'],
    canUpdate: ['*'],
    canDelete: ['*'],
    canAudit: ['*'],
    canInbound: ['*'],  // 管理员可以入库
    canOutbound: ['*']  // 管理员可以出库
  },
  PURCHASER: {
    // 采购员权限：可以管理供应商、采购、库存入库
    canView: ['dashboard', 'category', 'product', 'supplier', 'purchase', 'inventory'],
    canCreate: ['purchase', 'supplier', 'inventory_inbound'],
    canUpdate: ['purchase', 'supplier', 'inventory_inbound'],
    canDelete: [],
    canAudit: [],
    canInbound: ['inventory'], // 可以入库
    canOutbound: [] // 不能出库
  },
  SALESPERSON: {
    // 销售员权限：可以管理客户、销售、库存出库
    canView: ['dashboard', 'category', 'product', 'customer', 'sale', 'inventory'],
    canCreate: ['sale', 'customer', 'inventory_outbound'],
    canUpdate: ['sale', 'customer', 'inventory_outbound'],
    canDelete: [],
    canAudit: [],
    canInbound: [], // 不能入库
    canOutbound: ['inventory'] // 可以出库
  },
  CASHIER: {
    // 营业员权限：可以管理客户、销售、库存出库（与销售员相同）
    canView: ['dashboard', 'category', 'product', 'customer', 'sale', 'inventory'],
    canCreate: ['sale', 'customer', 'inventory_outbound'],
    canUpdate: ['sale', 'customer', 'inventory_outbound'],
    canDelete: [],
    canAudit: [],
    canInbound: [], // 不能入库
    canOutbound: ['inventory'] // 可以出库
  }
}

/**
 * 检查用户是否有指定权限
 * @param {string} action 操作类型：view, create, update, delete, audit
 * @param {string} resource 资源名称：dashboard, user, category, product等
 * @returns {boolean} 是否有权限
 */
export function hasPermission(action, resource) {
  const userStore = useUserStore()
  const userInfo = userStore.userInfo
  
  if (!userInfo || !userInfo.roleCode) {
    return false
  }
  
  const roleCode = userInfo.roleCode
  const permissions = ROLE_PERMISSIONS[roleCode]
  
  if (!permissions) {
    return false
  }
  
  const actionKey = `can${action.charAt(0).toUpperCase() + action.slice(1)}`
  const allowedResources = permissions[actionKey] || []
  
  // 如果包含通配符 * 表示拥有所有权限
  if (allowedResources.includes('*')) {
    return true
  }
  
  // 检查是否包含指定资源
  return allowedResources.includes(resource)
}

/**
 * 检查是否可以查看
 */
export function canView(resource) {
  return hasPermission('view', resource)
}

/**
 * 检查是否可以创建
 */
export function canCreate(resource) {
  return hasPermission('create', resource)
}

/**
 * 检查是否可以更新
 */
export function canUpdate(resource) {
  return hasPermission('update', resource)
}

/**
 * 检查是否可以删除
 */
export function canDelete(resource) {
  return hasPermission('delete', resource)
}

/**
 * 检查是否可以审核
 */
export function canAudit(resource) {
  return hasPermission('audit', resource)
}

/**
 * 检查是否可以入库
 */
export function canInbound(resource) {
  return hasPermission('inbound', resource)
}

/**
 * 检查是否可以出库
 */
export function canOutbound(resource) {
  return hasPermission('outbound', resource)
}

/**
 * 权限检查装饰器，用于按钮点击等操作
 * @param {string} action 操作类型
 * @param {string} resource 资源名称
 * @param {Function} callback 有权限时执行的回调
 * @param {string} message 无权限时的提示信息
 */
export function checkPermission(action, resource, callback, message = '暂无权限，如有需要请联系管理员') {
  if (hasPermission(action, resource)) {
    callback()
  } else {
    ElMessage.warning(message)
  }
}

/**
 * 获取当前用户角色
 */
export function getCurrentUserRole() {
  const userStore = useUserStore()
  return userStore.userInfo?.roleCode || null
}

/**
 * 是否为管理员
 */
export function isAdmin() {
  return getCurrentUserRole() === 'ADMIN'
}

/**
 * 是否为采购员
 */
export function isPurchaser() {
  return getCurrentUserRole() === 'PURCHASER'
}

/**
 * 是否为销售员
 */
export function isSalesperson() {
  return getCurrentUserRole() === 'SALESPERSON'
}

/**
 * 是否为营业员
 */
export function isCashier() {
  return getCurrentUserRole() === 'CASHIER'
}

/**
 * 获取角色中文名称
 */
export function getRoleDisplayName(roleCode) {
  const roleNames = {
    ADMIN: '管理员',
    PURCHASER: '采购员',
    SALESPERSON: '销售员',
    CASHIER: '营业员'
  }
  return roleNames[roleCode] || '未知角色'
}
