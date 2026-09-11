package com.lynas.slashadmin.system.user.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.lynas.slashadmin.common.response.ApiResponse;
import com.lynas.slashadmin.common.response.PageResponse;
import com.lynas.slashadmin.system.user.dto.SysUserPageQuery;
import com.lynas.slashadmin.system.user.dto.SysUserSaveDto;
import com.lynas.slashadmin.system.user.entity.SysUserEntity;
import com.lynas.slashadmin.system.user.vo.SysUserListVo;

public interface SysUserService extends IService<SysUserEntity> {
  ApiResponse<PageResponse<SysUserListVo>> getSysUserList(
    SysUserPageQuery query);

  ApiResponse<SysUserListVo> getSysUserDetail(
    Long id);

  ApiResponse<Void> addSysUser(
    SysUserSaveDto args);

  ApiResponse<Void> updateSysUserById(
    Long id,
    SysUserSaveDto args);

  ApiResponse<Void> deleteSysUserById(
    Long id);
}
