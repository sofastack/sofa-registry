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
package com.alipay.sofa.registry.server.session.push;

import com.alipay.sofa.registry.server.session.TestUtils;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;

public class PushSwitchServiceTest {

  private long init = -1L;

  @Test
  public void testGlobalOpen() throws Exception {
    PushSwitchService service = TestUtils.newPushSwitchService("testdc");
    service.getFetchStopPushService().setStopPushSwitch(System.currentTimeMillis(), true);
    Assert.assertFalse(service.canLocalDataCenterPush());

    service.getFetchGrayPushSwitchService().setOpenIps(init, Arrays.asList("127.0.0.1"));
    Assert.assertTrue(service.canLocalDataCenterPush());
    Assert.assertFalse(service.canIpPushLocal("127.0.0.2"));
    Assert.assertTrue(service.canIpPushLocal("127.0.0.1"));

    service.getFetchStopPushService().setStopPushSwitch(System.currentTimeMillis(), false);
    Assert.assertTrue(service.canIpPushLocal("127.0.0.2"));
    Assert.assertTrue(service.canIpPushLocal("127.0.0.1"));
  }
}
