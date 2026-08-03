package com.lynas.slashadmin.common.api;

/**
 * 与前端 Axios 拦截器兼容的统一响应结构。
 *
 * @param <T> 响应数据类型
 */
public record ApiResponse<T>(int status, String message, T data) {

  public static <T> ApiResponse<T> success(T data) {
    return new ApiResponse<>(0, "", data);
  }

  public static ApiResponse<Void> failure(int status, String message) {
    return new ApiResponse<>(status, message, null);
  }
}
