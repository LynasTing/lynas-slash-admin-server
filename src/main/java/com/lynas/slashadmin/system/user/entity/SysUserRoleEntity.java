package com.lynas.slashadmin.system.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户与角色关联表实体，用于授权关联及角色删除前的关联检查。
 */
@Getter
@Setter
@TableName("sys_user_role")
public class SysUserRoleEntity {
  /** 用户主键 */
  private Long userId;

  /** 角色主键 */
  private Long roleId;

  public SysUserRoleEntity() {
  }

  public SysUserRoleEntity(Long userId, Long roleId) {
    this.userId = userId;
    this.roleId = roleId;
  }
}
