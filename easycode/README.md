# EasyCode 模板

本目录是可被 EasyCodePlus 直接导入的项目级模板根目录，`group.json`、`Templates/`、`GlobalConfig/`、`ColumnConfig/` 和 `TypeMapperConfig/` 缺一不可。

| 文件 | 生成内容 |
| --- | --- |
| `Templates/LynasSlashAdmin/entity.java.vm` | MyBatis-Plus 实体类 |
| `Templates/LynasSlashAdmin/mapper.java.vm` | `BaseMapper` 接口 |
| `Templates/LynasSlashAdmin/service.java.vm` | `IService` 接口 |
| `Templates/LynasSlashAdmin/service-impl.java.vm` | `ServiceImpl` 实现类 |

## 使用方式

1. 在数据库表上选择 `EasyCodePlus` → `GenerateFromEasyCodeFolderFile`。
2. 选择本目录 `easycode/`；选择的是目录本身，不是其中某个 `.vm` 文件。
3. 在模板组中选择 `LynasSlashAdmin`，确认包名和保存路径后生成。

不要使用带 `(old)` 的三个菜单。它们读取 IDE 内的旧配置，不会读取本目录。

## 约定

- 模板按当前项目的垂直模块结构生成到 `entity`、`mapper`、`service` 和 `service.impl` 包。
- 实体主键使用 `IdType.ASSIGN_UUID`，适用于当前项目的 `CHAR(36)` UUID 主键；自增主键表必须在生成后改为匹配的主键策略。
- 不启用 MyBatis-Plus 逻辑删除；`deleted` 或 `delFlag` 仅按普通数据库字段生成。
- 数据库字段注释生成在字段上方的 Javadoc 中；每个“字段注释 + 字段”之间保留一个空行，不生成字段行内单行注释。
- 模板生成的类作者固定为 `LynasTing`，且不生成 `Serializable` 或 `serialVersionUID`。当前实体未使用 Java 原生序列化，保留它们没有收益。
- `TypeMapperConfig/JavaTime.json` 将 `datetime`、`timestamp` 映射为 `LocalDateTime`，并保留 `date` → `LocalDate`、`time` → `LocalTime` 的语义。
- 生成代码只提供通用持久化骨架。Controller、请求/响应 DTO、业务校验、事务和自定义查询必须按具体用例手写。
- 修改本目录后，下次通过 `GenerateFromEasyCodeFolderFile` 重新选择 `easycode/` 即可加载新配置，无须将模板复制到 IDE 的旧配置中。
