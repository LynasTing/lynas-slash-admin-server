package com.lynas.slashadmin.auth.enums;

import com.lynas.slashadmin.common.enums.ResponseCode;

public enum AuthCodeEnum implements ResponseCode {

  /**
   * 参数缺失
   */
  PARAMETER_MISSING(518, "参数缺失");

  private int code;
  private String message;

  AuthCodeEnum(Integer code, String message) {
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
