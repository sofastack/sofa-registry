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
import com.alipay.sofa.registry.common.model.store.URL;

/**
 *
 * @author shangyu.wh
 * @version $Id: MetaNode.java, v 0.1 2018-03-02 16:42 shangyu.wh Exp $
 */
public class MetaNode implements Node {

    private final NodeType nodeType = NodeType.META;

    private final URL      nodeUrl;

    private final String   dataCenter;

    private String         name;

    private String         regionId;

    private NodeStatus     nodeStatus;

    /**
     * constructor
     * @param nodeUrl
     * @param dataCenter
     */
    public MetaNode(URL nodeUrl, String dataCenter) {
        this.nodeUrl = nodeUrl;
        this.name = getIp();
        this.dataCenter = dataCenter;
        this.nodeStatus = NodeStatus.INIT;
    }

    @Override
    public NodeType getNodeType() {
        return nodeType;
    }

    @Override
    public NodeStatus getNodeStatus() {
        return nodeStatus;
    }

    @Override
    public URL getNodeUrl() {
        return nodeUrl;
    }

    /**
     * Getter method for property <tt>dataCenter</tt>.
     *
     * @return property value of dataCenter
     */
    public String getDataCenter() {
        return dataCenter;
    }

    /**
     * get ip address from nodeUrl
     * @return
     */
    public String getIp() {
        return nodeUrl == null ? "" : nodeUrl.getIpAddress();
    }

    /**
     * Setter method for property <tt>nodeStatus</tt>.
     *
     * @param nodeStatus  value to be assigned to property nodeStatus
     */
    @Override
    public void setNodeStatus(NodeStatus nodeStatus) {
        this.nodeStatus = nodeStatus;
    }

    /**
     * Getter method for property <tt>name</tt>.
     *
     * @return property value of name
     */
    public String getName() {
        return name;
    }

    /**
     * Setter method for property <tt>name</tt>.
     *
     * @param name  value to be assigned to property name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter method for property <tt>regionId</tt>.
     *
     * @return property value of regionId
     */
    public String getRegionId() {
        return regionId;
    }

    /**
     * Setter method for property <tt>regionId</tt>.
     *
     * @param regionId  value to be assigned to property regionId
     */
    public void setRegionId(String regionId) {
        this.regionId = regionId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MetaNode{");
        sb.append("nodeUrl=").append(getIp());
        sb.append('}');
        return sb.toString();
    }
}