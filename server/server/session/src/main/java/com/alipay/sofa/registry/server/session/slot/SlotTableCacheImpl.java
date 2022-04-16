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

import com.alipay.sofa.registry.common.model.multi.cluster.RemoteSlotTableStatus;
import com.alipay.sofa.registry.common.model.slot.Slot;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.common.model.slot.func.SlotFunction;
import com.alipay.sofa.registry.common.model.slot.func.SlotFunctionRegistry;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.shared.slot.SlotTableRecorder;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

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

  @Autowired private SessionServerConfig sessionServerConfig;

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
      curEpoch = slotTableMap.get(sessionServerConfig.getSessionServerDataCenter()).getEpoch();
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
    try {
      for (Entry<String, RemoteSlotTableStatus> entry : remoteSlotTableStatus.entrySet()) {
        RemoteSlotTableStatus value = entry.getValue();
        final long curEpoch = slotTableMap.get(entry.getKey()).getEpoch();

        if (!value.isSlotTableUpgrade() || value.getSlotTable() == null) {
          LOGGER.info(
              "skip update, dataCenter={}, current={}, upgrade=false", entry.getKey(), curEpoch);
          continue;
        }

        SlotTable slotTable = value.getSlotTable();

        if (curEpoch >= slotTable.getEpoch()) {
          LOGGER.info("skip update, current={}, update={}", curEpoch, slotTable.getEpoch());
          success = false;
        }
        recordSlotTable(slotTable);
        slotTableMap.put(sessionServerConfig.getSessionServerDataCenter(), slotTable);
      }
    } finally {
      lock.unlock();
    }

    return success;
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
    Map<String, Long> ret = Maps.newHashMapWithExpectedSize(slotTableMap.size() - 1);
    for (Entry<String, SlotTable> entry : slotTableMap.entrySet()) {
      if (StringUtils.equals(entry.getKey(), sessionServerConfig.getSessionServerDataCenter())) {
        continue;
      }
      ret.put(entry.getKey(), entry.getValue().getEpoch());
    }
    return ret;
  }

  @VisibleForTesting
  protected SlotTableCacheImpl setRecorders(List<SlotTableRecorder> recorders) {
    this.recorders = recorders;
    return this;
  }
}
