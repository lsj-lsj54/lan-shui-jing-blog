# 蓝水晶博客

前后端合一的个人博客：**Vue 3 前端** + **Spring Cloud 微服务后端**，并接入 **Spring AI ChatClient + DeepSeek** 做技术知识检索与沉淀。

由 [lsj-lsj54/-blog](https://github.com/lsj-lsj54/-blog)（前端）与 [lsj-lsj54/-blog2](https://github.com/lsj-lsj54/-blog2)（后端）合并而来。

公开仓库：[`lsj-lsj54/lan-shui-jing-blog`](https://github.com/lsj-lsj54/lan-shui-jing-blog)（GitHub 仓库名使用英文 slug，项目名仍为「蓝水晶博客」）。

## 仓库结构

```
frontend/          Vue 3 + Vite + Element Plus
backend/           Maven 多模块
  common-blog/     公共组件（JWT、统一返回、异常处理）
  gateway-blog/    网关（鉴权、路由、CORS）
  user-blog/       用户服务
  article-blog/    文章 / 评论 / 分类
  ai-blog/         Spring AI + DeepSeek 对话与知识库
  sql/             建表与升级脚本
```

## 环境要求

- JDK 17
- Maven 3.8+
- Node.js 18+
- MySQL 8
- Redis
- Nacos 2.x（默认 `192.168.175.128:8848`）
- DeepSeek API Key（[platform.deepseek.com](https://platform.deepseek.com)）

## 数据库

首次部署执行：

```bash
mysql -u root -p < backend/sql/init_schema.sql
```

已有库只需补知识表：

```bash
mysql -u root -p < backend/sql/upgrade_ai_knowledge.sql
```

会创建 `blog_user`、`blog_article`、`blog_ai` 三个库。各服务数据源仍建议放在 Nacos。

## DeepSeek 配置

**不要把 Key 提交进 Git。** 任选一种方式：

1. 环境变量（推荐）

```bash
# Windows PowerShell
$env:DEEPSEEK_API_KEY="sk-..."

# Linux / macOS
export DEEPSEEK_API_KEY=sk-...
```

2. Nacos 配置 `ai-blog.yaml`

```yaml
spring:
  ai:
    deepseek:
      api-key: ${DEEPSEEK_API_KEY}
      chat:
        options:
          model: deepseek-chat
          temperature: 0.3
```

本地 `backend/ai-blog/src/main/resources/application.yml` 已预留上述项，Key 从环境变量读取。

## 启动顺序

1. MySQL、Redis、Nacos
2. 后端（均在 `backend/` 下）

```bash
mvn -pl common-blog,user-blog,article-blog,ai-blog,gateway-blog -am package -DskipTests
```

建议启动顺序：`user-blog` → `article-blog` → `ai-blog` → `gateway-blog`（网关默认 `8080`）。

3. 前端

```bash
cd frontend
npm install
npm run dev
```

前端开发服务器 `5173`，`/api` 代理到 `http://localhost:8080`。

## 网关路由（本地默认）

| 前缀 | 服务 |
|------|------|
| `/api/user/**` | user-blog |
| `/api/article/**` | article-blog |
| `/api/ai/**` | ai-blog |

Nacos 里若已有同名路由，以实际生效配置为准。大模型接口超时已设为 120 秒。

## 知识库接口

登录后可用：

- `POST /api/ai/knowledge/search` `{ "keyword": "ChatClient.prompt()", "forceRefresh": false }`
- `GET /api/ai/knowledge/page`
- `GET /api/ai/knowledge/{id}`
- `DELETE /api/ai/knowledge/{id}`

相同关键词默认直接读库；需要重新生成时传 `forceRefresh: true`。

对话：`POST /api/ai/message` 会调用 DeepSeek，并同时写入用户消息与助手回复。
