package com.lynas.slashadmin.system.role.controller;

import com.lynas.slashadmin.common.response.ApiResponse;
import com.lynas.slashadmin.common.response.PageResponse;
import com.lynas.slashadmin.system.role.dto.SysRoleSaveDto;
import com.lynas.slashadmin.system.role.entity.SysRolePageQuery;
import com.lynas.slashadmin.system.role.service.SysRoleService;
import com.lynas.slashadmin.system.role.vo.SysRoleDetailVo;
import com.lynas.slashadmin.system.role.vo.SysRoleListVo;
import com.lynas.slashadmin.system.role.vo.SysRoleOptionVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 提供角色管理的 HTTP 接口。
 */
@RestController
@RequestMapping("/system/role")
@Tag(name = "角色管理", description = "角色管理 CURD 接口")
public class SysRoleController {
  private final SysRoleService sysRoleService;

  public SysRoleController(SysRoleService sysRoleService) {
    this.sysRoleService = sysRoleService;
  }

  /**
   * 分页获取角色列表。
   */
  @Operation(summary = "分页查询角色列表", description = "按页码和每页数量查询角色数据。")
  @GetMapping("/list")
  public ApiResponse<PageResponse<SysRoleListVo>> getRoleList(
    @Valid
    @ParameterObject
    @ModelAttribute SysRolePageQuery query) {
    return sysRoleService.getSysRoleList(query);
  }

  /**
   * 获取角色详情及已授权菜单。
   */
  @GetMapping("/{id}")
  public ApiResponse<SysRoleDetailVo> getRoleDetail(
    @PathVariable Long id) {
    return sysRoleService.getSysRoleDetail(id);
  }

  /**
   * 新增系统角色
   */
  @PostMapping("/add")
  public ApiResponse<Void> addSysRole(
    @Valid
    @RequestBody SysRoleSaveDto args) {
    return sysRoleService.addSysRole(args);
  }

  /**
   * 修改系统角色
   */
  @PutMapping("/{id}")
  public ApiResponse<Void> updateSysRoleById(
    @PathVariable Long id,
    @Valid
    @RequestBody SysRoleSaveDto args) {
    return sysRoleService.updateSysRoleById(id, args);
  }

  /**
   * 删除系统角色
   */
  @DeleteMapping("/{id}")
  public ApiResponse<Void> deleteSysRoleById(
    @PathVariable Long id) {
    return sysRoleService.deleteSysRoleById(id);
  }

  /**
   * 获取所有系统角色（不分页模式）
   */
  @Operation(summary = "查询角色选项", description = "查询用于下拉选择的全部角色。")
  @GetMapping("/options")
  public ApiResponse<List<SysRoleOptionVo>> getSysRoleOptions() {
    return sysRoleService.getSysRoleOptions();
  }

}
