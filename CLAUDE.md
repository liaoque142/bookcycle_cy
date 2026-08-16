# Claude Code 工作约束

## 项目信息
- 项目名称：BookCycle（书循环）
- 技术栈：Java 21 + Spring Boot 3 + Vue 3 + MySQL 8 + Redis
- 目录结构：
  - `BookCycle/Backend/` 后端 Spring Boot 项目
  - `BookCycle/Frontend/` 前端 Vue 3 项目
  - `BookCycle/sql/` 数据库脚本
  - `BookCycle/docs/` 文档

## 常用命令
- 后端启动：`cd BookCycle/Backend && mvn spring-boot:run`
- 后端测试：`cd BookCycle/Backend && mvn test`
- 前端启动：`cd BookCycle/Frontend && npm run dev`
- 前端构建：`cd BookCycle/Frontend && npm run build`

## Git 规则
- 若项目根目录无 `.git`，执行 `git init` 初始化仓库。
- **禁止自动执行** `git add`、`git commit`、`git push`，提交由用户手动完成。
- 每次任务完成后，在 `CHANGELOG.md` 中追加修改说明（日期、内容、涉及文件）。

## 代码规范
### Java
- 使用 Lombok，Controller 只做参数校验，业务在 Service。
- 多表写操作必须加 `@Transactional`。
- 使用 MyBatis-Plus，禁止 SQL 拼接。
- 统一响应类 `Result<T>`，全局异常处理。

### Vue 3
- 使用 TypeScript 和 `<script setup lang="ts">`。
- 状态管理用 Pinia，API 封装在 `src/api/`。
- 路由懒加载，样式 scoped。

## 工作准则
1. 一次只完成一个明确任务，不扩大范围。
2. 修改前先按需读取相关文件（用 grep 定位）。
3. 遵循现有代码风格，不随意重构。
4. 数据库变更必须同步更新实体、Mapper、DTO、SQL。
5. 接口变更同步更新 Swagger 和前端 API。
6. 密码 BCrypt 加密，用户输入必须校验。
7. 优先逻辑删除。
8. 不自动执行 git 提交，修改记录写入 `CHANGELOG.md`。

## 输出要求
- 中文简洁回答，只输出关键差异或代码片段。
- 修改文件时说明文件路径和修改内容。
- 不输出完整大文件。

## 禁止事项
- 修改与任务无关的文件。
- 删除或忽略已有测试。
- 未经确认执行危险数据库操作。
- 绕过认证或安全校验。
- 前端硬编码敏感信息。
- 自动执行 git add/commit/push。