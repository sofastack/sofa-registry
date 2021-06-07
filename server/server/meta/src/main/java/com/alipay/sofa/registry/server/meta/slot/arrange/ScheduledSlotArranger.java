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
package com.alipay.sofa.registry.server.meta.slot.arrange;

import com.alipay.sofa.registry.common.model.metaserver.nodes.DataNode;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.exception.DisposeException;
import com.alipay.sofa.registry.exception.InitializeException;
import com.alipay.sofa.registry.exception.SofaRegistrySlotTableException;
import com.alipay.sofa.registry.lifecycle.Suspendable;
import com.alipay.sofa.registry.lifecycle.impl.LifecycleHelper;
import com.alipay.sofa.registry.observer.Observable;
import com.alipay.sofa.registry.observer.impl.AbstractLifecycleObservable;
import com.alipay.sofa.registry.server.meta.MetaLeaderService;
import com.alipay.sofa.registry.server.meta.bootstrap.config.MetaServerConfig;
import com.alipay.sofa.registry.server.meta.cluster.node.NodeAdded;
import com.alipay.sofa.registry.server.meta.cluster.node.NodeRemoved;
import com.alipay.sofa.registry.server.meta.lease.data.DataManagerObserver;
import com.alipay.sofa.registry.server.meta.lease.data.DefaultDataServerManager;
import com.alipay.sofa.registry.server.meta.monitor.Metrics;
import com.alipay.sofa.registry.server.meta.monitor.SlotTableMonitor;
import com.alipay.sofa.registry.server.meta.slot.SlotAssigner;
import com.alipay.sofa.registry.server.meta.slot.SlotBalancer;
import com.alipay.sofa.registry.server.meta.slot.SlotManager;
import com.alipay.sofa.registry.server.meta.slot.assigner.DefaultSlotAssigner;
import com.alipay.sofa.registry.server.meta.slot.balance.DefaultSlotBalancer;
import com.alipay.sofa.registry.server.meta.slot.util.builder.SlotTableBuilder;
import com.alipay.sofa.registry.server.shared.comparator.NodeComparator;
import com.alipay.sofa.registry.server.shared.slot.SlotTableUtils;
import com.alipay.sofa.registry.server.shared.util.NodeUtils;
import com.alipay.sofa.registry.util.ConcurrentUtils;
import com.alipay.sofa.registry.util.JsonUtils;
import com.alipay.sofa.registry.util.SystemUtils;
import com.alipay.sofa.registry.util.WakeUpLoopRunnable;
import com.google.common.annotations.VisibleForTesting;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author chen.zhu
 *     <p>Jan 14, 2021
 */
@Component
public class ScheduledSlotArranger extends AbstractLifecycleObservable
    implements DataManagerObserver, Suspendable {

  private final DefaultDataServerManager dataServerManager;

  private final SlotManager slotManager;

  private final SlotTableMonitor slotTableMonitor;

  private final MetaLeaderService metaLeaderService;

  private final Arranger arranger = new Arranger();

  private final MetaServerConfig metaServerConfig;

  private final Lock lock = new ReentrantLock();

  private volatile boolean slotTableProtectionMode = true;

  @Autowired
  public ScheduledSlotArranger(
      DefaultDataServerManager dataServerManager,
      SlotManager slotManager,
      SlotTableMonitor slotTableMonitor,
      MetaLeaderService metaLeaderService,
      MetaServerConfig metaServerConfig) {
    this.dataServerManager = dataServerManager;
    this.slotManager = slotManager;
    this.slotTableMonitor = slotTableMonitor;
    this.metaLeaderService = metaLeaderService;
    this.metaServerConfig = metaServerConfig;
  }

  @PostConstruct
  public void postConstruct() throws Exception {
    LifecycleHelper.initializeIfPossible(this);
    LifecycleHelper.startIfPossible(this);
  }

  @PreDestroy
  public void preDestroy() throws Exception {
    LifecycleHelper.stopIfPossible(this);
    LifecycleHelper.disposeIfPossible(this);
  }

  @Override
  protected void doInitialize() throws InitializeException {
    super.doInitialize();
    dataServerManager.addObserver(this);
    Thread executor = ConcurrentUtils.createDaemonThread(getClass().getSimpleName(), arranger);
    executor.start();
  }

  @Override
  protected void doDispose() throws DisposeException {
    arranger.close();
    dataServerManager.removeObserver(this);
    super.doDispose();
  }

  @Override
  public void update(Observable source, Object message) {
    logger.warn("[update] receive from [{}], message: {}", source, message);
    if (message instanceof NodeRemoved) {
      arranger.wakeup();
    }
    if (message instanceof NodeAdded) {
      arranger.wakeup();
    }
  }

  public boolean tryLock() {
    return lock.tryLock();
  }

  public void unlock() {
    lock.unlock();
  }

  private SlotTableBuilder createSlotTableBuilder(
      SlotTable slotTable, List<String> currentDataNodeIps, int slotNum, int replicas) {
    NodeComparator comparator = new NodeComparator(slotTable.getDataServers(), currentDataNodeIps);
    SlotTableBuilder slotTableBuilder = new SlotTableBuilder(slotTable, slotNum, replicas);
    slotTableBuilder.init(currentDataNodeIps);

    comparator.getRemoved().forEach(slotTableBuilder::removeDataServerSlots);
    return slotTableBuilder;
  }

  protected boolean assignSlots(
      SlotTableBuilder slotTableBuilder, Collection<String> currentDataServers) {
    SlotTable slotTable = createSlotAssigner(slotTableBuilder, currentDataServers).assign();
    return refreshSlotTable(slotTable);
  }

  protected SlotAssigner createSlotAssigner(
      SlotTableBuilder slotTableBuilder, Collection<String> currentDataServers) {
    return new DefaultSlotAssigner(slotTableBuilder, currentDataServers);
  }

  protected boolean balanceSlots(
      SlotTableBuilder slotTableBuilder, Collection<String> currentDataServers) {
    SlotTable slotTable = createSlotBalancer(slotTableBuilder, currentDataServers).balance();
    return refreshSlotTable(slotTable);
  }

  private boolean refreshSlotTable(SlotTable slotTable) {
    if (slotTable == null) {
      logger.info("[refreshSlotTable] slot-table not change");
      return false;
    }
    if (!SlotTableUtils.isValidSlotTable(slotTable)) {
      throw new SofaRegistrySlotTableException(
          "slot table is not valid: \n" + JsonUtils.writeValueAsString(slotTable));
    }
    if (slotTable.getEpoch() > slotManager.getSlotTable().getEpoch()) {
      slotManager.refresh(slotTable);
      return true;
    } else {
      logger.warn(
          "[refreshSlotTable] slot-table epoch not change: {}",
          JsonUtils.writeValueAsString(slotTable));
      return false;
    }
  }

  protected SlotBalancer createSlotBalancer(
      SlotTableBuilder slotTableBuilder, Collection<String> currentDataServers) {
    return new DefaultSlotBalancer(slotTableBuilder, currentDataServers);
  }

  @Override
  public void suspend() {
    arranger.suspend();
  }

  @Override
  public void resume() {
    arranger.resume();
  }

  @Override
  public boolean isSuspended() {
    return arranger.isSuspended();
  }

  public boolean isSlotTableProtectionMode() {
    return slotTableProtectionMode;
  }

  private final class Arranger extends WakeUpLoopRunnable {

    private final int waitingMillis =
        SystemUtils.getSystemInteger("registry.slot.arrange.interval.millis", 1000);

    @Override
    public int getWaitingMillis() {
      return waitingMillis;
    }

    @Override
    public void runUnthrowable() {
      try {
        arrangeSync();
      } catch (Throwable e) {
        logger.error("failed to arrange", e);
      }
    }
  }

  private boolean tryArrangeSlots(List<DataNode> dataNodes) {
    if (!tryLock()) {
      logger.warn("[tryArrangeSlots] tryLock failed");
      return false;
    }
    boolean modified = false;
    boolean noAssign = false;
    try {
      List<String> currentDataNodeIps = NodeUtils.transferNodeToIpList(dataNodes);
      logger.info(
          "[tryArrangeSlots][begin]arrange slot with DataNode, size={}, {}",
          currentDataNodeIps.size(),
          currentDataNodeIps);
      final SlotTable curSlotTable = slotManager.getSlotTable();
      SlotTableBuilder tableBuilder =
          createSlotTableBuilder(
              curSlotTable,
              currentDataNodeIps,
              slotManager.getSlotNums(),
              slotManager.getSlotReplicaNums());

      noAssign = tableBuilder.hasNoAssignedSlots();
      if (noAssign) {
        logger.info("[re-assign][begin] assign slots to data-server");
        modified = assignSlots(tableBuilder, currentDataNodeIps);
        logger.info("[re-assign][end] modified={}", modified);
      } else if (slotTableMonitor.isStableTableStable()) {
        logger.info("[balance][begin] balance slots to data-server");
        modified = balanceSlots(tableBuilder, currentDataNodeIps);
        logger.info("[balance][end] modified={}", modified);
      } else {
        logger.info("[tryArrangeSlots][end] no arrangement");
      }
    } finally {
      unlock();
    }
    if (modified || noAssign) {
      // for log monitor
      logger.warn("[Arranging]noAssign={},modified={}", noAssign, modified);
    }
    return modified;
  }

  @VisibleForTesting
  public boolean arrangeSync() {
    if (metaLeaderService.amIStableAsLeader()) {
      final int minDataNodeNum = metaServerConfig.getDataNodeProtectionNum();
      // the start arrange with the dataNodes snapshot
      final List<DataNode> dataNodes =
          dataServerManager.getDataServerMetaInfo().getClusterMembers();
      if (dataNodes.isEmpty()) {
        logger.warn("[Arranger] empty data server list");
        return false;
      } else {
        if (dataNodes.size() <= minDataNodeNum) {
          slotTableProtectionMode = true;
          logger.warn("[ProtectionMode] dataServers={} <= {}", dataNodes.size(), minDataNodeNum);
          return false;
        }
        slotTableProtectionMode = false;
        Metrics.SlotArrange.begin();
        try {
          return tryArrangeSlots(dataNodes);
        } finally {
          Metrics.SlotArrange.end();
        }
      }
    } else {
      logger.info(
          "[arrangeSync] not stable leader for arrange, leader: [{}], is-leader: [{}], isWarmup [{}]",
          metaLeaderService.getLeader(),
          metaLeaderService.amILeader(),
          metaLeaderService.isWarmuped());
      return false;
    }
  }
}
