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
package com.alipay.sofa.registry.server.meta.store;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.Node.NodeType;
import com.alipay.sofa.registry.common.model.metaserver.DataCenterNodes;
import com.alipay.sofa.registry.common.model.metaserver.NodeChangeResult;

/**
 * interface for node store service
 * @author shangyu.wh
 * @version $Id: StoreService.java, v 0.1 2018-01-11 22:16 shangyu.wh Exp $
 */
public interface StoreService<T extends Node> {

    /**
     * get node type
     * @return
     */
    NodeType getNodeType();

    NodeChangeResult setNodes(List<T> nodes);

    /**
     * add new node,when reNew request not found node will be add again
     * @param node
     * @return
     */
    NodeChangeResult addNode(T node);

    /**
     * node change info push must be confirm received,
     * @param ipAddress received Node ipAddress
     * @param confirmNodeIp will be confirmed node ip
     */
    void confirmNodeStatus(String ipAddress, String confirmNodeIp);

    /**
     * remove current dataCenter dataNode by ipAddress
     * @param ipAddress
     * @return
     */
    boolean removeNode(String ipAddress);

    /**
     * remove current dataCenter dataNodes
     * @param nodes
     */
    void removeNodes(Collection<T> nodes);

    /**
     * heartbeat update node expired time
     * @param node
     * @return
     */
    void reNew(T node, int duration);

    /**
     * get expired node list
     * @return
     */
    Collection<T> getExpired();

    /**
     * get all Nodes from all dataCenter
     * @return
     */
    Map<String/*ipAddress*/, T> getNodes();

    /**
     * get node info request
     * @return
     */
    NodeChangeResult getNodeChangeResult();

    /**
     * schedule check other dataCenter node change and update
     */
    void getOtherDataCenterNodeAndUpdate();

    /**
     * get other meta server node change update version and node info,and push current dataCenter node change info
     *
     * @param dataCenterNodes
     */
    void updateOtherDataCenterNodes(DataCenterNodes<T> dataCenterNodes);

    /**
     * get DataCenter Nodes list contains version
     * @return
     */
    DataCenterNodes getDataCenterNodes();

    /**
     * push node change result
     */
    void pushNodeListChange();

}