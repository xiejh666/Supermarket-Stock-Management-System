import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, removeToken, isTokenValid } from '@/utils/auth'
import router from '@/router'
import userStatusChecker from '@/utils/userStatusCheck'

const service = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器
service.interceptors.request.use(
  async config => {
    const token = getToken()
    
    // 检查Token是否过期
    if (token && !isTokenValid()) {
      // Token已过期，清除并跳转登录页
      removeToken()
      ElMessage.warning('登录已过期，请重新登录')
      router.push('/login')
      return Promise.reject(new Error('Token已过期'))
    }
    
    // 如果有token且不是登录、退出登录或用户状态检查接口，则检查用户状态
    if (token && !isAuthRelatedRequest(config)) {
      try {
        const isUserValid = await userStatusChecker.checkUserStatus()
        if (!isUserValid) {
          return Promise.reject(new Error('用户状态异常'))
        }
      } catch (error) {
        console.error('用户状态检查失败:', error)
        // 如果检查失败，继续执行请求（避免影响正常业务）
      }
    }
    
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 判断是否为认证相关请求（这些请求不需要检查用户状态）
function isAuthRelatedRequest(config) {
  const authPaths = [
    '/auth/login',
    '/auth/logout',
    '/users/',  // 用户状态检查接口
  ]
  
  return authPaths.some(path => config.url.includes(path))
}

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data
    
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      
      // Token过期或无效，跳转登录页
      if (res.code === 401) {
        removeToken()
        router.push('/login')
      }
      
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    
    return res
  },
  error => {
    console.error('响应错误:', error)
    ElMessage.error(error.message || '请求失败')
    return Promise.reject(error)
  }
)

export default service

