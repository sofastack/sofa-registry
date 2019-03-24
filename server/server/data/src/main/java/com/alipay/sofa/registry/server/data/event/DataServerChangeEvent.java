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
package com.alipay.sofa.registry.server.data.event;

import com.alipay.sofa.registry.common.model.metaserver.DataNode;
import com.alipay.sofa.registry.server.data.cache.DataServerChangeItem;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author qian.lqlq
 * @version $Id: DataServerChangeEvent.java, v 0.1 2018-03-13 14:37 qian.lqlq Exp $
 */
public class DataServerChangeEvent {

    private DataServerChangeItem dataServerChangeItem;

    /**
     * constructor
     * @param dataServerChangeItem
     */
    public DataServerChangeEvent(DataServerChangeItem dataServerChangeItem) {
        this.dataServerChangeItem = dataServerChangeItem;
    }

    /**
     * constructor
     * @param serverMap
     * @param versionMap
     */
    public DataServerChangeEvent(Map<String, Map<String, DataNode>> serverMap,
                                 Map<String, Long> versionMap) {
        if (serverMap == null) {
            serverMap = new HashMap<>();
        }
        if (versionMap == null) {
            versionMap = new HashMap<>();
        }
        this.dataServerChangeItem = new DataServerChangeItem(serverMap, versionMap);
    }

    /**
     * Getter method for property <tt>dataServerChangeItem</tt>.
     *
     * @return property value of dataServerChangeItem
     */
    public DataServerChangeItem getDataServerChangeItem() {
        return dataServerChangeItem;
    }
}