package com.lynas.slashadmin.system.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.lynas.slashadmin.common.enums.ResponseCodeEnum;
import com.lynas.slashadmin.common.exception.BusinessException;
import com.lynas.slashadmin.common.response.ApiResponse;
import com.lynas.slashadmin.common.response.PageResponse;
import com.lynas.slashadmin.common.utils.BeanCopyUtils;
import com.lynas.slashadmin.system.role.entity.SysRoleEntity;
import com.lynas.slashadmin.system.role.mapper.SysRoleMapper;
import com.lynas.slashadmin.system.user.dto.SysUserPageQuery;
import com.lynas.slashadmin.system.user.dto.SysUserSaveDto;
import com.lynas.slashadmin.system.user.entity.SysUserEntity;
import com.lynas.slashadmin.system.user.entity.SysUserRoleEntity;
import com.lynas.slashadmin.system.user.enums.SysUserEnums;
import com.lynas.slashadmin.system.user.mapper.SysUserMapper;
import com.lynas.slashadmin.system.user.mapper.SysUserRoleMapper;
import com.lynas.slashadmin.system.user.service.SysUserService;
import com.lynas.slashadmin.system.user.vo.SysUserListVo;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUserEntity> implements SysUserService {
  private final SysRoleMapper sysRoleMapper;

  private final SysUserRoleMapper sysUserRoleMapper;

  private final PasswordEncoder passwordEncoder;

  public SysUserServiceImpl(SysRoleMapper sysRoleMapper, SysUserRoleMapper sysUserRoleMapper,
    PasswordEncoder passwordEncoder) {
    this.sysRoleMapper = sysRoleMapper;
    this.sysUserRoleMapper = sysUserRoleMapper;
    this.passwordEncoder = passwordEncoder;
  }

  /**
   * 分页获取用户列表
   */
  @Override
  public ApiResponse<PageResponse<SysUserListVo>> getSysUserList(
    SysUserPageQuery query) {
    LambdaQueryWrapper<SysUserEntity> wrapper = new LambdaQueryWrapper<>();
    wrapper
      .select(SysUserEntity::getId, SysUserEntity::getUsername, SysUserEntity::getNickname, SysUserEntity::getEmail,
        SysUserEntity::getPhone, SysUserEntity::getAvatar, SysUserEntity::getStatus, SysUserEntity::getCreatedAt)
      .orderByDesc(SysUserEntity::getCreatedAt).orderByDesc(SysUserEntity::getId);
    Page<SysUserEntity> page = new Page<>(query.getPageNum(), query.getPageSize());
    page(page, wrapper);
    List<SysUserListVo> sysUserListVos = BeanCopyUtils.beanListCopy(page.getRecords(), SysUserListVo.class);
    Map<Long, List<Long>> roleIdsByUserId = getRoleIdsByUserId(
      page.getRecords().stream().map(SysUserEntity::getId).toList());
    for (SysUserListVo user : sysUserListVos) {
      user.setRoleIds(roleIdsByUserId.getOrDefault(user.getId(), List.of()));
    }
    return ApiResponse.success(new PageResponse<>(sysUserListVos, page.getTotal()));
  }

  /**
   * 获取单个用户的可安全展示字段及角色关联。
   */
  @Override
  @Transactional(readOnly = true)
  public ApiResponse<SysUserListVo> getSysUserDetail(
    Long id) {
    validateUserId(id);
    SysUserEntity user = getOne(new LambdaQueryWrapper<SysUserEntity>()
      .select(SysUserEntity::getId, SysUserEntity::getUsername, SysUserEntity::getNickname, SysUserEntity::getEmail,
        SysUserEntity::getPhone, SysUserEntity::getAvatar, SysUserEntity::getStatus, SysUserEntity::getCreatedAt)
      .eq(SysUserEntity::getId, id));
    if (Objects.isNull(user)) {
      throw new BusinessException(SysUserEnums.USER_NOT_EXIST);
    }
    SysUserListVo detail = BeanCopyUtils.beanCopy(user, SysUserListVo.class);
    detail.setRoleIds(getRoleIdsByUserId(List.of(id)).getOrDefault(id, List.of()));
    return ApiResponse.success(detail);
  }

  private Map<Long, List<Long>> getRoleIdsByUserId(
    List<Long> userIds) {
    if (userIds.isEmpty()) {
      return Map.of();
    }
    return sysUserRoleMapper
      .selectList(new LambdaQueryWrapper<SysUserRoleEntity>()
        .select(SysUserRoleEntity::getUserId, SysUserRoleEntity::getRoleId).in(SysUserRoleEntity::getUserId, userIds))
      .stream().collect(Collectors.groupingBy(SysUserRoleEntity::getUserId,
        Collectors.mapping(SysUserRoleEntity::getRoleId, Collectors.toList())));
  }

  private List<Long> validateRoleIds(
    List<Long> ids) {
    List<Long> roleIds = Optional.ofNullable(ids).orElseGet(List::of).stream().distinct().toList();
    if (!roleIds.isEmpty()) {
      long existingCount = sysRoleMapper.selectCount(
        new LambdaQueryWrapper<SysRoleEntity>().select(SysRoleEntity::getId).in(SysRoleEntity::getId, roleIds));
      if (existingCount != roleIds.size()) {
        throw new BusinessException(SysUserEnums.ROLE_NOT_EXIST);
      }
    }
    return roleIds;
  }

  private void replaceUserRoles(
    Long userId,
    List<Long> roleIds) {
    sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRoleEntity>().eq(SysUserRoleEntity::getUserId, userId));
    for (Long roleId : roleIds) {
      if (sysUserRoleMapper.insert(new SysUserRoleEntity(userId, roleId)) != 1) {
        throw new BusinessException(ResponseCodeEnum.SYSTEM_ERROR);
      }
    }
  }

  /**
   * 校验路径中的用户 ID，避免无意义查询及错误的更新、删除范围。
   */
  private void validateUserId(
    Long id) {
    if (id == null || id <= 0) {
      throw new BusinessException(SysUserEnums.USER_NOT_EXIST);
    }
  }

  /**
   * 更新场景允许不传密码，传入新密码时仍应满足创建时的最低安全长度。
   */
  private void validateUpdatePassword(
    String password) {
    if (StringUtils.hasText(password) && password.length() < 8) {
      throw new BusinessException(SysUserEnums.PASSWORD_INVALID);
    }
  }

  /**
   * 新增/修改用户时校验重复信息
   */
  private void validateUnique(
    Long id,
    SysUserSaveDto args) {
    // 用户名
    boolean usernameExists = lambdaQuery().eq(SysUserEntity::getUsername, args.getUsername())
      .ne(id != null, SysUserEntity::getId, id).exists();
    if (usernameExists) {
      throw new BusinessException(SysUserEnums.NAME_ALREADY_EXISTS);
    }
    // 邮箱
    boolean emailExists = lambdaQuery().eq(SysUserEntity::getEmail, args.getEmail())
      .ne(id != null, SysUserEntity::getId, id).exists();
    if (emailExists) {
      throw new BusinessException(SysUserEnums.EMAIL_ALREADY_EXISTS);
    }
  }

  /**
   * 新增用户
   */
  @Override
  @Transactional(rollbackFor = Exception.class)
  public ApiResponse<Void> addSysUser(
    SysUserSaveDto args) {
    validateUnique(null, args);
    List<Long> roleIds = validateRoleIds(args.getRoleIds());
    SysUserEntity user = BeanCopyUtils.beanCopy(args, SysUserEntity.class);
    user.setPassword(passwordEncoder.encode(args.getPassword()));
    if (!save(user)) {
      throw new IllegalStateException(ResponseCodeEnum.SYSTEM_ERROR.getMessage());
    }
    replaceUserRoles(user.getId(), roleIds);
    return ApiResponse.success();
  }

  /**
   * 修改用户
   */
  @Override
  @Transactional(rollbackFor = Exception.class)
  public ApiResponse<Void> updateSysUserById(
    Long id,
    SysUserSaveDto args) {
    validateUserId(id);
    if (Objects.isNull(getById(id))) {
      throw new BusinessException(SysUserEnums.USER_NOT_EXIST);
    }
    validateUpdatePassword(args.getPassword());
    validateUnique(id, args);
    List<Long> roleIds = validateRoleIds(args.getRoleIds());
    SysUserEntity user = BeanCopyUtils.beanCopy(args, SysUserEntity.class);
    user.setId(id);
    // 空密码表示保留数据库中的既有散列，不能把表单占位值覆盖为明文空字符串。
    if (StringUtils.hasText(args.getPassword())) {
      user.setPassword(passwordEncoder.encode(args.getPassword()));
    } else {
      user.setPassword(null);
    }
    if (!updateById(user)) {
      throw new IllegalStateException(ResponseCodeEnum.SYSTEM_ERROR.getMessage());
    }
    replaceUserRoles(id, roleIds);
    return ApiResponse.success();
  }

  /**
   * 删除用户
   */
  @Override
  @Transactional(rollbackFor = Exception.class)
  public ApiResponse<Void> deleteSysUserById(
    Long id) {
    validateUserId(id);
    if (Objects.isNull(getById(id))) {
      throw new BusinessException(SysUserEnums.USER_NOT_EXIST);
    }
    sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRoleEntity>().eq(SysUserRoleEntity::getUserId, id));
    if (!removeById(id)) {
      throw new BusinessException(ResponseCodeEnum.SYSTEM_ERROR);
    }
    return ApiResponse.success();
  }
}
