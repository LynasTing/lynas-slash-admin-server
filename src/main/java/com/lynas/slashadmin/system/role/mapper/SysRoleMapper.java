package com.lynas.slashadmin.system.role.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lynas.slashadmin.system.role.entity.SysRoleEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色表的数据访问接口。
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRoleEntity> {
}
