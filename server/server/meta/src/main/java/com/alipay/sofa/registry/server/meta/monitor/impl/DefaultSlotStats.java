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
package com.alipay.sofa.registry.server.meta.monitor.impl;

import com.alipay.sofa.registry.common.model.slot.Slot;
import com.alipay.sofa.registry.common.model.slot.SlotStatus;
import com.alipay.sofa.registry.server.meta.monitor.SlotStats;
import com.google.common.collect.Maps;

import java.util.Map;

/**
 * @author chen.zhu
 * <p>
 * Jan 28, 2021
 */
public class DefaultSlotStats implements SlotStats {

    private final Slot                 slot;

    private volatile SlotStatus.LeaderStatus leaderStatus = SlotStatus.LeaderStatus.INIT;

    private final Map<String, Long> followerOffsets = Maps.newConcurrentMap();

    public DefaultSlotStats(Slot slot) {
        this.slot = slot;
    }

    @Override
    public Slot getSlot() {
        return slot;
    }

    @Override
    public boolean isLeaderStable() {
        return leaderStatus.isHealthy();
    }

    @Override
    public void updateSlotState(SlotStatus.LeaderStatus state) {
        this.leaderStatus = state;
    }

}
