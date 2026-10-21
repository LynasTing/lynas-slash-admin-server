package com.lynas.slashadmin.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.lynas.slashadmin.auth.entity.RefreshTokenEntity;
import com.lynas.slashadmin.auth.mapper.RefreshTokenMapper;
import com.lynas.slashadmin.auth.service.AuthSessionService;
import com.lynas.slashadmin.system.user.entity.SysUserEntity;
import com.lynas.slashadmin.system.user.mapper.SysUserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Set;

/**
 * 认证版本与 refresh token 会话失效服务实现
 *
 * <p>认证状态变化时，在同一事务内递增 auth_version 并撤销 refresh token,
 * 防止只完成其中一项而留下可继续使用的旧凭证
 */
@Service
public class AuthSessionServiceImpl implements AuthSessionService {
  private final SysUserMapper sysUserMapper;
  private final RefreshTokenMapper refreshTokenMapper;

  /**
   * 统一时间来源。
   *
   * <p>生产环境使用系统时间，测试环境使用固定时间，避免撤销时间依赖不可控的系统时钟。
   */
  private final Clock clock;

  public AuthSessionServiceImpl(SysUserMapper sysUserMapper, RefreshTokenMapper refreshTokenMapper, Clock clock) {
    this.sysUserMapper = sysUserMapper;
    this.refreshTokenMapper = refreshTokenMapper;
    this.clock = clock;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void revokeAllByUserId(
    Long userId) {
    revokeAllByUserIds(Set.of(userId));
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void revokeAllByUserIds(
    Collection<Long> userIds) {
    /**
     * <p>Set 不允许重复元素
     * Set.copyOf() 根据现有 Collection 创建一个 Set, 自动去重
     */
    Set<Long> distinctUserIds = Set.copyOf(userIds);
    if (distinctUserIds.isEmpty()) {
      return;
    }

    // 当前时间
    LocalDateTime revokedAt = LocalDateTime.now(clock);

    LambdaUpdateWrapper<RefreshTokenEntity> updateWrapper = new LambdaUpdateWrapper<>();
    // 筛选指定用户的 Refresh Token
    updateWrapper.in(RefreshTokenEntity::getUserId, distinctUserIds);
    // 只处理尚未撤销的 refresh token
    updateWrapper.isNull(RefreshTokenEntity::getRevokedAt);
    // 将撤销时间设置为当前时间
    updateWrapper.set(RefreshTokenEntity::getRevokedAt, revokedAt);
    // 执行 update
    refreshTokenMapper.update(null, updateWrapper);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void incrementAuthVersionAndRevoke(
    Long userId) {
    incrementAuthVersionsAndRevoke(Set.of(userId));
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void incrementAuthVersionsAndRevoke(
    Collection<Long> userIds) {
    // 1.去重并快速结束空集合，避免执行无条件数据库更新
    Set<Long> distinctUserIds = Set.copyOf(userIds);
    if (distinctUserIds.isEmpty()) {
      return;
    }

    // 2.递增数据库认证版本，使旧 Access JWT 的 av 失效
    LambdaUpdateWrapper<SysUserEntity> updateWrapper = new LambdaUpdateWrapper<>();
    // 指定需要更新的用户
    updateWrapper.in(SysUserEntity::getId, distinctUserIds);
    // 将这些用户的认证版本递增
    updateWrapper.setSql("auth_version = auth_version + 1");
    // 执行 update
    sysUserMapper.update(null, updateWrapper);
    // 撤销这些用户所有的 refresh token
    revokeAllByUserIds(distinctUserIds);
  }
}
