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
package com.alipay.sofa.registry.server.shared.resource;

import com.alipay.sofa.registry.common.model.GenericResponse;
import com.alipay.sofa.registry.common.model.slot.Slot;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;

public class SlotGenericResourceTest {
  @Test
  public void test() {
    SlotGenericResource resource = new SlotGenericResource();
    Set<Slot> slots = Sets.newHashSet(new Slot(1, "text", 2, Collections.emptyList()));
    SlotTable slotTable = new SlotTable(10, slots);
    resource.record(slotTable);

    GenericResponse<SlotTable> response = resource.slotTable();
    Assert.assertTrue(response.isSuccess());
    Assert.assertEquals(response.getData(), slotTable);

    GenericResponse<Long> epochResp = resource.epoch();
    Assert.assertTrue(response.isSuccess());
    Assert.assertEquals(epochResp.getData().longValue(), 10);
  }
}
