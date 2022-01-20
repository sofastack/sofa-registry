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

import com.alipay.sofa.registry.common.model.GenericResponse;
import com.alipay.sofa.registry.common.model.metaserver.ClientManagerAddress;
import com.alipay.sofa.registry.server.meta.AbstractMetaServerTestBase;
import com.alipay.sofa.registry.server.shared.client.manager.ClientManagerService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author xiaojian.xj
 * @version $Id: ClientManagerResourceTest.java, v 0.1 2021年05月29日 17:13 xiaojian.xj Exp $
 */
public class ClientManagerResourceTest extends AbstractMetaServerTestBase {

  private ClientManagerResource clientManagerResource;

  private final ClientManagerService clientManagerService =
      spy(new InMemoryClientManagerServiceRepo());

  private static final String CLIENT_OFF_STR = "1.1.1.1;2.2.2.2";
  private static final String CLIENT_OPEN_STR = "2.2.2.2;3.3.3.3";
  private static final String CLIENT_OFF_NEW_STR =
      "[{\"address\":\"1.1.1.1\",\"pub\":true,\"sub\":false},{\"address\":\"2.2.2.2\",\"pub\":true,\"sub\":false}]";

  @Before
  public void beforeClientManagerResourceTest() {
    clientManagerResource =
        new ClientManagerResource().setClientManagerService(clientManagerService);
  }

  @Test
  public void testClientManager() {
    clientManagerResource.clientOff(CLIENT_OFF_STR);

    clientManagerResource.clientOpen(CLIENT_OPEN_STR);

    GenericResponse<ClientManagerAddress> query = clientManagerResource.query();

    Assert.assertTrue(query.isSuccess());
    Assert.assertEquals(query.getData().getVersion(), 2L);
    Assert.assertEquals(query.getData().getClientOffAddress().size(), 1);
  }

  @Test
  public void testClientManagerNew() {

    clientManagerResource.clientOffWithSub(CLIENT_OFF_NEW_STR);

    clientManagerResource.clientOff(CLIENT_OFF_STR);

    clientManagerResource.clientOpen(CLIENT_OPEN_STR);

    GenericResponse<ClientManagerAddress> query = clientManagerResource.query();

    Assert.assertTrue(query.isSuccess());
    Assert.assertEquals(query.getData().getVersion(), 3L);
    Assert.assertEquals(query.getData().getClientOffAddress().size(), 1);
  }
}
