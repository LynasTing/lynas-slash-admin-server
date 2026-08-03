package com.lynas.slashadmin.module.system.user.controller;

import com.lynas.slashadmin.common.api.ApiResponse;
import com.lynas.slashadmin.module.system.user.dto.SysUserResponse;
import com.lynas.slashadmin.module.system.user.service.SysUserService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 用户基础联通接口。它用于验证前端、服务端和数据库链路，不承担正式 CRUD 职责。 */
@RestController
@RequestMapping("/system/user")
public class SysUserController {

  private final SysUserService sysUserService;

  public SysUserController(SysUserService sysUserService) {
    this.sysUserService = sysUserService;
  }

  @GetMapping("/list")
  public ApiResponse<List<SysUserResponse>> listUsers() {
    return ApiResponse.success(sysUserService.listUsers());
  }
}
