package com.lynas.slashadmin.system.role.controller;

import com.lynas.slashadmin.common.response.ApiResponse;
import com.lynas.slashadmin.system.role.dto.SysRoleSaveDto;
import com.lynas.slashadmin.system.role.service.SysRoleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import com.lynas.slashadmin.common.exception.BusinessException;
import com.lynas.slashadmin.system.role.enums.SysRoleEnum;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 角色控制器的请求校验测试。
 */
@WebMvcTest(SysRoleController.class)
class SysRoleControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private SysRoleService roleService;

  @Test
  void addRole_whenRequiredFieldIsMissing_returnsBadRequest() throws Exception {
    mockMvc.perform(post("/system/role/add").contentType(MediaType.APPLICATION_JSON).content("""
      {
        "code": "ADMIN",
        "sort": 1,
        "status": 1,
        "menuIds": []
      }
      """)).andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value(400))
      .andExpect(jsonPath("$.message").value("角色名称不能为空"));
  }

  @Test
  void addRole_whenRequestIsValid_callsService() throws Exception {
    org.mockito.Mockito.when(roleService.addSysRole(any(SysRoleSaveDto.class))).thenReturn(ApiResponse.success());

    mockMvc.perform(post("/system/role/add").contentType(MediaType.APPLICATION_JSON).content("""
      {
        "name": "管理员",
        "code": "ADMIN",
        "sort": 1,
        "status": 1,
        "menuIds": []
      }
      """)).andExpect(status().isOk()).andExpect(jsonPath("$.code").value(200));

    verify(roleService).addSysRole(any(SysRoleSaveDto.class));
  }

  @Test
  void getRoleOptions_returnsServiceResponse() throws Exception {
    org.mockito.Mockito.when(roleService.getSysRoleOptions()).thenReturn(ApiResponse.success(List.of()));

    mockMvc.perform(get("/system/role/options")).andExpect(status().isOk()).andExpect(jsonPath("$.code").value(200));

    verify(roleService).getSysRoleOptions();
  }

  @ParameterizedTest
  @ValueSource(
    strings = {"\"name\":\" \"", "\"code\":\" \"", "\"sort\":-1", "\"sort\":2147483648", "\"status\":2",
      "\"status\":null", "\"menuIds\":[0]", "\"menuIds\":[null]"})
  void addRole_whenFieldIsInvalid_rejectsBeforeService(
    String invalidField) throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode payload = (ObjectNode) mapper.readTree("""
      {"name":"测试","code":"TEST","sort":0,"status":1,"menuIds":[]}
      """);
    payload.setAll((ObjectNode) mapper.readTree("{" + invalidField + "}"));

    mockMvc.perform(post("/system/role/add").contentType(MediaType.APPLICATION_JSON).content(payload.toString()))
      .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value(400));

    verifyNoInteractions(roleService);
  }

  @ParameterizedTest
  @ValueSource(strings = {"pageNum=0", "pageSize=0", "pageSize=1000", "status=2"})
  void getRoleList_whenQueryIsInvalid_returnsBadRequest(
    String query) throws Exception {
    mockMvc.perform(get("/system/role/list?" + query)).andExpect(status().isBadRequest());
    verifyNoInteractions(roleService);
  }

  @Test
  void updateRole_withEmptyMenus_callsUpdateEndpoint() throws Exception {
    when(roleService.updateSysRoleById(eq(1L), any(SysRoleSaveDto.class))).thenReturn(ApiResponse.success());

    mockMvc.perform(put("/system/role/1").contentType(MediaType.APPLICATION_JSON).content("""
      {"name":"测试","code":"TEST","sort":0,"status":1,"menuIds":[]}
      """)).andExpect(status().isOk()).andExpect(jsonPath("$.code").value(200));

    verify(roleService).updateSysRoleById(eq(1L), any(SysRoleSaveDto.class));
  }

  @Test
  void deleteRole_whenAssignedToUser_returnsBusinessConflict() throws Exception {
    when(roleService.deleteSysRoleById(1L)).thenThrow(new BusinessException(SysRoleEnum.ROLE_IN_USE));

    mockMvc.perform(delete("/system/role/1")).andExpect(status().isConflict())
      .andExpect(jsonPath("$.code").value(513));
  }
}
