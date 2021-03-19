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
package com.alipay.sofa.registry.server.shared.config;

/**
 * @author chen.zhu
 *     <p>Dec 15, 2020
 */
public class DefaultSlotTableConfig implements SlotTableConfig {

  public static final String SLOT_NUMS = "slot.nums";

  public static final String SLOT_REPLICA_NUMS = "slot.replica.nums";

  private final int slotNums = Integer.getInteger(SLOT_NUMS, 2);

  private final int slotReplicaNums = Integer.getInteger(SLOT_REPLICA_NUMS, 1);

  @Override
  public int getSlotNums() {
    return slotNums;
  }

  @Override
  public int getSlotReplicaNums() {
    return slotReplicaNums;
  }
}
