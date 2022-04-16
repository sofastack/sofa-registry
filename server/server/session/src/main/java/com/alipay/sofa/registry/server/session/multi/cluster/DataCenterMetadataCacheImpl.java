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
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.providedata.FetchStopPushService;
import com.google.common.collect.Maps;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * @author xiaojian.xj
 * @version : SegmentMetadataCache.java, v 0.1 2022年07月19日 20:08 xiaojian.xj Exp $
 */
public class DataCenterMetadataCacheImpl implements DataCenterMetadataCache {

  private static final Logger LOGGER = LoggerFactory.getLogger(DataCenterMetadataCacheImpl.class);

  @Autowired private SessionServerConfig sessionServerConfig;

  @Autowired private FetchStopPushService fetchStopPushService;

  private Map<String, DataCenterMetadata> metadataCache = Maps.newConcurrentMap();

  @PostConstruct
  public void init() {
    // init local dataCenter zones
    metadataCache.put(
        sessionServerConfig.getSessionServerDataCenter(),
        new DataCenterMetadata(
            sessionServerConfig.getSessionServerDataCenter(),
            fetchStopPushService.isStopPushSwitch(),
            sessionServerConfig.getLocalDataCenterZones()));
  }

  /**
   * get zones of dataCenter
   *
   * @param dataCenter
   * @return
   */
  @Override
  public Set<String> dataCenterZonesOf(String dataCenter) {
    DataCenterMetadata metadata = metadataCache.get(dataCenter);

    if (metadata == null) {
      return null;
    }
    return metadata.getZones();
  }

  @Override
  public Boolean isStopPush(String dataCenter) {
    DataCenterMetadata metadata = metadataCache.get(dataCenter);

    if (metadata == null) {
      return null;
    }
    return metadata.isStopPush();
  }

  @Override
  public Map<String, Set<String>> dataCenterZonesOf(Set<String> dataCenters) {
    if (CollectionUtils.isEmpty(dataCenters)) {
      return Collections.EMPTY_MAP;
    }

    Map<String, Set<String>> ret = Maps.newHashMapWithExpectedSize(dataCenters.size());
    for (String dataCenter : dataCenters) {
      DataCenterMetadata metadata = metadataCache.get(dataCenter);
      if (metadata == null || CollectionUtils.isEmpty(metadata.getZones())) {
        LOGGER.error("[DataCenterMetadataCache]find dataCenter: {} zones error.", dataCenter);
        continue;
      }
      ret.put(dataCenter, metadata.getZones());
    }
    return ret;
  }

  @Override
  public boolean saveDataCenterZones(Map<String, RemoteSlotTableStatus> remoteSlotTableStatus) {

    boolean success = true;
    for (Entry<String, RemoteSlotTableStatus> entry :
        Optional.ofNullable(remoteSlotTableStatus).orElse(Maps.newHashMap()).entrySet()) {

      RemoteSlotTableStatus value = entry.getValue();
      if (StringUtils.isEmpty(entry.getKey())
          || value.getDataCenterMetadata() == null
          || CollectionUtils.isEmpty(value.getDataCenterMetadata().getZones())) {
        LOGGER.error(
            "[DataCenterMetadataCache]invalidate dataCenter: {} or metadata: {}",
            entry.getKey(),
            value);
        success = false;
        continue;
      }
      metadataCache.put(entry.getKey(), value.getDataCenterMetadata());
    }
    return success;
  }

  @Override
  public void updateLocalData(boolean stopPush) {
    metadataCache.put(
        sessionServerConfig.getSessionServerDataCenter(),
        new DataCenterMetadata(
            sessionServerConfig.getSessionServerDataCenter(),
            stopPush,
            sessionServerConfig.getLocalDataCenterZones()));
  }

  @Override
  public Set<String> getSyncDataCenters() {
    return metadataCache.keySet();
  }
}
