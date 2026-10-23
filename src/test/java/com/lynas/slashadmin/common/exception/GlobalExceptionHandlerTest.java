package com.lynas.slashadmin.common.exception;

import com.lynas.slashadmin.auth.enums.AuthCodeEnum;
import com.lynas.slashadmin.common.enums.ResponseCodeEnum;
import com.lynas.slashadmin.common.response.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 全局业务异常的 HTTP 状态映射测试。
 */
class GlobalExceptionHandlerTest {
  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

  @Test
  void handleBusinessException_whenNeedLogin_returnsHttp401() {
    ResponseEntity<ApiResponse<Void>> response = handler
      .handleBusinessException(new BusinessException(ResponseCodeEnum.NEED_LOGIN));

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    assertEquals(401, response.getBody().getCode());
  }

  @Test
  void handleBusinessException_whenForbidden_returnsHttp403() {
    ResponseEntity<ApiResponse<Void>> response = handler
      .handleBusinessException(new BusinessException(ResponseCodeEnum.FORBIDDEN));

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    assertEquals(403, response.getBody().getCode());
  }

  @Test
  void handleBusinessException_whenNormalBusinessError_keepsHttp409() {
    ResponseEntity<ApiResponse<Void>> response = handler
      .handleBusinessException(new BusinessException(AuthCodeEnum.PARAMETER_MISSING));

    assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    assertEquals(518, response.getBody().getCode());
  }
}
