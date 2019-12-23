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
package com.alipay.sofa.registry.server.data.remoting.metaserver;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alipay.sofa.jraft.entity.PeerId;
import com.alipay.sofa.registry.common.model.metaserver.ProvideData;
import com.alipay.sofa.registry.server.data.cache.DataServerChangeItem;
import com.alipay.sofa.registry.server.data.node.DataServerNode;

/**
 * The interface Meta server service.
 * @author qian.lqlq
 * @version $Id : IMetaServerService.java, v 0.1 2018-03-07 20:41 qian.lqlq Exp $
 */
public interface IMetaServerService {

    /**
     * Gets meta server map.
     *
     * @return meta server map
     */
    Map<String, Set<String>> getMetaServerMap();

    /**
     * Gets data server.
     *
     * @param dataCenter the data center 
     * @param dataInfoId the data info id 
     * @return data server
     */
    DataServerNode getDataServer(String dataCenter, String dataInfoId);

    /**
     * Gets data servers.
     *
     * @param dataCenter the data center 
     * @param dataInfoId the data info id 
     * @return the data servers
     */
    List<DataServerNode> getDataServers(String dataCenter, String dataInfoId);

    /**
     * Gets data servers.
     *
     * @param dataCenter the data center
     * @return the data servers
     */
    Collection<DataServerNode> getDataServers(String dataCenter);

    /**
     * Gets date servers.
     *
     * @return date servers
     */
    DataServerChangeItem getDateServers();

    /**
     * Gets other data centers.
     *
     * @return other data centers
     */
    List<String> getOtherDataCenters();

    /**
     * scheduler update data server expireTime
     */
    void renewNodeTask();

    /**
     * start raft client for get leader send request
     */
    void startRaftClient();

    /**
     * get raft leader
     * @return
     */
    PeerId getLeader();

    /**
     * renew a leader
     * @return
     */
    PeerId refreshLeader();

    /**
     * get provider data
     * @param dataInfoId
     * @return
     */
    ProvideData fetchData(String dataInfoId);
}