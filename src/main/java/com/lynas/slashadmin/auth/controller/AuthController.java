package com.lynas.slashadmin.auth.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lynas.slashadmin.auth.dto.LoginRequest;
import com.lynas.slashadmin.auth.dto.RegisterRequest;
import com.lynas.slashadmin.auth.service.AuthService;
import com.lynas.slashadmin.auth.vo.AccessTokenResponse;
import com.lynas.slashadmin.auth.vo.AuthenticatedUserVo;
import com.lynas.slashadmin.auth.vo.CsrfTokenResponse;
import com.lynas.slashadmin.auth.vo.LoginResponse;
import com.lynas.slashadmin.common.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.web.bind.annotation.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

/**
 * 认证相关 HTTP 接口
 *
 * <p>Controller 只负责接收 HTTP 参数、调用认证服务和包装统一响应，
 * 不直接处理密码、JWT、数据库或 Cookie 细节
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "认证、鉴权管理", description = "登录、刷新、退出和 CSRF Token接口")
public class AuthController {
  private final AuthService authService;

  /**
   * 创建认证控制器
   *
   * @param authService 认证业务
   */
  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  /**
   * 注册普通用户
   */
  @Operation(summary = "账号密码注册")
  @PostMapping("/register")
  public ApiResponse<Void> register(
    @Valid
    @RequestBody RegisterRequest request) {
    authService.register(request);
    return ApiResponse.success();
  }

  /**
   * 用户名 + 密码登录
   */
  @Operation(summary = "用户名 + 密码登录")
  @PostMapping("/login")
  public ApiResponse<LoginResponse> login(
    @Valid
    @RequestBody LoginRequest request,
    HttpServletResponse response) {
    return ApiResponse.success(authService.login(request, response));
  }

  /**
   * 使用 refresh token 刷新 access token
   *
   * @param request HTTP 请求，用于读取 refresh token Cookie
   * @param response HTTP 响应，用于写入新的 refresh token Cookie
   * @return 新的 access token
   */
  @Operation(summary = "刷新访问令牌")
  @PostMapping("/token/refresh")
  public ApiResponse<AccessTokenResponse> refresh(
    HttpServletRequest request,
    HttpServletResponse response) {
    return ApiResponse.success(authService.refresh(request, response));
  }

  /**
   * 退出当前登录会话
   *
   * @param request HTTP 请求，用于读取 refresh token Cookie
   * @param response HTTP 响应，用于清除 refresh token Cookie
   * @return 成功
   */
  @Operation(summary = "退出登录")
  @PostMapping("/logout")
  public ApiResponse<Void> logout(
    HttpServletRequest request,
    HttpServletResponse response) {
    authService.logout(request, response);
    return ApiResponse.success();
  }

  /**
   * 获取 CSRF Token
   *
   * @param csrfToken Spring Security 注入的 CSRF Token
   * @return CSRF Token 响应
   */
  @Operation(summary = "获取 CSRF Token")
  @GetMapping("/token/csrf")
  public ApiResponse<CsrfTokenResponse> csrf(
    @Parameter(hidden = true) CsrfToken csrfToken) {
    return ApiResponse.success(authService.createCsrfToken(csrfToken));
  }

  /**
   * 获取当前登录用户信息
   *
   * @param authentication 当前 Spring Security 认证对象
   * @return 用户公开资料
   */
  @Operation(summary = "获取当前登录用户公开信息")
  @GetMapping("/me")
  public ApiResponse<AuthenticatedUserVo> me(
    Authentication authentication) {
    return ApiResponse.success(authService.me(authentication));
  }
}
