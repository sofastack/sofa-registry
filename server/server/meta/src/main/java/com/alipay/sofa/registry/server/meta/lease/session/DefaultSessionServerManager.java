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
package com.alipay.sofa.registry.server.meta.lease.session;

import com.alipay.sofa.registry.common.model.metaserver.Lease;
import com.alipay.sofa.registry.common.model.metaserver.cluster.VersionedList;
import com.alipay.sofa.registry.common.model.metaserver.inter.heartbeat.HeartbeatRequest;
import com.alipay.sofa.registry.common.model.metaserver.nodes.SessionNode;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.lifecycle.impl.LifecycleHelper;
import com.alipay.sofa.registry.server.meta.MetaLeaderService;
import com.alipay.sofa.registry.server.meta.bootstrap.config.MetaServerConfig;
import com.alipay.sofa.registry.server.meta.cluster.node.NodeAdded;
import com.alipay.sofa.registry.server.meta.cluster.node.NodeModified;
import com.alipay.sofa.registry.server.meta.cluster.node.NodeRemoved;
import com.alipay.sofa.registry.server.meta.lease.impl.AbstractEvictableFilterableLeaseManager;
import com.alipay.sofa.registry.server.meta.monitor.Metrics;
import com.alipay.sofa.registry.server.meta.slot.SlotManager;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Objects;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author chen.zhu
 *     <p>Nov 24, 2020
 */
@Component
public class DefaultSessionServerManager
    extends AbstractEvictableFilterableLeaseManager<SessionNode> implements SessionServerManager {

  @Autowired private MetaServerConfig metaServerConfig;

  @Autowired private SlotManager slotManager;

  public DefaultSessionServerManager() {}

  public DefaultSessionServerManager(
      MetaServerConfig metaServerConfig,
      SlotManager slotManager,
      MetaLeaderService metaLeaderService) {
    this.metaServerConfig = metaServerConfig;
    this.slotManager = slotManager;
    this.metaLeaderService = metaLeaderService;
  }

  @PostConstruct
  public void postConstruct() throws Exception {
    LifecycleHelper.initializeIfPossible(this);
    LifecycleHelper.startIfPossible(this);
  }

  @PreDestroy
  public void preDestory() throws Exception {
    LifecycleHelper.stopIfPossible(this);
    LifecycleHelper.disposeIfPossible(this);
  }

  @Override
  public void register(Lease<SessionNode> lease) {
    super.register(lease);
    notifyObservers(new NodeAdded<>(lease.getRenewal()));
  }
  /**
   * Different from data server, session node maintains a 'ProcessId' to be as unique Id for Session
   * Process(not server)
   *
   * <p>Once a restart event happened on the same session-server, an notification will be sent
   */
  @Override
  public boolean renew(SessionNode renewal, int duration) {
    Metrics.Heartbeat.onSessionHeartbeat(renewal.getIp());
    Lease<SessionNode> lease = getLease(renewal);
    if (renewal.getProcessId() != null
        && lease != null
        && lease.getRenewal() != null
        && !Objects.equals(lease.getRenewal().getProcessId(), renewal.getProcessId())) {
      logger.warn(
          "[renew] session node is restart, as process-Id change from {} to {}",
          lease.getRenewal().getProcessId(),
          renewal.getProcessId());
      // replace the session node, as it has changed process-id already
      lease.setRenewal(renewal);
      super.register(new Lease<>(renewal, duration));
      notifyObservers(new NodeModified<>(lease.getRenewal(), renewal));
      return false;
    } else {
      return super.renew(renewal, duration);
    }
  }

  @Override
  public boolean cancel(Lease<SessionNode> lease) {
    boolean result = super.cancel(lease);
    if (result) {
      Metrics.Heartbeat.onSessionEvict(lease.getRenewal().getIp());
      notifyObservers(new NodeRemoved<>(lease.getRenewal()));
    }
    return result;
  }

  @Override
  protected int getIntervalMilli() {
    return metaServerConfig.getExpireCheckIntervalMillis();
  }

  @Override
  protected int getEvictBetweenMilli() {
    return metaServerConfig.getExpireCheckIntervalMillis();
  }

  @Override
  public VersionedList<SessionNode> getSessionServerMetaInfo() {
    VersionedList<Lease<SessionNode>> leaseMetaInfo = getLeaseMeta();
    List<SessionNode> sessionNodes = Lists.newArrayList();
    leaseMetaInfo
        .getClusterMembers()
        .forEach(
            lease -> {
              sessionNodes.add(lease.getRenewal());
            });
    return new VersionedList<>(leaseMetaInfo.getEpoch(), sessionNodes);
  }

  @Override
  public long getEpoch() {
    return currentEpoch.get();
  }

  @Override
  public void onHeartbeat(HeartbeatRequest<SessionNode> heartbeat) {
    if (amILeader() && !metaLeaderService.isWarmuped()) {
      learnFromSession(heartbeat);
    }
  }

  protected void learnFromSession(HeartbeatRequest<SessionNode> heartbeat) {
    SlotTable slotTable = heartbeat.getSlotTable();
    slotManager.refresh(slotTable);
  }
}
