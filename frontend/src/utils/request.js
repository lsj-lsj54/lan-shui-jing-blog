import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

const request = axios.create({
    baseURL: '/api',
    timeout: 10000
})

// Token 存储 key
const TOKEN_KEY = 'token'
const USER_KEY = 'user'

// 统一的 Token 管理
export const getToken = () => localStorage.getItem(TOKEN_KEY)
export const setToken = (token) => localStorage.setItem(TOKEN_KEY, token)
export const removeToken = () => localStorage.removeItem(TOKEN_KEY)
export const getUser = () => {
    const userStr = localStorage.getItem(USER_KEY)
    return userStr ? JSON.parse(userStr) : null
}
export const setUser = (user) => localStorage.setItem(USER_KEY, JSON.stringify(user))
export const removeUser = () => localStorage.removeItem(USER_KEY)

// 清除登录状态并跳转登录页
export const clearAuthAndRedirect = () => {
    removeToken()
    removeUser()
    router.push('/login')
}

// 请求拦截器：自动携带 Token
request.interceptors.request.use(
    (config) => {
        const token = getToken()
        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`
        }
        return config
    },
    (error) => Promise.reject(error)
)

// 响应拦截器：统一处理返回和错误
request.interceptors.response.use(
    (response) => {
        const res = response.data
        // 后端成功 code=1
        if (res.code === 1) {
            return res
        } else {
            ElMessage.error(res.msg || '请求失败')
            return Promise.reject(new Error(res.msg || '请求失败'))
        }
    },
    (error) => {
        // 401 未授权：Token 无效或过期
        if (error.response?.status === 401) {
            ElMessage.warning('登录已过期，请重新登录')
            clearAuthAndRedirect()
        } else {
            ElMessage.error(error.response?.data?.msg || error.message || '网络错误')
        }
        return Promise.reject(error)
    }
)

export default request
