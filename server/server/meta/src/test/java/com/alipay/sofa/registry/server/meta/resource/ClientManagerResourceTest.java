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
package com.alipay.sofa.registry.server.meta.resource;

import static org.mockito.Mockito.spy;

import com.alipay.sofa.registry.common.model.ServerDataBox;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.metaserver.ProvideData;
import com.alipay.sofa.registry.server.meta.AbstractMetaServerTestBase;
import com.alipay.sofa.registry.server.meta.provide.data.ClientManagerService;
import com.alipay.sofa.registry.store.api.DBResponse;
import com.alipay.sofa.registry.store.api.OperationStatus;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author xiaojian.xj
 * @version $Id: ClientManagerResourceTest.java, v 0.1 2021年05月29日 17:13 xiaojian.xj Exp $
 */
public class ClientManagerResourceTest extends AbstractMetaServerTestBase {

  private ClientManagerResource clientManagerResource;

  private ClientManagerService clientManagerService = spy(new InMemoryClientManagerServiceRepo());

  private static final String CLIENT_OFF_STR = "1.1.1.1;2.2.2.2";
  private static final String CLIENT_OPEN_STR = "2.2.2.2;3.3.3.3";

  @Before
  public void beforeClientManagerResourceTest() {
    clientManagerResource =
        new ClientManagerResource().setClientManagerService(clientManagerService);
  }

  class InMemoryClientManagerServiceRepo implements ClientManagerService {

    private final AtomicLong version = new AtomicLong(0L);

    private final AtomicReference<ConcurrentHashMap.KeySetView> cache =
        new AtomicReference<>(new ConcurrentHashMap<>().newKeySet());

    @Override
    public boolean clientOpen(Set<String> ipSet) {
      version.incrementAndGet();
      return cache.get().removeAll(ipSet);
    }

    @Override
    public boolean clientOff(Set<String> ipSet) {
      version.incrementAndGet();
      return cache.get().addAll(ipSet);
    }

    @Override
    public DBResponse<ProvideData> queryClientOffSet() {

      ProvideData provideData =
          new ProvideData(
              new ServerDataBox(cache.get()),
              ValueConstants.CLIENT_OFF_PODS_DATA_ID,
              version.get());
      return DBResponse.ok(provideData).build();
    }

    @Override
    public void becomeLeader() {}

    @Override
    public void loseLeader() {}
  }

  @Test
  public void testClientManager() {
    clientManagerResource.clientOff(CLIENT_OFF_STR);

    clientManagerResource.clientOpen(CLIENT_OPEN_STR);

    Map<String, Object> query = clientManagerResource.query();
    Set<String> ips = (Set<String>) query.get("ips");

    Assert.assertEquals(query.get("status"), OperationStatus.SUCCESS);
    Assert.assertEquals(query.get("version"), 2L);
    Assert.assertEquals(ips.size(), 1);
  }
}
