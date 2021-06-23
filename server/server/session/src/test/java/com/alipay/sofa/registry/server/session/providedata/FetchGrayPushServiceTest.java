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

import com.alipay.sofa.registry.common.model.CollectionSdks;
import com.alipay.sofa.registry.common.model.ServerDataBox;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.metaserver.ProvideData;
import com.alipay.sofa.registry.common.model.sessionserver.GrayOpenPushSwitchRequest;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.util.JsonUtils;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author xiaojian.xj
 * @version $Id: FetchStopPushServiceTest.java, v 0.1 2021年06月04日 19:09 xiaojian.xj Exp $
 */
public class FetchGrayPushServiceTest extends FetchGrayPushSwitchService {

  @Before
  public void beforeFetchStopPushServiceTest() {
    SessionServerConfig sessionServerConfig = mock(SessionServerConfig.class);

    this.setSessionServerConfig(sessionServerConfig);
  }

  @Test
  public void test() {
    Assert.assertFalse(canPush());

    GrayOpenPushSwitchRequest request = new GrayOpenPushSwitchRequest();
    List<String> openIps = CollectionSdks.toIpList("1.1.1.1;2.2.2.2;3.3.3.3");
    request.setIps(openIps);
    Assert.assertTrue(
        doProcess(
            storage.get(),
            new ProvideData(
                new ServerDataBox(JsonUtils.writeValueAsString(request)),
                ValueConstants.PUSH_SWITCH_GRAY_OPEN_DATA_ID,
                1L)));

    for (String openIp : getOpenIps()) {
      Assert.assertTrue(openIps.contains(openIp));
    }
    Assert.assertTrue(canPush());
  }
}
