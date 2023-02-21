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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.alipay.sofa.registry.common.model.console.PersistenceData;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.metaserver.ShutdownSwitch;
import com.alipay.sofa.registry.common.model.metaserver.ShutdownSwitch.CauseEnum;
import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.core.model.Result;
import com.alipay.sofa.registry.server.meta.AbstractMetaServerTestBase;
import com.alipay.sofa.registry.server.meta.provide.data.DefaultProvideDataNotifier;
import com.alipay.sofa.registry.server.meta.provide.data.FetchStopPushService;
import com.alipay.sofa.registry.server.meta.provide.data.ProvideDataService;
import com.alipay.sofa.registry.store.api.DBResponse;
import com.alipay.sofa.registry.util.JsonUtils;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author xiaojian.xj
 * @version : ShutdownSwitchResourceTest.java, v 0.1 2021年10月14日 20:57 xiaojian.xj Exp $
 */
public class ShutdownSwitchResourceTest {

  private ShutdownSwitchResource shutdownSwitchResource;

  private DefaultProvideDataNotifier dataNotifier;

  private ProvideDataService provideDataService =
      spy(new AbstractMetaServerTestBase.InMemoryProvideDataRepo());

  private FetchStopPushService fetchStopPushService = new FetchStopPushService();

  @Before
  public void beforeStopPushDataResourceTest() {
    dataNotifier = mock(DefaultProvideDataNotifier.class);
    fetchStopPushService.setProvideDataService(provideDataService);
    shutdownSwitchResource =
        new ShutdownSwitchResource()
            .setProvideDataNotifier(dataNotifier)
            .setProvideDataService(provideDataService)
            .setFetchStopPushService(fetchStopPushService);
  }

  @Test
  public void testShutdown() {

    String token = "6c62lk8dmQoE5B8X";
    ShutdownSwitch shutdownSwitch = new ShutdownSwitch(true, CauseEnum.FORCE.getCause());

    DataInfo dataInfo = DataInfo.valueOf(ValueConstants.STOP_PUSH_DATA_SWITCH_DATA_ID);
    PersistenceData persistenceData = new PersistenceData();
    persistenceData.setDataId(dataInfo.getDataId());
    persistenceData.setGroup(dataInfo.getGroup());
    persistenceData.setInstanceId(dataInfo.getInstanceId());
    persistenceData.setData("true");
    persistenceData.setVersion(1);
    when(provideDataService.queryProvideData(ValueConstants.STOP_PUSH_DATA_SWITCH_DATA_ID))
        .thenReturn(DBResponse.ok(persistenceData).build());
    Result ret = shutdownSwitchResource.shutdown("true", token);
    Assert.assertTrue(ret.isSuccess());

    Map<String, String> query = shutdownSwitchResource.query();

    Assert.assertEquals(query.get("switch"), JsonUtils.writeValueAsString(shutdownSwitch));

    ret = shutdownSwitchResource.shutdown("false", token);
    Assert.assertTrue(ret.isSuccess());

    shutdownSwitch = new ShutdownSwitch(false);
    query = shutdownSwitchResource.query();

    Assert.assertEquals(query.get("switch"), JsonUtils.writeValueAsString(shutdownSwitch));
  }
}
