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
import com.alipay.sofa.registry.consistency.hash.HashNode;

/**
 *
 * @author shangyu.wh
 * @version $Id: DataNode.java, v 0.1 2018-01-18 18:06 shangyu.wh Exp $
 */
public class DataNode implements Node, HashNode {

    private final URL    nodeUrl;

    private final String nodeName;

    private final String dataCenter;

    private String       regionId;

    private NodeStatus   nodeStatus;

    private long         registrationTimestamp;

    /**
     * constructor
     * @param nodeUrl
     * @param dataCenter
     */
    public DataNode(URL nodeUrl, String dataCenter) {
        this.nodeUrl = nodeUrl;
        this.nodeName = nodeUrl.getIpAddress();
        this.dataCenter = dataCenter;
        this.nodeStatus = NodeStatus.INIT;
    }

    /**
     * constructor
     * @param nodeUrl
     * @param dataCenter
     * @param status
     */
    public DataNode(URL nodeUrl, String dataCenter, NodeStatus status) {
        this(nodeUrl, dataCenter);
        this.nodeStatus = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DataNode)) {
            return false;
        }

        DataNode that = (DataNode) o;

        if (nodeStatus != null ? !nodeStatus.equals(that.nodeStatus) : that.nodeStatus != null) {
            return false;
        }
        if (nodeName != null ? !nodeName.equals(that.nodeName) : that.nodeName != null) {
            return false;
        }

        if (dataCenter != null ? !dataCenter.equals(that.dataCenter) : that.dataCenter != null) {
            return false;
        }

        if (regionId != null ? !regionId.equals(that.regionId) : that.regionId != null) {
            return false;
        }

        if (registrationTimestamp != that.registrationTimestamp) {
            return false;
        }

        return nodeUrl != null ? (nodeUrl.getAddressString() != null ? nodeUrl.getAddressString()
            .equals(that.nodeUrl.getAddressString()) : that.nodeUrl.getAddressString() != null)
            : that.nodeUrl != null;
    }

    /**
     * Hash code int.
     *
     * @return the int
     */
    @Override
    public int hashCode() {
        int result = nodeName != null ? nodeName.hashCode() : 0;
        result = 31 * result + (dataCenter != null ? dataCenter.hashCode() : 0);
        result = 31 * result + (regionId != null ? regionId.hashCode() : 0);
        result = 31 * result + (nodeStatus != null ? nodeStatus.hashCode() : 0);
        result = 31 * result + (int) (registrationTimestamp ^ (registrationTimestamp >>> 32));
        result = 31
                 * result
                 + (nodeUrl != null ? (nodeUrl.getAddressString() != null ? nodeUrl
                     .getAddressString().hashCode() : 0) : 0);
        return result;
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.DATA;
    }

    @Override
    public URL getNodeUrl() {
        return nodeUrl;
    }

    /**
     * get ip address for nodeUrl
     * @return
     */
    public String getIp() {
        return nodeUrl == null ? "" : nodeUrl.getIpAddress();
    }

    /**
     * Getter method for property <tt>nodeName</tt>.
     *
     * @return property value of nodeName
     */
    @Override
    public String getNodeName() {
        return nodeName;
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

    /**
     * Getter method for property <tt>dataCenter</tt>.
     *
     * @return property value of dataCenter
     */
    public String getDataCenter() {
        return dataCenter;
    }

    /**
     * Getter method for property <tt>nodeStatus</tt>.
     *
     * @return property value of nodeStatus
     */
    @Override
    public NodeStatus getNodeStatus() {
        return nodeStatus;
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
     * Getter method for property <tt>registrationTimestamp</tt>.
     *
     * @return property value of registrationTimestamp
     */
    public long getRegistrationTimestamp() {
        return registrationTimestamp;
    }

    /**
     * Setter method for property <tt>registrationTimestamp</tt>.
     *
     * @param registrationTimestamp  value to be assigned to property registrationTimestamp
     */
    public void setRegistrationTimestamp(long registrationTimestamp) {
        this.registrationTimestamp = registrationTimestamp;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DataNode{");
        sb.append("ip=").append(getIp());
        sb.append('}');
        return sb.toString();
    }

}