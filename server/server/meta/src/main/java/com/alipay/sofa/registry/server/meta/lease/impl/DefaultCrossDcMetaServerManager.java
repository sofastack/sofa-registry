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
package com.alipay.sofa.registry.server.meta.lease.impl;

import static com.alipay.sofa.registry.server.meta.bootstrap.MetaServerConfiguration.GLOBAL_EXECUTOR;
import static com.alipay.sofa.registry.server.meta.bootstrap.MetaServerConfiguration.SHARED_SCHEDULE_EXECUTOR;

import com.alipay.sofa.registry.exception.DisposeException;
import com.alipay.sofa.registry.exception.InitializeException;
import com.alipay.sofa.registry.exception.SofaRegistryRuntimeException;
import com.alipay.sofa.registry.exception.StartException;
import com.alipay.sofa.registry.exception.StopException;
import com.alipay.sofa.registry.lifecycle.impl.AbstractLifecycle;
import com.alipay.sofa.registry.lifecycle.impl.LifecycleHelper;
import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.server.meta.MetaLeaderService;
import com.alipay.sofa.registry.server.meta.bootstrap.config.MetaServerConfig;
import com.alipay.sofa.registry.server.meta.bootstrap.config.NodeConfig;
import com.alipay.sofa.registry.server.meta.metaserver.CrossDcMetaServer;
import com.alipay.sofa.registry.server.meta.metaserver.CrossDcMetaServerManager;
import com.alipay.sofa.registry.server.meta.metaserver.impl.DefaultCrossDcMetaServer;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author chen.zhu
 *     <p>Nov 23, 2020
 */
/**
 * Unique entrance for all "Cross-Dc-MetaServer" instances As while as a LeaderAware object, which
 * watches leader event and trigger cross-dc-metaservers' refresh job when leader term and stop all
 * of them when it's not a raft-cluster leader
 */
public class DefaultCrossDcMetaServerManager extends AbstractLifecycle
    implements CrossDcMetaServerManager {

  /** Map[DataCenter(String), CrossDcMetaServer] */
  private ConcurrentMap<String, CrossDcMetaServer> crossDcMetaServers = Maps.newConcurrentMap();

  @Autowired private NodeConfig nodeConfig;

  @Autowired private MetaServerConfig metaServerConfig;

  @Autowired MetaLeaderService metaLeaderService;

  @Resource(name = SHARED_SCHEDULE_EXECUTOR)
  private ScheduledExecutorService scheduled;

  @Resource(name = GLOBAL_EXECUTOR)
  private ExecutorService executors;

  // TODO the ref cause circular dependency
  @Autowired private Exchange boltExchange;

  @PostConstruct
  public void postConstruct() throws Exception {
    LifecycleHelper.initializeIfPossible(this);
  }

  @PreDestroy
  public void preDestory() throws Exception {
    LifecycleHelper.stopIfPossible(this);
    LifecycleHelper.disposeIfPossible(this);
  }

  @Override
  public CrossDcMetaServer getOrCreate(String dcName) {
    CrossDcMetaServer metaServer = crossDcMetaServers.get(dcName);
    if (metaServer == null) {
      synchronized (this) {
        metaServer = crossDcMetaServers.get(dcName);
        if (metaServer == null) {
          metaServer =
              new DefaultCrossDcMetaServer(
                  dcName,
                  nodeConfig.getDataCenterMetaServers(dcName),
                  scheduled,
                  boltExchange,
                  metaLeaderService,
                  metaServerConfig);
          try {
            LifecycleHelper.initializeIfPossible(metaServer);
          } catch (Throwable e) {
            logger.error(
                "[getOrCreate][{}]Cross-Dc-MetaServer create err, stop register to map", dcName, e);
            throw new SofaRegistryRuntimeException(e);
          }
          crossDcMetaServers.put(dcName, metaServer);
        }
      }
    }
    return metaServer;
  }

  @Override
  protected void doInitialize() throws InitializeException {
    super.doInitialize();
    for (Map.Entry<String, Collection<String>> entry : nodeConfig.getMetaNodeIP().entrySet()) {
      if (entry.getKey().equalsIgnoreCase(nodeConfig.getLocalDataCenter())) {
        continue;
      }
      getOrCreate(entry.getKey());
    }
  }

  @Override
  protected void doStart() throws StartException {
    super.doStart();
    new ConcurrentUtils.SafeParaLoop<CrossDcMetaServer>(executors, crossDcMetaServers.values()) {
      @Override
      protected void doRun0(CrossDcMetaServer metaServer) throws Exception {
        LifecycleHelper.startIfPossible(metaServer);
      }
    }.run();
  }

  @Override
  protected void doStop() throws StopException {
    new ConcurrentUtils.SafeParaLoop<CrossDcMetaServer>(executors, crossDcMetaServers.values()) {
      @Override
      protected void doRun0(CrossDcMetaServer metaServer) throws Exception {
        LifecycleHelper.stopIfPossible(metaServer);
      }
    }.run();
    super.doStop();
  }

  @Override
  protected void doDispose() throws DisposeException {
    this.crossDcMetaServers = Maps.newConcurrentMap();
    super.doDispose();
  }

  @VisibleForTesting
  DefaultCrossDcMetaServerManager setNodeConfig(NodeConfig nodeConfig) {
    this.nodeConfig = nodeConfig;
    return this;
  }

  @VisibleForTesting
  DefaultCrossDcMetaServerManager setMetaServerConfig(MetaServerConfig metaServerConfig) {
    this.metaServerConfig = metaServerConfig;
    return this;
  }

  @VisibleForTesting
  DefaultCrossDcMetaServerManager setScheduled(ScheduledExecutorService scheduled) {
    this.scheduled = scheduled;
    return this;
  }

  @VisibleForTesting
  DefaultCrossDcMetaServerManager setExecutors(ExecutorService executors) {
    this.executors = executors;
    return this;
  }

  @VisibleForTesting
  DefaultCrossDcMetaServerManager setExchange(Exchange boltExchange) {
    this.boltExchange = boltExchange;
    return this;
  }

  @VisibleForTesting
  ConcurrentMap<String, CrossDcMetaServer> getCrossDcMetaServers() {
    return crossDcMetaServers;
  }

  @Override
  public void becomeLeader() {
    try {
      LifecycleHelper.startIfPossible(this);
    } catch (Throwable th) {
      logger.error("[becomeLeader]", th);
    }
  }

  @Override
  public void loseLeader() {
    try {
      LifecycleHelper.stopIfPossible(this);
    } catch (Throwable th) {
      logger.error("[loseLeader]", th);
    }
  }
}
