import request from '@/utils/request'

/**
 * 获取所有角色
 */
export function getRoleList() {
  return request({
    url: '/roles/list',
    method: 'get'
  })
}



