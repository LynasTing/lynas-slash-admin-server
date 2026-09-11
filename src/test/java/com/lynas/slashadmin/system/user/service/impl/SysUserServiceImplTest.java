package com.lynas.slashadmin.system.user.service.impl;

import com.lynas.slashadmin.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 用户服务边界校验测试。
 */
class SysUserServiceImplTest {
  @Test
  void getUserDetail_whenIdIsNotPositive_throwsBusinessException() {
    SysUserServiceImpl service = new SysUserServiceImpl(null, null, new BCryptPasswordEncoder());

    assertThrows(BusinessException.class, () -> service.getSysUserDetail(0L));
  }

  @Test
  void deleteUser_whenIdIsNull_throwsBusinessException() {
    SysUserServiceImpl service = new SysUserServiceImpl(null, null, new BCryptPasswordEncoder());

    assertThrows(BusinessException.class, () -> service.deleteSysUserById(null));
  }
}
