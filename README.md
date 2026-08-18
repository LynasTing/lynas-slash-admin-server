# lynas-slash-admin-server

Java 21 / Spring Boot 3 / MyBatis-Plus 后端基础工程。

## 代码格式化

IntelliJ 项目已启用 `Format Java with Spotless on save` File Watcher：仅在手动保存 Java 文件时，会执行项目的 Eclipse formatter，不会为触发格式化而自动保存文件。提交时 Git hook 也会自动格式化已暂存的 Java 文件，并重新暂存格式化结果，随后执行 Maven 校验。因此正常开发和提交前不需要手动执行 `spotless:apply`。

首次克隆项目后执行一次 `pnpm install`，以启用 Husky hook。若某个 Java 文件处于“部分暂存”状态，hook 会中止提交，避免将未暂存的业务修改错误加入提交；请先分别处理该文件的暂存内容后再提交。

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
