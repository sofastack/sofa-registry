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
import com.alipay.sofa.registry.server.meta.store.RenewDecorate;

import java.io.Serializable;
import java.util.Map;

/**
 *
 * @author shangyu.wh
 * @version $Id: NodeRepository.java, v 0.1 2018-07-20 14:49 shangyu.wh Exp $
 */
public class NodeRepository<T extends Node> implements Serializable {

    /**
     * store node list existed dataCenter
     */
    private String                                      dataCenter;

    /**
     * store node ip list,and it expired info
     */
    private Map<String/*ipAddress*/, RenewDecorate<T>> nodeMap;

    /**
     * store current dataCenter's node list version
     */
    private Long                                        version;

    /**
     * constructor
     * @param dataCenter
     * @param nodeMap
     * @param version
     */
    public NodeRepository(String dataCenter, Map<String, RenewDecorate<T>> nodeMap, Long version) {
        this.dataCenter = dataCenter;
        this.nodeMap = nodeMap;
        this.version = version;
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
     * Setter method for property <tt>dataCenter</tt>.
     *
     * @param dataCenter  value to be assigned to property dataCenter
     */
    public void setDataCenter(String dataCenter) {
        this.dataCenter = dataCenter;
    }

    /**
     * Getter method for property <tt>nodeMap</tt>.
     *
     * @return property value of nodeMap
     */
    public Map<String, RenewDecorate<T>> getNodeMap() {
        return nodeMap;
    }

    /**
     * Setter method for property <tt>nodeMap</tt>.
     *
     * @param nodeMap  value to be assigned to property nodeMap
     */
    public void setNodeMap(Map<String, RenewDecorate<T>> nodeMap) {
        this.nodeMap = nodeMap;
    }

    /**
     * Getter method for property <tt>version</tt>.
     *
     * @return property value of version
     */
    public Long getVersion() {
        return version;
    }

    /**
     * Setter method for property <tt>version</tt>.
     *
     * @param version  value to be assigned to property version
     */
    public void setVersion(Long version) {
        this.version = version;
    }
}