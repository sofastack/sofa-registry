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

import com.alipay.sofa.registry.common.model.slot.Slot;
import com.alipay.sofa.registry.exception.SofaRegistryRuntimeException;
import com.alipay.sofa.registry.util.DatumVersionUtil;
import com.alipay.sofa.registry.util.JsonUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * @author chen.zhu
 * <p>
 * Jan 12, 2021
 */
public class SlotBuilder implements Builder<Slot> {

    private final int         slotId;

    private final int         followerNums;

    private String            leader;

    private final Set<String> followers = Sets.newHashSet();

    private long              epoch;

    public SlotBuilder(int slotId, int followerNums) {
        this.slotId = slotId;
        this.followerNums = followerNums;
    }

    public SlotBuilder(int slotId, int followerNums, String leader, long epoch) {
        this.slotId = slotId;
        this.followerNums = followerNums;
        this.leader = leader;
        this.epoch = epoch;
    }

    public SlotBuilder setLeader(String leader) {
        if (!StringUtils.equals(this.leader, leader)) {
            this.leader = leader;
            epoch = DatumVersionUtil.nextId();
        }
        return this;
    }

    public void addFollower(String follower) {
        if (StringUtils.isBlank(follower)) {
            throw new IllegalArgumentException("add empty follower");
        }
        followers.add(follower);
        if (followers.size() > followerNums) {
            throw new IllegalArgumentException(String.format("to many followers, %d: %s",
                followerNums, followers));
        }
    }

    public void addFollower(Collection<String> followerCollection) {
        this.followers.addAll(followerCollection);
        if (followers.size() > followerNums) {
            throw new IllegalArgumentException(String.format("to many followers, %d: %s",
                followerNums, followers));
        }
    }

    public boolean removeFollower(String follower) {
        return followers.remove(follower);
    }

    public int getFollowerSize() {
        return followers.size();
    }

    public boolean containsFollower(String follower) {
        return followers.contains(follower);
    }

    private boolean isReady() {
        return leader != null;
    }

    public String getLeader() {
        return leader;
    }

    public int getSlotId() {
        return slotId;
    }

    public Set<String> getFollowers() {
        return Collections.unmodifiableSet(Sets.newHashSet(followers));
    }

    public long getEpoch() {
        return epoch;
    }

    @Override
    public Slot build() {
        if (!isReady()) {
            throw new SofaRegistryRuntimeException("slot builder is not ready for build: leader["
                                                   + leader + "], followers["
                                                   + StringUtils.join(followers, ",") + "]");
        }
        if (epoch <= 0) {
            this.epoch = DatumVersionUtil.nextId();
        }
        return new Slot(slotId, leader, epoch, followers);
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
