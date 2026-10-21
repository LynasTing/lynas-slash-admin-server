package com.lynas.slashadmin.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.lynas.slashadmin.auth.entity.RefreshTokenEntity;
import com.lynas.slashadmin.auth.mapper.RefreshTokenMapper;
import com.lynas.slashadmin.system.user.entity.SysUserEntity;
import com.lynas.slashadmin.system.user.mapper.SysUserMapper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * 认证版本和 Refresh Token 会话失效服务测试。
 *
 * <p>测试验证服务确实执行两类数据库更新，并覆盖空集合保护逻辑。
 */
class AuthSessionServiceImplTest {
  private final SysUserMapper sysUserMapper = mock(SysUserMapper.class);
  private final RefreshTokenMapper refreshTokenMapper = mock(RefreshTokenMapper.class);
  private AuthSessionServiceImpl service;

  @BeforeEach
  void setUp() {
    // 纯单元测试没有 Spring/MyBatis 上下文，需要显式初始化 Lambda Wrapper 使用的实体元数据缓存。
    Configuration configuration = new Configuration();
    TableInfoHelper.initTableInfo(new MapperBuilderAssistant(configuration, ""), SysUserEntity.class);
    TableInfoHelper.initTableInfo(new MapperBuilderAssistant(configuration, ""), RefreshTokenEntity.class);

    service = new AuthSessionServiceImpl(sysUserMapper, refreshTokenMapper,
      Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC));
  }

  @Test
  void incrementAuthVersionAndRevoke_updatesUserVersionAndRefreshSessions() {
    service.incrementAuthVersionAndRevoke(42L);

    verify(sysUserMapper).update(isNull(), any(LambdaUpdateWrapper.class));
    verify(refreshTokenMapper).update(isNull(), any(LambdaUpdateWrapper.class));
  }

  @Test
  void incrementAuthVersionsAndRevoke_whenUserIdsContainDuplicates_updatesOnce() {
    service.incrementAuthVersionsAndRevoke(List.of(42L, 42L, 43L));

    verify(sysUserMapper).update(isNull(), any(LambdaUpdateWrapper.class));
    verify(refreshTokenMapper).update(isNull(), any(LambdaUpdateWrapper.class));
  }

  @Test
  void incrementAuthVersionsAndRevoke_whenUserIdsEmpty_doesNothing() {
    service.incrementAuthVersionsAndRevoke(List.of());

    verifyNoInteractions(sysUserMapper, refreshTokenMapper);
  }

  @Test
  void incrementAuthVersionsAndRevoke_containsAuthVersionIncrementExpression() {
    @SuppressWarnings("unchecked")
    LambdaUpdateWrapper<SysUserEntity>[] capturedUserUpdate = new LambdaUpdateWrapper[1];
    org.mockito.Mockito.doAnswer(invocation -> {
      capturedUserUpdate[0] = invocation.getArgument(1);
      return 1;
    }).when(sysUserMapper).update(isNull(), any(LambdaUpdateWrapper.class));

    service.incrementAuthVersionAndRevoke(42L);

    assertTrue(capturedUserUpdate[0].getSqlSet().contains("auth_version = auth_version + 1"));
  }
}
