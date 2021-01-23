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

import java.net.InetSocketAddress;

import javax.ws.rs.client.WebTarget;

import com.alipay.sofa.registry.common.model.store.URL;
import org.junit.Assert;
import org.junit.Test;

import com.alipay.sofa.registry.remoting.Channel;

/**
 *
 * @author shangyu.wh
 * @version $Id: BoltServerTest.java, v 0.1 2018-05-14 19:34 shangyu.wh Exp $
 */
public class BoltServerTest {

    @Test
    public void testGetChannel() {
        BoltServer sessionServer = new BoltServer(new URL(), null);
        for (int i = 0; i < 30; i++) {

            int finalI = i;
            sessionServer.addChannel(new Channel() {
                @Override
                public InetSocketAddress getRemoteAddress() {
                    return new InetSocketAddress("192.168.1." + finalI, 9000);
                }

                @Override
                public InetSocketAddress getLocalAddress() {
                    return null;
                }

                @Override
                public boolean isConnected() {
                    return finalI % 2 == 0;
                }

                @Override
                public Object getAttribute(String key) {
                    return null;
                }

                @Override
                public void setAttribute(String key, Object value) {

                }

                @Override
                public WebTarget getWebTarget() {
                    return null;
                }

                @Override
                public void close() {

                }
            });
        }
        sessionServer.getChannels();
        Channel channel = sessionServer.getChannel(new InetSocketAddress("192.168.1.1", 9000));
        Assert.assertNull(channel);

        channel = sessionServer.getChannel(new InetSocketAddress("192.168.1.18", 9000));
        Assert.assertNotNull(channel);
    }
}