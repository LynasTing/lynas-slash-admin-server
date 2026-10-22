package com.lynas.slashadmin.auth.service.impl;

import com.lynas.slashadmin.auth.entity.RefreshTokenEntity;
import com.lynas.slashadmin.auth.mapper.RefreshTokenMapper;
import com.lynas.slashadmin.auth.service.AuthSessionService;
import com.lynas.slashadmin.auth.service.RefreshTokenCookieService;
import com.lynas.slashadmin.auth.service.TokenService;
import com.lynas.slashadmin.auth.service.UserAuthorizationService;
import com.lynas.slashadmin.common.enums.ResponseCodeEnum;
import com.lynas.slashadmin.common.exception.BusinessException;
import com.lynas.slashadmin.config.JwtProperties;
import com.lynas.slashadmin.system.role.mapper.SysRoleMapper;
import com.lynas.slashadmin.system.user.mapper.SysUserMapper;
import com.lynas.slashadmin.system.user.mapper.SysUserRoleMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * AuthServiceImpl 的严格 logout 业务测试。
 *
 * <p>测试只验证 refresh 会话状态、认证版本失效调用和 Cookie 清理，不启动 Spring 上下文。
 */
class AuthServiceImplTest {
  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");
  private static final String RAW_TOKEN = "raw-refresh-token";
  private static final String TOKEN_HASH = "refresh-token-hash";

  private final RefreshTokenCookieService cookieService = mock(RefreshTokenCookieService.class);
  private final TokenService tokenService = mock(TokenService.class);
  private final RefreshTokenMapper refreshTokenMapper = mock(RefreshTokenMapper.class);
  private final AuthSessionService authSessionService = mock(AuthSessionService.class);
  private final PlatformTransactionManager transactionManager = mock(PlatformTransactionManager.class);
  private final HttpServletRequest request = mock(HttpServletRequest.class);
  private final HttpServletResponse response = mock(HttpServletResponse.class);
  private AuthServiceImpl service;

  @BeforeEach
  void setUp() {
    TransactionStatus transactionStatus = mock(TransactionStatus.class);
    when(transactionManager.getTransaction(any())).thenReturn(transactionStatus);
    when(cookieService.read(request)).thenReturn(Optional.of(RAW_TOKEN));
    when(tokenService.hashRefreshToken(RAW_TOKEN)).thenReturn(TOKEN_HASH);

    service = new AuthServiceImpl(mock(UserAuthorizationService.class), null, tokenService, refreshTokenMapper,
      cookieService, mock(JwtProperties.class), Clock.fixed(NOW, ZoneOffset.UTC), transactionManager,
      mock(SysUserMapper.class), mock(SysRoleMapper.class), mock(SysUserRoleMapper.class), authSessionService);
  }

  @Test
  void logout_whenRefreshCookieMissing_throwsNeedLoginAndClearsCookie() {
    when(cookieService.read(request)).thenReturn(Optional.empty());

    BusinessException exception = assertThrows(BusinessException.class, () -> service.logout(request, response));

    assertEquals(ResponseCodeEnum.NEED_LOGIN, exception.getResponseCode());
    verify(cookieService).clear(response);
    verifyNoAuthSessionInvalidation();
  }

  @Test
  void logout_whenRefreshRecordMissing_throwsNeedLoginAndClearsCookie() {
    when(refreshTokenMapper.selectByTokenHashForUpdate(TOKEN_HASH)).thenReturn(null);

    BusinessException exception = assertThrows(BusinessException.class, () -> service.logout(request, response));

    assertEquals(ResponseCodeEnum.NEED_LOGIN, exception.getResponseCode());
    verify(cookieService).clear(response);
    verifyNoAuthSessionInvalidation();
  }

  @Test
  void logout_whenRefreshRecordRevoked_throwsNeedLogin() {
    RefreshTokenEntity token = validToken();
    token.setRevokedAt(NOW.atZone(ZoneOffset.UTC).toLocalDateTime());
    when(refreshTokenMapper.selectByTokenHashForUpdate(TOKEN_HASH)).thenReturn(token);

    BusinessException exception = assertThrows(BusinessException.class, () -> service.logout(request, response));

    assertEquals(ResponseCodeEnum.NEED_LOGIN, exception.getResponseCode());
    verify(cookieService).clear(response);
    verifyNoAuthSessionInvalidation();
  }

  @Test
  void logout_whenRefreshRecordExpired_throwsNeedLogin() {
    RefreshTokenEntity token = validToken();
    token.setExpiresAt(NOW.minusSeconds(1).atZone(ZoneOffset.UTC).toLocalDateTime());
    when(refreshTokenMapper.selectByTokenHashForUpdate(TOKEN_HASH)).thenReturn(token);

    BusinessException exception = assertThrows(BusinessException.class, () -> service.logout(request, response));

    assertEquals(ResponseCodeEnum.NEED_LOGIN, exception.getResponseCode());
    verify(cookieService).clear(response);
    verifyNoAuthSessionInvalidation();
  }

  @Test
  void logout_whenRefreshRecordValid_invalidatesUserAuthenticationAndClearsCookie() {
    RefreshTokenEntity token = validToken();
    when(refreshTokenMapper.selectByTokenHashForUpdate(TOKEN_HASH)).thenReturn(token);

    service.logout(request, response);

    verify(authSessionService).incrementAuthVersionAndRevoke(42L);
    verify(cookieService).clear(response);
  }

  @Test
  void logout_whenSameRefreshRecordIsUsedAgain_secondCallThrowsNeedLogin() {
    RefreshTokenEntity token = validToken();
    when(refreshTokenMapper.selectByTokenHashForUpdate(TOKEN_HASH)).thenReturn(token);

    service.logout(request, response);
    token.setRevokedAt(NOW.atZone(ZoneOffset.UTC).toLocalDateTime());

    BusinessException exception = assertThrows(BusinessException.class, () -> service.logout(request, response));

    assertEquals(ResponseCodeEnum.NEED_LOGIN, exception.getResponseCode());
    verify(authSessionService).incrementAuthVersionAndRevoke(42L);
    verify(cookieService, org.mockito.Mockito.times(2)).clear(response);
  }

  private RefreshTokenEntity validToken() {
    RefreshTokenEntity token = new RefreshTokenEntity();
    token.setId(99L);
    token.setUserId(42L);
    token.setExpiresAt(NOW.plusSeconds(3600).atZone(ZoneOffset.UTC).toLocalDateTime());
    return token;
  }

  private void verifyNoAuthSessionInvalidation() {
    verify(authSessionService, never()).incrementAuthVersionAndRevoke(any());
  }
}
