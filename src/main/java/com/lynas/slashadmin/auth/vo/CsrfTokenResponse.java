package com.lynas.slashadmin.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * CSRF token 响应数据
 *
 * <p>CSRF token 会同时由 CookieCsrfTokenRepository 写入可读 Cookie，
 * 前端请求修改类操作时，应将 token 放入指定请求头
 */
@Schema(description = "CSRF Token 响应")
public record CsrfTokenResponse(
  @Schema(
    description = "CSRF token 值",
    example = "a1b2c3d4e5f6...",
    requiredMode = Schema.RequiredMode.REQUIRED) String token,
  @Schema(
    description = "提交 CSRF token 时使用的请求头名称",
    example = "X-XSRF-TOKEN",
    requiredMode = Schema.RequiredMode.REQUIRED) String headerName,
  @Schema(
    description = "CSRF token 参数名称",
    example = "_csrf",
    requiredMode = Schema.RequiredMode.REQUIRED) String parameterName) {
}
