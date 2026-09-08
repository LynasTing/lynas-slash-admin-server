package com.lynas.slashadmin.system.menu.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
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
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenuEntity> implements SysMenuService {
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
    if (!updateById(menu)) {
      throw new IllegalStateException(ResponseCodeEnum.SYSTEM_ERROR.getMessage());
    }
    return ApiResponse.success();
  }

  /**
   * 删除菜单
   */
  @Override
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
    if (!removeById(id)) {
      throw new BusinessException(ResponseCodeEnum.SYSTEM_ERROR);
    }
    return ApiResponse.success();
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
