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
package com.alipay.sofa.registry.server.meta.slot.tasks.reassign;

import com.google.common.collect.Maps;
import org.glassfish.jersey.internal.guava.Sets;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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
    private Set<Integer>          leaders   = Sets.newHashSet();
    /** key: slotId, value: counter (one slot could has multi followers to migrate) */
    private Map<Integer, Integer> followers = Maps.newHashMap();

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

    /**
     * Pickup leader slot and removes it from candidate set;
     *
     * @return the int
     */
    public int pickupLeader() {
        int slotId = leaders.iterator().next();
        leaders.remove(slotId);
        return slotId;
    }

    /**
     * Pickup follower slot and removes it from candidate set;
     *
     * @return the int
     */
    public int pickupFollower(Collection<Integer> ignoredSlots) {
        Iterator<Integer> iterator = followers.keySet().iterator();
        int slotId = iterator.next();
        while (ignoredSlots.contains(slotId)) {
            slotId = iterator.next();
        }
        int leftCounts = followers.get(slotId);
        if (leftCounts == 1) {
            followers.remove(slotId);
        } else {
            followers.put(slotId, leftCounts - 1);
        }
        return slotId;
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

    /**
     * Is empty boolean.
     *
     * @return the boolean
     */
    public boolean isEmpty() {
        return leaders.isEmpty() && followers.isEmpty();
    }
}
