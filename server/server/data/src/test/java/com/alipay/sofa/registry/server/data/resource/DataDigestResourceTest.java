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
package com.alipay.sofa.registry.server.data.resource;

import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.remoting.Server;
import com.alipay.sofa.registry.remoting.bolt.exchange.BoltExchange;
import com.alipay.sofa.registry.server.data.TestBaseUtils;
import com.alipay.sofa.registry.server.data.cache.DatumStorageDelegate;
import com.alipay.sofa.registry.server.data.remoting.metaserver.MetaServerServiceImpl;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class DataDigestResourceTest {

  private final String datacenter = "testdc";

  private DataDigestResource newResource() {
    DataDigestResource resource = new DataDigestResource();
    resource.dataServerConfig = TestBaseUtils.newDataConfig("testDc");
    DatumStorageDelegate datumStorageDelegate = TestBaseUtils.newLocalDatumDelegate("testDc", true);
    resource.datumStorageDelegate = datumStorageDelegate;
    resource.boltExchange = Mockito.mock(BoltExchange.class);
    resource.metaServerService = Mockito.mock(MetaServerServiceImpl.class);
    return resource;
  }

  @Test
  public void testDatum() {
    DataDigestResource resource = newResource();
    String count = resource.getDatumCount();
    Assert.assertTrue(count, count.contains("is 0"));

    Publisher pub = TestBaseUtils.createTestPublishers(10, 1).get(0);
    resource.datumStorageDelegate.getLocalDatumStorage().putPublisher(datacenter, pub);

    Map<String, Datum> map =
        resource.getDatumByDataInfoId(
            pub.getDataId(), pub.getGroup(), pub.getInstanceId(), "testDc");
    Assert.assertEquals(1, map.size());
    Datum datum = map.get("testDc");
    Assert.assertEquals(1, datum.getPubMap().size());
    Assert.assertEquals(pub, datum.getPubMap().get(pub.getRegisterId()));

    Map<String, Map<String, Publisher>> mapMap =
        resource.getPublishersByConnectId(
            Collections.singletonMap("192.168.1.1:100", "192.168.1.2:200"));
    Assert.assertEquals(0, mapMap.size());

    mapMap =
        resource.getPublishersByConnectId(
            Collections.singletonMap(
                pub.getSourceAddress().buildAddressString(),
                pub.getTargetAddress().buildAddressString()));
    Assert.assertEquals(1, mapMap.size());
    Assert.assertEquals(mapMap.get(pub.connectId().toString()).get(pub.getRegisterId()), pub);

    count = resource.getDatumCount();
    Assert.assertTrue(count, count.contains("is 1"));

    resource.datumStorageDelegate = null;
    count = resource.getDatumCount();
    Assert.assertTrue(count, count.contains("cache digest error"));
  }

  @Test
  public void testSessionList() {
    DataDigestResource resource = newResource();
    // bolt server is null
    Map<String, List<String>> map = resource.getServerListAll("session");
    Assert.assertEquals(1, map.size());
    Assert.assertEquals(0, map.get(resource.dataServerConfig.getLocalDataCenter()).size());

    Server server = Mockito.mock(Server.class);
    Mockito.when(resource.boltExchange.getServer(resource.dataServerConfig.getPort()))
        .thenReturn(server);
    TestBaseUtils.MockBlotChannel channel =
        TestBaseUtils.newChannel(resource.dataServerConfig.getPort(), "192.168.1.1", 12345);
    Mockito.when(server.selectAvailableChannelsForHostAddress())
        .thenReturn(Collections.singletonMap("192.168.1.1", channel));
    map = resource.getServerListAll("session");
    Assert.assertEquals(1, map.size());
    List<String> serverList = map.get(resource.dataServerConfig.getLocalDataCenter());
    Assert.assertEquals(1, serverList.size());
    Assert.assertEquals("192.168.1.1:12345", serverList.get(0));
  }

  @Test
  public void testMetaList() {
    DataDigestResource resource = newResource();
    // not meta leader
    Map<String, List<String>> map = resource.getServerListAll("meta");
    Assert.assertEquals(1, map.size());
    Assert.assertEquals(0, map.get(resource.dataServerConfig.getLocalDataCenter()).size());

    Mockito.when(resource.metaServerService.getMetaServerLeader()).thenReturn("xxx");
    map = resource.getServerListAll("meta");
    Assert.assertEquals(1, map.size());
    List<String> serverList = map.get(resource.dataServerConfig.getLocalDataCenter());
    Assert.assertEquals(1, serverList.size());
    Assert.assertEquals("xxx", serverList.get(0));
  }

  @Test
  public void testServerListUnsupported() {
    DataDigestResource resource = newResource();
    TestBaseUtils.assertException(
        IllegalArgumentException.class, () -> resource.getServerListAll("data"));
    TestBaseUtils.assertException(
        IllegalArgumentException.class, () -> resource.getServerListAll("xx"));
  }

  @Test
  public void testGetDataInfoIdList() {
    DataDigestResource resource = newResource();
    Assert.assertEquals(0, resource.getDataInfoIdList().size());
    Publisher pub = TestBaseUtils.createTestPublishers(10, 1).get(0);
    resource.datumStorageDelegate.getLocalDatumStorage().putPublisher(datacenter, pub);
    Assert.assertEquals(1, resource.getDataInfoIdList().size());
    Assert.assertTrue(resource.getDataInfoIdList().toString().contains(pub.getDataInfoId()));
    resource.datumStorageDelegate = null;
    TestBaseUtils.assertException(RuntimeException.class, () -> resource.getDataInfoIdList());
  }
}
