package com.lynas.slashadmin.system.role.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 所有角色列表（不分页模式）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "所有角色列表（不分页模式）")
public class SysRoleOptionVo {
  @Schema(description = "角色主键 ID", example = "1")
  private Long id;

  @Schema(description = "角色名称", example = "系统管理员")
  private String name;

  @Schema(description = "角色编码", example = "ADMIN")
  private String code;

  @Schema(description = "启用状态：0 表示禁用，1 表示启用", example = "1")
  private Integer status;

  @Schema(description = "显示顺序", example = "1")
  private Integer sort;
}
