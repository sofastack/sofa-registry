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

import com.alipay.sofa.registry.common.model.slot.Slot;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.google.common.collect.Maps;
import java.util.Map;
import org.apache.commons.lang.StringUtils;

/**
 * @author chen.zhu
 *     <p>Jan 21, 2021
 */
public class SlotTableUtils {

  private static final Logger logger = LoggerFactory.getLogger(SlotTableUtils.class);

  public static Map<String, Integer> getSlotTableLeaderCount(SlotTable slotTable) {
    Map<String, Integer> leaderCounter =
        Maps.newHashMapWithExpectedSize(slotTable.getSlots().size());
    for (Map.Entry<Integer, Slot> entry : slotTable.getSlotMap().entrySet()) {
      Slot slot = entry.getValue();
      incrCount(leaderCounter, slot.getLeader());
    }
    return leaderCounter;
  }

  public static Map<String, Integer> getSlotTableFollowerCount(SlotTable slotTable) {
    Map<String, Integer> followerCounter = Maps.newHashMap();
    for (Slot slot : slotTable.getSlots()) {
      for (String follower : slot.getFollowers()) {
        incrCount(followerCounter, follower);
      }
    }
    return followerCounter;
  }

  public static Map<String, Integer> getSlotTableSlotCount(SlotTable slotTable) {
    Map<String, Integer> slotCounter = Maps.newHashMapWithExpectedSize(slotTable.getSlots().size());
    for (Map.Entry<Integer, Slot> entry : slotTable.getSlotMap().entrySet()) {
      Slot slot = entry.getValue();
      incrCount(slotCounter, slot.getLeader());
      for (String follower : slot.getFollowers()) {
        incrCount(slotCounter, follower);
      }
    }
    return slotCounter;
  }

  private static void incrCount(Map<String, Integer> counter, String dataServer) {
    Integer count = counter.get(dataServer);
    if (count == null) {
      count = 0;
    }
    counter.put(dataServer, count + 1);
  }

  public static boolean isValidSlotTable(SlotTable slotTable) {
    return checkNoDupLeaderAndFollowers(slotTable) && checkNoLeaderEmpty(slotTable);
  }

  public static boolean checkNoDupLeaderAndFollowers(SlotTable slotTable) {
    for (Map.Entry<Integer, Slot> entry : slotTable.getSlotMap().entrySet()) {
      Slot slot = entry.getValue();
      if (slot.getFollowers().contains(slot.getLeader())) {
        logger.error(
            "[checkNoDupLeaderAndFollowers] slot[{}] leader and follower duplicates", slot);
        return false;
      }
    }
    return true;
  }

  public static boolean checkNoLeaderEmpty(SlotTable slotTable) {
    for (Map.Entry<Integer, Slot> entry : slotTable.getSlotMap().entrySet()) {
      Slot slot = entry.getValue();
      if (StringUtils.isEmpty(slot.getLeader())) {
        logger.error("[checkNoLeaderEmpty] slot[{}] empty leader", slot);
        return false;
      }
    }
    return true;
  }
}
