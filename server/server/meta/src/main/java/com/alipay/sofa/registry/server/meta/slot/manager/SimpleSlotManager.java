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
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author chen.zhu
 *     <p>Nov 13, 2020
 */
public class SimpleSlotManager extends AbstractLifecycleObservable implements SlotManager {

  public static final String LOCAL_SLOT_MANAGER = "LocalSlotManager";

  private final ReadWriteLock lock = new ReentrantReadWriteLock();

  private final AtomicReference<SlotTableCacheWrapper> localRepo =
      new AtomicReference<>(new SlotTableCacheWrapper(SlotTable.INIT, ImmutableMap.of()));

  @Override
  public SlotTable getSlotTable() {
    lock.readLock().lock();
    try {
      return localRepo.get().slotTable;
    } finally {
      lock.readLock().unlock();
    }
  }

  @Override
  public void refresh(SlotTable slotTable) {
    lock.writeLock().lock();
    try {
      if (slotTable.getEpoch() <= localRepo.get().slotTable.getEpoch()) {
        if (logger.isWarnEnabled()) {
          logger.warn(
              "[refresh]receive slot table,but epoch({}) is smaller than current({})",
              slotTable.getEpoch(),
              localRepo.get().slotTable.getEpoch());
        }
        return;
      }
      setSlotTableCacheWrapper(new SlotTableCacheWrapper(slotTable, refreshReverseMap(slotTable)));
    } finally {
      lock.writeLock().unlock();
    }
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
    return SlotConfig.SLOT_NUM;
  }

  @Override
  public int getSlotReplicaNums() {
    return SlotConfig.SLOT_REPLICAS;
  }

  @Override
  public DataNodeSlot getDataNodeManagedSlot(String dataNode, boolean ignoreFollowers) {
    lock.readLock().lock();
    try {
      // here we ignore port for data-node, as when store the reverse-map, we lose the port
      // information
      // besides, port is not matter here
      DataNodeSlot target = localRepo.get().reverseMap.get(dataNode);
      if (target == null) {
        return new DataNodeSlot(dataNode);
      }
      return target.fork(ignoreFollowers);
    } finally {
      lock.readLock().unlock();
    }
  }

  @VisibleForTesting
  protected void setSlotTableCacheWrapper(SlotTableCacheWrapper wrapper) {
    this.localRepo.set(wrapper);
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
