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
import com.alipay.sofa.registry.common.model.metaserver.nodes.SessionNode;
import com.alipay.sofa.registry.exception.SofaRegistryRuntimeException;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.Client;
import com.alipay.sofa.registry.remoting.exchange.NodeExchanger;
import com.alipay.sofa.registry.remoting.exchange.RequestException;
import com.alipay.sofa.registry.remoting.exchange.message.Request;
import com.alipay.sofa.registry.server.meta.AbstractTest;
import com.alipay.sofa.registry.server.meta.lease.session.SessionServerManager;
import com.alipay.sofa.registry.server.meta.remoting.DataNodeExchanger;
import com.alipay.sofa.registry.server.meta.remoting.connection.SessionConnectionHandler;
import com.alipay.sofa.registry.server.shared.remoting.AbstractServerHandler;
import com.google.common.util.concurrent.MoreExecutors;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeoutException;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

public class SessionServerProvideDataNotifierTest extends AbstractTest {

    private SessionServerProvideDataNotifier notifier = new SessionServerProvideDataNotifier();

    @Mock
    private NodeExchanger                    sessionNodeExchanger;

    @Mock
    private SessionConnectionHandler         sessionConnectionHandler;

    @Mock
    private SessionServerManager             sessionServerManager;

    @Before
    public void beforeSessionServerProvideDataNotifierTest() {
        MockitoAnnotations.initMocks(this);
        notifier.setSessionConnectionHandler(sessionConnectionHandler)
            .setSessionNodeExchanger(sessionNodeExchanger)
            .setSessionServerManager(sessionServerManager);
    }

    @Test(expected = SofaRegistryRuntimeException.class)
    public void testExpectedException() {
        notifier.setSessionConnectionHandler(new AbstractServerHandler() {
            @Override
            protected Node.NodeType getConnectNodeType() {
                return Node.NodeType.SESSION;
            }

            @Override
            public Object doHandle(Channel channel, Object request) {
                return null;
            }

            @Override
            public Class interest() {
                return null;
            }
        });
        notifier.getNodeConnectManager();
    }

    @Test
    public void testBoltRequest() throws RequestException, InterruptedException {
        String ip1 = randomIp(), ip2 = randomIp();
        Client rpcClient = spy(getRpcClient(scheduled, 10, new TimeoutException()));
        when(sessionNodeExchanger.request(any(Request.class))).then(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Request request = invocationOnMock.getArgumentAt(0, Request.class);
                rpcClient.sendCallback(request.getRequestUrl(), request.getRequestBody(),
                    request.getCallBackHandler(), 100);
                return null;
            }
        });
        notifier.setSessionNodeExchanger(sessionNodeExchanger);
        when(sessionConnectionHandler.getConnections(anyString())).thenReturn(
            Lists.newArrayList(new InetSocketAddress(ip1, Math.abs(random.nextInt(65535)) % 65535),
                new InetSocketAddress(ip2, Math.abs(random.nextInt(65535)) % 65535),
                new InetSocketAddress(randomIp(), 1024)));
        when(sessionServerManager.getClusterMembers()).thenReturn(
            Lists.newArrayList(new SessionNode(randomURL(ip1), getDc()), new SessionNode(
                randomURL(ip2), getDc()), new SessionNode(randomURL(randomIp()), getDc())));
        notifier.notifyProvideDataChange(new ProvideDataChangeEvent(
            ValueConstants.BLACK_LIST_DATA_ID, System.currentTimeMillis(), DataOperator.ADD));
        Thread.sleep(50);
        verify(rpcClient, atLeast(1)).sendCallback(any(), any(), any(), anyInt());
    }

    @Test
    public void testBoltResponsePositive() throws InterruptedException, RequestException {
        String ip1 = randomIp(), ip2 = randomIp();
        when(sessionConnectionHandler.getConnections(anyString())).thenReturn(
            Lists.newArrayList(new InetSocketAddress(ip1, Math.abs(random.nextInt(65535)) % 65535),
                new InetSocketAddress(ip2, Math.abs(random.nextInt(65535)) % 65535),
                new InetSocketAddress(randomIp(), 1024)));
        when(sessionServerManager.getClusterMembers()).thenReturn(
            Lists.newArrayList(new SessionNode(randomURL(ip1), getDc()), new SessionNode(
                randomURL(ip2), getDc()), new SessionNode(randomURL(randomIp()), getDc())));
        Client client2 = spy(getRpcClient(scheduled, 10, "Response"));
        DataNodeExchanger otherNodeExchanger = mock(DataNodeExchanger.class);
        when(otherNodeExchanger.request(any(Request.class))).then(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Request request = invocationOnMock.getArgumentAt(0, Request.class);
                logger.warn("[testBoltResponsePositive]");
                client2.sendCallback(request.getRequestUrl(), request.getRequestBody(),
                    request.getCallBackHandler(), 10000);
                return null;
            }
        });
        notifier.setSessionNodeExchanger(otherNodeExchanger);
        notifier.notifyProvideDataChange(new ProvideDataChangeEvent(
            ValueConstants.BLACK_LIST_DATA_ID, System.currentTimeMillis(), DataOperator.ADD));
        Thread.sleep(200);
    }

    @Test(expected = SofaRegistryRuntimeException.class)
    public void testBoltRequestThrowException() throws RequestException, InterruptedException {
        String ip1 = randomIp(), ip2 = randomIp();
        when(sessionConnectionHandler.getConnections(anyString())).thenReturn(
            Lists.newArrayList(new InetSocketAddress(ip1, Math.abs(random.nextInt(65535)) % 65535),
                new InetSocketAddress(ip2, Math.abs(random.nextInt(65535)) % 65535),
                new InetSocketAddress(randomIp(), 1024)));
        when(sessionServerManager.getClusterMembers()).thenReturn(
            Lists.newArrayList(new SessionNode(randomURL(ip1), getDc()), new SessionNode(
                randomURL(ip2), getDc()), new SessionNode(randomURL(randomIp()), getDc())));
        Client client = spy(getRpcClient(scheduled, 10, "Response"));
        DataNodeExchanger otherNodeExchanger = mock(DataNodeExchanger.class);
        when(otherNodeExchanger.request(any(Request.class))).thenThrow(
            new RequestException("mocked exception"));
        notifier.setSessionNodeExchanger(otherNodeExchanger);
        notifier.setExecutors(MoreExecutors.directExecutor());
        notifier.notifyProvideDataChange(new ProvideDataChangeEvent(
            ValueConstants.BLACK_LIST_DATA_ID, System.currentTimeMillis(), DataOperator.ADD));
        Thread.sleep(200);
    }
}