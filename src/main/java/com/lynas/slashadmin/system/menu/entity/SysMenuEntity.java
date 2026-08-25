package com.lynas.slashadmin.system.menu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 系统菜单表 表对应的持久化实体。
 *
 * <p>{@code @TableName} 指定本类映射的数据库表；{@code @TableId} 标记主键。{@code @Getter} 与 {@code @Setter}
 * 由 Lombok 在编译时为本类字段生成访问方法；它们不参与数据库字段映射。
 *
 * @author LynasTing
 */
@Getter
@Setter
@TableName("sys_menu")
public class SysMenuEntity {
  /**
   * 菜单主键 ID
   */
  @TableId(type = IdType.AUTO)
  private Long id;

  /**
   * 父级菜单ID, 0表示根节点
   */
  private Object parentId;

  /**
   * 菜单名称
   */
  private String name;

  /**
   * 唯一编码
   */
  private String code;

  /**
   * 节点类型 1 分组 2 目录 3 菜单 4 操作按钮
   */
  private Object category;

  /**
   * 排序值，越小越靠前
   */
  private Object sort;

  /**
   * 状态 0 禁用  1 启用
   */
  private Object status;

  /**
   * 前端路由路径
   */
  private String path;

  /**
   * 前端组件标识或组件路径
   */
  private String component;

  /**
   * 菜单图标
   */
  private String icon;

  /**
   * 是否隐藏 0 否 1 是
   */
  private Object hidden;

  /**
   * 菜单描述
   */
  private String description;

  /**
   * 外链地址
   */
  private String externalLink;

  /**
   * 创建时间
   */
  private LocalDateTime createdAt;

  /**
   * 更新时间
   */
  private LocalDateTime updatedAt;
}
