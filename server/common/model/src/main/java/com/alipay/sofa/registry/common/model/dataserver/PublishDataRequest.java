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

    private Publisher         publisher;

    private String            sessionServerProcessId;

    /**
     * Getter method for property <tt>publisher</tt>.
     *
     * @return property value of publisher
     */
    public Publisher getPublisher() {
        return publisher;
    }

    /**
     * Setter method for property <tt>publisher</tt>.
     *
     * @param publisher  value to be assigned to property publisher
     */
    public void setPublisher(Publisher publisher) {
        this.publisher = publisher;
    }

    /**
     * Getter method for property <tt>sessionServerProcessId</tt>.
     *
     * @return property value of sessionServerProcessId
     */
    public String getSessionServerProcessId() {
        return sessionServerProcessId;
    }

    /**
     * Setter method for property <tt>sessionServerProcessId</tt>.
     *
     * @param sessionServerProcessId  value to be assigned to property sessionServerProcessId
     */
    public void setSessionServerProcessId(String sessionServerProcessId) {
        this.sessionServerProcessId = sessionServerProcessId;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[PublishDataRequest] sessionServerProcessId=")
            .append(sessionServerProcessId);
        sb.append(", dataInfoId=");
        if (publisher != null) {
            sb.append(publisher.getDataInfoId());
            sb.append(", sourceAddress=").append(publisher.getSourceAddress());
            sb.append(", registerId=").append(publisher.getRegisterId());
        }
        return sb.toString();
    }
}