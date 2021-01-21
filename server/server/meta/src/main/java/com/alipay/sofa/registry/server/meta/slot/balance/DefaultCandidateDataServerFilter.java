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
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Set;

/**
 * @author chen.zhu
 * <p>
 * Jan 18, 2021
 */
public class DefaultCandidateDataServerFilter implements Filter<String> {

    private DefaultSlotBalancer slotBalancer;

    private Set<String>         targetDataServers;

    private int                 averageSlot;

    public DefaultCandidateDataServerFilter(DefaultSlotBalancer slotBalancer,
                                            Set<String> targetDataServers, int averageSlot) {
        this.slotBalancer = slotBalancer;
        this.targetDataServers = targetDataServers;
        this.averageSlot = averageSlot;
    }

    @Override
    public List<String> filter(List<String> candidates) {
        List<String> result = Lists.newArrayList();
        SlotTableBuilder slotTableBuilder = slotBalancer.getSlotTableBuilder();
        List<DataNodeSlot> dataNodeSlots = slotTableBuilder.getDataNodeTotalSlotsBeyond(averageSlot);
        dataNodeSlots.forEach(dataNodeSlot -> result.add(dataNodeSlot.getDataNode()));
        result.removeAll(targetDataServers);
        return result;
    }
}
