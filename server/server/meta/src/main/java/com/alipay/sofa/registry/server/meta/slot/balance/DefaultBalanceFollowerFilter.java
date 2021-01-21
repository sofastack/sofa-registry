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

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author chen.zhu
 * <p>
 * Jan 18, 2021
 */
public class DefaultBalanceFollowerFilter implements Filter<Integer> {

    private DefaultSlotBalancer slotBalancer;

    private List<String>        targetDataServers;

    public DefaultBalanceFollowerFilter(DefaultSlotBalancer slotBalancer,
                                        List<String> targetDataServers) {
        this.slotBalancer = slotBalancer;
        this.targetDataServers = targetDataServers;
    }

    @Override
    public List<Integer> filter(List<Integer> candidates) {
        return new TargetDataServerNotInCandidates(slotBalancer.getSlotTableBuilder(),
            targetDataServers).filter(new SlotFrozenFilter(slotBalancer).filter(candidates));
    }

    private static class TargetDataServerNotInCandidates implements Filter<Integer> {

        private SlotTableBuilder slotTableBuilder;

        private List<String>     targetDataServers;

        public TargetDataServerNotInCandidates(SlotTableBuilder slotTableBuilder,
                                               List<String> targetDataServers) {
            this.slotTableBuilder = slotTableBuilder;
            this.targetDataServers = targetDataServers;
        }

        @Override
        public List<Integer> filter(List<Integer> candidates) {
            return candidates
                    .stream()
                    .filter(slotId->{
                        SlotBuilder slotBuilder = slotTableBuilder.getOrCreate(slotId);
                        return !targetDataServers.containsAll(slotBuilder.getFollowers());
                    })
                    .collect(Collectors.toList());
        }
    }
}
