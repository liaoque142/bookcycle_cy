# CLAUDE.md

BookCycle（书循环）：二手书交易 + 社区论坛。技术栈 Java 21 + Spring Boot 3 + Vue 3 + MySQL 8 + MyBatis（XML Mapper）。

**文档分工（按需读，勿整读）：**

| 文件 | 用途 | 何时读 |
|---|---|---|
| `PROGRESS.md` | 当前状态 / 待办 / 风险（≤40 行） | 每个任务开始时 |
| `DECISIONS.md` | 设计决策与踩坑库（23 条） | 只在涉及相关模块时 Grep 定位、局部读 |
| `plan.md` | 完整需求与 7 阶段规划 | 只读相关章节（offset/limit） |
| `README.md` | 接口速查 / 启动 / 测试 / 测试账号 | 需要查接口或跑冒烟时 |
| `TOKENS.md` | 完整 token 纪律细则 | 新会话或大任务开始时读一次 |

## 目录结构
- `Backend/` 后端 Spring Boot 项目
- `Frontend/` 前端 Vue 3 项目
- `sql/` 数据库脚本：`schema.sql`（25 张表）、`seed.sql`、`upgrade_*.sql`（升级脚本，勿重复执行）

## 本机环境（2026-08-17 实测）
JDK 21.0.11 LTS · Maven 3.8.9 · Node.js 24.9.0 · npm 11.6.0 · MySQL 8.4.5（服务 MySQL84 / 端口 3306） · Git 2.54.0
未安装：Redis、Docker、Nginx、pnpm/yarn（相关功能需先安装）

## 常用命令
- 后端启动：`cd Backend && mvn spring-boot:run`（Git Bash 需内联注入 `MYSQL_PASS`，见 README）
- 后端编译 / 测试：`mvn -q compile`、`mvn test`（测试只输出摘要）
- 前端启动：`cd Frontend && npm run dev`；构建：`npm run build`
- 依赖安装：`npm install --silent`

## Token 纪律（每次生效的铁律）
1. 任务开始只读 `PROGRESS.md`；**不读** plan.md / README.md / DECISIONS.md —— 确有需要时再按需读。
2. 先 Grep 定位行号，再用 Read `offset`/`limit` 局部读；单次 Read ≤300 行，不整读 >200 行的文件。
3. 禁读 `node_modules/`、`target/`、`dist/`、`*.lock`、会话转录 `*.jsonl`。
4. 不回读刚编辑过的文件；不复述文件全文、不贴日志原文，只输出关键差异。
5. 阶段收尾固定四步：改 `PROGRESS.md` 状态行 → 新决策 append 到 `DECISIONS.md` → git commit → **提示用户开新会话**（新任务一律新开会话，不用 `-c` / `--resume` 续接长会话）。

细则见 [TOKENS.md](TOKENS.md)。

## Git 规则
- 若项目根目录无 `.git`，执行 `git init` 初始化仓库。
- 任务完成后自动执行 `git add` + `git commit` + `git push`（conventional commit 格式）。
- 不再维护 `CHANGELOG.md`：变更历史由 `git log --oneline` 承接。

## 代码规范

### Java
- 使用 Lombok，Controller 只做参数校验，业务在 Service；多表写操作加 `@Transactional`。
- 用 MyBatis XML Mapper 编写 SQL，禁止在 Java 代码中拼接 SQL。
- 参数用 `#{}` 预编译占位符，禁止 `${}` 拼接用户输入，防止 SQL 注入。
- 简单 SQL 可在 Mapper 接口用注解（`@Select` / `@Insert` / `@Update` / `@Delete`），复杂 SQL 必须写在 XML 映射文件。
- 分页查询统一用 PageHelper（`PageHelper.startPage()`），禁止手动拼接 LIMIT。
- 统一响应类 `Result<T>`，全局异常处理。

### Vue 3
- 使用 TypeScript 和 `<script setup lang="ts">`。
- 状态管理用 Pinia，API 封装在 `src/api/`。
- 路由懒加载，样式 scoped。

### XML Mapper
- 映射文件放在 `Backend/src/main/resources/mapper/`，命名与 Mapper 接口一致（如 `UserMapper.xml`）。
- `namespace` 必须是 Mapper 接口全限定名；每个 SQL 语句的 `id` 与接口方法名一致。
- 数据库变更必须同步更新：实体、Mapper 接口、XML 映射、DTO、SQL 脚本。

## 工作准则
1. 一次只完成一个明确任务，不扩大范围。
2. 局部修改优先 Edit，不重写整个文件；遵循现有代码风格，不随意重构。
3. 数据库变更同步更新实体、Mapper、DTO、SQL；接口变更同步更新 Swagger 和前端 API。
4. 密码 BCrypt 加密，用户输入必须校验；优先逻辑删除。
5. 不确定的点先询问，不擅自猜测；复杂任务先说明计划再执行。

## 禁止事项
- 修改与任务无关的文件；删除或忽略已有测试。
- 未经确认执行危险数据库操作。
- 绕过认证或安全校验。
- 前端硬编码敏感信息。
- 修改 BookCycle 以外的本机配置。
