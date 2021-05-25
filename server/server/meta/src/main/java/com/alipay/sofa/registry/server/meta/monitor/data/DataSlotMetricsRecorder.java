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
package com.alipay.sofa.registry.server.meta.monitor.data;

import com.alipay.sofa.registry.common.model.slot.DataNodeSlot;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.server.meta.monitor.Metrics;
import com.alipay.sofa.registry.server.shared.slot.SlotTableRecorder;
import java.util.List;

/**
 * @author chen.zhu
 *     <p>Mar 03, 2021
 */
public class DataSlotMetricsRecorder implements SlotTableRecorder {
  @Override
  public void record(SlotTable slotTable) {
    List<DataNodeSlot> dataNodeSlots = slotTable.transfer(null, false);
    // clear the gauge
    Metrics.DataSlot.clearLeaderNumbers();
    Metrics.DataSlot.clearFollowerNumbers();
    dataNodeSlots.forEach(
        dataNodeSlot -> {
          Metrics.DataSlot.setLeaderNumbers(
              dataNodeSlot.getDataNode(), dataNodeSlot.getLeaders().size());
          Metrics.DataSlot.setFollowerNumbers(
              dataNodeSlot.getDataNode(), dataNodeSlot.getFollowers().size());
        });
  }
}
