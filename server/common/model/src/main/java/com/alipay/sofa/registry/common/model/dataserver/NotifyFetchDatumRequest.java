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
package com.alipay.sofa.registry.common.model.dataserver;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author qian.lqlq
 * @version $Id: NotifyFetchDatumRequest.java, v 0.1 2018-04-29 15:07 qian.lqlq Exp $
 */
public class NotifyFetchDatumRequest implements Serializable {

    private static final long              serialVersionUID = 410885158191567105L;

    private Map<String, Map<String, Long>> dataVersionMap   = new HashMap<>();

    private String                         ip;

    private long                           changeVersion;

    /**
     * construtor
     * @param dataVersionMap
     * @param ip
     * @param changeVersion
     */
    public NotifyFetchDatumRequest(Map<String, Map<String, Long>> dataVersionMap, String ip,
                                   long changeVersion) {
        this.dataVersionMap = dataVersionMap;
        this.ip = ip;
        this.changeVersion = changeVersion;
    }

    /**
     * Getter method for property <tt>dataVersionMap</tt>.
     *
     * @return property value of dataVersionMap
     */
    public Map<String, Map<String, Long>> getDataVersionMap() {
        return dataVersionMap;
    }

    /**
     * Setter method for property <tt>dataVersionMap</tt>.
     *
     * @param dataVersionMap  value to be assigned to property dataVersionMap
     */
    public void setDataVersionMap(Map<String, Map<String, Long>> dataVersionMap) {
        this.dataVersionMap = dataVersionMap;
    }

    /**
     * Getter method for property <tt>ip</tt>.
     *
     * @return property value of ip
     */
    public String getIp() {
        return ip;
    }

    /**
     * Setter method for property <tt>ip</tt>.
     *
     * @param ip  value to be assigned to property ip
     */
    public void setIp(String ip) {
        this.ip = ip;
    }

    /**
     * Getter method for property <tt>changeVersion</tt>.
     *
     * @return property value of changeVersion
     */
    public long getChangeVersion() {
        return changeVersion;
    }

    /**
     * Setter method for property <tt>changeVersion</tt>.
     *
     * @param changeVersion  value to be assigned to property changeVersion
     */
    public void setChangeVersion(long changeVersion) {
        this.changeVersion = changeVersion;
    }

    @Override
    public String toString() {
        return new StringBuilder("[NotifyFetchDatumRequest] ip=").append(ip)
            .append(", changeVersion=").append(changeVersion).toString();
    }
}