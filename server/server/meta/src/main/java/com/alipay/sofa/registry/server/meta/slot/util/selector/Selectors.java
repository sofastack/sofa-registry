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

import com.alipay.sofa.registry.common.model.slot.DataNodeSlot;
import com.alipay.sofa.registry.server.meta.slot.util.builder.SlotTableBuilder;
import com.alipay.sofa.registry.server.meta.slot.util.comparator.Comparators;
import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author chen.zhu
 *     <p>Jan 27, 2021
 */
public class Selectors {

  public static Selector<String> slotLeaderSelector(
      int highWaterMark, SlotTableBuilder slotTableBuilder, int slotId) {
    return new DefaultSlotLeaderSelector(highWaterMark, slotTableBuilder, slotId);
  }

  abstract static class AbstractSlotTableBuilderAwareSelector<T> implements Selector<T> {

    protected final SlotTableBuilder slotTableBuilder;

    public AbstractSlotTableBuilderAwareSelector(SlotTableBuilder slotTableBuilder) {
      this.slotTableBuilder = slotTableBuilder;
    }
  }

  abstract static class AbstractDataServerSelector
      extends AbstractSlotTableBuilderAwareSelector<String> {

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

  static class LeastLeaderFirstSelector extends AbstractDataServerSelector {

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

  static class DefaultSlotLeaderSelector implements Selector<String> {

    private final SlotTableBuilder slotTableBuilder;
    private final int highWaterMark;
    private final int slotId;

    public DefaultSlotLeaderSelector(
        int highWaterMark, SlotTableBuilder slotTableBuilder, int slotId) {
      this.highWaterMark = highWaterMark;
      this.slotTableBuilder = slotTableBuilder;
      this.slotId = slotId;
    }

    @Override
    public String select(Collection<String> candidates) {
      Set<String> currentFollowers = slotTableBuilder.getOrCreate(slotId).getFollowers();
      Collection<String> followerCandidates = Lists.newArrayList(candidates);
      followerCandidates.retainAll(currentFollowers);
      // first, try to select the candidate which is the follower
      String leader = new LeastLeaderFirstSelector(slotTableBuilder).select(followerCandidates);
      if (leader != null) {
        // check the num of leaders
        DataNodeSlot dataNodeSlot = slotTableBuilder.getDataNodeSlot(leader);
        if (dataNodeSlot.getLeaders().size() < highWaterMark) {
          return leader;
        }
      }
      // second, find other candidate
      return new LeastLeaderFirstSelector(slotTableBuilder).select(candidates);
    }
  }
}
