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
package com.alipay.sofa.registry.server.meta.lease.data;

import com.alipay.sofa.registry.common.model.metaserver.Lease;
import com.alipay.sofa.registry.common.model.metaserver.cluster.VersionedList;
import com.alipay.sofa.registry.common.model.metaserver.inter.heartbeat.HeartbeatRequest;
import com.alipay.sofa.registry.common.model.metaserver.nodes.DataNode;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.lifecycle.impl.LifecycleHelper;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.meta.MetaLeaderService;
import com.alipay.sofa.registry.server.meta.bootstrap.config.MetaServerConfig;
import com.alipay.sofa.registry.server.meta.cluster.node.NodeAdded;
import com.alipay.sofa.registry.server.meta.cluster.node.NodeRemoved;
import com.alipay.sofa.registry.server.meta.lease.impl.AbstractEvictableFilterableLeaseManager;
import com.alipay.sofa.registry.server.meta.monitor.Metrics;
import com.alipay.sofa.registry.server.meta.monitor.data.DataServerStats;
import com.alipay.sofa.registry.server.meta.slot.SlotManager;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author chen.zhu
 *     <p>Nov 24, 2020
 */
@Component
public class DefaultDataServerManager extends AbstractEvictableFilterableLeaseManager<DataNode>
    implements DataServerManager {

  private static final Logger logger = LoggerFactory.getLogger(DefaultDataServerManager.class);

  @Autowired private MetaServerConfig metaServerConfig;

  @Autowired private SlotManager slotManager;

  private final Map<String, DataServerStats> dataServerStatses = Maps.newConcurrentMap();

  /** Constructor. */
  public DefaultDataServerManager() {}

  /**
   * Constructor.
   *
   * @param metaServerConfig the meta server config
   * @param metaLeaderService metaLeaderService
   */
  public DefaultDataServerManager(
      MetaServerConfig metaServerConfig, MetaLeaderService metaLeaderService) {
    this.metaServerConfig = metaServerConfig;
    this.metaLeaderService = metaLeaderService;
  }

  /**
   * Post construct.
   *
   * @throws Exception the exception
   */
  @PostConstruct
  public void postConstruct() throws Exception {
    LifecycleHelper.initializeIfPossible(this);
    LifecycleHelper.startIfPossible(this);
  }

  /**
   * Pre destory.
   *
   * @throws Exception the exception
   */
  @PreDestroy
  public void preDestory() throws Exception {
    LifecycleHelper.stopIfPossible(this);
    LifecycleHelper.disposeIfPossible(this);
  }

  @Override
  public void register(Lease<DataNode> lease) {
    super.register(lease);
    notifyObservers(new NodeAdded<>(lease.getRenewal()));
  }

  @Override
  public boolean cancel(Lease<DataNode> lease) {
    boolean result = super.cancel(lease);
    if (result) {
      notifyObservers(new NodeRemoved<>(lease.getRenewal()));
      Metrics.Heartbeat.onDataEvict(lease.getRenewal().getIp());
    }
    return result;
  }

  @Override
  public boolean renew(DataNode renewal, int leaseDuration) {
    Metrics.Heartbeat.onDataHeartbeat(renewal.getIp());
    return super.renew(renewal, leaseDuration);
  }

  @Override
  protected int getIntervalMilli() {
    return metaServerConfig.getExpireCheckIntervalMillis();
  }

  @Override
  protected int getEvictBetweenMilli() {
    return metaServerConfig.getExpireCheckIntervalMillis();
  }

  @VisibleForTesting
  DefaultDataServerManager setMetaServerConfig(MetaServerConfig metaServerConfig) {
    this.metaServerConfig = metaServerConfig;
    return this;
  }

  @VisibleForTesting
  DefaultDataServerManager setSlotManager(SlotManager slotManager) {
    this.slotManager = slotManager;
    return this;
  }

  /**
   * To string string.
   *
   * @return the string
   */
  @Override
  public String toString() {
    return "DefaultDataServerManager";
  }

  /**
   * On heartbeat.
   *
   * @param heartbeat the heartbeat
   */
  @Override
  public void onHeartbeat(HeartbeatRequest<DataNode> heartbeat) {
    String dataServer = heartbeat.getNode().getIp();
    dataServerStatses.put(
        dataServer,
        new DataServerStats(dataServer, heartbeat.getSlotTableEpoch(), heartbeat.getSlotStatus()));
    learnFromData(heartbeat);
  }

  protected void learnFromData(HeartbeatRequest<DataNode> heartbeat) {
    if (!amILeader()) {
      logger.info("data server heartbeat on follower.leader is:{}", metaLeaderService.getLeader());
      return;
    }

    if (!metaLeaderService.isWarmuped()) {
      logger.info("leader:{} is warming up.", metaLeaderService.getLeader());
      return;
    }

    if (heartbeat.getSlotTable() == null) {
      logger.info("data server:{} heartbeat slotTable is null.", heartbeat.getNode().getIp());
      return;
    }

    SlotTable slotTable = heartbeat.getSlotTable();
    slotManager.refresh(slotTable);
  }

  @Override
  public List<DataServerStats> getDataServersStats() {
    return Collections.unmodifiableList(Lists.newLinkedList(dataServerStatses.values()));
  }

  @Override
  public VersionedList<DataNode> getDataServerMetaInfo() {
    VersionedList<Lease<DataNode>> leaseMetaInfo = getLeaseMeta();
    List<DataNode> dataNodes = Lists.newArrayList();
    leaseMetaInfo
        .getClusterMembers()
        .forEach(
            lease -> {
              dataNodes.add(lease.getRenewal());
            });
    return new VersionedList<>(leaseMetaInfo.getEpoch(), dataNodes);
  }

  @Override
  public long getEpoch() {
    return currentEpoch.get();
  }

  public MetaServerConfig getMetaServerConfig() {
    return metaServerConfig;
  }
}
