# 测试规则

- 新增或修改业务规则时，至少覆盖正常路径、关键边界和失败路径；修复缺陷应先或同时添加能复现缺陷的测试。
- 纯业务逻辑优先写 JUnit 5 单元测试，不启动 Spring 上下文。
- Web 层使用 `@WebMvcTest` 与 MockMvc 测试参数绑定、校验、状态码和错误响应；不要用完整上下文测试替代所有 Controller 测试。
- 持久化与跨层集成场景使用 `@MybatisTest`、`@SpringBootTest` 或真实隔离数据库，按实际依赖选择。`@SpringBootTest` 只用于确实需要完整装配的路径。
- 测试名称描述行为和预期，例如 `createUser_whenUsernameExists_throwsConflictException`。
- 测试必须独立、可重复执行且不依赖执行顺序、真实生产服务或本机残留数据。时间、随机数和外部客户端应可控制或替换。
- 断言业务结果和可观察副作用，而不是无关的私有实现细节。
