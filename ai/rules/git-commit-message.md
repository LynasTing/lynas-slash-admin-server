# Git 提交信息规则

## 格式

本项目使用 Commitlint 和 Conventional Commits。提交标题必须符合：

```txt
<type>(<scope>): <subject>
```

`scope` 可省略，但应优先使用实际业务模块，例如 `user`、`role`、`auth`、`config`、`db`。

允许的 `type`：`feat`、`fix`、`refactor`、`perf`、`test`、`docs`、`style`、`build`、`ci`、`chore`、`revert`。

## 内容

- 本项目的提交信息说明一律使用中文：标题的 `<subject>` 和提交正文不得使用英文。`type` 与 `scope` 是 Conventional Commits 的结构化前缀，不受此限制。
- 先读取暂存区的实际变更再生成提交信息；禁止根据文件名臆测。
- 标题使用简洁中文、祈使语气且不以句号结尾，例如 `feat(user): 新增账号状态更新`。
- 改动超过两个独立要点时，在正文中使用完整中文说明各项改动。
- 一次提交只包含可独立理解的一组改动；无关变更必须拆分。
- 禁止 `update code`、`fix bug`、`修改文件` 等空泛标题，也不得夸大影响范围。

## 本地校验

安装依赖后，`pnpm run commitlint -- --edit .git/COMMIT_EDITMSG` 可手动校验提交信息；`.husky/commit-msg` 会在提交时自动执行同一检查。
