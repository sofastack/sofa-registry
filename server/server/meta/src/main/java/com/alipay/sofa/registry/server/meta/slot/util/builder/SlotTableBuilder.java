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
package com.alipay.sofa.registry.server.meta.slot.util.builder;

import com.alipay.sofa.registry.common.model.slot.DataNodeSlot;
import com.alipay.sofa.registry.common.model.slot.Slot;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.meta.slot.util.MigrateSlotGroup;
import com.alipay.sofa.registry.util.DatumVersionUtil;
import com.alipay.sofa.registry.util.JsonUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringUtils;

/**
 * @author chen.zhu
 *     <p>Jan 12, 2021
 */
public class SlotTableBuilder implements Builder<SlotTable> {

  private static final Logger logger = LoggerFactory.getLogger(SlotTableBuilder.class);

  private final Map<Integer, SlotBuilder> buildingSlots = Maps.newHashMapWithExpectedSize(256);

  private final Map<String, DataNodeSlot> reverseMap = Maps.newHashMap();

  private final int slotNums;

  private final int followerNums;

  private long epoch;

  private final SlotTable initSlotTable;

  public SlotTableBuilder(SlotTable initSlotTable, int slotNums, int slotReplicas) {
    this.slotNums = slotNums;
    this.followerNums = slotReplicas - 1;
    this.initSlotTable = initSlotTable;
  }

  public SlotBuilder getOrCreate(int slotId) {
    return buildingSlots.computeIfAbsent(slotId, k -> new SlotBuilder(slotId, followerNums));
  }

  public void init(List<String> dataServers) {
    for (int slotId = 0; slotId < slotNums; slotId++) {
      Slot slot = initSlotTable == null ? null : initSlotTable.getSlot(slotId);
      if (slot == null) {
        getOrCreate(slotId);
        continue;
      }
      SlotBuilder slotBuilder =
          new SlotBuilder(slotId, followerNums, slot.getLeader(), slot.getLeaderEpoch());
      if (!slotBuilder.addFollower(initSlotTable.getSlot(slotId).getFollowers())) {
        throw new IllegalArgumentException(String.format("to many followers, %s", slotBuilder));
      }
      buildingSlots.put(slotId, slotBuilder);
    }
    initReverseMap(dataServers);
  }

  private void initReverseMap(List<String> dataServers) {
    for (int slotId = 0; slotId < slotNums; slotId++) {
      SlotBuilder slotBuilder = getOrCreate(slotId);
      String leader = slotBuilder.getLeader();
      if (leader != null) {
        DataNodeSlot dataNodeSlot =
            reverseMap.computeIfAbsent(leader, k -> new DataNodeSlot(leader));
        dataNodeSlot.addLeader(slotId);
      }
      Set<String> followers = slotBuilder.getFollowers();
      for (String follower : followers) {
        DataNodeSlot dataNodeSlot =
            reverseMap.computeIfAbsent(follower, k -> new DataNodeSlot(follower));
        dataNodeSlot.addFollower(slotId);
      }
    }
    dataServers.forEach(
        dataServer -> reverseMap.putIfAbsent(dataServer, new DataNodeSlot(dataServer)));
  }

  public String replaceLeader(int slotId, String nextLeader) {
    SlotBuilder slotBuilder = getOrCreate(slotId);
    String prevLeader = slotBuilder.getLeader();
    slotBuilder.setLeader(nextLeader).removeFollower(nextLeader);
    DataNodeSlot nextLeaderDataNodeSlot =
        reverseMap.computeIfAbsent(nextLeader, k -> new DataNodeSlot(nextLeader));
    nextLeaderDataNodeSlot.addLeader(slotId);
    nextLeaderDataNodeSlot.removeFollower(slotId);
    if (!StringUtils.isEmpty(prevLeader)) {
      reverseMap.get(prevLeader).removeLeader(slotId);
    }
    return prevLeader;
  }

  public SlotTableBuilder removeFollower(int slotId, String follower) {
    SlotBuilder slotBuilder = getOrCreate(slotId);
    if (slotBuilder.removeFollower(follower)) {
      reverseMap.get(follower).removeFollower(slotId);
    }
    return this;
  }

  public SlotTableBuilder addFollower(int slotId, String follower) {
    SlotBuilder slotBuilder = getOrCreate(slotId);
    if (!slotBuilder.addFollower(follower)) {
      throw new IllegalArgumentException(String.format("to many followers, %s", slotBuilder));
    }
    DataNodeSlot dataNodeSlot =
        reverseMap.computeIfAbsent(follower, k -> new DataNodeSlot(follower));
    dataNodeSlot.addFollower(slotId);
    return this;
  }

  public List<String> getDataServersOwnsFollower(int followerSlot) {
    SlotBuilder slotBuilder = getOrCreate(followerSlot);
    return Lists.newArrayList(slotBuilder.getFollowers());
  }

  public String getDataServersOwnsLeader(int leaderSlot) {
    SlotBuilder slotBuilder = getOrCreate(leaderSlot);
    return slotBuilder.getLeader();
  }

  public boolean hasNoAssignedSlots() {
    if (buildingSlots.size() < slotNums) {
      return true;
    }
    for (SlotBuilder slotBuilder : buildingSlots.values()) {
      if (StringUtils.isEmpty(slotBuilder.getLeader())) {
        return true;
      }
      if (slotBuilder.getFollowerSize() < followerNums) {
        return true;
      }
    }
    return false;
  }

  public void removeDataServerSlots(String dataServer) {
    for (SlotBuilder slotBuilder : buildingSlots.values()) {
      if (slotBuilder.removeFollower(dataServer)) {
        logger.info(
            "[removeDataServerSlots] slot [{}] remove follower data-server[{}]",
            slotBuilder.getSlotId(),
            dataServer);
      }
      if (dataServer.equals(slotBuilder.getLeader())) {
        logger.info(
            "[removeDataServerSlots] slot [{}] remove leader data-server[{}]",
            slotBuilder.getSlotId(),
            dataServer);
        slotBuilder.setLeader(null);
      }
    }
    reverseMap.remove(dataServer);
  }

  public MigrateSlotGroup getNoAssignedSlots() {
    MigrateSlotGroup migrateSlotGroup = new MigrateSlotGroup();
    for (int slotId = 0; slotId < slotNums; slotId++) {
      SlotBuilder slotBuilder = getOrCreate(slotId);
      if (StringUtils.isEmpty(slotBuilder.getLeader())) {
        migrateSlotGroup.addLeader(slotId);
      }
      int lackFollowerNums = followerNums - slotBuilder.getFollowers().size();
      if (lackFollowerNums > 0) {
        migrateSlotGroup.addFollower(slotId, lackFollowerNums);
      }
    }
    return migrateSlotGroup;
  }

  public List<DataNodeSlot> getDataNodeSlotsLeaderBeyond(int num) {
    return reverseMap.values().stream()
        .filter(
            dataNodeSlot -> {
              return dataNodeSlot.getLeaders().size() > num;
            })
        .collect(Collectors.toList());
  }

  public List<DataNodeSlot> getDataNodeSlotsLeaderBelow(int num) {
    return reverseMap.values().stream()
        .filter(
            dataNodeSlot -> {
              return dataNodeSlot.getLeaders().size() < num;
            })
        .collect(Collectors.toList());
  }

  public List<DataNodeSlot> getDataNodeSlotsFollowerBeyond(int num) {
    return reverseMap.values().stream()
        .filter(
            dataNodeSlot -> {
              return dataNodeSlot.getFollowers().size() > num;
            })
        .collect(Collectors.toList());
  }

  public List<DataNodeSlot> getDataNodeSlotsFollowerBelow(int num) {
    return reverseMap.values().stream()
        .filter(
            dataNodeSlot -> {
              return dataNodeSlot.getFollowers().size() < num;
            })
        .collect(Collectors.toList());
  }

  public void incrEpoch() {
    this.epoch = DatumVersionUtil.nextId();
  }

  @Override
  public SlotTable build() {
    Map<Integer, Slot> stableSlots = Maps.newHashMap();
    buildingSlots.forEach(
        (slotId, slotBuilder) -> {
          stableSlots.put(slotId, slotBuilder.build());
          epoch = Math.max(epoch, stableSlots.get(slotId).getLeaderEpoch());
        });
    return new SlotTable(epoch, stableSlots.values());
  }

  public DataNodeSlot getDataNodeSlot(String dataServer) {
    DataNodeSlot dataNodeSlot = getDataNodeSlotIfPresent(dataServer);
    if (dataNodeSlot == null) {
      throw new IllegalArgumentException("no DataNodeSlot for " + dataServer);
    }
    return dataNodeSlot;
  }

  public DataNodeSlot getDataNodeSlotIfPresent(String dataServer) {
    return reverseMap.get(dataServer);
  }

  @Override
  public String toString() {
    try {
      return JsonUtils.getJacksonObjectMapper()
          .writerWithDefaultPrettyPrinter()
          .writeValueAsString(this);
    } catch (JsonProcessingException e) {
      return "";
    }
  }

  public SlotTable getInitSlotTable() {
    return initSlotTable;
  }

  public int getSlotNums() {
    return slotNums;
  }

  public int getSlotReplicas() {
    return followerNums + 1;
  }
}
