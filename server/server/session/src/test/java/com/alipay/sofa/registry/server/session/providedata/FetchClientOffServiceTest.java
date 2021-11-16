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
package com.alipay.sofa.registry.server.session.providedata;

import static com.alipay.sofa.registry.common.model.constants.ValueConstants.CLIENT_OFF;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.alipay.sofa.registry.common.model.CollectionSdks;
import com.alipay.sofa.registry.common.model.metaserver.ClientManagerAddress.AddressVersion;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.bolt.BoltChannel;
import com.alipay.sofa.registry.remoting.bolt.BoltClient;
import com.alipay.sofa.registry.remoting.bolt.BoltServer;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.connections.ConnectionsService;
import com.alipay.sofa.registry.server.session.providedata.FetchClientOffAddressService.ClientOffAddressResp;
import com.alipay.sofa.registry.server.session.registry.Registry;
import com.google.common.collect.Maps;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author xiaojian.xj
 * @version $Id: FetchStopPushServiceTest.java, v 0.1 2021年06月04日 19:09 xiaojian.xj Exp $
 */
public class FetchClientOffServiceTest {

  private static Registry sessionRegistry;

  private static SessionServerConfig sessionServerConfig;

  private static ConnectionsService connectionsService;

  private static FetchClientOffAddressService fetchClientOffAddressService;

  private static URL url = new URL("0.0.0.0", 12345);
  private static BoltServer server = new BoltServer(url, Collections.emptyList());

  private static BoltClient boltClient = new BoltClient(1);
  private static Channel channel;

  @BeforeClass
  public static void beforeFetchStopPushServiceTest() {

    // MockitoAnnotations.initMocks(this);
    sessionRegistry = mock(Registry.class);
    sessionServerConfig = mock(SessionServerConfig.class);
    connectionsService = mock(ConnectionsService.class);

    when(sessionServerConfig.getClientManagerIntervalMillis()).thenReturn(1000);
    fetchClientOffAddressService = new FetchClientOffAddressService();
    fetchClientOffAddressService
        .setSessionServerConfig(sessionServerConfig)
        .setSessionRegistry(sessionRegistry)
        .setConnectionsService(connectionsService);

    server.configWaterMark(1024 * 32, 1024 * 64);
    server.initServer();
    Assert.assertFalse(server.isOpen());
    Assert.assertTrue(server.isClosed());
    server.startServer();
    Assert.assertTrue(server.isOpen());
    Assert.assertFalse(server.isClosed());

    channel = boltClient.connect(url);
  }

  @AfterClass
  public static void after() {
    server.close();
  }

  @Test
  public void test() throws InterruptedException {
    fetchClientOffAddressService.postConstruct();
    Assert.assertEquals(0, fetchClientOffAddressService.getClientOffAddress().size());

    Set<String> offIps = CollectionSdks.toIpSet("1.1.1.1;2.2.2.2;3.3.3.3");

    Map<String, AddressVersion> addressVersionMap = Maps.newHashMapWithExpectedSize(offIps.size());
    for (String openIp : offIps) {
      addressVersionMap.put(openIp, new AddressVersion(System.currentTimeMillis(), openIp, true));
    }

    Assert.assertTrue(
        fetchClientOffAddressService.doProcess(
            fetchClientOffAddressService.getStorage(),
            new ClientOffAddressResp(1L, addressVersionMap, Collections.EMPTY_SET)));
    Assert.assertEquals(offIps.size(), fetchClientOffAddressService.getClientOffAddress().size());

    Thread.sleep(3000);
    offIps = CollectionSdks.toIpSet("1.1.1.1;2.2.2.2");

    Map<String, AddressVersion> addressVersionMapV1 =
        Maps.newHashMapWithExpectedSize(offIps.size());
    for (String openIp : offIps) {
      addressVersionMapV1.put(openIp, new AddressVersion(System.currentTimeMillis(), openIp, true));
    }
    Assert.assertTrue(
        fetchClientOffAddressService.doProcess(
            fetchClientOffAddressService.getStorage(),
            new ClientOffAddressResp(1L, addressVersionMapV1, Collections.singleton("3.3.3.3"))));
    Assert.assertEquals(offIps.size(), fetchClientOffAddressService.getClientOffAddress().size());
  }

  @Test
  public void testClientOpenFailWatcher() throws InterruptedException {

    BoltChannel boltChannel = (BoltChannel) this.channel;

    boltChannel.setConnAttribute(CLIENT_OFF, Boolean.TRUE);

    when(connectionsService.getAllChannel()).thenReturn(Collections.singletonList(this.channel));
    when(connectionsService.getIpFromConnectId(anyString())).thenReturn(url.getIpAddress());

    fetchClientOffAddressService.processClientOpen();
    Thread.sleep(2000);
    boltChannel.setConnAttribute(CLIENT_OFF, null);
    fetchClientOffAddressService.processClientOpen();

    Mockito.verify(connectionsService, Mockito.times(1)).closeIpConnects(anyList());
  }
}
