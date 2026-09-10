package com.lynas.slashadmin.system.role.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 新增/修改 角色参数
 */
@Data
@Schema(name = "新增/修改角色参数", description = "新增时主键 ID 由服务端创建")
public class SysRoleSaveDto {
  @NotBlank(message = "角色名称不能为空")
  @Size(max = 64, message = "角色名称不能超过 64 个字符")
  @Schema(description = "角色名称", requiredMode = Schema.RequiredMode.REQUIRED)
  private String name;

  @NotBlank(message = "角色编码不能为空")
  @Size(max = 64, message = "角色编码不能超过 64 个字符")
  @Schema(description = "角色编码", requiredMode = Schema.RequiredMode.REQUIRED)
  private String code;

  @Schema(description = "关联菜单 ID；省略、null 或空数组表示清空授权，重复 ID 合并", example = "[1, 2, 3]")
  private List<@NotNull(message = "菜单 ID 不能为空") @Positive(message = "菜单 ID 必须大于 0") Long> menuIds;

  @NotNull(message = "排序值不能为空")
  @Min(value = 0, message = "排序值不能小于 0")
  @Schema(description = "排序", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
  private Integer sort;

  @NotNull(message = "角色状态不能为空")
  @Min(value = 0, message = "角色状态只能为 0 或 1")
  @Max(value = 1, message = "角色状态只能为 0 或 1")
  @Schema(description = "启用状态：0 禁用，1 启用", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
  private Integer status;

  @Size(max = 255, message = "角色描述不能超过 255 个字符")
  @Schema(description = "角色描述", example = "管理员")
  private String description;
}
