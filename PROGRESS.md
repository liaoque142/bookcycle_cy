# 项目进度

## 当前阶段
阶段一：项目初始化、用户认证与最小闭环

## 已完成
- [x] 需求整合修订（plan.md：7 阶段划分、补 6 张表、信誉规则改数据驱动、错误码规范）
- [x] CLAUDE.md 环境基线（JDK 21 / MySQL 8.4.5 / Node 24.9 / npm 11.6）+ 进度管理配置
- [x] 前端脚手架初始化（Vite 8 + Vue 3 + TS + Element Plus + Pinia + Router）
- [x] 前端首页（极简白灰风：导航 / Hero 书架 / 数据条 / 分类导航 / 精选书市 / 最新上架 / 论坛 / 平台优势 / Footer，mock 数据）
- [x] 前端路由骨架 + 导航修复（Header/Footer 抽为全局布局；新增 /books /forum /about 占位页 + 404 兜底；导航高亮随路由联动；懒加载）
- [x] 前端 API 层 + 类型契约骨架（Result/Page/实体类型、axios 封装解包 Result、分模块 API：book/forum/auth、Pinia 登录 mock，VITE_API_MOCK 控制 mock/真实切换）
- [x] 前端核心页面铺设（书市列表搜索/筛选/分页、书籍详情、登录/注册、发布书籍、个人中心；路由鉴权守卫 + Header 登录态接线；BookCard 接详情跳转）
- [x] 数据库设计（sql/schema.sql 25 张表 + sql/seed.sql 初始数据：角色/分类，对齐 plan.md §7 与前端契约）并已应用到本机 bookcycle 库（utf8mb4 验证通过）
- [x] 后端脚手架（Spring Boot 3.5.16 + MyBatis XML Mapper + PageHelper 6.1.1，冒烟接口 GET /api/v1/categories 分页验证通过）

## 进行中
- [ ] 后端项目初始化（Backend 目录为空，尚未开工）
- [ ] 前端剩余页面（论坛详情、交易流程 UI、管理后台，待对应后端阶段）

## 待办（阶段一，按序）
- [ ] 注册登录 + JWT 认证（Spring Security + BCrypt，RBAC 统一账号）
- [ ] 最小闭环：注册 → 发布书籍 → 书籍详情 → 浏览

## 决策记录
- 2026-08-17：持久层用 MyBatis XML Mapper + PageHelper，不用 MyBatis-Plus（⚠️ plan.md 技术选型仍写 MyBatis-Plus，待同步修订）
- 2026-08-17：RBAC 统一账号（用户/管理员同走 sys_user + role，无独立 admin 表）
- 2026-08-17：优先逻辑删除；接口前缀 /api/v1
- 2026-08-17：前端 mock 先行——API 函数默认返回 mock（`VITE_API_MOCK=false` 切真实）；`Book` 保持展示用 VO 形态，后端对接返回含 categoryName/sellerName 的 VO
- 2026-08-17：发布书籍图片暂用自动 CSS 书封（按分类取色板），不接上传；交易/收藏/订单流程属阶段三
- 2026-08-17：数据库无物理外键、用逻辑索引；枚举存 TINYINT 码 + 注释，展示中文由后端 VO 映射；通用字段 id/create_time/update_time/deleted（纯追加表只留 id+create_time）
- 2026-08-17：评审建议（待对应阶段执行）——书籍讨论区提前至阶段二；阶段四拆序（先论坛发帖/评论/点赞，后好友/通知）；阶段四补 seed 论坛板块数据；ForumPost 契约拆分 + 回复数聚合
- 2026-08-17：后端 Spring Boot 3.5.16（plan.md 3.3.x 更新到 3.5.x 稳定线）；PageHelper 用独立 pagehelper 6.1.1 + MyBatis starter 3.x 自动注册 Interceptor bean（不引 pagehelper-spring-boot-starter，其面向 Boot 2）；Backend/.mvn/settings.xml 走阿里云镜像（本机直连 Maven Central 被墙，不改本机 ~/.m2）

## 阻塞/风险
- Redis 未安装，缓存/验证码/Token 黑名单需先安装（降级方案见 plan.md 风险表）
- Backend 未初始化，后端开发尚未开始
- 支付合规：初期模拟支付，真实支付需资质
