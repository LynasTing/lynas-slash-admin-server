package com.lynas.slashadmin.common.exception;

import com.lynas.slashadmin.common.api.ApiResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** 将常见异常转换为稳定的 API 响应，避免向前端泄露数据库或服务端细节。 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Void>> handleValidationException(
      MethodArgumentNotValidException exception) {
    String message =
        exception.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(error -> error.getDefaultMessage() == null ? "请求参数不合法" : error.getDefaultMessage())
            .orElse("请求参数不合法");
    return ResponseEntity.badRequest()
        .body(ApiResponse.failure(HttpStatus.BAD_REQUEST.value(), message));
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolationException() {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(ApiResponse.failure(HttpStatus.CONFLICT.value(), "数据约束冲突"));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleUnexpectedException() {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ApiResponse.failure(HttpStatus.INTERNAL_SERVER_ERROR.value(), "服务器内部错误"));
  }
}
