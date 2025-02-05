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

import com.alipay.sofa.registry.common.model.slot.BaseSlotStatus;
import java.util.List;

/**
 * @author chen.zhu
 *     <p>Feb 24, 2021
 */
public class DataServerStats {

  private final String dataServer;
  private final long slotTableEpoch;
  private final List<BaseSlotStatus> slotStatuses;

  /**
   * Constructor.
   *
   * @param dataServer dataServer
   * @param slotTableEpoch the slot table epoch
   * @param slotStatuses the slot status
   */
  public DataServerStats(
      String dataServer, long slotTableEpoch, List<BaseSlotStatus> slotStatuses) {
    this.dataServer = dataServer;
    this.slotTableEpoch = slotTableEpoch;
    this.slotStatuses = slotStatuses;
  }

  /**
   * Gets get slot table epoch.
   *
   * @return the get slot table epoch
   */
  public long getSlotTableEpoch() {
    return slotTableEpoch;
  }

  /**
   * Gets get slot status.
   *
   * @return the get slot status
   */
  public List<BaseSlotStatus> getSlotStatus() {
    return slotStatuses;
  }

  /**
   * Gets get data server.
   *
   * @return the get data server
   */
  public String getDataServer() {
    return dataServer;
  }

  @Override
  public String toString() {
    return "DataServerStats{"
        + "dataServer='"
        + dataServer
        + '\''
        + ", slotTableEpoch="
        + slotTableEpoch
        + ", slotStatuses="
        + slotStatuses
        + '}';
  }
}
