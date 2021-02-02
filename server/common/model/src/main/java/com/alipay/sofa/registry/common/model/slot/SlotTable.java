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
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Maps;

import java.io.Serializable;
import java.util.*;

/**
 * @author yuzhi.lyz
 * @version v 0.1 2020-10-30 10:08 yuzhi.lyz Exp $
 */
public final class SlotTable implements Serializable {
    public static final SlotTable    INIT = new SlotTable(-1, Collections.emptyList());
    private final long               epoch;
    private final Map<Integer, Slot> slots;

    private SlotTable(long epoch, Map<Integer, Slot> slots) {
        this.epoch = epoch;
        this.slots = Collections.unmodifiableSortedMap(new TreeMap<>(slots));
    }

    public SlotTable(long epoch, final Collection<Slot> slots) {
        this.epoch = epoch;
        SortedMap<Integer, Slot> slotMap = Maps.newTreeMap();
        slots.forEach(slot->{
            Slot exist = slotMap.putIfAbsent(slot.getId(), slot);
            if (exist != null) {
                throw new SofaRegistrySlotTableException("dup slot when construct slot table: "
                        + JsonUtils.writeValueAsString(slots));
            }
        });
        this.slots = Collections.unmodifiableSortedMap(slotMap);
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

    /**
     * Getter method for property <tt>epoch</tt>.
     * @return property value of epoch
     */
    public long getEpoch() {
        return epoch;
    }

    @Override
    public String toString() {
        return "SlotTable{" + "epoch=" + epoch + ", slotsNum=" + slots.size() + ", slotKeys="
               + slots.keySet() + '}';
    }

    /**
     * the slot table is too big for rpc
     */
    public List<DataNodeSlot> transfer(String targetDataNode, boolean ignoreFollowers) {
        Map<String, List<Integer>> leadersMap = new HashMap<>();
        Map<String, List<Integer>> followersMap = new HashMap<>();
        for (Slot slot : slots.values()) {
            if (targetDataNode == null) {
                List<Integer> leaders = leadersMap.computeIfAbsent(slot.getLeader(), k -> new ArrayList<>());
                leaders.add(slot.getId());
                if (!ignoreFollowers) {
                    for (String follower : slot.getFollowers()) {
                        List<Integer> followers = followersMap.computeIfAbsent(follower, k -> new ArrayList<>());
                        followers.add(slot.getId());
                    }
                }
                continue;
            }
            if (!ignoreFollowers && slot.getFollowers().contains(targetDataNode)) {
                List<Integer> followers = followersMap.computeIfAbsent(targetDataNode, k -> new ArrayList<>());
                followers.add(slot.getId());
            }
            if (targetDataNode.equals(slot.getLeader())) {
                List<Integer> leaders = leadersMap.computeIfAbsent(slot.getLeader(), k -> new ArrayList<>());
                leaders.add(slot.getId());
            }
        }
        Map<String, DataNodeSlot> dataNodeSlotMap = new HashMap<>(leadersMap.size());
        for (Map.Entry<String, List<Integer>> e : leadersMap.entrySet()) {
            DataNodeSlot dataNodeSlot = dataNodeSlotMap.computeIfAbsent(e.getKey(), k -> new DataNodeSlot(k));
            Collections.sort(e.getValue());
            dataNodeSlot.addLeader(e.getValue());
        }
        for (Map.Entry<String, List<Integer>> e : followersMap.entrySet()) {
            DataNodeSlot dataNodeSlot = dataNodeSlotMap.computeIfAbsent(e.getKey(), k -> new DataNodeSlot(k));
            Collections.sort(e.getValue());
            dataNodeSlot.addFollower(e.getValue());
        }
        return new ArrayList<>(dataNodeSlotMap.values());
    }

    public Set<String> getDataServers() {
        final Set<String> servers = new HashSet<>(64);
        slots.values().forEach(s -> {
            servers.add(s.getLeader());
            servers.addAll(s.getFollowers());
        });
        return servers;
    }

    public SlotTable filter(String ip) {
        if (slots.isEmpty()) {
            return this;
        }
        final Map<Integer, Slot> slotMap = new HashMap<>(slots.size());
        slots.forEach((k, v) -> {
            if (v.getLeader().equals(ip) || v.getFollowers().contains(ip)) {
                slotMap.put(k, v);
            }
        });
        return new SlotTable(epoch, slotMap);
    }

    public int getLeaderNum(String dataServerIp) {
        return (int) slots.values().stream().filter(s -> dataServerIp.equals(s.getLeader())).count();
    }

    public int getFollowerNum(String dataServerIp) {
        return (int) slots.values().stream().filter(s -> s.getFollowers().contains(dataServerIp)).count();
    }

    public void assertSlotLessThan(SlotTable slotTable) {
        slots.values().forEach(s -> {
            Slot after = slotTable.getSlot(s.getId());
            if (after == null) {
                return;
            }
            if (s.getLeaderEpoch() > after.getLeaderEpoch()) {
                throw new RuntimeException(String.format("not expect Slot.LeaderEpoch, %s, %s", s, after));
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

}
