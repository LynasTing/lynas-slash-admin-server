package com.lynas.slashadmin.common.security;

import com.lynas.slashadmin.auth.model.UserAuthSnapshot;
import com.lynas.slashadmin.auth.service.UserAuthorizationService;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/**
 * 补充 JWT 签名校验的当前用户认证状态校验器
 *
 * <p>JWT 签名只能证明 Token 未被篡改，不能证明当前用户仍启用。
 * 本校验器读取最小用户认证快照，使禁用、删除和认证版本变更立即使旧 JWT 失效
 */
@Component
public final class UserAuthenticationValidator implements OAuth2TokenValidator<Jwt> {

  private static final String AUTH_VERSION_CLAIM = "av";
  private static final OAuth2Error INVALID_TOKEN_ERROR = new OAuth2Error("invalid_token", "JWT 无效或已失效", null);
  private final UserAuthorizationService userAuthorizationService;

  /**
   * 注入当前用户认证状态查询服务
   *
   * @param userAuthorizationService 用户认证与授权读取服务
   */
  public UserAuthenticationValidator(UserAuthorizationService userAuthorizationService) {
    this.userAuthorizationService = userAuthorizationService;
  }

  /**
   * 构造统一的无效 token 校验结果
   *
   * @return OAuth2 invalid_token 校验失败结果
   */
  private OAuth2TokenValidatorResult invalidToken() {
    return OAuth2TokenValidatorResult.failure(INVALID_TOKEN_ERROR);
  }

  /**
   * 判断数据库快照是否仍与 JWT 的认证状态一致
   *
   * @param snapshot         当前用户认证快照
   * @param tokenAuthVersion JWT 中的认证版本
   * @return 用户启用、未删除且认证版本一致时返回 true
   */
  private boolean isCurrentAuthentication(
    UserAuthSnapshot snapshot,
    Integer tokenAuthVersion) {
    return Integer.valueOf(1).equals(snapshot.status()) && Integer.valueOf(0).equals(snapshot.deleted())
      && tokenAuthVersion.equals(snapshot.authVersion());
  }

  /**
   * 严格解析 JWT 自定义 av claim 的认证版本
   *
   * @param claim JWT 中的 av claim
   * @return 合法认证版本；类型或范围错误时 null
   */
  private Integer parseAuthVersion(
    Object claim) {
    if (claim instanceof Integer authVersion && authVersion > 0) {
      return authVersion;
    }
    if (claim instanceof Long authVersion && authVersion > 0 && authVersion <= Integer.MAX_VALUE) {
      return authVersion.intValue();
    }
    return null;
  }

  /**
   * 严格解析 JWT subject 中的正整数用户 ID
   *
   * @param subject JWT subject claim
   * @return 合法用户 ID; 格式错误时返回 null;
   */
  private Long parseUserId(
    String subject) {
    if (subject == null || subject.isBlank()) {
      return null;
    }
    try {
      long userId = Long.parseLong(subject);
      return userId > 0 ? userId : null;
    } catch (NumberFormatException exception) {
      return null;
    }
  }

  /**
   * 校验 JWT 中的用户 ID、认证版本与数据库当前认证状态
   *
   * @param jwt 已完成基础签名和时间校验的 JWT
   * @return 用户状态和认证版本均匹配时成功，否则返回 invalid_token
   */
  @Override
  public OAuth2TokenValidatorResult validate(
    Jwt jwt) {
    Long userId = parseUserId(jwt.getSubject());
    Integer tokenAuthVersion = parseAuthVersion(jwt.getClaim(AUTH_VERSION_CLAIM));

    if (userId == null || tokenAuthVersion == null) {
      return invalidToken();
    }

    return userAuthorizationService.findAuthenticationSnapshot(userId)
      .filter(snapshot -> isCurrentAuthentication(snapshot, tokenAuthVersion))
      .map(snapshot -> OAuth2TokenValidatorResult.success()).orElseGet(this::invalidToken);
  }

}
