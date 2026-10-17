package com.lynas.slashadmin.auth.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * 当前认证用户可访问的导航菜单节点
 *
 * <p>操作按钮（category = 4）只转换为 authority，不进入本菜单树
 */
public record AuthMenuNode(
  @Schema(description = "菜单主键 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED) Long id,
  @Schema(description = "父级菜单 ID，0 表示根节点", example = "0", requiredMode = Schema.RequiredMode.REQUIRED) Long parentId,
  @Schema(description = "菜单名称", example = "系统管理", requiredMode = Schema.RequiredMode.REQUIRED) String name,
  @Schema(
    description = "菜单唯一编码",
    example = "system:role:list",
    requiredMode = Schema.RequiredMode.REQUIRED) String code,
  @Schema(
    description = "节点类型：1 分组，2 目录，3 菜单",
    example = "3",
    requiredMode = Schema.RequiredMode.REQUIRED) Integer category,
  @Schema(description = "显示排序值", example = "1", requiredMode = Schema.RequiredMode.REQUIRED) Integer sort,
  @Schema(
    description = "启用状态：0 表示禁用，1 表示启用",
    example = "1",
    requiredMode = Schema.RequiredMode.REQUIRED) Integer status,
  @Schema(
    description = "前端路由路径；非页面节点时为 null",
    example = "/system/role",
    nullable = true,
    requiredMode = Schema.RequiredMode.REQUIRED) String path,
  @Schema(
    description = "前端组件标识或组件路径；非页面节点时为 null",
    example = "system/role/index",
    nullable = true,
    requiredMode = Schema.RequiredMode.REQUIRED) String component,
  @Schema(
    description = "菜单图标；未配置时为 null",
    example = "UserFilled",
    nullable = true,
    requiredMode = Schema.RequiredMode.REQUIRED) String icon,
  @Schema(
    description = "前端国际化资源键；未配置时为 null",
    example = "route.system.role",
    nullable = true,
    requiredMode = Schema.RequiredMode.REQUIRED) String i18nKey,
  @Schema(description = "是否隐藏：0 表示否，1 表示是", example = "0", requiredMode = Schema.RequiredMode.REQUIRED) Integer hidden,
  @Schema(
    description = "菜单描述；未填写时为 null",
    example = "系统角色管理页面",
    nullable = true,
    requiredMode = Schema.RequiredMode.REQUIRED) String description,
  @Schema(
    description = "菜单外链地址；非外链菜单时为 null",
    example = "https:/" + "/example.com/docs",
    nullable = true,
    requiredMode = Schema.RequiredMode.REQUIRED) String externalLink,
  @Schema(description = "子菜单；叶子节点返回空数组", requiredMode = Schema.RequiredMode.REQUIRED) List<AuthMenuNode> children) {
}
