# BookCycle（书循环）

二手书交易 + 社区论坛 Web 应用。让每一本书，都继续它的旅程。

## 技术栈

- **后端**：Java 21 + Spring Boot 3.5.16 + Spring Security（JWT）+ MyBatis XML Mapper + PageHelper + MySQL 8 + springdoc
- **前端**：Vue 3 + TypeScript + Vite + Element Plus + Pinia + Vue Router
- 详见 [plan.md](plan.md)（7 阶段需求）与 [PROGRESS.md](PROGRESS.md)（进度与决策记录）

## 当前可运行功能

> 当前为**阶段一 ~ 阶段五**：认证 + 最小闭环（注册 → 发布书籍 → 详情 → 浏览）+ 论坛（发帖/评论/点赞）+ 交易与信誉（下单/模拟支付/地址/互评/信誉分）+ 社交（好友/拉黑/书籍讨论/通知）+ 管理后台（后端 + 前端：用户/分类/书籍管理 + 操作日志）。

| 模块 | 功能 | 接口 / 入口 |
|---|---|---|
| 认证 | 注册 / 登录 / 登出（JWT，账号=手机号或邮箱） | `POST /api/v1/auth/register\|login\|logout` |
| 分类 | 根分类分页列表（游客可看） | `GET /api/v1/categories` |
| 书籍 | 发布（需登录）/ 分页列表 / 详情（游客可看），支持关键词、分类、交易方式、新旧程度筛选与排序 | `POST /api/v1/books`、`GET /api/v1/books`、`GET /api/v1/books/{id}` |
| 论坛 | 板块列表、帖子分页（板块/关键词过滤 + 最新/热门排序）、帖子详情、发帖/编辑/删除（仅本人）、评论、点赞/取消点赞 | `GET /api/v1/forum/boards`、`/posts`、`/posts/{id}`、`POST|PUT|DELETE /posts/{id}`、`GET /posts/{id}/comments`、`POST /comments`、`POST\|DELETE /posts/{id}/like` |
| 地址 | 收货地址增删改查 + 设默认（需登录，首条自动默认） | `GET\|POST /api/v1/addresses`、`PUT\|DELETE /{id}`、`PUT /{id}/default` |
| 交易 | 下单（出售/免费赠送，邮寄/自提，CAS 防超卖）、模拟支付、卖家发货、买家确认收货、取消（已支付自动退款） | `POST /api/v1/orders`、`POST /orders/{orderNo}/pay\|ship\|confirm\|cancel`、`GET /orders`、`GET /orders/{orderNo}` |
| 交易评价 | 订单完成后买卖双方互评（1-5 分，好评 ≥4 分给被评方信誉 +1） | `POST /orders/{orderNo}/evaluate`、`GET /orders/{orderNo}/evaluations` |
| 信誉 | 数据驱动规则（成功交易 +2、获得好评 +1、恶意取消 -3 等，seed 7 条可配置）、变动日志 | `GET /api/v1/credit/me`、`GET /api/v1/credit/logs` |
| 社交 | 用户搜索、好友申请/同意/拒绝/删除、拉黑（双向阻断 + 自动解除好友）、书籍讨论区（树形）、通知（交易/好友触发，铃铛未读） | `GET /api/v1/friends/search`、`GET|POST /friends`、`POST /friends/requests`、`PUT /friends/requests/{id}`、`DELETE /friends/{userId}`、`GET\|POST /api/v1/blacklist`、`DELETE /blacklist/{userId}`、`GET|POST /api/v1/books/{id}/discussions`、`DELETE /discussions/{discId}`、`GET /api/v1/notifications`、`GET /unread-count`、`PUT /{id}/read`、`PUT /read-all` |
| 管理后台（仅 admin，未登录 401 / 非管理员 403） | 用户管理：分页筛选/详情/禁用启用/调整信誉分（credit_log 记操作人）；分类管理：增删改查（同级同名去重、删前校验子分类/书籍占用）；书籍管理：全状态列表/下架/上架（交易中不可操作）；操作日志查询 | `/api/v1/admin/users`、`/api/v1/admin/categories`、`/api/v1/admin/books`、`/api/v1/admin/operation-logs` |
| 前端页面 | 首页、书市列表、书籍详情、登录/注册、发布书籍、个人中心、论坛、帖子详情、收货地址、我的订单、信誉记录、好友、通知、关于页、404、管理后台（/admin：用户/分类/书籍/操作日志） | 前端路由（见下） |

- 前端当前**默认 mock 数据**（`Frontend/src/data/`），切真实后端：`VITE_API_MOCK=false`。
- 后端 API 文档（Swagger）：`http://localhost:8080/swagger-ui.html`

## 启动方式

前置：MySQL 8.4 已运行，`bookcycle` 库已建表并 seed（`sql/schema.sql`、`sql/seed.sql`）；本机已设置用户环境变量 `MYSQL_PASS`（数据库密码）。

```bash
# 后端（8080）
cd Backend
# Git Bash 不继承 Windows 用户环境变量，需内联注入；cmd/PowerShell/IDE 直接 mvn spring-boot:run
MYSQL_PASS="$(powershell -NoProfile -Command '[Environment]::GetEnvironmentVariable("MYSQL_PASS","User")')" mvn spring-boot:run

# 前端（5173）
cd Frontend
npm install
npm run dev                      # mock 模式（默认）
VITE_API_MOCK=false npm run dev  # 走真实后端
```

> 前端 dev 已配置 `/api/v1` 代理到后端 8080（vite.config.ts），真实模式无需后端 CORS。
> 注意：Windows cmd 不支持 `VAR=value 命令` 写法，用 `set VITE_API_MOCK=false&& npm run dev` 或改用 Git Bash。

前端访问：`http://localhost:5173`；后端：`http://localhost:8080/api/v1`。

## 测试方式

### 后端单元测试
```bash
cd Backend && mvn test
```

### 接口冒烟（curl）

```bash
B=http://localhost:8080/api/v1

# 1) 注册（账号=手机号或邮箱）
curl -s -X POST $B/auth/register -H "Content-Type: application/json" \
  -d '{"account":"13800001234","password":"test123456","nickname":"tester"}'

# 2) 登录拿 token
curl -s -X POST $B/auth/login -H "Content-Type: application/json" \
  -d '{"account":"13800001234","password":"test123456"}'
TOKEN=$(curl -s -X POST $B/auth/login -H "Content-Type: application/json" \
  -d '{"account":"13800001234","password":"test123456"}' | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

# 3) 发布书籍（需登录；tradeType 1出售/2赠送/3交换，condition 1-4，categoryId 见 seed 分类）
curl -s -X POST $B/books -H "Content-Type: application/json" -H "Authorization: Bearer $TOKEN" \
  -d '{"title":"三体","author":"刘慈欣","categoryId":1,"tradeType":1,"condition":2,"price":25.5,"publisher":"重庆出版社","description":"第一部"}'

# 4) 列表 / 筛选 / 排序 / 详情（游客可看）
curl -s "$B/books?page=1&size=10&sort=latest"
curl -s "$B/books?keyword=三体&tradeType=1"
curl -s "$B/books/1"

# 5) 鉴权边界：无 token / 伪造 token → 401 JSON
curl -s -X POST $B/books -H "Content-Type: application/json" -d '{}'
```

> 注意：Windows 的 curl 会把中文字符按 GBK 发送导致 JSON 解析失败（返回 400「请求体格式不正确」），属客户端编码问题；浏览器/axios 发 UTF-8 无此问题。冒烟测试建议先用英文 payload 验证，中文链路以浏览器为准。

### 测试账号
- 管理员：`admin@bookcycle.dev` / `admin123456`（seed 内置，首次登录后请修改）
- 普通用户：注册任意手机号/邮箱即可

## 当前技术要点 / 已知约定

- **MyBatis 映射的 VO 类禁用 Lombok `@Builder`**：全参构造器会触发「列顺序构造器自动映射」，把时间列按位置塞给 Long 参数报 `Unsupported conversion from DATETIME to java.lang.Long`。改用 `@Data` + setter 映射。
- **PageHelper**：用独立 `pagehelper 6.1.1`，靠 MyBatis starter 3.x 自动注册 `PageInterceptor` bean（重复注册会 count 嵌套 NPE）。
- **枚举存编码**：`trade_type` 1出售/2赠送/3交换，`condition` 1全新/2九成新/3八成新/4七成新及以下；展示中文由后端 SQL/服务层映射。
- **RBAC 统一账号**：用户与管理员同 `sys_user` + `role`，无独立 admin 表；JWT 携带角色编码。
- **DB 密码不入库**：走本机环境变量 `MYSQL_PASS`；JWT 密钥走 `JWT_SECRET`（默认 dev 值）。
- **前端真实模式契约映射**：发布/筛选入参前端中文（分类名、交易方式、新旧程度）→ 后端编码（`categoryId`/`tradeType` 1-3/`condition` 1-4）；分类字典走 `GET /categories`；后端 BookVO 无书封色板，前端按分类 fallback；401 自动清登录态并跳登录页。
- **防超卖**：下单时书籍状态 `在售→已预订` 走 CAS（`UPDATE ... WHERE status=1`），0 行即「已被预订」；取消释放回在售，确认收货转已售出（预订/售出的书从书市列表消失）。
- **信誉分不设上限**：初始 100，奖励（成功交易 +2、好评 +1）可高于 100，仅钳下限 0（schema 注释 0-100 为参考）；扣减规则在 `credit_rule` 表数据驱动（后台可配置启用）。
- **Windows 应用 seed 注意**：`mysql.exe < sql/seed.sql` 需带 `--default-character-set=utf8mb4`，否则中文字段会被按 GBK 双编码入库。
- 进度、决策与踩坑记录见 [PROGRESS.md](PROGRESS.md)；变更历史见 [CHANGELOG.md](CHANGELOG.md)。
