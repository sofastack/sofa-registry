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
package com.alipay.sofa.registry.server.shared.meta;

import com.alipay.sofa.registry.common.model.ProcessId;
import com.alipay.sofa.registry.common.model.metaserver.FetchSystemPropertyResult;
import com.alipay.sofa.registry.common.model.metaserver.ProvideData;
import com.alipay.sofa.registry.common.model.metaserver.SlotTableChangeEvent;
import java.util.List;
import java.util.Set;

/**
 * @author yuzhi.lyz
 * @version v 0.1 2020-11-28 15:19 yuzhi.lyz Exp $
 */
public interface MetaServerService {
  /** Start renewer. */
  void startRenewer();

  /** suspend renewer. */
  void suspendRenewer();

  /** resume renewer. */
  void resumeRenewer();

  /** update data server expireTime */
  void renewNode();

  /**
   * Handle slot table change boolean.
   *
   * @param event the event
   * @return the boolean
   */
  boolean handleSlotTableChange(SlotTableChangeEvent event);

  /**
   * get provider data
   *
   * @param dataInfoId
   * @return
   */
  ProvideData fetchData(String dataInfoId);

  /** Add self to meta blacklist. */
  void addSelfToMetaBlacklist();

  /** Remove self from meta blacklist. */
  void removeSelfFromMetaBlacklist();

  /**
   * @param zonename zone is null, get all session
   * @return
   */
  List<String> getSessionServerList(String zonename);

  Set<ProcessId> getSessionProcessIds();
  /**
   * Gets get data server list.
   *
   * @return the get data server list
   */
  Set<String> getDataServerList();

  String getMetaServerLeader();

  /**
   * Gets get session server epoch.
   *
   * @return the get session server epoch
   */
  long getSessionServerEpoch();

  /**
   * get all datacenters
   *
   * @return
   */
  Set<String> getDataCenters();

  FetchSystemPropertyResult fetchSystemProperty(String dataInfoId, long currentVersion);
}
