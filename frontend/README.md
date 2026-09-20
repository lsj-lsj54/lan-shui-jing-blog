# 蓝水晶博客 · 前端

Vue 3 + Vite 单页应用，开发时把 `/api` 代理到网关 `http://localhost:8080`。

完整部署、接口和架构见仓库根目录 [README.md](../README.md)。

## 命令

```bash
npm install
npm run dev      # 开发，默认 http://localhost:5173
npm run build    # 产出 dist/
npm run preview  # 预览构建结果
```

## 页面

| 路由 | 页面 |
|------|------|
| `/login` | 登录 / 注册 |
| `/` | 首页文章列表 |
| `/article/:id` | 文章详情 |
| `/publish`、`/publish/:id` | 发布 / 编辑 |
| `/profile` | 个人中心 |
| `/knowledge` | 知识库（DeepSeek 检索并落库） |

需登录的页面没有 Token 会跳到 `/login`。Token 存在 `localStorage`，请求自动带 `Authorization: Bearer ...`。

## 接口封装

- `src/api/user.js` 用户
- `src/api/article.js` 文章、分类、评论、点赞
- `src/api/ai.js` 知识库与对话（超时 120 秒）

成功判断：响应体 `code === 1`。`401` 会清登录态并跳转登录页。
