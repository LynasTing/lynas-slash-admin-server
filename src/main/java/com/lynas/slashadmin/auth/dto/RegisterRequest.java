package com.lynas.slashadmin.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "账号 + 密码注册请求参数")
public class RegisterRequest {

  @NotBlank(message = "邮箱地址不能为空")
  @Size(max = 255, message = "邮箱地址不能超过 255 个字符")
  @Schema(description = "邮箱地址", example = "new-user@example", requiredMode = Schema.RequiredMode.REQUIRED)
  @Email(message = "邮箱地址格式不正确")
  private String email;

  @NotBlank(message = "用户名不能为空")
  @Size(min = 2, max = 64, message = "用户名长度应为 2 ～ 64 个字符")
  @Schema(description = "用户名", example = "new-user", requiredMode = Schema.RequiredMode.REQUIRED)
  private String username;

  @Size(min = 2, max = 64, message = "用户名长度应为 2 ～ 64 个字符")
  @Schema(description = "用户名", example = "new-user", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private String nickname;

  @NotBlank(message = "密码不能为空")
  @Size(min = 8, max = 72, message = "密码长度必须为 8 ～ 72个字符")
  @Schema(description = "注册密码，长度为 8 到 72 个字符", example = "User@123456", requiredMode = Schema.RequiredMode.REQUIRED)
  private String password;
}
