CREATE DATABASE IF NOT EXISTS lynas_slash_admin
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE lynas_slash_admin;

CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户主键 ID',
    username VARCHAR(64) NOT NULL COMMENT 'Login name',
    password VARCHAR(100) NOT NULL COMMENT 'BCrypt password hash',
    email VARCHAR(255) NOT NULL COMMENT 'Email address',
    phone VARCHAR(32) DEFAULT NULL COMMENT 'Phone number',
    avatar VARCHAR(512) DEFAULT NULL COMMENT 'Avatar URL',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '0 disabled, 1 enabled',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '0 active, 1 deleted',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_username (username),
    UNIQUE KEY uk_sys_user_email (email),
    KEY idx_sys_user_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='System users';

CREATE TABLE IF NOT EXISTS sys_role (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '角色主键 ID',
    name VARCHAR(64) NOT NULL COMMENT 'Role name',
    code VARCHAR(64) NOT NULL COMMENT 'Role code',
    sort INT NOT NULL DEFAULT 0 COMMENT 'Display order',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '0 disabled, 1 enabled',
    description VARCHAR(255) DEFAULT NULL COMMENT 'Role description',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_role_code (code),
    KEY idx_sys_role_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='System roles';

CREATE TABLE IF NOT EXISTS sys_menu (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '菜单主键 ID',
    `parent_id` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '父级菜单ID, 0表示根节点',

    `name` VARCHAR(64) NOT NULL COMMENT '菜单名称',
    `code` VARCHAR(128) NOT NULL COMMENT '唯一编码',

    `category` TINYINT UNSIGNED NOT NULL COMMENT '节点类型 1 分组 2 目录 3 菜单 4 操作按钮',
    `sort` SMALLINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '排序值，越小越靠前',
    `status` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态 0 禁用  1 启用',

    `path` VARCHAR(255) DEFAULT NULL COMMENT '前端路由路径',
    `component` VARCHAR(255) DEFAULT NULL COMMENT '前端组件标识或组件路径',
    `icon` VARCHAR(128) DEFAULT NULL COMMENT '菜单图标',

    `hidden` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否隐藏 0 否 1 是',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '菜单描述',

    `external_link` VARCHAR(500) DEFAULT NULL COMMENT '外链地址',

    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    CHECK (`category` IN (1, 2, 3, 4)),
    CHECK (`status` IN (0, 1)),
    CHECK (`hidden` IN (0, 1)),

    PRIMARY KEY (id),
    UNIQUE KEY `uk_code` (`code`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统菜单表'

CREATE TABLE IF NOT EXISTS sys_user_role (
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户 ID',
    `role_id` BIGINT UNSIGNED NOT NULL COMMENT '角色 ID',

    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_role_user
        FOREIGN KEY (user_id) REFERENCES sys_user (id),
    CONSTRAINT fk_user_role_role
        FOREIGN KEY (role_id) REFERENCES sys_role (id)
) COMMENT='用户与关联角色表'

CREATE TABLE sys_role_menu (
   role_id BIGINT UNSIGNED NOT NULL,
   menu_id BIGINT UNSIGNED NOT NULL,
   created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
   PRIMARY KEY (role_id, menu_id),
   CONSTRAINT fk_role_menu_role FOREIGN KEY (role_id) REFERENCES sys_role (id),
   CONSTRAINT fk_role_menu_menu FOREIGN KEY (menu_id) REFERENCES sys_menu (id)
) COMMENT='角色与菜单关联表'
