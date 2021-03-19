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
package com.alipay.sofa.registry.server.meta.metaserver.impl;

import com.alipay.sofa.registry.common.model.metaserver.cluster.VersionedList;
import com.alipay.sofa.registry.common.model.metaserver.nodes.MetaNode;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.server.meta.lease.data.DataServerManager;
import com.alipay.sofa.registry.server.meta.lease.session.SessionServerManager;
import com.alipay.sofa.registry.server.meta.metaserver.CurrentDcMetaServer;
import com.alipay.sofa.registry.server.meta.slot.SlotManager;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author chen.zhu
 *     <p>Dec 14, 2020
 */
public class LocalMetaServer extends AbstractMetaServer implements CurrentDcMetaServer {

  private final AtomicLong currentEpoch = new AtomicLong();

  @Autowired protected SlotManager slotManager;

  @Autowired protected DataServerManager dataServerManager;

  @Autowired protected SessionServerManager sessionServerManager;

  public LocalMetaServer() {}

  public LocalMetaServer(
      SlotManager slotManager,
      DataServerManager dataServerManager,
      SessionServerManager sessionServerManager) {
    this.slotManager = slotManager;
    this.dataServerManager = dataServerManager;
    this.sessionServerManager = sessionServerManager;
  }

  @Override
  public SlotTable getSlotTable() {
    return slotManager.getSlotTable();
  }

  public long getEpoch() {
    return currentEpoch.get();
  }

  @Override
  public List<MetaNode> getClusterMembers() {
    return Lists.newArrayList(metaServers);
  }

  @Override
  public void updateClusterMembers(VersionedList<MetaNode> versionedMetaNodes) {
    if (versionedMetaNodes.getEpoch() <= currentEpoch.get()) {
      logger.warn(
          "[updateClusterMembers]Epoch[{}] is less than current[{}], ignore: {}",
          currentEpoch.get(),
          versionedMetaNodes.getEpoch(),
          versionedMetaNodes.getClusterMembers());
    }
    lock.writeLock().lock();
    try {
      logger.warn(
          "[updateClusterMembers] update meta-servers, \nprevious[{}]: {} \ncurrent[{}]: {}",
          currentEpoch.get(),
          getClusterMembers(),
          versionedMetaNodes.getEpoch(),
          versionedMetaNodes.getClusterMembers());
      currentEpoch.set(versionedMetaNodes.getEpoch());
      metaServers = Sets.newHashSet(versionedMetaNodes.getClusterMembers());
    } finally {
      lock.writeLock().unlock();
    }
  }

  @Override
  public DataServerManager getDataServerManager() {
    return dataServerManager;
  }

  @Override
  public SessionServerManager getSessionServerManager() {
    return sessionServerManager;
  }

  @Override
  public void renew(MetaNode metaNode) {
    lock.writeLock().lock();
    try {
      logger.info("[renew]meta node [{}] renewed", metaNode);
      metaServers.add(metaNode);
    } finally {
      lock.writeLock().unlock();
    }
  }

  @Override
  public void cancel(MetaNode renewal) {
    lock.writeLock().lock();
    try {
      logger.info("[cancel]meta node [{}] removed", renewal);
      metaServers.remove(renewal);
    } finally {
      lock.writeLock().unlock();
    }
  }

  @Override
  public VersionedList<MetaNode> getClusterMeta() {
    return new VersionedList<>(getEpoch(), getClusterMembers());
  }
}
