import request from '@/utils/request'

/**
 * 上传用户头像
 * @param {File} file 图片文件
 * @returns {Promise}
 */
export function uploadAvatar(file) {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url: '/upload/avatar',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 上传商品图片
 * @param {File} file 图片文件
 * @returns {Promise}
 */
export function uploadProductImage(file) {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url: '/upload/product',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 删除文件
 * @param {string} fileUrl 文件URL
 * @returns {Promise}
 */
export function deleteFile(fileUrl) {
  return request({
    url: '/upload',
    method: 'delete',
    params: { url: fileUrl }
  })
}
