import { defineStore } from 'pinia'
import { getToken, setToken, removeToken } from '@/utils/auth'

// 从 localStorage 获取用户信息
const getUserInfoFromStorage = () => {
  try {
    const userInfoStr = localStorage.getItem('userInfo')
    return userInfoStr ? JSON.parse(userInfoStr) : null
  } catch (error) {
    console.error('解析用户信息失败:', error)
    return null
  }
}

// 保存用户信息到 localStorage
const saveUserInfoToStorage = (userInfo) => {
  try {
    localStorage.setItem('userInfo', JSON.stringify(userInfo))
  } catch (error) {
    console.error('保存用户信息失败:', error)
  }
}

// 从 localStorage 移除用户信息
const removeUserInfoFromStorage = () => {
  try {
    localStorage.removeItem('userInfo')
  } catch (error) {
    console.error('移除用户信息失败:', error)
  }
}

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken() || '',
    userInfo: getUserInfoFromStorage()
  }),
  
  actions: {
    setToken(token) {
      this.token = token
      setToken(token)
    },
    
    setUserInfo(userInfo) {
      this.userInfo = userInfo
      saveUserInfoToStorage(userInfo)
    },
    
    logout() {
      this.token = ''
      this.userInfo = null
      removeToken()
      removeUserInfoFromStorage()
    }
  }
})

