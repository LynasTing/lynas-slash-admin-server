CREATE DATABASE IF NOT EXISTS lynas_slash_admin
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE lynas_slash_admin;

CREATE TABLE IF NOT EXISTS sys_user (
    id CHAR(36) NOT NULL COMMENT 'UUID primary key',
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
    id CHAR(36) NOT NULL COMMENT 'UUID primary key',
    name VARCHAR(64) NOT NULL COMMENT 'Role name',
    code VARCHAR(64) NOT NULL COMMENT 'Role code',
    sort INT NOT NULL DEFAULT 0 COMMENT 'Display order',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '0 disabled, 1 enabled',
    description VARCHAR(255) DEFAULT NULL COMMENT 'Role description',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '0 active, 1 deleted',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_role_code (code),
    KEY idx_sys_role_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='System roles';
