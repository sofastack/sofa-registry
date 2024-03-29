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

import com.alipay.sofa.registry.common.model.slot.BaseSlotStatus;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import java.util.List;
import java.util.Set;

/**
 * @author yuzhi.lyz
 * @version v 0.1 2020-10-30 10:46 yuzhi.lyz Exp $
 */
public interface SlotManager extends SlotAccessor {

  boolean hasSlot();

  List<BaseSlotStatus> getSlotStatuses();

  boolean updateSlotTable(SlotTable slotTable);

  long getSlotTableEpoch();

  void triggerUpdateSlotTable(long epoch);

  Set<Integer> leaderSlotIds();
}
