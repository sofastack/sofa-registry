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
package com.alipay.sofa.registry.server.meta.slot.util.comparator;

import com.alipay.sofa.registry.common.model.slot.DataNodeSlot;
import com.alipay.sofa.registry.server.meta.slot.util.builder.SlotTableBuilder;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

/**
 * @author chen.zhu
 *     <p>Jan 27, 2021
 */
public class Comparators {

  public static DataServerFollowerSizeComparator mostFollowersFirst(
      SlotTableBuilder slotTableBuilder) {
    return new DataServerFollowerSizeComparator(slotTableBuilder, SortType.DES);
  }

  public static DataServerFollowerSizeComparator leastFollowersFirst(
      SlotTableBuilder slotTableBuilder) {
    return new DataServerFollowerSizeComparator(slotTableBuilder, SortType.ASC);
  }

  public static DataServerLeaderSizeComparator mostLeadersFirst(SlotTableBuilder slotTableBuilder) {
    return new DataServerLeaderSizeComparator(slotTableBuilder, SortType.DES);
  }

  public static DataServerLeaderSizeComparator leastLeadersFirst(
      SlotTableBuilder slotTableBuilder) {
    return new DataServerLeaderSizeComparator(slotTableBuilder, SortType.ASC);
  }

  public static SlotLeaderRelateDataServerLeaderSizeComparator slotLeaderHasMostLeaderSlots(
      SlotTableBuilder slotTableBuilder) {
    return new SlotLeaderRelateDataServerLeaderSizeComparator(slotTableBuilder, SortType.DES);
  }

  public abstract static class AbstractSlotComparator<T> implements Comparator<T> {

    protected SlotTableBuilder slotTableBuilder;

    protected SortType sortType;

    public AbstractSlotComparator(SlotTableBuilder slotTableBuilder, SortType sortType) {
      this.slotTableBuilder = slotTableBuilder;
      this.sortType = sortType;
    }
  }

  /**
   * ---------------------------------- Data Server Comparators
   * --------------------------------------------
   */
  public abstract static class AbstractDataServerComparator extends AbstractSlotComparator<String> {

    public AbstractDataServerComparator(SlotTableBuilder slotTableBuilder, SortType sortType) {
      super(slotTableBuilder, sortType);
    }

    @Override
    public int compare(String dataServer1, String dataServer2) {
      DataNodeSlot dataNodeSlot1 = slotTableBuilder.getDataNodeSlot(dataServer1);
      DataNodeSlot dataNodeSlot2 = slotTableBuilder.getDataNodeSlot(dataServer2);
      int score =
          getFirstClassMember(dataNodeSlot1).size() - getFirstClassMember(dataNodeSlot2).size();
      if (score == 0) {
        score = getEconomyMember(dataNodeSlot1).size() - getEconomyMember(dataNodeSlot2).size();
      }
      return sortType.getScore(score);
    }

    protected Collection<Integer> getFirstClassMember(DataNodeSlot dataNodeSlot) {
      return Collections.emptyList();
    }

    protected Collection<Integer> getEconomyMember(DataNodeSlot dataNodeSlot) {
      return Collections.emptyList();
    };
  }

  public static class DataServerFollowerSizeComparator extends AbstractDataServerComparator {

    public DataServerFollowerSizeComparator(SlotTableBuilder slotTableBuilder, SortType sortType) {
      super(slotTableBuilder, sortType);
    }

    @Override
    protected Collection<Integer> getFirstClassMember(DataNodeSlot dataNodeSlot) {
      return dataNodeSlot.getFollowers();
    }

    @Override
    protected Collection<Integer> getEconomyMember(DataNodeSlot dataNodeSlot) {
      return dataNodeSlot.getLeaders();
    }
  }

  public static class DataServerLeaderSizeComparator extends AbstractDataServerComparator {

    public DataServerLeaderSizeComparator(SlotTableBuilder slotTableBuilder, SortType sortType) {
      super(slotTableBuilder, sortType);
    }

    @Override
    protected Collection<Integer> getFirstClassMember(DataNodeSlot dataNodeSlot) {
      return dataNodeSlot.getLeaders();
    }

    @Override
    protected Collection<Integer> getEconomyMember(DataNodeSlot dataNodeSlot) {
      return dataNodeSlot.getFollowers();
    }
  }

  /**
   * ---------------------------------- Slot Comparators
   * --------------------------------------------
   */
  public abstract static class AbstractSlotNumberComparator
      extends AbstractSlotComparator<Integer> {

    public AbstractSlotNumberComparator(SlotTableBuilder slotTableBuilder, SortType sortType) {
      super(slotTableBuilder, sortType);
    }

    @Override
    public int compare(Integer slotId1, Integer slotId2) {
      String dataServer1 = slotTableBuilder.getOrCreate(slotId1).getLeader();
      String dataServer2 = slotTableBuilder.getOrCreate(slotId2).getLeader();
      return getDataServerComparator().compare(dataServer1, dataServer2);
    }

    protected abstract AbstractDataServerComparator getDataServerComparator();
  }

  public static class SlotLeaderRelateDataServerLeaderSizeComparator
      extends AbstractSlotNumberComparator {

    private final AbstractDataServerComparator dataServerComparator;

    public SlotLeaderRelateDataServerLeaderSizeComparator(
        SlotTableBuilder slotTableBuilder, SortType sortType) {
      super(slotTableBuilder, sortType);
      dataServerComparator = new DataServerLeaderSizeComparator(slotTableBuilder, sortType);
    }

    @Override
    protected AbstractDataServerComparator getDataServerComparator() {
      return dataServerComparator;
    }
  }
}
