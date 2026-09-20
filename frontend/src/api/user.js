import request from '../utils/request'

// 登录（返回 { token, user }）
export const login = (data) => {
    return request({
        url: '/user/login',
        method: 'post',
        data
    })
}

// 注册
export const register = (data) => {
    return request({
        url: '/user/register',
        method: 'post',
        data
    })
}

export const getUserById = (id) => {
    return request({
        url: `/user/${id}`,
        method: 'get'
    })
}

export const getUserPage = (params) => {
    return request({
        url: '/user/page',
        method: 'get',
        params
    })
}

export const addUser = (data) => {
    return request({
        url: '/user',
        method: 'post',
        data
    })
}

export const updateUser = (data) => {
    return request({
        url: '/user',
        method: 'put',
        data
    })
}

export const deleteUser = (id) => {
    return request({
        url: `/user/${id}`,
        method: 'delete'
    })
}
