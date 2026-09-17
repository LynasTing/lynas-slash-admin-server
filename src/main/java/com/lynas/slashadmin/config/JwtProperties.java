package com.lynas.slashadmin.config;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.Base64;

/**
 * JWT 签发与验证所需的安全配置
 *
 * <p>密钥仅从本地未提交配置或环境变量读取，不能写入源码、日志或接口响应。
 */
@Validated
@ConfigurationProperties(prefix = "security.jwt")
public record JwtProperties(@NotBlank(message = "security.jwt.issuer 不能为空") String issuer,

  @NotBlank(message = "security.jwt.audience 不能为空") String audience,
  @NotBlank(message = "security.jwt.secret-base64 不能为空") String secretBase64,
  @NotNull(message = "security.jwt.access-token-ttl 不能为空")
  @DurationMin(seconds = 1, message = "security.jwt.access-token-ttl 必须大于 0") Duration accessTokenTtl,
  @NotNull(message = "security.jwt.refresh-token-ttl 不能为空")
  @DurationMin(seconds = 1, message = "security.jwt.refresh-token-ttl 必须大于 0") Duration refreshTokenTtl) {
  /**
   * 校验 HS256 密钥是合法 Base64, 且解码后至少包含 32 字节 随机数据
   *
   * @return 密钥格式和长度均有效时 返回 true
   */
  @AssertTrue(message = "security.jwt.secret-base64 必须是合法 Base64, 且解码后至少为 32 字节")
  public boolean isSecretBase64Valid() {
    if (secretBase64 == null || secretBase64.isBlank()) {
      return false;
    }

    try {
      return Base64.getDecoder().decode(secretBase64).length >= 32;
    } catch (IllegalArgumentException exception) {
      return false;
    }
  }
}
