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
package com.alipay.sofa.registry.server.session.slot;

import com.alipay.sofa.registry.common.model.metaserver.MultiClusterSyncInfo;
import com.alipay.sofa.registry.common.model.multi.cluster.RemoteSlotTableStatus;
import com.alipay.sofa.registry.common.model.slot.Slot;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.common.model.slot.func.SlotFunction;
import com.alipay.sofa.registry.common.model.slot.func.SlotFunctionRegistry;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.shared.slot.SlotTableRecorder;
import com.alipay.sofa.registry.store.api.meta.MultiClusterSyncRepository;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

/**
 * @author yuzhi.lyz
 * @version v 0.1 2020-11-11 10:07 yuzhi.lyz Exp $
 */
public final class SlotTableCacheImpl implements SlotTableCache {
  private static final Logger LOGGER = LoggerFactory.getLogger(SlotTableCacheImpl.class);

  private final SlotFunction slotFunction = SlotFunctionRegistry.getFunc();

  private final Lock lock = new ReentrantLock();

  private final Map<String, SlotTable> slotTableMap = Maps.newConcurrentMap();

  @Autowired(required = false)
  private List<SlotTableRecorder> recorders;

  @Autowired private MultiClusterSyncRepository multiClusterSyncRepository;

  @Autowired private SessionServerConfig sessionServerConfig;

  @PostConstruct
  public void init() {
    slotTableMap.put(sessionServerConfig.getSessionServerDataCenter(), SlotTable.INIT);
  }

  @Override
  public int slotOf(String dataInfoId) {
    return slotFunction.slotOf(dataInfoId);
  }

  @Override
  public Slot getSlot(String dataCenter, String dataInfoId) {
    int slotId = slotOf(dataInfoId);
    return getSlot(dataCenter, slotId);
  }

  @Override
  public Slot getSlot(String dataCenter, int slotId) {
    // slotTable will be replace when update, not need to lock when reading
    SlotTable slotTable = slotTableMap.get(dataCenter);
    return slotTable == null ? null : slotTable.getSlot(slotId);
  }

  @Override
  public String getLeader(String dataCenter, int slotId) {
    final Slot slot = getSlot(dataCenter, slotId);
    return slot == null ? null : slot.getLeader();
  }

  @Override
  public long getEpoch(String dataCenter) {
    SlotTable slotTable = slotTableMap.get(dataCenter);
    return slotTable == null ? SlotTable.INIT.getEpoch() : slotTable.getEpoch();
  }

  @Override
  public boolean updateLocalSlotTable(SlotTable slotTable) {
    lock.lock();
    final long curEpoch;
    try {
      SlotTable exist = slotTableMap.get(sessionServerConfig.getSessionServerDataCenter());
      if (exist == null) {
        recordSlotTable(slotTable);
        slotTableMap.put(sessionServerConfig.getSessionServerDataCenter(), slotTable);
        return true;
      }
      curEpoch = exist.getEpoch();
      if (curEpoch >= slotTable.getEpoch()) {
        LOGGER.info(
            "skip update, dataCenter={}, current={}, update={}",
            sessionServerConfig.getSessionServerDataCenter(),
            curEpoch,
            slotTable.getEpoch());
        return false;
      }
      recordSlotTable(slotTable);
      slotTableMap.put(sessionServerConfig.getSessionServerDataCenter(), slotTable);
    } finally {
      lock.unlock();
    }
    checkForSlotTable(sessionServerConfig.getSessionServerDataCenter(), curEpoch, slotTable);
    return true;
  }

  @Override
  public boolean updateRemoteSlotTable(Map<String, RemoteSlotTableStatus> remoteSlotTableStatus) {

    boolean success = true;
    lock.lock();
    Set<String> difference = Sets.difference(slotTableMap.keySet(), remoteSlotTableStatus.keySet());
    Set<String> toBeRemove = new HashSet<>(difference);
    toBeRemove.remove(sessionServerConfig.getSessionServerDataCenter());

    try {
      for (Entry<String, RemoteSlotTableStatus> entry : remoteSlotTableStatus.entrySet()) {
        RemoteSlotTableStatus value = entry.getValue();

        String remoteDataCenter = entry.getKey();
        final long curEpoch =
            slotTableMap.computeIfAbsent(remoteDataCenter, k -> SlotTable.INIT).getEpoch();

        if (!value.isSlotTableUpgrade() || value.getSlotTable() == null) {
          LOGGER.info(
              "skip update, dataCenter={}, current={}, upgrade=false", remoteDataCenter, curEpoch);
          continue;
        }

        SlotTable slotTable = value.getSlotTable();

        if (curEpoch >= slotTable.getEpoch()) {
          LOGGER.warn("skip update, current={}, update={}", curEpoch, slotTable.getEpoch());
          continue;
        }
        recordSlotTable(slotTable);
        slotTableMap.put(remoteDataCenter, slotTable);
        LOGGER.info(
            "[updateRemoteSlotTable]dataCenter={}, prev.version={}, update.version={}, update={}",
            remoteDataCenter,
            curEpoch,
            slotTable.getEpoch(),
            slotTable);
      }
      processRemove(toBeRemove);
    } catch (Throwable throwable) {
      LOGGER.error("update remote slot table:{} error.", remoteSlotTableStatus, throwable);
      success = false;
    } finally {
      lock.unlock();
    }

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
      if (StringUtils.equals(remove, sessionServerConfig.getSessionServerDataCenter())) {
        continue;
      }

      if (syncing.contains(remove)) {
        LOGGER.error("dataCenter:{} remove slot table is forbidden.", remove);
        continue;
      }
      slotTableMap.remove(remove);
      LOGGER.info("remove dataCenter:{} slot table success.", remove);
    }
  }

  private void recordSlotTable(SlotTable slotTable) {
    if (recorders == null) {
      return;
    }
    for (SlotTableRecorder recorder : recorders) {
      if (recorder != null) {
        recorder.record(slotTable);
      }
    }
  }

  protected void checkForSlotTable(String dataCenter, long curEpoch, SlotTable updating) {
    for (Slot slot : updating.getSlots()) {
      if (StringUtils.isBlank(slot.getLeader())) {
        LOGGER.error("[NoLeader] {},{}", dataCenter, slot);
      }
    }
    LOGGER.info(
        "updating slot table, dataCenter={}, expect={}, current={}, {}",
        dataCenter,
        updating.getEpoch(),
        curEpoch,
        updating);
  }

  @Override
  public SlotTable getLocalSlotTable() {
    final SlotTable now = slotTableMap.get(sessionServerConfig.getSessionServerDataCenter());
    return new SlotTable(now.getEpoch(), now.getSlots());
  }

  @Override
  public Map<String, Long> getRemoteSlotTableEpoch() {
    if (CollectionUtils.isEmpty(slotTableMap)) {
      return Collections.emptyMap();
    }

    Map<String, Long> ret = Maps.newHashMapWithExpectedSize(slotTableMap.size());
    for (Entry<String, SlotTable> entry : slotTableMap.entrySet()) {
      if (StringUtils.equals(entry.getKey(), sessionServerConfig.getSessionServerDataCenter())) {
        continue;
      }
      ret.put(entry.getKey(), entry.getValue().getEpoch());
    }
    return ret;
  }

  @Override
  public SlotTable getSlotTable(String dataCenter) {
    final SlotTable slotTable = slotTableMap.get(dataCenter);
    return slotTable;
  }

  @VisibleForTesting
  protected SlotTableCacheImpl setRecorders(List<SlotTableRecorder> recorders) {
    this.recorders = recorders;
    return this;
  }

  /**
   * Setter method for property <tt>sessionServerConfig</tt>.
   *
   * @param sessionServerConfig value to be assigned to property sessionServerConfig
   * @return SlotTableCacheImpl
   */
  @VisibleForTesting
  public SlotTableCacheImpl setSessionServerConfig(SessionServerConfig sessionServerConfig) {
    this.sessionServerConfig = sessionServerConfig;
    return this;
  }
}
