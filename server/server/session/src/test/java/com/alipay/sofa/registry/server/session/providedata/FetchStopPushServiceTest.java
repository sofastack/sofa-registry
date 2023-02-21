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

import com.alipay.sofa.registry.server.session.TestUtils;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author xiaojian.xj
 * @version $Id: FetchStopPushServiceTest.java, v 0.1 2021年06月04日 19:09 xiaojian.xj Exp $
 */
public class FetchStopPushServiceTest extends FetchStopPushService {

  @Before
  public void beforeFetchStopPushServiceTest() {
    SessionServerConfig sessionServerConfig = TestUtils.newSessionConfig("testdc");

    this.setSessionServerConfig(sessionServerConfig);
  }

  @Test
  public void test() {
    Assert.assertTrue(isStopPushSwitch());

    Assert.assertTrue(doProcess(storage.get(), new StopPushStorage(2L, false)));
    Assert.assertEquals(isStopPushSwitch(), false);

    Assert.assertTrue(doProcess(storage.get(), new StopPushStorage(1L, true)));
    Assert.assertEquals(isStopPushSwitch(), true);
  }
}
