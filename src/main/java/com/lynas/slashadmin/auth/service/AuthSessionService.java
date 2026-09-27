package com.lynas.slashadmin.auth.service;

import java.util.Collection;

/**
 * 管理认证版本与 refresh token 会话失效
 *
 * <p>用户状态、密码、角色或权限发生变化时，通过本服务统一撤销会话，
 * 避免各业务模块直接操作 auth_refresh_token 表
 */
public interface AuthSessionService {
  /**
   * 撤销指定用户的全部有效 Refresh Token 会话。
   *
   * <p>该操作仅处理 Refresh Token：将当前用户仍然有效的 Refresh Token * 标记为已撤销，
   * 使这些 Token 无法继续用于刷新和获取新的 Access Token。
   *
   * <p>该操作不会主动使已经签发的 Access Token 立即失效。
   * 已签发的 Access Token 在自身过期之前，仍可能继续通过基础 JWT 校验。
   *
   * @param userId 用户 ID
   */
  void revokeAllByUserId(
    Long userId);

  /**
   *
   * 批量撤销多个用户的全部有效 refresh  token 会话
   *
   * @param userIds 用户 ID 集合
   */
  void revokeAllByUserIds(
    Collection<Long> userIds);

  /**
   * 递增指定用户的认证版本，并撤销其全部 refresh token 会话
   *
   * <p>认证版本 {@code authVersion} 用于控制已经签发的 Access Token 是否继续有效。
   * 签发 Access Token 时，将用户当前的 {@code authVersion} 写入 Token；
   * 后续认证时，将 Token 中的版本与用户当前认证版本进行比较。
   *
   * <p>当用户密码、状态、角色或权限等影响认证安全的信息发生变化时，
   * 将 {@code authVersion} 递增（例如 {@code 1 -> 2}）。
   * 此时旧 Access Token 中仍保存旧版本 {@code 1}，与当前版本 {@code 2}
   * 不一致，因此可以判定旧 Access Token 已失效。
   *
   * <p>同时撤销该用户全部有效 Refresh Token，防止旧 Refresh Token
   * 继续获取新的 Access Token。通过“认证版本递增 + Refresh Token 撤销”，
   * 可以同时使该用户已有的 Access Token 和 Refresh Token 失效，
   * 要求用户重新完成认证。
   *
   * @param userId 用户 ID
   */
  void incrementAuthVersionAndRevoke(
    Long userId);

  /**
   * 批量递增用户认证版本，并撤销其全部 refresh token 会话。
   *
   * @param userIds 用户 ID 集合
   */
  void incrementAuthVersionsAndRevoke(
    Collection<Long> userIds);
}
