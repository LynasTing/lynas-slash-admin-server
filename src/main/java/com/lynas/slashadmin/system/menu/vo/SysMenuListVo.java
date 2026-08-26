package com.lynas.slashadmin.system.menu.vo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 菜单列表响应数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "菜单列表项")
public class SysMenuListVo {
  @Schema(description = "菜单唯一标识", example = "b3e8f8a4-9af1-4eb9-9d88-4b28170b3e23")
  private Object id;

  @Schema(description = "父级菜单唯一标识，0 表示根节点", example = "0")
  private Object parentId;

  @Schema(description = "菜单名称", example = "系统管理")
  private String name;

  @Schema(description = "菜单唯一编码", example = "SYSTEM_MANAGEMENT")
  private String code;

  @Schema(description = "节点类型：1 分组，2 目录，3 菜单，4 操作按钮", example = "2")
  private Object category;

  @Schema(description = "显示顺序，数值越小越靠前", example = "1")
  private Object sort;

  @Schema(description = "启用状态：0 表示禁用，1 表示启用", example = "1")
  private Object status;

  @Schema(description = "前端路由路径", example = "/system")
  private String path;

  @Schema(description = "前端组件标识或组件路径", example = "Layout")
  private String component;

  @Schema(description = "菜单图标", example = "Setting")
  private String icon;

  @Schema(description = "是否隐藏：0 表示否，1 表示是", example = "0")
  private Object hidden;

  @Schema(description = "菜单描述", example = "系统管理相关功能")
  private String description;

  @Schema(description = "外链地址，使用完整 HTTP 或 HTTPS URL")
  private String externalLink;
}
