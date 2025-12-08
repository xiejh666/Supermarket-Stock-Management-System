import request from '@/utils/request'

/**
 * 获取当前用户信息
 * @returns {Promise}
 */
export function getUserProfile() {
  return request({
    url: '/user/profile',
    method: 'get'
  })
}

/**
 * 更新个人资料
 * @param {Object} data 个人资料数据
 * @returns {Promise}
 */
export function updateProfile(data) {
  return request({
    url: '/user/profile',
    method: 'put',
    data
  })
}

/**
 * 修改密码
 * @param {Object} data 密码数据
 * @returns {Promise}
 */
export function changePassword(data) {
  return request({
    url: '/user/profile/password',
    method: 'put',
    data
  })
}

/**
 * 更新用户头像
 * @param {string} avatarUrl 头像URL
 * @returns {Promise}
 */
export function updateAvatar(avatarUrl) {
  return request({
    url: '/user/profile/avatar',
    method: 'put',
    params: { avatarUrl }
  })
}

// 默认导出
export default {
  getUserProfile,
  updateProfile,
  changePassword,
  updateAvatar
}


