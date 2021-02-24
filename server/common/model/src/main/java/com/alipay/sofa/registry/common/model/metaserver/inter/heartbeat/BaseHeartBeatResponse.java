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
package com.alipay.sofa.registry.common.model.metaserver.inter.heartbeat;

import com.alipay.sofa.registry.common.model.metaserver.nodes.MetaNode;
import com.alipay.sofa.registry.common.model.metaserver.nodes.SessionNode;
import com.alipay.sofa.registry.common.model.slot.SlotTable;
import com.google.common.collect.Sets;

import java.io.Serializable;
import java.util.*;

/**
 * @author chen.zhu
 * <p>
 * Nov 27, 2020
 */
public class BaseHeartBeatResponse implements Serializable {

    private final long              metaServerEpoch;

    private final long              sessionServerEpoch;

    private final SlotTable         slotTable;

    private final List<MetaNode>    metaNodes;

    private final List<SessionNode> sessionNodes;

    public BaseHeartBeatResponse(long metaServerEpoch, SlotTable slotTable, List<MetaNode> metaNodes) {
        this(metaServerEpoch, slotTable, metaNodes, 0, Collections.emptyList());
    }

    public BaseHeartBeatResponse(long metaServerEpoch, SlotTable slotTable,
                                 List<MetaNode> metaNodes, long sessionServerEpoch,
                                 List<SessionNode> sessionNodes) {
        this.metaServerEpoch = metaServerEpoch;
        this.slotTable = slotTable;
        this.metaNodes = Collections.unmodifiableList(metaNodes);
        this.sessionServerEpoch = sessionServerEpoch;
        this.sessionNodes = Collections.unmodifiableList(sessionNodes);
    }

    public SlotTable getSlotTable() {
        return slotTable;
    }

    public List<MetaNode> getMetaNodes() {
        return metaNodes;
    }

    public Map<String, MetaNode> getMetaNodesMap() {
        final Map<String, MetaNode> m = new HashMap<>(metaNodes.size());
        metaNodes.forEach(s -> m.put(s.getIp(), s));
        return m;
    }

    public Set<String> getDataCentersFromMetaNodes() {
        Set<String> dcs = Sets.newHashSet();
        metaNodes.forEach(m -> dcs.add(m.getDataCenter()));
        return dcs;
    }

    public List<SessionNode> getSessionNodes() {
        return sessionNodes;
    }

    public Map<String, SessionNode> getSessionNodesMap() {
        final Map<String, SessionNode> m = new HashMap<>(sessionNodes.size());
        sessionNodes.forEach(s -> m.put(s.getIp(), s));
        return m;
    }

    public long getSessionServerEpoch() {
        return sessionServerEpoch;
    }

    public long getMetaServerEpoch() {
        return metaServerEpoch;
    }
}
