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
package com.alipay.sofa.registry.server.meta.slot.util;

import com.alipay.sofa.registry.common.model.slot.DataNodeSlot;
import com.alipay.sofa.registry.common.model.slot.Slot;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.util.DatumVersionUtil;
import com.alipay.sofa.registry.util.JsonUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * @author chen.zhu
 * <p>
 * Jan 12, 2021
 */
public class SlotTableBuilder implements Builder<SlotTable> {

    private static final Logger             logger        = LoggerFactory
                                                              .getLogger(SlotTableBuilder.class);

    private final Map<Integer, SlotBuilder> buildingSlots = Maps.newHashMap();

    private final Map<String, DataNodeSlot> reverseMap    = Maps.newHashMap();

    private final int                       totalSlotNums;

    private final int                       slotReplicas;

    private final AtomicLong                epoch         = new AtomicLong(0L);

    public SlotTableBuilder(int totalSlotNums, int slotReplicas) {
        this.totalSlotNums = totalSlotNums;
        this.slotReplicas = slotReplicas;
    }

    public SlotBuilder getOrCreate(int slotId) {
        buildingSlots.putIfAbsent(slotId, new SlotBuilder(slotId, slotReplicas - 1));
        return buildingSlots.get(slotId);
    }

    public void init(SlotTable slotTable, List<String> dataServers) {
        for (int slotId = 0; slotId < totalSlotNums; slotId++) {
            Slot slot = slotTable == null ? null : slotTable.getSlot(slotId);
            if (slot == null) {
                getOrCreate(slotId);
                continue;
            }
            SlotBuilder slotBuilder = new SlotBuilder(slotId, slotReplicas - 1, slot.getLeader(),
                slot.getLeaderEpoch());
            slotBuilder.getFollowers().addAll(slotTable.getSlot(slotId).getFollowers());
            buildingSlots.put(slotId, slotBuilder);
        }
        initReverseMap(dataServers);
    }

    private void initReverseMap(List<String> dataServers) {
        for (int slotId = 0; slotId < totalSlotNums; slotId++) {
            SlotBuilder slotBuilder = getOrCreate(slotId);
            String leader = slotBuilder.getLeader();
            if (leader != null) {
                DataNodeSlot dataNodeSlot = reverseMap.getOrDefault(leader,
                    new DataNodeSlot(leader));
                dataNodeSlot.getLeaders().add(slotId);
                reverseMap.put(leader, dataNodeSlot);
            }
            Set<String> followers = slotBuilder.getFollowers();
            for (String follower : followers) {
                DataNodeSlot dataNodeSlot = reverseMap.getOrDefault(follower, new DataNodeSlot(
                    follower));
                dataNodeSlot.getFollowers().add(slotId);
                reverseMap.put(follower, dataNodeSlot);
            }
        }
        if (dataServers == null || dataServers.isEmpty()) {
            return;
        }
        dataServers.forEach(dataServer->reverseMap.putIfAbsent(dataServer, new DataNodeSlot(dataServer)));
    }

    public SlotTableBuilder flipLeaderTo(int slotId, String nextLeader) {
        SlotBuilder slotBuilder = getOrCreate(slotId);
        String prevLeader = slotBuilder.getLeader();
        slotBuilder.forceSetLeader(nextLeader).removeFollower(nextLeader);
        DataNodeSlot nextLeaderDataNodeSlot = reverseMap.get(nextLeader);
        if(nextLeaderDataNodeSlot == null) {
            nextLeaderDataNodeSlot = new DataNodeSlot(nextLeader);
            reverseMap.put(nextLeader, nextLeaderDataNodeSlot);
        }
        nextLeaderDataNodeSlot.getLeaders().add(slotId);
        nextLeaderDataNodeSlot.getFollowers().remove(new Integer(slotId));
        if (!StringUtils.isEmpty(prevLeader)) {
            reverseMap.get(prevLeader).getLeaders().remove(new Integer(slotId));
            addFollower(slotId, prevLeader);
        }
        return this;
    }

    public SlotTableBuilder removeFollower(int slotId, String follower) {
        SlotBuilder slotBuilder = getOrCreate(slotId);
        if (slotBuilder.getFollowers().remove(follower)) {
            reverseMap.get(follower).getFollowers().remove(new Integer(slotId));
        }
        return this;
    }

    public SlotTableBuilder addFollower(int slotId, String follower) {
        SlotBuilder slotBuilder = getOrCreate(slotId);
        if (slotBuilder.addFollower(follower)) {
            DataNodeSlot dataNodeSlot = reverseMap.getOrDefault(follower,
                new DataNodeSlot(follower));
            dataNodeSlot.getFollowers().add(slotId);
            reverseMap.put(follower, dataNodeSlot);
        }
        return this;
    }

    public boolean hasNoAssignedSlots() {
        if (buildingSlots.size() < totalSlotNums) {
            return true;
        }
        for (SlotBuilder slotBuilder : buildingSlots.values()) {
            if (StringUtils.isEmpty(slotBuilder.getLeader())) {
                return true;
            }
            if (slotBuilder.getFollowers().size() < slotReplicas - 1) {
                return true;
            }
        }
        return false;
    }

    public void removeDataServerSlots(String dataServer) {
        for (SlotBuilder slotBuilder : buildingSlots.values()) {
            if (slotBuilder.getFollowers().remove(dataServer)) {
                logger.info("[removeDataServerSlots] slot [{}] remove follower data-server[{}]",
                    slotBuilder.getSlotId(), dataServer);
            }
            if (dataServer.equals(slotBuilder.getLeader())) {
                logger.info("[removeDataServerSlots] slot [{}] remove leader data-server[{}]",
                    slotBuilder.getSlotId(), dataServer);
                slotBuilder.forceSetLeader(null);
            }
        }
        reverseMap.remove(dataServer);
    }

    public MigrateSlotGroup getNoAssignedSlots() {
        MigrateSlotGroup migrateSlotGroup = new MigrateSlotGroup();
        for (int slotId = 0; slotId < totalSlotNums; slotId++) {
            SlotBuilder slotBuilder = getOrCreate(slotId);
            if (slotBuilder == null) {
                migrateSlotGroup.addLeader(slotId);
                migrateSlotGroup.addFollower(slotId, slotReplicas - 1);

            } else {
                if (StringUtils.isEmpty(slotBuilder.getLeader())) {
                    migrateSlotGroup.addLeader(slotId);
                }
                int lackFollowerNums = slotReplicas - 1 - slotBuilder.getFollowers().size();
                if (lackFollowerNums > 0) {
                    migrateSlotGroup.addFollower(slotId, lackFollowerNums);
                }
            }
        }
        return migrateSlotGroup;
    }

    public List<DataNodeSlot> getDataNodeSlotsLeaderBeyond(int num) {
        return reverseMap.values()
                .stream()
                .filter(dataNodeSlot -> {return dataNodeSlot.getLeaders().size() > num;})
                .collect(Collectors.toList());
    }

    public List<DataNodeSlot> getDataNodeSlotsLeaderBelow(int num) {
        return reverseMap.values()
                .stream()
                .filter(dataNodeSlot -> {return dataNodeSlot.getLeaders().size() <= num;})
                .collect(Collectors.toList());
    }

    public List<DataNodeSlot> getDataNodeTotalSlotsBeyond(int num) {
        return reverseMap.values()
                .stream()
                .filter(dataNodeSlot -> {return dataNodeSlot.getFollowers().size() + dataNodeSlot.getLeaders().size() > num;})
                .collect(Collectors.toList());
    }

    public List<DataNodeSlot> getDataNodeTotalSlotsBelow(int num) {
        return reverseMap.values()
                .stream()
                .filter(dataNodeSlot -> {return dataNodeSlot.getFollowers().size() + dataNodeSlot.getLeaders().size() <= num;})
                .collect(Collectors.toList());
    }

    public void incrEpoch() {
        epoch.set(DatumVersionUtil.nextId());
    }

    @Override
    public SlotTable build() {
        Map<Integer, Slot> stableSlots = Maps.newHashMap();
        buildingSlots.forEach((slotId, slotBuilder)->{
            stableSlots.put(slotId, slotBuilder.build());
            epoch.set(Math.max(epoch.get(), stableSlots.get(slotId).getLeaderEpoch()));
        });
        return new SlotTable(epoch.get(), stableSlots.values());
    }

    public DataNodeSlot getDataNodeSlot(String dataServer) {
        DataNodeSlot dataNodeSlot = reverseMap.get(dataServer);
        if(dataNodeSlot != null) {
            return dataNodeSlot;
        }
        DataNodeSlot newDataNodeSlot = new DataNodeSlot(dataServer);
        buildingSlots.forEach((slotId, slotBuilder)->{
            if(dataServer.equals(slotBuilder.getLeader())) {
                newDataNodeSlot.getLeaders().add(slotId);
            } else if(slotBuilder.getFollowers().contains(dataServer)) {
                newDataNodeSlot.getFollowers().add(slotId);
            }
        });
        return newDataNodeSlot;
    }

    public Map<Integer, SlotBuilder> getBuildingSlots() {
        return buildingSlots;
    }

    @Override
    public String toString() {
        try {
            return JsonUtils.getJacksonObjectMapper().writerWithDefaultPrettyPrinter()
                .writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "";
        }
    }
}
