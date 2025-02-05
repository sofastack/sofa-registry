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
package com.alipay.sofa.registry.server.data.slot;

import com.alipay.sofa.registry.common.model.slot.Slot;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.server.shared.remoting.ClientSideExchanger;
import com.alipay.sofa.registry.task.TaskErrorSilenceException;
import com.alipay.sofa.registry.util.StringFormatter;
import org.apache.commons.lang.StringUtils;

/**
 * @author xiaojian.xj
 * @version : SyncLeaderTask.java, v 0.1 2022年05月10日 21:33 xiaojian.xj Exp $
 */
public class SyncLeaderTask implements Runnable {

  private final String localDataCenter;
  private final String syncDataCenter;
  private final boolean syncLocalDataCenter;

  private final long startTimestamp = System.currentTimeMillis();
  private final long slotTableEpoch;
  private final Slot slot;
  private final SlotDiffSyncer syncer;
  private final ClientSideExchanger clientSideExchanger;
  private final SyncContinues continues;

  private final Logger SYNC_DIGEST_LOGGER;
  private final Logger SYNC_ERROR_LOGGER;

  public SyncLeaderTask(
      String localDataCenter,
      String syncDataCenter,
      long slotTableEpoch,
      Slot slot,
      SlotDiffSyncer syncer,
      ClientSideExchanger clientSideExchanger,
      SyncContinues continues,
      Logger syncDigestLogger,
      Logger syncErrorLogger) {
    this.localDataCenter = localDataCenter;
    this.syncDataCenter = syncDataCenter;
    syncLocalDataCenter = StringUtils.equals(localDataCenter, syncDataCenter);

    this.slotTableEpoch = slotTableEpoch;
    this.slot = slot;
    this.syncer = syncer;
    this.clientSideExchanger = clientSideExchanger;
    this.continues = continues;

    this.SYNC_DIGEST_LOGGER = syncDigestLogger;
    this.SYNC_ERROR_LOGGER = syncErrorLogger;
  }

  @Override
  public void run() {
    boolean success = false;
    try {
      success =
          syncer.syncSlotLeader(
              localDataCenter,
              syncDataCenter,
              syncLocalDataCenter,
              slot.getId(),
              slot.getLeader(),
              slot.getLeaderEpoch(),
              clientSideExchanger,
              slotTableEpoch,
              continues);
      if (!success) {
        throw new RuntimeException(StringFormatter.format("{} sync leader failed", syncDataCenter));
      }
    } catch (Throwable e) {
      SYNC_ERROR_LOGGER.error(
          "[syncLeader]syncLocal={}, syncDataCenter={}, failed={}, slot={}",
          syncDataCenter,
          syncDataCenter,
          slot.getLeader(),
          slot.getId(),
          e);
      // rethrow silence exception, notify the task is failed
      throw TaskErrorSilenceException.INSTANCE;
    } finally {
      SYNC_DIGEST_LOGGER.info(
          "[syncLeader]{},{},{},{},{},span={}",
          success ? 'Y' : 'N',
          syncLocalDataCenter ? 'L' : 'R',
          syncDataCenter,
          slot.getId(),
          slot.getLeader(),
          System.currentTimeMillis() - startTimestamp);
    }
  }

  @Override
  public String toString() {
    return "SyncLeaderTask{"
        + "syncDataCenter="
        + syncDataCenter
        + ", syncLocalDataCenter="
        + syncLocalDataCenter
        + ", slotTableEpoch="
        + slotTableEpoch
        + ", slot="
        + slot
        + '}';
  }
}
