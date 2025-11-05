import request from '@/utils/request'

/**
 * 获取商品列表
 */
export function getProductList(params) {
  return request({
    url: '/products/list',
    method: 'get',
    params
  })
}

/**
 * 获取商品详情
 */
export function getProductDetail(id) {
  return request({
    url: `/products/${id}`,
    method: 'get'
  })
}

/**
 * 创建商品
 */
export function createProduct(data) {
  return request({
    url: '/products',
    method: 'post',
    data
  })
}

/**
 * 更新商品
 */
export function updateProduct(data) {
  return request({
    url: '/products',
    method: 'put',
    data
  })
}

/**
 * 删除商品
 */
export function deleteProduct(id) {
  return request({
    url: `/products/${id}`,
    method: 'delete'
  })
}

/**
 * 修改商品状态
 */
export function updateProductStatus(id, status) {
  return request({
    url: `/products/${id}/status`,
    method: 'put',
    params: { status }
  })
}

/**
 * 获取所有商品（不分页）
 */
export function getAllProducts() {
  return request({
    url: '/products/all',
    method: 'get'
  })
}

export default {
  getList: getProductList,
  getDetail: getProductDetail,
  getAll: getAllProducts,
  create: createProduct,
  update: updateProduct,
  delete: deleteProduct,
  updateStatus: updateProductStatus
}

