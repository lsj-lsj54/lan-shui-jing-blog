<template>
  <div class="detail fade-in">
    <!-- 加载中 -->
    <div v-if="loading" class="detail-loading">
      <el-icon class="loading-icon" :size="48"><Loading /></el-icon>
      <p>文章加载中...</p>
    </div>

    <!-- 加载失败 -->
    <div v-else-if="loadError" class="detail-error">
      <div class="error-icon">⚠️</div>
      <h3>文章加载失败</h3>
      <p>{{ loadError }}</p>
      <el-button type="primary" @click="loadArticle">重新加载</el-button>
      <el-button @click="$router.back()">返回首页</el-button>
    </div>

    <!-- 文章内容 -->
    <div v-else-if="article">
    <div class="action-bar">
      <el-button class="back-btn" @click="$router.back()">
        <el-icon><ArrowLeft /></el-icon> 返回
      </el-button>
      <div class="action-right" v-if="String(article.userId) === String(currentUser?.id)">
        <el-button type="primary" @click="goEdit" class="action-btn">
          <el-icon><Edit /></el-icon> 编辑
        </el-button>
        <el-button @click="openVersionDialog" class="action-btn">
          <el-icon><Clock /></el-icon> 历史版本
        </el-button>
        <el-button type="danger" @click="handleDelete" class="action-btn">
          <el-icon><Delete /></el-icon> 删除
        </el-button>
      </div>
    </div>

    <el-card class="detail-card">
      <h1 class="article-title">{{ article.title }}</h1>
      <div class="article-meta">
        <span class="meta-item">
          <el-icon><User /></el-icon>
          {{ article.author || '匿名' }}
        </span>
        <span class="meta-item" v-if="categoryName">
          <el-icon><Folder /></el-icon>
          {{ categoryName }}
        </span>
        <span class="meta-item">
          <el-icon><View /></el-icon>
          {{ article.viewCount || 0 }}
        </span>
        <span class="meta-item">
          <el-icon><Star /></el-icon>
          {{ likeCount }}
        </span>
        <span class="meta-item">
          <el-icon><Clock /></el-icon>
          {{ formatTime(article.createTime) }}
        </span>
      </div>
      <div class="tag-list" v-if="article.tags">
        <span v-for="t in tagArray" :key="t" class="tag-chip">{{ t }}</span>
      </div>
      <div class="divider"></div>
      <MdPreview :modelValue="article.content" theme="dark" previewTheme="github" class="article-content"/>

      <!-- 点赞按钮区 -->
      <div class="divider"></div>
      <div class="like-wrapper">
        <button
            class="like-btn"
            :class="{ liked: isLiked }"
            @click="handleLike"
            :disabled="likeLoading"
        >
          <span class="like-icon" :class="{ animate: isLiked }">
            {{ isLiked ? '❤️' : '🤍' }}
          </span>
          <span class="like-text">{{ isLiked ? '已点赞' : '点个赞' }}</span>
          <span class="like-count">{{ likeCount }}</span>
        </button>
      </div>

      <!-- 评论区 -->
      <div class="divider"></div>
      <div class="comment-section">
        <h3 class="comment-title">
          <el-icon><ChatDotRound /></el-icon>
          评论 <span class="comment-count-num">{{ commentTotal }}</span>
        </h3>

        <!-- 发表评论输入框 -->
        <div class="comment-input-box">
          <el-input
              v-model="commentContent"
              type="textarea"
              :rows="3"
              :placeholder="isLoggedIn ? '写下你的评论...' : '请先登录后发表评论'"
              :disabled="!isLoggedIn"
              maxlength="500"
              show-word-limit
          />
          <div class="comment-submit">
            <el-button
                type="primary"
                :loading="commentLoading"
                :disabled="!isLoggedIn"
                @click="submitComment"
                round
            >
              <el-icon><Promotion /></el-icon> 发表评论
            </el-button>
          </div>
        </div>

        <!-- 评论列表 -->
        <div class="comment-list">
          <div v-if="commentList.length === 0" class="empty-comment">
            <span class="empty-icon">💬</span>
            <span>暂无评论，快来抢沙发吧~</span>
          </div>
          <div v-for="item in commentList" :key="item.id" class="comment-item stagger-item">
            <div class="comment-avatar">
              {{ (item.username || '匿').charAt(0).toUpperCase() }}
            </div>
            <div class="comment-body">
              <div class="comment-header">
                <span class="comment-user">{{ item.username || '匿名用户' }}</span>
                <span class="comment-time">{{ formatTime(item.createTime) }}</span>
                <el-button
                    v-if="String(item.userId) === String(currentUser?.id)"
                    type="text"
                    size="small"
                    class="delete-btn"
                    @click="handleDeleteComment(item.id)"
                >
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
              <div class="comment-content">{{ item.content }}</div>
            </div>
          </div>
        </div>

        <!-- 评论分页 -->
        <div class="comment-pagination" v-if="commentTotal > commentPageSize">
          <el-pagination
              v-model:current-page="commentPageNum"
              v-model:page-size="commentPageSize"
              :total="commentTotal"
              :page-sizes="[5, 10, 20]"
              layout="total, prev, pager, next"
              @size-change="loadComments"
              @current-change="loadComments"
          />
        </div>
      </div>
    </el-card>

    <!-- 历史版本弹窗 -->
    <el-dialog v-model="versionDialogVisible" title="历史版本" width="600px">
      <div v-if="versionList.length === 0" class="empty-version">暂无历史版本</div>
      <div v-for="v in versionList" :key="v.id" class="version-item">
        <div class="version-info">
          <span class="version-tag">v{{ v.version }}</span>
          <span class="version-title">{{ v.title }}</span>
          <span class="version-time">{{ formatTime(v.createTime) }}</span>
        </div>
        <el-button type="primary" size="small" @click="handleRollback(v)">回滚</el-button>
      </div>
    </el-dialog>
    </div>
  </div>
</template>

<script setup>
import {ref, computed, onMounted} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {
  ArrowLeft, Edit, Delete, User, Folder, View, Star, Clock,
  ChatDotRound, Promotion, Loading
} from '@element-plus/icons-vue'
import {
  getArticleById,
  deleteArticle,
  getVersionList,
  rollbackVersion,
  getCategoryList,
  likeArticle,
  getCommentList,
  addComment,
  deleteComment
} from '../api/article'
import {getToken, getUser} from '../utils/request'
import {ElMessage, ElMessageBox} from 'element-plus'

const route = useRoute()
const router = useRouter()
const article = ref(null)
const categoryList = ref([])
const categoryName = ref('')
const tagArray = ref([])
const currentUser = ref(getUser())
const isLoggedIn = computed(() => !!getToken())
const versionDialogVisible = ref(false)
const versionList = ref([])
const loading = ref(true)
const loadError = ref('')

// 点赞相关
const isLiked = ref(false)
const likeCount = ref(0)
const likeLoading = ref(false)

// 评论相关（分页）
const commentList = ref([])
const commentTotal = ref(0)
const commentPageNum = ref(1)
const commentPageSize = ref(10)
const commentContent = ref('')
const commentLoading = ref(false)

const formatTime = (t) => t ? t.replace('T', ' ').slice(0, 16) : ''

const goEdit = () => router.push(`/publish/${article.value.id}`)

const loadArticle = async () => {
  loading.value = true
  loadError.value = ''
  try {
    const res = await getArticleById(route.params.id)
    article.value = res.data

    // 加载分类列表匹配名称
    try {
      const catRes = await getCategoryList()
      categoryList.value = catRes.data || []
      const cat = categoryList.value.find(c => String(c.id) === String(article.value.categoryId))
      categoryName.value = cat ? cat.name : ''
    } catch (e) {
      console.warn('加载分类列表失败:', e)
    }

    tagArray.value = article.value.tags ? article.value.tags.split(',').filter(Boolean) : []

    // 初始化点赞数据
    likeCount.value = res.data.likeCount || 0
    isLiked.value = res.data.isLiked || false
  } catch (e) {
    console.error('加载文章失败:', e)
    loadError.value = e.response?.data?.msg || e.message || '网络错误，请稍后重试'
  } finally {
    loading.value = false
  }
}

const handleDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定删除文章「${article.value.title}」吗？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '确定删除',
      cancelButtonText: '取消'
    })
    await deleteArticle(article.value.id)
    ElMessage.success('删除成功')
    router.push('/')
  } catch (e) {
  }
}

const openVersionDialog = async () => {
  const res = await getVersionList(article.value.id)
  versionList.value = res.data
  versionDialogVisible.value = true
}

const handleRollback = async (v) => {
  try {
    await ElMessageBox.confirm(`确定回滚到版本 v${v.version} 吗？当前内容会保存为新版本。`, '回滚确认', {
      type: 'warning',
      confirmButtonText: '确定回滚',
      cancelButtonText: '取消'
    })
    await rollbackVersion(v.id)
    ElMessage.success('回滚成功')
    versionDialogVisible.value = false
    loadArticle()
  } catch (e) {
  }
}

// 点赞
const handleLike = async () => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录后再点赞')
    router.push('/login')
    return
  }
  if (likeLoading.value) return
  likeLoading.value = true
  try {
    const res = await likeArticle(article.value.id)
    isLiked.value = res.data.isLiked
    likeCount.value = res.data.likeCount
    ElMessage.success(isLiked.value ? '点赞成功' : '取消点赞')
  } finally {
    likeLoading.value = false
  }
}

// 评论（分页）
const loadComments = async () => {
  const res = await getCommentList(article.value.id, {
    pageNum: commentPageNum.value,
    pageSize: commentPageSize.value
  })
  commentList.value = res.data.records || []
  commentTotal.value = res.data.total || 0
}

const submitComment = async () => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  if (!commentContent.value.trim()) {
    ElMessage.warning('评论内容不能为空')
    return
  }
  commentLoading.value = true
  try {
    await addComment({
      articleId: article.value.id,
      content: commentContent.value.trim()
    })
    ElMessage.success('评论成功')
    commentContent.value = ''
    commentPageNum.value = 1
    loadComments()
  } finally {
    commentLoading.value = false
  }
}

const handleDeleteComment = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除这条评论吗？', '删除确认', {
      type: 'warning',
      confirmButtonText: '确定删除',
      cancelButtonText: '取消'
    })
    await deleteComment(id)
    ElMessage.success('删除成功')
    loadComments()
  } catch (e) {}
}

onMounted(async () => {
  await loadArticle()
  await loadComments()
})
</script>

<style scoped>
.detail {
  max-width: 900px;
  margin: 0 auto;
}

/* 加载中 */
.detail-loading {
  text-align: center;
  padding: 100px 20px;
  color: var(--text-muted);
}
.loading-icon {
  color: var(--primary);
  animation: rotate 1s linear infinite;
  margin-bottom: 16px;
}
@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
.detail-loading p {
  font-size: 14px;
}

/* 加载失败 */
.detail-error {
  text-align: center;
  padding: 80px 20px;
}
.detail-error .error-icon {
  font-size: 56px;
  margin-bottom: 16px;
}
.detail-error h3 {
  color: var(--text-primary);
  font-size: 20px;
  margin-bottom: 8px;
}
.detail-error p {
  color: var(--text-muted);
  font-size: 14px;
  margin-bottom: 24px;
}
.detail-error .el-button {
  margin: 0 8px;
  border-radius: 20px;
}

.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.action-right {
  display: flex;
  gap: 10px;
}

.back-btn {
  background: var(--card-bg-solid) !important;
  border: 1px solid var(--border-strong) !important;
  color: var(--primary-light) !important;
  border-radius: 20px !important;
  display: flex;
  align-items: center;
  gap: 6px;
}

.back-btn:hover {
  border-color: var(--primary) !important;
  box-shadow: 0 0 12px var(--glow) !important;
}

.action-btn {
  border-radius: 20px !important;
  display: flex;
  align-items: center;
  gap: 6px;
}

.detail-card {
  border-radius: 20px !important;
  padding: 30px 40px !important;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2) !important;
}

.article-title {
  font-size: 34px;
  font-weight: 800;
  background: linear-gradient(135deg, var(--primary-light), var(--accent));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 20px;
  line-height: 1.3;
  letter-spacing: 1px;
}

.article-meta {
  display: flex;
  gap: 20px;
  color: var(--text-muted);
  font-size: 13px;
  flex-wrap: wrap;
  margin-bottom: 16px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 5px;
}

.meta-item :deep(.el-icon) {
  font-size: 14px;
}

.divider {
  height: 1px;
  background: linear-gradient(90deg, var(--primary), transparent);
  margin: 28px 0;
  opacity: 0.5;
}

.article-content {
  color: #cbd5e1;
  font-size: 16px;
  line-height: 2;
}

/* 标签 */
.tag-list {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.tag-chip {
  background: var(--gradient-1);
  border: 1px solid var(--border-strong);
  color: var(--primary-light);
  font-size: 12px;
  padding: 4px 14px;
  border-radius: 14px;
}

/* ========== 点赞区 ========== */
.like-wrapper {
  text-align: center;
  padding: 10px 0;
}

.like-btn {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 14px 36px;
  border-radius: 50px;
  border: 2px solid var(--border-strong);
  background: var(--card-bg-solid);
  color: var(--text-secondary);
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.like-btn:hover:not(:disabled) {
  border-color: var(--primary);
  color: var(--primary-light);
  box-shadow: 0 0 20px var(--glow);
  transform: scale(1.05);
}

.like-btn.liked {
  border-color: #ec4899;
  background: linear-gradient(135deg, rgba(236, 72, 153, 0.15), rgba(244, 114, 182, 0.1));
  color: #f472b6;
  box-shadow: 0 0 25px rgba(236, 72, 153, 0.3);
}

.like-icon {
  font-size: 22px;
  display: inline-block;
  transition: transform 0.3s;
}

.like-icon.animate {
  animation: heartbeat 0.6s ease-in-out;
}

.like-count {
  background: var(--card-bg);
  padding: 2px 12px;
  border-radius: 12px;
  font-size: 14px;
  min-width: 32px;
  text-align: center;
}

.like-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* ========== 评论区 ========== */
.comment-section {
  color: var(--text-primary);
}

.comment-title {
  font-size: 20px;
  font-weight: 700;
  margin-bottom: 20px;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  gap: 8px;
}

.comment-title :deep(.el-icon) {
  color: var(--primary-light);
}

.comment-count-num {
  background: var(--gradient-1);
  color: var(--primary-light);
  font-size: 14px;
  padding: 2px 10px;
  border-radius: 12px;
  font-weight: 600;
}

.comment-input-box {
  margin-bottom: 28px;
}

.comment-submit {
  margin-top: 12px;
  text-align: right;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.empty-comment {
  text-align: center;
  color: var(--text-muted);
  padding: 40px 0;
  font-size: 14px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  align-items: center;
}

.empty-comment .empty-icon {
  font-size: 40px;
  opacity: 0.5;
}

.comment-item {
  display: flex;
  gap: 14px;
  padding: 16px;
  background: var(--card-bg-solid);
  border-radius: 14px;
  border: 1px solid var(--border);
  transition: all 0.25s;
}

.comment-item:hover {
  border-color: var(--border-strong);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.comment-avatar {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--primary), var(--accent));
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 700;
  color: #fff;
  flex-shrink: 0;
  box-shadow: 0 0 12px var(--glow);
}

.comment-body {
  flex: 1;
  min-width: 0;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.comment-user {
  font-weight: 600;
  color: var(--primary-light);
  font-size: 14px;
}

.comment-time {
  color: var(--text-muted);
  font-size: 12px;
  flex: 1;
}

.delete-btn {
  color: var(--el-color-danger) !important;
  padding: 0 !important;
}

.comment-content {
  font-size: 14px;
  line-height: 1.7;
  color: var(--text-primary);
  word-break: break-word;
}

.comment-pagination {
  display: flex;
  justify-content: center;
  margin-top: 28px;
}

/* 历史版本 */
.empty-version {
  text-align: center;
  color: var(--text-muted);
  padding: 30px 0;
}

.version-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 0;
  border-bottom: 1px solid var(--border);
}

.version-item:last-child {
  border-bottom: none;
}

.version-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.version-tag {
  background: linear-gradient(135deg, var(--primary), var(--primary-dark));
  color: #fff;
  padding: 3px 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
}

.version-title {
  color: var(--text-primary);
  font-size: 14px;
}

.version-time {
  color: var(--text-muted);
  font-size: 12px;
}
</style>
