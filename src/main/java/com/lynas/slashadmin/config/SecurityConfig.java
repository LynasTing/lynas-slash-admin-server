package com.lynas.slashadmin.config;

import com.lynas.slashadmin.common.security.ApiAccessDeniedHandler;
import com.lynas.slashadmin.common.security.ApiAuthenticationEntryPoint;
import com.lynas.slashadmin.common.security.JwtAuthenticationConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

/**
 * Spring Security 全局安全配置
 *
 * <p> 统一配置无状态 JWT 认证、CSRF、CORS、匿名路径、401 和 403 响应。
 * 认证和授权均由 Spring Security Filter Chain 执行， Controller 不自行解析 JWT
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

  /**
   * 无需登录即可访问的接口路径
   */
  private static final String[] PUBLIC_ENDPOINTS = {"/auth/register", "/auth/login", "/auth/token/refresh",
    "/auth/logout", "/auth/token/csrf", "/actuator/health", "/v3/api-docs/**", "/swagger-ui/**"};

  /**
   * 收到 Authorization 请求头时不解析 Bearer Token 的接口路径。
   *
   * <p>该配置只影响 Token 解析，不改变接口是否允许匿名访问，也不影响 refresh Cookie 的读取。
   */
  private static final String[] TOKEN_IGNORED_ENDPOINTS = {"/auth/register", "/auth/login", "/auth/token/refresh",
    "/auth/logout", "/auth/token/csrf", "/actuator/health", "/v3/api-docs/**", "/swagger-ui/**"};

  /**
   * 将 Token 忽略路径转换为 Spring Security 请求匹配器。
   */
  private static final RequestMatcher TOKEN_IGNORED_ENDPOINTS_MATCHER = new OrRequestMatcher(
    Arrays.stream(TOKEN_IGNORED_ENDPOINTS).map(path -> PathPatternRequestMatcher.withDefaults().matcher(path))
      .toArray(RequestMatcher[]::new));

  /**
   * 不要求 CSRF Token 的写请求。
   *
   * <p>注册和登录不依赖浏览器已有的 Cookie 凭证，因此允许客户端在没有 CSRF Token 时提交。
   * refresh 和 logout 依赖 HttpOnly Cookie，必须继续保留 CSRF 防护。
   */
  private static final RequestMatcher CSRF_IGNORED_REQUEST_MATCHER = new OrRequestMatcher(
    PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, "/auth/register"),
    PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, "/auth/login"));

  /**
   * 创建浏览器跨域配置
   *
   * @param corsProperties CORS 配置属性
   * @return CORS 配置源
   */
  @Bean
  public CorsConfigurationSource corsConfigurationSource(
    CorsProperties corsProperties) {
    CorsConfiguration configuration = new CorsConfiguration();

    configuration.setAllowedOrigins(corsProperties.allowedOrigins());
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(List.of("Accept", "Authorization", "Content-Type", "X-XSRF-TOKEN"));
    configuration.setAllowCredentials(true);
    configuration.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  /**
   * 创建按请求路径决定是否解析 Bearer Token 的解析器。
   *
   * @return 自定义 Bearer Token 解析器
   */
  @Bean
  public BearerTokenResolver bearerTokenResolver() {
    BearerTokenResolver defaultResolver = new DefaultBearerTokenResolver();

    return request -> {
      if (TOKEN_IGNORED_ENDPOINTS_MATCHER.matches(request)) {
        return null;
      }
      return defaultResolver.resolve(request);
    };
  }

  /**
   * 创建无状态 JWT 安全过滤链
   *
   * @param http                       HTTP 安全配置构建器
   * @param authenticationEntryPoint   未认证请求处理器
   * @param accessDeniedHandler        权限不足请求处理器
   * @param jwtAuthenticationConverter JWT authority 转换器
   * @return 配置完成的安全过滤器
   *
   * @throws Exception Spring Security 配置失败时抛出
   */
  @Bean
  public SecurityFilterChain securityFilterChain(
    HttpSecurity http,
    ApiAuthenticationEntryPoint authenticationEntryPoint,
    ApiAccessDeniedHandler accessDeniedHandler,
    JwtAuthenticationConverter jwtAuthenticationConverter,
    CorsConfigurationSource corsConfigurationSource,
    BearerTokenResolver bearerTokenResolver) throws Exception {

    // 1.配置 CSRF 防护
    http.csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
      .ignoringRequestMatchers(CSRF_IGNORED_REQUEST_MATCHER));

    // 2.配置 CORS 跨域
    http.cors(cors -> cors.configurationSource(corsConfigurationSource));

    // 3.配置无状态认证，不使用 Session 保存认证状态
    http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

    // 4.禁用不需要的默认认证功能
    http.requestCache(cache -> cache.disable());
    http.formLogin(form -> form.disable());
    http.httpBasic(basic -> basic.disable());
    http.logout(logout -> logout.disable());

    // 5.配置公开接口和认证要求
    http.authorizeHttpRequests(authorize -> authorize.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
      .requestMatchers(PUBLIC_ENDPOINTS).permitAll().anyRequest().authenticated());

    // 6.配置 JWT 身份认证及权限转换
    http.oauth2ResourceServer(resourceServer -> resourceServer.bearerTokenResolver(bearerTokenResolver)
      .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
      .authenticationEntryPoint(authenticationEntryPoint));

    // 7.配置 401 和 403 异常处理
    http.exceptionHandling(exceptionHandling -> exceptionHandling.authenticationEntryPoint(authenticationEntryPoint)
      .accessDeniedHandler(accessDeniedHandler));

    // 8.构建并返回
    return http.build();
  }
}
