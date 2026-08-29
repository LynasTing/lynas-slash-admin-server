package com.lynas.slashadmin.system.role.vo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 角色列表返回给客户端的数据。
 *
 * <p>本类只承载接口展示字段，不携带数据库映射注解；实体转换为本对象后再由 Controller 返回。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "角色列表项")
public class SysRoleListVo {
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

  @Schema(description = "角色补充说明", example = "拥有系统管理权限")
  private String description;
}
