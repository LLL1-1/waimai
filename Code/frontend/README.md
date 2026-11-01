# 前端目录

- `admin/`：管理端页面（登录、分类、员工、菜品、套餐、订单）
- `user/`：用户端页面（登录、点餐、购物车、订单、地址、支付）
- `images/`：菜品、套餐图片；数据库里存 `/images/...`
- `nginx.conf`：Nginx 配置，使用相对路径
- `mime.types`：Nginx MIME 配置
- `start-nginx.bat` / `stop-nginx.bat`：启动/停止 Nginx

> Nginx 二进制不提交仓库，需要把 Nginx 1.20.2 解压到 `Code/frontend/nginx-1.20.2`。

访问地址：

- 管理端：<http://localhost:8082>
- 用户端：<http://localhost:8083>

代理关系：

- `/api/` → `http://127.0.0.1:8080/`
- `/images/` → `Code/frontend/images`
- `/uploads/` → `http://127.0.0.1:8080/uploads/`

这里只放前端和静态资源，不要放 Java 后端代码。
