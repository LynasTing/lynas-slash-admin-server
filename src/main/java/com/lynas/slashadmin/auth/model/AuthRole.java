package com.lynas.slashadmin.auth.model;

/**
 * 当前认证用户拥有的启用角色。
 *
 * <p>该模型用于登录和当前用户响应，不包含角色管理页面的排序、状态或描述字段
 */
public record AuthRole(Long id, String code, String name) {
}
