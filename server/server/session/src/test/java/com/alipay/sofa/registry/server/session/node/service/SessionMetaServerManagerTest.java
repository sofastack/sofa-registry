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
package com.alipay.sofa.registry.server.session.node.service;

import com.alipay.sofa.registry.server.session.TestUtils;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import org.junit.Assert;
import org.junit.Test;

public class SessionMetaServerManagerTest {
  private SessionMetaServerManager sessionMetaServerManager;
  private SessionServerConfig cfg;

  private void init() {
    sessionMetaServerManager = new SessionMetaServerManager();
    cfg = TestUtils.newSessionConfig("testDc");
    sessionMetaServerManager.setSessionServerConfig(cfg);
  }

  @Test
  public void testConfig() {
    init();
    Assert.assertEquals(1, sessionMetaServerManager.getConnNum());
    Assert.assertEquals(cfg.getMetaServerPort(), sessionMetaServerManager.getServerPort());
    Assert.assertEquals(
        cfg.getMetaNodeExchangeTimeoutMillis(), sessionMetaServerManager.getRpcTimeoutMillis());
  }
}
