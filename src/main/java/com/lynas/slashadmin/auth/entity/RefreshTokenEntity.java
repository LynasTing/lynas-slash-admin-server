package com.lynas.slashadmin.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 可轮换 refresh token 会话表对应的持久化实体
 *
 * <p>本实体只保存 refresh token 的 SHA-256 摘要，绝不保存或返回原始 token
 */
@Getter
@Setter
@TableName("auth_refresh_token")
public class RefreshTokenEntity {

  /**
   * refresh 会话主键，由数据库自增生成
   */
  @TableId(type = IdType.AUTO)
  private Long id;

  /**
   * 所属用户 ID
   */
  private Long userId;

  /**
   * refresh token 的 SHA-256 十六进制摘要
   */
  private String tokenHash;

  /**
   * 同一轮换链路的会话族 UUID
   */
  private String familyId;

  /**
   * refresh token 的过期时间
   */
  private LocalDateTime expiresAt;

  /**
   * 撤销时间；为 null 时表示该 token 仍有效
   */
  private LocalDateTime revokedAt;

  /**
   * 本次轮换创建的新 refresh token 主键；首次签发时应为 null
   */
  private Long replacedById;

  /**
   * 创建时间
   */
  private LocalDateTime createdAt;
}
