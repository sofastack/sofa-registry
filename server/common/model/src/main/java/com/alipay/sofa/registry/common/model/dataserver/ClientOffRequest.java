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
import java.util.ArrayList;
import java.util.List;

/**
 * request to remove data of specific clients immediately
 *
 * @author qian.lqlq
 * @version $Id: ClientOffRequest.java, v 0.1 2017-12-01 15:48 qian.lqlq Exp $
 */
public class ClientOffRequest implements Serializable {

    private static final long serialVersionUID = -3547806571058756207L;

    /**
     * hosts of clients
     */
    private List<String>      hosts;

    private long              gmtOccur;

    /**
     * constructor
     */
    public ClientOffRequest() {
    }

    /**
     * constructor
     * @param host
     */
    public ClientOffRequest(String host) {
        this.hosts = new ArrayList<>();
        this.hosts.add(host);
    }

    /**
     * Getter method for property <tt>hosts</tt>.
     *
     * @return property value of hosts
     */
    public List<String> getHosts() {
        return hosts;
    }

    /**
     * Setter method for property <tt>hosts</tt>.
     *
     * @param hosts  value to be assigned to property hosts
     */
    public void setHosts(List<String> hosts) {
        this.hosts = hosts;
    }

    /**
     * Getter method for property <tt>gmtOccur</tt>.
     *
     * @return property value of gmtOccur
     */
    public long getGmtOccur() {
        return gmtOccur;
    }

    /**
     * Setter method for property <tt>gmtOccur</tt>.
     *
     * @param gmtOccur  value to be assigned to property gmtOccur
     */
    public void setGmtOccur(long gmtOccur) {
        this.gmtOccur = gmtOccur;
    }

    @Override
    public String toString() {
        return new StringBuilder("[ClientOffRequest] ips=").append(this.hosts)
            .append(", gmtOccur=").append(gmtOccur).toString();
    }
}
