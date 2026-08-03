# EasyCode 模板

这四份模板是项目受版本控制的 EasyCode 模板源文件。将对应文件内容粘贴到 IntelliJ IDEA 的 EasyCode 模板编辑器后，选择数据库表即可生成基础持久化层代码。

| 文件 | 生成内容 |
| --- | --- |
| `entity.java.vm` | MyBatis-Plus 实体类 |
| `mapper.java.vm` | `BaseMapper` 接口 |
| `service.java.vm` | `IService` 接口 |
| `service-impl.java.vm` | `ServiceImpl` 实现类 |

## 约定

- 模板按当前项目的垂直模块结构生成到 `entity`、`mapper`、`service` 和 `service.impl` 包。
- 实体主键使用 `IdType.ASSIGN_UUID`，适用于当前项目的 `CHAR(36)` UUID 主键；自增主键表必须在生成后改为匹配的主键策略。
- 名为 `deleted` 或 `delFlag` 的字段自动加 `@TableLogic`；当前项目的全局逻辑删除字段为 `deleted`。
- 数据库字段注释生成在字段上方的 Javadoc 中，不生成字段行内单行注释。
- 生成代码只提供通用持久化骨架。Controller、请求/响应 DTO、业务校验、事务和自定义查询必须按具体用例手写。
