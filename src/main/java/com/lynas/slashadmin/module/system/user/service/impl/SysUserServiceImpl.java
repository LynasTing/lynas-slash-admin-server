package com.lynas.slashadmin.module.system.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lynas.slashadmin.module.system.user.dto.SysUserResponse;
import com.lynas.slashadmin.module.system.user.entity.SysUser;
import com.lynas.slashadmin.module.system.user.mapper.SysUserMapper;
import com.lynas.slashadmin.module.system.user.service.SysUserService;
import java.util.List;
import org.springframework.stereotype.Service;

/** 用户基础查询实现。查询结果先转换为 DTO，保证密码哈希不会越过服务边界。 */
@Service
public class SysUserServiceImpl implements SysUserService {

  private final SysUserMapper sysUserMapper;

  public SysUserServiceImpl(SysUserMapper sysUserMapper) {
    this.sysUserMapper = sysUserMapper;
  }

  @Override
  public List<SysUserResponse> listUsers() {
    return sysUserMapper
        .selectList(new LambdaQueryWrapper<SysUser>().orderByAsc(SysUser::getId))
        .stream()
        .map(
            user ->
                new SysUserResponse(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getPhone(),
                    user.getAvatar(),
                    user.getStatus(),
                    user.getCreatedAt(),
                    user.getUpdatedAt()))
        .toList();
  }
}
