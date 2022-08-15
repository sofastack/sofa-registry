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
package com.alipay.sofa.registry.common.model.slot;

import com.alipay.sofa.registry.common.model.dataserver.DatumSummary;
import com.alipay.sofa.registry.common.model.slot.filter.SyncSlotAcceptorManager;
import com.google.common.collect.Lists;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * @author yuzhi.lyz
 * @version v 0.1 2020-11-05 14:24 yuzhi.lyz Exp $
 */
public class DataSlotDiffPublisherRequest implements Serializable {

  private final String localDataCenter;
  private final long slotTableEpoch;
  // parts of the summary to diff sync
  private final List<DatumSummary> datumSummaries;
  private final int slotId;

  private final SyncSlotAcceptorManager acceptorManager;

  public DataSlotDiffPublisherRequest(
      String localDataCenter,
      long slotTableEpoch,
      int slotId,
      SyncSlotAcceptorManager acceptorManager,
      Collection<DatumSummary> datumSummaries) {
    this.localDataCenter = localDataCenter;
    this.slotTableEpoch = slotTableEpoch;
    this.slotId = slotId;
    this.acceptorManager = acceptorManager;
    this.datumSummaries =
        datumSummaries == null
            ? Lists.newArrayListWithCapacity(0)
            : Lists.newArrayList(datumSummaries);
  }

  /**
   * Getter method for property <tt>localDataCenter</tt>.
   *
   * @return property value of localDataCenter
   */
  public String getLocalDataCenter() {
    return localDataCenter;
  }

  /**
   * Getter method for property <tt>slotId</tt>.
   *
   * @return property value of slotId
   */
  public int getSlotId() {
    return slotId;
  }

  /**
   * Getter method for property <tt>slotTableEpoch</tt>.
   *
   * @return property value of slotTableEpoch
   */
  public long getSlotTableEpoch() {
    return slotTableEpoch;
  }

  public List<DatumSummary> getDatumSummaries() {
    return datumSummaries;
  }

  /**
   * Getter method for property <tt>acceptorManager</tt>.
   *
   * @return property value of acceptorManager
   */
  public SyncSlotAcceptorManager getAcceptorManager() {
    return acceptorManager;
  }

  @Override
  public String toString() {
    return "DiffPublisher{"
        + "slotId="
        + slotId
        + ", epoch="
        + slotTableEpoch
        + ", summaries="
        + datumSummaries.size()
        + '}';
  }
}
