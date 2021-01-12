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

import com.alipay.sofa.registry.common.model.slot.Slot;
import com.alipay.sofa.registry.exception.SofaRegistryRuntimeException;
import com.alipay.sofa.registry.util.DatumVersionUtil;
import org.apache.commons.lang.StringUtils;
import org.glassfish.jersey.internal.guava.Sets;

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

    public SlotBuilder addLeader(String leader) {
        if (this.leader != null) {
            return this;
        }
        epoch = DatumVersionUtil.nextId();
        this.leader = leader;
        return this;
    }

    public SlotBuilder addFollower(String follower) {
        if (followers.size() < followerNums) {
            epoch = DatumVersionUtil.nextId();
            followers.add(follower);
        }
        return this;
    }

    public SlotBuilder removeFollower(String follower) {
        followers.remove(follower);
        return this;
    }

    public SlotBuilder removeLeader(String leader) {
        if (leader.equalsIgnoreCase(this.leader)) {
            this.leader = null;
        }
        return this;
    }

    public SlotBuilder epoch(long epoch) {
        this.epoch = epoch;
        return this;
    }

    private boolean isReady() {
        return leader != null && followers.size() == followerNums;
    }

    public String getLeader() {
        return leader;
    }

    public Set<String> getFollowers() {
        return followers;
    }

    @Override
    public Slot build() {
        if (!isReady()) {
            throw new SofaRegistryRuntimeException("slot builder is not ready for build: leader["
                                                   + leader + "], followers["
                                                   + StringUtils.join(followers, ",") + "]");
        }
        return new Slot(slotId, leader, epoch, followers);
    }
}
