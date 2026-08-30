package com.lynas.slashadmin.system.role.enums;

import com.lynas.slashadmin.common.enums.ResponseCode;

public enum SysRoleEnum implements ResponseCode {
  /**
   * 角色名称已存在
   */
  NAME_ALREADY_EXISTS(511, "角色名称已存在"),

  /**
   * 角色编码已存在
   */
  CODE_ALREADY_EXISTS(511, "角色编码已存在"),

  /**
   * 角色不存在
   */
  ROLE_NOT_EXIST(512, "角色不存在");

  private final int code;
  private final String message;

  SysRoleEnum(int code, String message) {
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
