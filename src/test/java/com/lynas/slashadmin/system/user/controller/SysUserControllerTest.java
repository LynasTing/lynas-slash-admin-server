package com.lynas.slashadmin.system.user.controller;

import com.lynas.slashadmin.common.exception.GlobalExceptionHandler;
import com.lynas.slashadmin.common.response.ApiResponse;
import com.lynas.slashadmin.system.user.dto.SysUserSaveDto;
import com.lynas.slashadmin.system.user.service.SysUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 用户控制器的请求校验测试。
 */
class SysUserControllerTest {
  private MockMvc mockMvc;

  private SysUserService userService;

  @BeforeEach
  void setUp() {
    userService = mock(SysUserService.class);
    SysUserController controller = new SysUserController(userService);
    LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
    validator.afterPropertiesSet();
    mockMvc = MockMvcBuilders.standaloneSetup(controller).setControllerAdvice(new GlobalExceptionHandler())
      .setValidator(validator).build();
  }

  @Test
  void addUser_whenPasswordIsMissing_returnsBadRequest() throws Exception {
    mockMvc.perform(post("/system/user/add").contentType(MediaType.APPLICATION_JSON).content("""
      {
        "username": "admin",
        "email": "admin@example.com",
        "status": 1,
        "roleIds": [1]
      }
      """)).andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value(400))
      .andExpect(jsonPath("$.message").value("密码不能为空"));
  }

  @Test
  void addUser_whenRequestIsValid_callsService() throws Exception {
    org.mockito.Mockito.when(userService.addSysUser(any(SysUserSaveDto.class))).thenReturn(ApiResponse.success());

    mockMvc.perform(post("/system/user/add").contentType(MediaType.APPLICATION_JSON).content("""
      {
        "username": "admin",
        "password": "Admin@123456",
        "email": "admin@example.com",
        "status": 1,
        "roleIds": [1]
      }
      """)).andExpect(status().isOk()).andExpect(jsonPath("$.code").value(200));

    verify(userService).addSysUser(any(SysUserSaveDto.class));
  }
}
