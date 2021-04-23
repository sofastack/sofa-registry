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
package com.alipay.sofa.registry.common.model.metaserver;

import com.alipay.sofa.registry.common.model.slot.DataNodeSlot;
import java.util.List;

/**
 * @author yuzhi.lyz
 * @version v 0.1 2020-11-11 13:48 yuzhi.lyz Exp $
 */
public class GetSlotTableResult {
  private final long epoch;
  private final List<DataNodeSlot> slots;

  public GetSlotTableResult(long epoch, List<DataNodeSlot> slots) {
    this.epoch = epoch;
    this.slots = slots;
  }

  /**
   * Getter method for property <tt>epoch</tt>.
   *
   * @return property value of epoch
   */
  public long getEpoch() {
    return epoch;
  }

  /**
   * Getter method for property <tt>slots</tt>.
   *
   * @return property value of slots
   */
  public List<DataNodeSlot> getSlots() {
    return slots;
  }

  @Override
  public String toString() {
    return "GetSlotTableResult{" + "epoch=" + epoch + ", slots=" + slots + '}';
  }
}
