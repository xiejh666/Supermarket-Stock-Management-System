import request from '@/utils/request'

/**
 * 获取用户列表
 */
export function getUserList(params) {
  return request({
    url: '/users/list',
    method: 'get',
    params
  })
}

/**
 * 创建用户
 */
export function createUser(data) {
  return request({
    url: '/users',
    method: 'post',
    data
  })
}

/**
 * 更新用户
 */
export function updateUser(data) {
  return request({
    url: '/users',
    method: 'put',
    data
  })
}

/**
 * 删除用户
 */
export function deleteUser(id) {
  return request({
    url: `/users/${id}`,
    method: 'delete'
  })
}

/**
 * 修改用户状态
 */
export function updateUserStatus(id, status) {
  return request({
    url: `/users/${id}/status`,
    method: 'put',
    params: { status }
  })
}

/**
 * 重置密码
 */
export function resetPassword(id) {
  return request({
    url: `/users/${id}/reset-password`,
    method: 'put'
  })
}

export default {
  getList: getUserList,
  create: createUser,
  update: updateUser,
  delete: deleteUser,
  updateStatus: updateUserStatus,
  resetPassword: resetPassword
}

