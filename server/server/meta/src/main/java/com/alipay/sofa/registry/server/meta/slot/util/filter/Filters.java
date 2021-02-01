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
package com.alipay.sofa.registry.server.meta.slot.util.filter;

import com.alipay.sofa.registry.common.model.slot.DataNodeSlot;
import com.alipay.sofa.registry.common.model.slot.Slot;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.meta.slot.balance.DefaultSlotBalancer;
import com.alipay.sofa.registry.server.meta.slot.util.builder.SlotBuilder;
import com.alipay.sofa.registry.server.meta.slot.util.builder.SlotTableBuilder;
import com.alipay.sofa.registry.util.DatumVersionUtil;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author chen.zhu
 * <p>
 * Jan 27, 2021
 */
public class Filters {

    public static DefaultBalanceLeaderFilter balanceLeaderFilter(DefaultSlotBalancer slotBalancer,
                                                                 List<String> targetDataServers) {
        return new DefaultBalanceLeaderFilter(slotBalancer, targetDataServers);
    }

    public static DefaultBalanceFollowerFilter balanceFollowerFilter(DefaultSlotBalancer slotBalancer,
                                                                     List<String> targetDataServers) {
        return new DefaultBalanceFollowerFilter(slotBalancer, targetDataServers);
    }

    public static DefaultCandidateDataServerFilter candidateDataServerFilter(DefaultSlotBalancer slotBalancer,
                                                                             Set<String> targetDataServers,
                                                                             int average) {
        return new DefaultCandidateDataServerFilter(slotBalancer, targetDataServers, average);
    }

    public static class DefaultBalanceFollowerFilter implements Filter<Integer> {

        private final FilterChain<Integer> filterChain;

        public DefaultBalanceFollowerFilter(DefaultSlotBalancer slotBalancer,
                                            List<String> targetDataServers) {
            this.filterChain = createFilterChain(new SlotFrozenFilter(slotBalancer),
                new FollowerTargetDataServerNotInCandidates(slotBalancer.getSlotTableBuilder(),
                    targetDataServers));
        }

        @Override
        public List<Integer> filter(List<Integer> candidates) {
            return filterChain.filter(candidates);
        }
    }

    public static class DefaultBalanceLeaderFilter implements Filter<Integer> {

        private final FilterChain<Integer> filterChain;

        public DefaultBalanceLeaderFilter(DefaultSlotBalancer slotBalancer,
                                          List<String> targetDataServers) {
            this.filterChain = createFilterChain(new SlotFrozenFilter(slotBalancer),
                new LeaderTargetDataServerNotInCandidates(slotBalancer.getSlotTableBuilder(),
                    targetDataServers));
        }

        @Override
        public List<Integer> filter(List<Integer> candidates) {
            return filterChain.filter(candidates);
        }
    }

    public static class DefaultCandidateDataServerFilter implements Filter<String> {

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

    public static <T> DefaultFilterChain<T> createFilterChain(Filter<T>... inputs) {
        return new DefaultFilterChain<T>(inputs);
    }

    public static class DefaultFilterChain<T> implements FilterChain<T> {

        private List<Filter<T>> filters = Lists.newArrayList();

        public DefaultFilterChain() {
        }

        public DefaultFilterChain(Filter<T>... inputs) {
            filters = Lists.newArrayList(inputs);
        }

        @Override
        public void addFilter(Filter<T> filter) {
            filters.add(filter);
        }

        @Override
        public List<Filter<T>> getFilters() {
            return Lists.newArrayList(filters);
        }

        @Override
        public List<T> filter(List<T> candidates) {
            List<T> result = candidates;
            for (Filter<T> filter : this.filters) {
                result = filter.filter(result);
            }
            return result;
        }
    }

    public static class SlotFrozenFilter implements Filter<Integer> {

        private static final Logger logger              = LoggerFactory
                                                            .getLogger(SlotFrozenFilter.class);

        private DefaultSlotBalancer slotBalancer;

        // "final" instead of "static final" to make the config dynamic and flexible
        private final long          leastChangeInterval = Long.getLong("slot.frozen.milli",
                                                            TimeUnit.SECONDS.toMillis(5));

        public SlotFrozenFilter(DefaultSlotBalancer slotBalancer) {
            this.slotBalancer = slotBalancer;
        }

        @Override
        public List<Integer> filter(List<Integer> candidates) {
            return candidates
                    .stream()
                    .filter(slotId->{
                        Slot slot = slotBalancer.getPrevSlotTable().getSlot(slotId);
                        if(slot == null) {
                            return false;
                        }
                        long epoch = slot.getLeaderEpoch();
                        long lastUpdate = DatumVersionUtil.getRealTimestamp(epoch);
                        boolean result = System.currentTimeMillis() - lastUpdate > leastChangeInterval;
                        if(!result) {
                            if(logger.isInfoEnabled()) {
                                logger.info("[filter] slot[{}] cannot balance for update too frequent," +
                                                " current - lastUpdate ({} - {} = {}ms), leastChangeInterval ({} ms)",
                                        slotId,
                                        System.currentTimeMillis(), lastUpdate, System.currentTimeMillis() - lastUpdate,
                                        leastChangeInterval);
                            }
                        }
                        return result;
                    })
                    .collect(Collectors.toList());
        }
    }

    private static class FollowerTargetDataServerNotInCandidates implements Filter<Integer> {

        private SlotTableBuilder slotTableBuilder;

        private List<String>     targetDataServers;

        public FollowerTargetDataServerNotInCandidates(SlotTableBuilder slotTableBuilder,
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

    private static class LeaderTargetDataServerNotInCandidates implements Filter<Integer> {

        private SlotTableBuilder slotTableBuilder;

        private List<String>     targetDataServers;

        public LeaderTargetDataServerNotInCandidates(SlotTableBuilder slotTableBuilder,
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
                        return !targetDataServers.contains(slotBuilder.getLeader());
                    })
                    .collect(Collectors.toList());
        }
    }
}
