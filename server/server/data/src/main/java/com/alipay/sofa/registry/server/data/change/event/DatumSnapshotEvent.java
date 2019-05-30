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
    private String                                host;

    private String                                dataCenter;

    private Map<String/*registerId*/, Publisher> pubMap;

    public DatumSnapshotEvent(String host, String dataCenter, Map<String, Publisher> pubMap) {
        this.host = host;
        this.dataCenter = dataCenter;
        this.pubMap = pubMap;
    }

    /**
     * Getter method for property <tt>host</tt>.
     *
     * @return property value of host
     */
    public String getHost() {
        return host;
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
     * Getter method for property <tt>pubMap</tt>.
     *
     * @return property value of pubMap
     */
    public Map<String, Publisher> getPubMap() {
        return pubMap;
    }

    @Override
    public DataChangeScopeEnum getScope() {
        return DataChangeScopeEnum.SNAPSHOT;
    }

}