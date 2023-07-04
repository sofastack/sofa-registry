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
package com.alipay.sofa.registry.server.session.multi.cluster;

import com.alipay.sofa.registry.common.model.multi.cluster.DataCenterMetadata;
import com.alipay.sofa.registry.common.model.multi.cluster.RemoteSlotTableStatus;
import java.util.Map;
import java.util.Set;

/**
 * @author xiaojian.xj
 * @version : DataCenterMetadataCache.java, v 0.1 2022年07月19日 20:09 xiaojian.xj Exp $
 */
public interface DataCenterMetadataCache {

  /**
   * get zones of dataCenter
   *
   * @param dataCenter dataCenter
   * @return Set
   */
  Set<String> dataCenterZonesOf(String dataCenter);

  Map<String, Set<String>> dataCenterZonesOf(Set<String> dataCenters);

  boolean saveDataCenterZones(Map<String, RemoteSlotTableStatus> remoteSlotTableStatus);

  Set<String> getSyncDataCenters();

  DataCenterMetadata metadataOf(String dataCenter);
}
