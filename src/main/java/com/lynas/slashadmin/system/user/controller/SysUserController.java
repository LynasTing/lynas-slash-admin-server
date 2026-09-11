package com.lynas.slashadmin.system.user.controller;

import com.lynas.slashadmin.common.response.ApiResponse;
import com.lynas.slashadmin.common.response.PageResponse;
import com.lynas.slashadmin.system.user.dto.SysUserPageQuery;
import com.lynas.slashadmin.system.user.dto.SysUserSaveDto;
import com.lynas.slashadmin.system.user.service.SysUserService;
import com.lynas.slashadmin.system.user.vo.SysUserListVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统用户管理接口
 */
@RestController
@RequestMapping("/system/user")
@Tag(name = "用户管理", description = "用户管理 CURD 接口")
public class SysUserController {
  private final SysUserService sysUserService;

  public SysUserController(SysUserService sysUserService) {
    this.sysUserService = sysUserService;
  }

  /**
   * 分页获取用户列表
   */
  @Operation(summary = "分页查询用户列表")
  @GetMapping("/list")
  public ApiResponse<PageResponse<SysUserListVo>> getSysUserList(
    @Valid
    @ParameterObject
    @ModelAttribute SysUserPageQuery query) {
    return sysUserService.getSysUserList(query);
  }

  /**
   * 获取用户详情。
   */
  @Operation(summary = "查询用户详情")
  @GetMapping("/{id}")
  public ApiResponse<SysUserListVo> getSysUserDetail(
    @PathVariable Long id) {
    return sysUserService.getSysUserDetail(id);
  }

  /**
   * 新增系统用户
   */
  @PostMapping("/add")
  public ApiResponse<Void> addSysUser(
    @Validated({jakarta.validation.groups.Default.class, SysUserSaveDto.Create.class})
    @RequestBody SysUserSaveDto args) {
    return sysUserService.addSysUser(args);
  }

  /**
   * 更新系统用户
   */
  @PutMapping("/{id}")
  public ApiResponse<Void> putSysUserById(
    @PathVariable Long id,
    @Valid
    @RequestBody SysUserSaveDto args) {
    return sysUserService.updateSysUserById(id, args);
  }

  /**
   * 删除用户
   */
  @DeleteMapping("/{id}")
  public ApiResponse<Void> deleteSysUserById(
    @PathVariable Long id) {
    return sysUserService.deleteSysUserById(id);
  }
}
