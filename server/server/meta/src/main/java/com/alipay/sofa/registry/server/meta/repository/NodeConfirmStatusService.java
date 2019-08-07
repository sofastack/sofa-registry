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
package com.alipay.sofa.registry.server.meta.repository;

import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.metaserver.DataOperator;
import com.alipay.sofa.registry.server.meta.node.NodeOperator;
import com.alipay.sofa.registry.store.api.annotation.ReadOnLeader;

import java.util.Collection;
import java.util.Map;
import java.util.Queue;

/**
 *
 * @author shangyu.wh
 * @version $Id: NodeConfirmStatusService.java, v 0.1 2018-05-15 17:49 shangyu.wh Exp $
 */
public interface NodeConfirmStatusService<T extends Node> {

    void putConfirmNode(T node, DataOperator nodeOperate);

    @ReadOnLeader
    NodeOperator<T> peekConfirmNode();

    NodeOperator<T> pollConfirmNode() throws InterruptedException;

    @ReadOnLeader
    Queue<NodeOperator> getAllConfirmNodes();

    Map<String, T> putExpectNodes(T confirmNode, Map<String, T> addNodes);

    @ReadOnLeader
    Map<String, T> getExpectNodes(T confirmNode);

    Map<String, T> removeExpectNodes(T confirmNode);

    Map<String, T> removeExpectConfirmNodes(T confirmNode, Collection<String> ips);
}