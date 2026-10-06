package com.lynas.slashadmin.auth.service;

import com.lynas.slashadmin.auth.dto.LoginRequest;
import com.lynas.slashadmin.auth.dto.RegisterRequest;
import com.lynas.slashadmin.auth.vo.AccessTokenResponse;
import com.lynas.slashadmin.auth.vo.AuthenticatedUserVo;
import com.lynas.slashadmin.auth.vo.CsrfTokenResponse;
import com.lynas.slashadmin.auth.vo.LoginResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;

/**
 * 认证业务服务
 *
 * <p>该接口统一定义登录、刷新访问令牌、退出登录和获取 CSRF Token 的业务入口，
 * Controller 只负责 HTTP 参数接收和响应包装，不直接处理认证细节
 */
public interface AuthService {

  /**
   * 注册普通用户
   *
   * <p>注册用户固定绑定 NORMAL_USER 角色，不允许客户端指定角色
   *
   * @parma request 注册请求参数
   */
  void register(
    RegisterRequest request);

  /**
   * 使用用户名和密码登录
   *
   * <p>成功后返回 access token，并通过 HttpOnly Cookie 写入 refresh token
   *
   * @param request  用户名密码请求
   * @param response HTTP 响应对象，用于写入 refresh token Cookie
   * @return 登录成功响应
   */
  LoginResponse login(
    LoginRequest request,
    HttpServletResponse response);

  /**
   * 使用 refresh token Cookie 刷新 access token
   *
   * <p>成功后会轮换 refresh token，并通过响应 Cookie 写入新的 refresh token
   *
   * @param request  HTTP 请求对象，用于读取 refresh token Cookie
   * @param response HTTP 响应对象，用于写入新的 refresh token Cookie
   * @return 新的 access token 响应
   */
  AccessTokenResponse refresh(
    HttpServletRequest request,
    HttpServletResponse response);

  /**
   * 退出当前 refresh token 会话
   *
   * <p>该操作需要具备幂等性，即使当前没有有效的 refresh token，也需要清理 Cookie
   *
   * @param request  HTTP 请求对象，用于读取 refresh token Cookie
   * @param response HTTP 响应对象，用于清除 refresh token Cookie
   */
  void logout(
    HttpServletRequest request,
    HttpServletResponse response);

  /**
   * 返回当前请求对应的 CSRF Token 信息
   *
   * <p>调用过程中应确保 CookieCsrfTokenRepository 已生成可读的 XSRF-TOKEN Cookie
   *
   * @return CSRF token 响应
   *
   * @parma csrfToken Spring Security 提供的 CSRF Token
   */
  CsrfTokenResponse createCsrfToken(
    CsrfToken csrfToken);

  /**
   * 查询当前已认证用户
   *
   * @param authentication 当前 Spring Security 认证对象
   * @return 当前用户公开资料
   */
  AuthenticatedUserVo me(
    Authentication authentication);
}
