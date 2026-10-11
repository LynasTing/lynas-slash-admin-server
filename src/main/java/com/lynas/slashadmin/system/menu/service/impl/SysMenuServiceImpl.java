package com.lynas.slashadmin.system.menu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.lynas.slashadmin.auth.service.AuthSessionService;
import com.lynas.slashadmin.common.enums.ResponseCodeEnum;
import com.lynas.slashadmin.common.exception.BusinessException;
import com.lynas.slashadmin.common.response.ApiResponse;
import com.lynas.slashadmin.common.utils.BeanCopyUtils;
import com.lynas.slashadmin.system.menu.dto.SysMenuSaveDto;
import com.lynas.slashadmin.system.menu.entity.SysMenuEntity;
import com.lynas.slashadmin.system.menu.enums.SysMenuEnum;
import com.lynas.slashadmin.system.menu.mapper.SysMenuMapper;
import com.lynas.slashadmin.system.menu.service.SysMenuService;
import com.lynas.slashadmin.system.menu.vo.SysMenuTreeVo;
import com.lynas.slashadmin.system.role.entity.SysRoleMenuEntity;
import com.lynas.slashadmin.system.role.mapper.SysRoleMenuMapper;
import com.lynas.slashadmin.system.user.entity.SysUserRoleEntity;
import com.lynas.slashadmin.system.user.mapper.SysUserRoleMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenuEntity> implements SysMenuService {

  /**
   * 认证会话失效服务
   *
   * <p>菜单权限变化时，使受影响用户的旧认证会话失效
   */
  private final AuthSessionService authSessionService;

  /**
   * 角色菜单关联 Mapper
   */
  private final SysRoleMenuMapper sysRoleMenuMapper;

  /**
   * 用户角色关联 Mapper
   */
  private final SysUserRoleMapper sysUserRoleMapper;

  public SysMenuServiceImpl(AuthSessionService authSessionService, SysRoleMenuMapper sysRoleMenuMapper,
    SysUserRoleMapper sysUserRoleMapper) {
    this.authSessionService = authSessionService;
    this.sysRoleMenuMapper = sysRoleMenuMapper;
    this.sysUserRoleMapper = sysUserRoleMapper;
  }

  /**
   * 获取完整菜单树。
   */
  @Override
  public ApiResponse<List<SysMenuTreeVo>> getSysMenuList() {
    List<SysMenuEntity> menus = lambdaQuery().orderByAsc(SysMenuEntity::getSort).orderByAsc(SysMenuEntity::getId)
      .list();
    return ApiResponse.success(buildMenuTree(menus));
  }

  static List<SysMenuTreeVo> buildMenuTree(
    List<SysMenuEntity> menus) {
    Map<Long, String> menuNames = menus.stream()
      .collect(Collectors.toMap(SysMenuEntity::getId, SysMenuEntity::getName));
    Map<Long, SysMenuTreeVo> nodes = new HashMap<>();
    for (SysMenuEntity menu : menus) {
      SysMenuTreeVo node = BeanCopyUtils.beanCopy(menu, SysMenuTreeVo.class);
      node.setParentName(menu.getParentId() == 0 ? null : menuNames.get(menu.getParentId()));
      nodes.put(node.getId(), node);
    }

    List<SysMenuTreeVo> roots = new ArrayList<>();
    for (SysMenuTreeVo node : nodes.values()) {
      if (node.getParentId() == 0) {
        roots.add(node);
        continue;
      }
      SysMenuTreeVo parent = nodes.get(node.getParentId());
      if (parent != null) {
        parent.getChildren().add(node);
      }
    }
    sortTree(roots);
    return roots;
  }

  private static void sortTree(
    List<SysMenuTreeVo> nodes) {
    nodes.sort(Comparator.comparing(SysMenuTreeVo::getSort).thenComparing(SysMenuTreeVo::getId));
    for (SysMenuTreeVo node : nodes) {
      sortTree(node.getChildren());
    }
  }

  /**
   * 新增/修改时校验重复信息
   */
  private void validateUnique(
    Long id,
    SysMenuSaveDto args) {
    boolean nameExists = lambdaQuery().eq(SysMenuEntity::getName, args.getName())
      .ne(id != null, SysMenuEntity::getId, id).exists();
    if (nameExists) {
      throw new BusinessException(SysMenuEnum.NAME_ALREADY_EXISTS);
    }

    boolean codeExists = lambdaQuery().eq(SysMenuEntity::getCode, args.getCode())
      .ne(id != null, SysMenuEntity::getId, id).exists();
    if (codeExists) {
      throw new BusinessException(SysMenuEnum.CODE_ALREADY_EXISTS);
    }
  }

  /**
   * 新增菜单
   */
  @Override
  public ApiResponse<Void> addSysMenu(
    SysMenuSaveDto args) {
    validateUnique(null, args);
    validateParent(null, args);
    validateExistingChildren(null, args.getCategory());
    SysMenuEntity menu = BeanCopyUtils.beanCopy(args, SysMenuEntity.class);
    if (!save(menu)) {
      throw new IllegalStateException(ResponseCodeEnum.SYSTEM_ERROR.getMessage());
    }
    return ApiResponse.success();
  }

  /**
   * 更新菜单
   */
  @Override
  @Transactional(rollbackFor = Exception.class)
  public ApiResponse<Void> updateSysMenuById(
    Long id,
    SysMenuSaveDto args) {
    if (getById(id) == null) {
      throw new BusinessException(SysMenuEnum.ID_NOT_FOUND);
    }
    validateUnique(id, args);
    validateParent(id, args);
    validateExistingChildren(id, args.getCategory());
    SysMenuEntity menu = BeanCopyUtils.beanCopy(args, SysMenuEntity.class);
    menu.setId(id);
    // 修改前记录受影响的用户
    Set<Long> affectedUserIds = findUserIdsByMenuId(id);

    if (!updateById(menu)) {
      throw new IllegalStateException(ResponseCodeEnum.SYSTEM_ERROR.getMessage());
    }
    // 菜单信息或权限编码变化后，使受影响用户的旧会话失效
    authSessionService.incrementAuthVersionsAndRevoke(affectedUserIds);
    return ApiResponse.success();
  }

  /**
   * 删除菜单
   */
  @Override
  @Transactional(rollbackFor = Exception.class)
  public ApiResponse<Void> deleteSysMenuById(
    Long id) {
    if (Objects.isNull(id)) {
      throw new BusinessException(ResponseCodeEnum.ID_IS_NULL);
    }
    if (getById(id) == null) {
      throw new BusinessException(SysMenuEnum.ID_NOT_FOUND);
    }
    if (hasChildren(id)) {
      throw new BusinessException(SysMenuEnum.MENU_HAS_CHILDREN);
    }
    // 删除前记录拥有该菜单的用户
    Set<Long> affectedUserIds = findUserIdsByMenuId(id);
    if (!removeById(id)) {
      throw new BusinessException(ResponseCodeEnum.SYSTEM_ERROR);
    }
    // 菜单删除成功后，使受影响用户的旧会话失效
    authSessionService.incrementAuthVersionsAndRevoke(affectedUserIds);
    return ApiResponse.success();
  }

  /**
   * 查询拥有指定菜单权限的用户 ID
   *
   * <p>查询路径为：菜单 -> 角色 -> 用户
   *
   * @param menuId 菜单 ID
   * @return 受影响的用户 ID 集合
   */
  private Set<Long> findUserIdsByMenuId(
    Long menuId) {
    // 1.查询拥有该菜单的角色 ID
    LambdaQueryWrapper<SysRoleMenuEntity> queryWrapper = new LambdaQueryWrapper<>();
    // 1.1只查询角色 ID 字段
    queryWrapper.select(SysRoleMenuEntity::getRoleId);
    // 1.2设置菜单 ID 查询条件
    queryWrapper.eq(SysRoleMenuEntity::getMenuId, menuId);
    // 1.3查询
    List<SysRoleMenuEntity> roleMenus = sysRoleMenuMapper.selectList(queryWrapper);
    // 1.4去重，但是保留查询结果的元素顺序
    Set<Long> uniqueRoleIds = new LinkedHashSet<>();
    // 1.5遍历拿到角色 ID
    for (SysRoleMenuEntity roleMenu : roleMenus) {
      // 角色 ID
      Long roleId = roleMenu.getRoleId();
      uniqueRoleIds.add(roleId);
    }
    // 1.6转成 List
    List<Long> roleIds = new ArrayList<>(uniqueRoleIds);

    // 2.该角色未分配给任何用户时，无需撤销任何用户的会话
    if (roleIds.isEmpty()) {
      return Set.of();
    }

    // 3.查询拥有这些角色的用户 ID
    LambdaQueryWrapper<SysUserRoleEntity> userRoleQueryWrapper = new LambdaQueryWrapper<>();

    userRoleQueryWrapper.select(SysUserRoleEntity::getUserId);
    userRoleQueryWrapper.in(SysUserRoleEntity::getRoleId, roleIds);
    List<SysUserRoleEntity> userRoles = sysUserRoleMapper.selectList(userRoleQueryWrapper);

    Set<Long> userIds = new LinkedHashSet<>();

    for (SysUserRoleEntity userRole : userRoles) {
      Long userId = userRole.getUserId();
      userIds.add(userId);
    }
    return userIds;
  }

  boolean hasChildren(
    Long menuId) {
    return lambdaQuery().eq(SysMenuEntity::getParentId, menuId).exists();
  }

  private void validateParent(
    Long menuId,
    SysMenuSaveDto args) {
    Long parentId = args.getParentId();
    if (parentId == 0) {
      return;
    }
    if (Objects.equals(menuId, parentId)) {
      throw new BusinessException(SysMenuEnum.PARENT_CANNOT_BE_SELF);
    }
    SysMenuEntity parent = getById(parentId);
    if (parent == null) {
      throw new BusinessException(SysMenuEnum.PARENT_NOT_FOUND);
    }
    if (menuId != null && isDescendant(parentId, menuId)) {
      throw new BusinessException(SysMenuEnum.PARENT_CANNOT_BE_DESCENDANT);
    }
    if (!isChildCategoryAllowed(parent.getCategory(), args.getCategory())) {
      throw new BusinessException(SysMenuEnum.INVALID_CATEGORY_HIERARCHY);
    }
  }

  private boolean isDescendant(
    Long nodeId,
    Long ancestorId) {
    Long currentId = nodeId;
    Set<Long> visited = new java.util.HashSet<>();
    while (currentId != 0 && visited.add(currentId)) {
      if (Objects.equals(currentId, ancestorId)) {
        return true;
      }
      SysMenuEntity current = getById(currentId);
      if (current == null) {
        return false;
      }
      currentId = current.getParentId();
    }
    return false;
  }

  private void validateExistingChildren(
    Long menuId,
    Integer category) {
    if (menuId == null) {
      return;
    }
    List<Integer> childCategories = lambdaQuery().eq(SysMenuEntity::getParentId, menuId).list().stream()
      .map(SysMenuEntity::getCategory).toList();
    if (childCategories.stream().anyMatch(childCategory -> !isChildCategoryAllowed(category, childCategory))) {
      throw new BusinessException(SysMenuEnum.INVALID_CATEGORY_HIERARCHY);
    }
  }

  private boolean isChildCategoryAllowed(
    Integer parentCategory,
    Integer childCategory) {
    return switch (parentCategory) {
      case 1 -> childCategory == 2 || childCategory == 3;
      case 2 -> childCategory == 2 || childCategory == 3;
      case 3 -> childCategory == 4;
      case 4 -> false;
      default -> false;
    };
  }
}
