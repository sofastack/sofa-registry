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
import com.alipay.sofa.registry.consistency.hash.HashNode;

/**
 *
 * @author qian.lqlq
 * @version $Id: DataServerNode.java, v 0.1 2018-03-13 18:29 qian.lqlq Exp $
 */
public class DataServerNode implements HashNode {

    private String     ip;

    private String     dataCenter;

    private Connection connection;

    /**
     * constructor
     * @param ip
     * @param dataCenter
     * @param connection
     */
    public DataServerNode(String ip, String dataCenter, Connection connection) {
        this.ip = ip;
        this.dataCenter = dataCenter;
        this.connection = connection;
    }

    @Override
    public String getNodeName() {
        return ip;
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
     * Getter method for property <tt>dataCenter</tt>.
     *
     * @return property value of dataCenter
     */
    public String getDataCenter() {
        return dataCenter;
    }

    /**
     * Getter method for property <tt>connection</tt>.
     *
     * @return property value of connection
     */
    public Connection getConnection() {
        return connection;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DataServerNode{");
        sb.append("ip='").append(ip).append('\'');
        sb.append(", dataCenter='").append(dataCenter);
        sb.append('}');
        return sb.toString();
    }
}