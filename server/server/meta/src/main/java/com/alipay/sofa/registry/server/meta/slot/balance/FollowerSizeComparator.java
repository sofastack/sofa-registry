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

import com.alipay.sofa.registry.server.meta.slot.util.SlotBuilder;
import com.alipay.sofa.registry.server.meta.slot.util.SlotTableBuilder;

import java.util.Comparator;

/**
 * @author chen.zhu
 * <p>
 * Jan 18, 2021
 */
public class FollowerSizeComparator implements Comparator<Integer> {

    private SlotTableBuilder slotTableBuilder;

    private SortType         sortType;

    public FollowerSizeComparator(SlotTableBuilder slotTableBuilder, SortType sortType) {
        this.slotTableBuilder = slotTableBuilder;
        this.sortType = sortType;
    }

    @Override
    public int compare(Integer slotId1, Integer slotId2) {
        SlotBuilder slotBuilder1 = slotTableBuilder.getOrCreate(slotId1);
        SlotBuilder slotBuilder2 = slotTableBuilder.getOrCreate(slotId2);
        return sortType.getScore(slotBuilder1.getFollowers().size()
                                 - slotBuilder2.getFollowers().size());
    }
}
