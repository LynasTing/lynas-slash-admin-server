package com.lynas.slashadmin.common.exception;
import com.lynas.slashadmin.common.enums.ResponseCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
  private final ResponseCode responseCode;
  public BusinessException(ResponseCode responseCode) {
    super(responseCode.getMessage());
    this.responseCode = responseCode;
  }

  public ResponseCode getResponseCode() {
    return responseCode;
  }
}
