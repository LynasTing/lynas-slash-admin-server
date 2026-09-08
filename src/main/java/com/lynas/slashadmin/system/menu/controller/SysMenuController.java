package com.lynas.slashadmin.system.menu.controller;

import com.lynas.slashadmin.common.response.ApiResponse;
import com.lynas.slashadmin.system.menu.dto.SysMenuSaveDto;
import com.lynas.slashadmin.system.menu.service.SysMenuService;
import com.lynas.slashadmin.system.menu.vo.SysMenuTreeVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 系统菜单管理接口
 */
@RestController
@RequestMapping("/system/menu")
@Tag(name = "菜单管理", description = "菜单管理 CURD 接口")
public class SysMenuController {
  @Autowired
  private SysMenuService sysMenuService;

  /**
   * 分页获取菜单列表
   */
  @Operation(summary = "查询菜单树")
  @GetMapping("/list")
  public ApiResponse<List<SysMenuTreeVo>> getSysMenuList() {
    return sysMenuService.getSysMenuList();
  }

  /**
   * 新增菜单
   */
  @PostMapping("/add")
  public ApiResponse<Void> addSysMenu(
    @Valid
    @RequestBody SysMenuSaveDto args) {
    return sysMenuService.addSysMenu(args);
  }

  /**
   * 修改菜单
   */
  @PutMapping("/{id}")
  public ApiResponse<Void> putSysMenuById(
    @PathVariable Long id,
    @Valid
    @RequestBody SysMenuSaveDto args) {
    return sysMenuService.updateSysMenuById(id, args);
  }

  /**
   * 删除菜单
   */
  @DeleteMapping("/{id}")
  public ApiResponse<Void> deleteSysMenuById(
    @PathVariable Long id) {
    return sysMenuService.deleteSysMenuById(id);
  }
}
