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

import com.alipay.sofa.registry.common.model.ProcessId;

import java.io.Serializable;

/**
 * request to unPublish data
 *
 * @author qian.lqlq
 * @version $Id: UnPublishDataRequest.java, v 0.1 2017-12-01 15:48 qian.lqlq Exp $
 */
public class UnPublishDataRequest implements Serializable {

    private static final long serialVersionUID = 4344115202203415801L;

    private final String      dataInfoId;

    private final String      registerId;

    private final long        registerTimestamp;

    private final ProcessId   processId;

    /**
     * construtor
     * @param dataInfoId
     * @param registerId
     * @param registerTimestamp
     */
    public UnPublishDataRequest(String dataInfoId, String registerId, long registerTimestamp,
                                ProcessId processId) {
        this.dataInfoId = dataInfoId;
        this.registerId = registerId;
        this.registerTimestamp = registerTimestamp;
        this.processId = processId;
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
     * Getter method for property <tt>registerId</tt>.
     *
     * @return property value of registerId
     */
    public String getRegisterId() {
        return registerId;
    }

    /**
     * Getter method for property <tt>registerTimestamp</tt>.
     *
     * @return property value of registerTimestamp
     */
    public long getRegisterTimestamp() {
        return registerTimestamp;
    }

    /**
     * Getter method for property <tt>processId</tt>.
     * @return property value of processId
     */
    public ProcessId getProcessId() {
        return processId;
    }

    @Override
    public String toString() {
        return new StringBuilder("[UnPublishDataRequest] dataInfoId=").append(this.dataInfoId)
            .append(", registerId=").append(this.registerId).append(", registerTimestamp=")
            .append(this.registerTimestamp).toString();
    }
}
