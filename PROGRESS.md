# 项目进度

## 当前阶段
阶段一：项目初始化、用户认证与最小闭环

## 已完成
- [x] 需求整合修订（plan.md：7 阶段划分、补 6 张表、信誉规则改数据驱动、错误码规范）
- [x] CLAUDE.md 环境基线（JDK 21 / MySQL 8.4.5 / Node 24.9 / npm 11.6）+ 进度管理配置
- [x] 前端脚手架初始化（Vite 8 + Vue 3 + TS + Element Plus + Pinia + Router）
- [x] 前端首页（极简白灰风：导航 / Hero 书架 / 数据条 / 分类导航 / 精选书市 / 最新上架 / 论坛 / 平台优势 / Footer，mock 数据）

## 进行中
- [ ] 后端项目初始化（Backend 目录为空，尚未开工）

## 待办（阶段一，按序）
- [ ] 数据库设计（sql/schema.sql，sql/ 目录待创建，表结构见 plan.md §7）
- [ ] 后端脚手架（Spring Boot 3 + MyBatis XML Mapper + PageHelper）
- [ ] 注册登录 + JWT 认证（Spring Security + BCrypt，RBAC 统一账号）
- [ ] 最小闭环：注册 → 发布书籍 → 书籍详情 → 浏览

## 决策记录
- 2026-08-17：持久层用 MyBatis XML Mapper + PageHelper，不用 MyBatis-Plus（⚠️ plan.md 技术选型仍写 MyBatis-Plus，待同步修订）
- 2026-08-17：RBAC 统一账号（用户/管理员同走 sys_user + role，无独立 admin 表）
- 2026-08-17：优先逻辑删除；接口前缀 /api/v1

## 阻塞/风险
- Redis 未安装，缓存/验证码/Token 黑名单需先安装（降级方案见 plan.md 风险表）
- Backend 未初始化，后端开发尚未开始
- 支付合规：初期模拟支付，真实支付需资质
