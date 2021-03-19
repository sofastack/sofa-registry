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

/**
 * @author chen.zhu
 *     <p>Jan 20, 2021
 */
public interface BalancePolicy {

  /**
   * Gets get low water mark slot leader nums. low watermark means the threshold we could endure
   * with, that once the slot leader numbers on a data-server is under the low water mark, we need
   * to rebalance the slot-table
   *
   * @param average the average
   * @return the get low water mark slot leader nums
   */
  int getLowWaterMarkSlotLeaderNums(int average);

  int getHighWaterMarkSlotLeaderNums(int average);

  /**
   * Gets get low water mark of follower slot nums. low watermark means the threshold we could
   * endure with, that once the follower slot numbers on a data-server is under the low water mark,
   * we need to rebalance the slot-table
   *
   * @param average the average
   * @return the get low water mark of slot follower nums
   */
  int getLowWaterMarkSlotFollowerNums(int average);

  int getHighWaterMarkSlotFollowerNums(int average);

  /**
   * Gets get max move leader slots. max move means the maximum number of slots' leader we are good
   * to migrate per balance time
   *
   * @return the get max move leader slots
   */
  int getMaxMoveLeaderSlots();

  /**
   * Gets get max move follower slots. max move means the maximum number of slots' followers we are
   * good to migrate per balance time
   *
   * @return the get max move follower slots
   */
  int getMaxMoveFollowerSlots();
}
