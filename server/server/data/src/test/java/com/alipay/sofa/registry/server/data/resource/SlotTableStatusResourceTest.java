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
package com.alipay.sofa.registry.server.data.resource;

import static org.mockito.Matchers.anyString;

import com.alipay.sofa.registry.common.model.GenericResponse;
import com.alipay.sofa.registry.common.model.Tuple;
import com.alipay.sofa.registry.common.model.slot.BaseSlotStatus;
import com.alipay.sofa.registry.common.model.slot.FollowerSlotStatus;
import com.alipay.sofa.registry.common.model.slot.LeaderSlotStatus;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.slot.SlotAccessorDelegate;
import com.google.common.collect.Lists;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class SlotTableStatusResourceTest {
  @Test
  public void test() {
    SlotTableStatusResource resource = new SlotTableStatusResource();
    resource.slotAccessorDelegate = Mockito.mock(SlotAccessorDelegate.class);
    resource.dataServerConfig = Mockito.mock(DataServerConfig.class);

    LeaderSlotStatus leaderSlotStatus =
        new LeaderSlotStatus(10, 20, "xxx", BaseSlotStatus.LeaderStatus.UNHEALTHY);
    FollowerSlotStatus followerSlotStatus =
        new FollowerSlotStatus(
            11, 30, "yyy", System.currentTimeMillis(), System.currentTimeMillis());

    List<BaseSlotStatus> list = Lists.newArrayList(leaderSlotStatus, followerSlotStatus);
    Mockito.when(resource.slotAccessorDelegate.getSlotTableEpochAndStatuses(anyString()))
        .thenReturn(Tuple.of(100L, list));

    GenericResponse resp = resource.getSlotTableSyncTaskStatus();
    Assert.assertTrue(resp.isSuccess());
    SlotTableStatusResource.SlotTableSyncTaskStatus status =
        (SlotTableStatusResource.SlotTableSyncTaskStatus) resp.getData();

    Assert.assertEquals(status.getEpoch(), 100);
    Assert.assertEquals(status.isCurrentSlotTableStable(), false);
    Assert.assertEquals(status.getSlotStatuses().get(0), list.get(0));
    Assert.assertEquals(status.getSlotStatuses().get(1), list.get(1));

    list.set(0, new LeaderSlotStatus(10, 20, "xxx", BaseSlotStatus.LeaderStatus.HEALTHY));

    resp = resource.getSlotTableSyncTaskStatus();
    Assert.assertTrue(resp.isSuccess());
    status = (SlotTableStatusResource.SlotTableSyncTaskStatus) resp.getData();
    Assert.assertEquals(status.getEpoch(), 100);
    Assert.assertEquals(status.isCurrentSlotTableStable(), true);
    Assert.assertEquals(status.getSlotStatuses().get(0), list.get(0));
    Assert.assertEquals(status.getSlotStatuses().get(1), list.get(1));

    list.set(
        1,
        new FollowerSlotStatus(
            11,
            30,
            "yyy",
            System.currentTimeMillis(),
            System.currentTimeMillis() - SlotTableStatusResource.MAX_SYNC_GAP - 1));

    resp = resource.getSlotTableSyncTaskStatus();
    Assert.assertTrue(resp.isSuccess());
    status = (SlotTableStatusResource.SlotTableSyncTaskStatus) resp.getData();
    Assert.assertEquals(status.getEpoch(), 100);
    Assert.assertEquals(status.isCurrentSlotTableStable(), false);
    Assert.assertEquals(status.getSlotStatuses().get(0), list.get(0));
    Assert.assertEquals(status.getSlotStatuses().get(1), list.get(1));
  }
}
