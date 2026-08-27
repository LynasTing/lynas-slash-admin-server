package com.lynas.slashadmin.system.menu.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.lynas.slashadmin.common.response.ApiResponse;
import com.lynas.slashadmin.common.response.PageResponse;
import com.lynas.slashadmin.system.menu.dto.SysMenuPageQuery;
import com.lynas.slashadmin.system.menu.dto.SysMenuSaveDto;
import com.lynas.slashadmin.system.menu.entity.SysMenuEntity;
import com.lynas.slashadmin.system.menu.vo.SysMenuListVo;

public interface SysMenuService extends IService<SysMenuEntity> {
  ApiResponse<PageResponse<SysMenuListVo>> getSysMenuList(
    SysMenuPageQuery query);

  ApiResponse<Void> addSysMenu(
    SysMenuSaveDto args);

  ApiResponse<Void> updateSysMenuById(
    Long id,
    SysMenuSaveDto args);

  ApiResponse<Void> deleteSysMenuById(
    Long id);
}
