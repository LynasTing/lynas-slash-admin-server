package com.lynas.slashadmin.system.user.enums;

import com.lynas.slashadmin.common.enums.ResponseCode;

public enum SysUserEnums implements ResponseCode {
  /**
   * 用户名重复
   */
  NAME_ALREADY_EXISTS(511, "用户名重复"),
  /**
   * 邮箱重复
   */
  EMAIL_ALREADY_EXISTS(512, "邮箱已被使用"),
  /**
   * 用户不存在
   */
  USER_NOT_EXIST(513, "用户不存在"),
  /**
   * 密码格式不正确
   */
  PASSWORD_INVALID(514, "密码长度必须为 8 到 72 个字符"),
  /**
   * 角色不存在
   */
  ROLE_NOT_EXIST(515, "角色不存在");
  private final int code;
  private final String message;
  SysUserEnums(int code, String message) {
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
