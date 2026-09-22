package com.lynas.slashadmin.auth.service;

import com.lynas.slashadmin.auth.model.AuthenticatedUser;
import com.lynas.slashadmin.auth.model.UserAuthSnapshot;

import java.util.Optional;

/**
 * 读取当前用户认证与授权数据
 *
 * <p>本服务是用户、角色、菜单数据模型与 Spring Security authority 模型之间的边界
 */
public interface UserAuthorizationService {
  /**
   * 按用户名查询启用且未删除的认证用户及当前授权数据
   *
   * @param username 登录用户名
   * @return Optional 容器：
   * 查询到符合条件的用户时，内部包含 AuthenticatedUser；
   * 用户不存在、已禁用或已删除时，返回 Optional.empty()，
   * 用于明确表示“可能查询不到用户”，避免直接返回 null
   */
  Optional<AuthenticatedUser> findActiveUserByUsername(
    String username);

  /**
   * 按用户 ID 查询启用且未删除的认证用户及当前授权数据
   *
   * @param userId 用户 ID
   * @return 用户不存在、禁用或已删除时返回空
   */
  Optional<AuthenticatedUser> findActiveUserById(
    Long userId);

  /**
   * 查询 JWT 校验所需的最小认证状态快照
   *
   * @param userId JWT subject 中的用户 ID
   * @return 用户不存在时返回空
   */
  Optional<UserAuthSnapshot> findAuthenticationSnapshot(
    Long userId);

}
