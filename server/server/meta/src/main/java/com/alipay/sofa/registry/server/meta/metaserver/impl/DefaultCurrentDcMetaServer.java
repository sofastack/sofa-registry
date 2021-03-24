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
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.exception.DisposeException;
import com.alipay.sofa.registry.exception.InitializeException;
import com.alipay.sofa.registry.lifecycle.impl.LifecycleHelper;
import com.alipay.sofa.registry.server.meta.bootstrap.config.NodeConfig;
import com.alipay.sofa.registry.server.meta.cluster.node.NodeAdded;
import com.alipay.sofa.registry.server.meta.cluster.node.NodeRemoved;
import com.alipay.sofa.registry.server.meta.lease.data.DataServerManager;
import com.alipay.sofa.registry.server.meta.lease.session.SessionServerManager;
import com.alipay.sofa.registry.server.meta.metaserver.CurrentDcMetaServer;
import com.alipay.sofa.registry.server.meta.slot.SlotManager;
import com.alipay.sofa.registry.util.DatumVersionUtil;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author chen.zhu
 *     <p>Nov 23, 2020
 */
@Component
public class DefaultCurrentDcMetaServer extends LocalMetaServer implements CurrentDcMetaServer {

  @Autowired private NodeConfig nodeConfig;

  public DefaultCurrentDcMetaServer() {}

  public DefaultCurrentDcMetaServer(
      SlotManager slotManager,
      DataServerManager dataServerManager,
      SessionServerManager sessionServerManager) {
    super(slotManager, dataServerManager, sessionServerManager);
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
  protected void doInitialize() throws InitializeException {
    super.doInitialize();
    initMetaServers();
  }

  private void initMetaServers() {
    Collection<String> metaIpAddresses =
        nodeConfig.getMetaNodeIP().get(nodeConfig.getLocalDataCenter());
    List<MetaNode> metaNodes = Lists.newArrayList();
    for (String ip : metaIpAddresses) {
      metaNodes.add(new MetaNode(new URL(ip), nodeConfig.getLocalDataCenter()));
    }
    updateClusterMembers(new VersionedList<>(DatumVersionUtil.nextId(), metaNodes));
  }

  @Override
  protected void doDispose() throws DisposeException {
    super.doDispose();
  }

  @Override
  public void renew(MetaNode metaNode) {
    super.renew(metaNode);
    notifyObservers(new NodeAdded<>(metaNode));
  }

  @Override
  public void cancel(MetaNode metaNode) {
    super.cancel(metaNode);
    notifyObservers(new NodeRemoved<MetaNode>(metaNode));
  }

  @VisibleForTesting
  DefaultCurrentDcMetaServer setSessionManager(SessionServerManager sessionServerManager) {
    this.sessionServerManager = sessionServerManager;
    return this;
  }

  @VisibleForTesting
  DefaultCurrentDcMetaServer setDataServerManager(DataServerManager dataServerManager) {
    this.dataServerManager = dataServerManager;
    return this;
  }

  @VisibleForTesting
  DefaultCurrentDcMetaServer setNodeConfig(NodeConfig nodeConfig) {
    this.nodeConfig = nodeConfig;
    return this;
  }

  @VisibleForTesting
  DefaultCurrentDcMetaServer setSlotManager(SlotManager slotManager) {
    this.slotManager = slotManager;
    return this;
  }
}
