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
package com.alipay.sofa.registry.server.meta.registry;

import java.util.List;

import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.Node.NodeType;
import com.alipay.sofa.registry.common.model.metaserver.DataCenterNodes;
import com.alipay.sofa.registry.common.model.metaserver.NodeChangeResult;

/**
 *
 * @author shangyu.wh
 * @version $Id: Registry.java, v 0.1 2018-01-11 17:18 shangyu.wh Exp $
 */
public interface Registry<T extends Node> {

    /**
     * replace all nodes
     *
     * @param nodes
     * @return
     */
    NodeChangeResult setNodes(List<T> nodes);

    /**
     * register new node
     * one node unique id by connectId "ip:port"
     * @return return dataList
     * @param node
     */
    NodeChangeResult register(T node);

    /**
     * Disconnected node cancel it by connectId
     *
     * @param connectId "ip:port"
     */
    void cancel(String connectId, NodeType nodeType);

    /**
     * check and evict all node expired,
     */
    void evict();

    /**
     * renew node expire time
     * @param node
     */
    void renew(T node, int duration);

    /**
     * get other dataCenter Nodes change scheduled
     * @param nodeType
     */
    void getOtherDataCenterNodeAndUpdate(NodeType nodeType);

    /**
     * get DataCenter Nodes list contains version
     * @param nodeType
     * @return
     */
    DataCenterNodes getDataCenterNodes(NodeType nodeType);

    /**
     * get all dataCenter Node list by NodeType
     * @param nodeType
     * @return
     */
    NodeChangeResult getAllNodes(NodeType nodeType);

    /**
     * push node change result
     */
    void pushNodeListChange(NodeType nodeType);
}