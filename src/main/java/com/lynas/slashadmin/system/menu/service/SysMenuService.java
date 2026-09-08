package com.lynas.slashadmin.system.menu.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.lynas.slashadmin.common.response.ApiResponse;
import com.lynas.slashadmin.system.menu.dto.SysMenuSaveDto;
import com.lynas.slashadmin.system.menu.entity.SysMenuEntity;
import com.lynas.slashadmin.system.menu.vo.SysMenuTreeVo;

import java.util.List;

public interface SysMenuService extends IService<SysMenuEntity> {
  ApiResponse<List<SysMenuTreeVo>> getSysMenuList();

  ApiResponse<Void> addSysMenu(
    SysMenuSaveDto args);

  ApiResponse<Void> updateSysMenuById(
    Long id,
    SysMenuSaveDto args);

  ApiResponse<Void> deleteSysMenuById(
    Long id);
}
