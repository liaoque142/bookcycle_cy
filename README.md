# BookCycle（书循环）

二手书交易 + 社区论坛 Web 应用。让每一本书，都继续它的旅程。

## 技术栈

- **后端**：Java 21 + Spring Boot 3.5.16 + Spring Security（JWT）+ MyBatis XML Mapper + PageHelper + MySQL 8 + springdoc
- **前端**：Vue 3 + TypeScript + Vite + Element Plus + Pinia + Vue Router
- 详见 [plan.md](plan.md)（7 阶段需求）、[PROGRESS.md](PROGRESS.md)（当前进度）、[DECISIONS.md](DECISIONS.md)（设计与踩坑决策）

- 管理员：`admin@bookcycle.dev` / `admin123456`（seed 内置，首次登录后请修改）
- 普通用户：注册任意手机号/邮箱即可

## 当前可运行功能

> 当前为**阶段一 ~ 阶段六**：认证 + 最小闭环（注册 → 发布书籍 → 详情 → 浏览）+ 论坛（发帖/评论/点赞）+ 交易与信誉（下单/模拟支付/地址/互评/信誉分）+ 社交（好友/好友私信/拉黑/书籍讨论/通知）+ 交换（以书换书：协商/双向发货/双向确认）+ 管理后台（用户/分类/书籍/交易管理 + 退款售后 + 数据统计 + 系统配置 + 操作日志）+ 优化完善（关于页、评论/私信发图、退出登录回主页、解除拉黑恢复好友、支付收款码、喜欢书籍收藏）。

| 模块 | 功能 | 接口 / 入口 |
|---|---|---|
| 认证 | 注册 / 登录 / 登出（JWT，账号=手机号或邮箱） | `POST /api/v1/auth/register\|login\|logout` |
| 分类 | 根分类分页列表（游客可看） | `GET /api/v1/categories` |
| 书籍 | 发布（需登录）/ 分页列表 / 详情（游客可看），支持关键词、分类、交易方式、新旧程度筛选与排序 | `POST /api/v1/books`、`GET /api/v1/books`、`GET /api/v1/books/{id}` |
| 论坛 | 板块列表、帖子分页（板块/关键词过滤 + 最新/热门排序）、帖子详情、发帖/编辑/删除（仅本人）、评论、点赞/取消点赞 | `GET /api/v1/forum/boards`、`/posts`、`/posts/{id}`、`POST|PUT|DELETE /posts/{id}`、`GET /posts/{id}/comments`、`POST /comments`、`POST\|DELETE /posts/{id}/like` |
| 地址 | 收货地址增删改查 + 设默认（需登录，首条自动默认） | `GET\|POST /api/v1/addresses`、`PUT\|DELETE /{id}`、`PUT /{id}/default` |
| 交易 | 下单（出售/免费赠送，邮寄/自提，CAS 防超卖）、模拟支付（支付弹窗展示卖家收款码扫码转账，预留支付宝/微信）、卖家发货、买家确认收货、取消（已支付自动退款） | `POST /api/v1/orders`、`POST /orders/{orderNo}/pay\|ship\|confirm\|cancel`、`GET /orders`、`GET /orders/{orderNo}` |
| 交易评价 | 订单完成后买卖双方互评（1-5 分，好评 ≥4 分给被评方信誉 +1） | `POST /orders/{orderNo}/evaluate`、`GET /orders/{orderNo}/evaluations` |
| 退款售后 | 买家申请售后（待发货仅退款 / 待收货、已完成退货退款，申请期间冻结发货/收货/取消），管理后台同意退款（订单取消 + 书籍释放回在售 + 原路退款）/ 拒绝退款 | `POST /orders/{orderNo}/refund`、`GET /api/v1/admin/refunds`、`PUT /admin/refunds/{id}/handle` |
| 交换（swap） | 以书换书：用我的在售书换卖家的交换书，对方同意后双向发货、双向确认收货（状态机：待同意/已同意/交换中/已完成/已取消/已拒绝），完成后双方信誉 +2 | `POST /api/v1/swaps`、`GET /swaps`、`GET /swaps/{swapNo}`、`PUT /swaps/{swapNo}/accept\|reject`、`POST /swaps/{swapNo}/cancel\|ship\|confirm` |
| 信誉 | 数据驱动规则（成功交易 +2、获得好评 +1、恶意取消 -3 等，seed 7 条可配置）、变动日志 | `GET /api/v1/credit/me`、`GET /api/v1/credit/logs` |
| 社交 | 用户搜索、好友申请/同意/拒绝/删除、拉黑（双向阻断 + 自动解除好友）、好友信息卡片弹窗（含喜欢书籍）、书籍讨论区（树形）、通知（交易/好友触发，铃铛未读） | `GET /api/v1/friends/search`、`GET|POST /friends`、`POST /friends/requests`、`PUT /friends/requests/{id}`、`DELETE /friends/{userId}`、`GET /friends/{userId}`、`GET /friends/{userId}/favorites`、`GET\|POST /api/v1/blacklist`、`DELETE /blacklist/{userId}`、`GET|POST /api/v1/books/{id}/discussions`、`DELETE /discussions/{discId}`、`GET /api/v1/notifications`、`GET /unread-count`、`PUT /{id}/read`、`PUT /read-all` |
| 收藏 | 喜欢书籍：详情页收藏/取消（详情返回 favorited）、我的收藏分页、好友弹窗展示对方收藏（仅好友可见） | `POST\|DELETE /api/v1/favorites/{bookId}`、`GET /favorites/me`、`GET /friends/{userId}/favorites` |
| 好友私信 | 好友列表选好友发私信，仅双方可见可回复（第三者访问为空），未读红点（好友行角标 + Header 轮询），打开会话即已读，可发图片 | `POST /api/v1/messages`、`GET /messages/{userId}`、`PUT /messages/{userId}/read`、`GET /messages/unread-count`、`GET /messages/unread-summary` |
| 图片上传 | 评论 / 私信发图 + 头像 + 收款码：上传返回 `/uploads/**` 静态 URL（需登录，scene 区分限额 message 10MB / avatar·payQr 5MB，仅 jpg/png 魔数嗅探校验，本地磁盘存储） | `POST /api/v1/upload/image?scene=message\|avatar\|payQr` |
| 管理后台（仅 admin，未登录 401 / 非管理员 403） | 用户管理：分页筛选/详情/禁用启用/调整信誉分（credit_log 记操作人）；分类管理：增删改查（同级同名去重、删前校验子分类/书籍占用）；书籍管理：全状态列表/下架/上架（交易中不可操作）；交易管理：全量订单查询/详情/多维筛选；退款售后：售后列表/同意退款/拒绝退款；数据统计：概览指标（用户/书籍/订单/交易额/售后）+ 7 日趋势；系统配置：信誉规则启用/禁用；操作日志查询 | `/api/v1/admin/users`、`/categories`、`/books`、`/orders`、`/refunds`、`/stats/*`、`/credit-rules`、`/operation-logs` |
| 前端页面 | 首页、书市列表、书籍详情、登录/注册、发布书籍、个人中心、论坛、帖子详情、收货地址、我的订单、我的换书、我的收藏、信誉记录、好友、通知、关于页、404、管理后台（/admin：用户/分类/书籍/交易/售后/统计/配置/操作日志） | 前端路由（见下） |

- 