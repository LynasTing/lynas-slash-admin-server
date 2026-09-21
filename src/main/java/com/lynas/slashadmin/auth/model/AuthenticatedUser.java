package com.lynas.slashadmin.auth.model;

import java.util.List;

/**
 * 已通过当前状态校验的认证用户
 *
 * <p>该内部模型提供登录、JWT签发和当前用户查询使用。 passwordHash 仅供 BCrypt 比较，
 * 绝不能直接作为接口响应字段
 */
public record AuthenticatedUser(Long userId, String username, String passwordHash, String nickname, String email,
  String phone, String avatar, Integer authVersion, List<String> authorities, List<AuthRole> roles,
  List<AuthMenuNode> menus) {
}
