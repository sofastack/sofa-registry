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
package com.alipay.sofa.registry.server.meta.node;

import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.metaserver.DataOperator;

import java.io.Serializable;

/**
 *
 * @author shangyu.wh
 * @version $Id: NodeOperator.java, v 0.1 2018-03-28 14:27 shangyu.wh Exp $
 */
public class NodeOperator<T extends Node> implements Serializable {

    private T            node;
    private DataOperator nodeOperate;

    public NodeOperator(T node, DataOperator nodeOperate) {
        this.node = node;
        this.nodeOperate = nodeOperate;
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
     * Getter method for property <tt>nodeOperate</tt>.
     *
     * @return property value of nodeOperate
     */
    public DataOperator getNodeOperate() {
        return nodeOperate;
    }

    /**
     * Setter method for property <tt>nodeOperate</tt>.
     *
     * @param nodeOperate  value to be assigned to property nodeOperate
     */
    public void setNodeOperate(DataOperator nodeOperate) {
        this.nodeOperate = nodeOperate;
    }

    @Override
    public String toString() {
        return "NodeOperator{" + "node=" + node.getNodeUrl().getAddressString() + ", nodeOperate="
               + nodeOperate + '}';
    }

}