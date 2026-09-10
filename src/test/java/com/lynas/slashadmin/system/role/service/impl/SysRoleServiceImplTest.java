package com.lynas.slashadmin.system.role.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lynas.slashadmin.common.exception.BusinessException;
import com.lynas.slashadmin.system.menu.entity.SysMenuEntity;
import com.lynas.slashadmin.system.menu.service.SysMenuService;
import com.lynas.slashadmin.system.role.dto.SysRoleSaveDto;
import com.lynas.slashadmin.system.role.entity.SysRoleEntity;
import com.lynas.slashadmin.system.role.entity.SysRoleMenuEntity;
import com.lynas.slashadmin.system.role.entity.SysRolePageQuery;
import com.lynas.slashadmin.system.role.enums.SysRoleEnum;
import com.lynas.slashadmin.system.role.mapper.SysRoleMapper;
import com.lynas.slashadmin.system.role.mapper.SysRoleMenuMapper;
import com.lynas.slashadmin.system.role.service.SysRoleService;
import com.lynas.slashadmin.system.user.entity.SysUserRoleEntity;
import com.lynas.slashadmin.system.user.mapper.SysUserRoleMapper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 验证角色授权写入、删除保护及事务失败边界，不依赖本机数据库。
 */
class SysRoleServiceImplTest {
  private SysRoleServiceImpl service;

  private SysRoleMapper roleMapper;

  private SysRoleMenuMapper roleMenuMapper;

  private SysUserRoleMapper userRoleMapper;

  private SysMenuService menuService;

  @BeforeEach
  void setUp() {
    MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "role-tests");
    TableInfoHelper.initTableInfo(assistant, SysRoleEntity.class);
    TableInfoHelper.initTableInfo(assistant, SysRoleMenuEntity.class);
    TableInfoHelper.initTableInfo(assistant, SysUserRoleEntity.class);
    TableInfoHelper.initTableInfo(assistant, SysMenuEntity.class);
    roleMapper = mock(SysRoleMapper.class);
    roleMenuMapper = mock(SysRoleMenuMapper.class);
    userRoleMapper = mock(SysUserRoleMapper.class);
    menuService = mock(SysMenuService.class);
    service = spy(new SysRoleServiceImpl(menuService, roleMapper, roleMenuMapper, userRoleMapper));
  }

  @Test
  void getRoleDetail_whenIdIsNotPositive_throwsBusinessException() {
    assertThrows(BusinessException.class, () -> service.getSysRoleDetail(0L));
    verifyNoInteractions(roleMapper, roleMenuMapper);
  }

  @Test
  void getRoleDetail_returnsRoleAndMenuIds() {
    SysRoleEntity role = role();
    doReturn(role).when(service).getOne(any(Wrapper.class));
    when(roleMenuMapper.selectList(any(Wrapper.class))).thenReturn(List.of(new SysRoleMenuEntity(1L, 10L)));

    var detail = service.getSysRoleDetail(1L).getData();

    assertEquals("测试角色", detail.getName());
    assertEquals(List.of(10L), detail.getMenuIds());
  }

  @Test
  void getRoleDetail_whenMissing_throwsNotFound() {
    doReturn(null).when(service).getOne(any(Wrapper.class));

    BusinessException error = assertThrows(BusinessException.class, () -> service.getSysRoleDetail(1L));

    assertEquals(SysRoleEnum.ROLE_NOT_EXIST, error.getResponseCode());
    verifyNoInteractions(roleMenuMapper);
  }

  @Test
  void getRoleList_preservesPaginationAndTotal() {
    doAnswer(invocation -> {
      Page<SysRoleEntity> page = invocation.getArgument(0);
      assertEquals(2, page.getCurrent());
      assertEquals(10, page.getSize());
      Wrapper<SysRoleEntity> query = invocation.getArgument(1);
      assertEquals("ORDER BY sort ASC,id ASC", query.getSqlSegment().trim());
      page.setRecords(List.of(role()));
      page.setTotal(11);
      return page;
    }).when(service).page(any(Page.class), any(Wrapper.class));
    SysRolePageQuery query = new SysRolePageQuery();
    query.setPageNum(2);
    query.setPageSize(10);

    var result = service.getSysRoleList(query).getData();

    assertEquals(11, result.getTotal());
    assertEquals(1L, result.getRecords().getFirst().getId());
  }

  @Test
  void addRole_whenNameExists_rejectsBeforeWriting() {
    when(roleMapper.exists(any(Wrapper.class))).thenReturn(true);

    BusinessException error = assertThrows(BusinessException.class, () -> service.addSysRole(request(List.of())));

    assertEquals(SysRoleEnum.NAME_ALREADY_EXISTS, error.getResponseCode());
    verify(service, never()).save(any(SysRoleEntity.class));
  }

  @Test
  void addRole_whenCodeExists_rejectsBeforeWriting() {
    when(roleMapper.exists(any(Wrapper.class))).thenReturn(false, true);

    BusinessException error = assertThrows(BusinessException.class, () -> service.addSysRole(request(List.of())));

    assertEquals(SysRoleEnum.CODE_ALREADY_EXISTS, error.getResponseCode());
    verify(service, never()).save(any(SysRoleEntity.class));
  }

  @Test
  void addRole_deduplicatesMenusAndUsesGeneratedRoleId() {
    when(menuService.count(any(Wrapper.class))).thenReturn(1L);
    doAnswer(invocation -> {
      SysRoleEntity saved = invocation.getArgument(0);
      saved.setId(7L);
      return true;
    }).when(service).save(any(SysRoleEntity.class));
    when(roleMenuMapper.insert(any(SysRoleMenuEntity.class))).thenAnswer(invocation -> {
      SysRoleMenuEntity relation = invocation.getArgument(0);
      assertEquals(7L, relation.getRoleId());
      assertEquals(10L, relation.getMenuId());
      return 1;
    });

    service.addSysRole(request(List.of(10L, 10L)));

    verify(roleMenuMapper).insert(any(SysRoleMenuEntity.class));
  }

  @Test
  void addRole_whenMenuMissing_rejectsBeforeWriting() {
    when(menuService.count(any(Wrapper.class))).thenReturn(0L);

    assertThrows(BusinessException.class, () -> service.addSysRole(request(List.of(10L))));

    verify(service, never()).save(any(SysRoleEntity.class));
    verifyNoInteractions(roleMenuMapper);
  }

  @Test
  void updateRole_withEmptyMenus_clearsAuthorization() {
    when(roleMapper.exists(any(Wrapper.class))).thenReturn(true, false, false);
    doReturn(true).when(service).updateById(any(SysRoleEntity.class));

    service.updateSysRoleById(1L, request(List.of()));

    verify(roleMenuMapper).delete(any(Wrapper.class));
    verify(roleMenuMapper, never()).insert(any(SysRoleMenuEntity.class));
  }

  @Test
  void updateRole_whenMenuMissing_preservesExistingAuthorization() {
    when(roleMapper.exists(any(Wrapper.class))).thenReturn(true, false, false);
    when(menuService.count(any(Wrapper.class))).thenReturn(0L);

    assertThrows(BusinessException.class, () -> service.updateSysRoleById(1L, request(List.of(10L))));

    verify(service, never()).updateById(any(SysRoleEntity.class));
    verifyNoInteractions(roleMenuMapper);
  }

  @Test
  void updateRole_whenMenuInsertFails_rollsBackTransaction() {
    when(roleMapper.exists(any(Wrapper.class))).thenReturn(true, false, false);
    when(menuService.count(any(Wrapper.class))).thenReturn(1L);
    doReturn(true).when(service).updateById(any(SysRoleEntity.class));
    when(roleMenuMapper.insert(any(SysRoleMenuEntity.class))).thenReturn(0);
    PlatformTransactionManager transactions = mock(PlatformTransactionManager.class);
    SimpleTransactionStatus transaction = new SimpleTransactionStatus();
    when(transactions.getTransaction(any())).thenReturn(transaction);
    ProxyFactory factory = new ProxyFactory(service);
    factory.addAdvice(new TransactionInterceptor(transactions, new AnnotationTransactionAttributeSource()));
    SysRoleService proxy = (SysRoleService) factory.getProxy();

    assertThrows(BusinessException.class, () -> proxy.updateSysRoleById(1L, request(List.of(10L))));

    verify(transactions).rollback(transaction);
    verify(transactions, never()).commit(any());
  }

  @Test
  void deleteRole_whenIdIsNull_throwsBusinessException() {
    assertThrows(BusinessException.class, () -> service.deleteSysRoleById(null));
    verifyNoInteractions(roleMapper, userRoleMapper, roleMenuMapper);
  }

  @Test
  void deleteRole_whenMissing_doesNotDeleteRelations() {
    when(roleMapper.exists(any(Wrapper.class))).thenReturn(false);

    assertThrows(BusinessException.class, () -> service.deleteSysRoleById(1L));

    verifyNoInteractions(userRoleMapper, roleMenuMapper);
  }

  @Test
  void deleteRole_whenAssignedToUser_rejectsWithoutDeleting() {
    when(roleMapper.exists(any(Wrapper.class))).thenReturn(true);
    when(userRoleMapper.exists(any(Wrapper.class))).thenReturn(true);

    BusinessException error = assertThrows(BusinessException.class, () -> service.deleteSysRoleById(1L));

    assertEquals(SysRoleEnum.ROLE_IN_USE, error.getResponseCode());
    verifyNoInteractions(roleMenuMapper);
    verify(service, never()).removeById(1L);
  }

  @Test
  void deleteRole_whenUnassigned_deletesMenusAndRole() {
    when(roleMapper.exists(any(Wrapper.class))).thenReturn(true);
    doReturn(true).when(service).removeById(1L);

    service.deleteSysRoleById(1L);

    verify(roleMenuMapper).delete(any(Wrapper.class));
    verify(service).removeById(1L);
  }

  @Test
  void getRoleOptions_selectsDisplayFieldsWithStableOrdering() {
    when(roleMapper.selectList(any(Wrapper.class))).thenAnswer(invocation -> {
      Wrapper<SysRoleEntity> query = invocation.getArgument(0);
      assertEquals("id,name,code,status,sort", query.getSqlSelect());
      assertEquals("ORDER BY sort ASC,id ASC", query.getSqlSegment().trim());
      return List.of(role());
    });

    var options = service.getSysRoleOptions().getData();

    assertEquals(1L, options.getFirst().getId());
    assertEquals("TEST", options.getFirst().getCode());
  }

  private SysRoleSaveDto request(
    List<Long> menuIds) {
    SysRoleSaveDto request = new SysRoleSaveDto();
    request.setName("测试角色");
    request.setCode("TEST");
    request.setSort(1);
    request.setStatus(1);
    request.setMenuIds(menuIds);
    return request;
  }

  private SysRoleEntity role() {
    SysRoleEntity role = new SysRoleEntity();
    role.setId(1L);
    role.setName("测试角色");
    role.setCode("TEST");
    role.setSort(1);
    role.setStatus(1);
    return role;
  }
}
