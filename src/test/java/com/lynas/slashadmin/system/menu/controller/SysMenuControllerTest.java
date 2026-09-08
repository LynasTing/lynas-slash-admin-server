package com.lynas.slashadmin.system.menu.controller;

import com.lynas.slashadmin.common.exception.GlobalExceptionHandler;
import com.lynas.slashadmin.common.response.ApiResponse;
import com.lynas.slashadmin.system.menu.dto.SysMenuSaveDto;
import com.lynas.slashadmin.system.menu.service.SysMenuService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 菜单控制器的请求校验与异常响应测试。
 */
class SysMenuControllerTest {
  private MockMvc mockMvc;

  private MenuServiceStub menuServiceStub;

  @BeforeEach
  void setUp() {
    menuServiceStub = new MenuServiceStub();
    SysMenuController controller = new SysMenuController();
    ReflectionTestUtils.setField(controller, "sysMenuService", createMenuService());

    LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
    validator.afterPropertiesSet();
    mockMvc = MockMvcBuilders.standaloneSetup(controller).setControllerAdvice(new GlobalExceptionHandler())
      .setValidator(validator).build();
  }

  @Test
  void addSysMenu_whenRequiredFieldIsMissing_returnsBadRequest() throws Exception {
    mockMvc.perform(post("/system/menu/add").contentType(MediaType.APPLICATION_JSON).content("""
      {
        "parentId": 0,
        "code": "dashboard",
        "category": 1,
        "sort": 0,
        "status": 1,
        "hidden": 0
      }
      """)).andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value(400))
      .andExpect(jsonPath("$.message").value("菜单名称不能为空"));

    assertFalse(menuServiceStub.called);
  }

  @Test
  void addSysMenu_whenRequestIsValid_callsService() throws Exception {
    mockMvc.perform(post("/system/menu/add").contentType(MediaType.APPLICATION_JSON).content("""
      {
        "parentId": 0,
        "name": "仪表板",
        "code": "dashboard",
        "category": 1,
        "sort": 0,
        "status": 1,
        "hidden": 0,
        "i18nKey": "sys.nav.dashboard"
      }
      """)).andExpect(status().isOk()).andExpect(jsonPath("$.code").value(200));

    assertTrue(menuServiceStub.called);
  }

  @Test
  void addSysMenu_whenDatabaseConstraintConflicts_returnsConflict() throws Exception {
    menuServiceStub.exception = new DataIntegrityViolationException("duplicate key");

    mockMvc.perform(post("/system/menu/add").contentType(MediaType.APPLICATION_JSON).content("""
      {
        "parentId": 0,
        "name": "仪表板",
        "code": "dashboard",
        "category": 1,
        "sort": 0,
        "status": 1,
        "hidden": 0
      }
      """)).andExpect(status().isConflict()).andExpect(jsonPath("$.code").value(409))
      .andExpect(jsonPath("$.message").value("数据约束冲突"));
  }

  /**
   * 创建仅实现新增菜单调用的服务替身；其余方法被调用时立即失败，防止测试误判。
   */
  private SysMenuService createMenuService() {
    return (SysMenuService) Proxy.newProxyInstance(SysMenuService.class.getClassLoader(),
      new Class<?>[]{SysMenuService.class}, menuServiceStub);
  }

  /**
   * 记录服务调用并按测试需要抛出指定异常。
   */
  private static class MenuServiceStub implements InvocationHandler {
    private boolean called;

    private RuntimeException exception;

    @Override
    public Object invoke(
      Object proxy,
      Method method,
      Object[] args) {
      if ("addSysMenu".equals(method.getName()) && args.length == 1 && args[0] instanceof SysMenuSaveDto) {
        called = true;
        if (exception != null) {
          throw exception;
        }
        return ApiResponse.success();
      }
      throw new UnsupportedOperationException("测试替身未实现方法：" + method.getName());
    }
  }
}
