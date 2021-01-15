CREATE TABLE `app_revision` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `data_center` varchar(512) NOT NULL COMMENT '集群名',
  `revision` varchar(512) NOT NULL COMMENT 'revision',
  `app_name` varchar(512) NOT NULL COMMENT '应用名',
  `base_params` longtext COMMENT '基础参数',
  `service_params` longtext COMMENT '服务参数',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `client_version` varchar(512) DEFAULT NULL COMMENT '客户端版本',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_data_center_revision` (`data_center`,`revision`)
) ENGINE=InnoDB AUTO_INCREMENT=52570 DEFAULT CHARSET=gbk COMMENT='服务元数据表'

CREATE TABLE `interface_revision_index` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `data_center` varchar(512) NOT NULL COMMENT '集群名称',
  `app_name` varchar(512) NOT NULL COMMENT '应用名',
  `interface_name` text NOT NULL COMMENT '接口名',
  `revision` varchar(512) NOT NULL COMMENT 'revision',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_data_center_app` (`data_center`,`app_name`)
) ENGINE=InnoDB AUTO_INCREMENT=105064 DEFAULT CHARSET=gbk COMMENT='interface与revision索引表'













