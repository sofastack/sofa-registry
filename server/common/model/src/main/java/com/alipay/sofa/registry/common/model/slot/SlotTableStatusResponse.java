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

import java.io.Serializable;
import java.util.Map;

public class SlotTableStatusResponse implements Serializable {

  private final long slotTableEpoch;

  private final boolean isSlotTableLeaderBalanced;
  private final boolean isSlotTableFollowerBalanced;

  private final boolean isSlotTableStable;
  private final boolean protectionMode;

  private final Map<String, Integer> leaderCount;

  private final Map<String, Integer> followerCount;

  public SlotTableStatusResponse(
      long slotTableEpoch,
      boolean slotTableLeaderBalanced,
      boolean slotTableFollowerBalanced,
      boolean slotTableStable,
      boolean protectionMode,
      Map<String, Integer> leaderCount,
      Map<String, Integer> followerCount) {
    this.slotTableEpoch = slotTableEpoch;
    this.isSlotTableLeaderBalanced = slotTableLeaderBalanced;
    this.isSlotTableFollowerBalanced = slotTableFollowerBalanced;
    this.protectionMode = protectionMode;
    this.isSlotTableStable = slotTableStable;
    this.leaderCount = leaderCount;
    this.followerCount = followerCount;
  }

  public boolean isSlotTableStable() {
    return isSlotTableStable;
  }

  public Map<String, Integer> getLeaderCount() {
    return leaderCount;
  }

  public Map<String, Integer> getFollowerCount() {
    return followerCount;
  }

  public boolean isSlotTableLeaderBalanced() {
    return isSlotTableLeaderBalanced;
  }

  public boolean isSlotTableFollowerBalanced() {
    return isSlotTableFollowerBalanced;
  }

  public long getSlotTableEpoch() {
    return slotTableEpoch;
  }

  public boolean isProtectionMode() {
    return protectionMode;
  }

  @Override
  public String toString() {
    return "SlotTableStatusResponse{"
        + "slotTableEpoch="
        + slotTableEpoch
        + ", isSlotTableLeaderBalanced="
        + isSlotTableLeaderBalanced
        + ", isSlotTableFollowerBalanced="
        + isSlotTableFollowerBalanced
        + ", isSlotTableStable="
        + isSlotTableStable
        + ", protectionMode="
        + protectionMode
        + ", leaderCount="
        + leaderCount
        + ", followerCount="
        + followerCount
        + '}';
  }
}
