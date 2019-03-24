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
package com.alipay.sofa.registry.remoting.bolt;

import com.alipay.remoting.AsyncContext;
import com.alipay.remoting.BizContext;
import com.alipay.remoting.Connection;
import com.alipay.sofa.registry.remoting.Channel;

import javax.ws.rs.client.WebTarget;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author shangyu.wh
 * @version $Id: BoltChannel.java, v 0.1 2017-11-24 16:46 shangyu.wh Exp $
 */
public class BoltChannel implements Channel {

    private Connection                connection;

    private AsyncContext              asyncContext;

    private BizContext                bizContext;

    private final Map<String, Object> attributes = new ConcurrentHashMap<>();

    @Override
    public InetSocketAddress getRemoteAddress() {
        if (connection != null) {
            return connection.getRemoteAddress();
        }
        return null;
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        if (connection != null) {
            return connection.getLocalAddress();
        }
        return null;
    }

    @Override
    public boolean isConnected() {
        if (connection != null) {
            return connection.isFine();
        }
        return false;
    }

    @Override
    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    @Override
    public void setAttribute(String key, Object value) {
        if (value == null) { // The null value unallowed in the ConcurrentHashMap.
            attributes.remove(key);
        } else {
            attributes.put(key, value);
        }
    }

    @Override
    public WebTarget getWebTarget() {
        return null;
    }

    /**
     * Getter method for property <tt>connection</tt>.
     *
     * @return property value of connection
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Setter method for property <tt>connection</tt>.
     *
     * @param connection  value to be assigned to property connection
     */
    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    /**
     * Getter method for property <tt>asyncContext</tt>.
     *
     * @return property value of asyncContext
     */
    public AsyncContext getAsyncContext() {
        return asyncContext;
    }

    /**
     * Setter method for property <tt>asyncContext</tt>.
     *
     * @param asyncContext  value to be assigned to property asyncContext
     */
    public void setAsyncContext(AsyncContext asyncContext) {
        this.asyncContext = asyncContext;
    }

    /**
     * Getter method for property <tt>bizContext</tt>.
     *
     * @return property value of bizContext
     */
    public BizContext getBizContext() {
        return bizContext;
    }

    /**
     * Setter method for property <tt>bizContext</tt>.
     *
     * @param bizContext  value to be assigned to property bizContext
     */
    public void setBizContext(BizContext bizContext) {
        this.bizContext = bizContext;
    }
}