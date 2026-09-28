# 智慧社区食堂管理系统

基于 Spring Boot、MyBatis、MySQL、Redis 和 Vue 2 的社区食堂管理项目，包含菜品、分类、居民、订单和 AI 推荐功能。

## 目录

- `kitchen-backend`：Java 后端。
- `kitchen-frontend`：Vue 前端。
- `kitchen-backend/sql/db_kitchen.sql`：仅包含表结构，不含原始账号、订单或居民数据。

## 本地运行

1. 准备 JDK 8、Maven、MySQL 8 和 Redis；前端使用与 Vue CLI 4 兼容的 Node.js 环境。
2. 创建 `db_kitchen` 数据库，在空数据库中导入 SQL 表结构。
3. 在启动后端的进程环境中设置 `MYSQL_USERNAME`、`MYSQL_PASSWORD`；按需设置 `MYSQL_DB_NAME`、`REDIS_HOST`、`REDIS_PORT`、`REDIS_PASSWORD`。
4. AI 功能使用后端环境变量 `OPENAI_API_KEY`、`OPENAI_API_URL`、`OPENAI_MODEL`，按所用服务填写。未配置密钥时使用代码中的本地推荐回退逻辑。不要把密钥放入 `VUE_APP_*` 变量。
5. 在 `kitchen-backend` 运行 `mvn spring-boot:run`。
6. 在 `kitchen-frontend` 运行 `npm ci` 和 `npm run dev`；生产构建命令为 `npm run build:prod`。

后端默认端口为 9111，上下文路径为 `/MealManager`。前端可复制 `.env.local.example` 为 `.env.local` 配置后端地址。后端需使用系统环境变量或 IDE 运行配置；不会自动加载 `.env` 文件。

## 数据与隐私

此版本不提供原始居民资料、账号密码、订单记录及上传图片。首次使用需自行注册账号、配置角色并录入菜品。发布时未包含开发工具配置、依赖目录、构建产物和旧 Git 历史。

源码脱敏不代表已完成应用安全审计。本次未进行依赖安装及完整运行验证。
