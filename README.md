# RAG Platform Skeleton

基于 `Spring Boot + Spring AI + PostgreSQL(pgvector) + Redis + RocketMQ` 的企业级知识库 RAG 平台基础骨架。

## 模块说明

- `rag-app`: 启动模块与集中配置
- `rag-common`: 通用返回、异常、上下文与基础工具
- `rag-domain`: 领域实体、仓储接口、端口定义
- `rag-infra`: MyBatis-Plus 持久化、Redis、RocketMQ、Spring AI 适配
- `rag-rag`: 核心业务服务，包含知识库、文档导入、索引编排、问答流程
- `rag-web`: REST API 与安全鉴权
- `rag-scheduler`: MQ 消费与后台任务执行

## 本地启动

1. 启动依赖：

```bash
docker compose up -d
```

2. 配置模型：

```bash
export OPENAI_BASE_URL=https://your-compatible-endpoint
export OPENAI_API_KEY=your-api-key
export OPENAI_CHAT_MODEL=gpt-4o-mini
export OPENAI_EMBEDDING_MODEL=text-embedding-3-small
```

3. 启动应用：

```bash
mvn -pl rag-app spring-boot:run
```

默认管理员账号在 [application.yml](/mnt/d/Amane/rag/rag-app/src/main/resources/application.yml) 中：

- 用户名：`admin`
- 密码：`ChangeMe123!`

## 当前提供的基础能力

- JWT 登录鉴权
- 知识库创建、查询、启停
- 文档上传、异步索引任务投递
- RocketMQ 消费文档索引任务
- 文本切片、pgvector 索引接入与降级检索
- 基于 Spring AI 的问答编排与模型缺省降级
- Redis 登录态缓存

## 关键接口

- `POST /api/v1/auth/login`
- `GET /api/v1/auth/me`
- `POST /api/v1/knowledge-bases`
- `GET /api/v1/knowledge-bases`
- `PATCH /api/v1/knowledge-bases/{id}/status`
- `POST /api/v1/documents/upload`
- `GET /api/v1/documents`
- `GET /api/v1/tasks`
- `POST /api/v1/chat/ask`
