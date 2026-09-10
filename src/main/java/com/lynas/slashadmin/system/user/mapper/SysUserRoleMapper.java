package com.lynas.slashadmin.system.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lynas.slashadmin.system.user.entity.SysUserRoleEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户与角色关联表数据访问接口。
 */
@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRoleEntity> {
}
