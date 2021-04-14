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
package com.alipay.sofa.registry.server.data.lease;

import com.alipay.sofa.registry.common.model.ProcessId;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.remoting.Server;
import com.alipay.sofa.registry.remoting.bolt.exchange.BoltExchange;
import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.server.data.TestBaseUtils;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.cache.LocalDatumStorage;
import com.alipay.sofa.registry.server.shared.env.ServerEnv;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class SessionLeaseManagerTest {
  @Test(expected = IllegalArgumentException.class)
  public void testValidate() {
    SessionLeaseManager slm = new SessionLeaseManager();
    slm.validateSessionLeaseSec(1);
  }

  @Test
  public void test() throws Exception {
    SessionLeaseManager slm = new SessionLeaseManager();
    DataServerConfig cfg = TestBaseUtils.newDataConfig("testDc");
    slm.setDataServerConfig(cfg);
    Exchange boltExchange = Mockito.mock(BoltExchange.class);
    slm.setExchange(boltExchange);
    Set<ProcessId> processIds = slm.getProcessIdsInConnection();
    Assert.assertTrue(processIds.isEmpty());

    Server server = Mockito.mock(Server.class);
    Mockito.when(boltExchange.getServer(Mockito.anyInt())).thenReturn(server);
    TestBaseUtils.MockBlotChannel channel = TestBaseUtils.newChannel(9620, "localhost", 2222);
    channel.setActive(true);
    channel.conn.setAttribute(ValueConstants.ATTR_RPC_CHANNEL_PROCESS_ID, ServerEnv.PROCESS_ID);
    Mockito.when(server.getChannels()).thenReturn(Lists.newArrayList(channel));

    processIds = slm.getProcessIdsInConnection();
    Assert.assertEquals(processIds.size(), 1);
    Assert.assertTrue(processIds.contains(ServerEnv.PROCESS_ID));

    LocalDatumStorage storage = TestBaseUtils.newLocalStorage("testDc", true);
    slm.setLocalDatumStorage(storage);
    DataServerConfig config = storage.getDataServerConfig();
    config.setSessionLeaseCheckIntervalSecs(1);
    config.setDatumCompactDelaySecs(1);
    config.setSessionLeaseSecs(1);
    slm.setDataServerConfig(config);
    slm.renewSession(ServerEnv.PROCESS_ID);
    Assert.assertTrue(slm.contains(ServerEnv.PROCESS_ID));
    Publisher p = TestBaseUtils.createTestPublisher("dataId");
    storage.put(p);
    Assert.assertEquals(storage.get(p.getDataInfoId()).getPubMap().get(p.getRegisterId()), p);
    // wait to clean, but connection remains
    Thread.sleep(1500);
    slm.clean();
    Assert.assertTrue(slm.contains(ServerEnv.PROCESS_ID));
    Assert.assertEquals(storage.tombstoneNum(), 0);
    Assert.assertEquals(storage.get(p.getDataInfoId()).getPubMap().get(p.getRegisterId()), p);

    // reset the connections
    Mockito.when(server.getChannels()).thenReturn(Collections.emptyList());
    Thread.sleep(1500);
    // wait to clean
    slm.clean();
    Assert.assertFalse(slm.contains(ServerEnv.PROCESS_ID));
    Assert.assertEquals(storage.tombstoneNum(), 0);
    Assert.assertEquals(storage.get(p.getDataInfoId()).publisherSize(), 0);

    // wait to compact
    Thread.sleep(1500);
    slm.clean();
    Assert.assertEquals(storage.tombstoneNum(), 0);
    Assert.assertEquals(storage.get(p.getDataInfoId()).publisherSize(), 0);
  }

  @Test
  public void testLoop() throws Exception {
    SessionLeaseManager slm = new SessionLeaseManager();
    BoltExchange boltExchange = Mockito.mock(BoltExchange.class);
    slm.setExchange(boltExchange);
    DataServerConfig cfg = TestBaseUtils.newDataConfig("testDc");
    slm.setDataServerConfig(cfg);
    Server server = Mockito.mock(Server.class);
    Mockito.when(boltExchange.getServer(Mockito.anyInt())).thenReturn(server);
    Mockito.when(server.getChannels()).thenReturn(Collections.emptyList());

    LocalDatumStorage storage = TestBaseUtils.newLocalStorage("testDc", true);
    slm.setLocalDatumStorage(storage);
    DataServerConfig config = storage.getDataServerConfig();
    config.setSessionLeaseCheckIntervalSecs(1);
    config.setDatumCompactDelaySecs(1);
    config.setSessionLeaseSecs(5);
    slm.setDataServerConfig(config);
    slm.init();
    slm.renewSession(ServerEnv.PROCESS_ID);
    Assert.assertTrue(slm.contains(ServerEnv.PROCESS_ID));
    Publisher p = TestBaseUtils.createTestPublisher("dataId");
    storage.put(p);
    Assert.assertEquals(storage.get(p.getDataInfoId()).getPubMap().get(p.getRegisterId()), p);
    // wait to clean
    config.setSessionLeaseSecs(1);
    Thread.sleep(2000);
    Assert.assertEquals(storage.tombstoneNum(), 0);
    Assert.assertEquals(storage.get(p.getDataInfoId()).publisherSize(), 0);
  }
}
