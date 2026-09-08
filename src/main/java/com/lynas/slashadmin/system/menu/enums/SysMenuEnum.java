package com.lynas.slashadmin.system.menu.enums;

import com.lynas.slashadmin.common.enums.ResponseCode;

public enum SysMenuEnum implements ResponseCode {
  /**
   * 菜单名称已存在
   */
  NAME_ALREADY_EXISTS(511, "菜单名称已存在"),

  /**
   * 菜单编码已存在
   */
  CODE_ALREADY_EXISTS(511, "菜单编码已存在"),

  /**
   * 菜单 ID 不存在
   */
  ID_NOT_FOUND(512, "菜单 ID 不存在"),

  PARENT_NOT_FOUND(513, "父级菜单不存在"),

  PARENT_CANNOT_BE_SELF(514, "父级菜单不能是自身"),

  PARENT_CANNOT_BE_DESCENDANT(515, "父级菜单不能是当前菜单的子节点"),

  INVALID_CATEGORY_HIERARCHY(516, "菜单类型层级不合法"),

  MENU_HAS_CHILDREN(517, "菜单存在子菜单，无法删除");

  private final int code;
  private final String message;

  SysMenuEnum(int code, String message) {
    this.code = code;
    this.message = message;
  }

  public int getCode() {
    return code;
  }

  public String getMessage() {
    return message;
  }
}
