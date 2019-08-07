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
package com.alipay.sofa.registry.remoting;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * The interface Endpoint.
 * @author shangyu.wh
 * @version $Id : Endpoint.java, v 0.1 2017-11-20 20:03 shangyu.wh Exp $
 */
public interface Endpoint {

    /**
     * get channel handlers.
     *
     * @return channel handlers
     */
    List<ChannelHandler> getChannelHandlers();

    /**
     * get local address.
     *
     * @return local address.
     */
    InetSocketAddress getLocalAddress();

    /**
     * close the channel.
     */
    void close();

    /**
     * Close.
     *
     * @param channel the channel
     */
    void close(final Channel channel);

    /**
     * is closed.
     *
     * @return closed boolean
     */
    boolean isClosed();

    /**
     * oneway send
     * @param channel the channel 
     * @param message the message
     */
    void sendOneway(final Channel channel, final Object message);

    /**
     * Sync send
     *
     * @param channel the channel 
     * @param message the message 
     * @param timeoutMillis the timeout millis 
     * @return object
     */
    Object sendSync(final Channel channel, final Object message, final int timeoutMillis);

    /**
     * send with callback handler
     *
     * @param channel the channel 
     * @param message the message 
     * @param callbackHandler the callback handler 
     * @param timeoutMillis the timeout millis
     */
    void sendCallback(final Channel channel, final Object message, CallbackHandler callbackHandler,
                      final int timeoutMillis);
}