drop table if exists APP_REVISION;
create table APP_REVISION (
  id bigint unsigned not null auto_increment primary key,
  data_center varchar(129) not null,
  revision varchar(128) not null,
  app_name varchar(128) not null,
  base_params text default null,
  service_params text default null,
  gmt_create timestamp not null,
  gmt_modified timestamp not null,
  client_version varchar(512) default null,
  UNIQUE KEY `uk_data_center_revision` (`data_center`, `revision`)
);

drop table if exists interface_apps_index;
CREATE TABLE interface_apps_index (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  data_center varchar(128) NOT NULL,
  app_name varchar(128) NOT NULL COMMENT '应用名',
  interface_name varchar(386) NOT NULL COMMENT '接口名',
  gmt_create timestamp(6) NOT NULL COMMENT '创建时间',
  gmt_modified timestamp(6) NOT NULL COMMENT '修改时间',
  hashcode varchar(128) NOT NULL COMMENT '唯一索引hashcode',
  reference tinyint(4) NOT NULL COMMENT '是否被引用',
  PRIMARY KEY(id),
  UNIQUE KEY `uk_hashcode` (`data_center`, `app_name`, `hashcode`),
  KEY `idx_data_center_interface` (`interface_name`)
);

drop table if exists distribute_lock;
CREATE TABLE distribute_lock (
  id bigint(20) NOT NULL AUTO_INCREMENT primary key,
  data_center varchar(128) NOT NULL,
  lock_name varchar(1024) NOT NULL,
  owner varchar(512) NOT NULL,
  duration bigint(20) NOT NULL,
  gmt_create timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  gmt_modified timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_data_center_lock` (`data_center`, `lock_name`),
  KEY `idx_lock_owner` (`owner`)
);

drop table if exists provide_data;
CREATE TABLE provide_data (
  id bigint(20) NOT NULL AUTO_INCREMENT primary key,
  data_center varchar(128) NOT NULL,
  data_key varchar(1024) NOT NULL,
  data_value mediumtext DEFAULT NULL ,
  gmt_create timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  gmt_modified timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `data_version` bigint(20) NOT NULL DEFAULT '0' COMMENT '版本号',
  UNIQUE KEY `uk_data_center_key` (`data_center`, `data_key`)
);











