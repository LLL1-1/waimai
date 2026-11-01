# 苍穹外卖（CangQiongWM）

![Java](https://img.shields.io/badge/Java-25-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-brightgreen)
![MyBatis-Plus](https://img.shields.io/badge/MyBatis--Plus-3.5.17-blue)
![MySQL](https://img.shields.io/badge/MySQL-9.x-4479A1)
![Redis](https://img.shields.io/badge/Redis-Memurai%208.2-DC382D)
![License](https://img.shields.io/badge/License-MIT-yellow)

按苍穹外卖的功能与结构实现，技术栈为 **Spring Boot 4.1.1 + JDK 25 + MyBatis-Plus**。

## 目录结构

```text
CangQiongWM/
├── Code/
│   ├── backend/                 # 后端父工程（Maven，artifactId=sky-take-out）
│   │   ├── pom.xml
│   │   ├── sky-common/          # 工具、常量、异常、结果、配置属性
│   │   ├── sky-pojo/            # entity、DTO、VO
│   │   └── sky-server/          # controller、service、mapper、config 等
│   └── frontend/                # 前端（admin/user 页面 + images 图片）
├── sql/
│   ├── sky_take_out.sql         # 建库 + 建表
│   └── seed.sql                 # 分类、菜品、套餐、管理员
└── README.md
```

## 技术栈

- JDK 25
- Spring Boot 4.1.1
- MyBatis-Plus 3.5.17（`mybatis-plus-spring-boot4-starter`）
- MySQL 9.x（`com.mysql:mysql-connector-j`）
- JWT + 拦截器（jjwt 0.12.x）
- springdoc + Swagger UI
- Jackson（不使用 Fastjson）
- Redis / Memurai、Nginx；Nginx 二进制不进仓库

## 当前状态

- 已建好父工程和三个模块的骨架。
- 已实现分类 `category` 的 CRUD：`/admin/category`（新增、修改、删除、详情、列表、分页、启用/禁用）。
- 已实现员工登录 + JWT 拦截器：`/admin/**` 需要请求头 `token`（`/admin/employee/login` 除外）。
- 已实现员工管理 CRUD：`/admin/employee`（分页、新增、修改、详情、启用/禁用）；不做物理删除，用 `status` 禁用。
- 已实现菜品 `dish` 的 CRUD 与口味维护：`/admin/dish`（分页、新增、修改、批量删除、详情、分类列表、起售/停售）；删除前检查套餐引用。
- 已实现套餐 `setmeal` 的 CRUD 与套餐菜品关系：`/admin/setmeal`（分页、新增、修改、批量删除、详情、分类列表、起售/停售）；删除前检查订单引用。
- 已实现管理端前端页面：登录、分类、员工、菜品、套餐、订单，Nginx 代理在 8082 端口。
- 已实现店铺营业状态：Redis 存 `SHOP_STATUS`，管理端查询/切换，用户端可查询。
- 已实现用户端：mock 登录、分类/菜品/套餐浏览、购物车、地址簿、下单、我的订单。
- 已实现管理端订单管理：分页、详情、接单、拒单、派送、完成。
- 已实现用户端前端页面：点餐、购物车、订单、地址，Nginx 在 8083 端口。
- 已实现模拟支付闭环：下单待付款 → 支付页 → 待接单；管理端只展示已支付订单。
- 已实现 WebSocket 来单提醒和今日经营概览 `/admin/report/statistics`。
- `create_user`/`update_user` 从 `BaseContext` 取当前登录人，未经过拦截器时回退为 `1`。
- `sql/sky_take_out.sql` 已包含建库、建表；`sql/seed.sql` 已包含 5 个分类、8 道菜、3 个套餐和管理员 `admin / 123456`（BCrypt）。
- 种子数据已导入 MySQL：5 个分类、8 道菜、3 个套餐、1 个管理员；图片是占位 PNG，见 `Code/frontend/images/README.md`。

## 数据库

1. 导入建库建表脚本（会创建 `sky_take_out` 库和 11 张表）：

   ```text
   mysql -u root -p --default-character-set=utf8mb4 < sql/sky_take_out.sql
   ```

2. 导入种子数据：

   ```text
   mysql -u root -p --default-character-set=utf8mb4 < sql/seed.sql
   ```

   PowerShell 不支持 `<`，可以用 `cmd /c` 包一层，或者直接用 DBeaver 打开两个 SQL 文件执行。

3. 配置数据库密码（必须）：

   - 启动前设置环境变量 `MYSQL_PASSWORD`，不写进配置文件；
   - 没设置时应用会直接启动失败，不会带着空密码启动；
   - 如果直接改 `application-dev.yml`，记得不要提交密码。

管理员账号：`admin / 123456`（数据库里存的是 BCrypt 哈希）。

## 启动后端

在 `Code/backend` 下执行：

```bash
# 第一次先安装 common/pojo 到本地仓库
mvn -DskipTests install

# 然后只启动 server 模块
mvn -pl sky-server spring-boot:run
```

也可以打包后直接运行：

```bash
mvn -DskipTests package
java -jar sky-server/target/sky-server-1.0-SNAPSHOT.jar
```

> 不要用 `mvn -pl sky-server -am spring-boot:run`：`-am` 会把父工程和 common/pojo 一起选进 reactor，`spring-boot:run` 会在父工程上执行，而父工程是 `pom` 打包、没有 main class，会报 `Unable to find a suitable main class`。

启动后：

- 后端：<http://localhost:8080>
- Swagger UI：<http://localhost:8080/swagger-ui.html>

> 注意：`MYSQL_PASSWORD` 必须在启动前设置，否则数据库连接会失败；数据库没建好时，第一次查询会报连接错误。数据库连接异常会返回“数据库连接失败，请检查数据库配置”，完整堆栈在控制台日志里。

## 启动前端

管理端前端在 `Code/frontend/admin`，用户端在 `Code/frontend/user`，都由 Nginx 提供。

1. 先启动后端（8080）。
2. 双击 `Code/frontend/start-nginx.bat`。
3. 管理端打开 <http://localhost:8082>，账号 `admin / 123456`。
4. 用户端打开 <http://localhost:8083>，mock code 填任意字符串即可登录。

> 首次登录会自动创建用户；mock code 只是演示用，不接真实微信登录。

停止 Nginx：双击 `Code/frontend/stop-nginx.bat`。

Nginx 配置在 [Code/frontend/nginx.conf](Code/frontend/nginx.conf)，Nginx 二进制不提交仓库：

- 8082：管理端页面
- 8083：用户端页面
- `/api/` 反代到 `http://127.0.0.1:8080/`
- `/images/` 指向 `Code/frontend/images`
- `/uploads/` 反代到后端

> 80 端口被 Steam++ Accelerator 占用，所以前端使用 8082 和 8083。

## Redis

- Memurai 8.2 已作为 Windows 服务安装，服务名 `Memurai`，自动启动，端口 6379。
- 营业状态 key：`SHOP_STATUS`，1 营业中，0 打烊。
- 管理端：`GET /admin/shop/status`、`PUT /admin/shop/{status}`
- 用户端：`GET /user/shop/status`
- 管理端页面顶部有“营业中/打烊”切换按钮。

## 支付

- 下单后订单状态为 `1`（待付款）
- 用户端支付页：`http://localhost:8083/pay.html?orderId=订单id`
- 模拟支付接口：`PUT /user/order/payment/{id}`
- 支付成功后状态变为 `2`（待接单），同时发送 WebSocket 来单提醒
- 管理端订单列表只展示状态 `>= 2` 的已支付订单

> 这是模拟支付，不接真实微信支付；真实支付需要商户号和证书。

## 开源说明

- 仓库不提交 `MYSQL_PASSWORD`、`.env`、`application-local.yml`、`uploads/`、`logs/` 和 Nginx 二进制。
- 复制 `.env.example` 作为变量清单，在 IDEA Run Configuration 或系统环境变量里设置 `MYSQL_PASSWORD`；Spring Boot 不会自动读取 `.env` 文件。
- Nginx 配置已改为相对路径，需把 Nginx 1.20.2 解压到 `Code/frontend/nginx-1.20.2` 后运行 `start-nginx.bat`。
- 项目使用 MIT License，见 [LICENSE](LICENSE)。

## 约定

- 基础包名：`com.sky`
- 启动类：`com.sky.SkyApplication`
- 模块名：`sky-common` / `sky-pojo` / `sky-server`
- 认证：JWT + 拦截器（管理端和用户端两套 token 配置）；管理端请求头名 `token`，用户端 `authentication`
- 分页：MyBatis-Plus 的 `Page` / `IPage`，不使用 PageHelper
- 主键：表主键是自增，`mybatis-plus.global-config.db-config.id-type` 已设为 `auto`
- 前端：`Code/frontend/admin`、`Code/frontend/user`、`Code/frontend/images`；图片路径 `/images/...`
- 注释：类/接口/公开方法写 Javadoc，关键分支写中文行内注释，不写无意义注释。

## 后续待办

1. 实现缓存、真实支付对接。
2. 自动化测试、图片上传 OSS、MySQL 服务自启动。
3. 报表页面、订单超时自动取消/退款。
