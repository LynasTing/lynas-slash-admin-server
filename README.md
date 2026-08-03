# lynas-slash-admin-server

Java 21 / Spring Boot 3 / MyBatis-Plus 后端基础工程。

## 本地启动

1. 在 DBeaver 中连接本机 MySQL，并执行 [`sql/init.sql`](sql/init.sql)。
2. 复制 `src/main/resources/application-local.example.yml` 为 `application-local.yml`，填写本机 MySQL 密码。该文件已被 Git 忽略。
3. 执行 `./mvnw spring-boot:run`。
4. 访问 `http://localhost:8080/actuator/health`，应返回 `UP`。
5. 访问 `http://localhost:8080/system/user/list`，应返回统一响应格式的用户列表；初始化数据库不预置业务用户。

## 前端联通

前端开发服务器将 `/api` 转发到 `http://localhost:8080`。MSW 默认保持启用，避免在认证接口尚未实现时破坏既有页面；需要请求本地 Spring Boot 服务时，在前端 `.env.development.local` 中设置：

```properties
VITE_APP_USE_MOCK=false
```
