package com.lynas.slashadmin.common.security;

import com.lynas.slashadmin.auth.model.UserAuthSnapshot;
import com.lynas.slashadmin.auth.service.UserAuthorizationService;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Access JWT 认证版本校验测试。
 */
class UserAuthenticationValidatorTest {
  private final UserAuthorizationService authorizationService = mock(UserAuthorizationService.class);
  private final UserAuthenticationValidator validator = new UserAuthenticationValidator(authorizationService);

  @Test
  void validate_whenTokenVersionMatchesCurrentUser_succeeds() {
    when(authorizationService.findAuthenticationSnapshot(42L))
      .thenReturn(Optional.of(new UserAuthSnapshot(42L, 1, 0, 3)));

    OAuth2TokenValidatorResult result = validator.validate(jwt(42L, 3));

    assertFalse(result.hasErrors());
  }

  @Test
  void validate_whenLogoutIncrementsVersion_rejectsOldToken() {
    when(authorizationService.findAuthenticationSnapshot(42L))
      .thenReturn(Optional.of(new UserAuthSnapshot(42L, 1, 0, 4)));

    OAuth2TokenValidatorResult result = validator.validate(jwt(42L, 3));

    assertTrue(result.hasErrors());
  }

  @Test
  void validate_whenUserDisabled_rejectsToken() {
    when(authorizationService.findAuthenticationSnapshot(42L))
      .thenReturn(Optional.of(new UserAuthSnapshot(42L, 0, 0, 3)));

    OAuth2TokenValidatorResult result = validator.validate(jwt(42L, 3));

    assertTrue(result.hasErrors());
  }

  private Jwt jwt(
    Long userId,
    Integer authVersion) {
    Instant issuedAt = Instant.parse("2026-01-01T00:00:00Z");
    return Jwt.withTokenValue("test-token").header("alg", "HS256").subject(String.valueOf(userId)).issuedAt(issuedAt)
      .expiresAt(issuedAt.plusSeconds(3600)).claim("av", authVersion).build();
  }
}
