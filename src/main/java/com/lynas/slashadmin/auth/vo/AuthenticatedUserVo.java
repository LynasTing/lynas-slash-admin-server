package com.lynas.slashadmin.auth.vo;

import java.util.ArrayList;
import java.util.List;

import com.lynas.slashadmin.auth.model.AuthMenuNode;
import com.lynas.slashadmin.auth.model.AuthRole;
import com.lynas.slashadmin.auth.model.AuthenticatedUser;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 当前登录用户返回给客户端的数据
 *
 * <p>该对象只包含前端需要的用户、角色、业务权限和菜单信息。
 * 密码散列、认证版本号等内部字段禁止通过该对象返回给客户端。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "当前登录用户信息")
public class AuthenticatedUserVo {

  @Schema(description = "用户主键 ID", example = "1")
  private Long userId;

  @Schema(description = "登录用户名", example = "admin")
  private String username;

  @Schema(description = "用户昵称", example = "管理员")
  private String nickname;

  @Schema(description = "电子邮箱", example = "admin@example.com")
  private String email;

  @Schema(description = "手机号码", example = "13800138000")
  private String phone;

  @Schema(description = "用户头像地址", example = "/avatars/admin.png")
  private String avatar;

  @Schema(description = "当前用户拥有的角色")
  private List<AuthRole> roles;

  @Schema(description = "当前用户拥有的业务权限编码", example = "[\"user:list\", \"menu:list\"]")
  private List<String> permissions;

  @Schema(description = "当前用户可访问的菜单树")
  private List<AuthMenuNode> menu;

  /**
   * 将内部认证用户转换为接口响应对象
   *
   * <p>内部 authorities 同时包含 ROLE_* 角色权限和业务操作权限，
   * 角色信息已经通过 roles 字段返回，因此 permissions 只保留业务操作权限
   */
  public static AuthenticatedUserVo from(
    AuthenticatedUser user) {
    // 1.提取业务权限，过滤 ROLE_ 开头的权限
    List<String> permissions = new ArrayList<>();

    if (user.authorities() != null) {
      for (String authority : user.authorities()) {
        if (authority != null && !authority.startsWith("ROLE_")) {
          permissions.add(authority);
        }
      }
    }

    // 2.获取角色列表
    List<AuthRole> roles = List.of();
    if (user.roles() != null) {
      roles = List.copyOf(user.roles());
    }

    // 3.获取菜单列表
    List<AuthMenuNode> menus = user.menus() == null ? List.of() : List.copyOf(user.menus());

    // 4.创建并返回响应对象
    return new AuthenticatedUserVo(user.userId(), user.username(), user.nickname(), user.email(), user.phone(),
      user.avatar(), roles, List.copyOf(permissions), menus);
  }
}
