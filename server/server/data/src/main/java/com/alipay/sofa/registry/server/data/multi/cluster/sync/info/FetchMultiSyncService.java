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
package com.alipay.sofa.registry.server.data.multi.cluster.sync.info;

import com.alipay.sofa.registry.common.model.console.MultiSegmentSyncSwitch;
import com.alipay.sofa.registry.common.model.metaserver.MultiClusterSyncInfo;
import com.alipay.sofa.registry.common.model.slot.filter.MultiSyncDataAcceptorManager;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.data.bootstrap.MultiClusterDataServerConfig;
import com.alipay.sofa.registry.store.api.meta.MultiClusterSyncRepository;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.WakeUpLoopRunnable;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.util.CollectionUtils;

/**
 * @author xiaojian.xj
 * @version : FetchMultiSyncService.java, v 0.1 2022年07月20日 15:27 xiaojian.xj Exp $
 */
public class FetchMultiSyncService implements ApplicationListener<ContextRefreshedEvent> {

  private static final Logger LOGGER = LoggerFactory.getLogger(FetchMultiSyncService.class);

  @Autowired private MultiClusterDataServerConfig multiClusterDataServerConfig;

  @Autowired private MultiClusterSyncRepository multiClusterSyncRepository;

  @Autowired private MultiSyncDataAcceptorManager multiSyncDataAcceptorManager;

  private final Worker worker = new Worker();

  private Map<String, MultiSegmentSyncSwitch> syncMap = Maps.newHashMap();

  private static final boolean INIT = false;

  public synchronized boolean multiSync(String dataCenter) {
    MultiSegmentSyncSwitch multiSegmentSyncSwitch = syncMap.get(dataCenter);
    if (multiSegmentSyncSwitch == null) {
      return INIT;
    }

    return multiSegmentSyncSwitch.isMultiSync();
  }

  public synchronized boolean multiPush(String dataCenter) {
    MultiSegmentSyncSwitch multiSegmentSyncSwitch = syncMap.get(dataCenter);
    if (multiSegmentSyncSwitch == null) {
      return INIT;
    }

    return multiSegmentSyncSwitch.isMultiPush();
  }

  public synchronized MultiSegmentSyncSwitch getMultiSyncSwitch(String dataCenter) {
    return syncMap.get(dataCenter);
  }

  public synchronized void setSyncMap(Map<String, MultiSegmentSyncSwitch> syncMap) {
    this.syncMap = syncMap;
  }

  private synchronized Set<String> syncingDataCenter() {
    return syncMap.keySet();
  }

  /**
   * Handle an application event.
   *
   * @param event the event to respond to
   */
  @Override
  public void onApplicationEvent(ContextRefreshedEvent event) {
    start();
  }

  public void start() {
    ConcurrentUtils.createDaemonThread("FetchMultiSyncInfo-Worker", worker).start();
  }

  private class Worker extends WakeUpLoopRunnable {

    @Override
    public void runUnthrowable() {
      Set<MultiClusterSyncInfo> multiClusterSyncInfos =
          multiClusterSyncRepository.queryLocalSyncInfos();

      boolean change = false;
      Map<String, MultiSegmentSyncSwitch> syncMap =
          Maps.newHashMapWithExpectedSize(multiClusterSyncInfos.size());
      for (MultiClusterSyncInfo multiClusterSyncInfo : multiClusterSyncInfos) {
        MultiSegmentSyncSwitch exist =
            getMultiSyncSwitch(multiClusterSyncInfo.getRemoteDataCenter());
        if (exist != null && exist.getDataVersion() > multiClusterSyncInfo.getDataVersion()) {
          LOGGER.error(
              "[FetchMultiSyncService]load config error, exist:{}, load:{}",
              exist,
              multiClusterSyncInfo);
          return;
        }

        if (exist == null || multiClusterSyncInfo.getDataVersion() > exist.getDataVersion()) {
          LOGGER.info(
              "[FetchMultiSyncService.addOrUpdate]dataCenter:{},remoteDataCenter:{}, config update from:{} to {}",
              multiClusterSyncInfo.getDataCenter(),
              multiClusterSyncInfo.getRemoteDataCenter(),
              exist,
              multiClusterSyncInfo);
          change = true;
        }
        syncMap.put(multiClusterSyncInfo.getRemoteDataCenter(), from(multiClusterSyncInfo));
      }

      Set<String> remove = Sets.difference(syncingDataCenter(), syncMap.keySet());
      if (!CollectionUtils.isEmpty(remove)) {
        change = true;
        LOGGER.info("[FetchMultiSyncService.remove]remove dataCenters:{}", remove);
      }
      if (change) {
        multiSyncDataAcceptorManager.updateFrom(syncMap.values());
        setSyncMap(syncMap);
      }
    }

    @Override
    public int getWaitingMillis() {
      return multiClusterDataServerConfig.getMultiClusterConfigReloadMillis();
    }
  }

  private MultiSegmentSyncSwitch from(MultiClusterSyncInfo multiClusterSyncInfo) {

    return new MultiSegmentSyncSwitch(
        multiClusterSyncInfo.isEnableSyncDatum(),
        multiClusterSyncInfo.isEnablePush(),
        multiClusterSyncInfo.getRemoteDataCenter(),
        multiClusterSyncInfo.getSynPublisherGroups(),
        multiClusterSyncInfo.getSyncDataInfoIds(),
        multiClusterSyncInfo.getIgnoreDataInfoIds(),
        multiClusterSyncInfo.getDataVersion());
  }
}
