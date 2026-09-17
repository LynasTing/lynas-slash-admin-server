package com.lynas.slashadmin.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * Refresh Token Cookie 配置
 *
 * <p>refresh token 只通过 HttpOnly Cookie 传输
 * 本地 HTTP 开发时允许 Secure=false, 生产环境必须改为 true
 */
@Validated
@ConfigurationProperties(prefix = "security.refresh-cookie")
public record RefreshTokenCookieProperties(@NotBlank(message = "security.refresh-cookie.name 不能为空") String name,
  @NotBlank(message = "security.refresh-cookie.path 不能为空") String path, boolean httpOnly, boolean secure,
  @NotBlank(message = "security.refresh-cookie.same-site 不能为空")
  @Pattern(
    regexp = "Strict|Lax|None",
    message = "security.refresh-cookie.same-site 只能为 Strict | Lax | None") String sameSite,
  @NotNull(message = "security.refresh-cookie.max-age 不能为空")
  @DurationMin(seconds = 1, message = "security.refresh-cookie.max-age 必须大于 0") Duration maxAge) {

}
