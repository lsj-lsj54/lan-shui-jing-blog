# 蓝水晶博客 · 后端

Spring Boot 3.5 微服务：网关 + 用户 + 文章 + AI。公共能力在 `common-blog`。

完整环境、Nacos、DeepSeek 与接口说明见仓库根目录 [README.md](../README.md)。

## 模块

| 模块 | 职责 |
|------|------|
| `common-blog` | `Result`、全局异常、JWT、BCrypt、MyBatis-Plus 自动填充 |
| `gateway-blog` | 路由、JWT 鉴权、CORS；本地默认监听与前端约定为 `8080` |
| `user-blog` | 注册登录、用户资料 |
| `article-blog` | 文章、分类、评论、点赞、版本；浏览量 Redis |
| `ai-blog` | ChatClient + DeepSeek；对话与 `t_ai_knowledge` |

父 POM 版本：Java 17、Spring Boot 3.5.5、Spring Cloud 2025.0.0、Spring Cloud Alibaba 2025.0.0.0、MyBatis-Plus 3.5.17、Spring AI 1.1.0。

## 编译

```bash
cd backend
mvn -pl common-blog,user-blog,article-blog,ai-blog,gateway-blog -am package -DskipTests
```

启动顺序：Nacos、MySQL、Redis → user-blog → article-blog → ai-blog → gateway-blog。

也可以在仓库根目录使用 Docker：`docker compose up -d --build`，说明见根 README。

## SQL

| 脚本 | 用途 |
|------|------|
| `sql/init_schema.sql` | 三个库全量建表 |
| `sql/upgrade_ai_knowledge.sql` | 已有 `blog_ai` 时补知识表 |
| `sql/upgrade_indexes.sql` | 老库补唯一索引与查询索引 |

历史重构说明见 [CHANGELOG.md](CHANGELOG.md)。
