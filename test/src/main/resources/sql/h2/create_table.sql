drop table if exists APP_REVISION;
create table APP_REVISION
(
    id                   bigint unsigned not null auto_increment primary key,
    data_center          varchar(129) not null,
    revision             varchar(128) not null,
    app_name             varchar(128) not null,
    base_params          longtext              default null,
    service_params       longtext              default null,
    service_params_large longtext              default null,
    gmt_create           timestamp    not null,
    gmt_modified         timestamp    not null,
    client_version       varchar(512)          default null,
    deleted              tinyint(1)   NOT NULL DEFAULT 0 COMMENT '是否被删除',
    UNIQUE KEY `uk_data_center_revision` (`data_center`, `revision`)
);

drop table if exists interface_apps_index;
CREATE TABLE interface_apps_index
(
    id             bigint(20)   NOT NULL AUTO_INCREMENT,
    data_center    varchar(128) NOT NULL,
    app_name       varchar(128) NOT NULL COMMENT '应用名',
    interface_name varchar(386) NOT NULL COMMENT '接口名',
    gmt_create     timestamp(6) NOT NULL COMMENT '创建时间',
    gmt_modified   timestamp(6) NOT NULL COMMENT '修改时间',
    hashcode       varchar(128) NOT NULL COMMENT '唯一索引hashcode',
    reference      tinyint(4)   NOT NULL COMMENT '是否被引用',
    PRIMARY KEY (id),
    UNIQUE KEY `uk_hashcode` (`data_center`, `app_name`, `hashcode`),
    KEY            `idx_data_center_interface` (`interface_name`)
);

drop table if exists distribute_lock;
CREATE TABLE distribute_lock
(
    id            bigint(20)    NOT NULL AUTO_INCREMENT primary key,
    data_center   varchar(128)  NOT NULL,
    lock_name     varchar(1024) NOT NULL,
    owner         varchar(512)  NOT NULL,
    duration      bigint(20)    NOT NULL,
    term          bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '任期',
    term_duration bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '租期',
    gmt_create    timestamp(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    gmt_modified  timestamp(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_data_center_lock` (`data_center`, `lock_name`),
    KEY           `idx_lock_owner` (`owner`)
);

drop table if exists provide_data;
CREATE TABLE provide_data
(
    id             bigint(20)    NOT NULL AUTO_INCREMENT primary key,
    data_center    varchar(128)  NOT NULL,
    data_key       varchar(1024) NOT NULL,
    data_value     mediumtext             DEFAULT NULL,
    gmt_create     timestamp     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    gmt_modified   timestamp     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `data_version` bigint(20)    NOT NULL DEFAULT '0' COMMENT '版本号',
    UNIQUE KEY `uk_data_center_key` (`data_center`, `data_key`)
);

drop table if exists client_manager_address;
CREATE TABLE `client_manager_address`
(
    `id`           bigint(20)   NOT NULL AUTO_INCREMENT primary key,
    `data_center`  varchar(128) NOT NULL COMMENT '集群名称',
    `address`      varchar(256) NOT NULL COMMENT 'address',
    `gmt_create`   timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `operation`    varchar(128) NOT NULL COMMENT '操作类型',
    `pub`          tinyint(1)   NOT NULL DEFAULT 1 COMMENT '是否持久化关流pub',
    `sub`          tinyint(1)   NOT NULL DEFAULT 1 COMMENT '是否持久化关流sub',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_data_center_address` (`data_center`, `address`)
);

drop table if exists recover_config;
CREATE TABLE `recover_config`
(
    `id`             bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '主键',
    `property_table` varchar(256) NOT NULL COMMENT '容灾独立存储配置table',
    `property_key`   varchar(512) NOT NULL COMMENT '容灾独立存储配置table_key',
    `gmt_create`     timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified`   timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_property_table_key` (`property_table`, `property_key`)
);

drop table if exists multi_cluster_sync_info;
CREATE TABLE `multi_cluster_sync_info`
(
    `id`                   bigint(20)    NOT NULL AUTO_INCREMENT COMMENT '主键' primary key,
    `data_center`          varchar(512)  NOT NULL COMMENT '集群名称',
    `remote_data_center`   varchar(512)  NOT NULL COMMENT '同步的集群名称',
    `remote_meta_address`  varchar(1024) NOT NULL COMMENT '同步的集群地址',
    `enable_sync_datum`    varchar(16)   NOT NULL COMMENT 'datum同步的开关是否开启',
    `enable_push`          varchar(16)   NOT NULL COMMENT '同步的数据是否允许推送',
    `sync_datainfoids`     MEDIUMTEXT    NOT NULL COMMENT '同步的dataInfoId名单',
    `syn_publisher_groups` varchar(4096) NOT NULL COMMENT '同步的group名单',
    `ignore_datainfoids`   MEDIUMTEXT    NOT NULL COMMENT '忽略同步的dataInfoId',
    `data_version`         bigint(20)    NOT NULL DEFAULT '0' COMMENT '版本号',
    `gmt_create`           timestamp     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified`         timestamp     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_data_center_remote_data_center` (`data_center`, `remote_data_center`),
    KEY                    `idx_data_center` (`data_center`)
)








