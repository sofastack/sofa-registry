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
package com.alipay.sofa.registry.server.data.cache;

import com.alipay.sofa.registry.common.model.metaserver.DataNode;
import com.alipay.sofa.registry.consistency.hash.ConsistentHash;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author qian.lqlq
 * @version $Id: BackupTriad.java, v 0.1 2018-04-28 23:41 qian.lqlq Exp $
 */
public class BackupTriad {
    /** dataInfoId */
    private String         dataInfoId;

    /**
     * calculate current dataServer list Consistent hash to get dataInfoId belong node and backup node list
     * @see  ConsistentHash#ConsistentHash(int, java.util.Collection)
     * @see  com.alipay.sofa.registry.consistency.hash.ConsistentHash#getNUniqueNodesFor(java.lang.Object, int)
     */
    private List<DataNode> triad;

    private Set<String>    ipSetOfNode = new HashSet<>();

    /**
     * constructor
     * @param dataInfoId
     * @param triad
     */
    public BackupTriad(String dataInfoId, List<DataNode> triad) {
        this.dataInfoId = dataInfoId;
        this.triad = triad;
        for (DataNode node : triad) {
            ipSetOfNode.add(node.getIp());
        }
    }

    /**
     * check contains current node
     * @return
     */
    public boolean containsSelf() {
        return !ipSetOfNode.isEmpty() && ipSetOfNode.contains(DataServerConfig.IP);
    }

    /**
     * get new joined nodes
     *
     * @param newTriad
     * @return
     */
    public List<DataNode> getNewJoined(List<DataNode> newTriad, Set<String> notWorking) {
        List<DataNode> list = new ArrayList<>();
        for (DataNode node : newTriad) {
            String ip = node.getIp();
            if (!ipSetOfNode.contains(ip) || notWorking.contains(ip)) {
                list.add(node);
            }
        }
        return list;
    }

    /**
     * Getter method for property <tt>dataInfoId</tt>.
     *
     * @return property value of dataInfoId
     */
    public String getDataInfoId() {
        return dataInfoId;
    }

    /**
     * Setter method for property <tt>dataInfoId</tt>.
     *
     * @param dataInfoId  value to be assigned to property dataInfoId
     */
    public void setDataInfoId(String dataInfoId) {
        this.dataInfoId = dataInfoId;
    }

    /**
     * Getter method for property <tt>triad</tt>.
     *
     * @return property value of triad
     */
    public List<DataNode> getTriad() {
        return triad;
    }

    /**
     * Setter method for property <tt>triad</tt>.
     *
     * @param triad  value to be assigned to property triad
     */
    public void setTriad(List<DataNode> triad) {
        this.triad = triad;
        Set<String> ipSetOfNode = new HashSet<>();
        for (DataNode node : triad) {
            ipSetOfNode.add(node.getIp());
        }
        this.ipSetOfNode = ipSetOfNode;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BackupTriad{");
        sb.append("dataInfoId='").append(dataInfoId).append('\'');
        sb.append(", ipSetOfNode=").append(ipSetOfNode);
        sb.append('}');
        return sb.toString();
    }
}