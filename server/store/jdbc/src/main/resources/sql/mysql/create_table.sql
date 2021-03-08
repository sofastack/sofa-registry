CREATE TABLE `app_revision` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `data_center` varchar(128) NOT NULL COMMENT '集群名',
  `revision` varchar(128) NOT NULL COMMENT 'revision',
  `app_name` varchar(128) NOT NULL COMMENT '应用名',
  `base_params` text DEFAULT NULL COMMENT '基础参数',
  `service_params` text DEFAULT NULL COMMENT '服务参数',
  `gmt_create` timestamp NOT NULL COMMENT '创建时间',
  `gmt_modified` timestamp NOT NULL COMMENT '修改时间',
  `client_version` varchar(512) DEFAULT NULL COMMENT '客户端版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_data_center_revision` (`data_center`, `revision`) BLOCK_SIZE 16384 GLOBAL
) AUTO_INCREMENT = 913172 DEFAULT CHARSET = utf8mb4 ROW_FORMAT = DYNAMIC COMPRESSION = 'zstd_1.0' REPLICA_NUM = 3 BLOCK_SIZE = 16384 USE_BLOOM_FILTER = FALSE TABLET_SIZE = 134217728 PCTFREE = 10 COMMENT = '服务元数据表'

CREATE TABLE `interface_apps_index` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `data_center` varchar(128) NOT NULL COMMENT '集群名称',
  `app_name` varchar(128) NOT NULL COMMENT '应用名',
  `interface_name` varchar(386) NOT NULL COMMENT '接口名',
  `gmt_create` timestamp(6) NOT NULL COMMENT '创建时间',
  `gmt_modified` timestamp(6) NOT NULL COMMENT '修改时间',
  `hashcode` varchar(128) NOT NULL COMMENT '唯一索引hashcode',
  `reference` tinyint(4) NOT NULL COMMENT '是否被引用',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_hashcode` (`data_center`, `app_name`, `hashcode`) BLOCK_SIZE 16384 GLOBAL,
  KEY `idx_data_center_interface` (`interface_name`) BLOCK_SIZE 16384 GLOBAL
) AUTO_INCREMENT = 1469202 DEFAULT CHARSET = utf8mb4 ROW_FORMAT = DYNAMIC COMPRESSION = 'zstd_1.0' REPLICA_NUM = 3 BLOCK_SIZE = 16384 USE_BLOOM_FILTER = FALSE TABLET_SIZE = 134217728 PCTFREE = 10 COMMENT = 'interface与revision索引表'

CREATE TABLE `provide_data` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `data_center` varchar(128) NOT NULL COMMENT '集群名',
  `data_key` varchar(1024) NOT NULL COMMENT 'data key',
  `data_value` mediumtext DEFAULT NULL COMMENT 'data value',
  `gmt_create` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_data_center_key` (`data_center`(128), `data_key`(1024)) BLOCK_SIZE 16384 GLOBAL
) AUTO_INCREMENT = 1 DEFAULT CHARSET = utf8mb4 ROW_FORMAT = DYNAMIC COMPRESSION = 'zstd_1.0' REPLICA_NUM = 3 BLOCK_SIZE = 16384 USE_BLOOM_FILTER = FALSE TABLET_SIZE = 134217728 PCTFREE = 10 COMMENT = '注册中心配置存储表'

CREATE TABLE `distribute_lock` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `data_center` varchar(128) NOT NULL COMMENT '集群名',
  `lock_name` varchar(1024) NOT NULL COMMENT '分布式锁名称',
  `owner` varchar(512) NOT NULL COMMENT '锁拥有者',
  `duration` bigint(20) NOT NULL COMMENT '持续周期',
  `gmt_create` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `gmt_modified` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_data_center_lock` (`data_center`(128), `lock_name`(1024)) BLOCK_SIZE 16384 GLOBAL,
  KEY `idx_lock_owner` (`owner`(512)) BLOCK_SIZE 16384 GLOBAL
) AUTO_INCREMENT = 1 DEFAULT CHARSET = utf8mb4 ROW_FORMAT = DYNAMIC COMPRESSION = 'zstd_1.0' REPLICA_NUM = 3 BLOCK_SIZE = 16384 USE_BLOOM_FILTER = FALSE TABLET_SIZE = 134217728 PCTFREE = 10 COMMENT = '分布式锁'
















