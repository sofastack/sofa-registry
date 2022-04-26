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
package com.alipay.sofa.registry.server.session.connections;

import com.alipay.sofa.registry.common.model.ConnectId;
import com.alipay.sofa.registry.remoting.Server;
import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.server.session.TestUtils;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfigBean;
import com.alipay.sofa.registry.server.session.mapper.ConnectionMapper;
import com.alipay.sofa.registry.server.session.store.DataStore;
import com.alipay.sofa.registry.server.session.store.SessionInterests;
import com.alipay.sofa.registry.server.session.store.SessionWatchers;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class ConnectionsServiceTest {
  @Test
  public void test() {
    SessionServerConfigBean configBean = TestUtils.newSessionConfig("testDc");
    ConnectionsService connectionsService = new ConnectionsService();
    connectionsService.sessionServerConfig = configBean;
    connectionsService.connectionMapper = new ConnectionMapper();
    connectionsService.sessionDataStore = Mockito.mock(DataStore.class);

    connectionsService.sessionInterests = new SessionInterests();
    connectionsService.sessionWatchers = new SessionWatchers();

    connectionsService.boltExchange = Mockito.mock(Exchange.class);
    String remoteIp = "192.168.8.8";
    Assert.assertEquals(connectionsService.getIpConnects(Sets.newHashSet(remoteIp)).size(), 0);
    Assert.assertEquals(connectionsService.closeIpConnects(Lists.newArrayList(remoteIp)).size(), 0);

    Server server = Mockito.mock(Server.class);
    Mockito.when(connectionsService.boltExchange.getServer(Mockito.anyInt())).thenReturn(server);

    TestUtils.MockBlotChannel channel = TestUtils.newChannel(9600, remoteIp, 1234);
    Mockito.when(server.getChannels()).thenReturn(Lists.newArrayList(channel));
    ConnectId connectId = ConnectId.of(channel.getRemoteAddress(), channel.getLocalAddress());
    Mockito.when(connectionsService.sessionDataStore.getConnectIds())
        .thenReturn(Sets.newHashSet(connectId));

    List<String> list = connectionsService.getConnections();
    Assert.assertEquals(list, Lists.newArrayList(remoteIp + ":1234"));

    List<ConnectId> connectIds = connectionsService.getIpConnects(Sets.newHashSet("222"));
    Assert.assertEquals(connectIds.size(), 0);

    connectIds = connectionsService.getIpConnects(Sets.newHashSet(remoteIp));
    Assert.assertEquals(connectIds.size(), 1);
    Assert.assertEquals(connectIds.get(0), connectId);
  }
}
