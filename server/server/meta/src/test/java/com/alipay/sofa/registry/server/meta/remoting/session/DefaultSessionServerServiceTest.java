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
package com.alipay.sofa.registry.server.meta.remoting.session;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.metaserver.ProvideDataChangeEvent;
import com.alipay.sofa.registry.common.model.metaserver.cluster.VersionedList;
import com.alipay.sofa.registry.common.model.metaserver.nodes.SessionNode;
import com.alipay.sofa.registry.remoting.Client;
import com.alipay.sofa.registry.remoting.exchange.RequestException;
import com.alipay.sofa.registry.remoting.exchange.message.Request;
import com.alipay.sofa.registry.server.meta.AbstractMetaServerTestBase;
import com.alipay.sofa.registry.server.meta.lease.session.SessionServerManager;
import com.alipay.sofa.registry.server.meta.remoting.SessionNodeExchanger;
import com.alipay.sofa.registry.server.meta.remoting.connection.SessionConnectionManager;
import com.alipay.sofa.registry.server.shared.env.ServerEnv;
import com.alipay.sofa.registry.util.DatumVersionUtil;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeoutException;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class DefaultSessionServerServiceTest extends AbstractMetaServerTestBase {

  private DefaultSessionServerService notifier = new DefaultSessionServerService();

  @Mock private SessionNodeExchanger sessionNodeExchanger;

  @Mock private SessionConnectionManager sessionConnectionManager;

  @Mock private SessionServerManager sessionServerManager;

  @Before
  public void beforeSessionServerProvideDataNotifierTest() {
    MockitoAnnotations.initMocks(this);
    notifier
        .setSessionConnectionHandler(sessionConnectionManager)
        .setSessionNodeExchanger(sessionNodeExchanger)
        .setSessionServerManager(sessionServerManager);
  }

  @Test
  public void testBoltRequest() throws RequestException, InterruptedException {
    String ip1 = randomIp(), ip2 = randomIp();
    Client rpcClient = spy(getRpcClient(scheduled, 10, new TimeoutException("expected")));
    when(sessionNodeExchanger.request(any(Request.class)))
        .then(
            new Answer<Object>() {
              @Override
              public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Request request = invocationOnMock.getArgumentAt(0, Request.class);
                rpcClient.sendCallback(
                    request.getRequestUrl(),
                    request.getRequestBody(),
                    request.getCallBackHandler(),
                    100);
                return null;
              }
            });
    notifier.setSessionNodeExchanger(sessionNodeExchanger);
    when(sessionConnectionManager.getConnections(anyString()))
        .thenReturn(
            Lists.newArrayList(
                new InetSocketAddress(ip1, Math.abs(random.nextInt(65535)) % 65535),
                new InetSocketAddress(ip2, Math.abs(random.nextInt(65535)) % 65535),
                new InetSocketAddress(randomIp(), 1024)));
    when(sessionServerManager.getSessionServerMetaInfo())
        .thenReturn(
            new VersionedList<>(
                DatumVersionUtil.nextId(),
                Lists.newArrayList(
                    new SessionNode(randomURL(ip1), getDc(), ServerEnv.PROCESS_ID),
                    new SessionNode(randomURL(ip2), getDc(), ServerEnv.PROCESS_ID),
                    new SessionNode(randomURL(randomIp()), getDc(), ServerEnv.PROCESS_ID))));
    notifier.notifyProvideDataChange(
        new ProvideDataChangeEvent(ValueConstants.BLACK_LIST_DATA_ID, System.currentTimeMillis()));
    Thread.sleep(100);
    verify(rpcClient, atLeast(1)).sendCallback(any(), any(), any(), anyInt());
  }

  @Test
  public void testBoltResponsePositive() throws InterruptedException, RequestException {
    String ip1 = randomIp(), ip2 = randomIp();
    when(sessionConnectionManager.getConnections(anyString()))
        .thenReturn(
            Lists.newArrayList(
                new InetSocketAddress(ip1, Math.abs(random.nextInt(65535)) % 65535),
                new InetSocketAddress(ip2, Math.abs(random.nextInt(65535)) % 65535),
                new InetSocketAddress(randomIp(), 1024)));
    when(sessionServerManager.getSessionServerMetaInfo())
        .thenReturn(
            new VersionedList<>(
                DatumVersionUtil.nextId(),
                Lists.newArrayList(
                    new SessionNode(randomURL(ip1), getDc(), ServerEnv.PROCESS_ID),
                    new SessionNode(randomURL(ip2), getDc(), ServerEnv.PROCESS_ID),
                    new SessionNode(randomURL(randomIp()), getDc(), ServerEnv.PROCESS_ID))));
    Client client2 = spy(getRpcClient(scheduled, 10, "Response"));
    SessionNodeExchanger otherNodeExchanger = mock(SessionNodeExchanger.class);
    when(otherNodeExchanger.request(any(Request.class)))
        .then(
            new Answer<Object>() {
              @Override
              public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Request request = invocationOnMock.getArgumentAt(0, Request.class);
                logger.warn("[testBoltResponsePositive]");
                client2.sendCallback(
                    request.getRequestUrl(),
                    request.getRequestBody(),
                    request.getCallBackHandler(),
                    10000);
                return null;
              }
            });
    notifier.setSessionNodeExchanger(otherNodeExchanger);
    notifier.notifyProvideDataChange(
        new ProvideDataChangeEvent(ValueConstants.BLACK_LIST_DATA_ID, System.currentTimeMillis()));
    Thread.sleep(200);
  }

  @Test
  public void testBroadcastInvoke() throws Exception {
    String ip1 = randomIp(), ip2 = randomIp();
    when(sessionConnectionManager.getConnections(anyString()))
        .thenReturn(
            Lists.newArrayList(
                new InetSocketAddress(ip1, Math.abs(random.nextInt(65535)) % 65535),
                new InetSocketAddress(ip2, Math.abs(random.nextInt(65535)) % 65535),
                new InetSocketAddress(randomIp(), 1024)));
    when(sessionServerManager.getSessionServerMetaInfo())
        .thenReturn(
            new VersionedList<>(
                DatumVersionUtil.nextId(),
                Lists.newArrayList(
                    new SessionNode(randomURL(ip1), getDc(), ServerEnv.PROCESS_ID),
                    new SessionNode(randomURL(ip2), getDc(), ServerEnv.PROCESS_ID),
                    new SessionNode(randomURL(randomIp()), getDc(), ServerEnv.PROCESS_ID))));

    SessionNodeExchanger otherNodeExchanger = mock(SessionNodeExchanger.class);
    when(otherNodeExchanger.request(any(Request.class))).thenReturn(Object::new);
    notifier.setSessionNodeExchanger(otherNodeExchanger);
    notifier.broadcastInvoke(
        new ProvideDataChangeEvent(ValueConstants.BLACK_LIST_DATA_ID, System.currentTimeMillis()),
        1000);
  }
}
