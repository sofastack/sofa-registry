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

import com.alipay.sofa.registry.common.model.multi.cluster.RemoteSlotTableStatus;
import com.alipay.sofa.registry.common.model.slot.Slot;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import java.util.Map;

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
   * @param dataCenter dataCenter
   * @return the get slot
   */
  Slot getSlot(String dataCenter, String dataInfoId);

  /**
   * Gets get slot.
   *
   * @param slotId the slot id
   * @param dataCenter dataCenter
   * @return the get slot
   */
  Slot getSlot(String dataCenter, int slotId);

  /**
   * Gets get leader.
   *
   * @param slotId the slot id
   * @param dataCenter dataCenter
   * @return the get leader
   */
  String getLeader(String dataCenter, int slotId);

  /**
   * Gets get epoch.
   *
   * @param dataCenter dataCenter
   * @return the get epoch
   */
  long getEpoch(String dataCenter);

  /**
   * Update slot table boolean.
   *
   * @param slotTable the slot table
   * @return the boolean
   */
  boolean updateLocalSlotTable(SlotTable slotTable);

  boolean updateRemoteSlotTable(Map<String, RemoteSlotTableStatus> remoteSlotTableStatus);

  /**
   * Gets get current slot table.
   *
   * @return the get current slot table
   */
  SlotTable getLocalSlotTable();

  Map<String, Long> getRemoteSlotTableEpoch();

  SlotTable getSlotTable(String dataCenter);
}
