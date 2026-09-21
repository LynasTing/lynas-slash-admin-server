package com.lynas.slashadmin.auth.model;

import java.util.List;

/**
 * 当前认证用户可访问的导航菜单节点
 *
 * <p>操作按钮（category = 4）只转换为 authority，不进入本菜单树
 */
public record AuthMenuNode(Long id, Long parentId, String name, String code, Integer category, Integer sort,
  Integer status, String path, String component, String icon, String i18nKey, Integer hidden, String description,
  String externalLink, List<AuthMenuNode> children) {
}
