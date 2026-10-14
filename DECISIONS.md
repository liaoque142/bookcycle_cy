# 设计与踩坑决策库

> ⚠️ 全文约 16KB。**禁止整文件读取**：先在本索引里 Grep 主题关键词（如 CAS / 头像 / 退款 / 魔数 / 超卖 / 拉黑 / 映射），再按命中行号用 Read offset 局部读取。

## 索引
- 2026-08-20 优化完善设计
- 2026-08-17 持久层用 MyBatis XML Mapper + PageHelper
- 2026-08-17 RBAC 统一账号
- 2026-08-17 优先逻辑删除
- 2026-08-17 前端 mock 先行
- 2026-08-17 发布书籍图片暂用自动 CSS 书封
- 2026-08-17 数据库无物理外键、用逻辑索引
- 2026-08-17 评审建议
- 2026-08-17 后端 Spring Boot 3.5.16
- 2026-08-17 认证 JWT 无状态
- 2026-08-17 Git Bash 会话不继承 Windows 用户环境变量 MYSQL_PASS
- 2026-08-17 MyBatis 映射的 VO 类**禁用 Lombok @Builder**
- 2026-08-18 论坛模块设计
- 2026-08-18 交易与信誉设计
- 2026-08-19 社交设计
- 2026-08-19 管理后台设计
- 2026-08-19 阶段五剩余第一批设计
- 2026-08-19 退款售后设计
- 2026-08-19 阶段六 swap 设计
- 2026-08-20 好友私信设计
- 2026-08-20 第二轮优化完善设计
- 2026-08-20 第三轮优化完善设计
- 2026-08-20 第四轮优化完善设计

---

## 2026-08-20 优化完善设计
优化完善设计——① 退出登录：无状态 JWT 服务端 logout 空实现，本地登出用 try/finally 保证接口异常也清 token，退出统一回主页（不限页面）；② 解除拉黑恢复好友：**不在黑名单表塞会话状态**，加 `was_friend` 一列记拉黑时是否为好友，解除时 `countBlackBetween==0`（对方未反向拉黑）才 `upsertFriend` 双向恢复，天然覆盖「非好友误加/互相拉黑」边界；③ 图片上传**不引 MinIO/OSS**（本机无），用本地磁盘 `uploads/` + `WebMvcConfigurer` 静态映射 `/uploads/**`（Security 放行供 <img> 免登录加载），`app.upload-dir` 可配，扩展名白名单 + 大小 5MB + content-type 校验，UUID 文件名防碰撞；④ 评论/私信图片**不加 content_type 列**，直接 `image_url` 可空列与文字并存（纯图/图文/纯文），服务端 `ContentValidator` 校验「图/文至少一项 + imageUrl 必须 /uploads/ 前缀」防注入任意 URL；前端 mock 模式上传用 FileReader 读 data URL（无后端也可预览）。**⚠️ 数据库加列 MySQL 8 不支持 ADD COLUMN IF NOT EXISTS**，旧库用 `sql/upgrade_2026-08-20.sql` 一次性 ALTER（勿重复执行），schema.sql 同步保证新装一致。

## 2026-08-17 持久层用 MyBatis XML Mapper + PageHelper
持久层用 MyBatis XML Mapper + PageHelper，不用 MyBatis-Plus（⚠️ plan.md 技术选型仍写 MyBatis-Plus，待同步修订）

## 2026-08-17 RBAC 统一账号
RBAC 统一账号（用户/管理员同走 sys_user + role，无独立 admin 表）

## 2026-08-17 优先逻辑删除
优先逻辑删除；接口前缀 /api/v1

## 2026-08-17 前端 mock 先行
前端 mock 先行——API 函数默认返回 mock（`VITE_API_MOCK=false` 切真实）；`Book` 保持展示用 VO 形态，后端对接返回含 categoryName/sellerName 的 VO

## 2026-08-17 发布书籍图片暂用自动 CSS 书封
发布书籍图片暂用自动 CSS 书封（按分类取色板），不接上传；交易/收藏/订单流程属阶段三

## 2026-08-17 数据库无物理外键、用逻辑索引
数据库无物理外键、用逻辑索引；枚举存 TINYINT 码 + 注释，展示中文由后端 VO 映射；通用字段 id/create_time/update_time/deleted（纯追加表只留 id+create_time）

## 2026-08-17 评审建议（待对应阶段执行）
评审建议（待对应阶段执行）——书籍讨论区提前至阶段二；阶段四拆序（先论坛发帖/评论/点赞，后好友/通知）；阶段四补 seed 论坛板块数据；ForumPost 契约拆分 + 回复数聚合

## 2026-08-17 后端 Spring Boot 3.5.16
后端 Spring Boot 3.5.16（plan.md 3.3.x 更新到 3.5.x 稳定线）；PageHelper 用独立 pagehelper 6.1.1 + MyBatis starter 3.x 自动注册 Interceptor bean（不引 pagehelper-spring-boot-starter，其面向 Boot 2）；Backend/.mvn/settings.xml 走阿里云镜像（本机直连 Maven Central 被墙，不改本机 ~/.m2）

## 2026-08-17 认证 JWT 无状态
认证 JWT 无状态（jjwt 0.12.6，HS256/384）；关闭 Security 表单/httpBasic/内存用户（排除 UserDetailsServiceAutoConfiguration），纯 JWT filter 写 SecurityContext；JWT 密钥走环境变量 JWT_SECRET（默认 dev 值）；注册账号复用为 sys_user.username；注册默认 user 角色；登录账号=手机号或邮箱（对齐前端 LoginResult 契约）；seed 管理员 admin（默认密码 admin123456，首次登录后修改）；全局异常补 400 请求体/404/405 错误码

## 2026-08-17 Git Bash 会话不继承 Windows 用户环境变量 MYSQL_PASS
Git Bash 会话不继承 Windows 用户环境变量 MYSQL_PASS，启动后端需内联注入（`MYSQL_PASS="$(powershell -NoProfile -Command '[Environment]::GetEnvironmentVariable("MYSQL_PASS","User")')" mvn spring-boot:run`）

## 2026-08-17 MyBatis 映射的 VO 类**禁用 Lombok @Builder**
MyBatis 映射的 VO 类**禁用 Lombok @Builder**（其全参构造器触发列顺序构造器自动映射，create_time 被按位置塞给 Long 参数报错；显式 resultMap/javaType 均无效，去 @Builder 走 setter 映射解决）；VO 枚举中文在 SQL CASE 映射；Book 接口过滤参数用编码（tradeType 1-3 / condition 1-4 / categoryId），发布需登录（JWT userId），GET 列表/详情游客可浏览（Security 放行 GET /books/**）

## 2026-08-18 论坛模块设计
论坛模块设计——回复数**不做冗余列**，列表/详情用相关子查询聚合 `forum_comment`（PageHelper count 无 GROUP BY 不冲突）；`like_count` 冗余列与 `forum_like` 在 `@Transactional` 内同步（先写 forum_like 唯一索引，重复点赞 catch DuplicateKeyException 幂等返回，取消用 `GREATEST(like_count-1,0)` 防负）；hot 排序按 `like_count + 回复数` 倒序（MySQL 支持 ORDER BY 引用 select 别名）；点赞/编辑/删除需登录，帖子所有权校验本人否则 403，`GET /forum/**` 游客放行；发帖人昵称缺省回退 username（`COALESCE(up.nickname, su.username)`）；板块 seed 对齐前端 mock（读书心得/书评/闲置交流/求助问答）

## 2026-08-18 交易与信誉设计
交易与信誉设计——订单号 `BC+yyyyMMddHHmmssSSS+6位随机`（≤32 字符）；**防超卖**用书籍状态 CAS `UPDATE book SET status=2 WHERE id=? AND status=1`（0 行即已被他人预订），取消 2→1 释放回在售、确认收货 2→3 已售出；出售下单待支付、赠送下单金额 0 直接待发货；取消仅限待支付/待发货（已支付自动置 payment 已退款 + 释放书籍，买家待发货取消触发 MALICIOUS_CANCEL -3、卖家取消不扣）；评价需订单已完成、每人每单一次（uk_order_evaluator），好评 ≥4 分触发 GOOD_REVIEW +1；**信誉分钳制踩坑**：`LEAST(GREATEST(credit+delta,0),100)` 方向写反导致加成永远封顶 100，改为仅 `GREATEST(credit+delta,0)` 钳下限（奖励可高于初始 100，schema 注释 0-100 为参考）；下单信誉阈值：<40 冻结、<60 且出售禁买。**⚠️ Windows mysql 客户端 `mysql < seed.sql` 不带 `--default-character-set=utf8mb4` 会把中文按 GBK 双编码入库**（credit_rule 名称曾因此乱码），应用 seed 必须带该参数

## 2026-08-19 社交设计
社交设计——好友关系**双向各一行**（uk(user_id,friend_id) 允许 A,B 与 B,A 并存），建立用 `INSERT ... ON DUPLICATE KEY UPDATE status=1`（逻辑删除的旧行可恢复），删除双向 `UPDATE status=0`；好友申请仅新增（历史留痕），重复待处理申请幂等拦截，已同意/已是好友/双向拉黑均拦截；黑名单**双向阻断**（任一方向拉黑即拦截好友申请与下单，countBlackBetween 在 FriendService.sendRequest + TradeService.createOrder 校验），拉黑自动解除双方好友；通知 type 1系统/2交易/3好友，交易全流程触发（下单/支付/发货/收货/取消/评价），NotificationService 供各模块调用；书籍讨论顶级 + 子回复**分两次查询 Service 端按 parentId 组装**（避免 XML collection 嵌套映射）；搜索接口手写 LIMIT 20（业务上限非分页）。**⚠️ 冒烟断言坑：BusinessException 走 HTTP 200 + Result.code（GlobalExceptionHandler 不设 HTTP 状态码），断言用 body.code 而非 HTTP 状态；逻辑删除 user_profile 后 uk_nickname 唯一索引仍被旧行占用导致同名昵称重注册事务回滚，冒烟昵称需唯一后缀或清理用物理删除**

## 2026-08-19 管理后台设计
管理后台设计——**admin 路由用 SecurityConfig `.hasRole("admin")`**（JWT filter 已注入 `ROLE_<role>` 权限，未登录 401、普通用户 403 均走现有 handler）；操作日志**不做 AOP**（pom 无 aspectj，遵循现有 Service 风格），用 OperationLogService.record() 在各 Admin Service `@Transactional` 内与业务同事务调用；用户禁用/信誉调整**禁止作用于 admin 账号**（防锁死），禁调自己信誉分；分类逻辑删除前校验子分类/书籍占用；书籍下架/上架用已有 `updateStatusIfEquals` CAS（1→4 / 4→1），交易中（2/3）不可操作；BookVO 增 statusName（CASE 中文）供管理列表展示

## 2026-08-19 阶段五剩余第一批设计
阶段五剩余第一批设计——交易订单管理**只读**（keyword 匹配订单号/书名/买卖方用户名，复用 `orderVoColumns` SQL fragment + 额外条件，读操作不记操作日志）；信誉规则数据驱动开关（`PUT /credit-rules/{id}/status`，禁用后 CreditService.apply 自动忽略，无需改业务代码）；统计聚合 SQL 走**独立 StatsMapper 注解**（简单聚合不进 XML），trend 用 MySQL `DATE_FORMAT` 按日聚合 + Java 侧补零对齐（避免日期脊柱 SQL）；前端 echarts 用 **core 按需引入**（LineChart + Grid/Tooltip/Legend + CanvasRenderer），AdminStats chunk 1.1MB→513KB，路由懒加载

## 2026-08-19 退款售后设计
退款售后设计——申请**不改订单状态**，仅置 `trade_order.after_sale=1`（订单保持原 2/3/4，ship/confirm/cancel 加 after_sale 守卫拦截），拒绝时直接回退 `after_sale=0` 买家可重试（无需存储原状态）；同意时订单取消（复用 cancel() 写 cancel_reason "售后退款：xxx"）+ 书籍 2/3→1 释放回在售（updateStatusIfEquals 两次兜底）+ 复用 refundPayment() 原路退款 + after_sale=3 + 售后单 CAS 置终态（refund.status 1→4/3，handler_id 记管理员，WHERE status=1 防并发重复处理）；售后类型按订单状态校验（待发货仅仅退款、待收货/已完成仅退货退款），金额必须 >0（赠送 0 元单不可退）；refund 表**无待处理唯一索引**，幂等靠 selectPendingByOrderId 查 status=1 + after_sale 守卫双保险；RefundVO 联表订单/书/申请人(昵称回退用户名)/处理人，refund.id 处理用 `PUT /admin/refunds/{id}/handle`，列表 keyword 匹配订单号/书名/申请人用户名

## 2026-08-19 阶段六 swap 设计
阶段六 swap 设计——**独立 `swap` 表不塞进 trade_order**（交换是双书 + 协商 + 双向发货/确认，与单书线性订单状态机本质不同）；状态机 1 待对方同意 / 2 已同意待发货 / 3 交换中 / 4 已完成 / 5 已取消 / 6 已拒绝，**发货/确认不做冗余列**，用 initiator/receiver 各自 shipped_at/confirmed_at 时间戳记录，`markBothShipped`/`markBothConfirmed` 用「状态 + 双方时间戳」条件 CAS 原子推进（幂等，仅当双方都满足时生效，天然防并发且 credit 只在 2→4 那刻发一次）；取消仅在 1/2 且本人未发货（已发货的书保持已售出，releaseBoth 用 updateStatusIfEquals 2→1 只释放未发货书）；同意时接收方填收书地址（accept CAS status=1→2 + 记 receiver_ship_type/address）；发起时双书 CAS 在售→已预订，任一步失败事务回滚；book 表复用 trade_type=3 交换（发起人可用**任意**在售书 B 换，不限 tradeType）；互评/管理后台展示**本批不做**（仅 TRADE_SUCCESS +2）；`GET /books/mine` 需登录，SecurityConfig 须在 `GET /books/**` permitAll **之前**匹配（字面量 /mine 优先于 /{id}）

## 2026-08-20 好友私信设计
好友私信设计——**不建会话表**，新增纯追加 `message` 表（sender/receiver/content/is_read/create_time），按好友未读从消息行 GROUP BY 聚合；**不引入 WebSocket/Redis**，沿用通知模块轮询模式（Header 私信红点 30s + 好友行角标，会话打开期间 5s 轮询拉对方新回复并自动已读）；**隐私靠查询边界**：历史/未读 SQL 一律以当前用户为参与者边界（`WHERE (sender=me AND receiver=peer) OR (sender=peer AND receiver=me)`），第三者访问他人会话自然返回空列表，无需额外越权判断；发送仅限好友（黑名单已自动解除好友关系，无需额外拉黑校验）；已读策略=打开会话即标记该好友发来全部已读（`UPDATE ... WHERE receiver=me AND sender=peer AND is_read=0`）

## 2026-08-20 第二轮优化完善设计
第二轮优化完善设计——**消息中心单条已读用 `PUT /messages/{id}`（1 段路径）**：若用 `/{id}/read` 与现有 `PUT /{userId}/read` 路径模板相同，Spring 启动即 Ambiguous mapping 报错，必须避让；`markSingleRead` 幂等**不抛错**（`AND is_read=0` 0 行即已读/非本人，MySQL 默认改同值影响行数 0，抛错会让已读消息重点误报），越权天然被 `WHERE receiver_id=me` 拦截；收件箱 `selectInboxPage` 普通 JOIN 无 LIMIT 子查询（PageHelper count 安全，user_profile 经 uk_user_id 1:1 不放大），聊天历史 `selectHistory` 是 LIMIT 子查询**不可包 PageHelper**。**头像复用 `user_profile.avatar` 列（早已存在）+ 现有上传接口**，不引对象存储；全站 8 个 VO 联表补 up.avatar（昵称沿用 COALESCE 回退），NotificationVO **不加**（notification 表无发送方概念无从联表）；前端通用 `Avatar.vue` 有图渲染/无图回退首字，黑名单 muted 变灰保持视觉。**资料接口**：昵称唯一先 `selectByNickname` 排除自己再捕 `DuplicateKeyException` 兜底（空串/并发不落 uk_nickname）；genre_ids 空列表置 NULL 而非空串、parse 对 NULL 返回空列表。**顺手修存量 bug**：`message.content` 改可空（纯图私信 content=null 原被 NOT NULL 拒绝，schema + 升级脚本同步）。

## 2026-08-20 第三轮优化完善设计
第三轮优化完善设计——**头像 bug 双修**：① `Avatar.vue` `broken` 状态只在 `<img>` `@error` 置位、src 变化从不复位 → 一次加载失败后该组件实例对任何新头像永久回退首字（Profile/HomeHeader 的 Avatar 是长生命周期组件不重建），加 `watch(() => props.src, ...)` 复位；② vite dev 代理只转发 `/api/v1`、未转发 `/uploads/**` → 上传头像 `/uploads/...` 在 5173 直接 404 触发 @error（这就是「保存成功但头像没换」的诱因），补 `/uploads` 代理（实测 5173→8080→200）。**好友详情接口**：`GET /friends/{userId}`（与现有 `DELETE /{userId}` 同路径模板不同方法不冲突），业务校验放 Service `friendDetail`（`countFriend>0` 否则 400「对方不是你的好友」），复用 `UserService.publicProfile`（=me 同源 toUserVO）组装完整资料——好友边界天然满足隐私；前端 `FriendDetail extends Friend` 复用，弹窗复刻 Messages.vue 卡片，`FriendDetail` 结构兼容 `openChat` 直接传参开抽屉；书籍喜好 tags 复用 `fetchCategories`（Profile.vue 同款 `genreNames` computed 写法）。

## 2026-08-20 第四轮优化完善设计
第四轮优化完善设计——**私信图片花屏/乱码根因**：`UploadService` 只按客户端文件名扩展名落盘（`getOriginalFilename()` 取后缀存 uuid.png/jpg）、从不校验真实字节，扩展名与字节不符时浏览器按错误格式解码即花屏；ContentType 只宽松判断 image/* 完全信客户端。修复=**魔数嗅探**（读头 16 字节：JPEG `FF D8 FF`→.jpg、PNG 签名+首块 IHDR→.png，不匹配 400「仅支持 jpg / png 格式」）按真实字节定扩展名落盘，删 ContentType 判断与 gif/webp 白名单。**上传限额按 scene 区分**：单接口加 `scene` 参数（message 10MB 默认 / avatar 5MB，SCENES 白名单校验）；`spring.servlet.multipart` 上限必须先提到 12MB/20MB（Spring 在进 Controller 前套上限，5MB 会先拒 10MB 图），超 Spring 上限的 `MaxUploadSizeExceededException` 在 GlobalExceptionHandler 落 400「图片大小不能超过 10MB」而非 500。前端 jpg/png 用「type ∈ jpeg/png 或扩展名 .jpg/.jpeg/.png」双判断（`file.type` 可能为空/非标准 image/jpg），accept 限定只过滤原生选择器需 JS 兜底。**mock 附带 bug**：mock `fetchHistory` 返回空列表 → 聊天抽屉 5s 轮询把本地刚发消息清空，`startPoll` 加 `if (USE_MOCK) return`。`.env*` 入 Frontend/.gitignore，本地新建 `.env.development`（`VITE_API_MOCK=false`）不入库用于真实后端验证（魔数/限额 mock 测不到）。

