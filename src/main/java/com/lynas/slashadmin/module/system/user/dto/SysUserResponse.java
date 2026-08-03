package com.lynas.slashadmin.module.system.user.dto;

import java.time.LocalDateTime;

/** 用户列表响应，不包含密码哈希或逻辑删除标记。 */
public record SysUserResponse(
    String id,
    String username,
    String email,
    String phone,
    String avatar,
    Integer status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {}
