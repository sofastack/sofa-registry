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
package com.alipay.sofa.registry.server.meta.slot.assigner;

import com.alipay.sofa.registry.server.meta.slot.util.SlotBuilder;

/**
 * @author chen.zhu
 * <p>
 * Jan 15, 2021
 */
public class LeaderEmergentScoreJury implements ScoreStrategy<Integer> {

    private DefaultSlotAssigner assigner;

    public LeaderEmergentScoreJury(DefaultSlotAssigner assigner) {
        this.assigner = assigner;
    }

    @Override
    public int score(Integer slotId) {
        SlotBuilder slotBuilder = assigner.getSlotTableBuilder().getOrCreate(slotId);
        int followerNums = slotBuilder.getFollowers().size();
        if (followerNums == 0) {
            return Integer.MAX_VALUE;
        }
        return slotBuilder.getFollowers().size();
    }
}
