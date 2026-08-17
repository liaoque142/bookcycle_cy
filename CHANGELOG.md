# 变更日志

## 2026-08-17
- 整合根目录文档：修订 CLAUDE.md（修正命令路径、合并 token.md 规则、去重、补充文件头），删除 token.md。
- 记录本机开发环境基线（JDK 21.0.11、Maven 3.8.9、Node 24.9.0、npm 11.6.0、MySQL 8.4.5 端口 3306），标注 Redis/Docker/Nginx 未安装；CLAUDE.md 新增「本机开发环境」，plan.md 技术选型标注本机版本。
- 按评审修订 plan.md 需求：补齐 favorite/trade_evaluation/refund/forum_board/forum_like/blacklist 六表；信誉规则改数据驱动并澄清歧义；交易一期做出售+免费赠送（交换二期）；补防超卖、自提、拉黑、RBAC 统一账号（删除 admin 表）、错误码规范；阶段改为 7 段；风险表补三条。
- 初始化 Frontend：Vite 8 + Vue 3 + TS + Element Plus + Pinia + Router 脚手架，创建极简白灰风完整首页（吸顶导航、Hero 书脊书架、数据条、分类导航、精选书市、最新上架、书友论坛、平台优势、Footer），书籍/帖子用 mock 数据；TypeScript 固定 5.9.x 以兼容 vue-tsc 3.x。
- 重写 PROGRESS.md 对齐实际状态：当前阶段改为阶段一（原记录误写阶段二/已完成 JWT 与 schema.sql，实际 Backend 为空、sql/ 未创建）；补入 CLAUDE.md 进度管理、plan.md 阶段一待办与功能建议；记录 XML Mapper 决策与 plan.md 技术选型（MyBatis-Plus）不一致待同步。
- CLAUDE.md Git 规则改为自动提交：`git add` + `git commit` + `git push` 全部自动执行；同步项目至 GitHub 私有仓库（origin=https://github.com/liaoque142/BookCycle），远端初始 README 提交经 rebase 保留。
- 配置本机 MySQL MCP（`.mcp.json`）：使用 `@benborla29/mcp-server-mysql` v2.0.9 连接本机 MySQL 8.4.5（root@127.0.0.1:3306），密码经 `MYSQL_PASS` 环境变量引用（setx 持久化），不入库；实测连接成功，可跨库查询，现有 18 个数据库。
- 前端路由骨架 + 导航修复：Header/Footer 从 `components/home/` 移至 `components/layout/` 并抽为全局布局（App.vue）；Home.vue 只保留内容区；新增 `/books` `/forum` `/about` 占位页（复用 `PagePlaceholder` 组件）与 404 兜底页，路由全部懒加载 + scrollBehavior；Header 导航高亮由写死 `n.to === '/'` 改为 `useRoute()` 随路由联动；"查看全部 →" 指向 `/books`。（涉及：App.vue、Home.vue、router/index.ts、HomeHeader/HomeFooter、HomeBookSection、新增 views/Books|Forum|About|NotFound、components/common/PagePlaceholder）
- 前端 API 层 + 类型契约骨架：新建 `types/api.ts`（Result/Page）、`types/user.ts`（User/LoginParams/LoginResult）、`types/book.ts` 补 BookQuery；`api/request.ts` axios 封装（baseURL `/api/v1`、请求带 token、响应解包 Result、401 清登录态）；`api/mock.ts`（USE_MOCK 开关 + delay + toPage）；分模块 API `book.ts`/`forum.ts`/`auth.ts`（mock 分支复用 `src/data/`，`VITE_API_MOCK=false` 切真实）；Pinia `stores/auth.ts` 登录 mock（token/user 持久化 localStorage）；Home.vue 改为 onMounted 经 API 取数，组件零改动。
