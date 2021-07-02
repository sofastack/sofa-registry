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

  public static final String PROP_LEADER_MAX_MOVE = "registry.slot.leader.max.move";
  public static final String PROP_FOLLOWER_MAX_MOVE = "registry.slot.follower.max.move";
  public static final String PROP_BALANCE_THRESHOLD = "registry.slot.balance.threshold";

  public static final int DEF_LEADER_MAX_MOVE = 6;

  private int balanceThreshold = Integer.getInteger(PROP_BALANCE_THRESHOLD, 10);

  private int maxMoveLeaderSlots = Integer.getInteger(PROP_LEADER_MAX_MOVE, DEF_LEADER_MAX_MOVE);

  private int maxMoveFollowerSlots = Integer.getInteger(PROP_FOLLOWER_MAX_MOVE, 10);

  @Override
  public int getLowWaterMarkSlotLeaderNums(int average) {
    // round down
    return average * (100 - balanceThreshold) / 100;
  }

  @Override
  public int getHighWaterMarkSlotLeaderNums(int average) {
    // round up
    return MathUtils.divideCeil(average * (100 + balanceThreshold), 100);
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

  public void setBalanceThreshold(int balanceThreshold) {
    this.balanceThreshold = balanceThreshold;
  }

  public void setMaxMoveLeaderSlots(int maxMoveLeaderSlots) {
    this.maxMoveLeaderSlots = maxMoveLeaderSlots;
  }

  public void setMaxMoveFollowerSlots(int maxMoveFollowerSlots) {
    this.maxMoveFollowerSlots = maxMoveFollowerSlots;
  }
}
