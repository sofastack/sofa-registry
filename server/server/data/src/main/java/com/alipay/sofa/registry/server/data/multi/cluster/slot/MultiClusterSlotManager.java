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
package com.alipay.sofa.registry.server.data.multi.cluster.slot;

import com.alipay.sofa.registry.common.model.multi.cluster.RemoteSlotTableStatus;
import com.alipay.sofa.registry.server.data.slot.SlotAccessor;
import java.util.Map;
import java.util.Set;

/**
 * @author xiaojian.xj
 * @version : MultiClusterSlotManager.java, v 0.1 2022年05月06日 15:04 xiaojian.xj Exp $
 */
public interface MultiClusterSlotManager extends SlotAccessor {

  /**
   * get remote cluster slotTable epoch
   *
   * @return map
   */
  public Map<String, Long> getSlotTableEpoch();

  /**
   * update remote slot table
   *
   * @param remoteSlotTableStatus remoteSlotTableStatus
   */
  void updateSlotTable(Map<String, RemoteSlotTableStatus> remoteSlotTableStatus);

  void dataChangeNotify(String dataCenter, Set<String> dataInfoIds);
}
