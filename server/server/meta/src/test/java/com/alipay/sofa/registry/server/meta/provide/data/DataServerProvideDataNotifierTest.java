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
package com.alipay.sofa.registry.server.meta.provide.data;

import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.metaserver.DataOperator;
import com.alipay.sofa.registry.common.model.metaserver.ProvideDataChangeEvent;
import com.alipay.sofa.registry.common.model.metaserver.nodes.DataNode;
import com.alipay.sofa.registry.remoting.exchange.RequestException;
import com.alipay.sofa.registry.remoting.exchange.message.Request;
import com.alipay.sofa.registry.remoting.exchange.message.Response;
import com.alipay.sofa.registry.server.meta.AbstractTest;
import com.alipay.sofa.registry.server.meta.lease.DataServerManager;
import com.alipay.sofa.registry.server.meta.remoting.DataNodeExchanger;
import com.alipay.sofa.registry.server.meta.remoting.connection.DataConnectionHandler;
import org.assertj.core.util.Lists;
import org.assertj.core.util.Sets;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.InetSocketAddress;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class DataServerProvideDataNotifierTest extends AbstractTest {

    private DataServerProvideDataNotifier notifier = new DataServerProvideDataNotifier();

    @Mock
    private DataServerManager             dataServerManager;

    @Mock
    private DataNodeExchanger             dataNodeExchanger;

    @Mock
    private DataConnectionHandler         dataConnectionHandler;

    @Before
    public void beforeDataServerProvideDataNotifierTest() {
        MockitoAnnotations.initMocks(this);
        notifier.setDataConnectionHandler(dataConnectionHandler)
            .setDataNodeExchanger(dataNodeExchanger).setDataServerManager(dataServerManager);
    }

    @Test
    public void testNotify() throws RequestException {
        notifier.notifyProvideDataChange(new ProvideDataChangeEvent(
            ValueConstants.BLACK_LIST_DATA_ID, System.currentTimeMillis(), DataOperator.ADD));
        verify(dataServerManager, never()).getClusterMembers();
        verify(dataNodeExchanger, never()).request(any(Request.class));
    }

    @Test
    public void testNotifyWithNoDataNodes() throws RequestException {
        when(dataConnectionHandler.getConnections(anyString())).thenReturn(
            Lists.newArrayList(new InetSocketAddress(randomIp(),
                Math.abs(random.nextInt(65535)) % 65535),
                new InetSocketAddress(randomIp(), Math.abs(random.nextInt(65535)) % 65535)));
        notifier.notifyProvideDataChange(new ProvideDataChangeEvent(
            ValueConstants.BLACK_LIST_DATA_ID, System.currentTimeMillis(), DataOperator.ADD));
        verify(dataServerManager, times(1)).getClusterMembers();
        verify(dataNodeExchanger, never()).request(any(Request.class));
    }

    @Test
    public void testNotifyNoMatchingDataNodesWithConnect() throws RequestException {
        when(dataConnectionHandler.getConnections(anyString()))
                .thenReturn(Lists.newArrayList(new InetSocketAddress(randomIp(), Math.abs(random.nextInt(65535)) % 65535),
                        new InetSocketAddress(randomIp(), Math.abs(random.nextInt(65535)) % 65535 ) ));
        when(dataServerManager.getClusterMembers())
                .thenReturn(Lists.newArrayList(new DataNode(randomURL(randomIp()), getDc()),
                        new DataNode(randomURL(randomIp()), getDc()),
                        new DataNode(randomURL(randomIp()), getDc())));
        when(dataNodeExchanger.request(any(Request.class))).thenReturn(()->{return null;});
        notifier.notifyProvideDataChange(new ProvideDataChangeEvent(ValueConstants.BLACK_LIST_DATA_ID,
                System.currentTimeMillis(), DataOperator.ADD));
        verify(dataNodeExchanger, never()).request(any(Request.class));
    }

    @Test
    public void testNotifyNormal() throws RequestException {
        String ip1 = randomIp(), ip2 = randomIp();
        when(dataConnectionHandler.getConnections(anyString()))
                .thenReturn(Lists.newArrayList(new InetSocketAddress(ip1, Math.abs(random.nextInt(65535)) % 65535),
                        new InetSocketAddress(ip2, Math.abs(random.nextInt(65535)) % 65535 ),
                        new InetSocketAddress(randomIp(), 1024)));
        when(dataServerManager.getClusterMembers())
                .thenReturn(Lists.newArrayList(new DataNode(randomURL(ip1), getDc()),
                        new DataNode(randomURL(ip2), getDc()),
                        new DataNode(randomURL(randomIp()), getDc())));
        when(dataNodeExchanger.request(any(Request.class))).thenReturn(()->{return null;});
        notifier.notifyProvideDataChange(new ProvideDataChangeEvent(ValueConstants.BLACK_LIST_DATA_ID,
                System.currentTimeMillis(), DataOperator.ADD));
        verify(dataNodeExchanger, times(2)).request(any(Request.class));
    }
}