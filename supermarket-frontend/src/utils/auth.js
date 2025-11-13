const TOKEN_KEY = 'supermarket_token'
const TOKEN_EXPIRE_KEY = 'supermarket_token_expire'

/**
 * 获取Token
 */
export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

/**
 * 设置Token和过期时间
 */
export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token)
  // 设置过期时间（15分钟后）
  const expireTime = Date.now() + 15 * 60 * 1000
  localStorage.setItem(TOKEN_EXPIRE_KEY, expireTime.toString())
}

/**
 * 清除Token
 */
export function removeToken() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(TOKEN_EXPIRE_KEY)
}

/**
 * 检查Token是否过期
 */
export function isTokenExpired() {
  const token = getToken()
  if (!token) {
    return true
  }
  
  const expireTime = localStorage.getItem(TOKEN_EXPIRE_KEY)
  if (!expireTime) {
    return true
  }
  
  return Date.now() > parseInt(expireTime)
}

/**
 * 检查Token是否有效（存在且未过期）
 */
export function isTokenValid() {
  return getToken() && !isTokenExpired()
}

