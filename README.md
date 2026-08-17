# BookCycle（书循环）

二手书交易 + 社区论坛 Web 应用。让每一本书，都继续它的旅程。

## 技术栈

- **后端**：Java 21 + Spring Boot 3.5.16 + Spring Security（JWT）+ MyBatis XML Mapper + PageHelper + MySQL 8 + springdoc
- **前端**：Vue 3 + TypeScript + Vite + Element Plus + Pinia + Vue Router
- 详见 [plan.md](plan.md)（7 阶段需求）与 [PROGRESS.md](PROGRESS.md)（进度与决策记录）

## 当前可运行功能

> 当前为**阶段一**：认证 + 最小闭环（注册 → 发布书籍 → 详情 → 浏览）。

| 模块 | 功能 | 接口 / 入口 |
|---|---|---|
| 认证 | 注册 / 登录 / 登出（JWT，账号=手机号或邮箱） | `POST /api/v1/auth/register\|login\|logout` |
| 分类 | 根分类分页列表（游客可看） | `GET /api/v1/categories` |
| 书籍 | 发布（需登录）/ 分页列表 / 详情（游客可看），支持关键词、分类、交易方式、新旧程度筛选与排序 | `POST /api/v1/books`、`GET /api/v1/books`、`GET /api/v1/books/{id}` |
| 前端页面 | 首页、书市列表、书籍详情、登录/注册、发布书籍、个人中心、论坛/关于占位页、404 | 前端路由（见下） |

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
- 进度、决策与踩坑记录见 [PROGRESS.md](PROGRESS.md)；变更历史见 [CHANGELOG.md](CHANGELOG.md)。
