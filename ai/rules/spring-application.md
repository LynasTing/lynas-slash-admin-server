# Spring 应用规则

## 分层与依赖

- Controller 不能直接访问 Mapper；必须经由 Service。Controller 不承载事务、领域规则或 SQL 组装。
- Service 定义用例边界。涉及多个写操作时，在 Service 方法上声明最小必要范围的 `@Transactional`；只读查询使用 `readOnly = true`（确认底层行为适用时）。
- Mapper 只处理持久化查询和命令。禁止把 HTTP 对象、Controller 返回模型或安全上下文传入 Mapper。
- Entity 仅映射持久化结构；请求体和响应体必须使用独立 DTO，避免字段泄漏和实体耦合。
- 配置通过 `@ConfigurationProperties` 映射并完成校验；禁止散落的 `@Value` 与硬编码环境地址、密钥或开关。

## Web 与校验

- API 使用清晰、稳定的资源路径和 HTTP 语义；创建返回 `201 Created`，无响应体的成功删除返回 `204 No Content`。
- 请求 DTO 必须使用 Bean Validation 注解，Controller 参数使用 `@Valid`；跨字段和数据库约束仍须在 Service 层校验。
- 统一异常处理使用 `@RestControllerAdvice`。错误响应应包含稳定错误码、可读消息和必要的请求追踪标识，不能暴露内部异常详情。
- 分页必须有稳定排序；对外分页参数需设置合理上限，避免无界查询。
- 日志使用 SLF4J 参数化模板；不记录密码、令牌、身份证件、完整手机号或其他敏感个人信息。

## 安全与配置

- 认证、授权和数据权限在服务端强制执行；前端隐藏按钮不构成权限控制。
- 所有外部输入均不可信。使用参数绑定、白名单和长度限制，禁止拼接 SQL、路径、命令或表达式。
- `application-local.yml` 仅用于本地且已被忽略；示例配置不得包含真实凭据。生产密钥由部署环境注入。
- 修改数据库 schema、权限、状态值或 API 契约时，必须同步评估迁移、回滚、前端兼容和文档更新。
