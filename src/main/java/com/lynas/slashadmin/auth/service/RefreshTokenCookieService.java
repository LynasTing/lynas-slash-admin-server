package com.lynas.slashadmin.auth.service;

import java.time.Duration;
import java.util.Optional;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import com.lynas.slashadmin.config.RefreshTokenCookieProperties;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Refresh Token Cookie 读写组件
 *
 * <p>该组件只负责 HTTP Cookie 传输，不负责 token 生成、摘要计算、数据库持久化或业务认证
 */
@Component
public class RefreshTokenCookieService {

  private final RefreshTokenCookieProperties properties;

  public RefreshTokenCookieService(RefreshTokenCookieProperties properties) {
    this.properties = properties;
  }

  /**
   * 将 refresh token 写入响应 Cookie
   *
   * @param response HTTP 响应
   * @param refreshToken refresh token
   */
  public void write(
    HttpServletResponse response,
    String refreshToken) {
    // 1.创建 Cookie 构建器，设置 Cookie 名称和 Refresh Token 原文
    ResponseCookie.ResponseCookieBuilder cookieBuilder = ResponseCookie.from(properties.name(), refreshToken);

    // 2.设置 HttpOnly 属性，控制前端能否读取 Cookie
    cookieBuilder.httpOnly(properties.httpOnly());

    // 3.设置 Secure 属性，控制 Cookie 是否通过 HTTPS 发送
    cookieBuilder.secure(properties.secure());

    // 4.设置 SameSite 属性，控制跨站请求携带 Cookie 的行为
    cookieBuilder.sameSite(properties.sameSite());

    // 5.设置 Cookie 的生效路径
    cookieBuilder.path(properties.path());

    // 6.设置有效期
    cookieBuilder.maxAge(properties.maxAge());

    // 7.构建 Cookie 对象
    ResponseCookie cookie = cookieBuilder.build();

    // 8.转换为响应头字符串
    String cookieHeader = cookie.toString();

    // 9.写入 HTTP 响应头
    response.addHeader(HttpHeaders.SET_COOKIE, cookieHeader);
  }

  /**
   * 从请求 Cookie 中读取 refresh token
   *
   * @param request HTTP 请求
   * @return 找到且非空时返回 token，否则返回空
   */
  public Optional<String> read(
    HttpServletRequest request) {

    // 1.获取请求中的所有 Cookie
    Cookie[] cookies = request.getCookies();

    // 2.如果不存在 Cookie，返回空
    if (cookies == null) {
      return Optional.empty();
    }

    // 3.遍历 Cookies
    for (Cookie cookie : cookies) {
      // 4.判断 Cookie 名称是否匹配
      boolean isTargetCookie = properties.name().equals(cookie.getName());
      // 5.判断 Cookie 的值是否存在
      boolean hasValue = cookie.getValue() != null;
      // 6.判断 Cookie 的值是否不为空白
      boolean isNotBlank = hasValue && !cookie.getValue().isBlank();
      // 7.满足全部条件，返回 Token
      if (isTargetCookie && isNotBlank) {
        return Optional.of(cookie.getValue());
      }
    }

    // 8.没有找到符合条件的 Cookie
    return Optional.empty();
  }

  /**
   * 清除 refresh token Cookie
   *
   * <p>清除时必须使用与原 Cookie 相同的 name、path 和 secure 等有关属性，否则浏览器可能保留原 Cookie
   *
   * @param response HTTP 响应
   */
  public void clear(
    HttpServletResponse response) {
    // 1.创建 Cookie 构建器，将 Cookie 值设置为空字符串
    ResponseCookie.ResponseCookieBuilder cookieBuilder = ResponseCookie.from(properties.name(), "");

    // 2.保持原 Cookie 的 HttpOnly 属性
    cookieBuilder.httpOnly(properties.httpOnly());

    // 3.保持原 Cookie 的 Secure 属性
    cookieBuilder.secure(properties.secure());

    // 4.保持原 Cookie 的 SameSite 属性
    cookieBuilder.sameSite(properties.sameSite());

    // 5.保持原 Cookie 的 Path 属性
    cookieBuilder.path(properties.path());

    // 6.将 Cookie 有效期设置为 0，使浏览器删除 Cookie
    cookieBuilder.maxAge(Duration.ZERO);

    // 7.构建 Cookie 对象
    ResponseCookie cookie = cookieBuilder.build();

    // 8.转换为响应头字符串
    String cookieHeader = cookie.toString();

    // 9.将清除 Cookie 的指令写入 HTTP 响应
    response.addHeader(HttpHeaders.SET_COOKIE, cookieHeader);
  }
}
