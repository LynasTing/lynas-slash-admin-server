package com.lynas.slashadmin.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lynas.slashadmin.common.enums.ResponseCodeEnum;
import com.lynas.slashadmin.common.response.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 统一处理未认证请求
 *
 * <p>认证失败发生在 Spring Security Filter Chain 中，通常早于 Controller，
 * 因此不能依赖 GlobalExceptionHandler。这里直接返回项目统一的 JSON 响应
 */
@Component
public class ApiAuthenticationEntryPoint implements AuthenticationEntryPoint {
  private final ObjectMapper objectMapper;

  /**
   * 注入 Spring Boot 管理的 JSON 序列化器
   *
   * @param objectMapper JSON 序列化器
   */
  public ApiAuthenticationEntryPoint(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  /**
   * 将未认证请求转换为 HTTP 401 JSON 响应
   *
   * <p>不向客户端暴露签名错误、过期原因或数据库校验细节，
   * 统一使用需要登录的安全提示
   *
   * @param request   当前 HTTP 请求
   * @param response  当前 HTTP 响应
   * @param exception Spring Security 认证失败异常
   * @throws IOException      JSON 写入失败时抛出
   * @throws ServletException Servlet 响应处理失败时抛出
   */
  @Override
  public void commence(
    HttpServletRequest request,
    HttpServletResponse response,
    AuthenticationException exception) throws IOException, ServletException {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    response.setContentType("application/json");

    ApiResponse<Void> body = ApiResponse.error(ResponseCodeEnum.NEED_LOGIN);
    response.getWriter().write(objectMapper.writeValueAsString(body));
  }

}
