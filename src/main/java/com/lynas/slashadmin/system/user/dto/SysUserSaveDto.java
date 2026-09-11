package com.lynas.slashadmin.system.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 新增或更新用户的请求参数。
 *
 * <p>用户主键、密码散列、逻辑删除标记和审计字段由服务端维护，客户端不得传入。
 */
@Data
@Schema(description = "新增/更新用户的请求参数")
public class SysUserSaveDto {
  /** 创建用户时启用的密码校验分组。 */
  public interface Create {
  }

  @NotBlank(message = "用户名不能为空")
  @Size(max = 64, message = "用户名不能超过 64 个字符")
  @Schema(description = "登录用户名", example = "admin", requiredMode = Schema.RequiredMode.REQUIRED)
  private String username;

  @Size(max = 50, message = "用户昵称不能超过 50 个字符")
  @Schema(description = "用户昵称；未填写时为空", example = "管理员")
  private String nickname;

  @NotBlank(groups = Create.class, message = "密码不能为空")
  @Size(min = 8, max = 72, groups = Create.class, message = "密码长度必须为 8 到 72 个字符")
  @Size(max = 72, message = "密码不能超过 72 个字符")
  @Schema(description = "登录密码；创建时必填且长度为 8 到 72 个字符，更新时为空表示不修改密码", example = "Admin@123456")
  private String password;

  @NotBlank(message = "邮箱不能为空")
  @Email(message = "邮箱格式不正确")
  @Size(max = 255, message = "邮箱不能超过 255 个字符")
  @Schema(description = "电子邮箱", example = "admin@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
  private String email;

  @Size(max = 32, message = "手机号不能超过 32 个字符")
  @Schema(description = "手机号；未填写时为空", example = "13800138000")
  private String phone;

  @Size(max = 512, message = "头像地址不能超过 512 个字符")
  @Schema(description = "用户头像地址", example = "/avatars/admin.png")
  private String avatar;

  @Min(value = 0, message = "用户状态只能为 0 或 1")
  @Max(value = 1, message = "用户状态只能为 0 或 1")
  @Schema(description = "启用状态：0 表示禁用，1 表示启用", example = "1")
  private Integer status;

  @Schema(description = "关联角色主键 ID 列表", example = "[1, 2]")
  private List<@NotNull(message = "角色 ID 不能为空") @Positive(message = "角色 ID 必须大于 0") Long> roleIds;
}
