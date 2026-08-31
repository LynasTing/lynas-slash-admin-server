package com.lynas.slashadmin.system.role.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 系统角色与菜单的关联表实体。
 *
 * <p>对应 {@code sys_role_menu} 表。一条记录表示一个角色拥有一个菜单；该表使用
 * {@code (role_id, menu_id)} 复合主键，因此不声明 {@code @TableId}。
 */
@Getter
@Setter
@NoArgsConstructor
@TableName("sys_role_menu")
public class SysRoleMenuEntity {
  /** 关联角色 ID。 */
  private Long roleId;

  /** 关联菜单 ID。 */
  private Long menuId;

  /** 创建时间。 */
  private LocalDateTime createdAt;

  /**
   * 创建角色与菜单的关联。
   *
   * @param roleId 角色 ID
   * @param menuId 菜单 ID
   */
  public SysRoleMenuEntity(Long roleId, Long menuId) {
    this.roleId = roleId;
    this.menuId = menuId;
  }
}
