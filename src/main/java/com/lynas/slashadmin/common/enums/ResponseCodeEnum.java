package com.lynas.slashadmin.common.enums;

public enum ResponseCodeEnum implements ResponseCode {
  /**
   * 成功
   */
  SUCCESS(200, "操作成功"),

  /**
   * 鉴权失败（未登录或 token 过期）
   */
  NEED_LOGIN(401, "需登录后操作"),

  /**
   * 用户名或密码错误
   */
  AUTHENTICATION_FAILED(401, "用户名或密码错误"),

  /**
   * 权限不足
   */
  FORBIDDEN(403, "权限不足"),

  /**
   * 异常错误
   */
  SYSTEM_ERROR(500, "异常错误"),

  /**
   * ID 为空
   */
  ID_IS_NULL(513, "ID 不能为空");

  private int code;
  private String message;

  ResponseCodeEnum(int code, String message) {
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
