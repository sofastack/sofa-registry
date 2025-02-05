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
package com.alipay.sofa.registry.common.model.dataserver;

import com.alipay.sofa.registry.common.model.ProcessId;
import java.io.Serializable;

/**
 * @author yuzhi.lyz
 * @version v 0.1 2020-12-04 16:44 yuzhi.lyz Exp $
 */
public abstract class AbstractSlotRequest implements Serializable {
  protected final ProcessId sessionProcessId;
  private final int slotId;
  protected long slotTableEpoch;
  protected long slotLeaderEpoch;

  protected AbstractSlotRequest(int slotId, ProcessId sessionProcessId) {
    this.slotId = slotId;
    this.sessionProcessId = sessionProcessId;
  }

  protected AbstractSlotRequest(
      int slotId, ProcessId sessionProcessId, long slotTableEpoch, long slotLeaderEpoc) {
    this.slotId = slotId;
    this.sessionProcessId = sessionProcessId;
    this.slotTableEpoch = slotTableEpoch;
    this.slotLeaderEpoch = slotLeaderEpoc;
  }

  /**
   * Getter method for property <tt>sessionProcessId</tt>.
   *
   * @return property value of sessionProcessId
   */
  public ProcessId getSessionProcessId() {
    return sessionProcessId;
  }

  /**
   * Getter method for property <tt>slotTableEpoch</tt>.
   *
   * @return property value of slotTableEpoch
   */
  public long getSlotTableEpoch() {
    return slotTableEpoch;
  }

  /**
   * Setter method for property <tt>slotTableEpoch</tt>.
   *
   * @param slotTableEpoch value to be assigned to property slotTableEpoch
   */
  public void setSlotTableEpoch(long slotTableEpoch) {
    this.slotTableEpoch = slotTableEpoch;
  }

  public long getSlotLeaderEpoch() {
    return slotLeaderEpoch;
  }

  public void setSlotLeaderEpoch(long slotLeaderEpoch) {
    this.slotLeaderEpoch = slotLeaderEpoch;
  }

  public int getSlotId() {
    return slotId;
  }
}
