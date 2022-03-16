use mysql;
update user set host = '%' where user = 'root';
update user set authentication_string = password("root") where user="root";
FLUSH PRIVILEGES;

CREATE DATABASE IF NOT EXISTS registrymetadb DEFAULT CHARACTER SET utf8mb4 DEFAULT COLLATE utf8mb4_unicode_ci;

use registrymetadb;

CREATE TABLE IF NOT EXISTS `app_revision`
(
    `id`             bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `data_center`    varchar(128) NOT NULL COMMENT '集群名',
    `revision`       varchar(128) NOT NULL COMMENT 'revision',
    `app_name`       varchar(128) NOT NULL COMMENT '应用名',
    `base_params`    text         DEFAULT NULL COMMENT '基础参数',
    `service_params` text         DEFAULT NULL COMMENT '服务参数',
    `gmt_create`     timestamp    NOT NULL COMMENT '创建时间',
    `gmt_modified`   timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    `client_version` varchar(512) DEFAULT NULL COMMENT '客户端版本',
    `deleted`        tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否被删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_data_center_revision` (`data_center`, `revision`)
    ) DEFAULT CHARSET=utf8mb4  COMMENT='服务元数据表';

CREATE TABLE IF NOT EXISTS `interface_apps_index`
(
    `id`             bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `data_center`    varchar(128) NOT NULL COMMENT '集群名称',
    `app_name`       varchar(128) NOT NULL COMMENT '应用名',
    `interface_name` varchar(386) NOT NULL COMMENT '接口名',
    `gmt_create`     timestamp(6) NOT NULL COMMENT '创建时间',
    `gmt_modified`   timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '修改时间',
    `hashcode`       varchar(128) NOT NULL COMMENT '唯一索引hashcode',
    `reference`      tinyint(4) NOT NULL COMMENT '是否被引用',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_hashcode` (`data_center`, `app_name`, `hashcode`),
    KEY              `idx_data_center_interface` (`interface_name`)
    ) DEFAULT CHARSET=utf8mb4 COMMENT='interface与revision索引表';

CREATE TABLE IF NOT EXISTS `provide_data`
(
    `id`           bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `data_center`  varchar(128)  NOT NULL COMMENT '集群名',
    `data_key`     varchar(512) NOT NULL COMMENT 'data.yaml key',
    `data_value`   mediumtext             DEFAULT NULL COMMENT 'data.yaml value',
    `gmt_create`   timestamp     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` timestamp     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `data_version` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '版本号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_data_center_key` (`data_center`(128), `data_key`(512))
    ) DEFAULT CHARSET = utf8mb4 COMMENT = '注册中心配置存储表';

CREATE TABLE IF NOT EXISTS `distribute_lock`
(
    `id`           bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `data_center`  varchar(128)  NOT NULL COMMENT '集群名',
    `lock_name`    varchar(512) NOT NULL COMMENT '分布式锁名称',
    `owner`        varchar(512)  NOT NULL COMMENT '锁拥有者',
    `duration`     bigint(20) NOT NULL COMMENT '持续周期',
    `gmt_create`   timestamp(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `gmt_modified` timestamp(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP (3) COMMENT '修改时间',
    `term`         bigint(20) NOT NULL DEFAULT 0 COMMENT '任期',
    `term_duration`        bigint(20) NOT NULL DEFAULT 0 COMMENT '租期',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_data_center_lock` (`data_center`(128), `lock_name`(512)),
    KEY            `idx_lock_owner` (`owner`(512))
    ) DEFAULT CHARSET = utf8mb4 COMMENT = '分布式锁';


CREATE TABLE IF NOT EXISTS `client_manager_address`
(
    `id`           bigint(20) NOT NULL AUTO_INCREMENT ,
    `data_center`  varchar(128) NOT NULL COMMENT '集群名称',
    `address`      varchar(256) NOT NULL COMMENT 'address',
    `gmt_create`   timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `operation`    varchar(128) NOT NULL COMMENT '操作类型',
    `pub`          tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否持久化关流pub',
    `sub`          tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否持久化关流sub',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_data_center_address` (`data_center`, `address`)
    ) DEFAULT CHARSET = utf8mb4 COMMENT = '关流量pod数据表';


CREATE TABLE IF NOT EXISTS  `recover_config`
(
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `property_table` varchar(256) NOT NULL COMMENT '容灾独立存储配置table',
    `property_key` varchar(512) NOT NULL COMMENT '容灾独立存储配置table_key',
    `gmt_create` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_table_key` (`property_table`, `property_key`)
    );