# 项目上下文

## 定位与技术栈

- 本项目是 `lynas-slash-admin` 的 Java 后端服务。
- 使用 Java 21、Spring Boot 3、Spring MVC、Bean Validation、MyBatis-Plus 和 MySQL。
- 根包为 `com.lynas.slashadmin`；新业务代码必须位于其下，包名全小写。
- 前端项目路径：`/Users/LynasTing/Documents/project/Ting/lynas-slash-admin`。前后端共享的业务语义、接口字段和提交规范应保持一致，但不要照搬前端实现细节。

## 目录约定

- `controller`：HTTP 入站适配层；只负责参数绑定、鉴权边界、调用应用服务和返回响应。
- `service`：业务用例、事务边界和跨聚合协调。
- `mapper`：数据库访问；不得承载业务编排。
- `entity`：持久化模型；不得直接作为对外请求或响应模型。
- `dto`：请求、响应和跨层传输模型；按 `request`、`response` 或具体业务进一步分包。
- `config`、`exception`、`common`：分别放配置、异常处理和真正跨模块的公共能力。禁止把业务逻辑塞进 `common`。

包应按业务能力优先组织；当模块扩大后，优先采用 `user`、`role` 等垂直业务包，并在包内分层，而不是无限扩张全局 `controller/service/mapper` 目录。
