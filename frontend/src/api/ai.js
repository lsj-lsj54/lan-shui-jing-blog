import request from '../utils/request'

const LLM_TIMEOUT = 120000

export const searchKnowledge = (data) =>
    request.post('/ai/knowledge/search', data, { timeout: LLM_TIMEOUT })

export const getKnowledgePage = (params) =>
    request.get('/ai/knowledge/page', { params })

export const getKnowledgeById = (id) =>
    request.get(`/ai/knowledge/${id}`)

export const deleteKnowledge = (id) =>
    request.delete(`/ai/knowledge/${id}`)

export const sendAiMessage = (data) =>
    request.post('/ai/message', data, { timeout: LLM_TIMEOUT })

export const getAiMessagesByUser = (userId) =>
    request.get(`/ai/message/user/${userId}`)
