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

import com.alipay.sofa.registry.common.model.Tuple;
import com.alipay.sofa.registry.common.model.slot.BaseSlotStatus;
import com.alipay.sofa.registry.common.model.slot.Slot;
import com.alipay.sofa.registry.common.model.slot.SlotAccess;
import com.alipay.sofa.registry.common.model.slot.func.SlotFunction;
import com.alipay.sofa.registry.common.model.slot.func.SlotFunctionRegistry;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.multi.cluster.slot.MultiClusterSlotManager;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author xiaojian.xj
 * @version : SlotAccessorDelegate.java, v 0.1 2022年05月23日 20:43 xiaojian.xj Exp $
 */
public class SlotAccessorDelegate implements SlotAccessor {

  @Autowired private DataServerConfig dataServerConfig;

  @Autowired private SlotManager slotManager;

  @Autowired private MultiClusterSlotManager multiClusterSlotManager;

  private final SlotFunction slotFunction = SlotFunctionRegistry.getFunc();

  @Override
  public int slotOf(String dataInfoId) {
    return slotFunction.slotOf(dataInfoId);
  }

  @Override
  public Slot getSlot(String dataCenter, int slotId) {
    return accessOf(dataCenter).getSlot(dataCenter, slotId);
  }

  @Override
  public SlotAccess checkSlotAccess(
      String dataCenter, int slotId, long srcSlotEpoch, long srcLeaderEpoch) {
    return accessOf(dataCenter).checkSlotAccess(dataCenter, slotId, srcSlotEpoch, srcLeaderEpoch);
  }

  @Override
  public boolean isLeader(String dataCenter, int slotId) {
    return accessOf(dataCenter).isLeader(dataCenter, slotId);
  }

  @Override
  public boolean isFollower(String dataCenter, int slotId) {
    return accessOf(dataCenter).isFollower(dataCenter, slotId);
  }

  @Override
  public Tuple<Long, List<BaseSlotStatus>> getSlotTableEpochAndStatuses(String dataCenter) {
    return accessOf(dataCenter).getSlotTableEpochAndStatuses(dataCenter);
  }

  private SlotAccessor accessOf(String dataCenter) {
    return StringUtils.equalsIgnoreCase(dataServerConfig.getLocalDataCenter(), dataCenter)
        ? slotManager
        : multiClusterSlotManager;
  }
}
