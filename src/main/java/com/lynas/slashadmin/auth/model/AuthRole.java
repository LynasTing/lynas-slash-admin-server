package com.lynas.slashadmin.auth.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 当前认证用户拥有的启用角色。
 *
 * <p>该模型用于登录和当前用户响应，不包含角色管理页面的排序、状态或描述字段
 */
public record AuthRole(
  @Schema(description = "角色主键 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED) Long id,
  @Schema(description = "角色唯一编码", example = "ADMIN", requiredMode = Schema.RequiredMode.REQUIRED) String code,
  @Schema(description = "角色名称", example = "系统管理员", requiredMode = Schema.RequiredMode.REQUIRED) String name) {
}
