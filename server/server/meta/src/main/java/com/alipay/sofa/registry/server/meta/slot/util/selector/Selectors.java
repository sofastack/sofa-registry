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
package com.alipay.sofa.registry.server.meta.slot.util.selector;

import com.alipay.sofa.registry.server.meta.slot.util.builder.SlotTableBuilder;
import com.alipay.sofa.registry.server.meta.slot.util.comparator.Comparators;
import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author chen.zhu
 * <p>
 * Jan 27, 2021
 */
public class Selectors {

    public static DefaultSlotLeaderSelector slotLeaderSelector(SlotTableBuilder slotTableBuilder,
                                                               int slotId) {
        return new DefaultSlotLeaderSelector(slotTableBuilder, slotId);
    }

    public static MostLeaderFirstSelector mostLeaderFirst(SlotTableBuilder slotTableBuilder) {
        return new MostLeaderFirstSelector(slotTableBuilder);
    }

    public static MostFollowerFirstSelector mostFollowerFirst(SlotTableBuilder slotTableBuilder) {
        return new MostFollowerFirstSelector(slotTableBuilder);
    }

    public static abstract class AbstractSlotTableBuilderAwareSelector<T> implements Selector<T> {

        protected final SlotTableBuilder slotTableBuilder;

        public AbstractSlotTableBuilderAwareSelector(SlotTableBuilder slotTableBuilder) {
            this.slotTableBuilder = slotTableBuilder;
        }
    }

    public static abstract class AbstractDataServerSelector
                                                           extends
                                                           AbstractSlotTableBuilderAwareSelector<String> {

        public AbstractDataServerSelector(SlotTableBuilder slotTableBuilder) {
            super(slotTableBuilder);
        }

        @Override
        public String select(Collection<String> candidates) {
            List<String> sortedCandidates = Lists.newArrayList(candidates);
            sortedCandidates.sort(getDataServerComparator());
            return sortedCandidates.isEmpty() ? null : sortedCandidates.get(0);
        }

        protected abstract Comparators.AbstractDataServerComparator getDataServerComparator();

    }

    public static class MostFollowerFirstSelector extends AbstractDataServerSelector {

        private final Comparators.AbstractDataServerComparator comparator;

        public MostFollowerFirstSelector(SlotTableBuilder slotTableBuilder) {
            super(slotTableBuilder);
            this.comparator = Comparators.mostFollowersFirst(slotTableBuilder);
        }

        @Override
        protected Comparators.AbstractDataServerComparator getDataServerComparator() {
            return comparator;
        }
    }

    public static class LeastFollowerFirstSelector extends AbstractDataServerSelector {

        private final Comparators.AbstractDataServerComparator comparator;

        public LeastFollowerFirstSelector(SlotTableBuilder slotTableBuilder) {
            super(slotTableBuilder);
            this.comparator = Comparators.leastFollowersFirst(slotTableBuilder);
        }

        @Override
        protected Comparators.AbstractDataServerComparator getDataServerComparator() {
            return comparator;
        }
    }

    public static class LeastLeaderFirstSelector extends AbstractDataServerSelector {

        private final Comparators.AbstractDataServerComparator comparator;

        public LeastLeaderFirstSelector(SlotTableBuilder slotTableBuilder) {
            super(slotTableBuilder);
            this.comparator = Comparators.leastLeadersFirst(slotTableBuilder);
        }

        @Override
        protected Comparators.AbstractDataServerComparator getDataServerComparator() {
            return comparator;
        }
    }

    public static class MostLeaderFirstSelector extends AbstractDataServerSelector {

        private final Comparators.AbstractDataServerComparator comparator;

        public MostLeaderFirstSelector(SlotTableBuilder slotTableBuilder) {
            super(slotTableBuilder);
            comparator = Comparators.mostLeadersFirst(slotTableBuilder);
        }

        @Override
        protected Comparators.AbstractDataServerComparator getDataServerComparator() {
            return comparator;
        }
    }

    public static class DefaultSlotLeaderSelector implements Selector<String> {

        private final SlotTableBuilder slotTableBuilder;

        private final int              slotId;

        public DefaultSlotLeaderSelector(SlotTableBuilder slotTableBuilder, int slotId) {
            this.slotTableBuilder = slotTableBuilder;
            this.slotId = slotId;
        }

        @Override
        public String select(Collection<String> candidates) {
            Set<String> currentFollowers = slotTableBuilder.getOrCreate(slotId).getFollowers();
            Collection<String> followerCandidates = Lists.newArrayList(candidates);
            followerCandidates.retainAll(currentFollowers);
            followerCandidates = followerCandidates.isEmpty() ? candidates : followerCandidates;
            return new LeastLeaderFirstSelector(slotTableBuilder).select(followerCandidates);
        }

    }
}
