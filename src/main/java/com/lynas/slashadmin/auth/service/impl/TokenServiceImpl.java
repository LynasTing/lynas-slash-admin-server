package com.lynas.slashadmin.auth.service.impl;

import com.lynas.slashadmin.auth.model.AuthenticatedUser;
import com.lynas.slashadmin.auth.service.TokenService;
import com.lynas.slashadmin.config.JwtProperties;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Access JWT 签发与 Refresh Token 安全值处理实现
 *
 * <p>本类只生成和计算 Token 值，不负责用户名密码校验、数据库会话写入、
 * Cookie 写入或 HTTP 响应构造。这样登录与刷新流程可以复用同一套 Token 安全规则
 */
@Service
public class TokenServiceImpl implements TokenService {
  private static final String AUTH_VERSION_CLAIM = "av";
  private static final String AUTHORITIES_CLAIM = "authorities";
  private static final String TOKEN_TYPE = "JWT";
  private static final int REFRESH_TOKEN_RANDOM_BYTES = 32;

  private final JwtEncoder jwtEncoder;
  private final JwtProperties jwtProperties;
  private final Clock clock;
  private final SecureRandom secureRandom;

  /**
   * 注入 JWT 编码器、配置与可替换的时间和随机数来源
   *
   * <p>Clock 和 SecureRandom 均通过构造器注入，使测试能够使用固定时间或可控随机源，
   * 生产环境则由后续安全配置提供 UTC 时钟和加密安全随机数
   *
   * @param jwtEncoder    Access JWT 编码器
   * @param jwtProperties JWT 配置属性
   * @param clock         统一时间来源
   * @param secureRandom  加密安全随机数来源
   */
  public TokenServiceImpl(JwtEncoder jwtEncoder, JwtProperties jwtProperties, Clock clock, SecureRandom secureRandom) {
    this.jwtEncoder = jwtEncoder;
    this.jwtProperties = jwtProperties;
    this.clock = clock;
    this.secureRandom = secureRandom;
  }

  /**
   * 校验签发 JWT 所需的内部认证数据完整性
   *
   * @param user 待签发 JWT 的认证用户
   */
  private void validateAuthenticatedUser(
    AuthenticatedUser user) {
    Objects.requireNonNull(user, "认证用户不能为空");

    if (user.userId() == null || user.userId() <= 0) {
      throw new IllegalArgumentException("认证用户 ID 不正确");
    }

    if (user.authVersion() == null || user.authVersion() <= 0) {
      throw new IllegalArgumentException("认证版本不正确");
    }

    Objects.requireNonNull(user.authorities(), "认证用户权限不能为空");
  }

  /**
   * 计算 Refresh Token 的 SHA-256 十六进制摘要
   *
   * @param rawToken Refresh Token 原文
   * @return 固定长度的 SHA-256 十六进制摘要
   */
  @Override
  public String hashRefreshToken(
    String rawToken) {
    if (rawToken == null || rawToken.isBlank()) {
      throw new IllegalArgumentException("Refresh Token 不能为空");
    }
    try {
      byte[] digest = MessageDigest.getInstance("SHA-256").digest(rawToken.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(digest);
    } catch (NoSuchAlgorithmException exception) {
      // SHA-256 是 Java 平台必须提供的算法，缺失则表示运行环境异常
      throw new IllegalStateException("当前 Java 环境不支持 SHA-256", exception);
    }
  }

  /**
   * 生成 Refresh Token 原文及其数据库摘要
   *
   * <p>原文由 32 字节加密安全随机数经 Base64 URL 编码生成，不使用 UUID。
   * 数据库只保存摘要，原文仅在后续认证流程中写入 HttpOnly Cookie
   *
   * @return 新生成的 Refresh Token 原文和 SHA-256 摘要
   */
  @Override
  public GeneratedRefreshToken createRefreshToken() {
    byte[] randomBytes = new byte[REFRESH_TOKEN_RANDOM_BYTES];
    secureRandom.nextBytes(randomBytes);

    String rawValue = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

    return new GeneratedRefreshToken(rawValue, hashRefreshToken(rawValue));
  }

  /**
   * 为认证用户签发 HS256 Access JWT
   *
   * <p>Token 不携带密码、邮箱、菜单等敏感或容易过期的数据，只携带服务端鉴权所需的
   * 用户ID、认证版本和 authority 快照
   *
   * @param user 已完成状态校验并加载授权数据的认证用户
   * @return 已签名的 Access JWT
   */
  @Override
  public String createAccessToken(
    AuthenticatedUser user) {
    validateAuthenticatedUser(user);

    Instant issuedAt = clock.instant();
    Instant expiresAt = issuedAt.plus(jwtProperties.accessTokenTtl());

    // 创建不可变的权限快照，避免 JWT 编码期间被调用方法修改集合内容
    List<String> authorities = List.copyOf(user.authorities());

    JwtClaimsSet claims = JwtClaimsSet.builder().issuer(jwtProperties.issuer())
      .audience(List.of(jwtProperties.audience())).subject(String.valueOf(user.userId())).issuedAt(issuedAt)
      .expiresAt(expiresAt).id(UUID.randomUUID().toString()).claim(AUTH_VERSION_CLAIM, user.authVersion())
      .claim(AUTHORITIES_CLAIM, authorities).build();

    // 显式指定 HS256，禁止编码器根据底层密钥类型隐式选择其它算法
    JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).type(TOKEN_TYPE).build();

    return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
  }
}
