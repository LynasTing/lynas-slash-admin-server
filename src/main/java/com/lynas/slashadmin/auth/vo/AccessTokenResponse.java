package com.lynas.slashadmin.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * 刷新访问令牌成功响应数据
 *
 * <p>refresh token 只通过 HttpOnly Cookie 返回，不进入该响应对象
 */
@Schema(description = "访问令牌响应数据")
public record AccessTokenResponse(@Schema(description = "访问令牌", example = "eyJhbGciOiJIUzI1NiJ9...") String accessToken,
  @Schema(description = "新的访问令牌的过期时间， 使用 ISO-8601 格式", example = "2026-08-14T10:30:00") Instant expiresAt) {
}
