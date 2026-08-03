# AGENT.md

## 使用方式

开始分析、修改或生成代码前，必须先阅读并遵循：

- `ai/project-context.md`
- `ai/rules/java-code-style.md`
- `ai/rules/spring-application.md`
- `ai/rules/api-and-data.md`
- `ai/rules/testing.md`
- `ai/rules/git-commit-message.md`
- `ai/workflow/development-workflow.md`
- `ai/workflow/pr-review-flow.md`

## 执行要求

- 以 `ai/rules` 中更具体的规则覆盖通用规则；与现有项目约束冲突时，以项目实际配置和用户明确要求为准。
- 改动前确认受影响的模块、接口、数据库对象和测试边界；不要仅凭文件名猜测业务意图。
- 改动后执行与风险匹配的验证，并在交付说明中列出已遵循的规则和实际执行的验证命令。
- 生成提交信息前，先读取暂存区的真实改动，并遵循 `ai/rules/git-commit-message.md`。
