# 项目进度

## 当前阶段
阶段五：管理后台（用户/分类/书籍/交易管理 + 数据统计 + 系统配置 + 操作日志已上线，剩退款售后）

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
- [x] 注册登录 + JWT 认证（Spring Security + BCrypt，RBAC 统一账号；seed 管理员 admin）
- [x] 后端 Book 模块（发布/列表/详情 + BookVO 映射：分类名/枚举中文/卖家昵称/信誉）
- [x] 最小闭环前端接通真实接口（`VITE_API_MOCK=false`）：后端补 `/books/featured`、`/books/latest` 只读接口；前端入参编码映射（分类名→categoryId、tradeType/condition 中文→码）、分类字典 api/category.ts、BookCover 按分类色板 fallback、401 跳登录页、vite dev 代理 `/api/v1`→8080；冒烟全链路（注册/登录/发布/精选/最新/筛选/详情/401/游客放行）通过
- [x] 阶段二论坛模块（后端 + 前端）：forum_board/post/comment/like 四表已落地（schema 原建）；后端 9 个接口（板块/列表/详情/发帖/编辑/删除/评论/点赞，点赞幂等 + @Transactional 同步 forum_like 与 like_count，回复数相关子查询聚合 forum_comment，GET /forum/** 游客放行）；seed 补 4 板块 + 2 条管理员示例帖；前端 types/forum.ts 契约 + api/forum.ts 全接口 + Forum.vue 论坛页（板块 Tab/搜索/latest-hot/分页/发帖弹窗）+ ForumDetail.vue（详情/点赞/评论/编辑/删除）+ 路由 /forum/posts/:id + HomeForum 适配；冒烟（游客/发帖/点赞幂等/取消/评论/编辑/删除/越权 403/无 token 401）+ 经 vite 代理 E2E 全通过，mvn test 与 vue-tsc/vite build 通过
- [x] 阶段三交易与信誉（后端 + 前端）：地址 CRUD + 设默认（首条自动默认）；下单（出售/赠送、邮寄/自提，书籍状态 CAS 在售→已预订防超卖，信誉 <40 冻结 / <60 禁购买）；模拟支付写 payment；取消释放书籍 + 已支付自动模拟退款 + 买家待发货取消扣 MALICIOUS_CANCEL -3；发货/确认收货（书转已售出、双方 TRADE_SUCCESS +2）；互评（uk 幂等、好评 ≥4 分 GOOD_REVIEW +1）；credit_rule 数据驱动（seed 7 条）+ credit_log 日志 + /credit/me、/credit/logs；前端 Address/Orders/Credit 三页 + 路由 + BookDetail 下单流（选地址/自提/模拟支付）+ Profile 接线；冒烟（防超卖/支付/发货/收货/互评幂等/信誉增减/取消扣分/退款/越权 403/401）+ 经 vite 代理 E2E 全通过，mvn test 与 vue-tsc/vite build 通过，冒烟数据已清理
- [x] 阶段四社交（后端 + 前端）：用户搜索 `GET /friends/search`；好友 `GET /friends`、`POST /friends/requests`（幂等：已是好友/待处理申请/双向拉黑拦截）、`GET /friends/requests`、`PUT /requests/{id}`（accept 双向 upsert friend、reject）、`DELETE /friends/{userId}`（双向 status=0）；黑名单 `GET|POST /api/v1/blacklist`、`DELETE /{userId}`（拉黑自动解除好友、INSERT IGNORE 幂等，双向阻断好友申请 + 下单）；书籍讨论 `GET|POST /api/v1/books/{id}/discussions`（游客可看、树组装）、`DELETE /{id}`（本人或书作者）；通知 `GET /api/v1/notifications`（分页/unreadOnly/typeName CASE）、`GET /unread-count`、`PUT /{id}/read`、`PUT /read-all`，交易全流程 + 好友申请触发（type 2/3）；前端 Friends/Notifications 页 + 路由、BookDetail 讨论区、HomeHeader 铃铛红点（30s 轮询）+ 下拉入口、Profile 菜单接通；冒烟 33 项全通过 + vue-tsc/build + 经 vite 代理 E2E 通过，冒烟数据已清理
- [x] 阶段五管理后台（后端第一批）：SecurityConfig 加 `.requestMatchers("/api/v1/admin/**").hasRole("admin")`（未登录 401、普通用户 403，JWT filter 已注入 ROLE_）；操作日志 operation_log（实体/Mapper/Service record+page，`@Transactional` 内同事务记录，查询接口 `GET /admin/operation-logs`）；用户管理 `GET /admin/users`（keyword/status 筛选分页）、`GET /users/{id}` 详情、`PUT /users/{id}/status`（禁管理员账号拦截、禁用后登录被拦 403）、`PUT /users/{id}/credit`（信用日志记操作人，禁调自己）；分类管理 `GET /admin/categories`（扁平组树）、`POST`、`PUT /{id}`、`DELETE /{id}`（同级同名去重、父分类存在性、删前查子分类/书籍占用）；书籍管理 `GET /admin/books`（全状态含已下架，keyword/status/tradeType/categoryId 筛选）、`PUT /{id}/status`（下架 1→4 / 上架 4→1，CAS 校验交易中不可操作）；BookVO 补 statusName（SQL CASE）；冒烟 20 项全通过（含越权 403/未登录 401/禁用登录 403/重复分类 400/交易中书下架 400/非法状态 400/缺参数 400/不存在 404），mvn test 通过，冒烟数据已清理
- [x] 阶段五管理后台（前端）：`types/admin.ts` + `api/admin.ts`（全接口 + mock 分支）；`/admin` 嵌套路由 + `AdminLayout`（深色侧边栏 el-menu + 顶栏返回前台/退出，requiresAdmin 守卫：未登录跳登录、非 admin 回首页）；`AdminUsers` 用户管理（keyword/status 筛选 + 表格 + 详情抽屉 + 禁用/启用确认 + 调整信誉分弹窗，admin 账号禁禁用置灰）；`AdminCategories` 分类管理（扁平组树 el-table tree + 新增根/新增子/编辑/删除 + 父分类下拉、启用开关）；`AdminBooks` 书籍管理（keyword/status/交易方式/分类筛选 + 表格 + 下架/上架，交易中按钮置灰）；`AdminOperationLogs` 操作日志（action 筛选 + 分页表格）；HomeHeader 登录下拉加「管理后台」入口（仅 admin 显示）；冒烟经 vite 代理：admin 各接口 200、无 token 401、普通用户 403、分类 CRUD + 同名 400、操作日志联表记录，vue-tsc/vite build 通过，冒烟数据已清理
- [x] 阶段五剩余第一批（交易管理/数据统计/系统配置）：**交易管理** `GET /admin/orders`（keyword 匹配订单号/书名/买卖方用户名 + status/tradeType 筛选分页，复用 orderVoColumns 加额外条件）、`GET /admin/orders/{orderNo}` 详情；**系统配置** `GET /admin/credit-rules`（7 条规则按 priority）+ `PUT /credit-rules/{id}/status`（启用/禁用，操作日志 TOGGLE_CREDIT_RULE，禁用后 CreditService.apply 自动忽略）；**数据统计** `GET /admin/stats/overview`（用户/书籍/订单/今日订单/今日新增/交易额/待处理售后）+ `GET /admin/stats/trend?days=1-30`（每日订单/新增用户按日聚合 + Java 补零对齐，独立 StatsMapper 注解聚合）；前端 `AdminOrders`（筛选表格 + 详情抽屉 + 分页）、`AdminStats`（概览卡片 + echarts 7 日趋势，core 按需引入 chunk 1.1MB→513KB）、`AdminConfig`（规则表格 + 启用/禁用开关确认）三页 + AdminLayout 菜单（购物车/趋势/设置图标）+ 路由；冒烟（overview/trend 补零/7 规则/toggle 往返/订单筛选/404/401）+ mvn test + vue-tsc/vite build + 经 vite 代理 E2E 全通过，冒烟操作日志已清理

## 进行中
- [ ] 阶段五剩余（退款售后：需先补用户侧申请退款接口 `POST /orders/{orderNo}/refund` + 管理后台 refund 列表/处理）→ 阶段六 swap（书友互换）→ 阶段七 测试与部署（Redis/Docker/Nginx 需先安装）

## 待办（阶段一，按序）
- [x] 最小闭环（认证 + Book）：后端 + 前端接通真实接口均完成
- [x] 阶段二：论坛模块（发帖/评论/点赞，板块 seed 数据），按评审建议「书籍讨论区提前至阶段二」——已完成后端 + 前端
- [x] 阶段三：交易与信誉（出售/赠送订单流程、模拟支付、防超卖 CAS、信誉评分规则、地址管理）——已完成后端 + 前端
- [x] 阶段四：社交（好友管理、拉黑、书籍讨论区、消息通知）——已完成后端 + 前端

## 决策记录
- 2026-08-17：持久层用 MyBatis XML Mapper + PageHelper，不用 MyBatis-Plus（⚠️ plan.md 技术选型仍写 MyBatis-Plus，待同步修订）
- 2026-08-17：RBAC 统一账号（用户/管理员同走 sys_user + role，无独立 admin 表）
- 2026-08-17：优先逻辑删除；接口前缀 /api/v1
- 2026-08-17：前端 mock 先行——API 函数默认返回 mock（`VITE_API_MOCK=false` 切真实）；`Book` 保持展示用 VO 形态，后端对接返回含 categoryName/sellerName 的 VO
- 2026-08-17：发布书籍图片暂用自动 CSS 书封（按分类取色板），不接上传；交易/收藏/订单流程属阶段三
- 2026-08-17：数据库无物理外键、用逻辑索引；枚举存 TINYINT 码 + 注释，展示中文由后端 VO 映射；通用字段 id/create_time/update_time/deleted（纯追加表只留 id+create_time）
- 2026-08-17：评审建议（待对应阶段执行）——书籍讨论区提前至阶段二；阶段四拆序（先论坛发帖/评论/点赞，后好友/通知）；阶段四补 seed 论坛板块数据；ForumPost 契约拆分 + 回复数聚合
- 2026-08-17：后端 Spring Boot 3.5.16（plan.md 3.3.x 更新到 3.5.x 稳定线）；PageHelper 用独立 pagehelper 6.1.1 + MyBatis starter 3.x 自动注册 Interceptor bean（不引 pagehelper-spring-boot-starter，其面向 Boot 2）；Backend/.mvn/settings.xml 走阿里云镜像（本机直连 Maven Central 被墙，不改本机 ~/.m2）
- 2026-08-17：认证 JWT 无状态（jjwt 0.12.6，HS256/384）；关闭 Security 表单/httpBasic/内存用户（排除 UserDetailsServiceAutoConfiguration），纯 JWT filter 写 SecurityContext；JWT 密钥走环境变量 JWT_SECRET（默认 dev 值）；注册账号复用为 sys_user.username；注册默认 user 角色；登录账号=手机号或邮箱（对齐前端 LoginResult 契约）；seed 管理员 admin（默认密码 admin123456，首次登录后修改）；全局异常补 400 请求体/404/405 错误码
- 2026-08-17：Git Bash 会话不继承 Windows 用户环境变量 MYSQL_PASS，启动后端需内联注入（`MYSQL_PASS="$(powershell -NoProfile -Command '[Environment]::GetEnvironmentVariable("MYSQL_PASS","User")')" mvn spring-boot:run`）
- 2026-08-17：MyBatis 映射的 VO 类**禁用 Lombok @Builder**（其全参构造器触发列顺序构造器自动映射，create_time 被按位置塞给 Long 参数报错；显式 resultMap/javaType 均无效，去 @Builder 走 setter 映射解决）；VO 枚举中文在 SQL CASE 映射；Book 接口过滤参数用编码（tradeType 1-3 / condition 1-4 / categoryId），发布需登录（JWT userId），GET 列表/详情游客可浏览（Security 放行 GET /books/**）
- 2026-08-18：论坛模块设计——回复数**不做冗余列**，列表/详情用相关子查询聚合 `forum_comment`（PageHelper count 无 GROUP BY 不冲突）；`like_count` 冗余列与 `forum_like` 在 `@Transactional` 内同步（先写 forum_like 唯一索引，重复点赞 catch DuplicateKeyException 幂等返回，取消用 `GREATEST(like_count-1,0)` 防负）；hot 排序按 `like_count + 回复数` 倒序（MySQL 支持 ORDER BY 引用 select 别名）；点赞/编辑/删除需登录，帖子所有权校验本人否则 403，`GET /forum/**` 游客放行；发帖人昵称缺省回退 username（`COALESCE(up.nickname, su.username)`）；板块 seed 对齐前端 mock（读书心得/书评/闲置交流/求助问答）
- 2026-08-18：交易与信誉设计——订单号 `BC+yyyyMMddHHmmssSSS+6位随机`（≤32 字符）；**防超卖**用书籍状态 CAS `UPDATE book SET status=2 WHERE id=? AND status=1`（0 行即已被他人预订），取消 2→1 释放回在售、确认收货 2→3 已售出；出售下单待支付、赠送下单金额 0 直接待发货；取消仅限待支付/待发货（已支付自动置 payment 已退款 + 释放书籍，买家待发货取消触发 MALICIOUS_CANCEL -3、卖家取消不扣）；评价需订单已完成、每人每单一次（uk_order_evaluator），好评 ≥4 分触发 GOOD_REVIEW +1；**信誉分钳制踩坑**：`LEAST(GREATEST(credit+delta,0),100)` 方向写反导致加成永远封顶 100，改为仅 `GREATEST(credit+delta,0)` 钳下限（奖励可高于初始 100，schema 注释 0-100 为参考）；下单信誉阈值：<40 冻结、<60 且出售禁买。**⚠️ Windows mysql 客户端 `mysql < seed.sql` 不带 `--default-character-set=utf8mb4` 会把中文按 GBK 双编码入库**（credit_rule 名称曾因此乱码），应用 seed 必须带该参数
- 2026-08-19：社交设计——好友关系**双向各一行**（uk(user_id,friend_id) 允许 A,B 与 B,A 并存），建立用 `INSERT ... ON DUPLICATE KEY UPDATE status=1`（逻辑删除的旧行可恢复），删除双向 `UPDATE status=0`；好友申请仅新增（历史留痕），重复待处理申请幂等拦截，已同意/已是好友/双向拉黑均拦截；黑名单**双向阻断**（任一方向拉黑即拦截好友申请与下单，countBlackBetween 在 FriendService.sendRequest + TradeService.createOrder 校验），拉黑自动解除双方好友；通知 type 1系统/2交易/3好友，交易全流程触发（下单/支付/发货/收货/取消/评价），NotificationService 供各模块调用；书籍讨论顶级 + 子回复**分两次查询 Service 端按 parentId 组装**（避免 XML collection 嵌套映射）；搜索接口手写 LIMIT 20（业务上限非分页）。**⚠️ 冒烟断言坑：BusinessException 走 HTTP 200 + Result.code（GlobalExceptionHandler 不设 HTTP 状态码），断言用 body.code 而非 HTTP 状态；逻辑删除 user_profile 后 uk_nickname 唯一索引仍被旧行占用导致同名昵称重注册事务回滚，冒烟昵称需唯一后缀或清理用物理删除**
- 2026-08-19：管理后台设计——**admin 路由用 SecurityConfig `.hasRole("admin")`**（JWT filter 已注入 `ROLE_<role>` 权限，未登录 401、普通用户 403 均走现有 handler）；操作日志**不做 AOP**（pom 无 aspectj，遵循现有 Service 风格），用 OperationLogService.record() 在各 Admin Service `@Transactional` 内与业务同事务调用；用户禁用/信誉调整**禁止作用于 admin 账号**（防锁死），禁调自己信誉分；分类逻辑删除前校验子分类/书籍占用；书籍下架/上架用已有 `updateStatusIfEquals` CAS（1→4 / 4→1），交易中（2/3）不可操作；BookVO 增 statusName（CASE 中文）供管理列表展示
- 2026-08-19：阶段五剩余第一批设计——交易订单管理**只读**（keyword 匹配订单号/书名/买卖方用户名，复用 `orderVoColumns` SQL fragment + 额外条件，读操作不记操作日志）；信誉规则数据驱动开关（`PUT /credit-rules/{id}/status`，禁用后 CreditService.apply 自动忽略，无需改业务代码）；统计聚合 SQL 走**独立 StatsMapper 注解**（简单聚合不进 XML），trend 用 MySQL `DATE_FORMAT` 按日聚合 + Java 侧补零对齐（避免日期脊柱 SQL）；前端 echarts 用 **core 按需引入**（LineChart + Grid/Tooltip/Legend + CanvasRenderer），AdminStats chunk 1.1MB→513KB，路由懒加载

## 阻塞/风险
- Redis 未安装，缓存/验证码/Token 黑名单需先安装（降级方案见 plan.md 风险表）
- 支付合规：初期模拟支付，真实支付需资质
