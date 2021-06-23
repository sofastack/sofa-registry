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

import static org.mockito.Mockito.mock;

import com.alipay.sofa.registry.common.model.ServerDataBox;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.metaserver.ProvideData;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.connections.ConnectionsService;
import com.alipay.sofa.registry.server.session.registry.Registry;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author xiaojian.xj
 * @version $Id: FetchBlackListTest.java, v 0.1 2021年06月04日 21:03 xiaojian.xj Exp $
 */
public class FetchBlackListTest extends FetchBlackListService {

  @Before
  public void beforeFetchBlackListTest() {

    Registry sessionRegistry = mock(Registry.class);
    SessionServerConfig sessionServerConfig = mock(SessionServerConfig.class);
    ConnectionsService connectionsService = mock(ConnectionsService.class);

    this.setSessionRegistry(sessionRegistry)
        .setSessionServerConfig(sessionServerConfig)
        .setConnectionsService(connectionsService);
  }

  @Test
  public void test() {
    Assert.assertEquals(0, getBlacklistConfigList().size());

    Assert.assertTrue(
        doProcess(
            storage.get(),
            new ProvideData(
                new ServerDataBox(
                    "{\"FORBIDDEN_PUB\":{\"IP_FULL\":[\"1.1.1.1\"]},\"FORBIDDEN_SUB_BY_PREFIX\":{\"IP_FULL\":[\"1.1.1.1\"]}}"),
                ValueConstants.BLACK_LIST_DATA_ID,
                1L)));
  }
}
