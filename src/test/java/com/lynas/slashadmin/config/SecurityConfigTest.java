package com.lynas.slashadmin.config;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Constructor;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * SecurityConfig 的路径策略测试。
 *
 * <p>这些测试只验证安全配置的决策，不启动完整应用，也不连接数据库。
 */
class SecurityConfigTest {

  @Test
  void bearerTokenResolver_ignoresInvalidAuthorizationOnPublicEndpoints() {
    BearerTokenResolver resolver = new SecurityConfig().bearerTokenResolver();
    MockHttpServletRequest request = request("POST", "/auth/register", "Bearer Token");

    assertNull(resolver.resolve(request));
  }

  @Test
  void bearerTokenResolver_keepsAuthorizationOnProtectedEndpoints() {
    BearerTokenResolver resolver = new SecurityConfig().bearerTokenResolver();
    MockHttpServletRequest request = request("GET", "/system/user/list", "Bearer valid-token");

    assertEquals("valid-token", resolver.resolve(request));
  }

  @Test
  void csrfMatcher_ignoresOnlyRegisterAndLoginPostRequests() {
    RequestMatcher matcher = csrfMatcher();

    assertTrue(matcher.matches(request("POST", "/auth/register", null)));
    assertTrue(matcher.matches(request("POST", "/auth/login", null)));
  }

  @Test
  void csrfMatcher_keepsProtectionForCookieBasedEndpointsAndOtherMethods() {
    RequestMatcher matcher = csrfMatcher();

    assertFalse(matcher.matches(request("POST", "/auth/token/refresh", null)));
    assertFalse(matcher.matches(request("POST", "/auth/logout", null)));
    assertFalse(matcher.matches(request("GET", "/auth/register", null)));
  }

  @Test
  void spaCsrfHandler_resolvesRawTokenFromRequestHeader() throws ReflectiveOperationException {
    CsrfTokenRequestHandler handler = spaCsrfHandler();
    MockHttpServletRequest request = request("POST", "/auth/logout", null);
    CsrfToken csrfToken = new DefaultCsrfToken("X-XSRF-TOKEN", "_csrf", "raw-token");
    request.addHeader(csrfToken.getHeaderName(), csrfToken.getToken());

    assertEquals("raw-token", handler.resolveCsrfTokenValue(request, csrfToken));
  }

  @Test
  void spaCsrfHandler_withoutHeader_keepsXorParameterCompatibility() throws ReflectiveOperationException {
    CsrfTokenRequestHandler handler = spaCsrfHandler();
    MockHttpServletRequest request = request("POST", "/auth/logout", null);
    CsrfToken csrfToken = new DefaultCsrfToken("X-XSRF-TOKEN", "_csrf", "raw-token");
    request.setParameter(csrfToken.getParameterName(), csrfToken.getToken());

    assertNull(handler.resolveCsrfTokenValue(request, csrfToken));
  }

  @Test
  void spaCsrfHandler_handleLoadsDeferredTokenForCookieRepository() throws ReflectiveOperationException {
    CsrfTokenRequestHandler handler = spaCsrfHandler();
    AtomicBoolean tokenLoaded = new AtomicBoolean();
    CsrfToken csrfToken = new DefaultCsrfToken("X-XSRF-TOKEN", "_csrf", "raw-token");

    handler.handle(new MockHttpServletRequest(), new MockHttpServletResponse(), () -> {
      tokenLoaded.set(true);
      return csrfToken;
    });

    assertTrue(tokenLoaded.get());
  }

  private RequestMatcher csrfMatcher() {
    return (RequestMatcher) ReflectionTestUtils.getField(SecurityConfig.class, "CSRF_IGNORED_REQUEST_MATCHER");
  }

  private CsrfTokenRequestHandler spaCsrfHandler() throws ReflectiveOperationException {
    Class<?> handlerType = Class.forName(SecurityConfig.class.getName() + "$SpaCsrfTokenRequestHandler");
    Constructor<?> constructor = handlerType.getDeclaredConstructor();
    constructor.setAccessible(true);
    return (CsrfTokenRequestHandler) constructor.newInstance();
  }

  private MockHttpServletRequest request(
    String method,
    String path,
    String authorization) {
    MockHttpServletRequest request = new MockHttpServletRequest(method, path);
    if (authorization != null) {
      request.addHeader(HttpHeaders.AUTHORIZATION, authorization);
    }
    return request;
  }
}
