# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目信息
- 项目名称：BookCycle（书循环）
- 技术栈：Java 21 + Spring Boot 3 + Vue 3 + MySQL 8 + Redis
- 目录结构：
  - `Backend/` 后端 Spring Boot 项目
  - `Frontend/` 前端 Vue 3 项目
  - `sql/` 数据库脚本（待创建）
  - `docs/` 文档（待创建）
- 详细需求见 [plan.md](plan.md)

## 本机开发环境（以本机为准，2026-08-17 实测）
| 工具 | 版本 | 状态 |
|------|------|------|
| JDK | 21.0.11 LTS | 已安装 |
| Maven | 3.8.9 | 已安装 |
| Node.js | 24.9.0 | 已安装 |
| npm | 11.6.0 | 已安装 |
| MySQL | 8.4.5（服务 MySQL84，端口 3306） | 已安装运行 |
| Git | 2.54.0 | 已安装 |
| Redis | — | 未安装，相关功能需先安装 |
| Docker / Nginx | — | 未安装，部署方案需先安装 |
| pnpm / yarn | — | 未安装，前端用 npm |

## 常用命令
- 后端启动：`cd Backend && mvn spring-boot:run`
- 后端测试：`cd Backend && mvn test`
- 前端启动：`cd Frontend && npm run dev`
- 前端构建：`cd Frontend && npm run build`
- 构建/测试加静默参数：`mvn -q compile`、`npm install --silent`；测试只输出摘要

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
2. 修改前先定位：用 grep 定位关键字，再按需 Read（offset/limit），单次不超过 500 行。
3. 不读 `node_modules`、`target`、`dist` 等构建产物。
4. 局部修改优先 Edit，不重写整个文件。
5. 遵循现有代码风格，不随意重构。
6. 数据库变更必须同步更新实体、Mapper、DTO、SQL。
7. 接口变更同步更新 Swagger 和前端 API。
8. 密码 BCrypt 加密，用户输入必须校验。
9. 优先逻辑删除。

## 输出要求
- 中文简洁回答，只输出关键差异或代码片段。
- 修改文件时说明文件路径和修改内容。
- 不输出完整大文件，只展示关键代码，避免输出大段日志和无关表格。
- 任务开始列出 TODO 清单，阶段完成用 2-3 句话总结。
- 对话变长时主动压缩历史，避免重复粘贴。
- 不确定的点先询问，不擅自猜测；复杂任务先说明计划再执行。

## 禁止事项
- 修改与任务无关的文件。
- 删除或忽略已有测试。
- 未经确认执行危险数据库操作。
- 绕过认证或安全校验。
- 前端硬编码敏感信息。
- 自动执行 git add/commit/push。
