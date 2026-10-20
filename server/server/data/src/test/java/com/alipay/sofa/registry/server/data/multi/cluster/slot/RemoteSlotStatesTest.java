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
package com.alipay.sofa.registry.server.data.multi.cluster.slot;

import static org.mockito.Mockito.when;

import com.alipay.sofa.registry.common.model.slot.Slot;
import com.alipay.sofa.registry.server.data.multi.cluster.slot.MultiClusterSlotManagerImpl.RemoteSlotStates;
import com.alipay.sofa.registry.server.data.multi.cluster.slot.MultiClusterSlotManagerImpl.SyncDataIdTask;
import com.alipay.sofa.registry.server.data.slot.SyncLeaderTask;
import com.alipay.sofa.registry.task.KeyedTask;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RemoteSlotStatesTest {
  @Mock private KeyedTask<SyncLeaderTask> leaderTask;
  @Mock private KeyedTask<SyncDataIdTask> dataIdTask;

  @Test
  public void leaderEpochChangeInvalidatesOldSyncProgress() {
    RemoteSlotStates state = syncedState();
    Slot updated = slot(2);

    state.update(updated);

    Assert.assertSame(updated, state.getSlot());
    Assert.assertFalse(state.isMigrated());
    Assert.assertEquals(-1L, state.lastSuccessSyncRemoteTime);
    Assert.assertEquals(-1L, state.lastSuccessDataInfoIdTime);
    Assert.assertNull(state.syncRemoteTask);
    Assert.assertNull(state.syncDataIdTask);
    Assert.assertFalse(state.hasPending());
  }

  @Test
  public void sameLeaderEpochPreservesSyncProgressAndPendingData() {
    RemoteSlotStates state = syncedState();
    Slot updated = slot(1);

    state.update(updated);

    Assert.assertSame(updated, state.getSlot());
    Assert.assertTrue(state.isMigrated());
    Assert.assertEquals(100L, state.lastSuccessSyncRemoteTime);
    Assert.assertEquals(200L, state.lastSuccessDataInfoIdTime);
    Assert.assertSame(leaderTask, state.syncRemoteTask);
    Assert.assertSame(dataIdTask, state.syncDataIdTask);
    Assert.assertTrue(state.hasPending());
    Assert.assertEquals(2, state.pendingDataInfoIds.size());
  }

  @Test
  public void onlySuccessfulLeaderSyncMarksSlotMigrated() {
    RemoteSlotStates state = new RemoteSlotStates("remote-dc", slot(1));
    state.completeSyncRemoteLeaderTask();
    Assert.assertFalse(state.isMigrated());
    Assert.assertEquals(-1L, state.lastSuccessSyncRemoteTime);

    state.syncRemoteTask = leaderTask;
    state.completeSyncRemoteLeaderTask();
    Assert.assertFalse(state.isMigrated());
    Assert.assertEquals(-1L, state.lastSuccessSyncRemoteTime);

    when(leaderTask.isSuccess()).thenReturn(true);
    when(leaderTask.getEndTime()).thenReturn(300L);
    state.completeSyncRemoteLeaderTask();
    Assert.assertTrue(state.isMigrated());
    Assert.assertEquals(300L, state.lastSuccessSyncRemoteTime);
  }

  @Test
  public void onlyFinishedSuccessfulDataIdSyncAdvancesTimestamp() {
    RemoteSlotStates state = new RemoteSlotStates("remote-dc", slot(1));
    state.lastSuccessDataInfoIdTime = 100L;
    state.completeSyncRemoteDataIdTask();
    Assert.assertEquals(100L, state.lastSuccessDataInfoIdTime);

    state.syncDataIdTask = dataIdTask;
    state.completeSyncRemoteDataIdTask();
    Assert.assertEquals(100L, state.lastSuccessDataInfoIdTime);

    when(dataIdTask.isFinished()).thenReturn(true);
    state.completeSyncRemoteDataIdTask();
    Assert.assertEquals(100L, state.lastSuccessDataInfoIdTime);

    when(dataIdTask.isSuccess()).thenReturn(true);
    when(dataIdTask.getEndTime()).thenReturn(400L);
    state.completeSyncRemoteDataIdTask();
    Assert.assertEquals(400L, state.lastSuccessDataInfoIdTime);
  }

  private RemoteSlotStates syncedState() {
    RemoteSlotStates state = new RemoteSlotStates("remote-dc", slot(1));
    state.synced = true;
    state.lastSuccessSyncRemoteTime = 100L;
    state.lastSuccessDataInfoIdTime = 200L;
    state.syncRemoteTask = leaderTask;
    state.syncDataIdTask = dataIdTask;
    state.addPending("data-1");
    state.addPending(Collections.singleton("data-2"));
    return state;
  }

  private static Slot slot(long leaderEpoch) {
    return new Slot(0, "remote-leader", leaderEpoch, Collections.emptyList());
  }
}
