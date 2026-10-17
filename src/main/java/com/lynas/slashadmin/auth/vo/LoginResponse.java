package com.lynas.slashadmin.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "登录成功响应")
public record LoginResponse(
  @Schema(
    description = "访问令牌",
    example = "eyJhbGciOiJIUzI1NiJ9...",
    requiredMode = Schema.RequiredMode.REQUIRED) String accessToken,
  @Schema(description = "当前登录用户信息", requiredMode = Schema.RequiredMode.REQUIRED) AuthenticatedUserVo user,
  @Schema(
    description = "访问令牌过期时间，使用 ISO-8601 格式",
    example = "2026-08-14T10:30:00",
    requiredMode = Schema.RequiredMode.REQUIRED) Instant expiresAt) {
}
