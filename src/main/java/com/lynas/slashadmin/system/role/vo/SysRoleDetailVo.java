package com.lynas.slashadmin.system.role.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * 角色编辑回显数据。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "角色详情")
public class SysRoleDetailVo {
  @Schema(description = "角色主键 ID", example = "1")
  private Long id;

  @Schema(description = "角色名称", example = "系统管理员")
  private String name;

  @Schema(description = "角色编码", example = "ADMIN")
  private String code;

  @Schema(description = "显示顺序", example = "1")
  private Integer sort;

  @Schema(description = "启用状态：0 表示禁用，1 表示启用", example = "1")
  private Integer status;

  @Schema(description = "角色补充说明，未填写时可以为 null", example = "拥有系统管理权限")
  private String description;

  @Schema(description = "已授权菜单 ID 列表", example = "[1, 2, 3]")
  private List<Long> menuIds;
}
