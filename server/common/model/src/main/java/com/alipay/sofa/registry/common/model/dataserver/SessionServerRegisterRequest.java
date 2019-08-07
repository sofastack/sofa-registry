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
import java.util.Set;

/**
 *
 * @author qian.lqlq
 * @version $Id: SessionServerRegisterRequest.java, v 0.1 2018-04-14 17:31 qian.lqlq Exp $
 */
public class SessionServerRegisterRequest implements Serializable {

    private static final long serialVersionUID = 4872633119038341583L;

    private String            processId;

    private Set<String>       connectIds;

    /**
     * constructor
     * @param processId
     * @param connectIds
     */
    public SessionServerRegisterRequest(String processId, Set<String> connectIds) {
        this.processId = processId;
        this.connectIds = connectIds;
    }

    /**
     * Getter method for property <tt>processId</tt>.
     *
     * @return property value of processId
     */
    public String getProcessId() {
        return processId;
    }

    /**
     * Setter method for property <tt>processId</tt>.
     *
     * @param processId  value to be assigned to property processId
     */
    public void setProcessId(String processId) {
        this.processId = processId;
    }

    /**
     * Getter method for property <tt>connectIds</tt>.
     *
     * @return property value of connectIds
     */
    public Set<String> getConnectIds() {
        return connectIds;
    }

    @Override
    public String toString() {
        return new StringBuilder("[SessionServerRegisterRequest] processId=")
            .append(this.processId).append(", connectIds=").append(this.connectIds).toString();
    }
}