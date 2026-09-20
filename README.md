# 蓝水晶博客 · 后端

Spring Boot 3.5 微服务：网关、用户、文章、AI（Spring AI + DeepSeek）。配套前端：[lan-shui-jing-blog-frontend](https://github.com/lsj-lsj54/lan-shui-jing-blog-frontend)。前后端合一仓库：[lan-shui-jing-blog](https://github.com/lsj-lsj54/lan-shui-jing-blog)。

## 模块

| 模块 | 职责 |
|------|------|
| `common-blog` | 统一返回、异常、JWT、BCrypt |
| `gateway-blog` | 路由、鉴权、CORS，默认 `8080` |
| `user-blog` | 注册登录，默认 `8081` |
| `article-blog` | 文章 / 评论 / 点赞，默认 `8082` |
| `ai-blog` | 对话与知识库，默认 `8083` |

## 本地启动

需要 JDK 17、Maven、MySQL 8、Redis、Nacos。先执行 `sql/init_schema.sql`。

```bash
mvn -pl common-blog,user-blog,article-blog,ai-blog,gateway-blog -am package -DskipTests
```

顺序：Nacos / MySQL / Redis → user-blog → article-blog → ai-blog → gateway-blog。

Nacos 地址默认 `192.168.175.128:8848`，可用环境变量 `NACOS_SERVER_ADDR` 覆盖。Docker 场景会关掉 Nacos 配置中心，改用环境变量注入数据源。

## Docker

```bash
cp .env.example .env
# 知识库需要 DEEPSEEK_API_KEY
docker compose up -d --build
```

网关：http://localhost:8080 ，Nacos：http://localhost:8848/nacos 。

前端仓库的 Compose 会加入本项目的 Docker 网络 `lan-shui-jing-blog_default`。

## SQL

| 脚本 | 用途 |
|------|------|
| `sql/init_schema.sql` | 三个库全量建表 |
| `sql/upgrade_ai_knowledge.sql` | 补知识表 |
| `sql/upgrade_indexes.sql` | 补索引 |

历史重构说明见 [CHANGELOG.md](CHANGELOG.md)。
