package com.lynas.slashadmin.common.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lynas.slashadmin.common.enums.ResponseCodeEnum;
import com.lynas.slashadmin.common.response.ApiResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 统一处理权限不足请求
 *
 * <p>JWT 合法只代表用户已认证，不代表用户拥有当前接口所需权限。
 * 这类请求必须返回 403 Forbidden，而不是 401 Unauthorized
 */
@Component
public class ApiAccessDeniedHandler implements AccessDeniedHandler {
  private final ObjectMapper objectMapper;

  /**
   * 注入 Spring Boot 管理的 JSON 序列化器
   *
   * @param objectMapper JSON 序列化器
   */
  public ApiAccessDeniedHandler(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  /**
   * 将权限不足请求转换为 HTTP 403 JSON 响应
   *
   * <p>不向客户端暴露具体缺少的权限信息，避免泄露接口授权细节
   *
   * @param request 当前 HTTP 请求
   * @param response 当前 HTTP 响应
   * @param exception Spring Security 权限拒绝异常
   * @throws IOException JSON 写入失败时抛出
   */
  @Override
  public void handle(
    HttpServletRequest request,
    HttpServletResponse response,
    AccessDeniedException exception) throws IOException {
    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    response.setContentType("application/json");

    ApiResponse<Void> body = ApiResponse.error(ResponseCodeEnum.FORBIDDEN);
    response.getWriter().write(objectMapper.writeValueAsString(body));
  }
}
