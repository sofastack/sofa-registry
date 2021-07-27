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

import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.alipay.sofa.registry.common.model.CollectionSdks;
import com.alipay.sofa.registry.common.model.metaserver.ClientManagerAddress.AddressVersion;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.connections.ConnectionsService;
import com.alipay.sofa.registry.server.session.providedata.FetchClientOffAddressService.ClientOffAddressResp;
import com.alipay.sofa.registry.server.session.registry.Registry;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Set;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

/**
 * @author xiaojian.xj
 * @version $Id: FetchStopPushServiceTest.java, v 0.1 2021年06月04日 19:09 xiaojian.xj Exp $
 */
public class FetchClientOffServiceTest {

  Registry sessionRegistry;

  SessionServerConfig sessionServerConfig;

  ConnectionsService connectionsService;

  FetchClientOffAddressService fetchClientOffAddressService;

  @Before
  public void beforeFetchStopPushServiceTest() {

    MockitoAnnotations.initMocks(this);
    sessionRegistry = mock(Registry.class);
    sessionServerConfig = mock(SessionServerConfig.class);
    connectionsService = mock(ConnectionsService.class);

    when(sessionServerConfig.getClientManagerIntervalMillis()).thenReturn(1000);
    fetchClientOffAddressService = new FetchClientOffAddressService();
    fetchClientOffAddressService
        .setSessionServerConfig(sessionServerConfig)
        .setSessionRegistry(sessionRegistry)
        .setConnectionsService(connectionsService);
  }

  @Test
  public void test() throws InterruptedException {
    fetchClientOffAddressService.postConstruct();
    Assert.assertEquals(0, fetchClientOffAddressService.getClientOffAddress().size());

    Set<String> openIps = CollectionSdks.toIpSet("1.1.1.1;2.2.2.2;3.3.3.3");

    Map<String, AddressVersion> addressVersionMap = Maps.newHashMapWithExpectedSize(openIps.size());
    for (String openIp : openIps) {
      addressVersionMap.put(openIp, new AddressVersion(System.currentTimeMillis(), openIp));
    }

    Assert.assertTrue(
        fetchClientOffAddressService.doProcess(
            fetchClientOffAddressService.getStorage(),
            new ClientOffAddressResp(1L, addressVersionMap)));
    Assert.assertEquals(openIps.size(), fetchClientOffAddressService.getClientOffAddress().size());

  }
}
