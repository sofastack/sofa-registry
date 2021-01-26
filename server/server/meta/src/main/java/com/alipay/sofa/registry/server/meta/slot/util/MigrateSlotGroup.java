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

import com.alipay.sofa.registry.server.meta.slot.assigner.FollowerToAssign;
import com.alipay.sofa.registry.server.meta.slot.assigner.ScoreStrategy;
import com.alipay.sofa.registry.util.JsonUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.glassfish.jersey.internal.guava.Sets;

import java.util.*;

/**
 * @author chen.zhu
 * <p>
 * Jan 12, 2021
 *
 * MigrateSlotGroup is used as a model object to cache all slots we wish to migrate
 * Separated through different roles(Leader/Follower)
 *
 */
public class MigrateSlotGroup {

    /** slotId */
    private final Set<Integer>          leaders   = Sets.newHashSet();
    /** key: slotId, value: counter (one slot could has multi followers to migrate) */
    private final Map<Integer, Integer> followers = Maps.newHashMap();

    /**
     * Add leader migrate slot group.
     *
     * @param slotId the slot id
     * @return the migrate slot group
     */
    public MigrateSlotGroup addLeader(int slotId) {
        leaders.add(slotId);
        return this;
    }

    /**
     * Add follower migrate slot group.
     *
     * @param slotId the slot id
     * @return the migrate slot group
     */
    public MigrateSlotGroup addFollower(int slotId) {
        followers.put(slotId, followers.getOrDefault(slotId, 0) + 1);
        return this;
    }

    public MigrateSlotGroup addFollower(int slotId, int num) {
        followers.put(slotId, followers.getOrDefault(slotId, 0) + num);
        return this;
    }

    public List<Integer> getLeadersByScore(ScoreStrategy<Integer> scoreStrategy) {
        List<Integer> leaders = new ArrayList<>(getLeaders());
        leaders.sort(new Comparator<Integer>() {
            @Override
            public int compare(Integer slotId1, Integer slotId2) {
                return scoreStrategy.score(slotId1) - scoreStrategy.score(slotId2);
            }
        });
        return leaders;
    }

    public List<FollowerToAssign> getFollowersByScore(ScoreStrategy<FollowerToAssign> scoreStrategy) {
        List<FollowerToAssign> assignees = Lists.newArrayList();
        getFollowers().forEach((slotId, nums)->assignees.add(new FollowerToAssign(slotId, nums)));
        assignees.sort(new Comparator<FollowerToAssign>() {
            @Override
            public int compare(FollowerToAssign o1, FollowerToAssign o2) {
                return scoreStrategy.score(o1) - scoreStrategy.score(o2);
            }
        });
        return assignees;
    }

    /**
     * Gets get leaders.
     *
     * @return the get leaders
     */
    public Set<Integer> getLeaders() {
        return leaders;
    }

    /**
     * Gets get followers.
     *
     * @return the get followers
     */
    public Map<Integer, Integer> getFollowers() {
        return followers;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MigrateSlotGroup that = (MigrateSlotGroup) o;
        return Objects.equals(leaders, that.leaders) &&
                Objects.equals(followers, that.followers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(leaders, followers);
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
