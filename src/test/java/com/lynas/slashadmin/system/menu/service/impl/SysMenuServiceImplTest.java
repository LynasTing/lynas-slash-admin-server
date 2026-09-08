package com.lynas.slashadmin.system.menu.service.impl;

import com.lynas.slashadmin.system.menu.entity.SysMenuEntity;
import com.lynas.slashadmin.system.menu.dto.SysMenuSaveDto;
import com.lynas.slashadmin.system.menu.vo.SysMenuTreeVo;
import com.lynas.slashadmin.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

class SysMenuServiceImplTest {
  @Test
  void buildMenuTree_buildsNestedNodesWithParentNamesAndSortOrder() {
    SysMenuEntity root = menu(1L, 0L, "系统管理", 1, 20);
    SysMenuEntity directory = menu(2L, 1L, "权限管理", 2, 10);
    SysMenuEntity page = menu(3L, 2L, "角色管理", 3, 0);
    SysMenuEntity earlierRoot = menu(4L, 0L, "仪表板", 3, 10);

    List<SysMenuTreeVo> tree = SysMenuServiceImpl.buildMenuTree(List.of(root, directory, page, earlierRoot));

    assertEquals(List.of(4L, 1L), tree.stream().map(SysMenuTreeVo::getId).toList());
    assertNull(tree.get(0).getParentName());
    assertEquals("系统管理", tree.get(1).getChildren().getFirst().getParentName());
    assertEquals("角色管理", tree.get(1).getChildren().getFirst().getChildren().getFirst().getName());
    assertEquals("权限管理", tree.get(1).getChildren().getFirst().getChildren().getFirst().getParentName());
    assertEquals(List.of(), tree.get(0).getChildren());
  }

  @Test
  void validateParent_whenParentDoesNotExist_throwsBusinessException() {
    SysMenuServiceImpl service = spy(new SysMenuServiceImpl());
    doReturn(null).when(service).getById(999L);

    assertThrows(BusinessException.class, () -> validateParent(service, 1L, request(999L, 3)));
  }

  @Test
  void validateParent_whenParentIsSelf_throwsBusinessException() {
    SysMenuServiceImpl service = spy(new SysMenuServiceImpl());

    assertThrows(BusinessException.class, () -> validateParent(service, 1L, request(1L, 3)));
  }

  @Test
  void validateParent_whenParentIsDescendant_throwsBusinessException() {
    SysMenuServiceImpl service = spy(new SysMenuServiceImpl());
    SysMenuEntity descendant = menu(3L, 1L, "子菜单", 3, 0);
    doReturn(descendant).when(service).getById(3L);

    assertThrows(BusinessException.class, () -> validateParent(service, 1L, request(3L, 4)));
  }

  @Test
  void validateParent_whenCategoryHierarchyIsInvalid_throwsBusinessException() {
    SysMenuServiceImpl service = spy(new SysMenuServiceImpl());
    SysMenuEntity parent = menu(2L, 0L, "页面", 3, 0);
    doReturn(parent).when(service).getById(2L);

    assertThrows(BusinessException.class, () -> validateParent(service, 1L, request(2L, 3)));
  }

  @Test
  void deleteSysMenuById_whenMenuHasChildren_throwsBusinessException() {
    SysMenuServiceImpl service = spy(new SysMenuServiceImpl());
    doReturn(menu(1L, 0L, "系统管理", 1, 0)).when(service).getById(1L);
    doReturn(true).when(service).hasChildren(1L);

    assertThrows(BusinessException.class, () -> service.deleteSysMenuById(1L));
  }

  private void validateParent(
    SysMenuServiceImpl service,
    Long menuId,
    SysMenuSaveDto request) {
    ReflectionTestUtils.invokeMethod(service, "validateParent", menuId, request);
  }

  private SysMenuSaveDto request(
    Long parentId,
    Integer category) {
    SysMenuSaveDto request = new SysMenuSaveDto();
    request.setParentId(parentId);
    request.setCategory(category);
    return request;
  }

  private SysMenuEntity menu(
    Long id,
    Long parentId,
    String name,
    Integer category,
    Integer sort) {
    SysMenuEntity menu = new SysMenuEntity();
    menu.setId(id);
    menu.setParentId(parentId);
    menu.setName(name);
    menu.setCode("menu:" + id);
    menu.setCategory(category);
    menu.setSort(sort);
    menu.setStatus(1);
    menu.setHidden(0);
    return menu;
  }
}
