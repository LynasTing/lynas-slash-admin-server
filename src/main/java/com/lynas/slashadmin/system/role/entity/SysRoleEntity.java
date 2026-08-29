package com.lynas.slashadmin.system.role.entity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * 系统角色表对应的持久化实体。
 *
 * <p>{@code @TableName} 指定本类映射 {@code sys_role} 表。
 */
@Getter
@Setter
@TableName("sys_role")
public class SysRoleEntity {
  /** 角色主键；由数据库自增生成。 */
  @TableId(type = IdType.AUTO)
  private Long id;
  /** 角色名称。 */
  private String name;
  /** 角色编码。 */
  private String code;
  /** 显示顺序。 */
  private Integer sort;
  /** 启用状态：{@code 0} 表示禁用，{@code 1} 表示启用。 */
  private Integer status;
  /** 角色补充说明；允许为空。 */
  private String description;
  /** 创建时间。 */
  private LocalDateTime createdAt;
  /** 最后更新时间。 */
  private LocalDateTime updatedAt;
}
