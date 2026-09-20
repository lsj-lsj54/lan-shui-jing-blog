<template>
  <div class="knowledge fade-in">
    <div class="page-header">
      <div>
        <h1 class="page-title">知识库</h1>
        <p class="page-desc">输入技术关键词，调用 DeepSeek 生成说明并沉淀到数据库</p>
      </div>
    </div>

    <el-card class="search-card">
      <div class="search-row">
        <el-input
            v-model="keyword"
            placeholder="例如：ChatClient.prompt() 的作用"
            clearable
            size="large"
            @keyup.enter="handleSearch(false)"
        />
        <el-checkbox v-model="forceRefresh">强制重新生成</el-checkbox>
        <el-button type="primary" size="large" :loading="searching" @click="handleSearch(false)">
          检索并入库
        </el-button>
      </div>
    </el-card>

    <div class="knowledge-layout">
      <aside class="history-pane">
        <el-card class="history-card">
          <div class="history-title">已沉淀知识</div>
          <el-input
              v-model="listKeyword"
              placeholder="筛选标题或关键词"
              clearable
              class="history-filter"
              @keyup.enter="loadList"
              @clear="loadList"
          />
          <div class="history-list" v-loading="listLoading">
            <div
                v-for="item in list"
                :key="item.id"
                class="history-item"
                :class="{ active: current?.id === item.id }"
                @click="openItem(item)"
            >
              <div class="history-item-title">{{ item.title || item.keywordRaw }}</div>
              <div class="history-item-meta">{{ item.keywordRaw }}</div>
            </div>
            <div v-if="!listLoading && list.length === 0" class="empty">暂无记录</div>
          </div>
          <el-pagination
              v-if="total > pageSize"
              small
              layout="prev, pager, next"
              :total="total"
              :page-size="pageSize"
              v-model:current-page="pageNum"
              @current-change="loadList"
          />
        </el-card>
      </aside>

      <section class="result-pane">
        <el-card class="result-card" v-loading="searching">
          <template v-if="current">
            <div class="result-header">
              <div>
                <h2 class="result-title">{{ current.title }}</h2>
                <p class="result-summary">{{ current.summary }}</p>
                <div class="result-tags">
                  <el-tag size="small">{{ current.keywordRaw }}</el-tag>
                  <el-tag size="small" type="info">{{ current.model || 'deepseek-chat' }}</el-tag>
                  <el-tag size="small" :type="current.cached ? 'success' : 'warning'">
                    {{ current.cached ? '来自数据库' : '刚刚生成' }}
                  </el-tag>
                </div>
              </div>
              <div class="result-actions">
                <el-button @click="handleSearch(true)">重新生成</el-button>
                <el-button type="danger" @click="handleDelete">删除</el-button>
              </div>
            </div>
            <MdPreview :modelValue="current.content" theme="dark" previewTheme="github" class="knowledge-md"/>
          </template>
          <div v-else class="empty-result">
            <p>试试搜索「ChatClient.prompt() 的作用」</p>
            <p class="hint">首次会调用大模型，之后相同关键词直接读库</p>
          </div>
        </el-card>
      </section>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { deleteKnowledge, getKnowledgeById, getKnowledgePage, searchKnowledge } from '../api/ai'

const keyword = ref('')
const forceRefresh = ref(false)
const searching = ref(false)
const current = ref(null)

const list = ref([])
const listKeyword = ref('')
const listLoading = ref(false)
const pageNum = ref(1)
const pageSize = 10
const total = ref(0)

const loadList = async () => {
  listLoading.value = true
  try {
    const res = await getKnowledgePage({
      pageNum: pageNum.value,
      pageSize,
      keyword: listKeyword.value || undefined
    })
    list.value = res.data?.records || []
    total.value = Number(res.data?.total || 0)
  } finally {
    listLoading.value = false
  }
}

const handleSearch = async (refresh) => {
  const query = (refresh ? current.value?.keywordRaw : keyword.value)?.trim()
  if (!query) {
    ElMessage.warning('请输入关键词')
    return
  }
  searching.value = true
  try {
    const res = await searchKnowledge({
      keyword: query,
      forceRefresh: refresh || forceRefresh.value
    })
    current.value = res.data
    keyword.value = query
    ElMessage.success(res.data?.cached ? '已从知识库读取' : '已生成并写入数据库')
    await loadList()
  } finally {
    searching.value = false
  }
}

const openItem = async (item) => {
  const res = await getKnowledgeById(item.id)
  current.value = { ...res.data, cached: true }
  keyword.value = res.data.keywordRaw
}

const handleDelete = async () => {
  if (!current.value?.id) {
    return
  }
  await ElMessageBox.confirm('确定删除这条知识吗？', '提示', { type: 'warning' })
  await deleteKnowledge(current.value.id)
  ElMessage.success('已删除')
  current.value = null
  await loadList()
}

onMounted(loadList)
</script>

<style scoped>
.page-header { margin-bottom: 20px; }
.page-title {
  margin: 0;
  font-size: 28px;
  background: linear-gradient(135deg, var(--primary-light), var(--accent));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}
.page-desc { color: var(--text-secondary); margin: 8px 0 0; }
.search-card {
  background: var(--card-bg);
  border: 1px solid var(--border);
  margin-bottom: 20px;
}
.search-row {
  display: flex;
  align-items: center;
  gap: 12px;
}
.knowledge-layout {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 20px;
}
.history-card, .result-card {
  background: var(--card-bg);
  border: 1px solid var(--border);
}
.history-title {
  font-weight: 600;
  margin-bottom: 12px;
  color: var(--text-primary);
}
.history-filter { margin-bottom: 12px; }
.history-list { min-height: 240px; }
.history-item {
  padding: 10px 8px;
  border-radius: 8px;
  cursor: pointer;
  margin-bottom: 6px;
}
.history-item:hover, .history-item.active {
  background: var(--card-bg-solid);
  box-shadow: 0 0 0 1px var(--border-strong);
}
.history-item-title {
  color: var(--text-primary);
  font-size: 14px;
}
.history-item-meta {
  color: var(--text-muted);
  font-size: 12px;
  margin-top: 4px;
}
.result-header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}
.result-title { margin: 0 0 8px; color: var(--text-primary); }
.result-summary { color: var(--text-secondary); margin: 0 0 12px; }
.result-tags { display: flex; gap: 8px; flex-wrap: wrap; }
.empty, .empty-result {
  color: var(--text-muted);
  text-align: center;
  padding: 40px 12px;
}
.hint { font-size: 13px; }
.knowledge-md { background: transparent; }
@media (max-width: 900px) {
  .knowledge-layout { grid-template-columns: 1fr; }
  .search-row { flex-wrap: wrap; }
}
</style>
