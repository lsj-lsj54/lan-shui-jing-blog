# 博客后端系统重构变更说明

## 一、改动总览

本次重构解决了代码审查中发现的全部问题，涵盖 **安全、并发、架构、代码质量、性能** 五大方面。

| 模块 | 主要改动 |
|------|----------|
| common-blog | 新增 JWT 工具、BCrypt 加密、自动填充、全局常量 |
| user-blog | 密码 BCrypt 加密、JWT 登录、VO 脱敏、注册接口、参数校验 |
| article-blog | DTO/VO 分层、Controller 瘦身、浏览量 Redis 计数、点赞并发安全、评论分页、事务完善 |
| gateway-blog | JWT 全局鉴权过滤器、CORS 跨域、服务发现 |
| ai-blog | DTO/VO 分层、参数校验 |

---

## 二、安全相关改动

### 2.1 密码加密
- **之前**：明文存储和比较
- **之后**：使用 BCrypt 加密存储，登录时 `passwordEncoder.matches()` 校验
- **影响**：已有用户的明文密码无法直接登录，需要重置密码或写迁移脚本

### 2.2 JWT 认证
- **之前**：无认证，直接从 header 传 user-id，可伪造
- **之后**：
  - 登录成功返回 JWT Token
  - 网关全局过滤器校验 Token，解析后将 user-id/username 注入请求头
  - 白名单路径（登录、注册、文章公开浏览）无需 Token
- **前端适配**：请求需携带 `Authorization: Bearer {token}` 头

### 2.3 敏感信息脱敏
- **之前**：接口直接返回 User PO，包含 password 字段
- **之后**：所有接口返回 VO 对象，排除 password 等敏感字段
- User 实体 password 字段增加 `@JsonIgnore` 双重保护

---

## 三、并发与数据一致性

### 3.1 点赞并发安全
- **之前**：先查后插，并发下可能重复点赞
- **之后**：
  - 数据库加唯一索引 `UNIQUE(article_id, user_id)`（需执行 SQL 脚本）
  - 代码捕获 `DuplicateKeyException` 处理并发冲突
  - 点赞数 -1 改为 `GREATEST(like_count - 1, 0)` 防止负数

### 3.2 文章版本号
- **之前**：`count + 1` 计算版本号，并发下可能重复
- **之后**：查最大版本号 +1，数据库加唯一索引 `UNIQUE(article_id, version)` 兜底

### 3.3 分类删除事务
- **之前**：无事务，先查文章再删，中途失败数据不一致
- **之后**：Service 层加 `@Transactional`，直接用 SQL 批量更新（不需要先查），文章分类置空

---

## 四、性能优化

### 4.1 浏览量 Redis 计数
- **之前**：每次访问文章都 UPDATE 数据库 + 删缓存，缓存形同虚设
- **之后**：
  - 浏览量用 Redis `INCR` 原子递增，不写数据库
  - 定时任务每 5 分钟批量落库（`flushViewCountToDB`）
  - 文章详情缓存正常使用，不随浏览量删除

### 4.2 Redis keys 改 scan
- **之前**：`deleteByPrefix` 用 `KEYS` 命令，大数据量下阻塞 Redis
- **之后**：改用 `SCAN` 迭代遍历，避免阻塞

### 4.3 评论分页
- **之前**：返回某文章全部评论，无分页
- **之后**：改为分页查询，默认每页 10 条，最大 100 条

---

## 五、代码质量

### 5.1 DTO/VO 分层
- **之前**：PO 实体直接作为 API 入参和返回值
- **之后**：
  - 新增 `dto` 包：接收前端参数（带校验注解）
  - 新增 `vo` 包：返回前端数据（排除敏感字段）
  - PO 仅用于数据库操作

### 5.2 Controller 瘦身
- **之前**：Controller 中直接写业务逻辑（版本保存、删文章等）
- **之后**：业务逻辑全部下沉到 Service 层，Controller 只做参数接收和返回

### 5.3 统一异常处理
- **之前**：部分地方 `return Result.error()`，部分地方抛异常
- **之后**：统一抛 `BusinessException`，由全局异常处理器处理

### 5.4 参数校验
- **之前**：大部分接口无参数校验
- **之后**：所有 `@RequestBody` 加 `@Valid`，DTO 字段加 `@NotBlank`/`@Size`/`@NotNull`
- 分页参数加 `@Min(1)`/`@Max(100)` 防止恶意传参

### 5.5 MyBatis-Plus 自动填充
- **之前**：有 `@TableField(fill=...)` 注解但无 `MetaObjectHandler` 实现，时间字段不自动填充
- **之后**：common-blog 新增 `MyMetaObjectHandler`，所有实体时间字段加 fill 注解

### 5.6 注入方式统一
- 统一使用构造器注入（`@RequiredArgsConstructor`），替代 `@Autowired`/`@Resource`

---

## 六、网关改动

### 6.1 JWT 全局鉴权过滤器
- 白名单路径直接放行
- 其他路径校验 Token，无效返回 401
- 解析后将 user-id、username 注入请求头传递给下游服务

### 6.2 CORS 跨域
- 网关层统一配置跨域，下游服务无需单独配置
- 允许所有来源、所有方法、所有头，允许携带凭证

### 6.3 白名单路径
```
/api/user/login          - 登录
/api/user/register       - 注册
/api/article/page        - 文章分页
/api/article/*           - 文章详情（GET）
/api/article/comment/list/*  - 评论列表
/api/article/category/list   - 分类列表
```

---

## 七、数据库变更

需执行 `sql/upgrade_indexes.sql` 脚本，主要添加：

| 表 | 索引 | 类型 | 用途 |
|----|------|------|------|
| t_user | uk_username | UNIQUE | 防重复注册 |
| t_article_like | uk_article_user | UNIQUE | 防并发重复点赞 |
| t_article_version | uk_article_version | UNIQUE | 防版本号重复 |
| t_article | idx_user_id, idx_category_id, idx_create_time | NORMAL | 查询优化 |
| t_comment | idx_article_id, idx_user_id | NORMAL | 查询优化 |

**注意**：添加唯一索引前需先清理已有重复数据，脚本中有清理 SQL 注释。

---

## 八、配置变更

### 8.1 新增 application.yml
- user-blog：JWT 配置
- article-blog：Redis 配置（从 bootstrap.yml 移入）、日志
- gateway-blog：JWT 配置、日志
- ai-blog：完善配置

### 8.2 JWT 配置
```yaml
jwt:
  secret: lsj-blog-secret-key-2024-must-be-at-least-256-bits-long-for-hs256
  expire: 86400000  # 24小时
```
**生产环境务必修改 secret！**

### 8.3 Nacos 配置
- Nacos 配置保持不变
- 各服务的端口、数据源等仍从 Nacos 读取

---

## 九、前端适配要点

1. **登录接口**：返回值从 `User` 变为 `LoginVO { token, user }`，需保存 token
2. **请求头**：所有需登录的接口携带 `Authorization: Bearer {token}`
3. **用户信息**：接口返回的 User 不再包含 password 字段（本来也不该有）
4. **评论接口**：`/article/comment/list/{articleId}` 改为分页，需传 pageNum/pageSize
5. **文章新增/修改**：入参从 Article PO 变为 ArticleDTO，字段有调整
6. **401 处理**：Token 过期或无效时网关返回 401，前端需跳转登录页

---

## 十、部署注意事项

1. **先执行数据库脚本**：`sql/upgrade_indexes.sql`
2. **密码迁移**：已有用户密码是明文，需重置或写迁移脚本批量加密
3. **Redis**：确保 Redis 可用，浏览量计数依赖 Redis
4. **启动顺序**：Nacos → 各微服务 → 网关
5. **JWT 密钥**：生产环境修改 `jwt.secret`，且网关和用户服务必须一致
6. **定时任务**：article-blog 开启了 `@EnableScheduling`，每5分钟执行浏览量落库
