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
package com.alipay.sofa.registry.common.model.multi;

import com.alipay.sofa.registry.common.model.multi.cluster.DataCenterMetadata;
import com.alipay.sofa.registry.common.model.multi.cluster.RemoteSlotTableStatus;
import com.alipay.sofa.registry.common.model.slot.Slot;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author xiaojian.xj
 * @version : RemoteSlotTableStatusTest.java, v 0.1 2023年02月02日 19:36 xiaojian.xj Exp $
 */
public class RemoteSlotTableStatusTest {

  @Test
  public void test() {

    SlotTable slotTable =
        new SlotTable(10, Lists.newArrayList(new Slot(1, null, 0, Collections.emptyList())));
    DataCenterMetadata dataCenterMetadata =
        new DataCenterMetadata("dc1", Sets.newHashSet("zone1", "zone2"));

    RemoteSlotTableStatus status = RemoteSlotTableStatus.conflict(slotTable);
    Assert.assertEquals(slotTable.getEpoch(), status.getSlotTableEpoch());
    Assert.assertFalse(status.isSlotTableUpgrade());
    Assert.assertTrue(status.isSlotTableEpochConflict());
    Assert.assertEquals(slotTable, status.getSlotTable());
    Assert.assertNull(status.getDataCenterMetadata());

    status = RemoteSlotTableStatus.notUpgrade(slotTable.getEpoch(), dataCenterMetadata);
    Assert.assertEquals(slotTable.getEpoch(), status.getSlotTableEpoch());
    Assert.assertFalse(status.isSlotTableUpgrade());
    Assert.assertFalse(status.isSlotTableEpochConflict());
    Assert.assertNull(status.getSlotTable());
    Assert.assertEquals(dataCenterMetadata, status.getDataCenterMetadata());

    status = RemoteSlotTableStatus.upgrade(slotTable, dataCenterMetadata);
    Assert.assertEquals(slotTable.getEpoch(), status.getSlotTableEpoch());
    Assert.assertTrue(status.isSlotTableUpgrade());
    Assert.assertFalse(status.isSlotTableEpochConflict());
    Assert.assertEquals(slotTable, status.getSlotTable());
    Assert.assertEquals(dataCenterMetadata, status.getDataCenterMetadata());
    status.toString();
  }
}
