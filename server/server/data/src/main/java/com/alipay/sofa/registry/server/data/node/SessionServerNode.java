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
package com.alipay.sofa.registry.server.data.node;

import com.alipay.remoting.Connection;

/**
 *
 * @author yuzhi.lyz
 * @version v 0.1 2020-11-04 16:06 yuzhi.lyz Exp $
 */
public class SessionServerNode {
    private String     ip;

    private String     dataCenter;

    private Connection connection;

    /**
     * Getter method for property <tt>ip</tt>.
     * @return property value of ip
     */
    public String getIp() {
        return ip;
    }

    /**
     * Getter method for property <tt>dataCenter</tt>.
     * @return property value of dataCenter
     */
    public String getDataCenter() {
        return dataCenter;
    }

    /**
     * Getter method for property <tt>connection</tt>.
     * @return property value of connection
     */
    public Connection getConnection() {
        return connection;
    }

    @Override public String toString() {
        return "SessionServerNode{" +
                "ip='" + ip + '\'' +
                ", dataCenter='" + dataCenter + '\'' +
                '}';
    }
}
