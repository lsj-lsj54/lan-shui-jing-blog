<template>
  <div class="publish fade-in">
    <div class="page-header">
      <div>
        <h1 class="page-title">{{ isEdit ? '编辑文章' : '发布文章' }}</h1>
        <p class="page-desc">用 Markdown 写出你的想法</p>
      </div>
    </div>

    <el-card class="publish-card">
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="请输入文章标题" maxlength="100" show-word-limit />
        </el-form-item>

        <el-form-item label="分类">
          <div class="category-row">
            <el-select v-model="form.categoryId" placeholder="选择分类" style="width: 240px" clearable>
              <el-option
                  v-for="c in categoryList"
                  :key="c.id"
                  :label="c.name"
                  :value="c.id"
              />
            </el-select>
            <el-button @click="showCategoryDialog = true" class="cat-manage-btn">
              <el-icon><Setting /></el-icon> 分类管理
            </el-button>
          </div>
        </el-form-item>

        <el-form-item label="标签">
          <el-select
              v-model="tagList"
              multiple
              filterable
              allow-create
              default-first-option
              placeholder="输入标签后回车添加"
              style="width: 100%"
          >
            <el-option v-for="t in allTags" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>

        <el-form-item label="内容">
          <MdEditor
              v-model="form.content"
              theme="dark"
              previewTheme="github"
              :toolbars="toolbars"
              style="height: 560px"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="submit" :loading="loading" class="submit-btn">
            <el-icon><Promotion /></el-icon> {{ isEdit ? '保存修改' : '发布文章' }}
          </el-button>
          <el-button @click="cancel" class="cancel-btn">
            <el-icon><Close /></el-icon> 取消
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 分类管理弹窗 -->
    <el-dialog v-model="showCategoryDialog" title="分类管理" width="420px">
      <div class="category-manage">
        <div class="category-list">
          <div v-for="c in categoryList" :key="c.id" class="category-item">
            <span class="cat-name">{{ c.name }}</span>
            <el-button type="danger" size="small" link @click="handleDeleteCategory(c)">
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
          <div v-if="categoryList.length === 0" class="empty-cat">暂无分类，创建一个吧~</div>
        </div>
        <el-divider />
        <div class="add-cat-row">
          <el-input v-model="newCategoryName" placeholder="输入新分类名称" @keyup.enter="confirmAddCategory" />
          <el-button type="primary" @click="confirmAddCategory">
            <el-icon><Plus /></el-icon> 添加
          </el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Setting, Promotion, Close, Delete, Plus } from '@element-plus/icons-vue'
import { addArticle, updateArticle, getArticleById, getCategoryList, addCategory, deleteCategory } from '../api/article'

const route = useRoute()
const router = useRouter()
const isEdit = ref(false)
const loading = ref(false)
const categoryList = ref([])
const tagList = ref([])
const allTags = ref(['Java', 'Vue', 'Spring', 'MySQL', 'Redis', '前端', '后端', '随笔'])
const showCategoryDialog = ref(false)
const newCategoryName = ref('')

const toolbars = [
  'bold', 'underline', 'italic', 'strikeThrough', 'title', 'sub', 'sup',
  'quote', 'unorderedList', 'orderedList', 'task', 'codeRow', 'code', 'link',
  'image', 'table', 'mermaid', 'katex', 'revoke', 'next', 'save', 'pageFullscreen'
]

const form = reactive({
  id: null,
  title: '',
  content: '',
  categoryId: null,
  tags: ''
})

const loadCategories = async () => {
  const res = await getCategoryList()
  categoryList.value = res.data || []
}

const confirmAddCategory = async () => {
  if (!newCategoryName.value.trim()) {
    ElMessage.warning('请输入分类名称')
    return
  }
  const res = await addCategory({ name: newCategoryName.value.trim() })
  categoryList.value.push(res.data)
  form.categoryId = res.data.id
  newCategoryName.value = ''
  ElMessage.success('分类创建成功')
}

const handleDeleteCategory = async (c) => {
  try {
    await ElMessageBox.confirm(`确定删除分类「${c.name}」吗？`, '删除确认', { type: 'warning' })
    await deleteCategory(c.id)
    categoryList.value = categoryList.value.filter(item => item.id !== c.id)
    if (form.categoryId === c.id) {
      form.categoryId = null
    }
    ElMessage.success('删除成功')
  } catch (e) {}
}

const submit = async () => {
  if (!form.title.trim()) {
    ElMessage.warning('请输入标题')
    return
  }
  if (!form.content.trim()) {
    ElMessage.warning('请输入内容')
    return
  }
  loading.value = true
  form.tags = tagList.value.join(',')
  try {
    if (isEdit.value) {
      await updateArticle(form)
      ElMessage.success('更新成功')
    } else {
      await addArticle(form)
      ElMessage.success('发布成功')
    }
    router.push('/')
  } finally {
    loading.value = false
  }
}

const cancel = () => {
  router.back()
}

onMounted(async () => {
  await loadCategories()
  const id = route.params.id
  if (id) {
    isEdit.value = true
    const res = await getArticleById(id)
    const article = res.data
    form.id = article.id
    form.title = article.title
    form.content = article.content
    form.categoryId = article.categoryId
    tagList.value = article.tags ? article.tags.split(',').filter(Boolean) : []
  }
})
</script>

<style scoped>
.publish { max-width: 1000px; margin: 0 auto; }
.page-header { margin-bottom: 24px; }
.page-title {
  font-size: 30px;
  font-weight: 800;
  background: linear-gradient(135deg, var(--primary-light), var(--accent));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 6px;
  letter-spacing: 1px;
}
.page-desc { color: var(--text-muted); font-size: 14px; }
.publish-card {
  border-radius: 20px !important;
  padding: 10px 24px !important;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2) !important;
}
.category-row { display: flex; gap: 12px; align-items: center; }
.cat-manage-btn {
  background: var(--card-bg-solid) !important;
  border: 1px solid var(--border-strong) !important;
  color: var(--text-secondary) !important;
  border-radius: 20px !important;
  display: flex;
  align-items: center;
  gap: 6px;
}
.cat-manage-btn:hover {
  color: var(--primary-light) !important;
  border-color: var(--primary) !important;
}
.submit-btn {
  border-radius: 20px !important;
  padding: 0 28px !important;
  display: flex;
  align-items: center;
  gap: 6px;
}
.cancel-btn {
  border-radius: 20px !important;
  background: var(--card-bg-solid) !important;
  border: 1px solid var(--border-strong) !important;
  color: var(--text-secondary) !important;
  display: flex;
  align-items: center;
  gap: 6px;
}
.cancel-btn:hover {
  color: var(--primary-light) !important;
  border-color: var(--primary) !important;
}
:deep(.el-form-item__label) {
  color: var(--primary-light) !important;
  font-weight: 600;
}
.category-manage { max-height: 400px; overflow-y: auto; }
.category-list { margin-bottom: 10px; }
.category-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px;
  border-bottom: 1px solid var(--border);
  border-radius: 8px;
  transition: background 0.2s;
}
.category-item:hover {
  background: var(--gradient-1);
}
.category-item:last-child { border-bottom: none; }
.cat-name { color: var(--text-primary); font-size: 14px; }
.empty-cat { text-align: center; color: var(--text-muted); padding: 24px 0; font-size: 13px; }
.add-cat-row { display: flex; gap: 10px; }
</style>
