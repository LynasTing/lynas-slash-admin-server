package com.lynas.slashadmin.auth.service.impl;

import com.lynas.slashadmin.auth.mapper.AuthQueryMapper;
import com.lynas.slashadmin.auth.model.AuthMenuNode;
import com.lynas.slashadmin.auth.model.AuthRole;
import com.lynas.slashadmin.auth.model.AuthenticatedUser;
import com.lynas.slashadmin.auth.model.UserAuthSnapshot;
import com.lynas.slashadmin.auth.service.UserAuthorizationService;
import com.lynas.slashadmin.system.menu.entity.SysMenuEntity;
import com.lynas.slashadmin.system.user.entity.SysUserEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 用户认证与授权数据读取服务实现
 *
 * <p>完整认证读取查询用户、角色、authority 和菜单；JWT 校验只查询最小认证快照
 * 避免每个受保护请求都加载完整授权树
 */
@Service
@Transactional(readOnly = true)
public class UserAuthorizationServiceImpl implements UserAuthorizationService {
  private final AuthQueryMapper authQueryMapper;

  public UserAuthorizationServiceImpl(AuthQueryMapper authQueryMapper) {
    this.authQueryMapper = authQueryMapper;
  }

  /**
   * 按用户名读取可认证的用户及其当前授权数据
   *
   * @param username 登录用户名
   * @return 启用且未删除的认证用户；不存在时返回空
   */
  @Override
  public Optional<AuthenticatedUser> findActiveUserByUsername(
    String username) {
    return Optional.ofNullable(authQueryMapper.selectActiveUserByUsername(username)).map(this::buildAuthenticatedUser);
  }

  /**
   * 按用户 ID 读取可认证的用户及其当前授权数据
   *
   * @param userId 用户 ID
   * @return 启用且未删除的认证用户；不存在时返回空
   */
  @Override
  public Optional<AuthenticatedUser> findActiveUserById(
    Long userId) {
    return Optional.ofNullable(authQueryMapper.selectActiveUserById(userId)).map(this::buildAuthenticatedUser);
  }

  /**
   * 查询 JWT 校验所需的最小认证状态
   *
   * @param userId JWT subject 中的用户 ID
   * @return 用户存在时返回认证快照；不存在时返回空
   */
  @Override
  public Optional<UserAuthSnapshot> findAuthenticationSnapshot(
    Long userId) {
    return Optional.ofNullable(authQueryMapper.selectUserAuthSnapshot(userId));
  }

  /**
   * 将活跃用户与当前角色、权限、菜单组合为认证领域模型
   *
   * <p>角色、authority、菜单拆成独立查询，避免角色与菜单连表后形成笛卡尔积
   * 造成重复数据和错误的菜单树
   *
   * @param user 已确认启用且未删除的用户实体
   * @return 包含当前授权数据的认证用户
   */
  private AuthenticatedUser buildAuthenticatedUser(
    SysUserEntity user) {
    List<AuthRole> roles = authQueryMapper.selectActiveRolesByUserId(user.getId());
    List<String> authorities = authQueryMapper.selectAuthoritiesByUserId(user.getId());
    List<SysMenuEntity> menus = authQueryMapper.selectNavigationMenusByUserId(user.getId());

    return new AuthenticatedUser(user.getId(), user.getUsername(), user.getPassword(), user.getNickname(),
      user.getEmail(), user.getPhone(), user.getAvatar(), user.getAuthVersion(), authorities, roles,
      buildMenuTree(menus));
  }

  /**
   * 将已按照 sort、id 排序的扁平菜单列表转换为导航树
   *
   * @param menus 当前用户可见的扁平导航菜单
   * @return 根节点菜单列表
   */
  private List<AuthMenuNode> buildMenuTree(
    List<SysMenuEntity> menus) {
    // 以 parentId 分组后，每次递归只读取当前父节点的直接子节点
    Map<Long, List<SysMenuEntity>> menusByParentId = menus.stream()
      .collect(Collectors.groupingBy(SysMenuEntity::getParentId));

    // 数据库约定 parentId = 0 表示根节点
    return buildMenuChildren(0L, menusByParentId);
  }

  /**
   * 递归构建指定父节点下的所有菜单节点
   *
   * @param parentId        当前父节点 ID
   * @param menusByParentId 按父节点分组的菜单数据
   * @return 当前父节点的直接子节点及其递归 children
   */
  private List<AuthMenuNode> buildMenuChildren(
    Long parentId,
    Map<Long, List<SysMenuEntity>> menusByParentId) {
    // 不存在子节点时返回空列表， 叶子菜单的 children 始终是空数组而非 null
    return menusByParentId.getOrDefault(parentId, List.of()).stream()
      .map(menu -> toAuthMenuNode(menu, buildMenuChildren(menu.getId(), menusByParentId))).toList();
  }

  /**
   * 将数据库菜单实体转换为认证响应菜单节点
   *
   * @param menu     当前菜单实体
   * @param children 已递归构建的子菜单
   * @return 不携带数据库时间字段的导航菜单节点
   */
  private AuthMenuNode toAuthMenuNode(
    SysMenuEntity menu,
    List<AuthMenuNode> children) {
    return new AuthMenuNode(menu.getId(), menu.getParentId(), menu.getName(), menu.getCode(), menu.getCategory(),
      menu.getSort(), menu.getStatus(), menu.getPath(), menu.getComponent(), menu.getIcon(), menu.getI18nKey(),
      menu.getHidden(), menu.getDescription(), menu.getExternalLink(), children);
  }
}
