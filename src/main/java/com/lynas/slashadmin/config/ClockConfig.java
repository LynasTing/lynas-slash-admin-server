package com.lynas.slashadmin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * 应用统一时间来源配置
 *
 * <p>认证服务通过注入 Clock 获取当前时间，避免业务代码散落 {@code Instant.now()}。
 * 测试时可以替换固定 Clock, 使 JWT 的签发时间和过期时间可重复验证
 */
@Configuration
public class ClockConfig {
  /**
   * 提供以 UTC 为基准的系统时钟
   *
   * <p>JWT 的 {@code iat} 和 {@code exp} 是绝对时间点，使用 UTC 可避免
   * 服务器默认时区参与安全时间计算
   *
   * @return 应用默认的 UTC 系统时钟
   */
  @Bean
  public Clock clock() {
    return Clock.systemUTC();
  }
}
