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
package com.alipay.sofa.registry.common.model.slot;

import com.alipay.sofa.registry.common.model.metaserver.nodes.DataNode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 *
 * @author yuzhi.lyz
 * @version v 0.1 2020-11-03 11:27 yuzhi.lyz Exp $
 */
public final class DataNodeSlot implements Serializable {
    private static final long   serialVersionUID = -4418378966762753298L;
    private final String        dataNode;
    private final List<Integer> leaders          = new ArrayList<>();
    private final List<Integer> followers        = new ArrayList<>();

    /**
     * Constructor.
     *
     * @param dataNode the data node
     */
    public DataNodeSlot(String dataNode) {
        this.dataNode = dataNode;
    }

    /**
     * Fork data node slot.
     *
     * @param ignoreFollowers the ignore followers
     * @return the data node slot
     */
    public DataNodeSlot fork(boolean ignoreFollowers) {
        DataNodeSlot clone = new DataNodeSlot(dataNode);
        clone.getLeaders().addAll(leaders);
        if (!ignoreFollowers) {
            clone.getFollowers().addAll(followers);
        }
        return clone;
    }

    /**
     * Getter method for property <tt>dataNode</tt>.
     * @return property value of dataNode
     */
    public String getDataNode() {
        return dataNode;
    }

    /**
     * Getter method for property <tt>leaders</tt>.
     * @return property value of leaders
     */
    public List<Integer> getLeaders() {
        return leaders;
    }

    /**
     * Getter method for property <tt>followers</tt>.
     * @return property value of followers
     */
    public List<Integer> getFollowers() {
        return followers;
    }

    /**
     * Total slot num int.
     *
     * @return the int
     */
    public int totalSlotNum() {
        return leaders.size() + followers.size();
    }

}
