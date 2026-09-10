package com.lynas.slashadmin.system.role.entity;

import com.lynas.slashadmin.common.dto.PageQuery;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SysRolePageQuery extends PageQuery {
  @Size(max = 64, message = "角色名称不能超过 64 个字符")
  private String name;

  @Size(max = 64, message = "角色编码不能超过 64 个字符")
  private String code;

  @Min(value = 0, message = "角色状态只能为 0 或 1")
  @Max(value = 1, message = "角色状态只能为 0 或 1")
  private Integer status;
}
