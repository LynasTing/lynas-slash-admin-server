package com.lynas.slashadmin.auth.mapper;

import com.lynas.slashadmin.auth.model.AuthRole;
import com.lynas.slashadmin.auth.model.UserAuthSnapshot;
import com.lynas.slashadmin.system.menu.entity.SysMenuEntity;
import com.lynas.slashadmin.system.user.entity.SysUserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 认证与授权所需的专用查询
 *
 * <p>查询显式过滤用户、角色和菜单状态，避免登录或 JWT 校验复用管理端 CRUD 查询
 */
@Mapper
public interface AuthQueryMapper {

  /**
   * 按用户名查询启用且未删除的用户。
   *
   * @param username 登录用户名
   * @return 匹配的用户；不存在时返回 null
   */
  @Select("""
    SELECT
      id,
      username,
      nickname,
      password,
      email,
      phone,
      avatar,
      status,
      deleted,
      auth_version,
      created_at,
      updated_at
    FROM sys_user
    WHERE username = #{username}
      AND status = 1
      AND deleted = 0
    """)
  SysUserEntity selectActiveUserByUsername(
    @Param("username") String username);

  /**
   * 按用户 ID 查询启用且未删除的用户。
   *
   * @param userId 用户 ID
   * @return 匹配的用户；不存在时返回 null
   */
  @Select("""
    SELECT
      id,
      username,
      nickname,
      password,
      email,
      phone,
      avatar,
      status,
      deleted,
      auth_version,
      created_at,
      updated_at
    FROM sys_user
    WHERE id = #{userId}
      AND status = 1
      AND deleted = 0
    """)
  SysUserEntity selectActiveUserById(
    @Param("userId") Long userId);

  /**
   * 查询 JWT 校验所需的用户状态和授权版本。
   *
   * @param userId 用户 ID
   * @return 用户授权快照；不存在时返回 null
   */
  @Select("""
    SELECT
      id AS user_id,
      status,
      deleted,
      auth_version
    FROM sys_user
    WHERE id = #{userId}
    """)
  UserAuthSnapshot selectUserAuthSnapshot(
    @Param("userId") Long userId);

  /**
   * 查询用户拥有的启用角色。
   *
   * @param userId 用户 ID
   * @return 按排序字段稳定排列的角色列表
   */
  @Select("""
    SELECT
      r.id,
      r.code,
      r.name
    FROM sys_user_role ur
    INNER JOIN sys_role r ON r.id = ur.role_id
    WHERE ur.user_id = #{userId}
      AND r.status = 1
      AND r.deleted = 0
    ORDER BY r.sort, r.id
    """)
  List<AuthRole> selectActiveRolesByUserId(
    @Param("userId") Long userId);

  /**
   * 查询 JWT claim 所需的角色和操作权限编码。
   *
   * @param userId 用户 ID
   * @return 去重且稳定排序的权限编码列表
   */
  @Select("""
    SELECT authority
    FROM (
      SELECT CONCAT('ROLE_', r.code) AS authority
      FROM sys_user_role ur
      INNER JOIN sys_role r ON r.id = ur.role_id
      WHERE ur.user_id = #{userId}
        AND r.status = 1
        AND r.deleted = 0

      UNION

      SELECT m.code AS authority
      FROM sys_user_role ur
      INNER JOIN sys_role r ON r.id = ur.role_id
      INNER JOIN sys_role_menu rm ON rm.role_id = r.id
      INNER JOIN sys_menu m ON m.id = rm.menu_id
      WHERE ur.user_id = #{userId}
        AND r.status = 1
        AND r.deleted = 0
        AND m.status = 1
        AND m.category = 4
    ) authorities
    ORDER BY authority
    """)
  List<String> selectAuthoritiesByUserId(
    @Param("userId") Long userId);

  /**
   * 查询用户可见的启动导航菜单。
   *
   * @param userId 用户 ID
   * @return 扁平化菜单列表；服务层负责构建 children 树
   */
  @Select("""
    SELECT DISTINCT
      m.id,
      m.parent_id,
      m.name,
      m.code,
      m.category,
      m.sort,
      m.status,
      m.path,
      m.component,
      m.icon,
      m.i18n_key,
      m.hidden,
      m.description,
      m.external_link,
      m.created_at,
      m.updated_at
    FROM sys_user_role ur
    INNER JOIN sys_role r ON r.id = ur.role_id
    INNER JOIN sys_role_menu rm ON rm.role_id = r.id
    INNER JOIN sys_menu m ON m.id = rm.menu_id
    WHERE ur.user_id = #{userId}
      AND r.status = 1
      AND r.deleted = 0
      AND m.status = 1
      AND m.category IN (1, 2, 3)
    ORDER BY m.sort, m.id
    """)
  List<SysMenuEntity> selectNavigationMenusByUserId(
    @Param("userId") Long userId);
}
