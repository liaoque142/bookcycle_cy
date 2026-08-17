# 变更日志

## 2026-08-17
- 整合根目录文档：修订 CLAUDE.md（修正命令路径、合并 token.md 规则、去重、补充文件头），删除 token.md。
- 记录本机开发环境基线（JDK 21.0.11、Maven 3.8.9、Node 24.9.0、npm 11.6.0、MySQL 8.4.5 端口 3306），标注 Redis/Docker/Nginx 未安装；CLAUDE.md 新增「本机开发环境」，plan.md 技术选型标注本机版本。
- 按评审修订 plan.md 需求：补齐 favorite/trade_evaluation/refund/forum_board/forum_like/blacklist 六表；信誉规则改数据驱动并澄清歧义；交易一期做出售+免费赠送（交换二期）；补防超卖、自提、拉黑、RBAC 统一账号（删除 admin 表）、错误码规范；阶段改为 7 段；风险表补三条。
- 初始化 Frontend：Vite 8 + Vue 3 + TS + Element Plus + Pinia + Router 脚手架，创建极简白灰风完整首页（吸顶导航、Hero 书脊书架、数据条、分类导航、精选书市、最新上架、书友论坛、平台优势、Footer），书籍/帖子用 mock 数据；TypeScript 固定 5.9.x 以兼容 vue-tsc 3.x。
- 重写 PROGRESS.md 对齐实际状态：当前阶段改为阶段一（原记录误写阶段二/已完成 JWT 与 schema.sql，实际 Backend 为空、sql/ 未创建）；补入 CLAUDE.md 进度管理、plan.md 阶段一待办与功能建议；记录 XML Mapper 决策与 plan.md 技术选型（MyBatis-Plus）不一致待同步。
