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
package com.alipay.sofa.registry.server.shared.meta;

import com.alipay.sofa.jraft.entity.PeerId;
import com.alipay.sofa.registry.common.model.metaserver.ProvideData;
import com.alipay.sofa.registry.common.model.metaserver.SlotTableChangeEvent;

import java.util.List;
import java.util.Set;

/**
 *
 * @author yuzhi.lyz
 * @version v 0.1 2020-11-28 15:19 yuzhi.lyz Exp $
 */
public interface MetaServerService {
    void startRenewer(int intervalMs);

    /**
     * update data server expireTime
     */
    void renewNode();

    boolean handleSlotTableChange(SlotTableChangeEvent event);

    /**
     * get provider data
     * @param dataInfoId
     * @return
     */
    ProvideData fetchData(String dataInfoId);

    /**
     * start raft client for get leader send request
     */
    void startRaftClient();

    PeerId getLeader();

    /**
     * get all datacenters
     * @return
     */
    Set<String> getDataCenters();

    long getSessionServerEpoch();

    /**
     * @param zonename zone is null, get all session
     * @return
     */
    public List<String> getSessionServerList(String zonename);

    public Set<String> getDataServerList();

    public Set<String> getMetaServerList();

    void connectServer();
}
