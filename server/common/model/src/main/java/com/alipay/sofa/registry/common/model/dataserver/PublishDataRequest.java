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
import com.alipay.sofa.registry.common.model.store.Publisher;

import java.io.Serializable;

/**
 * request to register publish data
 *
 * @author qian.lqlq
 * @version $Id: PublishDataRequest.java, v 0.1 2017-12-06 15:56 qian.lqlq Exp $
 */
public class PublishDataRequest implements Serializable {

    private static final long serialVersionUID = 3900211443485220361L;

    private final Publisher   publisher;

    private long              slotTableEpoch;

    private final ProcessId   sessionProcessId;

    public PublishDataRequest(Publisher publisher, ProcessId sessionProcessId) {
        this.publisher = publisher;
        this.sessionProcessId = sessionProcessId;
    }

    /**
     * Getter method for property <tt>publisher</tt>.
     *
     * @return property value of publisher
     */
    public Publisher getPublisher() {
        return publisher;
    }

    /**
     * Getter method for property <tt>slotTableEpoch</tt>.
     * @return property value of slotTableEpoch
     */
    public long getSlotTableEpoch() {
        return slotTableEpoch;
    }

    /**
     * Setter method for property <tt>slotTableEpoch</tt>.
     * @param slotTableEpoch value to be assigned to property slotEpoch
     */
    public void setSlotTableEpoch(long slotTableEpoch) {
        this.slotTableEpoch = slotTableEpoch;
    }

    /**
     * Getter method for property <tt>sessionProcessId</tt>.
     * @return property value of sessionProcessId
     */
    public ProcessId getSessionProcessId() {
        return sessionProcessId;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[PublishDataRequest] processId=")
            .append(sessionProcessId);
        sb.append(", epoch=").append(slotTableEpoch);
        sb.append(", dataInfoId=");
        if (publisher != null) {
            sb.append(publisher.getDataInfoId());
            sb.append(", sourceAddress=").append(publisher.getSourceAddress());
            sb.append(", registerId=").append(publisher.getRegisterId());
        }
        return sb.toString();
    }
}