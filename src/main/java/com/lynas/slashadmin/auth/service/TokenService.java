package com.lynas.slashadmin.auth.service;

import com.lynas.slashadmin.auth.model.AuthenticatedUser;

/**
 * Access Token 与 Refresh Token 的安全值处理服务
 *
 * <p>该接口是认证模块唯一允许签发 Access JWT、生成 Refresh Token
 * 以及计算 Refresh Token 摘要的位置。登录、刷新等业务流程只能调用本接口，
 * 不得自行拼接 JWT 或生成随机 Token
 */
public interface TokenService {

  /**
   * 为已认证用户签发 Access JWT
   *
   * <p>实现类负责从 {@link com.lynas.slashadmin.auth.model.AuthenticatedUser} 写入用户 ID、认证版本和权限，
   * 并使用统一的时钟生成签发时间与过期时间
   *
   * @param user 已完成状态校验并已加载授权数据的认证用户
   * @return 已签名的 Access JWT 字符串
   */
  String createAccessToken(
    AuthenticatedUser user);

  /**
   * 生成一个新的 Refresh Token 及其数据库存储摘要
   *
   * <p>rawValue 只能写入 HttpOnly Cookie，sha256Hash 只能写入数据库。
   * 两者都不得记录到日志、异常消息或 HTTP 响应 body
   *
   * @return 新生成的 Refresh Token 原文及其 SHA-256 摘要
   */
  GeneratedRefreshToken createRefreshToken();

  /**
   * 计算 Refresh Token 原文的 SHA-256 十六进制摘要
   *
   * <p>刷新请求读取 Cookie 原文后，先调用此方法得到摘要，
   * 再使用摘要查询 {@code auth_refresh_token}, 数据库不保存原始 Token
   *
   * @param rawToken Refresh Token 原文
   * @return 固定长度的 SHA-256 十六进制摘要
   */
  String hashRefreshToken(
    String rawToken);

  /**
   * 新生成 Refresh Token 的原文与数据库摘要
   *
   * <p>将其定义为 {@code TokenService} 的嵌套 record, 避免为了一个简单返回值
   * 额外创建文件，同时仍允许认证服务和实现类通过 {@code TokenService.GeneratedRefreshToken} 使用它
   *
   * @param rawValue   Refresh Token 原文，仅用于写入 HttpOnly Cookie
   * @param sha256Hash Token 的 SHA-256 摘要，仅用于数据库查询和存储
   */
  record GeneratedRefreshToken(String rawValue, String sha256Hash) {
  }
}
