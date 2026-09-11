package com.lynas.slashadmin.system.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统用户列表返回给客户端的数据。
 *
 * <p>密码散列和逻辑删除标记属于内部持久化数据，禁止通过该对象返回给客户端。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "系统用户列表项")
public class SysUserListVo {
  @Schema(description = "用户主键 ID", example = "1")
  private Long id;

  @Schema(description = "登录用户名", example = "admin")
  private String username;

  @Schema(description = "用户昵称；未填写时为空", example = "管理员")
  private String nickname;

  @Schema(description = "电子邮箱", example = "admin@example.com")
  private String email;

  @Schema(description = "手机号", example = "13800138000")
  private String phone;

  @Schema(description = "用户头像地址", example = "/avatars/admin.png")
  private String avatar;

  @Schema(description = "启用状态：0 表示禁用，1 表示启用", example = "1")
  private Integer status;

  @Schema(description = "创建时间", example = "2026-08-14T10:30:00")
  private LocalDateTime createdAt;

  @Schema(description = "关联角色主键 ID 列表", example = "[1, 2]")
  private List<Long> roleIds;
}
