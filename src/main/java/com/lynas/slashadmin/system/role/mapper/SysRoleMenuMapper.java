package com.lynas.slashadmin.system.role.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lynas.slashadmin.system.role.entity.SysRoleMenuEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色与菜单关联表的数据访问接口。
 */
@Mapper
public interface SysRoleMenuMapper extends BaseMapper<SysRoleMenuEntity> {
}
