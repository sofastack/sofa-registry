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

/**
 * @author yuzhi.lyz
 * @version v 0.1 2020-11-06 16:21 yuzhi.lyz Exp $
 */
public interface SlotTableCache {
  /**
   * Slot of int.
   *
   * @param dataInfoId the data info id
   * @return the int
   */
  int slotOf(String dataInfoId);

  /**
   * Gets get slot.
   *
   * @param dataInfoId the data info id
   * @return the get slot
   */
  Slot getSlot(String dataInfoId);

  /**
   * Gets get slot.
   *
   * @param slotId the slot id
   * @return the get slot
   */
  Slot getSlot(int slotId);

  /**
   * Gets get leader.
   *
   * @param slotId the slot id
   * @return the get leader
   */
  String getLeader(int slotId);

  /**
   * Gets get epoch.
   *
   * @return the get epoch
   */
  long getEpoch();

  /**
   * Update slot table boolean.
   *
   * @param slotTable the slot table
   * @return the boolean
   */
  boolean updateSlotTable(SlotTable slotTable);

  /**
   * Gets get current slot table.
   *
   * @return the get current slot table
   */
  SlotTable getCurrentSlotTable();
}
