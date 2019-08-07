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
package com.alipay.sofa.registry.common.model.metaserver;

import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.Node.NodeStatus;

import java.io.Serializable;

/**
 *
 * @author shangyu.wh
 * @version $Id: StatusConfirmRequest.java, v 0.1 2018-03-24 17:13 shangyu.wh Exp $
 */
public class StatusConfirmRequest<T extends Node> implements Serializable {

    private T          node;

    private NodeStatus nodeStatus;

    /**
     * construtor
     * @param node
     * @param nodeStatus
     */
    public StatusConfirmRequest(T node, NodeStatus nodeStatus) {
        this.node = node;
        this.nodeStatus = nodeStatus;
    }

    /**
     * Getter method for property <tt>node</tt>.
     *
     * @return property value of node
     */
    public T getNode() {
        return node;
    }

    /**
     * Setter method for property <tt>node</tt>.
     *
     * @param node  value to be assigned to property node
     */
    public void setNode(T node) {
        this.node = node;
    }

    /**
     * Getter method for property <tt>nodeStatus</tt>.
     *
     * @return property value of nodeStatus
     */
    public NodeStatus getNodeStatus() {
        return nodeStatus;
    }

    /**
     * Setter method for property <tt>nodeStatus</tt>.
     *
     * @param nodeStatus  value to be assigned to property nodeStatus
     */
    public void setNodeStatus(NodeStatus nodeStatus) {
        this.nodeStatus = nodeStatus;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("StatusConfirmRequest{");
        sb.append("node=").append(node);
        sb.append(", nodeStatus=").append(nodeStatus);
        sb.append('}');
        return sb.toString();
    }
}