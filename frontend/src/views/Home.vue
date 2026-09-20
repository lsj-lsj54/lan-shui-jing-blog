<template>
  <div class="home fade-in">
    <div class="home-layout">
      <!-- 左侧分类侧边栏 -->
      <aside class="sidebar">
        <el-card class="sidebar-card">
          <h3 class="sidebar-title">
            <span class="title-icon">📂</span> 文章分类
          </h3>
          <div
              class="category-item"
              :class="{ active: !selectedCategory }"
              @click="selectCategory(null)"
          >
            <span class="cat-dot"></span>
            全部文章
            <span class="cat-count">{{ total }}</span>
          </div>
          <div
              v-for="c in categoryList"
              :key="c.id"
              class="category-item"
              :class="{ active: selectedCategory === c.id }"
              @click="selectCategory(c.id)"
          >
            <span class="cat-dot"></span>
            {{ c.name }}
          </div>
          <div v-if="categoryList.length === 0" class="empty-cat">
            <span>暂无分类</span>
            <span class="empty-hint">发布文章时可创建</span>
          </div>
        </el-card>
      </aside>

      <!-- 右侧文章列表 -->
      <div class="main-content">
        <div class="page-header">
          <div>
            <h1 class="page-title">我的文章</h1>
            <p class="page-desc">共 <span class="highlight">{{ total }}</span> 篇文章</p>
          </div>
        </div>

        <div class="search-bar">
          <div class="search-input-wrapper">
            <el-icon class="search-icon"><Search /></el-icon>
            <el-input
                v-model="keyword"
                placeholder="搜索文章标题或作者..."
                clearable
                @keyup.enter="search"
            />
          </div>
          <el-button type="primary" @click="search" class="search-btn">
            <el-icon><Search /></el-icon> 搜索
          </el-button>
          <el-button @click="resetSearch" v-if="keyword || selectedCategory" class="reset-btn">
            <el-icon><RefreshLeft /></el-icon> 重置
          </el-button>
        </div>

        <div class="article-list" v-loading="loading">
          <div
              v-for="(item, index) in articles"
              :key="item.id"
              class="article-card stagger-item"
              :style="{ '--card-index': index }"
              @click="goDetail(item.id)"
          >
            <!-- 渐变封面 -->
            <div class="article-cover" :style="getCoverStyle(item.id)">
              <div class="cover-overlay"></div>
              <div class="cover-icon">📄</div>
              <div class="cover-category" v-if="getCategoryName(item.categoryId)">
                {{ getCategoryName(item.categoryId) }}
              </div>
              <div class="cover-fade"></div>
            </div>

            <div class="article-body">
              <h2 class="article-title">{{ item.title }}</h2>
              <p class="article-summary">{{ item.summary || (item.content ? item.content.substring(0, 120) + '...' : '暂无摘要') }}</p>

              <div class="tag-list" v-if="item.tags">
                <span v-for="t in item.tags.split(',').filter(Boolean)" :key="t" class="tag-chip">{{ t }}</span>
              </div>

              <div class="article-meta">
                <div class="meta-left">
                  <span class="meta-item author">
                    <el-icon><User /></el-icon>
                    {{ item.author || '匿名' }}
                  </span>
                  <span class="meta-item">
                    <el-icon><Clock /></el-icon>
                    {{ formatTime(item.createTime) }}
                  </span>
                </div>
                <div class="meta-right">
                  <span class="meta-item stat">
                    <el-icon><View /></el-icon>
                    {{ item.viewCount || 0 }}
                  </span>
                  <span class="meta-item stat">
                    <el-icon><Star /></el-icon>
                    {{ item.likeCount || 0 }}
                  </span>
                  <span class="meta-item stat">
                    <el-icon><ChatDotRound /></el-icon>
                    {{ item.commentCount || 0 }}
                  </span>
                  <el-button
                      v-if="item.userId === currentUser?.id || String(item.userId) === String(currentUser?.id)"
                      type="danger"
                      size="small"
                      text
                      class="delete-btn"
                      @click.stop="handleDelete(item)"
                  >
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </div>
              </div>
            </div>
          </div>

          <!-- 空状态 -->
          <div v-if="articles.length === 0 && !loading" class="empty-state">
            <div class="empty-icon">📝</div>
            <div class="empty-text">还没有文章</div>
            <div class="empty-hint">点击右上角「发布文章」开始创作吧</div>
          </div>
        </div>

        <div class="pagination-wrapper" v-if="total > 0">
          <el-pagination
              v-model:current-page="pageNum"
              v-model:page-size="pageSize"
              :total="total"
              :page-sizes="[5, 10, 20]"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="loadArticles"
              @current-change="loadArticles"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {ref, onMounted} from 'vue'
import {useRouter} from 'vue-router'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Search, RefreshLeft, User, Clock, View, Star, ChatDotRound, Delete} from '@element-plus/icons-vue'
import {getArticlePage, deleteArticle, getCategoryList} from '../api/article'
import {getUser} from '../utils/request'

const router = useRouter()
const currentUser = ref(getUser())
const articles = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const keyword = ref('')
const selectedCategory = ref(null)
const categoryList = ref([])
const loading = ref(false)

// 渐变色板（用于文章封面）
const coverGradients = [
  'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
  'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
  'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
  'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
  'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
  'linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)',
  'linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%)',
  'linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%)',
  'linear-gradient(135deg, #a1c4fd 0%, #c2e9fb 100%)',
  'linear-gradient(135deg, #d299c2 0%, #fef9d7 100%)',
]

const getCoverStyle = (id) => {
  const num = parseInt(String(id).slice(-2), 10) || 0
  return { background: coverGradients[num % coverGradients.length] }
}

const getCategoryName = (categoryId) => {
  const cat = categoryList.value.find(c => String(c.id) === String(categoryId))
  return cat ? cat.name : ''
}

const loadCategories = async () => {
  try {
    const res = await getCategoryList()
    categoryList.value = res.data || []
  } catch (e) {
    console.error('加载分类失败:', e)
  }
}

const selectCategory = (id) => {
  selectedCategory.value = id
  pageNum.value = 1
  loadArticles()
}

const loadArticles = async () => {
  loading.value = true
  const params = {
    pageNum: pageNum.value,
    pageSize: pageSize.value,
    userId: currentUser.value?.id
  }
  if (keyword.value) params.keyword = keyword.value
  if (selectedCategory.value) params.categoryId = selectedCategory.value
  try {
    const res = await getArticlePage(params)
    articles.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) {
    console.error('加载文章失败:', e)
    ElMessage.error('加载文章失败，请检查后端服务')
  } finally {
    loading.value = false
  }
}

const search = () => {
  pageNum.value = 1
  loadArticles()
}

const resetSearch = () => {
  keyword.value = ''
  selectedCategory.value = null
  pageNum.value = 1
  loadArticles()
}

const goDetail = (id) => {
  router.push(`/article/${id}`)
}

const handleDelete = async (item) => {
  try {
    await ElMessageBox.confirm(`确定删除文章「${item.title}」吗？`, '提示', {type: 'warning'})
    await deleteArticle(item.id)
    ElMessage.success('删除成功')
    loadArticles()
  } catch (e) {}
}

const formatTime = (time) => {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 16)
}

onMounted(() => {
  loadCategories()
  loadArticles()
})
</script>

<style scoped>
.home {
  padding: 10px 0;
}

.home-layout {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

/* ========== 侧边栏 ========== */
.sidebar {
  width: 240px;
  flex-shrink: 0;
  position: sticky;
  top: 84px;
}

.sidebar-card {
  border-radius: 16px !important;
  padding: 8px 0 !important;
}

.sidebar-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
  padding: 12px 20px 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.title-icon {
  font-size: 18px;
}

.category-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 20px;
  cursor: pointer;
  color: var(--text-secondary);
  font-size: 14px;
  transition: all 0.25s ease;
  position: relative;
}

.category-item:hover {
  color: var(--primary-light);
  background: var(--gradient-1);
}

.category-item.active {
  color: var(--primary-light);
  background: var(--gradient-1);
  font-weight: 600;
}

.category-item.active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 60%;
  background: linear-gradient(180deg, var(--primary), var(--accent));
  border-radius: 0 3px 3px 0;
}

.cat-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--text-muted);
  transition: all 0.25s;
}

.category-item:hover .cat-dot,
.category-item.active .cat-dot {
  background: var(--primary);
  box-shadow: 0 0 8px var(--glow);
}

.cat-count {
  margin-left: auto;
  font-size: 12px;
  background: var(--card-bg-solid);
  padding: 2px 8px;
  border-radius: 10px;
  color: var(--text-muted);
}

.empty-cat {
  padding: 20px;
  text-align: center;
  color: var(--text-muted);
  font-size: 13px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.empty-hint {
  font-size: 11px;
  opacity: 0.6;
}

/* ========== 主内容区 ========== */
.main-content {
  flex: 1;
  min-width: 0;
}

.page-header {
  margin-bottom: 24px;
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
}

.page-title {
  font-size: 32px;
  font-weight: 800;
  background: linear-gradient(135deg, var(--primary-light), var(--accent));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 6px;
  letter-spacing: 1px;
}

.page-desc {
  color: var(--text-muted);
  font-size: 14px;
}

.highlight {
  color: var(--primary-light);
  font-weight: 700;
  font-size: 16px;
}

/* ========== 搜索栏 ========== */
.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  align-items: center;
}

.search-input-wrapper {
  flex: 1;
  position: relative;
}

.search-icon {
  position: absolute;
  left: 14px;
  top: 50%;
  transform: translateY(-50%);
  color: var(--text-muted);
  z-index: 1;
  font-size: 16px;
}

.search-input-wrapper :deep(.el-input__wrapper) {
  padding-left: 40px !important;
  border-radius: 24px !important;
}

.search-btn {
  border-radius: 24px !important;
  padding: 0 24px !important;
}

.reset-btn {
  border-radius: 24px !important;
  background: var(--card-bg-solid) !important;
  border: 1px solid var(--border-strong) !important;
  color: var(--text-secondary) !important;
}

.reset-btn:hover {
  color: var(--primary-light) !important;
  border-color: var(--primary) !important;
}

/* ========== 文章卡片 ========== */
.article-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.article-card {
  background: var(--card-bg);
  border: 1px solid var(--border);
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  backdrop-filter: blur(10px);
}

.article-card:hover {
  transform: translateY(-4px);
  border-color: var(--border-strong);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.3), 0 0 20px var(--glow);
}

/* 渐变封面 */
.article-cover {
  height: 110px;
  position: relative;
  overflow: hidden;
}

.cover-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(90deg, transparent 0%, rgba(255,255,255,0.15) 50%, transparent 100%);
  animation: shimmer 3s infinite;
  background-size: 200% 100%;
}

.cover-icon {
  position: absolute;
  left: 24px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 40px;
  opacity: 0.9;
  filter: drop-shadow(0 2px 8px rgba(0,0,0,0.2));
}

.cover-category {
  position: absolute;
  right: 16px;
  top: 16px;
  background: rgba(0, 0, 0, 0.35);
  backdrop-filter: blur(6px);
  color: #fff;
  font-size: 12px;
  font-weight: 500;
  padding: 5px 14px;
  border-radius: 14px;
  white-space: nowrap;
  max-width: 50%;
  overflow: hidden;
  text-overflow: ellipsis;
  border: 1px solid rgba(255,255,255,0.2);
}

.cover-fade {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 30px;
  background: linear-gradient(to bottom, transparent, var(--card-bg));
  pointer-events: none;
}

.article-body {
  padding: 20px 24px;
}

.article-title {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 10px;
  line-height: 1.4;
  transition: color 0.25s;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.article-card:hover .article-title {
  color: var(--primary-light);
}

.article-summary {
  color: var(--text-secondary);
  font-size: 14px;
  line-height: 1.7;
  margin-bottom: 14px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 标签 */
.tag-list {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.tag-chip {
  background: var(--gradient-1);
  border: 1px solid var(--border-strong);
  color: var(--primary-light);
  font-size: 12px;
  padding: 3px 12px;
  border-radius: 12px;
  transition: all 0.25s;
}

.tag-chip:hover {
  background: var(--gradient-2);
  border-color: var(--primary);
}

/* 元信息 */
.article-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 14px;
  border-top: 1px solid var(--border);
  flex-wrap: wrap;
  gap: 10px;
}

.meta-left, .meta-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 5px;
  color: var(--text-muted);
  font-size: 13px;
  transition: color 0.25s;
}

.meta-item :deep(.el-icon) {
  font-size: 14px;
}

.meta-item.author {
  color: var(--text-secondary);
  font-weight: 500;
}

.meta-item.stat {
  font-variant-numeric: tabular-nums;
}

.article-card:hover .meta-item {
  color: var(--text-secondary);
}

.delete-btn {
  padding: 4px 8px !important;
  margin-left: 8px;
}

/* ========== 空状态 ========== */
.empty-state {
  text-align: center;
  padding: 80px 20px;
}

.empty-state .empty-icon {
  font-size: 72px;
  margin-bottom: 20px;
  opacity: 0.6;
  animation: float 3s ease-in-out infinite;
}

.empty-state .empty-text {
  font-size: 20px;
  color: var(--text-secondary);
  margin-bottom: 8px;
  font-weight: 600;
}

.empty-state .empty-hint {
  font-size: 14px;
  color: var(--text-muted);
}

/* ========== 分页 ========== */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}

/* ========== 响应式 ========== */
@media (max-width: 900px) {
  .home-layout {
    flex-direction: column;
  }
  .sidebar {
    width: 100%;
    position: static;
  }
  .article-meta {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
