import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import router from '@/router'
import request from '@/utils/request'

/**
 * 全局用户状态检查工具
 */
class UserStatusChecker {
  constructor() {
    this.isChecking = false // 防止重复检查
    this.lastCheckTime = 0 // 上次检查时间
    this.checkInterval = 30000 // 检查间隔：30秒
  }

  /**
   * 检查当前用户状态
   * @param {boolean} force 是否强制检查（忽略时间间隔）
   * @returns {Promise<boolean>} true-用户状态正常，false-用户已被禁用
   */
  async checkUserStatus(force = false) {
    const userStore = useUserStore()
    const currentUser = userStore.userInfo
    
    // 如果没有用户信息，直接返回false
    if (!currentUser) {
      return false
    }

    const now = Date.now()
    
    // 如果不是强制检查且距离上次检查时间不足间隔时间，跳过检查
    if (!force && (now - this.lastCheckTime) < this.checkInterval) {
      return true
    }

    // 如果正在检查中，等待检查完成
    if (this.isChecking) {
      return new Promise((resolve) => {
        const checkInterval = setInterval(() => {
          if (!this.isChecking) {
            clearInterval(checkInterval)
            resolve(true)
          }
        }, 100)
      })
    }

    this.isChecking = true
    this.lastCheckTime = now

    try {
      const currentUserId = currentUser.userId || currentUser.id
      
      // 调用后端接口检查用户状态
      const response = await request.get(`/users/${currentUserId}/status`)
      
      if (response.code === 200) {
        const userStatus = response.data.status
        
        // 如果用户被禁用
        if (userStatus === 0) {
          await this.handleUserDisabled()
          return false
        }
        
        return true
      } else {
        console.warn('检查用户状态失败:', response.message)
        return true // 检查失败时不强制退出
      }
    } catch (error) {
      console.error('检查用户状态出错:', error)
      
      // 如果是401错误，说明token无效
      if (error.response?.status === 401) {
        await this.handleTokenInvalid()
        return false
      }
      
      return true // 其他错误不强制退出
    } finally {
      this.isChecking = false
    }
  }

  /**
   * 处理用户被禁用的情况
   */
  async handleUserDisabled() {
    try {
      await ElMessageBox.alert(
        '您的账号已被管理员禁用，即将退出登录',
        '账号已禁用',
        {
          confirmButtonText: '确定',
          type: 'warning',
          showClose: false,
          closeOnClickModal: false,
          closeOnPressEscape: false
        }
      )
    } catch (error) {
      // 用户可能关闭了弹窗，继续执行退出逻辑
    }

    await this.forceLogout('账号已被禁用')
  }

  /**
   * 处理Token无效的情况
   */
  async handleTokenInvalid() {
    await this.forceLogout('登录已过期，请重新登录')
  }

  /**
   * 强制退出登录
   * @param {string} message 提示信息
   */
  async forceLogout(message) {
    const userStore = useUserStore()
    
    try {
      await userStore.logout()
      ElMessage.warning(message)
      
      // 延迟跳转，确保用户看到提示
      setTimeout(() => {
        router.push('/login')
      }, 1500)
    } catch (error) {
      console.error('退出登录失败:', error)
      // 即使退出失败也要跳转到登录页
      router.push('/login')
    }
  }

  /**
   * 在关键操作前进行状态检查
   * @param {string} operationName 操作名称（用于日志）
   * @returns {Promise<boolean>} true-可以继续操作，false-应该停止操作
   */
  async checkBeforeOperation(operationName = '操作') {
    console.log(`执行${operationName}前检查用户状态`)
    
    const isValid = await this.checkUserStatus()
    
    if (!isValid) {
      console.log(`${operationName}被阻止：用户状态异常`)
      return false
    }
    
    return true
  }

  /**
   * 开始定期检查用户状态
   */
  startPeriodicCheck() {
    // 每30秒检查一次用户状态
    setInterval(() => {
      this.checkUserStatus(false)
    }, this.checkInterval)
    
    console.log('已启动用户状态定期检查')
  }
}

// 创建全局实例
const userStatusChecker = new UserStatusChecker()

export default userStatusChecker
