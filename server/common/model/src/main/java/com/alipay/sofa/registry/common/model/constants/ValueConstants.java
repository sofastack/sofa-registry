/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alipay.sofa.registry.common.model.constants;

import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.util.SystemUtils;

/**
 * @author zhuoyu.sjw
 * @version $Id: ValueConstants.java, v 0.1 2018-03-28 23:07 zhuoyu.sjw Exp $$
 */
public class ValueConstants {

  /** connectId: sourceAddress_targetAddress */
  public static final String CONNECT_ID_SPLIT = "_";

  /** The constant DEFAULT_GROUP. */
  public static final String DEFAULT_GROUP = "DEFAULT_GROUP";

  /** The constant DEFAULT_ZONE. */
  public static final String DEFAULT_ZONE = "DEFAULT_ZONE";

  public static final String DEFAULT_INSTANCE_ID = "DEFAULT_INSTANCE_ID";

  /** The constant DEFAULT_DATA_CENTER. */
  public static final String DEFAULT_DATA_CENTER = "DefaultDataCenter";

  public static final long DEFAULT_NO_DATUM_VERSION = 1L;

  public static final String ALL_DATACENTER = "ALL_DATACENTER";

  private static final Integer SYSTEM_RAFT_PORT = Integer.getInteger("RAFT_SERVER_PORT");

  public static final int RAFT_SERVER_PORT = SYSTEM_RAFT_PORT != null ? SYSTEM_RAFT_PORT : 9614;

  private static final String SYSTEM_RAFT_GROUP = System.getProperty("RAFT_SERVER_GROUP");

  public static final String RAFT_SERVER_GROUP =
      SYSTEM_RAFT_GROUP != null ? SYSTEM_RAFT_GROUP : "RegistryGroup";

  public static final String SESSION_PROVIDE_DATA_GROUP = "CONFIG";
  public static final String SESSION_PROVIDE_DATA_INSTANCE_ID = "9600";

  public static final String STOP_PUSH_DATA_SWITCH_DATA_ID =
      DataInfo.toDataInfoId(
          "session.stop.push.data.switch",
          SESSION_PROVIDE_DATA_INSTANCE_ID,
          SESSION_PROVIDE_DATA_GROUP);

  public static final String PUSH_SWITCH_GRAY_OPEN_DATA_ID =
      DataInfo.toDataInfoId(
          "session.push.switch.gray.open",
          SESSION_PROVIDE_DATA_INSTANCE_ID,
          SESSION_PROVIDE_DATA_GROUP);

  public static final String SHUTDOWN_SWITCH_DATA_ID =
      DataInfo.toDataInfoId(
          "session.shutdown.switch", SESSION_PROVIDE_DATA_INSTANCE_ID, SESSION_PROVIDE_DATA_GROUP);

  public static final String BLACK_LIST_DATA_ID =
      DataInfo.toDataInfoId(
          "session.blacklist.data", SESSION_PROVIDE_DATA_INSTANCE_ID, SESSION_PROVIDE_DATA_GROUP);

  public static final String CLIENT_OFF_ADDRESS_DATA_ID =
      DataInfo.toDataInfoId(
          "registry.client.off.list", SESSION_PROVIDE_DATA_INSTANCE_ID, SESSION_PROVIDE_DATA_GROUP);

  public static final String CIRCUIT_BREAKER_DATA_ID =
      DataInfo.toDataInfoId(
          "registry.circuit.breaker", SESSION_PROVIDE_DATA_INSTANCE_ID, SESSION_PROVIDE_DATA_GROUP);

  // registry node quit interceptor disable
  public static final String NODE_QUIT_INTERCEPTOR_DISABLE_DATA_ID =
      DataInfo.toDataInfoId(
          "registry.node.quit.interceptor.disable",
          SESSION_PROVIDE_DATA_INSTANCE_ID,
          SESSION_PROVIDE_DATA_GROUP);

  // make interceptor(check meta leader status) disable
  public static final String META_LEADER_INTERCEPTOR_DISABLE_DATA_ID =
      DataInfo.toDataInfoId(
          "registry.meta.leader.interceptor.disable",
          SESSION_PROVIDE_DATA_INSTANCE_ID,
          SESSION_PROVIDE_DATA_GROUP);

  // make interceptor(check data capacity) disable
  public static final String DATA_CAPACITY_INTERCEPTOR_DISABLE_DATA_ID =
      DataInfo.toDataInfoId(
          "registry.data.capacity.interceptor.disable",
          SESSION_PROVIDE_DATA_INSTANCE_ID,
          SESSION_PROVIDE_DATA_GROUP);

  // make interceptor(check session capacity) disable
  public static final String SESSION_CAPACITY_INTERCEPTOR_DISABLE_DATA_ID =
      DataInfo.toDataInfoId(
          "registry.session.capacity.interceptor.disable",
          SESSION_PROVIDE_DATA_INSTANCE_ID,
          SESSION_PROVIDE_DATA_GROUP);

  public static final String DATA_DATUM_SYNC_SESSION_INTERVAL_SEC =
      DataInfo.toDataInfoId(
          "data.datum.sync.session.interval.sec",
          SESSION_PROVIDE_DATA_INSTANCE_ID,
          SESSION_PROVIDE_DATA_GROUP);

  public static final String DATA_SESSION_LEASE_SEC =
      DataInfo.toDataInfoId(
          "data.session.lease.sec", SESSION_PROVIDE_DATA_INSTANCE_ID, SESSION_PROVIDE_DATA_GROUP);

  public static final String REGISTRY_SERVER_BLACK_LIST_DATA_ID =
      DataInfo.toDataInfoId(
          "registry.server.black.list",
          SESSION_PROVIDE_DATA_INSTANCE_ID,
          SESSION_PROVIDE_DATA_GROUP);

  public static final String NODE_SERVER_OPERATING_DATA_ID =
      DataInfo.toDataInfoId(
          "registry.node.server.operating.data",
          SESSION_PROVIDE_DATA_INSTANCE_ID,
          SESSION_PROVIDE_DATA_GROUP);

  public static final String APP_REVISION_CLEANER_ENABLED_DATA_ID =
      DataInfo.toDataInfoId(
          "app_revision.cleaner.enabled",
          SESSION_PROVIDE_DATA_INSTANCE_ID,
          SESSION_PROVIDE_DATA_GROUP);
  public static final String COMPRESS_PUSH_SWITCH_DATA_ID =
      DataInfo.toDataInfoId(
          "compress.push.switch", SESSION_PROVIDE_DATA_INSTANCE_ID, SESSION_PROVIDE_DATA_GROUP);
  public static final String COMPRESS_DATUM_SWITCH_DATA_ID =
      DataInfo.toDataInfoId(
          "compress.datum.switch", SESSION_PROVIDE_DATA_INSTANCE_ID, SESSION_PROVIDE_DATA_GROUP);

  public static final String APP_REVISION_WRITE_SWITCH_DATA_ID =
      DataInfo.toDataInfoId(
          "app_revision.write.switch",
          SESSION_PROVIDE_DATA_INSTANCE_ID,
          SESSION_PROVIDE_DATA_GROUP);

  public static final String CHANGE_PUSH_TASK_DELAY_CONFIG_DATA_ID =
      DataInfo.toDataInfoId(
          "change_push_task.delay.config",
          SESSION_PROVIDE_DATA_INSTANCE_ID,
          SESSION_PROVIDE_DATA_GROUP);

  public static final String DISABLE_DATA_ID_CASE_SENSITIVE_SWITCH =
      "disable.dataId.case.sensitive";

  /**
   * switch for dataId sensitive is disable or not, default value is false which means dataId is
   * case sensitive
   */
  public static final boolean DISABLE_DATA_ID_CASE_SENSITIVE =
      Boolean.parseBoolean(SystemUtils.getSystem(DISABLE_DATA_ID_CASE_SENSITIVE_SWITCH));

  // response status code
  public static final int METADATA_STATUS_PROCESS_SUCCESS = 200;
  public static final int METADATA_STATUS_DATA_NOT_FOUND = 404;
  public static final int METADATA_STATUS_METHOD_NOT_ALLOW = 405;
  public static final int METADATA_STATUS_PROCESS_ERROR = 500;

  public static final String ATTR_RPC_CHANNEL_PROCESS_ID = "attr.registry.rpc.channel.processId";

  public static final String CLIENT_OFF = "clientOff";

  public static final String CLIENT_OPEN = "clientOpen";

  public static final String REDUCE = "reduce";

  public static final String BLOCKED_REQUEST_KEY = "!Blocked";

  public static final String META_LEADER_QUERY_URL = "http://%s:9615/meta/leader/query";
}
