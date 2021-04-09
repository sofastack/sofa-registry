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
package com.alipay.sofa.registry.server.meta.remoting.data;

import static org.mockito.Mockito.*;

import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.metaserver.ProvideDataChangeEvent;
import com.alipay.sofa.registry.common.model.metaserver.cluster.VersionedList;
import com.alipay.sofa.registry.common.model.metaserver.nodes.DataNode;
import com.alipay.sofa.registry.remoting.Client;
import com.alipay.sofa.registry.remoting.exchange.RequestException;
import com.alipay.sofa.registry.remoting.exchange.message.Request;
import com.alipay.sofa.registry.server.meta.AbstractMetaServerTestBase;
import com.alipay.sofa.registry.server.meta.lease.data.DataServerManager;
import com.alipay.sofa.registry.server.meta.remoting.DataNodeExchanger;
import com.alipay.sofa.registry.server.meta.remoting.connection.DataConnectionManager;
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

public class DefaultDataServerServiceTest extends AbstractMetaServerTestBase {

  private DefaultDataServerService notifier = new DefaultDataServerService();

  @Mock private DataServerManager dataServerManager;

  @Mock private DataNodeExchanger dataNodeExchanger;

  @Mock private DataConnectionManager dataConnectionManager;

  @Before
  public void beforeDataServerProvideDataNotifierTest() {
    MockitoAnnotations.initMocks(this);
    notifier
        .setDataConnectionManager(dataConnectionManager)
        .setDataNodeExchanger(dataNodeExchanger)
        .setDataServerManager(dataServerManager);
  }

  @Test
  public void testNotify() throws RequestException {
    notifier.notifyProvideDataChange(
        new ProvideDataChangeEvent(ValueConstants.BLACK_LIST_DATA_ID, System.currentTimeMillis()));
    verify(dataServerManager, never()).getDataServerMetaInfo();
    verify(dataNodeExchanger, never()).request(any(Request.class));
  }

  @Test
  public void testNotifyWithNoDataNodes() throws RequestException {
    when(dataConnectionManager.getConnections(anyString()))
        .thenReturn(
            Lists.newArrayList(
                new InetSocketAddress(randomIp(), Math.abs(random.nextInt(65535)) % 65535),
                new InetSocketAddress(randomIp(), Math.abs(random.nextInt(65535)) % 65535)));
    when(dataServerManager.getDataServerMetaInfo())
        .thenReturn(new VersionedList<>(DatumVersionUtil.nextId(), Lists.newArrayList()));
    notifier.notifyProvideDataChange(
        new ProvideDataChangeEvent(ValueConstants.BLACK_LIST_DATA_ID, System.currentTimeMillis()));
    verify(dataServerManager, times(1)).getDataServerMetaInfo();
    verify(dataNodeExchanger, never()).request(any(Request.class));
  }

  @Test
  public void testNotifyNoMatchingDataNodesWithConnect() throws RequestException {
    when(dataConnectionManager.getConnections(anyString()))
        .thenReturn(
            Lists.newArrayList(
                new InetSocketAddress(randomIp(), Math.abs(random.nextInt(65535)) % 65535),
                new InetSocketAddress(randomIp(), Math.abs(random.nextInt(65535)) % 65535)));
    when(dataServerManager.getDataServerMetaInfo())
        .thenReturn(new VersionedList<>(DatumVersionUtil.nextId(), randomDataNodes(3)));
    when(dataNodeExchanger.request(any(Request.class)))
        .thenReturn(
            () -> {
              return null;
            });
    notifier.notifyProvideDataChange(
        new ProvideDataChangeEvent(ValueConstants.BLACK_LIST_DATA_ID, System.currentTimeMillis()));
    verify(dataNodeExchanger, never()).request(any(Request.class));
  }

  @Test
  public void testNotifyNormal() throws RequestException, InterruptedException {
    String ip1 = randomIp(), ip2 = randomIp();
    when(dataConnectionManager.getConnections(anyString()))
        .thenReturn(
            Lists.newArrayList(
                new InetSocketAddress(ip1, Math.abs(random.nextInt(65535)) % 65535),
                new InetSocketAddress(ip2, Math.abs(random.nextInt(65535)) % 65535),
                new InetSocketAddress(randomIp(), 1024)));
    when(dataServerManager.getDataServerMetaInfo())
        .thenReturn(
            new VersionedList<>(
                DatumVersionUtil.nextId(),
                Lists.newArrayList(
                    new DataNode(randomURL(ip1), getDc()),
                    new DataNode(randomURL(ip2), getDc()),
                    new DataNode(randomURL(randomIp()), getDc()))));
    when(dataNodeExchanger.request(any(Request.class)))
        .thenReturn(
            () -> {
              return null;
            });
    notifier.notifyProvideDataChange(
        new ProvideDataChangeEvent(ValueConstants.BLACK_LIST_DATA_ID, System.currentTimeMillis()));
    Thread.sleep(50);
    verify(dataNodeExchanger, times(2)).request(any(Request.class));
  }

  @Test
  public void testExpectedException() {
    notifier.setDataConnectionManager(new DataConnectionManager());
    notifier.getNodeConnectManager();
  }

  @Test
  public void testBoltRequest() throws RequestException, InterruptedException {
    String ip1 = randomIp(), ip2 = randomIp();
    Client rpcClient = spy(getRpcClient(scheduled, 10, new TimeoutException("expected")));
    when(dataNodeExchanger.request(any(Request.class)))
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
    notifier.setDataNodeExchanger(dataNodeExchanger);
    when(dataConnectionManager.getConnections(anyString()))
        .thenReturn(
            Lists.newArrayList(
                new InetSocketAddress(ip1, Math.abs(random.nextInt(65535)) % 65535),
                new InetSocketAddress(ip2, Math.abs(random.nextInt(65535)) % 65535),
                new InetSocketAddress(randomIp(), 1024)));
    when(dataServerManager.getDataServerMetaInfo())
        .thenReturn(
            new VersionedList<>(
                DatumVersionUtil.nextId(),
                Lists.newArrayList(
                    new DataNode(randomURL(ip1), getDc()),
                    new DataNode(randomURL(ip2), getDc()),
                    new DataNode(randomURL(randomIp()), getDc()))));
    notifier.notifyProvideDataChange(
        new ProvideDataChangeEvent(ValueConstants.BLACK_LIST_DATA_ID, System.currentTimeMillis()));
    Thread.sleep(100);
    verify(rpcClient, atLeast(1)).sendCallback(any(), any(), any(), anyInt());
  }

  @Test
  public void testBoltResponsePositive() throws InterruptedException, RequestException {
    String ip1 = randomIp(), ip2 = randomIp();
    when(dataConnectionManager.getConnections(anyString()))
        .thenReturn(
            Lists.newArrayList(
                new InetSocketAddress(ip1, Math.abs(random.nextInt(65535)) % 65535),
                new InetSocketAddress(ip2, Math.abs(random.nextInt(65535)) % 65535),
                new InetSocketAddress(randomIp(), 1024)));
    when(dataServerManager.getDataServerMetaInfo())
        .thenReturn(
            new VersionedList<>(
                DatumVersionUtil.nextId(),
                Lists.newArrayList(
                    new DataNode(randomURL(ip1), getDc()),
                    new DataNode(randomURL(ip2), getDc()),
                    new DataNode(randomURL(randomIp()), getDc()))));
    Client client2 = spy(getRpcClient(scheduled, 10, "Response"));
    DataNodeExchanger otherNodeExchanger = mock(DataNodeExchanger.class);
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
    notifier.setDataNodeExchanger(otherNodeExchanger);
    notifier.notifyProvideDataChange(
        new ProvideDataChangeEvent(ValueConstants.BLACK_LIST_DATA_ID, System.currentTimeMillis()));
    Thread.sleep(200);
  }
}
