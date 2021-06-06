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
package com.alipay.sofa.registry.server.shared.slot;

import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.util.JsonUtils;

/**
 * @author chen.zhu
 *     <p>Dec 25, 2020
 */
public class DiskSlotTableRecorder implements SlotTableRecorder {

  private static final Logger logger = LoggerFactory.getLogger(DiskSlotTableRecorder.class);

  private volatile SlotTable lastRecord;

  @Override
  public void record(SlotTable slotTable) {
    try {
      if (lastRecord == null || lastRecord.getEpoch() != slotTable.getEpoch()) {
        String slotStr = JsonUtils.writeValueAsString(slotTable);
        logger.info("[record] record slot: {}", slotStr);
        this.lastRecord = slotTable;
      }
    } catch (Throwable e) {
      logger.error("[record]", e);
    }
  }
}
