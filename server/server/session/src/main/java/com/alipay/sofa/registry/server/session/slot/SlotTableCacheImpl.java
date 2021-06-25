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

import com.alipay.sofa.registry.common.model.slot.Slot;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.common.model.slot.func.SlotFunction;
import com.alipay.sofa.registry.common.model.slot.func.SlotFunctionRegistry;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.shared.slot.SlotTableRecorder;
import com.google.common.annotations.VisibleForTesting;
import java.util.List;
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

  private volatile SlotTable slotTable = SlotTable.INIT;
  private final Lock lock = new ReentrantLock();

  @Autowired(required = false)
  private List<SlotTableRecorder> recorders;

  @Override
  public int slotOf(String dataInfoId) {
    return slotFunction.slotOf(dataInfoId);
  }

  @Override
  public Slot getSlot(String dataInfoId) {
    int slotId = slotOf(dataInfoId);
    return slotTable.getSlot(slotId);
  }

  @Override
  public Slot getSlot(int slotId) {
    return slotTable.getSlot(slotId);
  }

  @Override
  public String getLeader(int slotId) {
    final Slot slot = slotTable.getSlot(slotId);
    return slot == null ? null : slot.getLeader();
  }

  @Override
  public long getEpoch() {
    return slotTable.getEpoch();
  }

  @Override
  public boolean updateSlotTable(SlotTable slotTable) {
    lock.lock();
    final long curEpoch;
    try {
      curEpoch = this.slotTable.getEpoch();
      if (curEpoch >= slotTable.getEpoch()) {
        LOGGER.info("skip update, current={}, update={}", curEpoch, slotTable.getEpoch());
        return false;
      }
      recordSlotTable(slotTable);
      this.slotTable = slotTable;
    } finally {
      lock.unlock();
    }
    checkForSlotTable(curEpoch, slotTable);
    return true;
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

  protected void checkForSlotTable(long curEpoch, SlotTable updating) {
    for (Slot slot : updating.getSlots()) {
      if (StringUtils.isBlank(slot.getLeader())) {
        LOGGER.error("[NoLeader] {}", slot);
      }
    }
    LOGGER.info(
        "updating slot table, expect={}, current={}, {}", updating.getEpoch(), curEpoch, updating);
  }

  @Override
  public SlotTable getCurrentSlotTable() {
    final SlotTable now = this.slotTable;
    return new SlotTable(now.getEpoch(), now.getSlots());
  }

  @VisibleForTesting
  protected SlotTableCacheImpl setRecorders(List<SlotTableRecorder> recorders) {
    this.recorders = recorders;
    return this;
  }
}
