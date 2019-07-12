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
package com.alipay.sofa.registry.server.data.change.event;

import java.util.Map;

import com.alipay.sofa.registry.common.model.store.Publisher;

/**
 *
 * @author kezhu.wukz
 * @version $Id: DatumSnapshotEvent.java, v 0.1 2019-05-30 18:22 kezhu.wukz Exp $
 */
public class DatumSnapshotEvent implements IDataChangeEvent {

    /** connId, format is ip:port */
    private String                                connectId;

    private Map<String/*registerId*/, Publisher> pubMap;

    private Map<String/*registerId*/, Publisher> cachePubMap;

    public DatumSnapshotEvent(String connectId, Map<String, Publisher> cachePubMap,
                              Map<String, Publisher> pubMap) {
        this.connectId = connectId;
        this.cachePubMap = cachePubMap;
        this.pubMap = pubMap;
    }

    /**
     * Getter method for property <tt>connectId</tt>.
     *
     * @return property value of connectId
     */
    public String getConnectId() {
        return connectId;
    }

    /**
     * Getter method for property <tt>pubMap</tt>.
     *
     * @return property value of pubMap
     */
    public Map<String, Publisher> getPubMap() {
        return pubMap;
    }

    /**
     * Getter method for property <tt>cachePubMap</tt>.
     *
     * @return property value of cachePubMap
     */
    public Map<String, Publisher> getCachePubMap() {
        return cachePubMap;
    }

    @Override
    public DataChangeScopeEnum getScope() {
        return DataChangeScopeEnum.SNAPSHOT;
    }

}