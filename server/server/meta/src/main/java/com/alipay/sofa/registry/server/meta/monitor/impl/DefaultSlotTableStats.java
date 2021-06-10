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
package com.alipay.sofa.registry.server.meta.monitor.impl;

import com.alipay.sofa.registry.common.model.metaserver.nodes.DataNode;
import com.alipay.sofa.registry.common.model.slot.*;
import com.alipay.sofa.registry.exception.InitializeException;
import com.alipay.sofa.registry.lifecycle.impl.AbstractLifecycle;
import com.alipay.sofa.registry.server.meta.bootstrap.config.MetaServerConfig;
import com.alipay.sofa.registry.server.meta.monitor.Metrics;
import com.alipay.sofa.registry.server.meta.monitor.SlotStats;
import com.alipay.sofa.registry.server.meta.monitor.SlotTableStats;
import com.alipay.sofa.registry.server.meta.slot.SlotManager;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.commons.lang.StringUtils;

/**
 * @author chen.zhu
 *     <p>Jan 28, 2021
 */
public class DefaultSlotTableStats extends AbstractLifecycle implements SlotTableStats {

  private final SlotManager slotManager;

  private final Map<Integer, SlotStats> slotStatses = Maps.newConcurrentMap();

  private final ReadWriteLock lock = new ReentrantReadWriteLock();

  private final MetaServerConfig metaServerConfig;

  public DefaultSlotTableStats(SlotManager slotManager, MetaServerConfig metaServerConfig) {
    this.slotManager = slotManager;
    this.metaServerConfig = metaServerConfig;
  }

  @Override
  protected void doInitialize() throws InitializeException {
    super.doInitialize();
    for (int slotId = 0; slotId < SlotConfig.SLOT_NUM; slotId++) {
      Slot slot = slotManager.getSlotTable().getSlot(slotId);
      if (slot == null) {
        slot = new Slot(slotId, null, 0L, Collections.emptyList());
      }
      slotStatses.put(
          slotId, new DefaultSlotStats(slot, metaServerConfig.getDataReplicateMaxGapMillis()));
    }
  }

  @Override
  public boolean isSlotLeadersStable() {
    lock.readLock().lock();
    try {
      if (slotManager.getSlotTable() == SlotTable.INIT) {
        logger.warn("[isSlotLeadersStable] slot table empty now");
        return false;
      }
      for (int slotId = 0; slotId < SlotConfig.SLOT_NUM; slotId++) {
        Slot slot = slotManager.getSlotTable().getSlot(slotId);
        if (slot == null) {
          logger.error("[isSlotLeadersStable] slot manager has no slot: [{}]", slotId);
          return false;
        }
        String leader = slot.getLeader();
        SlotStats slotStats = slotStatses.get(slotId);
        if (StringUtils.isBlank(leader)
            || !slotStats.getSlot().getLeader().equals(leader)
            || !slotStatses.get(slotId).isLeaderStable()) {
          logger.warn("[isSlotLeadersStable]slot[{}] leader[{}] not stable", slotId, leader);
          return false;
        }
      }
      return true;
    } finally {
      lock.readLock().unlock();
    }
  }

  @Override
  public boolean isSlotFollowersStable() {
    lock.readLock().lock();
    try {
      if (slotManager.getSlotTable() == SlotTable.INIT) {
        logger.warn("[isSlotFollowersStable] slot table empty now");
        return false;
      }
      for (Map.Entry<Integer, SlotStats> entry : slotStatses.entrySet()) {
        Slot slot = slotManager.getSlotTable().getSlot(entry.getKey());
        if (slot == null) {
          logger.error("[isSlotFollowersStable] slot manager has no slot: [{}]", entry.getKey());
          return false;
        }
        Set<String> followers = slot.getFollowers();
        for (String follower : followers) {
          if (!entry.getValue().isFollowerStable(follower)) {
            logger.warn(
                "[isSlotFollowersStable]slot[{}] follower not stable {}",
                entry.getKey(),
                entry.getValue());
            return false;
          }
        }
      }
      return true;
    } finally {
      lock.readLock().unlock();
    }
  }

  @Override
  public void checkSlotStatuses(DataNode node, List<BaseSlotStatus> slotStatuses) {
    try {
      lock.writeLock().lock();
      for (BaseSlotStatus slotStatus : slotStatuses) {
        int slotId = slotStatus.getSlotId();
        SlotStats slotStats = slotStatses.get(slotId);
        if (slotStats == null || slotStats.getSlot() == null) {
          continue;
        }
        if (slotStats.getSlot().getLeaderEpoch() > slotStatus.getSlotLeaderEpoch()) {
          logger.warn(
              "[checkSlotStatuses] won't update slot status, slot[{}] leader-epoch[{}] is less than current[{}]",
              slotId,
              slotStatus.getSlotLeaderEpoch(),
              slotStats.getSlot().getLeaderEpoch());
          continue;
        } else if (slotStats.getSlot().getLeaderEpoch() < slotStatus.getSlotLeaderEpoch()) {
          Metrics.DataSlot.setDataSlotGreaterThanMeta(node.getIp(), slotId);
          logger.error(
              "[checkSlotStatuses] won't update slot status, slot[{}] leader-epoch[{}] reported by data({}) is more than current[{}]",
              slotId,
              slotStatus.getSlotLeaderEpoch(),
              node.getIp(),
              slotStats.getSlot().getLeaderEpoch());
          continue;
        }
        if (!slotStats.getSlot().equals(slotManager.getSlotTable().getSlot(slotId))) {
          logger.error(
              "[checkSlotStatuses] slot reported by data({}) is not equals with mine({}), not update",
              slotStats.getSlot(),
              slotManager.getSlotTable().getSlot(slotId));
          continue;
        }

        if (slotStatus.getRole() == Slot.Role.Leader) {
          if (!slotStats.getSlot().getLeader().equals(node.getIp())) {
            logger.error(
                "[checkSlotStatuses] slot leader({}) is not equal with reported data-server({})",
                slotStats.getSlot().getLeader(),
                node.getIp());
            Metrics.DataSlot.setDataReportNotStable(node.getIp(), slotId);
            continue;
          }
          slotStats.updateLeaderState((LeaderSlotStatus) slotStatus);
        } else {
          if (!slotStats.getSlot().getFollowers().contains(node.getIp())) {
            logger.error(
                "[checkSlotStatuses] slot follower({}) is not containing reported data-server({})",
                slotStats.getSlot().getFollowers(),
                node.getIp());
            Metrics.DataSlot.setDataReportNotStable(node.getIp(), slotId);
            continue;
          }
          slotStats.updateFollowerState((FollowerSlotStatus) slotStatus);
        }
      }
    } finally {
      lock.writeLock().unlock();
    }
  }

  @Override
  public void updateSlotTable(SlotTable slotTable) {
    try {
      lock.writeLock().lock();
      logger.info("[updateSlotTable] update slot table for epoch [{}]", slotTable.getEpoch());
      slotTable
          .getSlotMap()
          .forEach(
              (slotId, slot) -> {
                SlotStats slotStats = slotStatses.get(slotId);
                if (slotStats.getSlot().getLeaderEpoch() < slot.getLeaderEpoch()) {
                  slotStatses.put(
                      slotId,
                      new DefaultSlotStats(slot, metaServerConfig.getDataReplicateMaxGapMillis()));
                } else if (slotStats.getSlot().getLeaderEpoch() == slot.getLeaderEpoch()
                    && !slotStats.getSlot().equals(slot)) {
                  slotStatses.put(
                      slotId,
                      new DefaultSlotStats(slot, metaServerConfig.getDataReplicateMaxGapMillis()));
                } else {
                  logger.warn("[updateSlotTable]skip slot[{}]", slotId);
                }
              });
    } finally {
      lock.writeLock().unlock();
    }
  }

  @VisibleForTesting
  public SlotStats getSlotStats(int slotId) {
    return slotStatses.get(slotId);
  }
}
