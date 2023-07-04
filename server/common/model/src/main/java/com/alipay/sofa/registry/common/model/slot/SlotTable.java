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
package com.alipay.sofa.registry.common.model.slot;

import com.alipay.sofa.registry.exception.SofaRegistrySlotTableException;
import com.alipay.sofa.registry.util.JsonUtils;
import com.alipay.sofa.registry.util.StringFormatter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.io.Serializable;
import java.util.*;
import org.springframework.util.CollectionUtils;

/**
 * @author yuzhi.lyz
 * @version v 0.1 2020-10-30 10:08 yuzhi.lyz Exp $
 */
public final class SlotTable implements Serializable {
  public static final SlotTable INIT = new SlotTable(-1, Collections.emptyList());
  private final long epoch;
  private final Map<Integer, Slot> slots;

  private SlotTable(long epoch, Map<Integer, Slot> slots) {
    this.epoch = epoch;
    if (!CollectionUtils.isEmpty(slots)) {
      this.slots = new TreeMap<>(slots);
    } else {
      this.slots = new TreeMap<>();
    }
  }

  public SlotTable(long epoch, final Collection<Slot> slots) {
    this.epoch = epoch;
    SortedMap<Integer, Slot> slotMap = Maps.newTreeMap();
    if (!CollectionUtils.isEmpty(slots)) {
      slots.forEach(
          slot -> {
            Slot exist = slotMap.putIfAbsent(slot.getId(), slot);
            if (exist != null) {
              throw new SofaRegistrySlotTableException(
                  "dup slot when construct slot table: " + JsonUtils.writeValueAsString(slots));
            }
          });
    }
    this.slots = slotMap;
  }

  public List<Slot> getSlots() {
    return new ArrayList<>(slots.values());
  }

  @JsonIgnore
  public Map<Integer, Slot> getSlotMap() {
    return Maps.newHashMap(slots);
  }

  @JsonIgnore
  public Set<Integer> getSlotIds() {
    return new TreeSet<>(slots.keySet());
  }

  public Slot getSlot(int slotId) {
    return slots.get(slotId);
  }

  public Map<Integer, String> slotLeaders() {
    Map<Integer, String> ret = Maps.newTreeMap();
    for (Map.Entry<Integer, Slot> e : slots.entrySet()) {
      ret.put(e.getKey(), e.getValue().getLeader());
    }
    return ret;
  }

  /**
   * Getter method for property <tt>epoch</tt>.
   *
   * @return property value of epoch
   */
  public long getEpoch() {
    return epoch;
  }

  @Override
  public String toString() {
    return StringFormatter.format(
        "SlotTable{epoch={}, num={}, leaders={}}", epoch, slots.size(), slotLeaders());
  }

  /**
   * the slot table is too big for rpc
   *
   * @param targetDataNode targetDataNode
   * @param ignoreFollowers ignoreFollowers
   * @return List
   */
  public List<DataNodeSlot> transfer(String targetDataNode, boolean ignoreFollowers) {
    Map<String, List<Integer>> leadersMap = new HashMap<>();
    Map<String, List<Integer>> followersMap = new HashMap<>();
    for (Slot slot : slots.values()) {
      if (targetDataNode == null) {
        List<Integer> leaders =
            leadersMap.computeIfAbsent(slot.getLeader(), k -> new ArrayList<>());
        leaders.add(slot.getId());
        if (!ignoreFollowers) {
          for (String follower : slot.getFollowers()) {
            List<Integer> followers =
                followersMap.computeIfAbsent(follower, k -> new ArrayList<>());
            followers.add(slot.getId());
          }
        }
        continue;
      }
      if (!ignoreFollowers && slot.getFollowers().contains(targetDataNode)) {
        List<Integer> followers =
            followersMap.computeIfAbsent(targetDataNode, k -> new ArrayList<>());
        followers.add(slot.getId());
      }
      if (targetDataNode.equals(slot.getLeader())) {
        List<Integer> leaders =
            leadersMap.computeIfAbsent(slot.getLeader(), k -> new ArrayList<>());
        leaders.add(slot.getId());
      }
    }
    Map<String, DataNodeSlot> dataNodeSlotMap = Maps.newHashMapWithExpectedSize(leadersMap.size());
    for (Map.Entry<String, List<Integer>> e : leadersMap.entrySet()) {
      DataNodeSlot dataNodeSlot =
          dataNodeSlotMap.computeIfAbsent(e.getKey(), k -> new DataNodeSlot(k));
      Collections.sort(e.getValue());
      dataNodeSlot.addLeader(e.getValue());
    }
    for (Map.Entry<String, List<Integer>> e : followersMap.entrySet()) {
      DataNodeSlot dataNodeSlot =
          dataNodeSlotMap.computeIfAbsent(e.getKey(), k -> new DataNodeSlot(k));
      Collections.sort(e.getValue());
      dataNodeSlot.addFollower(e.getValue());
    }
    return new ArrayList<>(dataNodeSlotMap.values());
  }

  public Set<String> getDataServers() {
    final Set<String> servers = Sets.newTreeSet();
    slots
        .values()
        .forEach(
            s -> {
              servers.add(s.getLeader());
              servers.addAll(s.getFollowers());
            });
    return servers;
  }

  public SlotTable filter(Set<Integer> slotIds) {
    if (slots.isEmpty()) {
      return this;
    }
    final Map<Integer, Slot> slotMap = Maps.newTreeMap();
    for (Integer slotId : slotIds) {
      slotMap.put(slotId, slots.get(slotId));
    }
    return new SlotTable(epoch, slotMap);
  }

  public SlotTable filter(String ip) {
    if (slots.isEmpty()) {
      return this;
    }
    final Map<Integer, Slot> slotMap = Maps.newTreeMap();
    slots.forEach(
        (k, v) -> {
          if (v.getLeader().equals(ip) || v.getFollowers().contains(ip)) {
            slotMap.put(k, v);
          }
        });
    return new SlotTable(epoch, slotMap);
  }

  public SlotTable filterLeaderInfo() {
    if (slots.isEmpty()) {
      return this;
    }
    final Map<Integer, Slot> slotMap = Maps.newTreeMap();
    slots.forEach(
        (k, v) -> {
          // filter followers
          slotMap.put(
              k, new Slot(v.getId(), v.getLeader(), v.getLeaderEpoch(), Collections.emptyList()));
        });
    return new SlotTable(epoch, slotMap);
  }

  public int getLeaderNum(String dataServerIp) {
    return (int) slots.values().stream().filter(s -> dataServerIp.equals(s.getLeader())).count();
  }

  public int getSlotNum() {
    return slots.size();
  }

  public int getFollowerNum(String dataServerIp) {
    return (int)
        slots.values().stream().filter(s -> s.getFollowers().contains(dataServerIp)).count();
  }

  public void assertSlotLessThan(SlotTable slotTable) {
    slots
        .values()
        .forEach(
            s -> {
              Slot newSlot = slotTable.getSlot(s.getId());
              if (newSlot == null) {
                return;
              }
              if (s.getLeaderEpoch() > newSlot.getLeaderEpoch()) {
                throw new RuntimeException(
                    String.format("not expect Slot.LeaderEpoch, %s, %s", s, newSlot));
              }
            });
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SlotTable slotTable = (SlotTable) o;
    return epoch == slotTable.epoch && slots.equals(slotTable.slots);
  }

  @Override
  public int hashCode() {
    return Objects.hash(epoch, slots);
  }

  public static final class SlotTableBuilder {

    private final long epoch;

    private final Map<Integer, SlotBuilder> slotBuilderMap = Maps.newHashMap();

    private SlotTableBuilder(long epoch) {
      this.epoch = epoch;
    }

    public SlotTableBuilder slotLeader(int slotId, String leader) {
      slotBuilderMap.putIfAbsent(slotId, new SlotBuilder(slotId));
      slotBuilderMap.get(slotId).leader(leader);
      return this;
    }

    public SlotTableBuilder slotFollower(int slotId, String follower) {
      slotBuilderMap.putIfAbsent(slotId, new SlotBuilder(slotId));
      slotBuilderMap.get(slotId).follower(follower);
      return this;
    }

    public SlotTable build() {
      Map<Integer, Slot> slotMap = Maps.newHashMap();
      for (Map.Entry<Integer, SlotBuilder> entry : slotBuilderMap.entrySet()) {
        slotMap.put(entry.getKey(), entry.getValue().build());
      }
      return new SlotTable(epoch, slotMap);
    }
  }

  private static final class SlotBuilder {

    private final int slotId;

    private String leader;

    private long leaderEpoch;

    private final Set<String> followers = Sets.newHashSet();

    public SlotBuilder(int slotId) {
      this.slotId = slotId;
    }

    public SlotBuilder leader(String leader) {
      if (this.leader != null) {
        throw new IllegalStateException(
            String.format("leader is not null(%s), but try to set (%s)", this.leader, leader));
      }
      this.leader = leader;
      return this;
    }

    public SlotBuilder followers(String... follower) {
      followers.addAll(Arrays.asList(follower));
      return this;
    }

    public SlotBuilder follower(String foll) {
      followers.add(foll);
      return this;
    }

    public SlotBuilder leaderEpoch(long leaderEpoch) {
      this.leaderEpoch = leaderEpoch;
      return this;
    }

    public Slot build() {
      return new Slot(slotId, leader, leaderEpoch, followers);
    }
  }
}
