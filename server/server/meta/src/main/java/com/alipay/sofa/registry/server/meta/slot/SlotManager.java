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
package com.alipay.sofa.registry.server.meta.slot;

import com.alipay.sofa.registry.common.model.slot.DataNodeSlot;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.observer.Observable;

/**
 * @author chen.zhu
 *     <p>Nov 13, 2020
 */
public interface SlotManager extends SlotTableAware, Observable {

  /**
   * Refresh.
   *
   * @param slotTable the slot table
   */
  boolean refresh(SlotTable slotTable);

  /**
   * Gets get slot nums.
   *
   * @return the get slot nums
   */
  int getSlotNums();

  /**
   * Gets get slot replica nums.
   *
   * @return the get slot replica nums
   */
  int getSlotReplicaNums();

  /**
   * Gets get data node managed slot.
   *
   * @param dataNode the data node
   * @param ignoreFollowers the ignore followers
   * @return the get data node managed slot
   */
  DataNodeSlot getDataNodeManagedSlot(String dataNode, boolean ignoreFollowers);
}
