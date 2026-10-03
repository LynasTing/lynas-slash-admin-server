package com.lynas.slashadmin.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户名密码请求参数
 *
 * <p>暂时只支持账号密码登录，不包含 rememberMe、设备回话及其它登录方式
 */
@Data
@Schema(description = "用户名 + 密码登录请求参数")
public class LoginRequest {

  @NotBlank(message = "用户名不能为空")
  @Size(max = 64, message = "用户名不能超过 64 个字符")
  @Schema(description = "登录用户名", example = "admin", requiredMode = Schema.RequiredMode.REQUIRED)
  private String username;

  @NotBlank(message = "密码不能为空")
  @Size(min = 8, max = 72, message = "密码长度必须为 8 到 72 个字符")
  @Schema(description = "登录密码,长度为 8 到 72 个字符", example = "Admin@123456", requiredMode = Schema.RequiredMode.REQUIRED)
  private String password;
}
