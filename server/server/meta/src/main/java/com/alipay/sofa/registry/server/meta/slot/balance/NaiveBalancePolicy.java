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
 * <p>
 * Jan 20, 2021
 */
public class NaiveBalancePolicy implements BalancePolicy {

    // "final" instead of "static final" to make it dynamic and flexible
    private final int threshold = Integer.getInteger("slot.threshold", 30);

    // "final" instead of "static final" to make it dynamic and flexible
    private final int maxMoveSlots = Integer.getInteger("slot.leader.max.move", 2);

    @Override
    public int getLowWaterMarkSlotNums(int average) {
        return average * threshold / 100;
    }

    @Override
    public int getMaxMoveFollowerSlots(int average, int totalSlotNum) {
        return Math.min(maxMoveSlots, average - totalSlotNum);
    }

    @Override
    public int getLowWaterMarkSlotLeaderNums(int average) {
        return average * threshold / 100;
    }

    @Override
    public int getMaxMoveLeaderSlots(int average, int leaderSlotNum) {
        return Math.min(maxMoveSlots, average - leaderSlotNum);
    }
}
