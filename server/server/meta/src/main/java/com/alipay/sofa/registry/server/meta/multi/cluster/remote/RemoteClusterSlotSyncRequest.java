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
package com.alipay.sofa.registry.server.meta.multi.cluster.remote;

import java.io.Serializable;

/**
 * @author xiaojian.xj
 * @version : RemoteClusterSlotSyncRequest.java, v 0.1 2022年04月15日 21:29 xiaojian.xj Exp $
 */
public class RemoteClusterSlotSyncRequest implements Serializable {

  private static final long serialVersionUID = -7873925175337400773L;

  /** remote data center */
  private final String dataCenter;

  /** slot table epoch */
  private final long slotTableEpoch;

  public RemoteClusterSlotSyncRequest(String dataCenter, long slotTableEpoch) {
    this.dataCenter = dataCenter;
    this.slotTableEpoch = slotTableEpoch;
  }

  /**
   * Getter method for property <tt>dataCenter</tt>.
   *
   * @return property value of dataCenter
   */
  public String getDataCenter() {
    return dataCenter;
  }

  /**
   * Getter method for property <tt>slotTableEpoch</tt>.
   *
   * @return property value of slotTableEpoch
   */
  public long getSlotTableEpoch() {
    return slotTableEpoch;
  }

  @Override
  public String toString() {
    return "RemoteClusterSlotSyncRequest{"
        + "dataCenter='"
        + dataCenter
        + '\''
        + ", slotTableEpoch="
        + slotTableEpoch
        + '}';
  }
}
