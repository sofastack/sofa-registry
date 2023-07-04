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

import com.alipay.sofa.registry.common.model.metaserver.MultiClusterSyncInfo;
import com.alipay.sofa.registry.common.model.multi.cluster.DataCenterMetadata;
import com.alipay.sofa.registry.common.model.multi.cluster.RemoteSlotTableStatus;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.store.api.meta.MultiClusterSyncRepository;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
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

  @Autowired private MultiClusterSyncRepository multiClusterSyncRepository;

  private Map<String, DataCenterMetadata> metadataCache = Maps.newConcurrentMap();

  @PostConstruct
  public void init() {
    // init local dataCenter zones
    metadataCache.put(
        sessionServerConfig.getSessionServerDataCenter(),
        new DataCenterMetadata(
            sessionServerConfig.getSessionServerDataCenter(),
            sessionServerConfig.getLocalDataCenterZones()));
  }

  /**
   * get zones of dataCenter
   *
   * @param dataCenter dataCenter
   * @return Set
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

    Set<String> set = Sets.newHashSet(remoteSlotTableStatus.keySet());
    set.add(sessionServerConfig.getSessionServerDataCenter());
    Set<String> tobeRemove = Sets.difference(metadataCache.keySet(), set);

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

      DataCenterMetadata exist = metadataCache.get(entry.getKey());
      if (!value.equals(exist)) {
        metadataCache.put(entry.getKey(), value.getDataCenterMetadata());
        LOGGER.info("[DataCenterMetadataCache]update from:{} to:{}", exist, value);
      }
    }

    processRemove(tobeRemove);
    return success;
  }

  private void processRemove(Set<String> tobeRemove) {
    if (CollectionUtils.isEmpty(tobeRemove)) {
      return;
    }
    Set<MultiClusterSyncInfo> syncInfos = multiClusterSyncRepository.queryLocalSyncInfos();
    Set<String> syncing =
        syncInfos.stream()
            .map(MultiClusterSyncInfo::getRemoteDataCenter)
            .collect(Collectors.toSet());
    for (String remove : tobeRemove) {
      if (syncing.contains(remove)) {
        LOGGER.error("dataCenter:{} remove metadata is forbidden.", remove);
        continue;
      }
      metadataCache.remove(remove);
      LOGGER.info("remove dataCenter:{} metadata success.", remove);
    }
  }

  @Override
  public Set<String> getSyncDataCenters() {
    return metadataCache.keySet();
  }

  @Override
  public DataCenterMetadata metadataOf(String dataCenter) {
    DataCenterMetadata metadata = metadataCache.get(dataCenter);
    return metadata;
  }

  /**
   * Setter method for property <tt>sessionServerConfig</tt>.
   *
   * @param sessionServerConfig value to be assigned to property sessionServerConfig
   * @return DataCenterMetadataCacheImpl
   */
  @VisibleForTesting
  public DataCenterMetadataCacheImpl setSessionServerConfig(
      SessionServerConfig sessionServerConfig) {
    this.sessionServerConfig = sessionServerConfig;
    return this;
  }
}
