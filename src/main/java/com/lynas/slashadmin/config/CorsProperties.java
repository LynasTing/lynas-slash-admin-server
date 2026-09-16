package com.lynas.slashadmin.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * 浏览器跨域访问配置
 *
 * <p>允许的前端 origin 从配置文件读取，避免把环境地址硬编码到安全过滤链
 */
@Validated
@ConfigurationProperties(prefix = "security.cors")
public record CorsProperties(@NotEmpty(
  message = "security.cors.allowed-origins 不能为空") List<@NotBlank(message = "CORS origin 不能为空") String> allowedOrigins) {
}
