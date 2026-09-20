package com.lynas.slashadmin.auth.model;

/**
 * 用户当前认证状态的最小数据库快照
 *
 * <p>JWT 校验只读取用户是否存在、是否启用、是否删除及认证版本，
 * 不加载角色、权限或菜单树
 */
public record UserAuthSnapshot(Long userId, Integer status, Integer deleted, Integer authVersion) {
}
