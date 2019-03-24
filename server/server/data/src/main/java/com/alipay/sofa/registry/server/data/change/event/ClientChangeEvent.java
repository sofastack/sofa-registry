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

/**
 *
 * @author qian.lqlq
 * @version $Id: ClientChangeEvent.java, v 0.1 2018-05-10 17:12 qian.lqlq Exp $
 */
public class ClientChangeEvent implements IDataChangeEvent {

    private String host;

    private String dataCenter;

    private long   occurredTimestamp;

    private long   version;

    /**
     * constructor
     * @param host
     * @param dataCenter
     * @param occurredTimestamp
     */
    public ClientChangeEvent(String host, String dataCenter, long occurredTimestamp) {
        this.host = host;
        this.dataCenter = dataCenter;
        this.occurredTimestamp = occurredTimestamp;
        this.version = System.currentTimeMillis();
    }

    @Override
    public DataChangeScopeEnum getScope() {
        return DataChangeScopeEnum.CLIENT;
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
     * Getter method for property <tt>occurredTimestamp</tt>.
     *
     * @return property value of occurredTimestamp
     */
    public long getOccurredTimestamp() {
        return occurredTimestamp;
    }

    /**
     * Getter method for property <tt>version</tt>.
     *
     * @return property value of version
     */
    public long getVersion() {
        return version;
    }
}