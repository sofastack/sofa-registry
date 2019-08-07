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
 *
 * @author qian.lqlq
 * @version $Id: NotifyOnlineRequest.java, v 0.1 2018-04-29 14:34 qian.lqlq Exp $
 */
public class NotifyOnlineRequest implements Serializable {

    private static final long serialVersionUID = 934663828370697909L;

    private String            ip;

    private long              version;

    /**
     * construtor
     * @param ip
     * @param version
     */
    public NotifyOnlineRequest(String ip, long version) {
        this.ip = ip;
        this.version = version;
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
     * Getter method for property <tt>version</tt>.
     *
     * @return property value of version
     */
    public long getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return new StringBuilder("[NotifyOnlineRequest] ip=").append(ip).append(", version=")
            .append(version).toString();
    }
}