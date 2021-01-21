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

import com.alipay.sofa.registry.common.model.slot.DataNodeSlot;
import com.alipay.sofa.registry.server.meta.slot.util.SlotTableBuilder;

import java.util.Comparator;

/**
 * @author chen.zhu
 * <p>
 * Jan 18, 2021
 */
public class DataServerLeaderSizeComparator implements Comparator<String> {

    private SlotTableBuilder slotTableBuilder;

    private SortType         sortType;

    public DataServerLeaderSizeComparator(SlotTableBuilder slotTableBuilder, SortType sortType) {
        this.slotTableBuilder = slotTableBuilder;
        this.sortType = sortType;
    }

    @Override
    public int compare(String dataServer1, String dataServer2) {
        DataNodeSlot dataNodeSlot1 = slotTableBuilder.getDataNodeSlot(dataServer1);
        DataNodeSlot dataNodeSlot2 = slotTableBuilder.getDataNodeSlot(dataServer2);
        int score = dataNodeSlot1.getLeaders().size() - dataNodeSlot2.getLeaders().size();
        if (score == 0) {
            score = dataNodeSlot1.getFollowers().size() - dataNodeSlot2.getFollowers().size();
        }
        return sortType.getScore(score);
    }
}
