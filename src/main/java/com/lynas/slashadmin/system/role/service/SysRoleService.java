package com.lynas.slashadmin.system.role.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.lynas.slashadmin.common.response.ApiResponse;
import com.lynas.slashadmin.common.response.PageResponse;
import com.lynas.slashadmin.system.role.dto.SysRoleSaveDto;
import com.lynas.slashadmin.system.role.entity.SysRoleEntity;
import com.lynas.slashadmin.system.role.entity.SysRolePageQuery;
import com.lynas.slashadmin.system.role.vo.SysRoleDetailVo;
import com.lynas.slashadmin.system.role.vo.SysRoleListVo;
import com.lynas.slashadmin.system.role.vo.SysRoleOptionVo;

import java.util.List;

/**
 * 角色业务接口。
 */
public interface SysRoleService extends IService<SysRoleEntity> {

  ApiResponse<PageResponse<SysRoleListVo>> getSysRoleList(
    SysRolePageQuery query);

  ApiResponse<SysRoleDetailVo> getSysRoleDetail(
    Long id);

  ApiResponse<Void> addSysRole(
    SysRoleSaveDto args);

  ApiResponse<Void> updateSysRoleById(
    Long id,
    SysRoleSaveDto args);

  ApiResponse<Void> deleteSysRoleById(
    Long id);

  ApiResponse<List<SysRoleOptionVo>> getSysRoleOptions();
}
