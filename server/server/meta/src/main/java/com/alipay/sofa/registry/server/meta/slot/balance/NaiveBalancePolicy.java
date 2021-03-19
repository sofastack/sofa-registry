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
package com.alipay.sofa.registry.server.meta.slot.balance;

import com.alipay.sofa.registry.util.MathUtils;

/**
 * @author chen.zhu
 *     <p>Jan 20, 2021
 */
public class NaiveBalancePolicy implements BalancePolicy {

  // "final" instead of "static final" to make it dynamic and flexible
  private final int threshold = Integer.getInteger("slot.threshold", 10);

  // "final" instead of "static final" to make it dynamic and flexible
  private final int maxMoveLeaderSlots = Integer.getInteger("slot.leader.max.move", 2);

  private final int maxMoveFollowerSlots = Integer.getInteger("slot.follower.max.move", 10);

  @Override
  public int getLowWaterMarkSlotLeaderNums(int average) {
    // round down
    return average * (100 - threshold) / 100;
  }

  @Override
  public int getHighWaterMarkSlotLeaderNums(int average) {
    // round up
    return MathUtils.divideCeil(average * (100 + threshold), 100);
  }

  @Override
  public int getLowWaterMarkSlotFollowerNums(int average) {
    // same as getLowWaterMarkSlotLeaderNums
    return getLowWaterMarkSlotLeaderNums(average);
  }

  @Override
  public int getHighWaterMarkSlotFollowerNums(int average) {
    // same as getHighWaterMarkSlotLeaderNums
    return getHighWaterMarkSlotLeaderNums(average);
  }

  @Override
  public int getMaxMoveLeaderSlots() {
    return maxMoveLeaderSlots;
  }

  @Override
  public int getMaxMoveFollowerSlots() {
    return maxMoveFollowerSlots;
  }
}
