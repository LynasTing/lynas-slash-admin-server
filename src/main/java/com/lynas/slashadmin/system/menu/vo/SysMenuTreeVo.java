package com.lynas.slashadmin.system.menu.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 菜单树节点响应数据。
 */
@Data
@Schema(description = "菜单树节点")
public class SysMenuTreeVo {
  @Schema(description = "菜单唯一标识", example = "1")
  private Long id;

  @Schema(description = "父级菜单唯一标识，0 表示根节点", example = "0")
  private Long parentId;

  @Schema(description = "父级菜单名称；根节点为 null", example = "系统管理")
  private String parentName;

  @Schema(description = "菜单名称", example = "角色管理")
  private String name;

  @Schema(description = "菜单唯一编码", example = "system:role:list")
  private String code;

  @Schema(description = "节点类型：1 分组，2 目录，3 菜单，4 操作按钮", example = "3")
  private Integer category;

  @Schema(description = "显示顺序，数值越小越靠前", example = "1")
  private Integer sort;

  @Schema(description = "启用状态：0 表示禁用，1 表示启用", example = "1")
  private Integer status;

  @Schema(description = "前端路由路径", example = "/system/role")
  private String path;

  @Schema(description = "前端组件标识或组件路径", example = "system/role/index")
  private String component;

  @Schema(description = "菜单图标", example = "UserFilled")
  private String icon;

  @Schema(description = "前端国际化翻译键", example = "route.system.role")
  private String i18nKey;

  @Schema(description = "是否隐藏：0 表示否，1 表示是", example = "0")
  private Integer hidden;

  @Schema(description = "菜单描述", example = "系统角色管理页面")
  private String description;

  @Schema(description = "外链地址，使用完整 HTTP 或 HTTPS URL")
  private String externalLink;

  @Schema(description = "子菜单；叶子节点返回空数组")
  private List<SysMenuTreeVo> children = new ArrayList<>();
}
