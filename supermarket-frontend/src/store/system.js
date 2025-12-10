import { defineStore } from 'pinia'
import { getSettings } from '@/api/settings'

export const useSystemStore = defineStore('system', {
  state: () => ({
    systemName: '超市管理系统',
    systemDescription: ''
  }),
  
  actions: {
    // 设置系统名称
    setSystemName(name) {
      this.systemName = name
    },
    
    // 设置系统描述
    setSystemDescription(description) {
      this.systemDescription = description
    },
    
    // 从服务器加载系统设置
    async loadSystemSettings() {
      try {
        const res = await getSettings()
        if (res.code === 200) {
          if (res.data.systemName) {
            this.systemName = res.data.systemName
          }
          if (res.data.systemDescription) {
            this.systemDescription = res.data.systemDescription
          }
        }
      } catch (error) {
        console.error('加载系统设置失败:', error)
      }
    }
  }
})
