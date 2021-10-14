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

import com.alipay.sofa.registry.common.model.metaserver.StopServerSwitch;
import com.alipay.sofa.registry.common.model.metaserver.StopServerSwitch.CauseEnum;
import com.alipay.sofa.registry.core.model.Result;
import com.alipay.sofa.registry.server.meta.AbstractMetaServerTestBase;
import com.alipay.sofa.registry.server.meta.provide.data.DefaultProvideDataNotifier;
import com.alipay.sofa.registry.server.meta.provide.data.ProvideDataService;
import com.alipay.sofa.registry.util.JsonUtils;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author xiaojian.xj
 * @version : StopServerSwitchResourceTest.java, v 0.1 2021年10月14日 20:57 xiaojian.xj Exp $
 */
public class StopServerSwitchResourceTest {

  private StopServerSwitchResource stopPushDataResource;

  private DefaultProvideDataNotifier dataNotifier;

  private ProvideDataService provideDataService =
      spy(new AbstractMetaServerTestBase.InMemoryProvideDataRepo());

  @Before
  public void beforeStopPushDataResourceTest() {
    dataNotifier = mock(DefaultProvideDataNotifier.class);
    stopPushDataResource =
        new StopServerSwitchResource()
            .setProvideDataNotifier(dataNotifier)
            .setProvideDataService(provideDataService);
  }

  @Test
  public void testStopServer() {

    StopServerSwitch stopServerSwitch = new StopServerSwitch(true, CauseEnum.FORCE);
    Result ret = stopPushDataResource.stop("true");
    Assert.assertTrue(ret.isSuccess());

    Map<String, String> query = stopPushDataResource.query();

    Assert.assertEquals(query.get("switch"), JsonUtils.writeValueAsString(stopServerSwitch));

    ret = stopPushDataResource.stop("false");
    Assert.assertTrue(ret.isSuccess());

    stopServerSwitch = new StopServerSwitch(false);
    query = stopPushDataResource.query();

    Assert.assertEquals(query.get("switch"), JsonUtils.writeValueAsString(stopServerSwitch));
  }
}
