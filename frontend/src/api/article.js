import request from '../utils/request'

// 文章
export const getArticleById = (id) => request.get(`/article/${id}`)
export const getArticlePage = (params) => request.get('/article/page', {params})
export const getArticlesByUserId = (userId) => request.get(`/article/user/${userId}`)
export const addArticle = (data) => request.post('/article', data)
export const updateArticle = (data) => request.put('/article', data)
export const deleteArticle = (id) => request.delete(`/article/${id}`)

// 版本
export const getVersionList = (articleId) => request.get(`/article/version/list/${articleId}`)
export const rollbackVersion = (versionId) => request.post(`/article/version/rollback/${versionId}`)

// 分类
export function getCategoryList() {
    return request.get('/article/category/list')
}

export function addCategory(data) {
    return request.post('/article/category', data)
}

export function deleteCategory(id) {
    return request.delete(`/article/category/${id}`)
}

// 点赞/取消点赞（切换状态）
export const likeArticle = (articleId) => {
    return request.post(`/article/like/${articleId}`)
}

// 获取文章评论列表（分页）
export const getCommentList = (articleId, params = {}) => {
    return request.get(`/article/comment/list/${articleId}`, { params })
}

// 发表评论
export const addComment = (data) => {
    return request.post('/article/comment/add', data)
}

// 删除评论
export const deleteComment = (commentId) => {
    return request.delete(`/article/comment/${commentId}`)
}
