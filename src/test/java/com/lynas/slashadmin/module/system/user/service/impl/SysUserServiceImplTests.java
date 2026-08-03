package com.lynas.slashadmin.module.system.user.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.lynas.slashadmin.module.system.user.dto.SysUserResponse;
import com.lynas.slashadmin.module.system.user.entity.SysUser;
import com.lynas.slashadmin.module.system.user.mapper.SysUserMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class SysUserServiceImplTests {

  @Test
  void excludesPasswordHashFromListResponse() {
    SysUserMapper mapper = Mockito.mock(SysUserMapper.class);
    SysUser user = new SysUser();
    user.setId("test-user-id");
    user.setUsername("demo");
    user.setPassword("password-hash-must-not-be-exposed");
    user.setEmail("demo@example.com");
    user.setStatus(1);
    when(mapper.selectList(any(Wrapper.class))).thenReturn(List.of(user));

    List<SysUserResponse> responses = new SysUserServiceImpl(mapper).listUsers();

    assertThat(responses)
        .singleElement()
        .satisfies(
            response -> {
              assertThat(response.username()).isEqualTo("demo");
              assertThat(response.email()).isEqualTo("demo@example.com");
            });
    assertThat(SysUserResponse.class.getRecordComponents())
        .extracting(component -> component.getName())
        .doesNotContain("password");
  }
}
