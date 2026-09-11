package com.lynas.slashadmin.system.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * System users 表对应的持久化实体。
 *
 * <p>{@code @TableName} 指定本类映射的数据库表；{@code @TableId} 标记主键。{@code @Getter} 与 {@code @Setter}
 * 由 Lombok 在编译时为本类字段生成访问方法；它们不参与数据库字段映射。
 *
 * @author LynasTing
 */
@Getter
@Setter
@TableName("sys_user")
public class SysUserEntity {
  /**
   * UUID primary key
   */
  @TableId(type = IdType.AUTO)
  private Long id;
  /**
   * Login name
   */
  private String username;
  /**
   * 昵称
   */
  private String nickname;
  /**
   * BCrypt password hash
   */
  private String password;
  /**
   * Email address
   */
  private String email;
  /**
   * Phone number
   */
  private String phone;
  /**
   * Avatar URL
   */
  private String avatar;
  /**
   * 0 disabled, 1 enabled
   */
  private Integer status;
  /**
   * 0 active, 1 deleted
   */
  private Integer deleted;
  /**
   * Creation time
   */
  private LocalDateTime createdAt;
  /**
   * Update time
   */
  private LocalDateTime updatedAt;
}
