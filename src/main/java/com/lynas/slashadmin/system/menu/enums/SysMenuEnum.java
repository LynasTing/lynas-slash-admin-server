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
  ID_NOT_FOUND(512, "菜单 ID 不存在");

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
