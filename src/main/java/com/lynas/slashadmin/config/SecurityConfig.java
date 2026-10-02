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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

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
    CorsConfigurationSource corsConfigurationSource) throws Exception {

    // 1.配置 CSRF 防护
    http.csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()));

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
      .requestMatchers("/auth/register", "/auth/login", "/auth/token/refresh", "/auth/logout", "/auth/token/csrf")
      .permitAll().requestMatchers("/actuator/health", "/v3/api-docs/**", "/swagger-ui/**").permitAll().anyRequest()
      .authenticated());

    // 6.配置 JWT 身份认证及权限转换
    http.oauth2ResourceServer(
      resourceServer -> resourceServer.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
        .authenticationEntryPoint(authenticationEntryPoint));

    // 7.配置 401 和 403 异常处理
    http.exceptionHandling(exceptionHandling -> exceptionHandling.authenticationEntryPoint(authenticationEntryPoint)
      .accessDeniedHandler(accessDeniedHandler));

    // 8.构建并返回
    return http.build();
  }
}
