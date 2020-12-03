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

/**
 * request to get specific data
 *
 * @author qian.lqlq
 * @version $Id: GetDataRequest.java, v 0.1 2017-12-01 15:48 qian.lqlq Exp $
 */
public class GetDataRequest implements Serializable {

    private static final long serialVersionUID = 8133437572926931258L;

    private final String      dataInfoId;

    /**
     * if datacenter is null, means all datacenters
     */
    private final String      dataCenter;

    private long              slotEpoch;

    /**
     * constructor
     * @param dataInfoId
     * @param dataCenter
     */
    public GetDataRequest(String dataInfoId, String dataCenter) {
        this.dataInfoId = dataInfoId;
        this.dataCenter = dataCenter;
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
     * Getter method for property <tt>dataCenter</tt>.
     *
     * @return property value of dataCenter
     */
    public String getDataCenter() {
        return dataCenter;
    }

    /**
     * Getter method for property <tt>slotEpoch</tt>.
     * @return property value of slotEpoch
     */
    public long getSlotEpoch() {
        return slotEpoch;
    }

    /**
     * Setter method for property <tt>slotEpoch</tt>.
     * @param slotEpoch value to be assigned to property slotEpoch
     */
    public void setSlotEpoch(long slotEpoch) {
        this.slotEpoch = slotEpoch;
    }

    @Override
    public String toString() {
        return new StringBuilder("[GetDataRequest] dataCenter=").append(this.dataCenter)
            .append(", dataInfoId=").append(this.dataInfoId).append(", slotEpoch=")
            .append(slotEpoch).toString();
    }
}
