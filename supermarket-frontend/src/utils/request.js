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
        // 如果检查失败，继续执行请求（避免影响正常业务）
      }
    }
    
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
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
    // 如果是文件下载（blob类型），直接返回
    if (response.config.responseType === 'blob') {
      return response.data
    }
    
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
    // 如果是401错误，跳转登录页
    if (error.response && error.response.status === 401) {
      removeToken()
      ElMessage.warning('登录已过期，请重新登录')
      router.push('/login')
      return Promise.reject(error)
    }
    
    // 根据响应状态码提供更详细的错误提示
    let errorMessage = '请求失败'
    
    if (error.response) {
      const status = error.response.status
      const data = error.response.data
      
      // 优先使用后端返回的错误信息
      if (data && data.message) {
        errorMessage = data.message
      } else {
        // 根据状态码提供默认提示
        switch (status) {
          case 400:
            errorMessage = '请求参数错误'
            break
          case 403:
            errorMessage = '没有权限访问该资源'
            break
          case 404:
            errorMessage = '请求的资源不存在'
            break
          case 413:
            errorMessage = '上传文件过大'
            break
          case 500:
            errorMessage = '服务器内部错误'
            break
          case 502:
            errorMessage = '网关错误'
            break
          case 503:
            errorMessage = '服务不可用'
            break
          case 504:
            errorMessage = '网关超时'
            break
          default:
            errorMessage = `请求失败（错误码: ${status}）`
        }
      }
    } else if (error.request) {
      // 请求已发送但没有收到响应
      errorMessage = '网络连接失败，请检查网络'
    } else {
      // 请求配置错误
      errorMessage = error.message || '请求失败'
    }
    
    ElMessage.error(errorMessage)
    return Promise.reject(error)
  }
)

export default service

