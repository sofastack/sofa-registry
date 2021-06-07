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
package com.alipay.sofa.registry.server.meta.slot.manager;

import com.alipay.sofa.registry.common.model.slot.DataNodeSlot;
import com.alipay.sofa.registry.common.model.slot.SlotConfig;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.observer.impl.AbstractLifecycleObservable;
import com.alipay.sofa.registry.server.meta.slot.SlotManager;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author chen.zhu
 *     <p>Nov 13, 2020
 */
public class SimpleSlotManager extends AbstractLifecycleObservable implements SlotManager {

  private final ReadWriteLock lock = new ReentrantReadWriteLock();

  private volatile SlotTableCacheWrapper localRepo =
      new SlotTableCacheWrapper(SlotTable.INIT, ImmutableMap.of());

  private int slotNums = SlotConfig.SLOT_NUM;
  private int slotReplicas = SlotConfig.SLOT_REPLICAS;

  @Override
  public SlotTable getSlotTable() {
    lock.readLock().lock();
    try {
      return localRepo.slotTable;
    } finally {
      lock.readLock().unlock();
    }
  }

  /**
   * The function means to update slot-table if possible(like CAS(xxx, xxx)) return 1. true: if
   * there were any changes on slot-table (by check whether slot-table epoch getting bigger) 2.
   * false: if there were no changes, including we try to refresh a lower or equal epoch slot-table
   *
   * <p>Then, it will notify observers about this slot-table change event
   */
  @Override
  public boolean refresh(SlotTable slotTable) {
    lock.writeLock().lock();
    try {
      long localEpoch = localRepo.slotTable.getEpoch();
      // same epoch means no changes here, so we try not to notify observers
      if (slotTable.getEpoch() == localEpoch) {
        return false;
      }
      if (slotTable.getEpoch() < localEpoch) {
        if (logger.isWarnEnabled()) {
          logger.warn(
              "[refresh]receive slot table,but epoch({}) is smaller than current({})",
              slotTable.getEpoch(),
              localRepo.slotTable.getEpoch());
        }
        return false;
      }
      setSlotTableCacheWrapper(new SlotTableCacheWrapper(slotTable, refreshReverseMap(slotTable)));
    } finally {
      lock.writeLock().unlock();
    }
    return true;
  }

  private Map<String, DataNodeSlot> refreshReverseMap(SlotTable slotTable) {
    Map<String, DataNodeSlot> newMap = Maps.newHashMap();
    List<DataNodeSlot> dataNodeSlots = slotTable.transfer(null, false);
    for (DataNodeSlot dataNodeSlot : dataNodeSlots) {
      newMap.put(dataNodeSlot.getDataNode(), dataNodeSlot);
    }
    return ImmutableMap.copyOf(newMap);
  }

  @Override
  public int getSlotNums() {
    return slotNums;
  }

  @Override
  public int getSlotReplicaNums() {
    return slotReplicas;
  }

  @VisibleForTesting
  public void setSlotNums(int slotNums) {
    this.slotNums = slotNums;
  }

  @VisibleForTesting
  public void setSlotReplicas(int slotReplicas) {
    this.slotReplicas = slotReplicas;
  }

  @Override
  public DataNodeSlot getDataNodeManagedSlot(String dataNode, boolean ignoreFollowers) {
    lock.readLock().lock();
    try {
      // here we ignore port for data-node, as when store the reverse-map, we lose the port
      // information
      // besides, port is not matter here
      DataNodeSlot target = localRepo.reverseMap.get(dataNode);
      if (target == null) {
        return new DataNodeSlot(dataNode);
      }
      return target.fork(ignoreFollowers);
    } finally {
      lock.readLock().unlock();
    }
  }

  private void setSlotTableCacheWrapper(SlotTableCacheWrapper wrapper) {
    this.localRepo = wrapper;
  }

  private static final class SlotTableCacheWrapper {
    private final SlotTable slotTable;

    private final Map<String, DataNodeSlot> reverseMap;

    public SlotTableCacheWrapper(SlotTable slotTable, Map<String, DataNodeSlot> reverseMap) {
      this.slotTable = slotTable;
      this.reverseMap = reverseMap;
    }
  }
}
